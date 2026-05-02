package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expensetrackerapplication.utils.Report_Menu

class ReportMenuViewModel(application : Application) : AndroidViewModel(application = application)
{
//    var _showDayWiseReport = MutableLiveData<Boolean>()
//    var showDayWiseReport : LiveData<Boolean> = _showDayWiseReport

//    var _showMonthlySummaryReport = MutableLiveData<Boolean>()
//    var showMonthlySummaryReport : LiveData<Boolean> = _showMonthlySummaryReport

//    var _showCategoryReport = MutableLiveData<Boolean>()
//    var showCategoryReport : LiveData<Boolean> = _showCategoryReport

//    var _showYearlyReport = MutableLiveData<Boolean>()
//    var showYearlyReport : LiveData<Boolean> = _showYearlyReport

//    var _showPaymentTypeReport = MutableLiveData<Boolean>()
//    var showPaymentTypeReport : LiveData<Boolean> = _showPaymentTypeReport

    var _selectReport = MutableLiveData<Report_Menu?>()
    var selectReport : LiveData<Report_Menu?> = _selectReport

    fun clearReportSelection() {
        _selectReport.value = null
    }
    fun fnShowDayWiseReport()
    {
//        _showDayWiseReport.value=true
        _selectReport.value = Report_Menu.DayWiseReport
    }

    fun fnShowMonthlySummaryReport()
    {
//        _showMonthlySummaryReport.value=true
        _selectReport.value = Report_Menu.MonthlyReport
    }

    fun fnShowCategoryReport()
    {
//        _showCategoryReport.value=true
        _selectReport.value = Report_Menu.CategoryReport
    }

    fun fnShowPaymentTypeReport()
    {
//        _showPaymentTypeReport.value=true
        _selectReport.value = Report_Menu.PaymentTypeReport
    }

    fun fnShowYearlyReport(){
//        _showYearlyReport.value = true
        _selectReport.value = Report_Menu.YearlyReport
    }
}