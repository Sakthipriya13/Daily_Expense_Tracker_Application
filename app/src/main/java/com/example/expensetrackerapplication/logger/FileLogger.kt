package com.example.expensetrackerapplication.logger

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.paging.LOG_TAG
import com.google.android.play.core.assetpacks.ca
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogger(private val context: Context) : Logger {
//    private val logFile: File by lazy {
//        File(context.filesDir, "app_logs.txt")
//    }
    // Internal file for Android < 10 fallback
    private val internalLogFile: File by lazy {
        File(context.filesDir, "app_logs.txt")
    }

    val TAG = "FILE_LOGGER"
    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    enum class Level {
        DEBUG, INFO, WARNING, ERROR
    }

    override fun logDebug(tag: String, message: String)
    {
        try
        {
            log(Level.DEBUG, tag, message)
        }
        catch (e: Exception)
        {
            Log.e(TAG,"Log Debug Function: ${e.message}")
        }
    }

    override fun logInfo(tag: String, message: String) {
        try
        {
            log(Level.INFO, tag, message)
        }
        catch (e: Exception)
        {
            Log.e(TAG,"Log Info Function: ${e.message}")
        }
    }

    override fun logWarning(tag: String, message: String) {
        try
        {
            log(Level.WARNING, tag, message)
        }
        catch (e: Exception)
        {
            Log.e(TAG,"Log Warning Function: ${e.message}")
        }
    }

    override fun logError(tag: String, message: String) {
        try
        {
            log(Level.ERROR, tag, message)
        }
        catch (e: Exception)
        {
            Log.e(TAG,"Log Error Function: ${e.message}")
        }    }

    private fun log(level: Level, tag: String, message: String)
    {
        try
        {
            val time = dateFormat.format(Date())
            val logMessage = "$time [$level] $tag: $message\n"

            writeToFile(logMessage)

            when (level) {
                Level.DEBUG -> Log.d(tag, message)
                Level.INFO -> Log.i(tag, message)
                Level.WARNING -> Log.w(tag, message)
                Level.ERROR -> Log.e(tag, message)
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG,"Function Log: ${e.message}")
        }

    }
    private fun writeToFile(msg: String) {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                writeUsingMediaStore(msg)
            }
            else
            {
                writeUsingFile(msg)
            }

        }
        catch (e: Exception)
        {
//            log(Level.ERROR,"FILE_LOGGER","Error Writing Log: ${e.message}")
            Log.e(TAG, "Error Writing Log: ${e.message}")
        }
    }

    // Android 9 and below
    private fun writeUsingFile(msg: String)
    {
        try
        {
            val documentsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            )

            val appFolder = File(documentsDir, "ExpenseTracker")
            if (!appFolder.exists()) {
                appFolder.mkdirs()
            }

            val logFile = File(appFolder, "app_logs.txt")

            if (logFile.exists() && logFile.length() > 1_000_000) {
                logFile.delete()
            }

            FileWriter(logFile, true).use { writer ->
                writer.append(msg)
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG,"Write Using File: ${e.message}")
        }

    }

    // Android 10+
    private fun writeUsingMediaStore(msg: String)
    {
        try
        {
            val fileName = "app_logs.txt"
            val relativePath = Environment.DIRECTORY_DOCUMENTS + "/ExpenseTracker/"

            val collection = MediaStore.Files.getContentUri("external")
            var fileUri = null as android.net.Uri?

            // Step 1: Check if file already exists
            val projection = arrayOf(MediaStore.MediaColumns._ID)

            val selection =
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"

            val selectionArgs = arrayOf(fileName, relativePath)

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->

                if (cursor.moveToFirst())
                {
                    val id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    )

                    fileUri = android.content.ContentUris.withAppendedId(
                        collection,
                        id
                    )
                }
            }

            // Step 2: Create file only if not exists
            if (fileUri == null)
            {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        relativePath
                    )
                }

                fileUri = context.contentResolver.insert(
                    collection,
                    contentValues
                )
            }

            // Step 3: Write / Append
            fileUri?.let { uri ->

                context.contentResolver.openOutputStream(uri, "wa")?.use { outputStream ->
                    outputStream.write(msg.toByteArray())
                }

            } ?: run {

                // fallback internal storage
                if (internalLogFile.exists() &&
                    internalLogFile.length() > 1_000_000)
                {
                    internalLogFile.delete()
                }

                FileWriter(internalLogFile, true).use { writer ->
                    writer.append(msg)
                }
            }

        }
        catch (e: Exception)
        {
            Log.e(TAG,"Write Using Media Store: ${e.message}")
        }
    }



//    private fun writeUsingMediaStore(msg: String)
//    {
//        try
//        {
//            val fileName = "app_logs.txt"
//
//            val contentValues = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
//                put(
//                    MediaStore.MediaColumns.RELATIVE_PATH,
//                    Environment.DIRECTORY_DOCUMENTS + "/ExpenseTracker"
//                )
//            }
//
//            val uri = context.contentResolver.insert(
//                MediaStore.Files.getContentUri("external"),
//                contentValues
//            )
//
//            uri?.let {
//                context.contentResolver.openOutputStream(it, "wa")?.use { outputStream ->
//                    outputStream.write(msg.toByteArray())
//                }
//            } ?: run {
//                // fallback internal storage
//                if (internalLogFile.exists() && internalLogFile.length() > 1_000_000) {
//                    internalLogFile.delete()
//                }
//
//                FileWriter(internalLogFile, true).use { writer ->
//                    writer.append(msg)
//                }
//            }
//        }
//        catch (e: Exception)
//        {
//            Log.e(TAG,"Write Using Media Store: ${e.message}")
//        }
//
//    }
}