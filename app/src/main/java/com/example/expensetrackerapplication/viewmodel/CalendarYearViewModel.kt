package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class CalendarYearViewModel(application: Application): AndroidViewModel(application = application)
{
    var _selectedYear = MutableLiveData<String>()
    var selectedYear : LiveData<String> = _selectedYear
}