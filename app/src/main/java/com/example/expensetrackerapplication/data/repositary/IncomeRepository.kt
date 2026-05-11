package com.example.expensetrackerapplication.data.repositary

import android.util.Log
import androidx.paging.LOG_TAG
import com.example.expensetrackerapplication.data.dao.IncomeDao
import com.example.expensetrackerapplication.data.entity.ExpenseEntity
import com.example.expensetrackerapplication.data.entity.IncomeEntity
import com.example.expensetrackerapplication.utils.Global
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IncomeRepository(val incomeDao : IncomeDao) {

    val LOG_TAG = "INCOME_REPOSITORY"

    val firestore = FirebaseFirestore.getInstance()

//    suspend fun fnInsertIncome(income: IncomeEntity): Long {
//        return incomeDao.fnInsertIncome(income)
//    }

    suspend fun fnUpdateIncome(income: IncomeEntity) {
        incomeDao.fnUpdateIncome(income)
    }
    suspend fun fnGetUnSyncedIncomes(): List <IncomeEntity> {
        return incomeDao.fnGetUnSyncedIncomes(Global.lUserId)
    }
    suspend fun fnInsertAllIncomes(income: List<IncomeEntity>): List <Long> {
        return incomeDao.fnInsertAllIncomes(income)
    }

    suspend fun fnSyncDemo() : Result<String>
    {
        return try{
            val docRef2 = firestore
                .collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)  // replace with Firebase UID
                .collection("Categories_One")
                .document()

            val map2 = hashMapOf(
                "userId" to 1,
                "cloudId" to docRef2.id,
                "isSynced" to 1,
                "categoryId" to 1,
                "signUpDate" to "15-03-206",
                "categoryName" to "Cat1"
            )

            docRef2.set(map2).await()

            return Result.success("Successfully Synced")
        }
        catch(e : Exception){
            Log.e("SYNC DEMO","Sync Demo: ${e.message}")
            Result.failure(Exception("${e.message}"))
        }

    }

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
        catch (e : Exception){
            Log.e("INSERT INCOME TO LOCAL AND CLOUD","Insert Income To Local And Cloud: Failed(${e.message})")
            false
        }
    }

    suspend fun fnGetIncomePerMonth(curMonth : String): Float{
        return try {
            incomeDao.fnGetIncomePerMonth(curMonth,Global.lUserId)
        }
        catch (e : Exception){
            Log.e("GET INCOME PER MONTH","Get Income Per Month: ${e.message}")
            0.0f
        }
    }

    suspend fun fnGetIncomePerMonthAndYear(month : String,year:String): Float{
        return try {
            Log.i(LOG_TAG,"Month From fnGetIncomePerMonthAndYear: $month")
            incomeDao.fnGetIncomePerMonthAndYear(month,year,Global.lUserId)
        }
        catch (e : Exception){
            Log.e("GET INCOME PER MONTH","Get Income Per Month: ${e.message}")
            0.0f
        }
    }

    suspend fun fnGetIncomePerYear(year : String): Float{
        return try {
            incomeDao.fnGetIncomePerYear(year,Global.lUserId)
        }
        catch (e : Exception){
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
            emptyList<IncomeEntity>()
        }
    }
}