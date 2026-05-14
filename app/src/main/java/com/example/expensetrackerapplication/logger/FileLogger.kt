package com.example.expensetrackerapplication.logger

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogger(private val context: Context) : Logger {

    private val logFile: File by lazy {
        File(context.filesDir, "app_logs.txt")
    }

    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    enum class Level {
        DEBUG, INFO, WARNING, ERROR
    }

    override fun logDebug(tag: String, message: String) {
        log(Level.DEBUG, tag, message)
    }

    override fun logInfo(tag: String, message: String) {
        log(Level.INFO, tag, message)
    }

    override fun logWarning(tag: String, message: String) {
        log(Level.WARNING, tag, message)
    }

    override fun logError(tag: String, message: String) {
        log(Level.ERROR, tag, message)
    }

    private fun log(level: Level, tag: String, message: String) {
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

    private fun writeToFile(text: String) {
        try {
            if (logFile.exists() && logFile.length() > 1_000_000) {
                logFile.delete()
            }

            FileWriter(logFile, true).use { writer ->
                writer.append(text)
            }

        }
        catch (e: Exception)
        {
            log(Level.ERROR,"FILE_LOGGER","Error Writing Log: ${e.message}")
            Log.e("FILE_LOGGER", "Error Writing Log: ${e.message}")
        }
    }

//    fun getLogFile(): File = logFile
}