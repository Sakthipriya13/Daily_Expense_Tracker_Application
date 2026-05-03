package com.example.expensetrackerapplication.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetrackerapplication.data.logger.Logger
import com.example.expensetrackerapplication.ui.main.fragments.reports.YearlySummaryReport
import com.example.expensetrackerapplication.viewmodel.CategoryWiseReportViewModel
import com.example.expensetrackerapplication.viewmodel.DashBoardViewModel
import com.example.expensetrackerapplication.viewmodel.DayWiseReportViewModel
import com.example.expensetrackerapplication.viewmodel.MonthlySummaryViewModel
import com.example.expensetrackerapplication.viewmodel.NewExpenseViewModel
import com.example.expensetrackerapplication.viewmodel.PaymentTypeReportViewModel
import com.example.expensetrackerapplication.viewmodel.ProfileViewModel
import com.example.expensetrackerapplication.viewmodel.ReportMenuViewModel
import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.YearlySummaryReportViewModel

class AppViewModelFactory(
    private val application: Application,
    private val logger : Logger
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(NewExpenseViewModel::class.java) -> {
                NewExpenseViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(DashBoardViewModel::class.java) ->{
                DashBoardViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->{
                SettingsViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->{
                ProfileViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(ReportMenuViewModel::class.java) ->{
                ReportMenuViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(DayWiseReportViewModel::class.java)->{
                DayWiseReportViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(MonthlySummaryViewModel::class.java)->{
                MonthlySummaryViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(YearlySummaryReport::class.java) ->{
                YearlySummaryReportViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(CategoryWiseReportViewModel::class.java) ->{
                CategoryWiseReportViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(PaymentTypeReportViewModel::class.java) ->{
                PaymentTypeReportViewModel(application, logger) as T
            }
            else ->{
                throw IllegalArgumentException(
                    "Unknown ViewModel Class: ${modelClass.name}"
                )
            }
        }
    }
}