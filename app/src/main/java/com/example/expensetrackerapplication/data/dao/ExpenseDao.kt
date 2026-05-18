package com.example.expensetrackerapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetrackerapplication.data.entity.ExpenseEntity
import com.example.expensetrackerapplication.model.CategoryChartModel
import com.example.expensetrackerapplication.model.ExpenseDetailsPerMonth
import com.example.expensetrackerapplication.model.PaymentTypeChartModel

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fnInsertNewExpense(expenseEntity:ExpenseEntity) : Long

    @Insert
    suspend fun fnInsertAllExpense(expense: List<ExpenseEntity>) : List<Long>

//    @Query("SELECT * FROM ExpenseTable")
//    suspend fun fnGetAllExpense(): List<ExpenseEntity>

//    @Query("SELECT * FROM ExpenseTable WHERE ExpenseDate= :date AND UserId= :luserId")
//    suspend fun fnGetExpensePerDate(date: String?,luserId : Int): List<ExpenseEntity>

    @Query("SELECT * FROM ExpenseTable WHERE ExpenseDate= :date AND UserId= :luserId AND ExpenseStatus != :editStatus")
    suspend fun fnGetExpensePerDate(date: String?,luserId : Int,editStatus : Int): List<ExpenseEntity>

    //28-03-2026
    @Query("UPDATE ExpenseTable SET ExpenseStatus = :delExpense,IsSynced=0 WHERE expenseId= :id AND UserId= :luserId AND ExpenseStatus != :delExpense")
    suspend fun fnDeleteExpensePerId(id: Int?, delExpense: Int,luserId : Int) : Int
    //28-03-2026
    @Query("UPDATE ExpenseTable SET ExpenseStatus = :delExpense,IsSynced=0 WHERE expenseId= :expenseId AND ExpenseDate = :expenseDate AND UserId= :luserId AND ExpenseStatus != :delExpense")
    suspend fun fnDeleteExpensePerId(
        expenseId: Int?,
        expenseDate: String?, delExpense: Int,
        luserId: Int
    ) : Int

    @Query("SELECT SUM(ExpenseAmountt) FROM ExpenseTable WHERE ExpenseDate= :curDate AND UserId= :luserId AND ExpenseStatus = :expenseStatus")
    suspend fun fnGetDaySummary(curDate : String,luserId : Int,expenseStatus : Int):Float

    @Query("SELECT SUM(ExpenseAmountt) FROM ExpenseTable WHERE SUBSTR(ExpenseDate,6,2)= :curMonth AND UserId= :luserId AND ExpenseStatus = :expenseStatus")
    suspend fun fnGetMonthSummary(curMonth : String,luserId : Int,expenseStatus : Int):Float

    @Query("SELECT SUM(ExpenseAmountt) FROM ExpenseTable WHERE SUBSTR(ExpenseDate,6,2)= :month AND SUBSTR(ExpenseDate,1,4)= :year " +
            "AND UserId= :luserId AND ExpenseStatus = :expenseStatus")
    suspend fun fnGetExpensePerMonthAndYear(month : String,year: String,luserId : Int,expenseStatus : Int):Float

    @Query("SELECT SUM(ExpenseAmountt) FROM ExpenseTable WHERE SUBSTR(ExpenseDate,1,4)= :curYear " +
            "AND UserId= :luserId AND ExpenseStatus = :expenseStatus")
    suspend fun fnGetYearSummary(curYear : String,luserId : Int,expenseStatus : Int):Float

    @Query("SELECT UserId AS userId" +
            ",ExpenseCategoryId AS categoryId" +
            ",ExpenseCategoryName AS categoryName" +
            ",SUM(ExpenseAmountt) As expenseAmt" +
            " FROM ExpenseTable WHERE ExpenseDate= :day AND ExpenseStatus = :expenseStatus AND UserId= :luserId " +
            "GROUP BY ExpenseCategoryId")
    suspend fun fnGetCatDetailsPerDay(day : String?,expenseStatus : Int,luserId : Int): List<CategoryChartModel>


    @Query(
        "SELECT UserId AS userId" +
                ",ExpenseCategoryId AS categoryId" +
                ",ExpenseCategoryName AS categoryName" +
                ",SUM(ExpenseAmountt) As expenseAmt" +
                " FROM ExpenseTable WHERE SUBSTR(ExpenseDate,6,2)= :month AND UserId= :luserId AND ExpenseStatus = :expenseStatus " +
                "GROUP BY ExpenseCategoryId"
    )
    suspend fun fnGetCatDetailsPerMonth(month : String,luserId : Int,expenseStatus : Int,): List<CategoryChartModel>

    @Query("SELECT UserId AS userId" +
            ",ExpenseCategoryId AS categoryId" +
            ",ExpenseCategoryName AS categoryName" +
            ",SUM(ExpenseAmountt) As expenseAmt" +
            " FROM ExpenseTable WHERE SUBSTR(ExpenseDate,1,4)= :year AND UserId= :luserId AND ExpenseStatus = :expenseStatus" +
            " GROUP BY ExpenseCategoryId"
    )
    suspend fun fnGetCatDetailsPerYear(year : String,luserId : Int,expenseStatus : Int,): List<CategoryChartModel>

    @Query("SELECT UserId AS userId,SUM(ExpenseAmountt) AS totalExpense,SUM(ExpenseAmtInCash) AS paymentType_CashAmt,SUM(ExpenseAmtInCard) AS paymentType_CardAmt," +
            "SUM(ExpenseAmtInUpi) AS paymentType_UpiAmt,SUM(ExpenseAmtInOthers) AS paymentType_OthersAmt " +
            "FROM ExpenseTable WHERE SUBSTR(ExpenseDate,6,2)= :curMonth AND ExpenseStatus = :expenseStatus AND UserId= :luserId " +
            "GROUP BY ExpenseAmtInCash AND ExpenseAmtInCard AND ExpenseAmtInUpi AND ExpenseAmtInOthers")
    suspend fun fnGetPaymentTypesForCurMonth(curMonth : String,expenseStatus : Int,luserId : Int) : List<PaymentTypeChartModel>

    @Query("SELECT UserId AS userId,SUM(ExpenseAmountt) AS totalExpense,SUM(ExpenseAmtInCash) AS paymentType_CashAmt,SUM(ExpenseAmtInCard) AS paymentType_CardAmt," +
            "SUM(ExpenseAmtInUpi) AS paymentType_UpiAmt,SUM(ExpenseAmtInOthers) AS paymentType_OthersAmt " +
            "FROM ExpenseTable WHERE ExpenseDate= :day AND ExpenseStatus = :expenseStatus " +
            "AND UserId= :luserId " +
            "GROUP BY ExpenseAmtInCash AND ExpenseAmtInCard AND ExpenseAmtInUpi AND ExpenseAmtInOthers")
    suspend fun fnGetPaymentTypesPerDay(day : String,expenseStatus : Int,luserId : Int) : List<PaymentTypeChartModel>

    @Query("SELECT UserId AS userId,SUM(ExpenseAmountt) AS totalExpense,SUM(ExpenseAmtInCash) AS paymentType_CashAmt,SUM(ExpenseAmtInCard) AS paymentType_CardAmt," +
            "SUM(ExpenseAmtInUpi) AS paymentType_UpiAmt,SUM(ExpenseAmtInOthers) AS paymentType_OthersAmt FROM ExpenseTable WHERE SUBSTR(ExpenseDate,1,4)= :curYear " +
            "AND ExpenseStatus = :expenseStatus AND UserId= :luserId GROUP BY ExpenseAmtInCash AND ExpenseAmtInCard AND ExpenseAmtInUpi AND ExpenseAmtInOthers")
    suspend fun fnGetPaymentTypesForCurYear(curYear : String,expenseStatus : Int,luserId : Int) : List<PaymentTypeChartModel>


    @Query("""
    WITH RECURSIVE days(dayNo) AS (
        SELECT 1
        UNION ALL
        SELECT dayNo + 1
        FROM days
        WHERE dayNo < CAST(strftime('%d',
                date(:year || '-' || :month || '-01',
                '+1 month',
                '-1 day')) AS INTEGER)
    )
    SELECT
        d.dayNo AS expenseDate,
        COUNT(e.expenseId) AS transactionsCount,
        COALESCE(SUM(e.ExpenseAmountt), 0) AS expenseSummaryAmt
    FROM days d
    LEFT JOIN ExpenseTable e
        ON CAST(strftime('%d', e.ExpenseDate) AS INTEGER) = d.dayNo
        AND strftime('%m', e.ExpenseDate) = :month
        AND strftime('%Y', e.ExpenseDate) = :year
        AND e.ExpenseStatus = :expenseStatus
        AND e.UserId = :lUserId
    GROUP BY d.dayNo
    ORDER BY d.dayNo
""")
    suspend fun fnGetExpenseDetailsPerMonth(month : String,year : String,expenseStatus : Int,lUserId : Int) : List<ExpenseDetailsPerMonth>



    @Query("""
        WITH months(monthNo) As (
            SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL 
            SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL 
            SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT ALL 12
        )
        SELECT
            m.monthNo AS expenseDate,
            COUNT(e.expenseId) AS transactionsCount,
            COALESCE(SUM(e.ExpenseAmountt),0) as expenseSummaryAmt
        FROM months as m 
        LEFT JOIN ExpenseTable AS e 
        ON CAST(strftime('%m', e.ExpenseDate) AS INTEGER) = m.monthNo
    AND strftime('%Y', e.ExpenseDate) = :year
    AND e.ExpenseStatus = :expenseStatus
    AND e.UserId = :lUserId
        GROUP BY m.monthNo
        ORDER BY m.monthNo
    """)
    suspend fun fnGetExpenseDataPerYear(year : String,expenseStatus : Int,lUserId : Int): List<ExpenseDetailsPerMonth>

    @Update
    suspend fun fnUpdateExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM ExpenseTable WHERE UserId = :lUserId AND IsSynced=0")
    suspend fun fnGetUnSyncedExpense(lUserId: Int) : List<ExpenseEntity>

    @Query("SELECT * FROM ExpenseTable WHERE expenseId = :expenseId AND ExpenseStatus= :status")
    suspend fun fnGetExpenseDetailsPerId(expenseId: Int?,status: Int): List<ExpenseEntity>

    @Query("DELETE FROM ExpenseTable WHERE UserId= :userId")
    suspend fun DeleteExpensePerUserId(userId:Int,deleteStatus: Int): Int

    @Query("SELECT COUNT(*) FROM ExpenseTable WHERE UserId= :userId AND ExpenseStatus= :status ")
    suspend fun fnGetExpenseCountPerUser(userId: Int,status: Int) : Int

    @Query("SELECT COUNT(*) FROM ExpenseTable WHERE UserId= :userId AND ExpenseStatus= :status AND ExpenseCategoryId= :categoryId")
    suspend fun fnCheckExpenseExistForSelectedCate(userId: Int?, categoryId: Int?, status: Int) : Int

}