package com.example.expensetrackerapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetrackerapplication.data.entity.CategoryEntitty

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fnInsertCategories(categoryEntity: CategoryEntitty) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fnInsertDefaultCategories(categoryEntity: List<CategoryEntitty>) : List<Long>

    @Query("Select * from Categories WHERE UserId= :lUserId")
    suspend fun fnGetAllCategories(lUserId :Int) : List<CategoryEntitty>

    @Query("Select * from Categories WHERE UserId= :lUserId LIMIT 5")
    suspend fun fnGetDefaultCategories(lUserId :Int) : List<CategoryEntitty>

    @Query("DELETE FROM Categories WHERE categoryId = :categoryId AND UserId= :userId")
    suspend fun fnDeleteCategoryFromDb(categoryId : Int?, userId : Int?) : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fnInsertAllCategory(list: List<CategoryEntitty>): List<Long>

    @Update
    suspend fun fnUpdateCategory(expense: CategoryEntitty)

    @Query("SELECT * FROM Categories WHERE UserId = :lUserId AND IsSynced=0")
    suspend fun fnGetUnSyncedCategories(lUserId: Int) : List<CategoryEntitty>



}