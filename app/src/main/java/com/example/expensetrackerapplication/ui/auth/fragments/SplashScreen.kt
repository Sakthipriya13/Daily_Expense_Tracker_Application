package com.example.expensetrackerapplication.ui.auth.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.databinding.SplashScreenBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SplashScreen.newInstance] factory method to
 * create an instance of this fragment.
 */
class SplashScreen : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }

    private val viewModel: SplashViewModel by viewModels{
        appViewModelFactory
    }
    private lateinit var splashDataBinding: SplashScreenBinding

    private lateinit var topAnimation : Animation
    private lateinit var bottomAnimation : Animation

    val settingsViewModel : SettingsViewModel  by activityViewModels{
        appViewModelFactory
    }

    private lateinit var logger : FileLogger
//        FileLogger(requireContext().applicationContext)
    val LOG_TAG = "SPLASH_SCREEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        logger = FileLogger(requireContext().applicationContext)

        splashDataBinding= DataBindingUtil.inflate(inflater,R.layout.splash_screen,container,false)

//        viewModel= SplashViewModel(requireContext())

        splashDataBinding.splashViewModel=viewModel
        splashDataBinding.lifecycleOwner=viewLifecycleOwner

        // Inflate the layout for this fragment
        return splashDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            topAnimation= AnimationUtils.loadAnimation(requireContext(), R.anim.top_animation)
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Assign Top Animation: ${e.message}")
        }

        try {
            bottomAnimation= AnimationUtils.loadAnimation(requireContext(),R.anim.bottom_animation)
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Assign Bottom Animation: ${e.message}")
        }

        try {
            splashDataBinding.idAppLogo.animation=topAnimation
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Assign Top Animation To Logo: ${e.message}")
        }

//        splashDataBinding.idAppName.animation=topAnimation

//        lifecycleScope.b launchWhenStarted {
//            settingsViewModel.fnInsertCategories()
//            settingsViewModel.fnGetAllCategories()
//        }

//        lifecycleScope.launch {
//            viewModel._cloudUserId.value=fnGetCloudUserId()
//        }

        lifecycleScope.launchWhenStarted {
            viewModel.navigateToLogin.collect { shouldNavigate ->
                try
                {
                    if(shouldNavigate)
                    {
                        findNavController().navigate(R.id.action_splash_to_login)
                    }
                }
                catch (e: Exception)
                {
                    logger.logError(LOG_TAG,"Navigate To Login: ${e.message}")
                }
            }
        }
    }
//    suspend fun fnGetCloudUserId(): String
//    {
//        var auth = FirebaseAuth.getInstance()
//        if(auth.currentUser == null){
//            Log.i("CLOUD USER ID","Cloud User Id: NULL")
//            auth.signInAnonymously().await()
//        }
//        else{
//            Log.i("CLOUD USER ID","Cloud User Id: ${auth.currentUser!!.uid}")
//        }
//        return auth.currentUser!!.uid
//    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SplashScreen.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SplashScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}