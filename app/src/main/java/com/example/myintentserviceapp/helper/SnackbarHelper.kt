package com.example.myintentserviceapp.helper

import android.app.Activity
import android.content.Intent
import android.view.View
import com.example.myintentserviceapp.R
import com.google.android.material.snackbar.Snackbar

object SnackbarHelper {
    fun <T> withStartActivity(view: View, activity: Activity, clazz: Class<T>, msgId: Int) {
        var snackbar =
            Snackbar.make(view, msgId, Snackbar.LENGTH_INDEFINITE)
        withStartActivity(activity, clazz, snackbar)
    }

    fun <T> withStartActivity(view: View, activity: Activity, clazz: Class<T>, msg: String) {
        var snackbar =
            Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE)
        withStartActivity(activity, clazz, snackbar)
    }

    fun <T> withStartActivity(activity: Activity, clazz: Class<T>, snackbar: Snackbar) {
        snackbar.setAction(
            R.string.open
        ) { view ->
            val intent = Intent(activity, clazz)
            activity.startActivity(intent)
        }
        snackbar.show()
    }
}