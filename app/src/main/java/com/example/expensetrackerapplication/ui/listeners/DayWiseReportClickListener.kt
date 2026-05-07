package com.example.expensetrackerapplication.ui.listeners

import com.example.expensetrackerapplication.model.CurrentDayReportModel

interface DayWiseReportClickListener
{
    fun onDeleteClick(expense : CurrentDayReportModel)

    fun onClickEdit(expense : CurrentDayReportModel)

}