package com.example.expensetrackerapplication.utils

sealed class ResultState {
    data class success(var message : String) : ResultState()
    data class fail(var message : String) : ResultState()
}

sealed class ResultState1 {
    data class success(var message : Int) : ResultState1()
    data class fail(var message : Int) : ResultState1()
}