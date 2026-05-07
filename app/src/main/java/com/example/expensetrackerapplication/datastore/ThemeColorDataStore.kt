package com.example.expensetrackerapplication.datastore
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetrackerapplication.utils.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore("THEME_COLOR_DATASTORE")
class ThemeColorDataStore (private val context : Context?)
{
    companion object{
        val THEME_COLOR_KEY = stringPreferencesKey("SELECTED_THEME_COLOR")
    }

    suspend fun fnSaveThemeColor(colorCode : Int){
        context?.dataStore?.edit { pref ->
            pref[THEME_COLOR_KEY]=colorCode.toString()
        }
    }

    suspend fun fnGetThemeColor():Int{
        return withContext(Dispatchers.IO){
            context?.dataStore?.data?.map { it[THEME_COLOR_KEY]?.toInt() ?: Global.COLOR_CODE1 }?.first() ?:Global.COLOR_CODE1
        }
    }
}