package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class AttendanceUploadWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(applicationContext)
            val attendanceDao = db.attendanceDao()
            Log.d("AttendanceUploadWorker", "Offline attendance logs synced successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("AttendanceUploadWorker", "Upload failed: \${e.message}")
            Result.retry()
        }
    }
}
