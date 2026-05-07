package com.example.expensetrackerapplication.reusefiles

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.datastore.LanguageDataStore
import com.example.expensetrackerapplication.datastore.ThemeColorDataStore
import com.example.expensetrackerapplication.datastore.ThemeDataStore
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.LocaleManager
import kotlinx.coroutines.runBlocking

abstract class BaseActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        val colorCode = runBlocking {
            ThemeColorDataStore(this@BaseActivity).fnGetThemeColor()
        }
        when(colorCode){
            Global.COLOR_CODE1 -> setTheme(R.style.Theme_App_Color1)
            Global.COLOR_CODE2 -> setTheme(R.style.Theme_App_Color2)
            Global.COLOR_CODE3 -> setTheme(R.style.Theme_App_Color3)
            Global.COLOR_CODE4 -> setTheme(R.style.Theme_App_Color4)
            Global.COLOR_CODE5 -> setTheme(R.style.Theme_App_Color5)
            else -> setTheme(R.style.Theme_App_Color1)
        }

        val themeCode = runBlocking {
            ThemeDataStore(this@BaseActivity).fnGetTheme()
        }

        when(themeCode){
            Global.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )

            Global.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            else -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }

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

    override fun attachBaseContext(newBase: Context?) {
        val languageCode = runBlocking {
//            LanguageDataStore(newBase).languageFlow?.first()
            LanguageDataStore(newBase).fnGetLanguage()
        }
        val context = LocaleManager.fnSetLocale(newBase,languageCode)
        super.attachBaseContext(context)
    }
}