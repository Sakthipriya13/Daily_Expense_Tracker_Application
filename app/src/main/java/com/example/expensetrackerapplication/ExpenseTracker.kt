package com.example.expensetrackerapplication

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.paging.LOG_TAG
import com.example.expensetrackerapplication.datastore.ThemeDataStore
import com.example.expensetrackerapplication.logger.FileLogger
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
    private lateinit var logger : FileLogger
    val LOG_TAG = "EXPENSE_TRACKER"

    override fun onCreate() {
        super.onCreate()
        logger = FileLogger(this.applicationContext)
        applicationScope.launch {
            try
            {
                val themeCode = ThemeDataStore(applicationContext,logger).fnGetTheme()
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
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Get Theme From DataStore And Set Theme: ${e.message}")
            }
        }

    }
}