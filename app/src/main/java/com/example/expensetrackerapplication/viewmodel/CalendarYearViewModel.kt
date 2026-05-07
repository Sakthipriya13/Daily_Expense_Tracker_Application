package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expensetrackerapplication.logger.FileLogger

class CalendarYearViewModel(
    application: Application,
    private val logger: FileLogger): AndroidViewModel(application = application)
{
    var _selectedYear = MutableLiveData<String>()
    var selectedYear : LiveData<String> = _selectedYear

    var _isClose = MutableLiveData<Boolean>()
    var isClose : LiveData<Boolean> = _isClose

    var _isConfirm = MutableLiveData<Boolean>()
    var isConfirm : LiveData<Boolean> = _isConfirm

    val LOG_TAG = "CALENDAR_YEAR_VIEW_MODEL"

    fun onClickCancel()
    {
        try {
            _isClose.value = true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"On Click Cancel Button: ${e.message}")
        }
    }

    fun onClickOk()
    {
        try {
            _isConfirm.value = true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"On Click Ok Button: ${e.message}")
        }
    }
}