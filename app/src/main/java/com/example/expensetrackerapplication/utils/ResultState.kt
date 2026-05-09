package com.example.expensetrackerapplication.utils

sealed class ResultState {
    object Success : ResultState()
    object fail : ResultState()
}

sealed class ResultState1 {
    data class success(var message : Int) : ResultState1()
    data class fail(var message : Int) : ResultState1()
}