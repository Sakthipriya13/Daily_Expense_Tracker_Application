package com.example.expensetrackerapplication.model

data class CategoryChartModel (
    var userId : Int,
    var categoryId : Int,
    var categoryName : String,
    var expenseAmt : Float
)