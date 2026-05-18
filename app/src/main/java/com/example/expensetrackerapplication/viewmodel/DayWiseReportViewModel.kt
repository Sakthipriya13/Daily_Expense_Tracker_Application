package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.model.CurrentDayReportModel
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class DayWiseReportViewModel(
    application: Application,
    private val logger: FileLogger) : AndroidViewModel(application = application)
{
    // Expense Repository Variable Initialization
    private lateinit var expenseRepository : ExpenseRepository
    init {
         val dao = AppDatabase.getdatabase(application,logger)?.ExpenseDao()
         dao?.let {
             expenseRepository= ExpenseRepository(dao,logger)
         }
    }
    // Close Day Wise Report
    var _isClosed = MutableLiveData<Boolean>()
    var isClosed : LiveData<Boolean> = _isClosed

    // Date
    var _selectedDateUi = MutableLiveData<String>(Global.fnGetCurrentDateUi(logger))
    var selectedDateUi : LiveData<String> = _selectedDateUi

    var _selectedDate = MutableLiveData<String>(Global.fnGetCurrentDate(logger))
    var selectedDate : LiveData<String> = _selectedDate



    // Total Expense Summary
    var _totalExpenseSummary = MutableLiveData<String>("0.00")
    var totalExpenseSummary : LiveData<String> = _totalExpenseSummary

    // Added Expense Summary
    var _addedExpenseSummary = MutableLiveData<String>("0.00")
    var addedExpenseSummary : LiveData<String> = _addedExpenseSummary

    // Deleted Expense Summary
    var _deletedExpenseSummary = MutableLiveData<String>("0.00")
    var deletedExpenseSummary : LiveData<String> = _deletedExpenseSummary

    // Expense List
    var _expenseList = MutableLiveData<List<CurrentDayReportModel>>()
    var expenseList : LiveData<List<CurrentDayReportModel>> = _expenseList

    // Export Status
    var _exportStatus = MutableLiveData<ResultState1>()
    var exportStatus : LiveData<ResultState1> = _exportStatus

    // Load Progressbar
    var _isExportLoading = MutableLiveData<Boolean>(false)
    var isExportLoading : LiveData<Boolean> = _isExportLoading

    // Expense Delete Status
    var _expenseDeleteStatus = MutableLiveData<ResultState1>()
    var expenseDeleteStatus : LiveData<ResultState1> = _expenseDeleteStatus


    val LOG_TAG = "DAY_WISE_REPORT_VIEW_MODEL"

    fun isBack()
    {
        try
        {
            _isClosed.value=true
            _selectedDateUi.value = Global.fnGetCurrentDateUi(logger)
            _selectedDate.value = Global.fnGetCurrentDate(logger)
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Close The Day-Wise Report: ${e.message}")
            Log.e("DAY_WISE_REPORT_VIEW_MODEL","Close The Day-Wise Report: ${e.message}")
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
            Log.e("DAY_WISE_REPORT", "Reset Close State: ${e.message}")
        }
    }

    fun fnClearAllFields(){
        try
        {
            _totalExpenseSummary.value="0.00"
            _addedExpenseSummary.value="0.00"
            _deletedExpenseSummary.value="0.00"
            _expenseList.value = emptyList<CurrentDayReportModel>()
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Clear All Fields: ${e.message}")
            Log.e("DAY_WISE_REPORT_VIEW_MODEL","Clear All Fields: ${e.message}")
        }
    }

    fun fnGetExpenseDetails(date: String?){
        viewModelScope.launch {
            try
            {
//                _isExportLoading.postValue(true)

                var totalExpenseAmtSum = 0.0f
                var addedExpenseAmtSum = 0.0f
                var deletedExpenseAmtSum = 0.0f

                _totalExpenseSummary.value= "0.00"
                _addedExpenseSummary.value ="0.00"
                _deletedExpenseSummary.value ="0.00"


                val res = withContext(Dispatchers.IO){
                    expenseRepository.fnGetExpenseDetailsPerDate(date)
                }
                var list : MutableList<CurrentDayReportModel> = mutableListOf()
                var exPaymentType = ""
                if(res.isNotEmpty())
                {
                    res.forEach { ex ->
                        list.add(CurrentDayReportModel(
                            expenseId = ex.expenseId,
                            categoryId= ex.expenseCategoryId,
                            catgeoryName= ex.expenseCategoryName,
                            expenseAmt = Global.fnFormatFloatTwoDigits(ex.expenseAmt.toFloat() ?:0.00f,logger),
                            expenseRemarks = ex.expenseRemarks,
                            isDelete = if (ex.expenseStatus == Global.EXPENSE_STATUS_DELETED) "DELETED" else "NOT DELETED",

                            paymentType = when{
                                ex.paymentType == Global.PAYMENT_TYPE_CASH -> "CASH"
                                ex.paymentType == Global.PAYMENT_TYPE_CARD -> "CARD"
                                ex.paymentType == Global.PAYMENT_TYPE_UPI -> "UPI"
                                ex.paymentType == Global.PAYMENT_TYPE_OTHER -> "OTHERS"
                                else -> {
                                    when{
                                        (ex.expenseAmtInCash  ?: 0.0f) > 0.0f && (ex.expenseAmtInCard  ?: 0.0f) > 0.0f && (ex.expenseAmtInUpi  ?: 0.0f) > 0.0f -> "CASH,CARD,UPI"
                                        (ex.expenseAmtInCash  ?: 0.0f) > 0.0f && (ex.expenseAmtInCard  ?: 0.0f) > 0.0f -> "CASH,CARD"
                                        (ex.expenseAmtInCash  ?: 0.0f) > 0.0f && (ex.expenseAmtInUpi  ?: 0.0f) > 0.0f -> "CASH,UPI"
                                        (ex.expenseAmtInCard  ?: 0.0f) > 0.0f && (ex.expenseAmtInUpi  ?: 0.0f) > 0.0f -> "CARD,UPI"
                                        (ex.expenseAmtInCash  ?: 0.0f) > 0.0f -> "CASH"
                                        (ex.expenseAmtInCard  ?: 0.0f) > 0.0f -> "CARD"
                                        (ex.expenseAmtInUpi   ?: 0.0f) > 0.0f -> "UPI"
                                        else -> ""
                                    }
                                }
                            }
                        ))
                        totalExpenseAmtSum = totalExpenseAmtSum + (ex.expenseAmt?.toFloat() ?:0.0f )
                        if(ex.expenseStatus == Global.EXPENSE_STATUS_DELETED) deletedExpenseAmtSum = deletedExpenseAmtSum + (ex.expenseAmt?.toFloat() ?:0.0f ) else addedExpenseAmtSum=addedExpenseAmtSum+ (ex.expenseAmt?.toFloat() ?:0.0f )

                    }
                    _expenseList.postValue(list)
                    _totalExpenseSummary.postValue(Global.fnFormatFloatTwoDigits(totalExpenseAmtSum,logger))
                    _addedExpenseSummary.postValue(Global.fnFormatFloatTwoDigits(addedExpenseAmtSum,logger))
                    _deletedExpenseSummary.postValue(Global. fnFormatFloatTwoDigits(deletedExpenseAmtSum,logger))

                }
                else
                {
                    _expenseList.postValue(emptyList<CurrentDayReportModel>())
                }
            }
            catch (e : Exception)
            {
                _expenseList.postValue(emptyList<CurrentDayReportModel>())
                logger.logError(LOG_TAG,"Get Expense Details Per Date: ${e.message}")
                Log.e("DAY_WISE_REPORT_VIEW_MODEL","Get Expense Details Per Date: ${e.message}")
            }
        }
    }

    fun fnDeleteExpense(expenseId: Int?)
    {
        viewModelScope.launch {
            try
            {
                var delStatus = withContext(Dispatchers.IO){
                    expenseRepository.fnDeleteExpense(expenseId)
                }

                if(delStatus)
                {
                    _expenseDeleteStatus.postValue(ResultState1.success(R.string.dayWiseReport_DeleteExpenseSuccess))
                    fnGetExpenseDetails(selectedDate.value)
                }
                else{
                    _expenseDeleteStatus.postValue(ResultState1.fail(R.string.dayWiseReport_DeleteExpenseFailed))
                }
            }
            catch(e : Exception)
            {
                logger.logError(LOG_TAG,"Delete Expense: ${e.message}")
                _expenseDeleteStatus.postValue(ResultState1.fail(R.string.somethingWrong))
                Log.e("DAY_WISE_REPORT_VIEW_MODEL","Delete Expense: ${e.message}")
            }
        }
    }

    fun fnExportReport(){
        viewModelScope.launch {
            try
            { 
                if(isExportLoading.value==false)
                {
                    _isExportLoading.value=true

                    delay(1000L)
                    
                    var start = Global.fnGetCurrentTime(logger)

                    var workBook = XSSFWorkbook()
                    var sheet = workBook.createSheet("DAY WISE REPORT")

                    sheet.setColumnWidth(0,30*256)
                    sheet.setColumnWidth(1,20*256)
                    sheet.setColumnWidth(2,20*256)
                    sheet.setColumnWidth(3,50*256)
                    sheet.setColumnWidth(4,20*256)


                    val headerFont = Global.fnHeaderFont(workBook,logger)
                    val subHeaderFont = Global.fnHeaderFont(workBook,logger)
                    val summaryFont =  Global.fnSummaryFont(workBook,logger)
                    //Header Style
                    val headerStyle = Global.fnHeaderStyle(workBook,headerFont,logger)
                    //Sub Header Style
                    val subHeaderStyle = Global.fnHeaderStyle(workBook,subHeaderFont,logger)
                    //Summary Style
                    val summaryStyle = Global.fnSummaryStyle(workBook,summaryFont,logger)
                    //Create Table Header Style
                    val tableHeaderStyle = Global.fnTableHeaderStyle(workBook,logger)
                    //Create Table Date Style
                    val dataStyle = Global.fnTableDateStyle(workBook,logger)

                    //Header Row
                    var headerRow = sheet.createRow(0)
                    var headerCell = headerRow.createCell(0)
                    headerCell.setCellValue("DAY WISE REPORT")
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
                    dateCell20.setCellValue("EXPORT DATE:    ${Global.fnGetCurrentDateUi(logger)}")
                    dateCell20.cellStyle=summaryStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(3,3,0,4)
                    )

                    var timeRow = sheet.createRow(4)
                    var timeCell0 = timeRow.createCell(0)
                    timeCell0.setCellValue("EXPORT TIME:    ${Global.fnGetCurrentTime(logger)}")
                    timeCell0.cellStyle=summaryStyle


                    sheet.addMergedRegion(
                        CellRangeAddress(4,4,0,4)
                    )

                    var expenseSummaryRow = sheet.createRow(6)
                    var expenseSummaryCell = expenseSummaryRow.createCell(0)
                    expenseSummaryCell.setCellValue("EXPENSE SUMMARY")
                    expenseSummaryCell.cellStyle=subHeaderStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(6,6,0,4)
                    )

                    var totalExpenseRow = sheet.createRow(7)
                    var totalExpenseCell0 = totalExpenseRow.createCell(0)
                    totalExpenseCell0.setCellValue("TOTAL:     ${totalExpenseSummary.value}")
                    totalExpenseCell0.cellStyle=summaryStyle


                    sheet.addMergedRegion(
                        CellRangeAddress(7,7,0,4)
                    )

                    var addedExpenseRow = sheet.createRow(8)
                    var addedExpenseCell0 = addedExpenseRow.createCell(0)
                    addedExpenseCell0.setCellValue("ADDED:    ${addedExpenseSummary.value}")
                    addedExpenseCell0.cellStyle=summaryStyle


                    sheet.addMergedRegion(
                        CellRangeAddress(8,8,0,4)
                    )

                    var deletedExpenseRow = sheet.createRow(9)
                    var deletedExpenseCell0 = deletedExpenseRow.createCell(0)
                    deletedExpenseCell0.setCellValue("DELETED: ${deletedExpenseSummary.value}")
                    deletedExpenseCell0.cellStyle=summaryStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(9,9,0,4)
                    )

                    //Table Header Row
                    var tableHeaderRow = sheet.createRow(11)
                    var cell0 = tableHeaderRow.createCell(0)
                    cell0.setCellValue("CATEGORY")
                    cell0.cellStyle=tableHeaderStyle
                    var cell1 =tableHeaderRow.createCell(1)
                    cell1.setCellValue("EXPENSE AMOUNT")
                    cell1.cellStyle=tableHeaderStyle
                    var cell2=tableHeaderRow.createCell(2)
                    cell2.setCellValue("PAYMENT TYPE")
                    cell2.cellStyle = tableHeaderStyle
                    var cell3=tableHeaderRow.createCell(3)
                    cell3.setCellValue("REMARKS")
                    cell3.cellStyle=tableHeaderStyle
                    var cell4=tableHeaderRow.createCell(4)
                    cell4.setCellValue("STATUS")
                    cell4.cellStyle=tableHeaderStyle

                    //Table Data Row
                    expenseList.value?.forEachIndexed { index, expense ->
                        var dataRow = sheet.createRow(index+12)

                        var dataCell0=dataRow.createCell(0)
                        dataCell0.setCellValue(expense.catgeoryName)
                        dataCell0.cellStyle=dataStyle
                        var dataCell1=dataRow.createCell(1)
                        dataCell1.setCellValue(expense.expenseAmt)
                        dataCell1.cellStyle=dataStyle
                        var dataCell2=dataRow.createCell(2)
                        dataCell2.setCellValue(expense.paymentType)
                        dataCell2.cellStyle=dataStyle
                        var dataCell3=dataRow.createCell(3)
                        dataCell3.setCellValue(expense.expenseRemarks)
                        dataCell3.cellStyle=dataStyle
                        var dataCell4=dataRow.createCell(4)
                        dataCell4.setCellValue(expense.isDelete)
                        dataCell4.cellStyle=dataStyle

                    }
                    var exportResult = fnExportReportToDownloads(workBook,"DayWiseReport_${Global.fnGetCurrentDateUi(logger)}_${Global.fnGetCurrentTime(logger)}.xlsx")
                    if(exportResult)
                    {
                        _isExportLoading.value=false
                        _exportStatus.value = ResultState1.success(R.string.dayWiseReport_ExportSuccess)
                    }
                    else
                    {
                        _isExportLoading.value=false
                        _exportStatus.value = ResultState1.fail(R.string.dayWiseReport_ExportFailed)
                    }
                }
            }
            catch (e : Exception)
            {
                _isExportLoading.value=false
                _exportStatus.value = ResultState1.fail(R.string.dayWiseReport_ExportFailed)
                Log.e("DAY_WISE_REPORT_VIEW_MODEL","Excel File Creation: ${e.message}")
                logger.logError(LOG_TAG,"Excel File Creation: ${e.message}")
            }
        }

    }

    fun fnExportReportToDownloads(workBook: XSSFWorkbook, fileName: String): Boolean
    {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                // Android 10+
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOCUMENTS + "/ExpenseTracker"
                    )
                }

                val uri: Uri = application.contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    values
                ) ?: return false

                application.contentResolver.openOutputStream(uri)?.use { os ->
                    workBook.write(os)
                }

            }
            else
            {
                // Android 9 and below
                val documentsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
                )

                val appFolder = File(documentsDir, "ExpenseTracker")

                if (!appFolder.exists()) {
                    appFolder.mkdirs()
                }

                val file = File(appFolder, fileName)

                FileOutputStream(file).use { fos ->
                    workBook.write(fos)
                }
            }

            workBook.close()
            _isExportLoading.value = false
            true

        }
        catch (e: Exception)
        {

            logger.logError(
                LOG_TAG,
                "Export Day-Wise Report Failed: ${e.message}"
            )
            _isExportLoading.value = false
            false
        }
    }


//    fun fnExportReportToDownloads(workBook : XSSFWorkbook, fileName : String): Boolean
//    {
//        return try {
//            var values = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                put(MediaStore.MediaColumns.MIME_TYPE,
//                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//                put(
//                    MediaStore.MediaColumns.RELATIVE_PATH,
//                    Environment.DIRECTORY_DOCUMENTS+"/ExpenseTracker"
//                )
//            }
//
//            val uri: Uri =application.contentResolver.insert(
//                MediaStore.Files.getContentUri("external"),
//                values
//            ) ?: return false
//
//            application.contentResolver.openOutputStream(uri)?.use { os ->
//                workBook.write(os)
//            }
//
//            workBook.close()
////            delay(1000L)
//            _isExportLoading.value=false
//            true
//        }
//        catch (e : Exception)
//        {
//            logger.logError(LOG_TAG,"Export Day-Wise Report To Internal Storage(Document Path): ${e.message}")
//            Log.e("DAY_WISE_REPORT_VIEW_MODEL","Export Day-Wise Report To Internal Storage(Document Path): ${e.message}")
//            false
//        }
//
//    }

//    fun fnPreWarmExcelEngine()
//    {
//        viewModelScope.launch(Dispatchers.IO) {
//            try
//            {
//                val wb = XSSFWorkbook()
//                wb.createSheet("warmup")
//                wb.close()
//            }
//            catch (e: Exception)
//            {
//                logger.logError(LOG_TAG,"PreWarm Excel Engine: ${e.message}")
//                Log.e("DAY_WISE_REPORT_VIEW_MODEL","PreWarm Excel Engine: ${e.message}")
//            }
//        }
//    }

}