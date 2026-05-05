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
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.data.logger.Logger
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.model.PaymentTypeChartModel
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class PaymentTypeReportViewModel(application: Application, logger: FileLogger) : AndroidViewModel(application = application)
{
    // Expense Repository Variable Initialization
    private var expenseRepository : ExpenseRepository

    init{
        var expenseDao = AppDatabase.getdatabase(application).ExpenseDao()
        expenseRepository = ExpenseRepository(expenseDao)
    }

    // Selected Date Variable Initialization
    var _selectedDate = MutableLiveData<String>(Global.fnGetCurrentDate())
    var selectedDate : LiveData<String> = _selectedDate

    var _selectedDateUi = MutableLiveData<String>(Global.fnGetCurrentDateUi())
    var selectedDateUi : LiveData<String> = _selectedDateUi

    // Close Report Variable Initialization
    var _isClosed = MutableLiveData<Boolean>()
    var isClosed : LiveData<Boolean> = _isClosed

    // ProgressBar Loading Status Variable Initialization
    var _isExportLoading = MutableLiveData<Boolean>(false)
    var isExportLoading : LiveData<Boolean> = _isExportLoading

    // Export Status Variable Initialization
    var _exportStatus = MutableLiveData<ResultState1>()
    var exportStatus : LiveData<ResultState1> = _exportStatus

    // Payment Type List Variable Initialization
    var _paymentTypeList = MutableLiveData<List<PaymentTypeChartModel>>(mutableListOf<PaymentTypeChartModel>())
    var paymentTypeList : LiveData<List<PaymentTypeChartModel>> = _paymentTypeList

    // Cash Variable Initialization
    var _cashAmt = MutableLiveData<Float>(0.00f)
    var cashAmt : LiveData<Float> = _cashAmt

    // Card Amount Variable Initialization
    var _cardAmt = MutableLiveData<Float>(0.00f)
    var cardAmt : LiveData<Float> = _cardAmt

    // Upi Amount Variable Initialization
    var _upiAmt = MutableLiveData<Float>(0.00f)
    var upiAmt : LiveData<Float> = _upiAmt

    // Others Variable Initialization
    var _othersAmt = MutableLiveData<Float>(0.00f)
    var othersAmt : LiveData<Float> = _othersAmt

    val LOG_TAG = "PAYMENT_TYPE_REPORT_VIEW_MODEL"
    
    fun isBack()
    {
        try {
            _isClosed.value = true
        }
        catch (e: Exception){
            Log.e(LOG_TAG,"Close The Payment Type Report Screen: ${e.message}")
        }
    }

    fun resetCloseState()
    {
        try {
            _isClosed.value = false
        }
        catch (e: Exception)
        {
            Log.e(LOG_TAG, "Reset Close State: ${e.message}")
        }
    }

    fun fnGetPaymentTypeList(date: String)
    {
        viewModelScope.launch {
            try{
                _isExportLoading.postValue(true)

                _paymentTypeList.postValue(emptyList<PaymentTypeChartModel>())
                _cashAmt.postValue(0.00f)
                _cardAmt.postValue(0.00f)
                _upiAmt.postValue(0.00f)
                _othersAmt.postValue(0.00f)

                var res = expenseRepository.fnGetPaymentTypeAmtSummaryPerDay(date)
                var list : MutableList<PaymentTypeChartModel> = mutableListOf()
                if(res.isNotEmpty())
                {
                    res.forEach { ob ->
                        list.add(
                            PaymentTypeChartModel(
                                userId = ob.userId,
                                paymentType_CashAmt=ob.paymentType_CashAmt,
                                paymentType_CardAmt=ob.paymentType_CardAmt,
                                paymentType_UpiAmt=ob.paymentType_UpiAmt,
                                paymentType_OthersAmt=ob.paymentType_OthersAmt
                            )
                        )
                        _cashAmt.postValue(ob.paymentType_CashAmt)
                        _cardAmt.postValue(ob.paymentType_CardAmt)
                        _upiAmt.postValue(ob.paymentType_UpiAmt)
                        _othersAmt.postValue(ob.paymentType_OthersAmt)
                    }
                    _paymentTypeList.postValue(list)
                }
                else
                {
                    _paymentTypeList.postValue(emptyList<PaymentTypeChartModel>())
                }
            }
            catch(e : Exception)
            {
                Log.e(LOG_TAG,"Get Payment Type List: ${e.message}")
            }
        }
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
                    var sheet = workBook.createSheet("PAYMENT TYPE REPORT")

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
                    headerCell.setCellValue("PAYMENT TYPE REPORT")
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
                    cell0.setCellValue("PAYMENT TYPE")
                    cell0.cellStyle=tableHeaderStyle
                    var cell1 =tableHeaderRow.createCell(1)
                    cell1.setCellValue("EXPENSE AMOUNT")
                    cell1.cellStyle=tableHeaderStyle

                    //Table Data Row
                    var dataRow4 = sheet.createRow(7)
                    var dataCell40=dataRow4.createCell(0)
                    dataCell40.setCellValue("Cash")
                    dataCell40.cellStyle=dataStyle
                    var dataCell41=dataRow4.createCell(1)
                    dataCell41.setCellValue("${cashAmt.value}")
                    dataCell41.cellStyle=dataStyle

                    //Table Data Row
                    var dataRow5 = sheet.createRow(8)
                    var dataCell50=dataRow5.createCell(0)
                    dataCell50.setCellValue("Upi")
                    dataCell50.cellStyle=dataStyle
                    var dataCell51=dataRow5.createCell(1)
                    dataCell51.setCellValue("${upiAmt.value}")
                    dataCell51.cellStyle=dataStyle

                    //Table Data Row
                    var dataRow6 = sheet.createRow(9)
                    var dataCell60=dataRow6.createCell(0)
                    dataCell60.setCellValue("Card")
                    dataCell60.cellStyle=dataStyle
                    var dataCell61=dataRow6.createCell(1)
                    dataCell61.setCellValue("${cardAmt.value}")
                    dataCell61.cellStyle=dataStyle


                    //Table Data Row
                    var dataRow7 = sheet.createRow(10)
                    var dataCell70=dataRow7.createCell(0)
                    dataCell70.setCellValue("Others")
                    dataCell70.cellStyle=dataStyle
                    var dataCell71=dataRow7.createCell(1)
                    dataCell71.setCellValue("${upiAmt.value}")
                    dataCell71.cellStyle=dataStyle

                    var result = fnExportReportToDownloads(workBook,"PaymentTypeReport_${selectedDate.value}_${Global.fnGetCurrentTime()}.xlsx")

                    if(result)
                    {
                        _exportStatus.value = ResultState1.success(R.string.paymentTypeReport_ExportSuccess)
                    }
                    else
                    {
                        _exportStatus.value = ResultState1.fail(R.string.paymentTypeReport_ExportFailed)
                    }
                }
            }
            catch (e : Exception)
            {
                _isExportLoading.value = false
                _exportStatus.value = ResultState1.fail(R.string.paymentTypeReport_ExportFailed)
                Log.e(LOG_TAG,"Excel File Creation: ${e.message}")
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
            Log.e(LOG_TAG,"Export Payment Type Report To Internal Storage(Document Path): ${e.message}")
            false
        }

    }

    fun fnPreWarmExcelEngine()
    {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wb = XSSFWorkbook()
                wb.createSheet("warmup")
                wb.close()
            }
            catch (e: Exception) {
                Log.e(LOG_TAG,"PreWarm Excel Engine: ${e.message}")
            }
        }
    }

}