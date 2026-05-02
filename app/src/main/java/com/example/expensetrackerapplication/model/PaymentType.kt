package com.example.expensetrackerapplication.model

data class PaymentType(
    var cash : Float = 0.00f,
    var card : Float = 0.00f,
    var upi : Float = 0.00f,
    var others : Float = 0.00f
)
