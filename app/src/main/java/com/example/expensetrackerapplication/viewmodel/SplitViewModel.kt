package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.utils.ResultState1

class SplitViewModel(
    application: Application,
    private val logger: FileLogger) : AndroidViewModel(application = application) {
    var expenseRepository : ExpenseRepository

    init {

        var expenseDao = AppDatabase.getdatabase(application).ExpenseDao()
        expenseRepository = ExpenseRepository(expenseDao)

    }

    // Total Amount Variable
    var _totAmt = MutableLiveData<String?>("0.00f")
    var totAmt : LiveData<String?> = _totAmt

    // Total Amount Variable
    var _totAmtUi = MutableLiveData<String?>("Tot: 0.00f")
    var totAmtUi : LiveData<String?> = _totAmtUi

    // Amount In Cash
    var _amtInCash = MutableLiveData<String?>("0.00")
    var amtInCash : LiveData<String?> = _amtInCash

    // Amount In Card
    var _amtInCard = MutableLiveData<String?>("0.00")
    var amtInCard : LiveData<String?> = _amtInCard

    //Amount In Upi
    var _amtInUpi = MutableLiveData<String?>("0.00")
    var amtInUpi : LiveData<String?> = _amtInUpi
    
    // Close The Split Dialog
    var _isClosed = MutableLiveData<Boolean>()
    var isClosed : LiveData<Boolean> = _isClosed

    // Ok Button Variable
    var _okSplit = MutableLiveData<String>()
    var okSplit : LiveData<String> = _okSplit

    // Split Status
    var _splitStatus = MutableLiveData<ResultState1>()
    var splitStatus : LiveData<ResultState1> = _splitStatus


    val LOG_TAG ="SPLIT_VIEW_MODEL"

    fun onClickCancel()
    {
        try
        {
            _isClosed.value = true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"On Click Cancel: ${e.message}")
        }
    }

    fun onClickOk()
    {
        try
        {
            val cashAmt = amtInCash.value?.toFloatOrNull() ?: 0f
            val cardAmt = amtInCard.value?.toFloatOrNull() ?: 0f
            val upiAmt  = amtInUpi.value?.toFloatOrNull() ?: 0f

            val amt = cashAmt + cardAmt + upiAmt

            if(amt != (totAmt.value?.toFloatOrNull() ?:0f))
            {
                _splitStatus.value = ResultState1.fail(R.string.split_TotalAmtMissMatch)
            }

            _okSplit.value = totAmt.toString()

        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"On Click Ok: ${e.message}")
        }
    }

    fun fnUpdateTotalAmtFromCash()
    {
        try
        {
            val total = totAmt.value?.toFloatOrNull() ?: 0f
            val cash = amtInCash.value?.toFloatOrNull() ?: 0f

            val remaining = total - cash

            _amtInCard.value = if (remaining > 0) remaining.toString() else "0.00"

        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Update Total Amount From Cash; ${e.message}")
        }
    }

    fun fnUpdateTotalAmtFromCard()
    {
        try
        {
            val total = totAmt.value?.toFloatOrNull() ?: 0f
            val cash = amtInCash.value?.toFloatOrNull() ?: 0f
            val card = amtInCard.value?.toFloatOrNull() ?: 0f

            var remaining = total-cash-card

            _amtInUpi.value = if (remaining > 0) remaining.toString() else "0.00"
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Update Total Amount From Card: ${e.message}")
        }
    }


}