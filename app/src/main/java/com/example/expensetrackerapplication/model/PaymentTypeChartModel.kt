package com.example.expensetrackerapplication.model

data class PaymentTypeChartModel(
    var userId : Int,
    var paymentType_CashAmt : Float,
    var paymentType_UpiAmt : Float,
    var paymentType_CardAmt : Float,
    var paymentType_OthersAmt : Float,
)
