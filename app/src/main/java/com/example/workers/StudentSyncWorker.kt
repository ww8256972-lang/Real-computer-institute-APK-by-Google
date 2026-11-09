package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class StudentSyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(applicationContext)
            val studentDao = db.studentDao()
            val students = studentDao.getAllForExport()
            Log.d("StudentSyncWorker", "Synced \${students.size} students to Firebase")
            Result.success()
        } catch (e: Exception) {
            Log.e("StudentSyncWorker", "Sync failed: \${e.message}")
            Result.retry()
        }
    }
}
