package com.example.expensetrackerapplication

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.repositary.CategoryRepository
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.data.repositary.IncomeRepository
import com.example.expensetrackerapplication.data.repositary.UserRepository
import com.example.expensetrackerapplication.utils.Global
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncWorker(context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        return try {

            Log.i("SYNC_WORKER", "Sync Started")

            val firestore = FirebaseFirestore.getInstance()
            val db = AppDatabase.getdatabase(applicationContext)

            fnSyncCategory(db, firestore)
            fnSyncExpense(db, firestore)
            fnSyncIncome(db, firestore)
            fnSyncUser(db, firestore)

            Log.i("SYNC_WORKER", "Sync Completed")

            Result.success()

        } catch (e: Exception) {

            Log.e("SYNC_WORKER", "Sync Failed: ${e.message}")

            Result.retry()
        }
    }

    private suspend fun fnSyncUser(
        db: AppDatabase,
        firestore: FirebaseFirestore
    ) {
        val repository = UserRepository(db.userDao(), application = applicationContext as Application)
        val users = repository.fnGetUnSyncedCurUserDetails()

        Log.i("SYNC_USER", "Unsynced Users: ${users.size}")

        for (u in users) {

            val docRef = firestore
                .collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)

            val map = hashMapOf(
                "userId" to u.userId,
                "cloudId" to Global.cloudUserId,
                "isSynced" to 1,
                "signUpDate" to u.signUpDate,
                "userName" to u.userName,
                "userEmail" to u.userEmail,
                "userMobileNo" to u.userMobileNo,
                "userPassword" to u.userPassword
//                "userProfilePhotoUri" to u.userProfilePhotoUri
            )
            docRef.set(map).await()

            u.isSynced = 1
            u.cloudId = docRef.id

            repository.fnUpdateCategoryDb(u)
        }
    }

    private suspend fun fnSyncCategory(
        db: AppDatabase,
        firestore: FirebaseFirestore
    ) {

        val repository = CategoryRepository(db.CategoryDao())
        val categories = repository.fnGetUnSyncedCategoryDb()

        Log.i("SYNC_CATEGORY", "Unsynced Categories: UserId: ${Global.lUserId}")

        Log.i("SYNC_CATEGORY", "Unsynced Categories: Size : ${categories.size} And Categories: $categories")

        for (cat in categories) {

            val docRef = firestore
                .collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)
                .collection("Categories")
                .document()

            val map = hashMapOf(
                "userId" to cat.userId,
                "cloudId" to docRef.id,
                "isSynced" to 1,
                "categoryId" to cat.categoryId,
                "signUpDate" to cat.signUpDate,
                "categoryName" to cat.categoryName
            )

            docRef.set(map).await()

            cat.isSynced = 1
            cat.cloudId = docRef.id

            repository.fnUpdateCategoryDb(cat)
        }
    }

    private suspend fun fnSyncExpense(
        db: AppDatabase,
        firestore: FirebaseFirestore
    ) {

        val repository = ExpenseRepository(db.ExpenseDao())
        val expenses = repository.fnGetUnSyncedExpense()

        Log.i("SYNC_EXPENSE", "Unsynced Expenses: ${expenses.size}")

        for (expense in expenses) {

            val docRef = firestore
                .collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)
                .collection("Expenses")
                .document()

            val map = hashMapOf(
                "userId" to expense.userId,
                "cloudId" to docRef.id,
                "isSynced" to 1,
                "expenseId" to expense.expenseId,
                "expenseDate" to expense.expenseDate,
                "expenseAmt" to expense.expenseAmt,
                "expenseCategoryId" to expense.expenseCategoryId,
                "expenseCategoryName" to expense.expenseCategoryName,
                "paymentType" to expense.paymentType,
                "expenseAmtInCash" to expense.expenseAmtInCash,
                "expenseAmtInCard" to expense.expenseAmtInCard,
                "expenseAmtInUpi" to expense.expenseAmtInUpi,
                "expenseAmtInOthers" to expense.expenseAmtInOthers,
                "expenseRemarks" to expense.expenseRemarks,
                "expenseStatus" to expense.expenseStatus
            )

            docRef.set(map).await()

            expense.isSynced = 1
            expense.cloudId = docRef.id

            repository.fnUpdateExpense(expense)
        }
    }

    private suspend fun fnSyncIncome(
        db: AppDatabase,
        firestore: FirebaseFirestore
    ) {
        val repository = IncomeRepository(db.IncomeDao())
        val incomes = repository.fnGetUnSyncedIncomes()

        Log.i("SYNC_INCOME", "Unsynced Incomes: ${incomes.size}")

        for (income in incomes) {

            val docRef = firestore
                .collection("ExpenseTrackerUser")
                .document(Global.cloudUserId)
                .collection("Incomes")
                .document()

            val map = hashMapOf(
                "userId" to income.userId,
                "cloudId" to docRef.id,
                "isSynced" to 1,
                "incomeId" to income.incomeId,
                "date" to income.date,
                "income" to income.income
            )

            docRef.set(map).await()

            income.isSynced = 1
            income.cloudId = docRef.id

            repository.fnUpdateIncome(income)
        }
    }
}