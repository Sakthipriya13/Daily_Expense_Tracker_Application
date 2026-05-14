package com.example.expensetrackerapplication.ui.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.datastore.LanguageDataStore
import com.example.expensetrackerapplication.datastore.ThemeColorDataStore
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.LocaleManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class BaseActivity : AppCompatActivity()
{
    private lateinit var logger : FileLogger

    val LOG_TAG = "BASE_ACTIVITY"

    override fun attachBaseContext(newBase: Context?)
    {
        if (newBase == null) {
            super.attachBaseContext(newBase)
            return
        }

        val safeLogger = FileLogger(newBase.applicationContext)

        try {
            val languageCode = runBlocking {
                LanguageDataStore(newBase, logger = safeLogger).fnGetLanguage()
            }

            val context = LocaleManager.fnSetLocale(newBase, languageCode, safeLogger)
            super.attachBaseContext(context)

        } catch (e: Exception) {
            safeLogger.logError(LOG_TAG, "Attach Base Context: ${e.message}")
            super.attachBaseContext(newBase)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        logger = FileLogger(this.applicationContext)
        try {
            val colorCode = runBlocking {
                ThemeColorDataStore(this@BaseActivity,logger).fnGetThemeColor()
            }
            when(colorCode){
                Global.COLOR_CODE1 -> setTheme(R.style.Theme_App_Color1)
                Global.COLOR_CODE2 -> setTheme(R.style.Theme_App_Color2)
                Global.COLOR_CODE3 -> setTheme(R.style.Theme_App_Color3)
                Global.COLOR_CODE4 -> setTheme(R.style.Theme_App_Color4)
                Global.COLOR_CODE5 -> setTheme(R.style.Theme_App_Color5)
                else -> setTheme(R.style.Theme_App_Color1)
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Get Theme Color From DataStore And Set Theme Color: ${e.message}")
        }

//        }

//        val colorCode = runBlocking {
//            ThemeColorDataStore(this@BaseActivity).fnGetThemeColor()
//        }
//        when(colorCode){
//            Global.COLOR_CODE1 -> setTheme(R.style.Theme_App_Color1)
//            Global.COLOR_CODE2 -> setTheme(R.style.Theme_App_Color2)
//            Global.COLOR_CODE3 -> setTheme(R.style.Theme_App_Color3)
//            Global.COLOR_CODE4 -> setTheme(R.style.Theme_App_Color4)
//            Global.COLOR_CODE5 -> setTheme(R.style.Theme_App_Color5)
//            else -> setTheme(R.style.Theme_App_Color1)
//        }

//        val themeCode = runBlocking {
//            ThemeDataStore(this@BaseActivity).fnGetTheme()
//        }
//
//        when(themeCode){
//            Global.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(
//                AppCompatDelegate.MODE_NIGHT_YES
//            )
//
//            Global.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(
//                AppCompatDelegate.MODE_NIGHT_NO
//            )
//            else -> AppCompatDelegate.setDefaultNightMode(
//                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
//            )
//        }

        super.onCreate(savedInstanceState)
    }

//    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        themeColorDataStore = ThemeColorDataStore(this@BaseActivity)
//        fnApplyThemeColor()
//        super.onCreate(savedInstanceState, persistentState)
//    }

//    fun fnApplyThemeColor(){
//        lifecycleScope.launch {
//            when(themeColorDataStore.fnGetThemeColor()){
//                Global.COLOR_CODE1 -> setTheme(R.style.Theme_App_Color1)
//                Global.COLOR_CODE2 -> setTheme(R.style.Theme_App_Color2)
//                Global.COLOR_CODE3 -> setTheme(R.style.Theme_App_Color3)
//                Global.COLOR_CODE4 -> setTheme(R.style.Theme_App_Color4)
//                Global.COLOR_CODE5 -> setTheme(R.style.Theme_App_Color5)
//                else -> setTheme(R.style.Theme_App_Color1)
//            }
//        }
//    }
}