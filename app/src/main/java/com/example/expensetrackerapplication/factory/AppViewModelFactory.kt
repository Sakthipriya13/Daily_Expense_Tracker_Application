package com.example.expensetrackerapplication.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.ui.main.fragments.reports.YearlySummaryReport
import com.example.expensetrackerapplication.viewmodel.AddInComeViewModel
import com.example.expensetrackerapplication.viewmodel.AuthViewModel
import com.example.expensetrackerapplication.viewmodel.CalendarMonthViewModel
import com.example.expensetrackerapplication.viewmodel.CalendarYearViewModel
import com.example.expensetrackerapplication.viewmodel.CategoryWiseReportViewModel
import com.example.expensetrackerapplication.viewmodel.ChangePasswordViewModel
import com.example.expensetrackerapplication.viewmodel.DashBoardViewModel
import com.example.expensetrackerapplication.viewmodel.DayWiseReportViewModel
import com.example.expensetrackerapplication.viewmodel.EditExpenseViewModel
import com.example.expensetrackerapplication.viewmodel.ForgetViewModel
import com.example.expensetrackerapplication.viewmodel.LoginViewModel
import com.example.expensetrackerapplication.viewmodel.MainViewModel
import com.example.expensetrackerapplication.viewmodel.MonthlySummaryViewModel
import com.example.expensetrackerapplication.viewmodel.NewExpenseViewModel
import com.example.expensetrackerapplication.viewmodel.ParentReportViewModel
import com.example.expensetrackerapplication.viewmodel.PaymentTypeReportViewModel
import com.example.expensetrackerapplication.viewmodel.ProfileViewModel
import com.example.expensetrackerapplication.viewmodel.ReportMenuViewModel
import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.SignUpViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel
import com.example.expensetrackerapplication.viewmodel.SplitViewModel
import com.example.expensetrackerapplication.viewmodel.YearlySummaryReportViewModel

class AppViewModelFactory(
    private val application: Application,
    private val logger: FileLogger
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
            modelClass.isAssignableFrom(YearlySummaryReportViewModel::class.java) ->{
                YearlySummaryReportViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(CategoryWiseReportViewModel::class.java) ->{
                CategoryWiseReportViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(PaymentTypeReportViewModel::class.java) ->{
                PaymentTypeReportViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->{
                LoginViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(SignUpViewModel::class.java) ->{
                SignUpViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(SplashViewModel::class.java) ->{
                SplashViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(AddInComeViewModel::class.java)->{
                AddInComeViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->{
                AuthViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)->{
                ChangePasswordViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(EditExpenseViewModel::class.java) ->{
                EditExpenseViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(ForgetViewModel::class.java) ->{
                ForgetViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) ->{
                MainViewModel(application, logger) as T
            }
            modelClass.isAssignableFrom(ParentReportViewModel::class.java) ->{
                ParentReportViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(SplitViewModel::class.java) ->{
                SplitViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(CalendarYearViewModel::class.java)->{
                CalendarYearViewModel(application,logger) as T
            }
            modelClass.isAssignableFrom(CalendarMonthViewModel::class.java)->{
                CalendarMonthViewModel(application,logger) as T
            }
            else ->{
                throw IllegalArgumentException(
                    "Unknown ViewModel Class: ${modelClass.name}"
                )
            }
        }
    }
}