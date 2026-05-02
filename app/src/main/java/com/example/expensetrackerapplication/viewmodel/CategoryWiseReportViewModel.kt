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
import com.example.expensetrackerapplication.`object`.Global
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.format.DateTimeFormatter

class CategoryWiseReportViewModel(application: Application) : AndroidViewModel(application = application)
{
    private var expenseRepository : ExpenseRepository

    init{
        var expenseDao = AppDatabase.getdatabase(application).ExpenseDao()
        expenseRepository = ExpenseRepository(expenseDao)
    }

    var _selectedDate = MutableLiveData<String>(Global.fnGetCurrentDate())
    var selectedDate : LiveData<String> = _selectedDate

    var _selectedDateUi = MutableLiveData<String>(Global.fnGetCurrentDateUi())
    var selectedDateUi : LiveData<String> = _selectedDateUi

    var _categoryList = MutableLiveData<List<CategoryChartModel>>(mutableListOf<CategoryChartModel>())
    var categoryList : LiveData<List<CategoryChartModel>> = _categoryList

    var _closeCategoryReport = MutableLiveData<Boolean>()
    var closeCategoryReport : LiveData<Boolean> = _closeCategoryReport

    var _isExportLoading = MutableLiveData<Boolean>(false)
    var isExportLoading : LiveData<Boolean> = _isExportLoading

    var _exportStatus = MutableLiveData<Boolean>()
    var exportStatus : LiveData<Boolean> = _exportStatus

    var _totalExpenseSummary = MutableLiveData<String>("0.00")
    var totalExpenseSummary : LiveData<String> = _totalExpenseSummary

    var _addedExpenseSummary = MutableLiveData<String>("0.00")
    var addedExpenseSummary : LiveData<String> = _addedExpenseSummary

    var _deletedExpenseSummary = MutableLiveData<String>("0.00")
    var deletedExpenseSummary : LiveData<String> = _deletedExpenseSummary

    val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    fun fnGetCategoryDetailsPerDay(date : String?){
        viewModelScope.launch {
            try{
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
                else{
                    _categoryList.postValue(mutableListOf<CategoryChartModel>())
                }
            }
            catch (ex : Exception){
                Log.e("GET CATEGORY LIST PER DAY","Get Category List Per Day: ${ex.message}")
            }
        }
    }

    fun fnCloseReport(){
        _closeCategoryReport.value = true
    }

    fun fnExportCategoryList()
    {
        viewModelScope.launch {
            try{
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

                    _exportStatus.value = fnExportReportToDownloads(workBook,"CategoryWiseReport_${selectedDate.value}_${Global.fnGetCurrentTime()}.xlsx")

                    Log.i("TIME DIFF","Time Diff: ${Global.fnGetCurrentTime()} ms")

                }
            }
            catch (e : Exception)
            {
                Log.e("EXPORT CATEGORY LIST AS EXCEL SHEET","Export Category List As Excel Sheet: ${e.message}")
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
            Log.e("FN_EXPORT_REPORT_TO_DOWNLOADS","Fn Export Report To Downloads: ${e.message}")
            false
        }

    }

    fun fnPreWarmExcelEngine() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wb = XSSFWorkbook()
                wb.createSheet("warmup")
                wb.close()
            } catch (_: Exception) {}
        }
    }

}