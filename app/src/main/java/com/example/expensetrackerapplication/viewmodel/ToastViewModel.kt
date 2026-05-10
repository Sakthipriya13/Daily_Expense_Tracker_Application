package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expensetrackerapplication.logger.FileLogger

class ToastViewModel(
    application: Application,
    private val logger: FileLogger
): AndroidViewModel(application = application)
{
    var _toastMsg = MutableLiveData<String>()
    var toastMsg : LiveData<String> = _toastMsg
}