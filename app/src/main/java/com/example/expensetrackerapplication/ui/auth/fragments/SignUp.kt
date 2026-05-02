package com.example.expensetrackerapplication.ui.auth.fragments

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
import com.example.expensetrackerapplication.databinding.SignUpBinding
import com.example.expensetrackerapplication.reusefiles.fnShowMessage
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.SignUpViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUp.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUp : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val signUpViewModel : SignUpViewModel by viewModels()

    val splashViewModel : SplashViewModel by viewModels()

    private lateinit var signUpDataBinding : SignUpBinding

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
    ): View?
    {
        signUpDataBinding= DataBindingUtil.inflate(inflater,R.layout.sign_up, container, false)
        signUpDataBinding.lifecycleOwner=viewLifecycleOwner
        signUpDataBinding.signUpViewModel=signUpViewModel

        return signUpDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        signUpViewModel._firestoreCloudId.value =  splashViewModel.cloudUserId.value

//        signUpViewModel.networkStatus.observe(viewLifecycleOwner) { isAvailable ->
//            when (isAvailable) {
//                is ResultState.success -> {}
//                is ResultState.fail -> {
//                    fnShowMessage(isAvailable.message, requireContext(), R.drawable.error_bg)
//                }
//            }
//        }
        signUpViewModel.isLoading.observe(viewLifecycleOwner){ status ->
            if(status){
                signUpDataBinding.isExportLoading.visibility = View.VISIBLE
            }
            else{
                signUpDataBinding.isExportLoading.visibility = View.GONE
            }
        }
        signUpViewModel.clearAllFields.observe(viewLifecycleOwner){ ob ->
            if(ob){
                signUpDataBinding.idUserName.isFocusable=true
                signUpDataBinding.idUserName.requestFocus()
            }
        }

//        signUpViewModel.nameErrorStatus.observe(viewLifecycleOwner){ ob ->
//            if(ob){
//                signUpDataBinding.idUserName.isFocusable = true
//                signUpDataBinding.idUserName.requestFocus()
//                fnShowMessage("Name Field Was An Empty",requireContext(),R.drawable.error_bg)
//            }
//        }
//        signUpViewModel.mobileNoErrorStatus.observe(viewLifecycleOwner){ state ->
//            when(state)
//            {
//                is ResultState.success -> {}
//                is ResultState.fail ->{
//                    signUpDataBinding.idMobileNo.isFocusable = true
//                    signUpDataBinding.idMobileNo.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }
//        signUpViewModel.emailErrorStatus.observe(viewLifecycleOwner){ state ->
//            when(state){
//                is ResultState.success -> {}
//                is ResultState.fail -> {
//                    signUpDataBinding.idEmail.isFocusable = true
//                    signUpDataBinding.idEmail.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }
//        signUpViewModel.passwordErrorStatus.observe(viewLifecycleOwner){ state ->
//            when(state)
//            {
//                is ResultState.success -> {}
//                is ResultState.fail ->{
//                    signUpDataBinding.idPassword.isFocusable = true
//                    signUpDataBinding.idPassword.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }
//        signUpViewModel.bothFieldsErrorStatus.observe(viewLifecycleOwner){ ob ->
//            if(ob){
//                signUpDataBinding.idUserName.isFocusable=true
//                signUpDataBinding.idUserName.requestFocus()
//                fnShowMessage("All Fields Were Empty",requireContext(),R.drawable.error_bg)
//            }
//        }
        signUpViewModel.insertStatus.observe(viewLifecycleOwner){ state ->
            when(state){
                is ResultState1.success -> {
                    Log.d("DATA_INSERT_STATUS", "Data Successfully Inserted")
                    findNavController().navigate(R.id.action_signup_to_login)
                    fnShowMessage(getString(state.message),requireContext(),R.drawable.bg_success)
                }
                is ResultState1.fail -> {
                    Log.d("DATA_INSERT_STATUS", "Data Insterted Failed")
                    fnShowMessage(getString(state.message),requireContext(),R.drawable.error_bg)

                    if(state.message == R.string.signup_AllFieldsEmpty){
                        signUpDataBinding.idUserName.isFocusable=true
                        signUpDataBinding.idUserName.requestFocus()
                    }
                    else if(state.message == R.string.signup_nameFieldEmpty){
                        signUpDataBinding.idUserName.isFocusable = true
                        signUpDataBinding.idUserName.requestFocus()
                    }
                    else if(state.message == R.string.signup_PasswordFieldEmpty){
                        signUpDataBinding.idPassword.isFocusable = true
                        signUpDataBinding.idPassword.requestFocus()
                    }
                    else if(state.message == R.string.passwordAtleast6Chars){
                        signUpDataBinding.idPassword.isFocusable = true
                        signUpDataBinding.idPassword.requestFocus()
                    }
                    else if(state.message == R.string.signup_EmailFieldEmpty){
                        signUpDataBinding.idEmail.isFocusable = true
                        signUpDataBinding.idEmail.requestFocus()
                    }
                    else if(state.message == R.string.signup_InvalidEmail){
                        signUpDataBinding.idEmail.isFocusable = true
                        signUpDataBinding.idEmail.requestFocus()
                    }
                    else if(state.message == R.string.signup_AlreadyEmailWasUsed){
                        signUpDataBinding.idEmail.isFocusable = true
                        signUpDataBinding.idEmail.requestFocus()
                    }
                    else if(state.message == R.string.signup_MobileNoFieldEmpty){
                        signUpDataBinding.idMobileNo.isFocusable = true
                        signUpDataBinding.idMobileNo.requestFocus()
                    }
                    else if(state.message == R.string.signup_MobileNoMustBe10Chars){
                        signUpDataBinding.idMobileNo.isFocusable = true
                        signUpDataBinding.idMobileNo.requestFocus()
                    }
//                    else if(state.message == R.string.signup_NewUserCreationFailed){
//
//                    }
                    else if(state.message == R.string.signup_MayBeTheEmailAlreadyUsed){
                        signUpDataBinding.idEmail.isFocusable = true
                        signUpDataBinding.idEmail.requestFocus()
                    }

                }
            }
        }

        signUpViewModel.navigateToLogin.observe(viewLifecycleOwner){ ob ->
            if(ob){
                findNavController().navigate(R.id.action_signup_to_login)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignUp.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUp().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}