package com.example.expensetrackerapplication.data.repositary

import android.util.Log
import com.example.expensetrackerapplication.data.dao.IncomeDao
import com.example.expensetrackerapplication.data.entity.IncomeEntity
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.utils.Global
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IncomeRepository(
    val incomeDao : IncomeDao,
    val logger: FileLogger)
{

    val LOG_TAG = "INCOME_REPOSITORY"

    val firestore = FirebaseFirestore.getInstance()

//    suspend fun fnInsertIncome(income: IncomeEntity): Long {
//        return incomeDao.fnInsertIncome(income)
//    }

    suspend fun fnUpdateIncome(income: IncomeEntity) : Boolean
    {
        return try
        {
            var res = incomeDao.fnUpdateIncome(income)
            if(res == 1)
            {
                true
            }
            else{
                false
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Update Income: ${e.message}")
            false
        }
    }
    suspend fun fnGetUnSyncedIncomes(): List <IncomeEntity>
    {
        return try {
            incomeDao.fnGetUnSyncedIncomes(Global.lUserId)
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Get Unsynced Incomes: ${e.message}")
            emptyList<IncomeEntity>()
        }
    }
    suspend fun fnInsertAllIncomes(income: List<IncomeEntity>): List <Long>
    {
        return try {
            incomeDao.fnInsertAllIncomes(income)
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Insert All Incomes: ${e.message}")
            emptyList<Long>()
        }
    }

//    suspend fun fnSyncDemo() : Result<String>
//    {
//        return try{
//            val docRef2 = firestore
//                .collection("ExpenseTrackerUser")
//                .document(Global.cloudUserId)  // replace with Firebase UID
//                .collection("Categories_One")
//                .document()
//
//            val map2 = hashMapOf(
//                "userId" to 1,
//                "cloudId" to docRef2.id,
//                "isSynced" to 1,
//                "categoryId" to 1,
//                "signUpDate" to "15-03-206",
//                "categoryName" to "Cat1"
//            )
//
//            docRef2.set(map2).await()
//
//            return Result.success("Successfully Synced")
//        }
//        catch(e : Exception){
//            Log.e("SYNC DEMO","Sync Demo: ${e.message}")
//            Result.failure(Exception("${e.message}"))
//        }
//
//    }

    suspend fun fnInsertIncome(income: IncomeEntity): Boolean{
        return try {
            var result = incomeDao.fnInsertIncome(income)
            if(result <= 0)
            {
                Log.e("INSERT INCOME TO LOCAL STATUS","Insert Income To Local Status: Failed")
                return false
            }
            true
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Insert Incomes: ${e.message}")
            Log.e("INSERT INCOME TO LOCAL AND CLOUD","Insert Income To Local And Cloud: Failed(${e.message})")
            false
        }
    }

    suspend fun fnGetIncomePerMonth(curMonth : String): Float{
        return try {
            incomeDao.fnGetIncomePerMonth(curMonth,Global.lUserId, Global.INCOME_ADDED)
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Get Income Per Month: ${e.message}")
            Log.e("GET INCOME PER MONTH","Get Income Per Month: ${e.message}")
            0.0f
        }
    }

    suspend fun fnGetIncomePerMonthAndYear(month : String,year:String): Float{
        return try {
            incomeDao.fnGetIncomePerMonthAndYear(month,year,Global.lUserId, Global.INCOME_ADDED)
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Get Income Per Month And Year: ${e.message}")
            Log.e("GET INCOME PER MONTH","Get Income Per Month: ${e.message}")
            0.0f
        }
    }

    suspend fun fnGetIncomePerYear(year : String): Float{
        return try {
            incomeDao.fnGetIncomePerYear(year,Global.lUserId,Global.INCOME_ADDED)
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Get Income Per Year: ${e.message}")
            Log.e("GET INCOME PER YEAR","Get Income Per Year: ${e.message}")
            0.0f
        }
    }

    suspend  fun fnGetIncomesFromCloud() : List<IncomeEntity> {
        return try {
            val incomeCloudList = firestore
                .collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)
                .collection("Incomes")
                .get()
                .await()

            val incomeList = incomeCloudList.toObjects(IncomeEntity::class.java)

            incomeList
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Get Incomes From Cloud: ${e.message}")
            emptyList<IncomeEntity>()
        }
    }

    fun fnGetIncomePerDay(date: String?) : List<IncomeEntity> {
        return try
        {
            Log.i(LOG_TAG,"Get Income Per Date:$date")
            incomeDao.fnGetIncomePerDay(date, Global.INCOME_ADDED)
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Get Income Per Day: ${e.message}")
            emptyList<IncomeEntity>()
        }
    }

    suspend fun fnDeleteIncomePerUser(userId : Int) : Boolean{
        return try
        {
            var incomeCount = incomeDao.fnGetIncomeCountPerUser(userId)
            if(incomeCount > 0)
            {
                var result = incomeDao.fnDeleteIncomePerUserId(userId)
                if(result > 0) true else false
            }
            else
            {
                return true
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Delete User: ${e.message}")
            false
        }
    }

}