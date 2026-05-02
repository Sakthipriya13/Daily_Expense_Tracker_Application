package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
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

    // Selected Report Type Variable Initialization
    var _selectedReportType = MutableLiveData<Report_Menu?>()
    var selectedReportType : LiveData<Report_Menu?> = _selectedReportType

    fun clearReportSelection() 
    {
        try {
            _selectedReportType.value = null
        }
        catch (e: Exception){
            Log.e("REPORT_MENU_VIEW_MODEL","Closed The Report Selection: ${e.message}")
        }
    }
    fun fnShowDayWiseReport()
    {
        try {
            _selectedReportType.value = Report_Menu.DayWiseReport
        }
        catch (e: Exception){
            Log.e("REPORT_MENU_VIEW_MODEL","Day-Wise Report Selection: ${e.message}")
        }
    }

    fun fnShowMonthlySummaryReport()
    {
        try {
            _selectedReportType.value = Report_Menu.MonthlyReport
        }
        catch (e: Exception){
            Log.e("REPORT_MENU_VIEW_MODEL","Monthly Summary Report Selection: ${e.message}")
        }
    }

    fun fnShowCategoryReport()
    {
        try {
            _selectedReportType.value = Report_Menu.CategoryReport
        }
        catch (e: Exception){
            Log.e("REPORT_MENU_VIEW_MODEL","Category Report Selection: ${e.message}")
        }
    }

    fun fnShowPaymentTypeReport()
    {
        try {
            _selectedReportType.value = Report_Menu.PaymentTypeReport
        }
        catch (e: Exception){
            Log.e("REPORT_MENU_VIEW_MODEL","Payment Type Report Selection: ${e.message}")
        }
    }

    fun fnShowYearlyReport()
    {
        try {
            _selectedReportType.value = Report_Menu.YearlyReport
        }
        catch (e: Exception){
            Log.e("REPORT_MENU_VIEW_MODEL","Yearly Report Selection: ${e.message}")
        }
    }
}