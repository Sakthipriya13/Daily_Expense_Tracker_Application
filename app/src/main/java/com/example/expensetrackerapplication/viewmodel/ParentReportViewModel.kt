package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.expensetrackerapplication.logger.FileLogger

class ParentReportViewModel(application: Application, logger: FileLogger): AndroidViewModel(application = application)
{

}