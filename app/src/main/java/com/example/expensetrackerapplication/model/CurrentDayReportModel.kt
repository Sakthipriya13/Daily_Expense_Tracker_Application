package com.example.expensetrackerapplication.model

data class CurrentDayReportModel(
    var expenseId : Int?,
    var categoryId : Int?,
    var catgeoryName : String?,
    var expenseAmt : String?,
    var paymentType : String?,
    var expenseRemarks : String?,
    var isDelete : String?
)
