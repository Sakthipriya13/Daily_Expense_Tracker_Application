package com.example.expensetrackerapplication.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import com.example.expensetrackerapplication.data.entity.UserEntity
import com.example.expensetrackerapplication.logger.FileLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.collections.get

private val Context.dataStore by preferencesDataStore(name="Login_DataStore")

class LoginDataStore(
    private val context : Context?,
    val logger : FileLogger)
{
    companion object{
        val USER_ID = stringPreferencesKey("USER_ID")

        val USER_NAME = stringPreferencesKey("USER_NAME")

        val USER_MOBILE_NO = stringPreferencesKey("USER_MOBILE_NO")

        val USER_EMAIL = stringPreferencesKey("USER_EMAIL")

        val USER_PASSWORD = stringPreferencesKey("USER_PASSWORD")

        val USER_CLOUD_ID = stringPreferencesKey("USER_CLOUD_ID")

        val SYNC_STATUS = stringPreferencesKey(name="SYNC_STATUS")

        val SIGN_UP_DATE = stringPreferencesKey(name = "SIGN_UP_DATE")

        val LOG_TAG = "LOGIN_DATASTORE"

    }

    suspend fun fnSaveUser(userId : Int,userName:String,userMobileNo:String,userEmail : String,
                           userPassword:String,userCloudId:String,syncStatus : Int,signUpDate : String ){
        try
        {
            context?.dataStore?.edit { pref ->
                pref[USER_ID] = userId.toString()
                pref[USER_NAME] = userName
                pref[USER_MOBILE_NO] = userMobileNo
                pref[USER_EMAIL] = userEmail
                pref[USER_PASSWORD] = userPassword
                pref[USER_CLOUD_ID] = userCloudId
                pref[SYNC_STATUS] = syncStatus.toString()
                pref[SIGN_UP_DATE] = signUpDate
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Save User: ${e.message}")
        }
    }

    val userDatas: Flow<UserEntity>? = context?.dataStore?.data
        ?.catch { e ->
            logger.logError(LOG_TAG, "Get User Datas: ${e.message}")
            emit(emptyPreferences())
        }
        ?.map { pref ->
            UserEntity(
                userId = pref[USER_ID]?.toInt() ?: 0,
                cloudId = pref[USER_CLOUD_ID] ?: "",
                isSynced = pref[SYNC_STATUS]?.toInt() ?: 0,
                userName = pref[USER_NAME] ?: "",
                userEmail = pref[USER_EMAIL] ?: "",
                userPassword = pref[USER_PASSWORD] ?: "",
                userMobileNo = pref[USER_MOBILE_NO] ?: "",
                signUpDate = pref[SIGN_UP_DATE] ?: ""
            )
        }

//    val userDatas : Flow<UserEntity>? =
//        context?.dataStore?.data?.map { pref ->
//            UserEntity(
//                userId = pref[USER_ID]?.toInt() ?: 0,
//                cloudId = pref[USER_CLOUD_ID] ?: "",
//                isSynced = pref[SYNC_STATUS]?.toInt() ?: 0,
//                userName = pref[USER_NAME] ?: "",
//                userEmail = pref[USER_EMAIL] ?: "",
//                userPassword = pref[USER_PASSWORD] ?: "",
//                userMobileNo = pref[USER_MOBILE_NO] ?: "",
//                signUpDate = pref[SIGN_UP_DATE] ?: ""
//            )
//        }
//


}