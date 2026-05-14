package com.example.expensetrackerapplication.utils

import android.content.Context
import android.content.res.Configuration
import com.example.expensetrackerapplication.logger.FileLogger
import java.util.Locale

object LocaleManager {
    val LOG_TAG = "LOCALE_MANAGER"
    fun fnSetLocale(context: Context?, language: String?,logger: FileLogger): Context?
    {
        return try
        {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val config = Configuration(context?.resources?.configuration)
            config.setLocale(locale)

            context?.createConfigurationContext(config)
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Set Locale: ${e.message}")
            null
        }
    }
}