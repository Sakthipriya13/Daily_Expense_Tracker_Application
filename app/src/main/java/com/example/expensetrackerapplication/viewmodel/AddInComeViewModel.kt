package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.entity.IncomeEntity
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.IncomeRepository
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.launch

class AddInComeViewModel(
    application: Application,
    private val logger: FileLogger) : AndroidViewModel(application)
{
    val incomeRepository : IncomeRepository

    init{
        var incomeDao = AppDatabase.getdatabase(application).IncomeDao()
        incomeRepository = IncomeRepository(incomeDao,logger)
    }

    //Date Field
    var _selectedDate = MutableLiveData<String>(Global.fnGetCurrentDate())
    var selectedDate : LiveData<String> = _selectedDate

    var _selectedDateUi = MutableLiveData<String>(Global.fnGetCurrentDateUi())
    var selectedDateUi : LiveData<String> = _selectedDateUi

    //Income
    var _income = MutableLiveData<String>()
    var income : LiveData<String> = _income

    //Close Add Income Screen
    var _isClosed = MutableLiveData<Boolean>()
    var isClosed : LiveData<Boolean> = _isClosed

    //Add Income
    var _insertStatus = MutableLiveData<ResultState1>()
    var insertStatus : LiveData<ResultState1> = _insertStatus

//    var _firestoreCloudId = MutableLiveData<String>()
//    var firestoreCloudId : LiveData<String> = _firestoreCloudId

    val LOG_TAG ="ADD_INCOME_VIEW_MODEL"

    fun onClickCancel()
    {
        try {
            _isClosed.value = true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Close The Add Income Screen: ${e.message}")
            Log.e(LOG_TAG,"Close The Add Income Screen: ${e.message}")
        }
    }

    fun onClickSubmit()
    {
        try {
            when{
                selectedDate.value.isNullOrBlank() && income.value.isNullOrBlank() -> {
                    _insertStatus.value= ResultState1.fail(R.string.newEx_AllFieldsAreEmpty)
                }
                selectedDateUi.value.isNullOrBlank() ->{
                    _insertStatus.value= ResultState1.fail(R.string.newEx_DateMissing)
                }
                income.value.isNullOrBlank() -> {
                    _insertStatus.value= ResultState1.fail(R.string.income_IncomeFieldEmpty)
                }
                else -> {
                    fnInsertIncome()
                }
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"On Click Submit Button: ${e.message}")
        }
    }

    fun fnInsertIncome(){
        viewModelScope.launch {
            try
            {
                if(!selectedDate.value.isNullOrBlank() && !income.value.isNullOrBlank()){
                    var income = IncomeEntity(
                        incomeId = 0,
                        userId = Global.lUserId,
                        date = selectedDate.value,
                        income = income.value?.toFloatOrNull(),
                        cloudId = Global.cloudUserId ?:"",
                        isSynced = 0
                    )
                    var result = incomeRepository.fnInsertIncome(income)
                    if(result) {
                        _insertStatus.postValue(ResultState1.success(R.string.income_InsertIncomeSuccess))
                        logger.logInfo(LOG_TAG,"Insert Income Success")
                    }
                    else {
                        _insertStatus.postValue(ResultState1.fail(R.string.income_InsertIncomeFailed))
                        logger.logError(LOG_TAG,"Insert Income Failed")
                    }
                }
            }
            catch (e : Exception)
            {
                logger.logError(LOG_TAG,"Insert Income Status: ${e.message}")
                Log.e("INSERT INCOME STATUS","Insert Income Status: ${e.message}")
            }
        }
    }

    fun fnClearAllFields()
    {
        try{

            _selectedDate.value = Global.fnGetCurrentDate()
            _selectedDateUi.value = Global.fnGetCurrentDateUi()
            _income.value =""
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Clear All Fields Value: ${e.message}")
        }
    }
}