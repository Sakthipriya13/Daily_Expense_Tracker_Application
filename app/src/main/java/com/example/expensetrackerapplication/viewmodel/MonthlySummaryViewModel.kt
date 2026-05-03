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
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.logger.Logger
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
import java.time.YearMonth
import kotlin.math.abs

class MonthlySummaryViewModel(application: Application, logger: Logger) : AndroidViewModel(application = application)
{
    // Expense Repository Variable Declaration
    val expenseRepository : ExpenseRepository
    // Income Repository Variable Declaration
    val incomeRepository : IncomeRepository
    init {
        // Income & Expense Repository Variable Initialization
        val expenseDao = AppDatabase.getdatabase(application).ExpenseDao()
        val incomeDao = AppDatabase.getdatabase(application).IncomeDao()
        incomeRepository= IncomeRepository(incomeDao)
        expenseRepository= ExpenseRepository(expenseDao)
    }
    // Close The Monthly Report Screen
    var _isClosed = MutableLiveData<Boolean>()
    var isClosed : LiveData<Boolean> = _isClosed

    // Month Variable Initialization
    var _selectedMonth = MutableLiveData<String>(Global.fnGetCurrentMonth())
    var selectedMonth : LiveData<String> = _selectedMonth
    // Year Variable Initialization
    var _selectedYear = MutableLiveData<String>(Global.fnGetCurrentYear())
    var selectedYear : LiveData<String> = _selectedYear

    // selected Month And Year Variable Initialization
    var _selectedMonthAndYear = MutableLiveData<String>("${selectedMonth.value}/${selectedYear.value}")
    var selectedMonthAndYear : LiveData<String> = _selectedMonthAndYear

    // Income AMount Variable Initialization
    var _incomeAmt = MutableLiveData<String?>("0.00")
    var incomeAmt : LiveData<String?> = _incomeAmt

    // Expense Amount Variable Initialization
    var _expenseAmt = MutableLiveData<String?>("0.00")
    var expenseAmt : LiveData<String?> = _expenseAmt

    // Balance AMount Variable Initialization
    var _balanceAmt = MutableLiveData<String?>("0.00")
    var  balanceAmt : LiveData<String?> = _balanceAmt

    // Monthly Summary Report Variable Initialization
    var _monthlySummaryReportList = MutableLiveData<List<ExpenseDetailsPerMonth>>(emptyList<ExpenseDetailsPerMonth>())
    var monthlySummaryReportList : LiveData<List<ExpenseDetailsPerMonth>> = _monthlySummaryReportList

    // Export Status Variable Initialization
    var _exportStatus = MutableLiveData<ResultState1>()
    var exportStatus : LiveData<ResultState1> = _exportStatus

    // Display Progressbar Status Variable
    var _isExportLoading = MutableLiveData<Boolean>(false)
    var isExportLoading : LiveData<Boolean> = _isExportLoading

    var _isCalendarSelected = MutableLiveData<Boolean>()
    var isCalendarSelected : LiveData<Boolean> = _isCalendarSelected

    fun isBack(){
        try {
            _isClosed.value = true
        }
        catch (e: Exception)
        {
            Log.e("MONTHLY_SUMMARY_REPORT_VIEW_MODEL","Close The Monthly Report Screen: ${e.message}")
        }
    }

    fun resetCloseState()
    {
        try {
            _isClosed.value = false
        }
        catch (e: Exception)
        {
            Log.e("MONTHLY_SUMMARY_REPORT_VIEW_MODEL", "Reset Close State: ${e.message}")
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

    fun fnGetExpenseDetailsPerMonth(month : String, year : String)
    {
        viewModelScope.launch {
            try{
                _isExportLoading.postValue(true)

                _incomeAmt.postValue("0.00")
                _expenseAmt.postValue("0.00")
                _balanceAmt.postValue("0.00")

                var income = incomeRepository.fnGetIncomePerMonthAndYear(month,year)
                var expense = expenseRepository.fnGetExpensePerMonthAndYear(month,year)
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

                val yearMonth = YearMonth.of(year.toInt(),month.toInt())

                val startDate = yearMonth.atDay(1).toString()
                val endDate = yearMonth.atEndOfMonth().toString()

                var resList = expenseRepository.fnGetExpenseDetailsPerMonth(month,year)

                var list : MutableList<ExpenseDetailsPerMonth> = mutableListOf<ExpenseDetailsPerMonth>()

                if(resList.isNotEmpty()){
                    resList.forEach { ob ->
                        list.add(
                            ExpenseDetailsPerMonth(
                                expenseDate = ob.expenseDate,
                                transactionsCount = ob.transactionsCount,
                                expenseSummaryAmt = ob.expenseSummaryAmt
                            )
                        )
                    }

                    _monthlySummaryReportList.value = list
                }
                else
                {
                    _monthlySummaryReportList.value = emptyList<ExpenseDetailsPerMonth>()
                }

            }
            catch(e : Exception)
            {
                Log.e("MONTHLY_SUMMARY_REPORT_VIEW_MODEL","Get Expense Details Per Month: ${e.message}")
            }

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
                    var sheet = workBook.createSheet("MONTHLY SUMMARY REPORT")

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
                    headerCell.setCellValue("MONTHLY SUMMARY REPORT")
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
                    dateCell0.setCellValue("SELECTED MONTH: ${selectedMonthAndYear.value}")
                    dateCell0.cellStyle=summaryStyle
                    sheet.addMergedRegion(
                        CellRangeAddress(2,2,0,4)
                    )

                    var dateRow = sheet.createRow(3)
                    var dateCell1 = dateRow.createCell(0)
                    dateCell1.setCellValue("EXPORT DATE:         ${Global.fnGetCurrentDateUi()}")
                    dateCell1.cellStyle=summaryStyle
                    sheet.addMergedRegion(
                        CellRangeAddress(3,3,0,4)
                    )

                    var timeRow = sheet.createRow(4)
                    var dateCell2 = timeRow.createCell(0)
                    dateCell2.setCellValue("EXPORT TIME:         ${Global.fnGetCurrentTime()}")
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

                    var totalExpenseRow = sheet.createRow(7)
                    var totalExpenseCell0 = totalExpenseRow.createCell(0)
                    totalExpenseCell0.setCellValue("INCOME:    ${incomeAmt.value}")
                    totalExpenseCell0.cellStyle=summaryStyle


                    sheet.addMergedRegion(
                        CellRangeAddress(7,7,0,4)
                    )

                    var addedExpenseRow = sheet.createRow(8)
                    var addedExpenseCell0 = addedExpenseRow.createCell(0)
                    addedExpenseCell0.setCellValue("EXPENSE:   ${expenseAmt.value}")
                    addedExpenseCell0.cellStyle=summaryStyle


                    sheet.addMergedRegion(
                        CellRangeAddress(8,8,0,4)
                    )

                    var deletedExpenseRow = sheet.createRow(9)
                    var deletedExpenseCell0 = deletedExpenseRow.createCell(0)
                    deletedExpenseCell0.setCellValue("BALANCE:  ${balanceAmt.value}")
                    deletedExpenseCell0.cellStyle=summaryStyle

                    sheet.addMergedRegion(
                        CellRangeAddress(9,9,0,4)
                    )

                    //Table Header Row
                    var tableHeaderRow = sheet.createRow(11)
                    var cell0 = tableHeaderRow.createCell(0)
                    cell0.setCellValue("DATE")
                    cell0.cellStyle=tableHeaderStyle
                    var cell1 =tableHeaderRow.createCell(1)
                    cell1.setCellValue("TRANSACTIONS COUNT")
                    cell1.cellStyle=tableHeaderStyle
                    var cell2=tableHeaderRow.createCell(2)
                    cell2.setCellValue("EXPENSE AMOUNT")
                    cell2.cellStyle = tableHeaderStyle

                    //Table Data Row
                    monthlySummaryReportList.value?.forEachIndexed { index, ob ->
                        var dataRow = sheet.createRow(index+12)

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

                    val exportStatus  = fnExportReportToDownloads(workBook,"MonthlySummaryReport${Global.fnGetCurrentDate()}_${Global.fnGetCurrentTime()}.xlsx")

                    if(exportStatus){
                        _exportStatus.value = ResultState1.success(R.string.monthlyReport_ExportSuccess)
                    }
                    else{
                        _exportStatus.value = ResultState1.fail(R.string.monthlyReport_ExportFailed)
                    }
                }
            }
            catch (e : Exception)
            {
                _isExportLoading.value=false
                _exportStatus.value = ResultState1.fail(R.string.monthlyReport_ExportFailed)
                Log.e("MONTHLY_SUMMARY_REPORT_VIEW_MODEL","Excel File Creation: ${e.message}")
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
        catch (e : Exception){
            Log.e("MONTHLY_SUMMARY_REPORT_VIEW_MODEL","Export Monthly Summary Report To Internal Storage(Document Path): ${e.message}")
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