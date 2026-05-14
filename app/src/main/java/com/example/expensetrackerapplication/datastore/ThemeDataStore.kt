package com.example.expensetrackerapplication.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.utils.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore("THEME_DATASTORE")
class ThemeDataStore(
    private val context: Context?,
    val logger : FileLogger)
{
     companion object{
         val THEME_KEY = stringPreferencesKey("SELECTED_THEME")
         val LOG_TAG ="THEME_DATA_STORE"
     }

    suspend fun fnSaveTheme(themeCode : Int)
    {
        try {
            context?.dataStore?.edit { pref ->
                pref[THEME_KEY] = themeCode.toString()
            }
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Save Theme: ${e.message}")
        }

    }

    suspend fun fnGetTheme(): Int{
        return try {
            withContext(Dispatchers.IO){
                context?.dataStore?.data?.map { it[THEME_KEY]?.toInt() ?: Global.THEME_SYSTEM }?.first() ?: Global.THEME_SYSTEM
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Get Theme: ${e.message}")
            0
        }

    }
}