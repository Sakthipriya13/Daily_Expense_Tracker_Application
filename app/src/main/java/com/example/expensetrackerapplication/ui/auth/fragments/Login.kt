package com.example.expensetrackerapplication.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.databinding.ForgetPasswordBinding
import com.example.expensetrackerapplication.databinding.LoginBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.ui.main.Main
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.ForgetViewModel
import com.example.expensetrackerapplication.viewmodel.LoginViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Login.newInstance] factory method to
 * create an instance of this fragment.
 */
class Login : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }
    val loginViewModel : LoginViewModel by viewModels{
        appViewModelFactory
    }
    private lateinit var loginDataBinding : LoginBinding

    lateinit var logger : FileLogger

//    val logger = FileLogger(requireContext().applicationContext)

    val LOG_TAG = "LOGIN"

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

        loginDataBinding= DataBindingUtil.inflate(inflater,R.layout.login,container,false)
        loginDataBinding.loginViewModel=loginViewModel
        loginDataBinding.lifecycleOwner=viewLifecycleOwner

        return loginDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.clearAllFields.observe(viewLifecycleOwner){ob ->
            try
            {
                if(ob)
                {
                    loginDataBinding.idUserName.isFocusable=true
                    loginDataBinding.idUserName.requestFocus()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Clear All Fields Value And Set Focus To UserName: ${e.message}")
            }
        }


//        loginViewModel.userNameEmptyStatus.observe(viewLifecycleOwner){ state ->
//            when(state)
//            {
//                is ResultState.success -> {}
//                is ResultState.fail ->{
//                    loginDataBinding.idUserName.isFocusable=true
//                    loginDataBinding.idUserName.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }

//        loginViewModel.userPasswordEmptyStatus.observe(viewLifecycleOwner){ state ->
//            when(state)
//            {
//                is ResultState.success -> {}
//                is ResultState.fail ->{
//                    loginDataBinding.idPassword.isFocusable = true
//                    loginDataBinding.idPassword.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }

//        loginViewModel.bothNameAndPasswordEmptyStatus.observe(viewLifecycleOwner){ state ->
//            when(state)
//            {
//                is ResultState.success -> {}
//                is ResultState.fail ->{
//                    loginDataBinding.idUserName.isFocusable=true
//                    loginDataBinding.idUserName.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }

        loginViewModel.loginStatus.observe(viewLifecycleOwner){ state ->
            try
            {
                when(state)
                {
                    is ResultState1.success -> {
                        loginViewModel._isLoading.value = false
                        fnShowMessage(getString(state.message),requireContext(),R.drawable.bg_success)
                        var intent = Intent(requireContext(), Main::class.java)
                        startActivity(intent)
                        requireActivity().finish()

                    }

                    is ResultState1.fail -> {
                        loginViewModel._isLoading.value = false
                        Log.e("LOGIN STATUS", "Login Status Value Was False")
                        fnShowMessage(getString(state.message),requireContext(),R.drawable.error_bg)

                        if(state.message == R.string.login_BothFieldsEmpty){
                            loginViewModel._userName.value=""
                            loginViewModel._userPassword.value=""
                            loginDataBinding.idUserName.isFocusable=true
                            loginDataBinding.idUserName.requestFocus()
                        }
                        else if(state.message == R.string.login_UserNameFieldEmpty){
                            loginViewModel._userName.value=""
                            loginDataBinding.idUserName.isFocusable=true
                            loginDataBinding.idUserName.requestFocus()
                        }
                        else if(state.message == R.string.login_PasswordFieldEmpty){
                            loginViewModel._userPassword.value=""
                            loginDataBinding.idPassword.isFocusable=true
                            loginDataBinding.idPassword.requestFocus()
                        }
                        else if(state.message == R.string.passwordAtleast6Chars){
                            loginViewModel._userPassword.value=""
                            loginDataBinding.idPassword.isFocusable=true
                            loginDataBinding.idPassword.requestFocus()
                        }
                        else if(state.message == R.string.login_UserNotFound){
                            loginViewModel._userName.value=""
                            loginViewModel._userPassword.value=""
                            loginDataBinding.idUserName.isFocusable=true
                            loginDataBinding.idUserName.requestFocus()
                        }
                        else if(state.message == R.string.login_UserOrPasswordWrong){
                            loginViewModel._userName.value=""
                            loginViewModel._userPassword.value=""
                            loginDataBinding.idUserName.isFocusable=true
                            loginDataBinding.idUserName.requestFocus()
                        }
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Login Status Value Observed: ${e.message}")
            }
        }

        loginViewModel.isLoading.observe(viewLifecycleOwner){ status ->
            try
            {
                if(status)
                {
                    loginDataBinding.isExportLoading.visibility = View.VISIBLE
                }
                else
                {
                    loginDataBinding.isExportLoading.visibility = View.GONE
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Is Loading Value Observed: ${e.message}")
            }
        }

//        loginViewModel.loginStatus_fail.observe(viewLifecycleOwner){ ob ->
//            if(ob)
//            {
//                Log.e("LOGIN STATUS", "Login Status Value Was False")
//                fnShowMessage("User Not Found,Enter Valid User ",requireContext(),R.drawable.error_bg)
//                loginViewModel._userName.value=""
//                loginViewModel._userPassword.value=""
//                loginDataBinding.idUserName.isFocusable=true
//                loginDataBinding.idUserName.requestFocus()
//            }
//        }
//        loginViewModel.loginStatus_success.observe(viewLifecycleOwner){ ob ->
//            if(ob)
//            {
//                fnShowMessage("Successfully Login",requireContext(),R.drawable.bg_success)
////                findNavController().navigate(R.id.action_login_to_main)
//                var intent = Intent(requireContext(), Main::class.java)
//                startActivity(intent)
//                requireActivity().finish()
//            }
//
//        }

        loginViewModel.navigateToSignUp.observe(viewLifecycleOwner){ ob ->
            try {
                if(ob)
                {
                    findNavController().navigate(R.id.action_login_to_signup)
                }
                else
                {
                    Log.e("GO TO SIGNUP", "Go To SignUp Value Was False")
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Navigate To Signup: ${e.message}")
            }
        }

        loginViewModel.isPasswordForget.observe(viewLifecycleOwner){ status ->
            try
            {
                if(status)
                {
                    if(Global.isBottomSheetSelected == false){
                        Global.isBottomSheetSelected = true
                        ForgetPassword().show(parentFragmentManager,"ForgetBottomSheet")
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Display Forget Password Screen: ${e.message}")
            }
        }
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
//
//            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
//
//            v.setPadding(
//                v.paddingLeft,
//                v.paddingTop,
//                v.paddingRight,
//                imeBottom
//            )
//
//            insets
//        }
//    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Login.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Login().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

class ForgetPassword : BottomSheetDialogFragment()
{
    val appViewModelFactory : AppViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
        FileLogger(requireContext().applicationContext)
        )
    }
    private lateinit var forgetBinding : ForgetPasswordBinding
    private val forgetViewModel : ForgetViewModel by viewModels{
        appViewModelFactory
    }
    val LOG_TAG = "FORGET_PASSWORD"

    val logger = FileLogger(requireContext().applicationContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        forgetBinding = DataBindingUtil.inflate(inflater,R.layout.forget_password,container,false)
        forgetBinding.forget = forgetViewModel
        forgetBinding.lifecycleOwner = viewLifecycleOwner

        return forgetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        forgetViewModel.isCancel.observe(viewLifecycleOwner){ isCancelable ->
            try
            {
                if(isCancelable)
                {
                    Global.isBottomSheetSelected = false
                    dismiss()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Close The Forget Password Screen: ${e.message}")
            }
        }

//        forgetViewModel.emailErrorStatus.observe(viewLifecycleOwner){ state ->
//            when(state){
//                is ResultState.success -> {}
//                is ResultState.fail -> {
//                    forgetBinding.idEEmail.isFocusable = true
//                    forgetBinding.idEEmail.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }
//        forgetViewModel.passwordErrorStatus.observe(viewLifecycleOwner){ state ->
//            when(state)
//            {
//                is ResultState.success -> {}
//                is ResultState.fail ->{
//                    forgetBinding.idENewPassword.isFocusable = true
//                    forgetBinding.idENewPassword.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }

        forgetViewModel.resetStatus.observe(viewLifecycleOwner) { state ->
            try {
                when (state) {
                    is ResultState1.success -> {
                        fnShowMessage(
                            getString(state.message),
                            requireContext(),
                            R.drawable.bg_success
                        )
                        Global.isBottomSheetSelected = false
                        dismiss()
                    }

                    is ResultState1.fail -> {
                        fnShowMessage(
                            getString(state.message),
                            requireContext(),
                            R.drawable.error_bg
                        )

                        if (state.message == R.string.forget_EmailFieldEmpty) {
                            forgetViewModel._email.value = ""
                            forgetBinding.idEEmail.isFocusable = true
                            forgetBinding.idEEmail.requestFocus()
                        } else if (state.message == R.string.forget_InvalidEmail) {
                            forgetViewModel._email.value = ""
                            forgetBinding.idEEmail.isFocusable = true
                            forgetBinding.idEEmail.requestFocus()
                        } else if (state.message == R.string.forget_PasswordFieldEmpty) {
                            forgetViewModel._newPassword.value = ""
                            forgetBinding.idENewPassword.isFocusable = true
                            forgetBinding.idENewPassword.requestFocus()
                        } else if (state.message == R.string.passwordAtleast6Chars) {
                            forgetViewModel._newPassword.value = ""
                            forgetBinding.idENewPassword.isFocusable = true
                            forgetBinding.idENewPassword.requestFocus()
                        } else if (state.message == R.string.forget_PasswordResetFailed) {
                            forgetViewModel._email.value = ""
                            forgetViewModel._newPassword.value = ""
                            forgetBinding.idEEmail.isFocusable = true
                            forgetBinding.idEEmail.requestFocus()
                        } else if (state.message == R.string.bothFieldsEmpty) {
                            forgetBinding.idEEmail.isFocusable = true
                            forgetBinding.idEEmail.requestFocus()
                        }
                    }
                }

            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG, "Reset Status Value Observed: ${e.message}")
            }

        }
    }
}