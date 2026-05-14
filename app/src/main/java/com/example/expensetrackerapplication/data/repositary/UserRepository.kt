package com.example.expensetrackerapplication.data.repositary

import android.app.Application
import android.util.Log
import androidx.paging.LOG_TAG
import com.example.expensetrackerapplication.data.dao.UserDao
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.entity.CategoryEntitty
import com.example.expensetrackerapplication.data.entity.ExpenseEntity
import com.example.expensetrackerapplication.data.entity.IncomeEntity
import com.example.expensetrackerapplication.data.entity.UserEntity
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.logger.Logger
import com.example.expensetrackerapplication.utils.Global
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    val userDao: UserDao,
    val application: Application,
    val logger: FileLogger)
{
    val LOG_TAG = "USER_REPOSITORY"
    val firebaseAuth = FirebaseAuth.getInstance()
    var fireStore = FirebaseFirestore.getInstance()

    var incomeInsertStatus = emptyList<Long>()
    var expenseInsertStatus = emptyList<Long>()
    var categoryInsertStatus = emptyList<Long>()

    suspend fun fnGetSignUpUserId(email: String?):Int
    {
        return try {
            userDao.fnGetSignUpUserId(email)
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"User Repository: ${e.message}")
            0
        }
    }

    suspend fun fnUsersCount():Int
    {
        return try
        {
            userDao.fnGetUsersCount()
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"User Count: ${e.message}")
            0
        }
    }

    suspend fun fnInsertUserDetails(user: UserEntity) : Result<String>
    {
        return try {
            // Check username already exists
            val checkUserName = fireStore.collection("UserNames")
                .document(user.userName!!)
                .get()
                .await()

            if (checkUserName.exists()) {
                return Result.failure(Exception("Username already exists"))
            }

            // Firebase Auth Account Creation
            var cloudUserIdRes =
                firebaseAuth.createUserWithEmailAndPassword(user.userEmail!!, user.userPassword!!)
                    .await()

            val cloudUserId = cloudUserIdRes.user!!.uid

            user.cloudId = cloudUserId
            user.isSynced = 1
            Global.cloudUserId = cloudUserId

            // Main User Document
            var docRef1 = fireStore.collection("ExpenseTrackerUser")
                .document(cloudUserId)
            docRef1.set(user).await()

            // Username Mapping
            fireStore.collection("UserNames")
                .document(user.userName!!)
                .set(hashMapOf("userEmail" to user.userEmail))
                .await()

            var userInsertStatus = userDao.fnInsertUser(user)

            if (userInsertStatus > 0) {
                return Result.success("New User Account Successfully Created")
            } else {
                return Result.failure(Exception("New User Account Creation Failed"))
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Insert User Details: ${e.message}")
            Log.e("NEW USER CREATION","New User Creation: ${e.message}")
            Result.failure(Exception("${e.message}"))
        }
    }

    suspend fun fnGetUserDetailsBasedOnUserName(name: String?, password: String?): List<UserEntity>
    {
        return try
        {
            userDao.fnGetUserBasedOnUserName(name,password)
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Get User Details Based On User Name: ${e.message}")
            Log.e(
                "GET USER DETAILS BASED ON USERNAME",
                "Error fetching user details: ${e.message}"
            )
            mutableListOf()
        }
    }

    suspend fun fnDeleteUser(userId : Int) : Boolean{
        return try {
            var result = userDao.fnDeleteUserAccountFromDb(userId)
            if(result > 0) true else false
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Delete User: ${e.message}")
            Log.e(
                "DELETE USER ACCOUNT",
                "Delete User Account: ${e.message}"
            )
            false
        }
    }

    suspend fun fnUpdateLoginUserPassword(currentPassword: String?, userId: Int, newPassword: String?):Boolean{
        return try {
            var result = userDao.fnUpdateUserPassword(newPassword = newPassword,userId, currentPassword = currentPassword)
            if(result>0) true else false
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Update Login User Password: ${e.message}")
            Log.e("UPDATE USER PASSWORD","Update User Password: ${e.message}")
            false
        }
    }

    suspend fun fnResetLoginUserPassword(newPassword: String?, email: String?):Boolean {
        return try {
            var result = userDao.fnResetUserPassword(newPassword = newPassword,email)
            if(result>0) true else false
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Reset Login User Password: ${e.message}")
            Log.e("UPDATE USER PASSWORD","Update User Password: ${e.message}")
            false
        }
    }

    suspend fun fnUpdateLoginUserProfilePhoto(userImgUri: String?, userId: Int):Boolean{
//        return try {
////            var result = userDao.fnUpdateUserProfilePhoto(userImgUri,userId)
////            if(result>0) true else false
//        } catch (e : Exception){
//            Log.e("UPDATE USER PASSWORD","Update User Password: ${e.message}")
//            false
//        }
        return true
    }

    suspend fun fnGetLoginUserProfilePhotoUri(userId: Int):String?{
//        return try {
////             userDao.fnGetUserProfilePhotoUri(userId)
//        }
//        catch (e : Exception)
//        {
//            Log.e("GET USER PROFILE PHOTO","Get User Profile Photo: ${e.message}")
//            null
//        }
        return ""
    }

    suspend fun fnLoginCloudAccount(userName : String?,userPassword:String?): Result<String>
    {
        return try {

            // May 12
//            var getEmailStatus = fireStore.collection("ExpenseTrackerUser")
//                .whereEqualTo("userName",userName)
//                .get()
//                .await()
//            if(getEmailStatus.isEmpty)
//            {
//                Log.i("USER_REPOSITORY","Get Email; $getEmailStatus")
//                return Result.failure<String>(Exception("User Not Found"))
//            }
//
//            val doc = getEmailStatus.documents.first()
//            val cloudUserEmail = doc.getString("userEmail") ?:""

            val userNameDoc = fireStore.collection("UserNames")
                .document(userName!!)
                .get()
                .await()

            if (!userNameDoc.exists()) {
                Log.i("USER_REPOSITORY", "Username Not Found: $userName")
                return Result.failure(Exception("User Not Found"))
            }

            val cloudUserEmail = userNameDoc.getString("userEmail") ?: ""

            if (cloudUserEmail.isEmpty()) {
                return Result.failure(Exception("User Email Not Found"))
            }


            Log.i("USER DETAILS", "cloudUserId Email: $cloudUserEmail")

            firebaseAuth.signInWithEmailAndPassword(cloudUserEmail,userPassword!!)
                .await()

            val cloudUserId = firebaseAuth.currentUser!!.uid
            Global.cloudUserId = cloudUserId

            Log.i("USER DETAILS", "cloudUserId: $cloudUserId")

            var userDoc = fireStore.collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)
                .get()
                .await()

            var userDetails = userDoc.toObject(UserEntity::class.java)

            var user = UserEntity(
                userId = userDetails?.userId ?:0,
                cloudId = userDetails?.cloudId ?:"",
                isSynced = userDetails?.isSynced ?:0,
                signUpDate = userDetails?.signUpDate ?:"",
                userName = userDetails?.userName ?:"",
                userEmail = userDetails?.userEmail ?:"",
                userMobileNo = userDetails?.userMobileNo ?:"",
                userPassword = userDetails?.userPassword ?:"",
//                userProfilePhotoUri = userDetails?.userProfilePhotoUri ?:""
            )

            var userInsertStatus = userDao.fnInsertUser(user)

            if (userInsertStatus !=-1L) {
                Log.i("STORE USER DETAILS", "Store userDetails2: $userInsertStatus")

            } else {
                Log.i("STORE USER DETAILS", "Store userDetails Error: $userInsertStatus")

            }
            val catDao = AppDatabase.getdatabase(application,logger)?.CategoryDao()
            catDao?.let {
                val caterpository = CategoryRepository(catDao,logger)
                var categoryDoc = fireStore.collection("ExpenseTrackerUser")
                    .document(Global.cloudUserId)
                    .collection("Categories")
                    .get()
                    .await()
                var categoryList = categoryDoc.toObjects(CategoryEntitty::class.java)

                Log.i("CATEGORY DETAILS", "Category Details: $categoryList")
                for(cat in categoryList){
                    cat.isSynced = 1
                }
                categoryInsertStatus = caterpository.fnInsertAllCategoryDb(categoryList)
                if(categoryInsertStatus.isNotEmpty() && categoryInsertStatus.all { it != -1L })
                {
                    Log.i("STORE CATEGORY DETAILS", "Store Category Details: Success")
                }
                else
                {
                    logger.logError(LOG_TAG,"Store Category Details: Fail")
                    Log.i("STORE CATEGORY DETAILS", "Store Category Details: Fail")
                }
            }


            val expenseDao = AppDatabase.getdatabase(application,logger)?.ExpenseDao()
            expenseDao?.let {
                val expenserpository = ExpenseRepository(expenseDao,logger)
                var expenseDoc = fireStore.collection("ExpenseTrackerUser")
                    .document(Global.cloudUserId)
                    .collection("Expenses")
                    .get()
                    .await()
                var expenseList = expenseDoc.toObjects(ExpenseEntity::class.java)
                for(ex in expenseList){
                    ex.isSynced = 1
                }
                expenseInsertStatus = expenserpository.fnInsertAllExpense(expenseList)
                if(expenseInsertStatus.isNotEmpty() && expenseInsertStatus.all { it != -1L })
                {
                    Log.i("STORE EXPENSE DETAILS", "Store Expense Details: Success")
                }
                else
                {
                    logger.logError(LOG_TAG,"Store Expense Details: Fail")
                    Log.i("STORE EXPENSE DETAILS", "Store Expense Details: Fail")
                }
            }

            val incomeDao = AppDatabase.getdatabase(application,logger)?.IncomeDao()
            incomeDao?.let {
                val incomerpository = IncomeRepository(incomeDao,logger)

                var incomeDoc = fireStore.collection("ExpenseTrackerUser")
                    .document(Global.cloudUserId)
                    .collection("Incomes")
                    .get()
                    .await()
                var incomeList = incomeDoc.toObjects(IncomeEntity::class.java)
                for(income in incomeList){
                    income.isSynced = 1
                }
                incomeInsertStatus = incomerpository.fnInsertAllIncomes(incomeList)
                if(incomeInsertStatus.isNotEmpty() && incomeInsertStatus.all { it != -1L })
                {
                    Log.i("STORE INCOME DETAILS", "Store INCOME Details: Success")
                }
                else
                {
                    logger.logError(LOG_TAG,"Store INCOME Details: Fail")
                    Log.i("STORE INCOME DETAILS", "Store INCOME Details: Fail")
                }

            }

            if(incomeInsertStatus.isNotEmpty() && incomeInsertStatus.all { it != -1L } &&
                expenseInsertStatus.isNotEmpty() && expenseInsertStatus.all { it != -1L } &&
                categoryInsertStatus.isNotEmpty() && categoryInsertStatus.all { it != -1L } &&
                userInsertStatus !=-1L)
            {
                Global.lUserId = userDetails?.userId ?:0
                Global.lUserName= userDetails?.userName ?:""
                Global.lUserPassword=userDetails?.userPassword ?:""
                Global.lUserMobileNo=userDetails?.userMobileNo ?:""
                Global.lUssrEmail=userDetails?.userEmail ?:""

                return Result.success("User Successfully Login")
            }
           else{
                return Result.success("User Not Found")
            }

        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Login Cloud Account: ${e.message}")
            Log.e("LOGIN CLOUD ACCOUNT","Login Cloud Account : ${e.message}")
            return Result.failure(Exception("Login Cloud Account: ${e.message}"))
        }
    }

    suspend  fun fnAddImageUri(imageUri: String) {
//        userDao.fnUpdateProfileUri(imageUri,Global.cloudUserId)

    }

    suspend fun fnGetProfileImageUri(): String {
//        return userDao.fnGetProfileUri(Global.cloudUserId,Global.lUserId)
        return ""
    }

    suspend fun fnGetUnSyncedCurUserDetails(): List<UserEntity>
    {
        return try {
            userDao.fnGetUnSyncedCurUserDetails(Global.lUserId)
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Get Unsynced User Details: ${e.message}")
            emptyList<UserEntity>()
        }
    }

    suspend fun fnUpdateCategoryDb(user: UserEntity)
    {
        try {
            userDao.fnUpdateCurUserDetails(user)
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Update Category: ${e.message}")
        }
    }

    suspend fun isEmailExistsFun(email: String?) : Boolean{
        return try
        {
            var userDetails = userDao.isEmailExistsFun(email)
            if(userDetails.isNotEmpty() && userDetails !=null)
            {
                true
            }
            else{
                false
            }
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Is Email Exists: ${e.message}")
            Log.e("IS EMAIL EXISTS","Is Email Exists: ${e.message}")
            false
        }
    }
}