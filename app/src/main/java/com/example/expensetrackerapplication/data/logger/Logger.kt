package com.example.expensetrackerapplication.data.logger

interface Logger
{
    fun logDebug(tag:String,message: String)
    fun logError(tag:String,message: String)
    fun logInfo(tag: String,message: String)
    fun logWarning(tag: String,message: String)

}