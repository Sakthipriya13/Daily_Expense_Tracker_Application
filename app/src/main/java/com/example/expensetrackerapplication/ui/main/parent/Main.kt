package com.example.expensetrackerapplication.ui.main.parent

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.databinding.MainBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.ui.auth.parent.Auth
import com.example.expensetrackerapplication.ui.base.BaseActivity
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.viewmodel.MainViewModel
import com.example.expensetrackerapplication.viewmodel.ToastViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Main : BaseActivity() {
    val appViewModelFactory by lazy {
        AppViewModelFactory(
            this.application,
            FileLogger(this.applicationContext)
        )
    }
    lateinit var mainDataBinding: MainBinding
    val mainViewModel : MainViewModel by viewModels {
        appViewModelFactory
    }
    var isExpanded = false
    val fromBottomFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab)
    }

    val toBottomFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab)
    }

    val rotateClockWiseFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_clock_wise)
    }

    val rotateAntiClockWiseFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_anticlock_wise)
    }


    val fromBottomAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim)
    }

    val toBottomAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim)
    }

    private lateinit var navController : NavController

    private lateinit var logger : FileLogger
//        FileLogger(this.applicationContext)
    val TAG = "MAIN"

    val toastViewModel : ToastViewModel by viewModels {
        appViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        logger = FileLogger(this.applicationContext)

        mainDataBinding= MainBinding.inflate(layoutInflater)
        mainDataBinding.mainViewModel=mainViewModel
        mainDataBinding.lifecycleOwner=this
        setContentView(mainDataBinding.root)

        hideSystemUI()

        setSupportActionBar(mainDataBinding.idToolBar)

//        =====================
//        WorkManager.getInstance(this)
//            .getWorkInfosForUniqueWorkLiveData("SYNC_WORK")
//            .observe(this) { list ->
//                list.forEach {
//                    Log.i("SYNC_DEBUG", "Work State: ${it.state}")
//                }
//            }
//        =======================

//        mainViewModel.triggerProfile.observe(this){ isTrigger ->
//            if(isTrigger){
//                lifecycleScope.launch {
//
//                    val path = withContext(Dispatchers.IO) {
//                        mainViewModel.fnGetImage().await()
//                    }
//
//                    path.let {
//                        Log.e("PROFILE URI STRING","Profile Uri String: $it")
//                        if(it!=null && !it.equals("")){
//                            Glide.with(this@Main)
//                                .load(File(it))
//                                .into(mainDataBinding.idUserProfileImg)
//                        }
//                        else{
//                            mainDataBinding.idUserProfileImg.setImageResource(R.drawable.user)
//                            mainDataBinding.idUserProfileImg.imageTintList = ColorStateList.valueOf(
//                                ContextCompat.getColor(this@Main, R.color.text_color_white)
//                            )
//                        }
//                    }
//                }
//
//            }
//        }

//        lifecycleScope.launch {
//
//            val path = withContext(Dispatchers.IO) {
//                mainViewModel.fnGetImage().await()
//            }
//
//            path.let {
//                Log.e("PROFILE URI STRING","Profile Uri String: $it")
//                if(it!=null && !it.equals("")){
//                    Glide.with(this@Main)
//                        .load(File(it))
//                        .into(mainDataBinding.idUserProfileImg)
//                }
//                else{
//                    mainDataBinding.idUserProfileImg.setImageResource(R.drawable.user)
//                    mainDataBinding.idUserProfileImg.imageTintList = ColorStateList.valueOf(
//                        ContextCompat.getColor(this@Main, R.color.text_color_white)
//                    )
//                }
//            }
//        }

//        mainViewModel.profilePath.observe(this){ path ->
//            path.let {
//                Log.e("PROFILE URI STRING","Profile Uri String: $it")
//                if(it!=null && !it.equals("")){
//                    Glide.with(this@Main)
//                        .load(File(it))
//                        .into(mainDataBinding.idUserProfileImg)
//                }
//                else{
//                    mainDataBinding.idUserProfileImg.setImageResource(R.drawable.user)
//                    mainDataBinding.idUserProfileImg.imageTintList = ColorStateList.valueOf(
//                        ContextCompat.getColor(this@Main, R.color.text_color_white)
//                    )
//                }
//            }
//        }

        fnShrinkFab()

        mainViewModel.logoutStatus.observe(this) { isLoggedOut ->
            try
            {
                if(isLoggedOut)
                {
                    Global.lUserId =-1
                    Global.lUserName=""
                    Global.lUserPassword=""
                    Global.lUserMobileNo=""
                    Global.lUssrEmail=""
                    val intent = Intent(this, Auth::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            catch (e: Exception)
            {
                logger.logError(TAG,"Logout Status: ${e.message}")
            }
        }

        mainDataBinding.idMenuFab.setOnClickListener {
            try
            {
                if(isExpanded)
                {
                    fnExpandFab()
                }
                else
                {
                    fnShrinkFab()
                }
            }
            catch (e: Exception)
            {
                logger.logError(TAG,"Menu Fab: ${e.message}")
            }
        }
        mainDataBinding.idReportFab.setOnClickListener {
            try
            {
                navController = findNavController(R.id.idContainer)
                if (navController.currentDestination?.id != R.id.idParentReport) {
                    fnShrinkFab()
                    findNavController(R.id.idContainer).navigate(R.id.idParentReport)
                }
                else {
                    fnShowMessage(getString(R.string.main_AlreadyInReport),this, R.drawable.error_bg,logger,LOG_TAG,toastViewModel)
                }
            }
            catch (e: Exception)
            {
                logger.logError(TAG,"Select Report: ${e.message}")
            }
        }

        mainDataBinding.idDashboardFab.setOnClickListener {
            try
            {
                navController = findNavController(R.id.idContainer)
                if(navController.currentDestination?.id != R.id.idDashBoard)
                {
                    fnShrinkFab()
                    findNavController(R.id.idContainer).navigate(R.id.idDashBoard)
                }
                else
                {
                    fnShowMessage(getString(R.string.main_AlreadyInDashboard),this, R.drawable.error_bg,logger,LOG_TAG,toastViewModel)
                }
            }
            catch (e: Exception)
            {
                logger.logError(TAG,"Select Dashboard: ${e.message}")
            }
        }

        mainDataBinding.idAddExpenseFab.setOnClickListener {
            try
            {
                navController = findNavController(R.id.idContainer)
                if(navController.currentDestination?.id != R.id.idNewExpense)
                {
                    lifecycleScope.launch {
                        fnShrinkFab()
                        delay(200)
                        findNavController(R.id.idContainer).navigate(R.id.idNewExpense)
                    }
                }
                else
                {
                    fnShowMessage(getString(R.string.main_AlreadyInNewExpense),this, R.drawable.error_bg,logger,LOG_TAG,toastViewModel)
                }
            }
            catch (e: Exception)
            {
                logger.logError(TAG,"Select New Expense: ${e.message}")
            }
        }

        mainDataBinding.idSettingsFab.setOnClickListener {
            try
            {
                navController = findNavController(R.id.idContainer)
                if(navController.currentDestination?.id != R.id.idSettings)
                {
                    fnShrinkFab()
                    findNavController(R.id.idContainer).navigate(R.id.idSettings)
                }
                else
                {
                    fnShowMessage(getString(R.string.main_AlreadyInSettings),this, R.drawable.error_bg,logger,LOG_TAG,toastViewModel)
                }
            }
            catch (e: Exception)
            {
                logger.logError(TAG,"Select Settings: ${e.message}")
            }
        }

        mainDataBinding.idProfileFab.setOnClickListener {
            try
            {
                navController = findNavController(R.id.idContainer)
                if(navController.currentDestination?.id != R.id.idProfile)
                {
                    fnShrinkFab()
                    findNavController(R.id.idContainer).navigate(R.id.idProfile)
                }
                else
                {
                    fnShowMessage(getString(R.string.main_AlreadyInProfile),this, R.drawable.error_bg,logger,LOG_TAG,toastViewModel)
                }
            }
            catch (e: Exception)
            {
                logger.logError(TAG,"Select Profile: ${e.message}")
            }
        }

        mainViewModel.displayTransparentBg.observe(this){ isDisplay ->
            try
            {
                if(isDisplay)
                    mainDataBinding.idTransparentBg.visibility= View.VISIBLE
                else
                    mainDataBinding.idTransparentBg.visibility= View.GONE
            }
            catch (e: Exception)
            {
                logger.logError(TAG,"Display Transparent Background: ${e.message}")
            }
        }
    }
    private fun hideSystemUI()
    {
        try
        {
            // Android 11 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.hide(
                    WindowInsets.Type.navigationBars() or
                            WindowInsets.Type.statusBars()
                )
                window.insetsController?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            // Android 10 and below
            else
            {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
        catch (e: Exception)
        {
            logger.logError(TAG,"Hide System Ui: ${e.message}")
        }
    }
    fun fnExpandFab()
    {
        try
        {
            mainDataBinding.idDashboardFab.visibility= View.INVISIBLE
            mainDataBinding.idReportFab.visibility= View.INVISIBLE
            mainDataBinding.idAddExpenseFab.visibility= View.INVISIBLE
            mainDataBinding.idSettingsFab.visibility= View.INVISIBLE
            mainDataBinding.idProfileFab.visibility= View.INVISIBLE

            mainDataBinding.idHomeText.visibility= View.INVISIBLE
            mainDataBinding.idReportText.visibility= View.INVISIBLE
            mainDataBinding.idAddText.visibility= View.INVISIBLE
            mainDataBinding.idSettingsText.visibility= View.INVISIBLE
            mainDataBinding.idProfileText.visibility= View.INVISIBLE

            mainDataBinding.idTransparentBg.visibility= View.INVISIBLE

            mainDataBinding.idTransparentBg.startAnimation(fromBottomAnim)

            mainDataBinding.idMenuFab.startAnimation(rotateClockWiseFabAnim)

            mainDataBinding.idMenuFab.setImageResource(R.drawable.add)


            mainDataBinding.idDashboardFab.startAnimation(fromBottomFabAnim)
            mainDataBinding.idReportFab.startAnimation(fromBottomFabAnim)
            mainDataBinding.idAddExpenseFab.startAnimation(fromBottomFabAnim)
            mainDataBinding.idSettingsFab.startAnimation(fromBottomFabAnim)
            mainDataBinding.idProfileFab.startAnimation(fromBottomFabAnim)

            mainDataBinding.idHomeText.startAnimation(fromBottomFabAnim)
            mainDataBinding.idReportText.startAnimation(fromBottomFabAnim)
            mainDataBinding.idAddText.startAnimation(fromBottomFabAnim)
            mainDataBinding.idSettingsText.startAnimation(fromBottomFabAnim)
            mainDataBinding.idProfileText.startAnimation(fromBottomFabAnim)

            isExpanded = !isExpanded

        }
        catch (e: Exception)
        {
            logger.logError(TAG,"Function Expand Fab: ${e.message}")
        }

    }

    fun fnShrinkFab()
    {
        try
        {
            mainDataBinding.idTransparentBg.startAnimation(toBottomAnim)
            mainDataBinding.idMenuFab.startAnimation(rotateAntiClockWiseFabAnim)

            mainDataBinding.idMenuFab.setImageResource(R.drawable.menu1)

            mainDataBinding.idDashboardFab.startAnimation(toBottomFabAnim)
            mainDataBinding.idAddExpenseFab.startAnimation(toBottomFabAnim)
            mainDataBinding.idReportFab.startAnimation(toBottomFabAnim)
            mainDataBinding.idSettingsFab.startAnimation(toBottomFabAnim)
            mainDataBinding.idProfileFab.startAnimation(toBottomFabAnim)

            mainDataBinding.idHomeText.startAnimation(toBottomFabAnim)
            mainDataBinding.idAddText.startAnimation(toBottomFabAnim)
            mainDataBinding.idReportText.startAnimation(toBottomFabAnim)
            mainDataBinding.idSettingsText.startAnimation(toBottomFabAnim)
            mainDataBinding.idProfileText.startAnimation(toBottomFabAnim)

            isExpanded = !isExpanded


            mainDataBinding.idDashboardFab.visibility= View.GONE
            mainDataBinding.idReportFab.visibility= View.GONE
            mainDataBinding.idAddExpenseFab.visibility= View.GONE
            mainDataBinding.idSettingsFab.visibility= View.GONE
            mainDataBinding.idProfileFab.visibility= View.GONE

            mainDataBinding.idHomeText.visibility= View.GONE
            mainDataBinding.idReportText.visibility= View.GONE
            mainDataBinding.idAddText.visibility= View.GONE
            mainDataBinding.idSettingsText.visibility= View.GONE
            mainDataBinding.idProfileText.visibility= View.GONE

            mainDataBinding.idTransparentBg.visibility= View.GONE

        }
        catch (e: Exception)
        {
            logger.logError(TAG,"Function Shrink Fab: ${e.message}")
        }
    }
}