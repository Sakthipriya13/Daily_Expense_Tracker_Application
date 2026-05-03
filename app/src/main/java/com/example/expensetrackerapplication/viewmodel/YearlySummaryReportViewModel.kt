package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.paging.LOG_TAG
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.data.repositary.IncomeRepository
import com.example.expensetrackerapplication.model.ExpenseDetailsPerMonth
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import kotlin.math.abs

class YearlySummaryReportViewModel(application: Application) : AndroidViewModel(application = application)
{
    // Expense And Income Repository Variable Initialization
    val expenseRepository : ExpenseRepository

    private var incomeRepository: IncomeRepository


    init{
        val expenseDao = AppDatabase.getdatabase(application).ExpenseDao()
        expenseRepository = ExpenseRepository(expenseDao)

        val incomeDao = AppDatabase.getdatabase(application).IncomeDao()
        incomeRepository = IncomeRepository(incomeDao)
    }

    // Income Amount Variable Initialization
    var _incomeAmt = MutableLiveData<String?>("0.00")
    var incomeAmt : LiveData<String?> = _incomeAmt

    // Expense Amount Variable Initialization
    var _expenseAmt = MutableLiveData<String?>("0.00")
    var expenseAmt : LiveData<String?> = _expenseAmt

    // Balance Amount Variable Initialization
    var _balanceAmt = MutableLiveData<String?>("0.00")
    var  balanceAmt : LiveData<String?> = _balanceAmt
    
    // Close The Yearly Report Variable Initialization
    var _isClosed = MutableLiveData<Boolean>()
    var isClosed : LiveData<Boolean> = _isClosed

    // Selected Year Variable Initialization
    var _selectedYear = MutableLiveData<String>(Global.fnGetCurrentYear())
    var selectedYear : LiveData<String> = _selectedYear

    // Yearly Summary List Variable Initialization
    var _yearSummaryList = MutableLiveData<List<ExpenseDetailsPerMonth>>(emptyList<ExpenseDetailsPerMonth>())
    var yearSummaryList : LiveData<List<ExpenseDetailsPerMonth>> = _yearSummaryList

    // Export Status Variable Initialization
    var _exportStatus = MutableLiveData<ResultState1>()
    var exportStatus : LiveData<ResultState1> = _exportStatus

    // ProgressBar Status Variable Initialization
    var _isExportLoading = MutableLiveData<Boolean>(false)
    var isExportLoading : LiveData<Boolean> = _isExportLoading

    // MonthArray Variable Initialization
    var _monthArray = MutableLiveData<Array<String>>()
    var monthArray : LiveData<Array<String>> = _monthArray

    var _isCalendarSelected = MutableLiveData<Boolean>()
    var isCalendarSelected : LiveData<Boolean> = _isCalendarSelected

    fun isBack()
    {
        try {
            _isClosed.value = true
        }
        catch (e: Exception){
            Log.e("YEARLY_SUMMARY_REPORT_VIEW_MODEL","Close The Yearly Summary Report: ${e.message}")
        }
    }
    fun resetCloseState()
    {
        try {
            _isClosed.value = false
        }
        catch (e: Exception) {
            Log.e("YEARLY_SUMMARY_REPORT_VIEW_MODEL", "Reset Close State: ${e.message}")
        }
    }
    fun getIsCalendarSelected(){
        try {
            _isCalendarSelected.value = true
        }
        catch (e: Exception) {
            Log.e("MONTHLY_SUMMARY_REPORT_VIEW_MODEL", "Calendar Selected: ${e.message}")
        }
    }
    fun fnGetExpenseDetailsPerYear(year:String){
        viewModelScope.launch {
            try{
                _incomeAmt.postValue("0.00")
                _expenseAmt.postValue("0.00")
                _balanceAmt.postValue("0.00")

                var income = incomeRepository.fnGetIncomePerYear(year)
                var expense = expenseRepository.fnGetYearSummary(year)
                var balance = income-expense

                if(income != 0.0f){
                    _incomeAmt.postValue(Global.fnFormatFloatTwoDigits(income))
                }
                if(expense != 0.0f){
                    _expenseAmt.postValue(Global.fnFormatFloatTwoDigits(expense))
                }
                if(balance != 0.0f){
                    _balanceAmt.postValue(Global.fnFormatFloatTwoDigits(abs(balance)))
                }

                var resList = expenseRepository.fnGetExpenseDetailsPerYear(year)
                var list : MutableList<ExpenseDetailsPerMonth> = mutableListOf<ExpenseDetailsPerMonth>()
                if(resList.isNotEmpty())
                {
                    resList.forEach { ob ->
                        list.add(
                            ExpenseDetailsPerMonth(
                                expenseDate = when(ob.expenseDate) {
                                    "1", "01" -> getMonthName("1",monthArray.value)
                                    "2", "02" ->getMonthName("2",monthArray.value)
                                    "3", "03" -> getMonthName("3",monthArray.value)
                                    "4", "04" -> getMonthName("4",monthArray.value)
                                    "5", "05" -> getMonthName("5",monthArray.value)
                                    "6", "06" -> getMonthName("6",monthArray.value)
                                    "7", "07" -> getMonthName("7",monthArray.value)
                                    "8", "08" -> getMonthName("8",monthArray.value)
                                    "9", "09" -> getMonthName("9",monthArray.value)
                                    "10" -> getMonthName("10",monthArray.value)
                                    "11" ->getMonthName("11",monthArray.value)
                                    "12" -> getMonthName("12",monthArray.value)
                                    else -> "Invalid"
                                },
                                transactionsCount = ob.transactionsCount,
                                expenseSummaryAmt = ob.expenseSummaryAmt
                            )
                        )
                    }
                    _yearSummaryList.postValue(list)
                }
                else{
                    _yearSummaryList.postValue(emptyList<ExpenseDetailsPerMonth>())
                }
            }
            catch (e : Exception)
            {
                Log.e("YEARLY_SUMMARY_REPORT_VIEW_MODEL","Get Expense Details Per Year: ${e.message}")
            }
        }
    }
    fun getMonthName(monthValue: String, monthArray: Array<String>?): String
    {
        return try {
            val monthIndex = monthValue.toIntOrNull()?.minus(1)

             if (monthIndex != null && monthArray?.indices?.contains(monthIndex) == true) {
                monthArray?.get(monthIndex) ?: "Invalid"
            } else {
                "Invalid"
            }
        }
        catch (e: Exception){
            Log.e("YEARLY_SUMMARY_REPORT_VIEW_MODEL","Return Month Name: ${e.message}")
            "Invalid"
        }
    }

    fun fnExportYearlySummaryReport(){
        viewModelScope.launch {
            try
            {
                if(isExportLoading.value==false)
                {
                    _isExportLoading.value=true

                    delay(1000L)

                    var start = Global.fnGetCurrentTime()

                    var workBook = XSSFWorkbook()
                    var sheet = workBook.createSheet("YEARLY SUMMARY REPORT")

                    sheet.setColumnWidth(0,30*256)
                    sheet.setColumnWidth(1,30*256)
                    sheet.setColumnWidth(2,30*256)


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
                    headerCell.setCellValue("YEARLY SUMMARY REPORT")
                    headerCell.cellStyle = headerStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(
                            0,  // first row (0th row)
                            0,  // last row
                            0,  // first column
                            4   // last column
                        )
                    )

                    var yearRow = sheet.createRow(2)
                    var dateCell0 = yearRow.createCell(0)
                    dateCell0.setCellValue("SELECTED YEAR: ${selectedYear.value}") //15
                    dateCell0.cellStyle=summaryStyle
                    sheet.addMergedRegion(
                        CellRangeAddress(2,2,0,4)
                    )

                    var dateRow = sheet.createRow(3)
                    var dateCell1 = dateRow.createCell(0)
                    dateCell1.setCellValue("EXPORT DATE:    ${Global.fnGetCurrentDateUi()}")
                    dateCell1.cellStyle=summaryStyle
                    sheet.addMergedRegion(
                        CellRangeAddress(3,3,0,4)
                    )

                    var timeRow = sheet.createRow(4)
                    var dateCell2 = timeRow.createCell(0)
                    dateCell2.setCellValue("EXPORT TIME:    ${Global.fnGetCurrentTime()}")
                    dateCell2.cellStyle=summaryStyle
                    sheet.addMergedRegion(
                        CellRangeAddress(4,4,0,4)
                    )

                    var expenseSummaryRow = sheet.createRow(6)
                    var expenseSummaryCell = expenseSummaryRow.createCell(0)
                    expenseSummaryCell.setCellValue("EXPENSE SUMMARY")
                    expenseSummaryCell.cellStyle=headerStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(6,6,0,4)
                    )

                    var totalExpenseRow = sheet.createRow(8)
                    var totalExpenseCell0 = totalExpenseRow.createCell(0)
                    totalExpenseCell0.setCellValue("INCOME:    ${incomeAmt.value}")
                    totalExpenseCell0.cellStyle=summaryStyle


                    sheet.addMergedRegion(
                        CellRangeAddress(8,8,0,4)
                    )

                    var addedExpenseRow = sheet.createRow(9)
                    var addedExpenseCell0 = addedExpenseRow.createCell(0)
                    addedExpenseCell0.setCellValue("EXPENSE:   ${expenseAmt.value}")
                    addedExpenseCell0.cellStyle=summaryStyle


                    sheet.addMergedRegion(
                        CellRangeAddress(9,9,0,4)
                    )

                    var deletedExpenseRow = sheet.createRow(10)
                    var deletedExpenseCell0 = deletedExpenseRow.createCell(0)
                    deletedExpenseCell0.setCellValue("BALANCE:  ${balanceAmt.value}")
                    deletedExpenseCell0.cellStyle=summaryStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(10,10,0,4)
                    )

                    //Table Header Row
                    var tableHeaderRow = sheet.createRow(12)
                    var cell0 = tableHeaderRow.createCell(0)
                    cell0.setCellValue("MONTH")
                    cell0.cellStyle=tableHeaderStyle
                    var cell1 =tableHeaderRow.createCell(1)
                    cell1.setCellValue("TRANSACTIONS COUNT")
                    cell1.cellStyle=tableHeaderStyle
                    var cell2=tableHeaderRow.createCell(2)
                    cell2.setCellValue("EXPENSE AMOUNT")
                    cell2.cellStyle = tableHeaderStyle

                    //Table Data Row
                    yearSummaryList.value?.forEachIndexed { index, ob ->
                        var dataRow = sheet.createRow(index+13)

                        var dataCell0=dataRow.createCell(0)
                        dataCell0.setCellValue(ob.expenseDate)
                        dataCell0.cellStyle=dataStyle
                        var dataCell1=dataRow.createCell(1)
                        dataCell1.setCellValue("${ob.transactionsCount}")
                        dataCell1.cellStyle=dataStyle
                        var dataCell2=dataRow.createCell(2)
                        dataCell2.setCellValue("${ob.expenseSummaryAmt}")
                        dataCell2.cellStyle=dataStyle

                    }

                    var result =  fnExportReportToDownloads(workBook,"YearlySummaryReport${Global.fnGetCurrentDate()}_${Global.fnGetCurrentTime()}.xlsx")

                    if(result == true)
                    {
                        Log.i(
                            LOG_TAG, "Export Success")
                        _exportStatus.value = ResultState1.success(R.string.yearlyReport_ExportSuccess)
                    }
                    else{
                        _exportStatus.value = ResultState1.fail(R.string.yearlyReport_ExportFailed)
                    }
                }
            }
            catch (e : Exception)
            {
                _isExportLoading.value=false
                _exportStatus.value = ResultState1.fail(R.string.yearlyReport_ExportFailed)
                Log.e("YEARLY_SUMMARY_REPORT_VIEW_MODEL","Excel File Creation: ${e.message}")
            }
        }

    }

    fun fnExportReportToDownloads(workBook : XSSFWorkbook, fileName : String): Boolean
    {
        return try {
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
            Log.e("YEARLY_SUMMARY_REPORT_VIEW_MODEL","Export Yearly Summary Report To Internal Storage(Document Path): ${e.message}")
            false
        }

    }

    fun fnPreWarmExcelEngine() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wb = XSSFWorkbook()
                wb.createSheet("warmup")
                wb.close()
            }
            catch (e: Exception) {
                Log.e("MONTHLY_SUMMARY_REPORT_VIEW_MODEL","PreWarm Excel Engine: ${e.message}")
            }
        }
    }


}