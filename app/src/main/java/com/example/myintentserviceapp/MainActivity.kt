package com.example.myintentserviceapp

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.myintentserviceapp.helper.SnackbarHelper
import com.example.myintentserviceapp.network.Smb
import com.example.myintentserviceapp.util.RuntimePermissionUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*


private const val NUM_PAGES = 2

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2

    private val EXTERNAL_STORAGE_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myToolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(myToolbar)

        viewPager = findViewById(R.id.pager)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter

        //バックアップフラグ確認
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean(SettingsActivity.KEY_SYNC, false)) {
            Log.i(TAG, "onCreate: backup on")
            //smb書込み権限確認
            //TODO GlobalScope見直し
            val errorHandler = CoroutineExceptionHandler { _, exception ->
                SnackbarHelper.withStartActivity(
                    this.findViewById(R.id.main_activity),
                    this,
                    SettingsActivity::class.java,
                    R.string.ask_config
                )
            }
            GlobalScope.launch(errorHandler) {
                val writePermission = withContext(Dispatchers.IO) {
                    val smb = Smb(application)
                    smb.checkPermissionWrite()
                }
                if (writePermission) {
                    Log.i(TAG, "onCreate: smb.canWrite true")
                } else {
                    Log.i(TAG, "onCreate: smb.canWrite false")
                    Toast.makeText(
                        this@MainActivity,
                        "NASの書込み許可をされないとアプリが実行できません\n権限をチェックしてください",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Log.i(TAG, "onCreate: backup off")
        }
        if (RuntimePermissionUtils.checkPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            MyIntentService.startActionLocal(this)
            MyIntentService.startActionSMB(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_sync -> {
                if (RuntimePermissionUtils.checkPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    MyIntentService.startActionLocal(this)
                    MyIntentService.startActionSMB(this)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 結果の受け取り
    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        //TODO 整理

        // requestPermissionsの引数に指定した値が、requestCodeで返却される
        if (requestCode != EXTERNAL_STORAGE_REQUEST_CODE) {
            return
        }
        // 自分がリクエストしたコードで戻ってきた場合
        // 使用が許可された
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // ローカルファイルの読み取り処理実行
            MyIntentService.startActionLocal(this)
            return
        }
        // 拒否されたが永続的ではない場合
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT).show()
            // パーミッションの取得を依頼
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                EXTERNAL_STORAGE_REQUEST_CODE
            )
            return
        }
        // 永続的に拒否された場合
        var snackbar =
            Snackbar.make(window.decorView, R.string.ask_config, Snackbar.LENGTH_INDEFINITE)

        val snackTextView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

        // expression lambda
        snackbar.setAction(
            "Reply"
        ) { view -> snackTextView.setText(R.string.ask_config) }
        snackbar.show()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    class ScreenSlidePagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> GridFragment()
            1 -> StatusFragment.newInstance()
            else -> GridFragment()
        }
    }

    companion object {
        const val BROADCAST_ACTION = "com.example.myintentserviceapp.MainActivity.broadcast"
        private const val TAG = "MainActivity"
    }
}