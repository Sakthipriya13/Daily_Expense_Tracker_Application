package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.entity.ExpenseEntity
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.data.logger.Logger
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.model.PaymentType
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.launch

class NewExpenseViewModel(
    application: Application,
    logger: FileLogger
) : AndroidViewModel(application)
{
    //Expense Repository Variable
    var expenseRepository : ExpenseRepository

    init{
        var expenseDao = AppDatabase.getdatabase(application).ExpenseDao()
        expenseRepository = ExpenseRepository(expenseDao)
    }

    //Date
    var _selectedDate = MutableLiveData<String?>(Global.fnGetCurrentDate() )
    var selectedDate : LiveData<String?> = _selectedDate

    var _selectedDateUi = MutableLiveData<String?>(Global.fnGetCurrentDateUi() )
    var selectedDateUi : LiveData<String?> = _selectedDateUi

    //ExpenseAmount
    var _expenseAmt = MutableLiveData<String?>("")
    var expenseAmt : LiveData<String?> = _expenseAmt

    //Category Id
    var _selectedCategoryId = MutableLiveData<Int>(-1)
    var selectedCategoryId : LiveData<Int> = _selectedCategoryId

    //Category Name
    var _selectedCategoryName = MutableLiveData<String?>("")
    var selectedCategoryName : LiveData<String?> = _selectedCategoryName

    //Payment Type Id
//    var _paymentType = MutableLiveData<Int>(-1)
//    var paymentType : LiveData<Int> = _paymentType

    // Payment type selected in RadioGroup
    val _selectedPaymentType = MutableLiveData<Int?>(-1)   // IMPORTANT: initially null
    val selectedPaymentType: LiveData<Int?> = _selectedPaymentType

    //Remarks
    var _expenseRemarks = MutableLiveData<String?>("")
    var expenseRemarks : LiveData<String?> = _expenseRemarks

    //Payment Type
    var _selectedPaymentTypeAmt = MutableLiveData<PaymentType>()
    var selectedPaymentTypeAmt : LiveData<PaymentType> = _selectedPaymentTypeAmt

    //Amount In Cash
//    var _amtInCash = MutableLiveData<Float>(0.0f)
//    var amtInCash : LiveData<Float> = _amtInCash

    //Amount In Card
//    var _amtInCard = MutableLiveData<Float>(0.0f)
//    var amtInCard : LiveData<Float> = _amtInCard

    //Amount In Upi
//    var _amtInUpi = MutableLiveData<Float>(0.0f)
//    var amtInUpi : LiveData<Float> = _amtInUpi

    //Amount In Others
//    var _amtInOthers = MutableLiveData<Float>(0.0f)
//    var amtInOthers : LiveData<Float> = _amtInOthers

//    var _valueMissingError = MutableLiveData<String?>()
//    var valueMissingError : LiveData<String?> = _valueMissingError

    // New Expense Insert Status
    var _newExpenseInsertStatus = MutableLiveData<ResultState1>()
    var newExpenseInsertStatus : LiveData<ResultState1> = _newExpenseInsertStatus

    // Display Split Dialog Flag
    var _showSplitDialog = MutableLiveData<Boolean>()
    var showSplitDialog : LiveData<Boolean> = _showSplitDialog

    // Insert Flag Variable
    var _insertFlag = MutableLiveData<Int>(0)
    var insertFlag : LiveData<Int> = _insertFlag

    //Clear All Fields
    var _clearAllFields = MutableLiveData<Boolean>()
    var clearAllFields : LiveData<Boolean> = _clearAllFields

//    var _bothFieldsEmptyError = MutableLiveData<String?>()
//    var bothFieldsEmptyError : LiveData<String?> = _bothFieldsEmptyError
//
//    var _amtFieldsEmptyError = MutableLiveData<String?>()
//    var amtFieldsEmptyError : LiveData<String?> = _amtFieldsEmptyError
//
//    var _paymentFieldsEmptyError = MutableLiveData<String?>()
//    var paymentFieldsEmptyError : LiveData<String?> = _paymentFieldsEmptyError
//
//    var _cateFieldsEmptyError = MutableLiveData<String?>()
//    var cateFieldsEmptyError : LiveData<String?> = _cateFieldsEmptyError

//    val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

//    var _fireStoreCloudId = MutableLiveData<String>()
//    var fireStoreCloudId : LiveData<String> = _fireStoreCloudId

    fun fnClearAllFieldsValue()
    {
        try {
            _clearAllFields.value=true

            _selectedDate.value=Global.fnGetCurrentDate()
            _selectedDateUi.value=Global.fnGetCurrentDateUi()

            _expenseAmt.value=""
            _selectedCategoryId.value=-1
            _selectedCategoryName.value=""
//        _paymentType.value= -1
            _selectedPaymentType.value= -1
            _expenseRemarks.value=""

//        _amtInUpi.value=0.0f
//        _amtInCard.value=0.0f
//        _amtInCash.value=0.0f
//        _amtInOthers.value=0.0f

            var paymentType = PaymentType(
                cash = 0.0f,
                card = 0.0f,
                upi = 0.0f,
                others = 0.0f
            )
            _selectedPaymentTypeAmt.value =  paymentType
        }
        catch (e: Exception){
            Log.e("CLEAR_ALL_FIELDS","Clear All Fields: ${e.message}")
        }
    }

    fun fnAddExpenseToDb()
    {
        try {
            //Log.i("PAYMENT TYPE VALUE","Payment Type Value: ${paymentType.value}")
            when{
                expenseAmt.value.isNullOrBlank() &&
                        selectedCategoryId.value ==-1 &&
                        selectedCategoryName.value.isNullOrBlank() &&
//            paymentType.value== -1 &&
                        selectedPaymentType.value==-1 -> {
                    _newExpenseInsertStatus.value = ResultState1.fail(R.string.newEx_AllFieldsAreEmpty)
                }

                selectedDate.value.isNullOrBlank() -> {
                    _newExpenseInsertStatus.value = ResultState1.fail(R.string.newEx_DateMissing)
                }

                expenseAmt.value.isNullOrBlank() || expenseAmt.value.equals(0f.toString() ) -> {
                    _newExpenseInsertStatus.value = ResultState1.fail(R.string.newEx_ExpenseAmountMissing)
                }

                selectedCategoryId.value ==-1 && selectedCategoryName.value.isNullOrBlank() -> {
                    _newExpenseInsertStatus.value = ResultState1.fail(R.string.newEx_CategoryMissing)
                }
//            paymentType.value == -1 &&
                selectedPaymentType.value == -1 -> {
                    _newExpenseInsertStatus.value = ResultState1.fail(R.string.newEx_PaymentTypeMissing)
                }

                else -> {
                    if(insertFlag.value==0)
                    {
                        fnInsertExpense()
                    }
                }
            }
        }
        catch (e : Exception){
            Log.e("ADD_EXPENSE_TO_DB","Add Expense To Db: ${e.message}")
        }
    }

    private fun fnInsertExpense()
    {
        try {
            _insertFlag.value=1
            viewModelScope.launch {
                var expenseEntity = ExpenseEntity(
                    expenseId =0,
                    userId = Global.lUserId,
                    expenseDate = selectedDate.value ?: "",
                    expenseAmt = expenseAmt.value?.toFloatOrNull() ?:0.0f,
                    expenseCategoryId = selectedCategoryId.value ?: -1,
                    expenseCategoryName  = selectedCategoryName.value ?: "",
                    paymentType =when(selectedPaymentType.value){
                        R.id.idCashPayment -> Global.PAYMENT_TYPE_CASH
                        R.id.idCardPayment -> Global.PAYMENT_TYPE_CARD
                        R.id.idUpiPayment -> Global.PAYMENT_TYPE_UPI
                        R.id.idOthersPayment -> Global.PAYMENT_TYPE_OTHER
                        R.id.idSplitPayment -> Global.PAYMENT_TYPE_SPLIT
                        else -> {
                            Global.PAYMENT_TYPE_CASH
                        }
                    } ,
                    expenseAmtInCash = selectedPaymentTypeAmt.value?.cash ?: 0.00f,
                    expenseAmtInCard = selectedPaymentTypeAmt.value?.card ?: 0.00f,
                    expenseAmtInUpi = selectedPaymentTypeAmt.value?.upi ?: 0.00f,
                    expenseAmtInOthers = selectedPaymentTypeAmt.value?.others ?: 0.00f,
                    expenseRemarks = expenseRemarks.value ?: "",
                    expenseStatus = Global.EXPENSE_STATUS_ADDED,
                    cloudId = Global.cloudUserId ?:"",
                    isSynced = 0
                )

                var result = expenseRepository.fnInsertExpenseDatasToDb(expenseEntity)
                if(result)
                {
                    _insertFlag.value = 0
                    _newExpenseInsertStatus.value = ResultState1.success(R.string.newEx_InsertExpenseSuccess)
                    fnClearAllFieldsValue()
                }
                else
                {
                    _insertFlag.value = 0
                    _newExpenseInsertStatus.value = ResultState1.fail(R.string.newEx_InsertExpenseFailed)
                    fnClearAllFieldsValue()
                }
            }
        }
        catch (e : Exception){
            _newExpenseInsertStatus.value = ResultState1.fail(R.string.newEx_InsertExpenseFailed)
            Log.e("INSERT_EXPENSE","Insert_Expense: ${e.message}")
        }
    }

    // Set default ONLY during the very first fragment open
//    fun applyDefaultIfFirstOpen() {
//        if (_selectedPaymentType.value == null || _selectedPaymentType.value != R.id.idCashPayment) {  // only once
//            _selectedPaymentType.value = R.id.idCashPayment
//            _paymentType.value = Global.PAYMENT_TYPE_CASH
//        }
//    }

    fun fnCashPayment(){
//        _paymentType.value=Global.PAYMENT_TYPE_CASH
//        _amtInCash.value = expenseAmt.value?.toFloatOrNull() ?:0.0f
//        Log.v("PAYMENT TYPE","Payment Type: CASH")
//        Log.v("DEFAULT PAYMENT TYPE","Default Payment Type: ${amtInCash.value}")
        try {
            _selectedPaymentType.value= R.id.idCashPayment
            val paymentType = PaymentType(
                cash = Global.fnFormatFloatTwoDigits(expenseAmt.value?.toFloat()).toFloatOrNull() ?:0.0f,
                card = 0.0f,
                upi = 0.0f,
                others = 0.0f
            )
            _selectedPaymentTypeAmt.value = paymentType
        }
        catch (e : Exception){
            Log.e("CASH_PAYMENT","Cash Payment: ${e.message}")
        }
    }
    fun fnSplitPayment(){
//        _paymentType.value=Global.PAYMENT_TYPE_SPLIT
        try {
            if(expenseAmt.value.isNullOrBlank())
            {
                _showSplitDialog.value = false
                _selectedPaymentType.value = -1
            }
            else
            {
                _selectedPaymentType.value= R.id.idSplitPayment
                _showSplitDialog.value = true
            }
        }
        catch (e : Exception){
            Log.e("SPLIT_PAYMENT","Split Payment: ${e.message}")
        }
    }
    fun fnCardPayment(){
//        _paymentType.value=Global.PAYMENT_TYPE_CARD
//        _amtInCard.value =expenseAmt.value?.toFloatOrNull() ?:0.0f
//        Log.v("PAYMENT TYPE","Payment Type: CARD")
        try {
            _selectedPaymentType.value= R.id.idCardPayment
            val paymentType = PaymentType(
                cash = 0.0f,
                card = Global.fnFormatFloatTwoDigits(expenseAmt.value?.toFloat()).toFloatOrNull() ?:0.0f,
                upi = 0.0f,
                others = 0.0f
            )
            _selectedPaymentTypeAmt.value = paymentType
        }
        catch (e : Exception){
            Log.e("CARD_PAYMENT","Card Payment: ${e.message}")
        }
    }

    fun fnUpiPayment(){
//        _paymentType.value=Global.PAYMENT_TYPE_UPI
//        _amtInUpi.value =expenseAmt.value?.toFloatOrNull() ?:0.0f
//        Log.v("PAYMENT TYPE","Payment Type: UPI")
        try {
            _selectedPaymentType.value= R.id.idUpiPayment
            val paymentType = PaymentType(
                cash = 0.0f,
                card = 0.0f,
                upi = Global.fnFormatFloatTwoDigits(expenseAmt.value?.toFloat()).toFloatOrNull() ?:0.0f,
                others = 0.0f
            )
            _selectedPaymentTypeAmt.value = paymentType
        }
        catch (e : Exception){
            Log.e("UPI_PAYMENT","Upi Payment: ${e.message}")
        }
    }

    fun fnOtherPayment(){
//         _paymentType.value=Global.PAYMENT_TYPE_OTHER
//        _amtInOthers.value =expenseAmt.value?.toFloatOrNull() ?:0.0f
//        Log.v("PAYMENT TYPE","Payment Type: OTHER")
        try {
            _selectedPaymentType.value= R.id.idOthersPayment
            val paymentType = PaymentType(
                cash = 0.0f,
                card = 0.0f,
                upi = 0.0f,
                others = Global.fnFormatFloatTwoDigits(expenseAmt.value?.toFloat()).toFloatOrNull() ?:0.0f
            )
            _selectedPaymentTypeAmt.value = paymentType
        }
        catch (e: Exception){
            Log.e("OTHER_PAYMENT","Other Payment: ${e.message}")
        }
    }

}