package com.example.expensetrackerapplication.data.repositary

import android.util.Log
import com.example.expensetrackerapplication.data.dao.CategoryDao
import com.example.expensetrackerapplication.data.entity.CategoryEntitty
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.utils.Global
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryRepository(
    val categoryDao: CategoryDao,
    val logger : FileLogger)
{
    val LOG_TAG = "CATEGORY_REPOSITORY"
    var firestore = FirebaseFirestore.getInstance()
    var firebaseAuth = FirebaseAuth.getInstance()

//    suspend fun fnInsertCategoryDb(category: CategoryEntitty): Long {
//        return categoryDao.fnInsertCategories(category)
//    }

    suspend fun fnUpdateCategoryDb(category: CategoryEntitty) {
        try
        {
            categoryDao.fnUpdateCategory(category)

        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Update Category To Db: ${e.message}")
        }
    }

    suspend fun fnGetUnSyncedCategoryDb(): List <CategoryEntitty>
    {
        return try {
            categoryDao.fnGetUnSyncedCategories(Global.lUserId)
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Get Unsynced Categories From Db: ${e.message}")
            emptyList<CategoryEntitty>()
        }
    }
    suspend fun fnInsertAllCategoryDb(category: List<CategoryEntitty>): List <Long>
    {
        return try
        {
            categoryDao.fnInsertAllCategory(category)
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Insert All Category Into Db: ${e.message}")
            emptyList<Long>()
        }
    }

    suspend fun fnInsertCategoriesToDb(category: CategoryEntitty): Boolean
    {
        return try
        {
            val insertStatus=categoryDao.fnInsertCategories(category)
            if(insertStatus<=0)
            {
                Log.e("INSERT CATEGORY STATUS LOCAL","Insert Category Status Local: Failed")
                return false
            }
            true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Insert Category Into Db: ${e.message}")
            Log.e("INSERT CATEGORIES STATUS LOCAL","Insert Categories Status Local: Failed(${e.message})")
            return false
        }

    }

    suspend fun fnInsertDefaultCategoriesToDb(categoryEntity: List<CategoryEntitty>): List<Long>
    {
        try{
            return categoryDao.fnInsertDefaultCategories(categoryEntity)

        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Insert Default Category Into Db: ${e.message}")
            Log.e("INSERT CATEGORIES","Insert Categories: "+e.message)
            return listOf()
        }

    }

    suspend fun fnGetAllCategoriesFromDb() : List<CategoryEntitty>{
        try
        {
            var categoryList = categoryDao.fnGetAllCategories(Global.lUserId, Global.CATEGORY_ADDED)
            return categoryList
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Get All Categories From Db: ${e.message}")
            Log.e("GET CATEGORIES","Get Categories: "+e.message)
            return mutableListOf()
        }
    }


//    suspend fun fnGetDefaultCategoriesFromDb() : List<CategoryEntitty>{
//        return try {
//            var categoryList = categoryDao.fnGetDefaultCategories(Global.lUserId)
//            categoryList
//        }
//        catch (e: Exception)
//        {
//            Log.e("GET CATEGORIES","Get Categories: "+e.message)
//            mutableListOf()
//        }
//    }


    suspend fun fnDeleteCategory(categoryId: Int?, userId: Int?):Boolean{
        return try
        {
//            var res= categoryDao.fnDeleteCategoryFromDb(categoryId = categoryId,userId)
            var res = categoryDao.fnUpdateCategoryDeleteStatus(Global.CATEGORY_DELETED,categoryId,userId,0)
            if(res > 0)
                true
            else
                false
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Delete Category: ${e.message}")
            Log.e("GET CATEGORIES","Get Categories: "+e.message)
            false
        }
    }

    suspend  fun fnGetCategoriesFromCloud() : List<CategoryEntitty> {
        return try {
            val categoryCloudList = firestore
                .collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)
                .collection("Categories")
                .get()
                .await()

            val categoryList = categoryCloudList.toObjects(CategoryEntitty::class.java)

            categoryList
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Get Categories From Cloud: ${e.message}")
            emptyList<CategoryEntitty>()
        }
    }

    suspend fun fnDeleteCategoryPerUser(userId : Int) : Boolean{
        return try
        {
            var cateCount = categoryDao.fnGetCategoryCountPerUser(userId, Global.CATEGORY_ADDED)
            if(cateCount > 0)
            {
                var result = categoryDao.DeleteCategoryPerUserId(userId)
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