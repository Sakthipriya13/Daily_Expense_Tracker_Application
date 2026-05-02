package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.entity.UserEntity
import com.example.expensetrackerapplication.data.repositary.UserRepository
import com.example.expensetrackerapplication.datastore.LoginDataStore
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.launch

class LoginViewModel( application: Application) : AndroidViewModel(application)
{
    var userRepository : UserRepository
    init {
        val userDao= AppDatabase.getdatabase(application).userDao()
        userRepository= UserRepository(userDao,application)
    }

    //User Name Variable
    var _userName = MutableLiveData<String?>("")
    var userName : LiveData<String?> = _userName

    //User Password Variable
    var _userPassword = MutableLiveData<String?>("")
    var userPassword : LiveData<String?> = _userPassword

    //Login Result Status
    var _loginStatus = MutableLiveData<ResultState1>()
    var loginStatus : LiveData<ResultState1> = _loginStatus

    // User Details
    var _userDetailList = MutableLiveData<List<UserEntity>>()
    var userDetailList : LiveData<List<UserEntity>>  = _userDetailList

    //Navigate To Signup
    var _navigateToSignUp = MutableLiveData<Boolean>()
    var navigateToSignUp : LiveData<Boolean> = _navigateToSignUp

    //Clear All Fields
    var _clearAllFields = MutableLiveData<Boolean>()
    var clearAllFields : LiveData<Boolean> = _clearAllFields

    // Is Password Forget
    var _isPasswordForget = MutableLiveData<Boolean>(false)
    var isPasswordForget : LiveData<Boolean> = _isPasswordForget

    // Display Loading Component
    var _isLoading = MutableLiveData<Boolean>(false)
    var isLoading : LiveData<Boolean> = _isLoading

    var loginDataStore : LoginDataStore = LoginDataStore(application)

    fun fnIsForgetPassword(){
        _isPasswordForget.value = true
    }

    fun clickLoginButton()
    {
        viewModelScope.launch {
            when{

                userName.value.isNullOrBlank() &&  userPassword.value.isNullOrBlank() -> {
                    _userName.value=""
                    _loginStatus.value=ResultState1.fail(R.string.login_BothFieldsEmpty)
                }

                userName.value.isNullOrBlank()-> {
                    _userName.value=""
                    _loginStatus.value= ResultState1.fail(R.string.login_UserNameFieldEmpty)
                }

                userPassword.value.isNullOrBlank()-> {
                    _userPassword.value = ""
                    _loginStatus.value= ResultState1.fail(R.string.login_PasswordFieldEmpty)
                }
                userPassword.value?.length !=6 ->{
                    _userPassword.value = ""
                    _loginStatus.value= ResultState1.fail(R.string.passwordAtleast6Chars)
                }

                else -> {
                    _isLoading.value=true
                    _userDetailList.value=userRepository.fnGetUserDetailsBasedOnUserName(userName.value,userPassword.value)
                    var result= _userDetailList.value?.isNotEmpty()
                    Log.v("USER DETAILS","User Details: ${userDetailList.value}")
                    if(result==false)
                    {
                        var res = userRepository.fnLoginCloudAccount(userName.value,userPassword.value)
                        if(res.isSuccess){
                            _loginStatus.postValue(ResultState1.success(R.string.login_Success))
                        }
                        else{
                            Global.lUserId =-1
                            Global.lUserName=""
                            Global.lUserPassword=""
                            Global.lUserMobileNo=""
                            Global.lUssrEmail=""
                            _loginStatus.postValue(ResultState1.fail(R.string.login_UserNotFound))
                        }

                    }
                    else
                    {
                        if((userName.value?.equals(userDetailList.value?.firstOrNull()?.userName ?: "") == true)
                            && (userPassword.value?.equals(userDetailList.value?.firstOrNull()?.userPassword ?: "") == true))
                        {
                            Global.lUserId = userDetailList.value?.firstOrNull()?.userId ?: -1
                            Global.lUserName= userDetailList.value?.firstOrNull()?.userName ?: ""
                            Global.lUserPassword= userDetailList.value?.firstOrNull()?.userPassword ?: ""
                            Global.lUserMobileNo= userDetailList.value?.firstOrNull()?.userMobileNo ?: ""
                            Global.lUssrEmail= userDetailList.value?.firstOrNull()?.userEmail ?: ""
                            Global.cloudUserId= userDetailList.value?.firstOrNull()?.cloudId ?: ""

                            loginDataStore.fnSaveUser(
                                userDetailList.value?.firstOrNull()?.userId ?: -1,
                                userDetailList.value?.firstOrNull()?.userName ?: "",
                                userDetailList.value?.firstOrNull()?.userMobileNo ?: "",
                                userDetailList.value?.firstOrNull()?.userEmail ?: "",
                                userDetailList.value?.firstOrNull()?.userPassword ?: "",
                                userDetailList.value?.firstOrNull()?.cloudId ?: "",
                                1,
                                userDetailList.value?.firstOrNull()?.signUpDate ?:""
                            )

                            _loginStatus.postValue(ResultState1.success(R.string.login_Success))
                        }
                        else
                        {
                            Global.lUserId =-1
                            Global.lUserName=""
                            Global.lUserPassword=""
                            Global.lUserMobileNo=""
                            Global.lUssrEmail=""

                            _loginStatus.postValue(ResultState1.fail(R.string.login_UserOrPasswordWrong))

                        }
                    }
                }

            }

        }
    }

    fun fnClearAllFields()
    {
        _clearAllFields.value=true
        _userName.value=""
        _userPassword.value=""
    }


    fun fnNavigateToSignUp()
    {
        _navigateToSignUp.value=true
    }

}