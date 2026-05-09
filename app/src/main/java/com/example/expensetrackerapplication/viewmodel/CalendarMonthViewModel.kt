package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.utils.ResultState

class CalendarMonthViewModel(
    application: Application,
    val logger: FileLogger): AndroidViewModel(application = application)
{
    var _selectedYear = MutableLiveData<String>()
    var selectedYear : LiveData<String> = _selectedYear

    var _isClose = MutableLiveData<Boolean>()
    var isClose : LiveData<Boolean> = _isClose

    var _isYearClicked = MutableLiveData<Boolean>()
    var isYearClicked : LiveData<Boolean> = _isYearClicked

    var _isYearSelected = MutableLiveData<ResultState>()
    var isYearSelected : LiveData<ResultState> = _isYearSelected

    val LOG_TAG = "CALENDAR_MONTH_VIEW_MODEL"

    fun onClickYear()
    {
        try
        {
            _isYearClicked.value = true
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"On Click Year: ${e.message}")
        }
    }

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

    fun isCloseYearCalendar()
    {
        try
        {
            _isYearSelected.value = ResultState.fail
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Is Close Year Calendar: ${e.message}")
        }
    }

    fun isYearConfirm()
    {
        try
        {
            _isYearSelected.value = ResultState.Success
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Is Year Selected: ${e.message}")
        }
    }

}