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

private val Context.dataStore by preferencesDataStore("THEME_COLOR_DATASTORE")
class ThemeColorDataStore (
    private val context : Context?,
    val logger : FileLogger)
{
    companion object{
        val THEME_COLOR_KEY = stringPreferencesKey("SELECTED_THEME_COLOR")
        val LOG_TAG = "THEME_COLOR_DATASTORE"
    }

    suspend fun fnSaveThemeColor(colorCode : Int)
    {
        try {
            context?.dataStore?.edit { pref ->
                pref[THEME_COLOR_KEY]=colorCode.toString()
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Save Theme Color: ${e.message}")
        }
    }

    suspend fun fnGetThemeColor():Int
    {
        return try {
            withContext(Dispatchers.IO){
                context?.dataStore?.data?.map { it[THEME_COLOR_KEY]?.toInt() ?: Global.COLOR_CODE1 }?.first() ?:Global.COLOR_CODE1
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Geet Theme Color: ${e.message}")
            0
        }

    }
}