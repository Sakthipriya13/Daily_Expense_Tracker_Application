package com.example.expensetrackerapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetrackerapplication.data.entity.IncomeEntity

@Dao
interface IncomeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fnInsertIncome(incomeEntity: IncomeEntity) : Long

    @Query("SELECT SUM(Income) From IncomeTable WHERE SUBSTR(date,6,2)= :curMonth AND UserId= :luserId")
    suspend fun fnGetIncomePerMonth(curMonth:String,luserId : Int) : Float

    @Query("SELECT SUM(Income) From IncomeTable WHERE SUBSTR(date,6,2)= :month AND SUBSTR(date,1,4)= :year AND UserId= :luserId")
    suspend fun fnGetIncomePerMonthAndYear(month:String,year:String,luserId : Int) : Float

    @Query("SELECT SUM(Income) From IncomeTable WHERE SUBSTR(date,1,4)= :curYear AND UserId= :luserId")
    suspend fun fnGetIncomePerYear(curYear:String,luserId : Int) : Float

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fnInsertAllIncomes(income: List<IncomeEntity>): List<Long>

    @Update
    suspend fun fnUpdateIncome(income: IncomeEntity)

    @Query("SELECT * FROM IncomeTable WHERE UserId = :lUserId AND IsSynced=0")
    suspend fun fnGetUnSyncedIncomes(lUserId: Int) : List<IncomeEntity>
}