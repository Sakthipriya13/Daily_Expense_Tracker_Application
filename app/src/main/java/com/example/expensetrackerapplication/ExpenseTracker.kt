package com.example.expensetrackerapplication

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class ExpenseTracker : Application() {
    override fun onCreate() {
        super.onCreate()
//        try{
//            Log.i("SYNC_WORKER","Start Sync From Expense Tracker1")
//
//            val uId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//            startSyncWork(uId)
//
//            Log.i("SYNC_WORKER","Start Sync From Expense Tracker2")
//
//        }
//        catch (e : Exception){
//            Log.e("START_SYNC","Start Sync From Expense Tracker (Exception): ${e.message}")
//        }
    }

     fun startSyncWork(uId: String) {
         Log.i("SYNC_WORKER","Start Sync From Expense Tracker3")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest =
//            PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                    .setInputData(
                        workDataOf("Cloud_User_Id" to uId)
                    )
                .build()
//            .getInstance(this)
//            .enqueueUniquePeriodicWork(
        WorkManager.getInstance(this).enqueueUniqueWork(
                "SYNC",
//                ExistingPeriodicWorkPolicy.KEEP,
            ExistingWorkPolicy.KEEP,
            workRequest
            )

    }
}