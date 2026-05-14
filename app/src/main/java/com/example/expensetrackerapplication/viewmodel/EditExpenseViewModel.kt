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
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.model.PaymentType
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.SettingsViewModel.UiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditExpenseViewModel(
    application: Application,
    private  val logger: FileLogger) : AndroidViewModel(application = application)
{
    private lateinit var expenseRepository : ExpenseRepository

    init{
        var expenseDao = AppDatabase.getdatabase(application,logger)?.ExpenseDao()
        expenseDao?.let {
            expenseRepository = ExpenseRepository(expenseDao,logger)
        }
    }

    // Date
    var _selectedDate = MutableLiveData<String?>()
    var selectedDate : LiveData<String?> = _selectedDate

    var _selectedDateUi = MutableLiveData<String?>()
    var selectedDateUi : LiveData<String?> = _selectedDateUi

    // Expense
    var _expenseAmt = MutableLiveData<String?>("")
    var expenseAmt : LiveData<String?> = _expenseAmt

    // Category
    var _selectedCategoryId = MutableLiveData<Int>(-1)
    var selectedCategoryId : LiveData<Int> = _selectedCategoryId

    var _selectedCategoryName = MutableLiveData<String?>("")
    var selectedCategoryName : LiveData<String?> = _selectedCategoryName

//    var _paymentType = MutableLiveData<Int>(-1)
//    var paymentType : LiveData<Int> = _paymentType

    // Payment type selected in RadioGroup
    val _selectedPaymentType = MutableLiveData<Int?>(-1)   // IMPORTANT: initially null
    val selectedPaymentType: LiveData<Int?> = _selectedPaymentType
    // Assign Expense For Selected Payment Type
    var _selectedPaymentTypeAmt = MutableLiveData<PaymentType>()
    var selectedPaymentTypeAmt : LiveData<PaymentType> = _selectedPaymentTypeAmt

    // Remarks
    var _remarks = MutableLiveData<String?>("")
    var remarks : LiveData<String?> = _remarks

//    var _amtInCash = MutableLiveData<Float>(0.0f)
//    var amtInCash : LiveData<Float> = _amtInCash
//
//    var _amtInCard = MutableLiveData<Float>(0.0f)
//    var amtInCard : LiveData<Float> = _amtInCard
//
//    var _amtInUpi = MutableLiveData<Float>(0.0f)
//    var amtInUpi : LiveData<Float> = _amtInUpi
//    var _amtInOthers = MutableLiveData<Float>(0.0f)
//    var amtInOthers : LiveData<Float> = _amtInOthers

//    var _valueMissingError = MutableLiveData<String?>()
//    var valueMissingError : LiveData<String?> = _valueMissingError
    
    // Edit Expense Status
//    var _editStatus = MutableLiveData<Boolean>()
//    var editStatus : LiveData<Boolean> = _editStatus
    // Edit Expense Falg Value
//    var _editFlag = MutableLiveData<Int>(0)
//    var editFlag : LiveData<Int> = _editFlag

   // Show Split Payment Type Dialog Screen
    var _showSplitDialog = MutableLiveData<Boolean>()
    var showSplitDialog : LiveData<Boolean> = _showSplitDialog
    
    // Clear All Fields Value
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
//
//    val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

//    var _fireStoreCloudId = MutableLiveData<String>()
//    var fireStoreCloudId : LiveData<String> = _fireStoreCloudId

    // Close The Edit Expense Screen
    var _isClosed = MutableLiveData<Boolean>(false)
    var isClosed : LiveData<Boolean> = _isClosed

    // Expense List
    var _expenseList = MutableLiveData<List<ExpenseEntity>>()
    var expenseList : LiveData<List<ExpenseEntity>> = _expenseList

    // Id For Edited Expense
    var _editedExpenseId = MutableLiveData<Int>()
    var editedExpenseId : LiveData<Int> = _editedExpenseId

    // Date For Edited Expense
    var _editedExpenseDate = MutableLiveData<String>()
    var editedExpenseDate : LiveData<String> = _editedExpenseDate

//    var _deleteExpenseStatus = MutableLiveData<Boolean>()
//    var deleteExpenseStatus : LiveData<Boolean> = _deleteExpenseStatus
    
    // Add Expense To Db Flag
    var _insertFlag = MutableLiveData<Int>(0)
    var insertFlag : LiveData<Int> = _insertFlag

    // Edit Expense Status
    var _expenseInsertStatus = MutableLiveData<ResultState1>()
    var expenseInsertStatus : LiveData<ResultState1> = _expenseInsertStatus

    val LOG_TAG = "EDIT_EXPENSE_VIEW_MODEL"


    var _uiEvent = MutableSharedFlow<UiEvent>()
    var uiEvent : SharedFlow<UiEvent> = _uiEvent.asSharedFlow()


    fun onClickEdit(){
        viewModelScope.launch {
            try
            {
                fnDeleteExpense(editedExpenseId.value,editedExpenseDate.value)

            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Delete Expense1: ${e.message}")
                Log.e("EDIT_EXPENSE_VIEW_MODEL","Delete Expense1: ${e.message}")
            }
        }
    }
    fun fnInsertExpense()
    {
        try {
            viewModelScope.launch {
                _insertFlag.value=1
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
                    expenseRemarks = remarks.value ?: "",
                    expenseStatus = Global.EXPENSE_STATUS_ADDED,
                    cloudId = Global.cloudUserId ?:"",
                    isSynced = 0
                )
                var result = withContext(Dispatchers.IO){
                    expenseRepository.fnInsertExpenseDatasToDb(expenseEntity)
                }
                if(result)
                {
                    _insertFlag.postValue( 0)
                    _expenseInsertStatus.postValue(ResultState1.success(R.string.newEx_InsertExpenseSuccess))
                    onClickClear()
                    logger.logInfo(LOG_TAG,"Expense successfully Stored")
                    _uiEvent.emit(UiEvent.RecreateActivity)

                }
                else
                {
                    _insertFlag.postValue(0)
                    _expenseInsertStatus.postValue(ResultState1.fail(R.string.newEx_InsertExpenseFailed))
                    onClickClear()
                    logger.logError(LOG_TAG,"Store Expense Failed")
                }
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Insert Expense: ${e.message}")
            _expenseInsertStatus.postValue(ResultState1.fail(R.string.newEx_InsertExpenseFailed))
            Log.e("EDIT_EXPENSE_VIEW_MODEL","Insert Expense: ${e.message}")
        }
    }

    fun onClickBack()
    {
        try
        {
            _isClosed.value = true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"CLose Edit Expense Screen: ${e.message}")
            Log.e("EDIT_EXPENSE_VIEW_MODEL","CLose Edit Expense Screen: ${e.message}")
        }
    }

    fun onClickClear()
    {
        try {
            _clearAllFields.value=true

            _selectedDate.value=Global.fnGetCurrentDate(logger)
            _selectedDateUi.value=Global.fnGetCurrentDateUi(logger)

            _expenseAmt.value=""
            _selectedCategoryId.value=-1
            _selectedCategoryName.value=""
//        _paymentType.value= -1
            _selectedPaymentType.value= -1
            _remarks.value=""

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
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Clear All Fields Value: ${e.message}")
            Log.e("EDIT_EXPENSE_VIEW_MODEL","Clear All Fields Value: ${e.message}")
        }
    }

    fun fnCashPayment(){
        try
        {
            _selectedPaymentType.value= R.id.idCashPayment
            val paymentType = PaymentType(
                cash = Global.fnFormatFloatTwoDigits(expenseAmt.value?.toFloat(),logger).toFloatOrNull() ?:0.0f,
                card = 0.0f,
                upi = 0.0f,
                others = 0.0f
            )
            _selectedPaymentTypeAmt.value = paymentType
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Cash Payment: ${e.message}")
            Log.e("EDIT_EXPENSE_VIEW_MODEL","Cash Payment: ${e.message}")
        }
    }
    fun fnSplitPayment(){
        try
        {
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
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Split Payment: ${e.message}")
            Log.e("SPLIT_PAYMENT","Split Payment: ${e.message}")
        }
    }
    fun fnCardPayment(){
        try
        {
            _selectedPaymentType.value= R.id.idCardPayment
            val paymentType = PaymentType(
                cash = 0.0f,
                card = Global.fnFormatFloatTwoDigits(expenseAmt.value?.toFloat(),logger).toFloatOrNull()  ?:0.0f,
                upi = 0.0f,
                others = 0.0f
            )
            _selectedPaymentTypeAmt.value = paymentType
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Card Payment: ${e.message}")
            Log.e("EDIT_EXPENSE_VIEW_MODEL","Card Payment: ${e.message}")
        }
    }

    fun fnUpiPayment(){
        try {
            _selectedPaymentType.value= R.id.idUpiPayment
            val paymentType = PaymentType(
                cash = 0.0f,
                card = 0.0f,
                upi = Global.fnFormatFloatTwoDigits(expenseAmt.value?.toFloat(),logger).toFloatOrNull()  ?:0.0f,
                others = 0.0f
            )
            _selectedPaymentTypeAmt.value = paymentType
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Upi Payment: ${e.message}")
            Log.e("EDIT_EXPENSE_VIEW_MODEL","Upi Payment: ${e.message}")
        }
    }

    fun fnOtherPayment()
    {
        try {
            _selectedPaymentType.value= R.id.idOthersPayment
            val paymentType = PaymentType(
                cash = 0.0f,
                card = 0.0f,
                upi = 0.0f,
                others = Global.fnFormatFloatTwoDigits(expenseAmt.value?.toFloat(),logger).toFloatOrNull()  ?:0.0f
            )
            _selectedPaymentTypeAmt.value = paymentType
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Other Payment: ${e.message}")
            Log.e("EDIT_EXPENSE_VIEW_MODEL","Other Payment: ${e.message}")
        }
    }

    fun fnGetExpenseDetailsPerId(expenseId: Int?)
    {
        viewModelScope.launch {
            try{
                var list = withContext(Dispatchers.IO){
                    expenseRepository.fnGetExpenseDetails(expenseId)
                }
                if(list.isNotEmpty())
                {
                    _expenseList.postValue(list)
                }
                else
                {
                    _expenseList.postValue(emptyList<ExpenseEntity>())
                }
            }
            catch(e : Exception)
            {
                _expenseList.postValue(emptyList<ExpenseEntity>())
                logger.logError(LOG_TAG,"Get Expense Details Per Id: ${e.message}")
                Log.e("EDIT_EXPENSE_VIEW_MODEL","Get Expense Details Per Id: ${e.message}")
            }
        }
    }
    fun fnDeleteExpense(expenseId: Int?,expenseDate: String?)
    {
        viewModelScope.launch {
            try
            {
                when{
                    expenseAmt.value.isNullOrBlank() &&
                    selectedCategoryId.value ==-1 &&
                    selectedCategoryName.value.isNullOrBlank() &&
                    selectedPaymentType.value==-1 -> {
                        _expenseInsertStatus.value = ResultState1.fail(R.string.newEx_AllFieldsAreEmpty)
                    }

                    selectedDate.value.isNullOrBlank() -> {
                        _expenseInsertStatus.value = ResultState1.fail(R.string.newEx_DateMissing)
                    }

                    expenseAmt.value.isNullOrBlank() || expenseAmt.value.equals(0f.toString() ) -> {
                        _expenseInsertStatus.value = ResultState1.fail(R.string.newEx_ExpenseAmountMissing)
                    }

                    selectedCategoryId.value ==-1 && selectedCategoryName.value.isNullOrBlank() -> {
                        _expenseInsertStatus.value = ResultState1.fail(R.string.newEx_CategoryMissing)
                    }
                    selectedPaymentType.value == -1 -> {
                        _expenseInsertStatus.value = ResultState1.fail(R.string.newEx_PaymentTypeMissing)
                    }

                    else -> {
                        if(insertFlag.value==0)
                        {
                            var delRes= withContext(Dispatchers.IO){
                                expenseRepository.fnDeleteExpense(expenseId,expenseDate)
                            }
                            if(delRes ==  true)
                            {
                                fnInsertExpense()
                            }
                            else
                            {
                                logger.logError(LOG_TAG,"Delete Expense Failed")
                                Log.e("EDIT_EXPENSE_VIEW_MODEL","Delete Expense Failed")
                            }
                        }
                    }

                }
            }
            catch(e : Exception)
            {
                logger.logError(LOG_TAG,"Delete Expense2: ${e.message}")
                Log.e("EDIT_EXPENSE_VIEW_MODEL","Delete Expense2: ${e.message}")
            }
        }

    }


}