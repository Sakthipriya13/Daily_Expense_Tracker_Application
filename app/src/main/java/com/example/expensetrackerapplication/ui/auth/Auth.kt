package com.example.expensetrackerapplication.ui.auth

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.databinding.AuthBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.reusefiles.BaseActivity
import com.example.expensetrackerapplication.viewmodel.AuthViewModel
import kotlin.math.log

class Auth : BaseActivity()
{
    val appViewModelFactory by lazy {
        AppViewModelFactory(
            this.application,
            FileLogger(this.applicationContext)
        )
    }

    val authViewModel : AuthViewModel by viewModels {
        appViewModelFactory
    }

    lateinit var authBinding : AuthBinding

    val logger  = FileLogger(this.applicationContext)

    val LOG_TAG = "AUTH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authBinding = AuthBinding.inflate(layoutInflater)
        authBinding.authViewModel = authViewModel
        authBinding.lifecycleOwner = this
        setContentView(authBinding.root)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//
//            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
//            v.setPadding(
//                v.paddingLeft,
//                v.paddingTop,
//                v.paddingRight,
//                imeInsets.bottom
//            )
//
//            insets
//        }

        hideSystemUI()
    }

    private fun hideSystemUI()
    {
        try
        {
            // Android 11 and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.insetsController?.hide(
                    WindowInsets.Type.navigationBars() or
                            WindowInsets.Type.statusBars()
                )
                window.insetsController?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            // Android 10 and below
            else {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Hide System Ui: ${e.message}")
        }
    }
}