package com.example.expensetrackerapplication.data.repositary

import android.util.Log
import com.example.expensetrackerapplication.data.dao.ExpenseDao
import com.example.expensetrackerapplication.data.entity.ExpenseEntity
import com.example.expensetrackerapplication.model.CategoryChartModel
import com.example.expensetrackerapplication.model.ExpenseDetailsPerMonth
import com.example.expensetrackerapplication.model.PaymentTypeChartModel
import com.example.expensetrackerapplication.utils.Global
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.collections.List

class ExpenseRepository(val expenseDao: ExpenseDao)
{
    var firestore = FirebaseFirestore.getInstance()
    var firebaseAuth = FirebaseAuth.getInstance()

    suspend fun fnInsertExpense(expense: ExpenseEntity): Long {
        return expenseDao.fnInsertNewExpense(expense)
    }

    suspend fun fnInsertAllExpense(expense: List<ExpenseEntity>): List<Long> {
        return expenseDao.fnInsertAllExpense(expense)
    }

    suspend fun fnUpdateExpense(expense: ExpenseEntity) {
        expenseDao.fnUpdateExpense(expense)
    }

    suspend fun fnGetUnSyncedExpense(): List <ExpenseEntity> {
        return expenseDao.fnGetUnSyncedExpense(Global.lUserId)
    }

    suspend fun fnInsertExpenseDatasToDb(expense : ExpenseEntity) : Boolean
    {
        return try {
            val result = expenseDao.fnInsertNewExpense(expense)

            if (result <= 0) {
                Log.e("INSERT_EXPENSE_STATUS_LOCAL", "Insert Expense To Local Status Failed")
                return false
            }

            true

        }
        catch (e: Exception) {
            Log.e("INSERT_EXPENSE_STATUS", "Insert Expense To Local And Cloud Failed: ${e.message}")
            false
        }

    }
    //28-03-2026
    suspend fun fnGetExpenseDetailsPerDate(date: String?): List<ExpenseEntity>{
        try {
            return expenseDao.fnGetExpensePerDate(date,Global.lUserId,Global.EXPENSE_STATUS_EDITED)
        }
        catch (e : Exception){
            Log.e("GET EXPENSE DETAILS PER DATE","Get Expense Details Per Date: ${e.message}")
            return listOf()
        }
    }



    suspend fun fnDeleteExpense(expenseId: Int?): Boolean {
        try
        {
            var delStatus = expenseDao.fnDeleteExpensePerId(expenseId,Global.EXPENSE_STATUS_DELETED,Global.lUserId)
            if(delStatus>0)
                return true
            else
                return false
        }
        catch (e : Exception){
            Log.e("DELETE EXPENSE","Delete Expense: ${e.message}")
            return false
        }
    }
    //28-03-2026
    suspend fun fnDeleteExpense(expenseId: Int?, expenseDate: String?): Boolean {
        try
        {
            var delStatus = expenseDao.fnDeleteExpensePerId(expenseId,expenseDate,Global.EXPENSE_STATUS_EDITED,Global.lUserId)
            if(delStatus>0)
                return true
            else
                return false
        }
        catch (e : Exception){
            Log.e("DELETE EXPENSE","Delete Expense: ${e.message}")
            return false
        }
    }

    suspend fun fnGetDaySummary(curDate : String): Float
    {
        return try {
            expenseDao.fnGetDaySummary(curDate,Global.lUserId,Global.EXPENSE_STATUS_ADDED)
        }
        catch (e : Exception){
            Log.e("GET CURRENT DAY SUMMARY AMOUNT","Get Current Day Summary Amount: ${e.message}")
            0.0f
        }
    }

    suspend fun fnGetMonthSummary(curMonth : String): Float
    {
        return try {
            expenseDao.fnGetMonthSummary(curMonth,Global.lUserId,Global.EXPENSE_STATUS_ADDED)
        }
        catch (e : Exception){
            Log.e("GET CURRENT DAY SUMMARY AMOUNT","Get Current Day Summary Amount: ${e.message}")
            0.0f
        }
    }

    suspend fun fnGetExpensePerMonthAndYear(month : String,year: String): Float
    {
        return try {
            expenseDao.fnGetExpensePerMonthAndYear(month,year,Global.lUserId,Global.EXPENSE_STATUS_ADDED)
        }
        catch (e : Exception){
            Log.e("GET CURRENT DAY SUMMARY AMOUNT","Get Current Day Summary Amount: ${e.message}")
            0.0f
        }
    }

    suspend fun fnGetYearSummary(curYear : String): Float
    {
        return try {
            expenseDao.fnGetYearSummary(curYear,Global.lUserId,Global.EXPENSE_STATUS_ADDED)
        }
        catch (e : Exception){
            Log.e("GET CURRENT DAY SUMMARY AMOUNT","Get Current Day Summary Amount: ${e.message}")
            0.0f
        }
    }

    suspend fun fnGetCateDetailsPerDay(day: String?): List<CategoryChartModel>
    {
        return try {
            expenseDao.fnGetCatDetailsPerDay(day,Global.EXPENSE_STATUS_ADDED,Global.lUserId)
        }
        catch (e : Exception){
            Log.e("GET CURRENT DAY SUMMARY AMOUNT","Get Current Day Summary Amount: ${e.message}")
            emptyList<CategoryChartModel>()
        }
    }

    suspend fun fnGetCateDetailsPerMonth(month : String): List<CategoryChartModel>
    {
        return try {
            expenseDao.fnGetCatDetailsPerMonth(month,Global.lUserId,Global.EXPENSE_STATUS_ADDED)
        }
        catch (e : Exception){
            Log.e("GET CURRENT DAY SUMMARY AMOUNT","Get Current Day Summary Amount: ${e.message}")
            emptyList<CategoryChartModel>()
        }
    }

    suspend fun fnGetCateDetailsPerYear(year : String): List<CategoryChartModel>
    {
        return try {
            expenseDao.fnGetCatDetailsPerYear(year,Global.lUserId,Global.EXPENSE_STATUS_ADDED)
        }
        catch (e : Exception){
            Log.e("GET CURRENT DAY SUMMARY AMOUNT","Get Current Day Summary Amount: ${e.message}")
            emptyList<CategoryChartModel>()
        }
    }

    suspend fun fnGetPaymentTypeAmtSummaryPerDay(day: String): List<PaymentTypeChartModel>
    {
        return try {
            expenseDao.fnGetPaymentTypesPerDay(day,Global.EXPENSE_STATUS_ADDED,Global.lUserId)
        }
        catch (e : Exception){
            Log.e("GET PAYMENT TYPE SUMMARY AMOUNT PER DAY","Get Payment Type Summary Amount Per Day: ${e.message}")
            emptyList<PaymentTypeChartModel>()
        }
    }

    suspend fun fnGetPaymentTypeAmtSummaryPerMonth(curMonth: String): List<PaymentTypeChartModel>
    {
        return try {
            expenseDao.fnGetPaymentTypesForCurMonth(curMonth,Global.EXPENSE_STATUS_ADDED,Global.lUserId)
        }
        catch (e : Exception){
            Log.e("GET CURRENT MONTH PAYMENT TYPE SUMMARY AMOUNT","Get Current Month Payment Type Summary Amount: ${e.message}")
            emptyList<PaymentTypeChartModel>()
        }
    }

    suspend fun fnGetPaymentTypeAmtSummaryPerYear(curYear: String): List<PaymentTypeChartModel>
    {
        return try {
            expenseDao.fnGetPaymentTypesForCurYear(curYear,Global.EXPENSE_STATUS_ADDED,Global.lUserId)
        }
        catch (e : Exception){
            Log.e("GET CURRENT MONTH PAYMENT TYPE SUMMARY AMOUNT","Get Current Month Payment Type Summary Amount: ${e.message}")
            emptyList<PaymentTypeChartModel>()
        }
    }

    suspend fun fnGetExpenseDetailsPerMonth(month:String,year:String): List<ExpenseDetailsPerMonth>
    {
        return try {
            expenseDao.fnGetExpenseDetailsPerMonth(month,year,Global.EXPENSE_STATUS_ADDED,Global.lUserId)
        }
        catch (e : Exception){
            Log.e("GET EXPENSE PER MONTH","Get Expense Per Month: ${e.message}")
            emptyList<ExpenseDetailsPerMonth>()
        }
    }

    suspend fun fnGetExpenseDetailsPerYear(year:String): List<ExpenseDetailsPerMonth>
    {
        return try {
            expenseDao.fnGetExpenseDataPerYear(year,Global.EXPENSE_STATUS_ADDED,Global.lUserId)
        }
        catch (e : Exception){
            Log.e("GET EXPENSE PER MONTH","Get Expense Per Month: ${e.message}")
            emptyList<ExpenseDetailsPerMonth>()
        }
    }

    suspend  fun fnGetExpensesFromCloud() : List<ExpenseEntity> {
        return try {
            val expenseCloudList = firestore
                .collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)
                .collection("Expenses")
                .get()
                .await()

            val expenseList = expenseCloudList.toObjects(ExpenseEntity::class.java)

            expenseList
        }
        catch (e : Exception)
        {
            emptyList<ExpenseEntity>()
        }
    }

    suspend fun  fnGetExpenseDetails(expenseId: Int?) : List<ExpenseEntity> {
        return try{
            return expenseDao.fnGetExpenseDetailsPerId(expenseId)
        }
        catch(e:Exception){
            Log.e("GET EXPENSE DETAILS","Get Expense Details: ${e.message}")
            emptyList<ExpenseEntity>()
        }
    }


}