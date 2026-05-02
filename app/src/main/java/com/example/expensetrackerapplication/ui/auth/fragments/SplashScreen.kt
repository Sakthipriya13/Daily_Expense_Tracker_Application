package com.example.expensetrackerapplication.ui.auth.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.databinding.SplashScreenBinding
import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    private val viewModel: SplashViewModel by viewModels()
    private lateinit var splashDataBinding: SplashScreenBinding

    private lateinit var topAnimation : Animation
    private lateinit var bottomAnimation : Animation

    val settingsViewModel : SettingsViewModel  by activityViewModels()


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

        splashDataBinding= DataBindingUtil.inflate(inflater,R.layout.splash_screen,container,false)

//        viewModel= SplashViewModel(requireContext())

        splashDataBinding.splashViewModel=viewModel
        splashDataBinding.lifecycleOwner=viewLifecycleOwner

        // Inflate the layout for this fragment
        return splashDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topAnimation= AnimationUtils.loadAnimation(requireContext(), R.anim.top_animation)
        bottomAnimation= AnimationUtils.loadAnimation(requireContext(),R.anim.bottom_animation)

        splashDataBinding.idAppLogo.animation=topAnimation
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
                if(shouldNavigate)
                {
                    findNavController().navigate(R.id.action_splash_to_login)
                }
            }
        }
    }
    suspend fun fnGetCloudUserId(): String
    {
        var auth = FirebaseAuth.getInstance()
        if(auth.currentUser == null){
            Log.i("CLOUD USER ID","Cloud User Id: NULL")
            auth.signInAnonymously().await()
        }
        else{
            Log.i("CLOUD USER ID","Cloud User Id: ${auth.currentUser!!.uid}")
        }
        return auth.currentUser!!.uid
    }
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