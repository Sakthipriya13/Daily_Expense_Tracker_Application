package com.example.expensetrackerapplication.data.repositary

import android.util.Log
import com.example.expensetrackerapplication.data.dao.CategoryDao
import com.example.expensetrackerapplication.data.entity.CategoryEntitty
import com.example.expensetrackerapplication.`object`.Global
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryRepository(val categoryDao: CategoryDao)
{
    var firestore = FirebaseFirestore.getInstance()
    var firebaseAuth = FirebaseAuth.getInstance()

    suspend fun fnInsertCategoryDb(category: CategoryEntitty): Long {
        return categoryDao.fnInsertCategories(category)
    }

    suspend fun fnUpdateCategoryDb(category: CategoryEntitty) {
        categoryDao.fnUpdateCategory(category)
    }

    suspend fun fnGetUnSyncedCategoryDb(): List <CategoryEntitty> {
        return categoryDao.fnGetUnSyncedCategories(Global.lUserId)
    }
    suspend fun fnInsertAllCategoryDb(category: List<CategoryEntitty>): List <Long> {
        return categoryDao.fnInsertAllCategory(category)
    }

    suspend fun fnInsertCategoriesToDb(category: CategoryEntitty): Boolean
    {
        return try{
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
            Log.e("INSERT CATEGORIES","Insert Categories: "+e.message)
            return listOf()
        }

    }

    suspend fun fnGetAllCategoriesFromDb() : List<CategoryEntitty>{
        try {
            var categoryList = categoryDao.fnGetAllCategories(Global.lUserId)
            return categoryList
        }
        catch (e: Exception)
        {
            Log.e("GET CATEGORIES","Get Categories: "+e.message)
            return mutableListOf()
        }
    }


    suspend fun fnGetDefaultCategoriesFromDb() : List<CategoryEntitty>{
        return try {
            var categoryList = categoryDao.fnGetDefaultCategories(Global.lUserId)
            categoryList
        }
        catch (e: Exception)
        {
            Log.e("GET CATEGORIES","Get Categories: "+e.message)
            mutableListOf()
        }
    }


    suspend fun fnDeleteCategory(categoryId : Int, userId : Int):Boolean{
        return try{
            var res= categoryDao.fnDeleteCategoryFromDb(categoryId = categoryId,userId)

            if(res > 0)
                true
            else
                false
        }
        catch (e : Exception){
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
            emptyList<CategoryEntitty>()
        }
    }
}