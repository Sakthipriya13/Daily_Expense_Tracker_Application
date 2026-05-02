package com.example.expensetrackerapplication.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "language_datastore")

class LanguageDataStore(private val context: Context?)
{
    companion object{
        val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }

    suspend fun fnSaveLanguage(languageCode : String){
        context?.dataStore?.edit{ preferences ->
            preferences[LANGUAGE_KEY]=languageCode
        }
    }

//    val languageFlow : Flow<String>? = context?.dataStore?.data?.map{ preferences ->
//        preferences[LANGUAGE_KEY] ?: "en" //Default Language : English
//    }

    suspend fun fnGetLanguage():String{
        return context?.dataStore?.data?.map { it[LANGUAGE_KEY] ?: "en"}?.first() ?:"en"
    }

}