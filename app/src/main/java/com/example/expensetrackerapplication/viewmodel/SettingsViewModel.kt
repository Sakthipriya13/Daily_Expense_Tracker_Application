package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.entity.CategoryEntitty
import com.example.expensetrackerapplication.data.entity.ExpenseEntity
import com.example.expensetrackerapplication.data.entity.IncomeEntity
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.CategoryRepository
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.data.repositary.IncomeRepository
import com.example.expensetrackerapplication.datastore.LanguageDataStore
import com.example.expensetrackerapplication.datastore.ThemeColorDataStore
import com.example.expensetrackerapplication.datastore.ThemeDataStore
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class SettingsViewModel(
    application: Application,
    private val logger: FileLogger) : AndroidViewModel(application)
{
    private lateinit var categoryRepository : CategoryRepository
    private lateinit var expenseRepository : ExpenseRepository
    private lateinit var incomeRepository : IncomeRepository
    val languageDataStore: LanguageDataStore
    val themeColorDataStore : ThemeColorDataStore
    val themeDataStore: ThemeDataStore

    init {
        var dao = AppDatabase.getdatabase(application,logger)?.CategoryDao()
        dao?.let {
            categoryRepository= CategoryRepository(dao,logger)
        }
        var exdao = AppDatabase.getdatabase(application,logger)?.ExpenseDao()
        exdao?.let {
            expenseRepository= ExpenseRepository(exdao,logger)
        }
        var indao = AppDatabase.getdatabase(application,logger)?.IncomeDao()
        indao?.let {
            incomeRepository= IncomeRepository(indao,logger)
        }

        languageDataStore= LanguageDataStore(application,logger)
        themeColorDataStore = ThemeColorDataStore(application,logger)
        themeDataStore = ThemeDataStore(application,logger)

    }

    //Category List
    var _categoryList = MutableLiveData<List<CategoryEntitty>>()
    val categoryList: LiveData<List<CategoryEntitty>> get() = _categoryList

    //New Category
    var _newCategory = MutableLiveData<String?>()
    val newCategory : LiveData<String?> = _newCategory

    //InsertCategoryStatus
    var _insertCategoryStatus = MutableLiveData<ResultState1?>(null)
    var insertCategoryStatus : LiveData<ResultState1?> = _insertCategoryStatus

    //Share Data Via Email Status
    var _shareDataStatus = MutableLiveData<ResultState1?>(null)
    var shareDataStatus : LiveData<ResultState1?> = _shareDataStatus

    //Delete Category Status
    var _deleteCategoryStatus = MutableLiveData<ResultState1>()
    var deleteCategoryStatus : LiveData<ResultState1> = _deleteCategoryStatus

    //ProgressBar Flag Value
    var _isLoading= MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

//    val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

//    var _firestoreCloudId = MutableLiveData<String>()
//    var firestoreCloudId : LiveData<String> = _firestoreCloudId

    //Send Email Event
    private val _sendEmailEvent = MutableLiveData<Intent?>(null)
    val sendEmailEvent: LiveData<Intent?> get() = _sendEmailEvent

    var _uiEvent = MutableSharedFlow<UiEvent>()
    var uiEvent : SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    var _internetStatus = MutableLiveData<ResultState1>()
    var internetStatus : LiveData<ResultState1> = _internetStatus

    var _btnThemeCode = MutableLiveData<Int>()
    var btnThemeCode : LiveData<Int> = _btnThemeCode

    val LOG_TAG = "SETTINGS_VIEW_MODEL"

//    var _uiEvent = MutableLiveData<UiEvent>()
//    var uiEvent : LiveData<UiEvent> = _uiEvent

    sealed class UiEvent{
        object RecreateActivity : UiEvent()
    }

    fun fnClearNewCategoryField()
    {
        try
        {
            _newCategory.value = ""
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Clear New Category Field Value: ${e.message}")
        }
    }

//    fun fnGetUnSyncedcategories(){
//        viewModelScope.launch {
//            var list : List<CategoryEntitty> = emptyList()
//            list= categoryRepository.fnGetUnSyncedCategoryDb()
//            Log.i("UNSYNCED CATGGORIES","UnSynced Categories: $list")
//        }
//    }

    fun  fnInsertCategories()
    {
        try
        {
            when{
                newCategory.value.isNullOrBlank() ->
                {
                    _insertCategoryStatus.value = ResultState1.fail(R.string.set_NewCategoryFieldEmpty)
                }
                else ->
                {
                    fnInsertNewCategory()
                }
            }
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"On Click Add Button: ${e.message}")
            Log.e("INSERT_NEW_CATEGORY","Insert New Category1: ${e.message}")
        }
    }

    fun fnInsertNewCategory()
    {
        viewModelScope.launch {
            try
            {
                var expenseDate = Global.fnGetCurrentDate(logger)
                var newCategory = CategoryEntitty(
                    userId = Global.lUserId,
                    cloudId = Global.cloudUserId ?:"",
                    isSynced = 0,
                    categoryId = 0,
                    signUpDate = expenseDate,
                    categoryName=newCategory.value
                )
                var result = withContext(Dispatchers.IO){
                    categoryRepository.fnInsertCategoriesToDb(
                        newCategory
                    )
                }

                if(result){
                    fnGetAllCategories()
                    fnClearNewCategoryField()
                    _insertCategoryStatus.postValue(ResultState1.success(R.string.set_InsertCategorySuccess))
                    logger.logInfo(LOG_TAG,"Category successfully stored")
                }
                else{
                    _insertCategoryStatus.postValue(ResultState1.fail(R.string.set_InsertCategoryFailed))
                    logger.logError(LOG_TAG,"Store Category Failed")
                }
            }
            catch (e : Exception)
            {
                _insertCategoryStatus.postValue(ResultState1.fail(R.string.somethingWrong))
                logger.logError(LOG_TAG,"Insert New Category: ${e.message}")
                Log.e("INSERT_NEW_CATEGORY","Insert New Category2: ${e.message}")
            }

        }

    }

//    fun fnGetDefaultCategories(){
//        viewModelScope.launch {
//            try {
//                var category_List=categoryRepository.fnGetDefaultCategoriesFromDb()
//
//                if(category_List.isNotEmpty()) {
//                    Log.v("CATEGORY LIST","Category List: $category_List")
//                }
//            }
//            catch (e : Exception)
//            {
//                Log.e("GET DEFAULT CATEGORIES FROM VIEW MODEL","Get Default Categories: $e.message")
//            }
//        }
//    }

    fun fnGetAllCategories(){
        viewModelScope.launch {
            try
            {
                var category_List= withContext(Dispatchers.IO){
                    categoryRepository.fnGetAllCategoriesFromDb()
                }
                if(category_List.isNotEmpty()){
                    _categoryList.postValue(category_List)
                }
                else{
                    _categoryList.postValue(emptyList<CategoryEntitty>())
                }
            }
            catch (e : Exception)
            {
                _categoryList.postValue(emptyList<CategoryEntitty>())
                logger.logError(LOG_TAG,"Get All Categories From Db: ${e.message}")
                Log.e("GET ALL CATEGORIES FROM DB","Get All Categories From Db: ${e.message}")
            }
        }
    }

    fun fnDeleteCategory(categoryId: Int?, userId: Int?)
    {
        viewModelScope.launch {
            try {
                var status = withContext(Dispatchers.IO){
                    categoryRepository.fnDeleteCategory(categoryId,userId)
                }
                if(status) {
                    fnGetAllCategories()
                    _deleteCategoryStatus.postValue(ResultState1.success(R.string.set_DeleteCategorySuccess))
                }
                else
                    _deleteCategoryStatus.postValue(ResultState1.fail(R.string.set_DeleteCategoryFailed))
            }
            catch (e: Exception)
            {
                _deleteCategoryStatus.postValue(ResultState1.fail(R.string.somethingWrong))
                logger.logError(LOG_TAG,"Delete Category: ${e.message}")
                Log.e("DELETE_CATEGORY","Delete Category: ${e.message}")
            }
        }
    }

    fun onClickExport()
    {
        viewModelScope.launch {
            try{

                Log.i(LOG_TAG,"Start Export1")

                _isLoading.value = true

                Log.i(LOG_TAG,"Cloud user ID: ${Global.cloudUserId}")

                val isNetworkAvail = Global.isNetworkAvailable(application,logger)

                if(isNetworkAvail)
                {
                    val files = ArrayList<Uri>()

                    val cateList = withContext(Dispatchers.IO){
                        categoryRepository.fnGetCategoriesFromCloud()
                    }
                    val expenseList = withContext(Dispatchers.IO){
                        expenseRepository.fnGetExpensesFromCloud()
                    }
                    val incomeList = withContext(Dispatchers.IO){
                        incomeRepository.fnGetIncomesFromCloud()
                    }

                    if(cateList.isEmpty() && expenseList.isEmpty() &&
                        incomeList.isEmpty())
                    {
                        _shareDataStatus.postValue(ResultState1.fail(R.string.set_ShareData_NoData))
                        return@launch
                    }
                    if(cateList.isNotEmpty())
                    {
                        val csvFile : File? = fnConvertCateListToCsvFile(cateList)
                        csvFile?.let {
                            val uri1 = FileProvider.getUriForFile(
                                application,
                                application.packageName + ".provider",
                                csvFile
                            )
                            files.add(uri1)
                        }
                    }

                    if(expenseList.isNotEmpty())
                    {
                        val csvFile = fnConvertExpenseListToCsvFile(expenseList)
                        csvFile?.let {
                            val uri1 = FileProvider.getUriForFile(
                                application,
                                application.packageName + ".provider",
                                csvFile
                            )
                            files.add(uri1)
                        }

                    }

                    if(incomeList.isNotEmpty())
                    {
                        val csvFile = fnConvertIncomeListToCsvFile(incomeList)

                        csvFile?.let {
                            val uri1 = FileProvider.getUriForFile(
                                application,
                                application.packageName + ".provider",
                                csvFile
                            )
                            files.add(uri1)
                        }
                    }

                    if(files.size > 0)
                    {
                        Log.i(LOG_TAG,"Start Export2")
                        fnSendFilesViaEmail(files)
                    }
                    else
                    {
                        _shareDataStatus.postValue(ResultState1.fail(R.string.set_ShareData_NoData))
                        return@launch
                    }
                }
                else
                {
                    _shareDataStatus.postValue(ResultState1.fail(R.string.noInternet))
                }
            }
            catch (e : Exception)
            {
                _isLoading.value = false
                logger.logError(LOG_TAG,"Share Data: ${e.message}")
                Log.e("SHARE DATA","Share Data: ${e.message}")
                _shareDataStatus.postValue(ResultState1.fail(R.string.somethingWrong))
            }
        }
    }
    fun fnConvertCateListToCsvFile(list : List<CategoryEntitty>): File?
    {
        try
        {
            val file = File(application.cacheDir,"Category_Data.csv")

            var writer = FileWriter(file)

            writer.append("Date,CategoryName\n")

            for(cat in list){
                writer.append(
                    "${cat.signUpDate},${cat.categoryName}\n"
                )
            }

            writer.flush()
            writer.close()

            return file
        }
        catch (e: Exception)
        {
            _isLoading.value = false
            logger.logError(LOG_TAG,"Convert Category List To Csv File: ${e.message}")
            Log.e("CONVERT_CATEGORY_LIST_TO_CSV_FILE","Convert Category List To Csv File: ${e.message}")
            return null
        }
    }
    fun fnConvertExpenseListToCsvFile(list : List<ExpenseEntity>): File? {
        try
        {
            val file = File(application.cacheDir,"Expense_Data.csv")

            var writer = FileWriter(file)

            writer.append("ExpenseDate,CategoryName,Expense,PaymentType,ExpenseInCash,ExpenseInCard,ExpenseInUpi," +
                    "ExpenseInOthers,Remarks\n")

            for(cat in list){
                var paymentType = when(cat.paymentType){
                    2 -> "CARD"
                    3 -> "UPI"
                    4 -> "SPLIT"
                    else -> {
                        "CASH"
                    }
                }
                writer.append(
                    "${cat.expenseDate}," +
                            "${cat.expenseCategoryName}," +
                            "${cat.expenseAmt},$paymentType,${cat.expenseAmtInCash}," +
                            "${cat.expenseAmtInCard},${cat.expenseAmtInUpi},${cat.expenseAmtInOthers}," +
                            "${cat.expenseRemarks}\n"
                )
            }

            writer.flush()
            writer.close()

            return file
        }
        catch (e: Exception)
        {
            _isLoading.value = false
            logger.logError(LOG_TAG,"Convert Expense List To Csv File: ${e.message}")
            Log.e("CONVERT_CATEGORY_LIST_TO_CSV_FILE","Convert Expense List To Csv File: ${e.message}")
            return null
        }
    }
    fun fnConvertIncomeListToCsvFile(list : List<IncomeEntity>): File? {
        try {
            val file = File(application.cacheDir,"Income_Data.csv")

            var writer = FileWriter(file)

            writer.append("IncomeDate,Income\n")

            for(cat in list){
                writer.append(
                    "${cat.date},${cat.income}\n"
                )
            }

            writer.flush()
            writer.close()

            return file
        }
        catch (e: Exception)
        {
            _isLoading.value = false
            logger.logError(LOG_TAG,"Convert Income List To Csv File: ${e.message}")
            Log.e("CONVERT_CATEGORY_LIST_TO_CSV_FILE","Convert Income List To Csv File: ${e.message}")
            return null
        }
    }
    fun fnSendFilesViaEmail(file: ArrayList<Uri>){
        viewModelScope.launch {
            try
            {
                Log.i(LOG_TAG,"Start Export3")

                var isNetworkAvailable = Global.isNetworkAvailable(application,logger)

                if(isNetworkAvailable)
                {
                    Log.i(LOG_TAG,"Start Export4")

                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type ="text/csv"

                        putExtra(Intent.EXTRA_SUBJECT,"EXPENSE TRACKER")
                        putExtra(Intent.EXTRA_TEXT,"Open These Files In Excel")

                        putParcelableArrayListExtra(Intent.EXTRA_STREAM,file)

                        // Important for Gmail / Outlook
                        clipData = ClipData.newRawUri("", file[0])
                        file.drop(1).forEach { uri ->
                            clipData?.addItem(ClipData.Item(uri))
                        }

                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    _sendEmailEvent.value = Intent.createChooser(intent, "Send Email")
                }
                else
                {
                    Log.i(LOG_TAG,"Start Export5")
                    _internetStatus.value = ResultState1.fail(R.string.noInternet)
                }
            }
            catch (e: Exception)
            {
                _isLoading.value = false
                _internetStatus.value = ResultState1.fail(R.string.somethingWrong)
                logger.logError(LOG_TAG,"Send Data Via Email: ${e.message}")
                Log.e("SEND_FILE_VIA_EMAIL","Send Via Email: ${e.message}")
            }
        }
    }

    // Update Selected Language
    fun fnUpdateLan(langCode : String){
        viewModelScope.launch {
            try
            {
                val currLang = languageDataStore.fnGetLanguage()

                if(currLang == langCode){
                    _isLoading.value = false
                    return@launch
                }

                _isLoading.value = true

                languageDataStore.fnSaveLanguage(langCode)

                _insertCategoryStatus.value = null
                _shareDataStatus.value = null
                _sendEmailEvent.value = null

                _uiEvent.emit(UiEvent.RecreateActivity)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Update Language: ${e.message}")
                _isLoading.value = false
            }
            finally
            {
                _isLoading.value = false
            }
        }
    }

    //Update App Theme
    fun fnUpdateThemeColor(colorCode : Int) {
        viewModelScope.launch {
            try
            {
                val currColor = themeColorDataStore.fnGetThemeColor()

                if (currColor == colorCode) return@launch

                _isLoading.value = true

                themeColorDataStore.fnSaveThemeColor(colorCode)

                _insertCategoryStatus.value = null
                _shareDataStatus.value = null
                _sendEmailEvent.value = null

                _uiEvent.emit(UiEvent.RecreateActivity)

//                _uiEvent.value = UiEvent.RecreateActivity

//                _recreateActvity.value = true

            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Update Theme Color: ${e.message}")
                Log.e("UPDATE_THEME_COLOR","Update Theme Color: ${e.message}")
                _isLoading.value = false
            }
            finally {
                _isLoading.value = false
            }
        }
    }

    //Update Theme
    fun fnUpdateTheme(themeCode : Int){
        viewModelScope.launch {
            try
            {
                val curThemeCode = themeDataStore.fnGetTheme()

                if(curThemeCode == themeCode) return@launch

                _isLoading.value = true

                themeDataStore.fnSaveTheme(themeCode)

                _insertCategoryStatus.value = null
                _shareDataStatus.value = null
                _sendEmailEvent.value = null
                _btnThemeCode.value = themeCode

                when(themeCode){
                    Global.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                    )

                    Global.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )

                    else -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                }

                _isLoading.value = false
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Update Theme: ${e.message}")
                Log.e("UPDATE_THEME","Update Theme: ${e.message}")
                _isLoading.value = false
            }
            finally
            {
                _isLoading.value = false
            }
        }
    }
}