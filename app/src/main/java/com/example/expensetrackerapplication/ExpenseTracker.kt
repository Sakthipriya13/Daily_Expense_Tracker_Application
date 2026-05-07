package com.example.expensetrackerapplication

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.expensetrackerapplication.datastore.ThemeDataStore
import com.example.expensetrackerapplication.utils.Global
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument

class ExpenseTracker : Application() {
    val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )
    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            val themeCode = ThemeDataStore(applicationContext).fnGetTheme()
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
        }

    }
}