package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.utils.ResultState
import kotlin.math.log

class DeletePromptViewModel(
    application: Application,
    private val logger : FileLogger
) : AndroidViewModel(application = application)
{
    // Title Variable Initialization
    var _title = MutableLiveData<String>()
    var title : LiveData<String> = _title

    // Message Variable Initialization
    var _message = MutableLiveData<String>()
    var message : LiveData<String> = _message

    val LOG_TAG = "DELETE_PROMPT_VIEW_MODEL"

    var _isClose = MutableLiveData<ResultState>()
    var isClose : LiveData<ResultState> = _isClose

    fun onClickCancel()
    {
        try
        {
            _isClose.value = ResultState.Success
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"On Click Cancel: ${e.message}")
        }
    }

    fun onClickOk()
    {
        try {
            _isClose.value = ResultState.fail
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"On Click Ok: ${e.message}")
        }
    }
}