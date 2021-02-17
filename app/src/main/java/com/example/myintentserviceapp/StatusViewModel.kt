package com.example.myintentserviceapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myintentserviceapp.network.Smb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StatusViewModel(application: Application) : AndroidViewModel(application) {
    private var _isSmbWrite: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().also { mutableLiveData ->
            mutableLiveData.value = false
        }
    val isSmbWrite: LiveData<Boolean>
        get() = _isSmbWrite

    private val _isSmbWriteText: MutableLiveData<String> =
        MutableLiveData<String>().also { mutableLiveData ->
            mutableLiveData.value = "Ready"
        }
    val isSmbWriteText: LiveData<String>
        get() = _isSmbWriteText

    init {
        load(application)
    }

    private fun load(application: Application) {
        viewModelScope.launch(Dispatchers.IO) {
            var smb = Smb(application)
            var check = smb.checkPermissionWrite()
            if(check){
                _isSmbWriteText.postValue("smb ok!")
            } else{
                _isSmbWriteText.postValue("smb ng!")
            }
            _isSmbWrite.postValue(smb.checkPermissionWrite())
        }
    }
}