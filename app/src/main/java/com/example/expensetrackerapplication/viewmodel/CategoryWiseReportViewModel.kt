package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.model.CategoryChartModel
import com.example.expensetrackerapplication.utils.Global
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.application
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class CategoryWiseReportViewModel(
    application: Application,
    private val logger: FileLogger) : AndroidViewModel(application = application)
{
    // Expense Repository Variable Initialization
    private var expenseRepository : ExpenseRepository

    init{
        var expenseDao = AppDatabase.getdatabase(application).ExpenseDao()
        expenseRepository = ExpenseRepository(expenseDao)
    }

    // Date Variables Initialization
    var _selectedDate = MutableLiveData<String>(Global.fnGetCurrentDate())
    var selectedDate : LiveData<String> = _selectedDate

    var _selectedDateUi = MutableLiveData<String>(Global.fnGetCurrentDateUi())
    var selectedDateUi : LiveData<String> = _selectedDateUi

    // Category List Variable Initialization
    var _categoryList = MutableLiveData<List<CategoryChartModel>>(mutableListOf<CategoryChartModel>())
    var categoryList : LiveData<List<CategoryChartModel>> = _categoryList
    
    // Close The Category Report Variable Initialization
    var _isClosed = MutableLiveData<Boolean>()
    var isClosed : LiveData<Boolean> = _isClosed

    // ProgressBar Loading Status Variable Initialization
    var _isExportLoading = MutableLiveData<Boolean>(false)
    var isExportLoading : LiveData<Boolean> = _isExportLoading

    // Export Status Variable Initialization
    var _exportStatus = MutableLiveData<ResultState1>()
    var exportStatus : LiveData<ResultState1> = _exportStatus

    // Total Expense Summary Variable Initialization
    var _totalExpenseSummary = MutableLiveData<String>("0.00")
    var totalExpenseSummary : LiveData<String> = _totalExpenseSummary

    // Added Expense Summary Variable Initialization
    var _addedExpenseSummary = MutableLiveData<String>("0.00")
    var addedExpenseSummary : LiveData<String> = _addedExpenseSummary

    // Deleted Expense Summary Variable Initialization
    var _deletedExpenseSummary = MutableLiveData<String>("0.00")
    var deletedExpenseSummary : LiveData<String> = _deletedExpenseSummary

//    val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    val LOG_TAG = "CATEGORY_WISE_REPORT_VIEW_MODEL"
    fun fnGetCategoryDetailsPerDay(date : String?)
    {
        viewModelScope.launch {
            try
            {
                _isExportLoading.postValue(true)
                var res = expenseRepository.fnGetCateDetailsPerDay(date)
                var list : MutableList<CategoryChartModel> = mutableListOf()
                if(res.isNotEmpty())
                {
                    res.forEach { ob ->
                        list.add(
                            CategoryChartModel(
                                userId = ob.userId,
                                categoryId = ob.categoryId,
                                categoryName = ob.categoryName,
                                expenseAmt = ob.expenseAmt
                            )
                        )
                    }
                    _categoryList.postValue(list)
                }
                else
                {
                    _categoryList.postValue(mutableListOf<CategoryChartModel>())
                }
            }
            catch (ex : Exception)
            {
                logger.logError(LOG_TAG,"Get Category List Per Day: ${ex.message}")
                Log.e(LOG_TAG,"Get Category List Per Day: ${ex.message}")
            }
        }
    }

    fun isBack()
    {
        try
        {
            _isClosed.value = true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Close The Category Report Screen: ${e.message}")
            Log.e(LOG_TAG,"Close The Category Report Screen: ${e.message}")
        }
    }

    fun resetCloseState()
    {
        try
        {
            _isClosed.value = false
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Reset Close State: ${e.message}")
            Log.e(LOG_TAG, "Reset Close State: ${e.message}")
        }
    }

    fun fnExportCategoryList()
    {
        viewModelScope.launch {
            try
            {
                if(isExportLoading.value==false)
                {
                    _isExportLoading.postValue(true)

                    delay(1000L)

                    var start = Global.fnGetCurrentTime()

                    var workBook = XSSFWorkbook()
                    var sheet = workBook.createSheet("CATEGORY-WISE REPORT")

                    sheet.setColumnWidth(0,30*256)
                    sheet.setColumnWidth(1,20*256)

                    val headerFont = Global.fnHeaderFont(workBook)
                    val summaryFont =  Global.fnSummaryFont(workBook)
                    //Header Style
                    val headerStyle = Global.fnHeaderStyle(workBook,headerFont)
                    //Summary Style
                    val summaryStyle = Global.fnSummaryStyle(workBook,summaryFont)
                    //Create Table Header Style
                    val tableHeaderStyle = Global.fnTableHeaderStyle(workBook)
                    //Create Table Date Style
                    val dataStyle = Global.fnTableDateStyle(workBook)

                    //Header Row
                    var headerRow = sheet.createRow(0)
                    var headerCell = headerRow.createCell(0)
                    headerCell.setCellValue("CATEGORY-WISE REPORT")
                    headerCell.cellStyle = headerStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(
                            0,  // first row (0th row)
                            0,  // last row
                            0,  // first column
                            4   // last column
                        )
                    )

                    var dateRow = sheet.createRow(2)
                    var dateCell0 = dateRow.createCell(0)
                    dateCell0.setCellValue("SELECTED DATE: ${selectedDateUi.value}")
                    dateCell0.cellStyle=summaryStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(2,2,0,4)
                    )

                    var dateRow2 = sheet.createRow(3)
                    var dateCell20 = dateRow2.createCell(0)
                    dateCell20.setCellValue("EXPORT DATE:    ${Global.fnGetCurrentDateUi()}")
                    dateCell20.cellStyle=summaryStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(3,3,0,4)
                    )

                    var timeRow = sheet.createRow(4)
                    var timeCell0 = timeRow.createCell(0)
                    timeCell0.setCellValue("EXPORT TIME:    ${Global.fnGetCurrentTime()}")
                    timeCell0.cellStyle=summaryStyle


                    sheet.addMergedRegion(
                        CellRangeAddress(4,4,0,4)
                    )

                    //Table Header Row
                    var tableHeaderRow = sheet.createRow(6)
                    var cell0 = tableHeaderRow.createCell(0)
                    cell0.setCellValue("CATEGORY")
                    cell0.cellStyle=tableHeaderStyle
                    var cell1 =tableHeaderRow.createCell(1)
                    cell1.setCellValue("EXPENSE AMOUNT")
                    cell1.cellStyle=tableHeaderStyle

                    //Table Data Row
                    categoryList.value?.forEachIndexed { index, expense ->
                        var dataRow = sheet.createRow(index+7)

                        var dataCell0=dataRow.createCell(0)
                        dataCell0.setCellValue(expense.categoryName)
                        dataCell0.cellStyle=dataStyle
                        var dataCell1=dataRow.createCell(1)
                        dataCell1.setCellValue("${expense.expenseAmt}")
                        dataCell1.cellStyle=dataStyle

                    }

                    val result = fnExportReportToDownloads(workBook,"CategoryWiseReport_${selectedDate.value}_${Global.fnGetCurrentTime()}.xlsx")

                    if(result){
                        _exportStatus.value = ResultState1.success(R.string.cateReport_ExportSuccess)
                    }
                    else{
                        _exportStatus.value = ResultState1.fail(R.string.cateReport_ExportFailed)
                    }
                }
            }
            catch (e : Exception)
            {
                _isExportLoading.value = false
                _exportStatus.value = ResultState1.fail(R.string.cateReport_ExportFailed)
                logger.logError(LOG_TAG,"Excel File Creation: ${e.message}")
                Log.e(LOG_TAG,"Excel File Creation: ${e.message}")
            }
        }
    }


    fun fnExportReportToDownloads(workBook : XSSFWorkbook, fileName : String): Boolean
    {
        return try
        {
            var values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS+"/ExpenseTracker"
                )
            }

            val uri: Uri =application.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                values
            ) ?: return false

            application.contentResolver.openOutputStream(uri)?.use { os ->
                workBook.write(os)
            }

            workBook.close()
//            delay(1000L)
            _isExportLoading.value=false
            true
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Export Category Wise Report To Internal Storage(Document Path): ${e.message}")
            Log.e(LOG_TAG,"Export Category Wise Report To Internal Storage(Document Path): ${e.message}")
            false
        }

    }

    fun fnPreWarmExcelEngine()
    {
        viewModelScope.launch(Dispatchers.IO) {
            try
            {
                val wb = XSSFWorkbook()
                wb.createSheet("warmup")
                wb.close()
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"PreWarm Excel Engine: ${e.message}")
                Log.e(LOG_TAG,"PreWarm Excel Engine: ${e.message}")
            }
        }
    }

}