package com.example.expensetrackerapplication.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetrackerapplication.logger.FileLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.collections.get


private val Context.dataStore by preferencesDataStore(name = "language_datastore")

class LanguageDataStore(
    private val context: Context?,
    val logger: FileLogger)
{
    companion object{
        val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        val LOG_TAG = "LANGUAGE_DATASTORE"

    }

    suspend fun fnSaveLanguage(languageCode : String)
    {
        try {
            context?.dataStore?.edit{ preferences ->
                preferences[LANGUAGE_KEY]=languageCode
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Save Language: ${e.message}")
        }
    }

    suspend fun fnGetLanguage():String
    {
        return try {
            context?.dataStore?.data?.map { it[LANGUAGE_KEY] ?: "en"}?.first() ?:"en"
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Get Language: ${e.message}")
            ""
        }
    }

}