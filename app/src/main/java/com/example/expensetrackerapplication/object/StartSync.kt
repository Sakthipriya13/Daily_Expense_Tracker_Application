package com.example.expensetrackerapplication.`object`

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.expensetrackerapplication.SyncWorker

object StartSync {
    fun startSyncWork(context: Context) {
        fun startSyncWork(uId: String) {
            Log.i("SYNC_WORK  ER","Start Sync From Expense Tracker3")

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
            WorkManager.getInstance(context).enqueueUniqueWork(
                "SYNC",
//                ExistingPeriodicWorkPolicy.KEEP,
                ExistingWorkPolicy.KEEP,
                workRequest
            )

        }

    }
}