package com.example.expensetrackerapplication.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetrackerapplication.`object`.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore("THEME_DATASTORE")
class ThemeDataStore(private val context: Context?)
{
     companion object{
         val THEME_KEY = stringPreferencesKey("SELECTED_THEME")
     }

    suspend fun fnSaveTheme(themeCode : Int){
        context?.dataStore?.edit { pref ->
            pref[THEME_KEY] = themeCode.toString()
        }
    }

    suspend fun fnGetTheme(): Int{
        return withContext(Dispatchers.IO){
            context?.dataStore?.data?.map { it[THEME_KEY]?.toInt() ?: Global.THEME_SYSTEM }?.first() ?: Global.THEME_SYSTEM
        }
    }
}