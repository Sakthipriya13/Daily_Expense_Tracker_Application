package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.data.logger.FileLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(application: Application, logger: FileLogger) : AndroidViewModel(application)
{

    private val _navigateToLogin = MutableStateFlow(false)

    val navigateToLogin = _navigateToLogin.asStateFlow()

    var _cloudUserId = MutableLiveData<String>()
    var cloudUserId : LiveData<String> = _cloudUserId

    // The code inside the init block runs automatically when the viewmodel is created
    init {
        viewModelScope.launch {
            delay(2500)
            _navigateToLogin.value=true
        }
    }


}