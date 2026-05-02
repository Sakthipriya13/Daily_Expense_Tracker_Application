package com.example.expensetrackerapplication.utils
sealed class Report_Menu() {
    object DayWiseReport : Report_Menu()
    object MonthlyReport : Report_Menu()
    object YearlyReport : Report_Menu()
    object CategoryReport : Report_Menu()
    object PaymentTypeReport : Report_Menu()
}