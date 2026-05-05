package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.UserRepository
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.launch

class ForgetViewModel(application: Application, logger: FileLogger) : AndroidViewModel(application = application)
{
    var userRepository : UserRepository

    init {
        var userDao = AppDatabase.getdatabase(application).userDao()
        userRepository = UserRepository(userDao,application)
    }

    //Email Field
    var _email = MutableLiveData<String>()
    var email : LiveData<String> = _email

    //New Password Field
    var _newPassword = MutableLiveData<String>()
    var newPassword : LiveData<String> = _newPassword

    var _isCancel = MutableLiveData<Boolean>()
    var isCancel : LiveData<Boolean> = _isCancel

    //password Reset Status
    var _resetStatus = MutableLiveData<ResultState1>()
    var resetStatus : LiveData<ResultState1> = _resetStatus

//    var _passwordErrorStatus = MutableLiveData<ResultState>()
//    var passwordErrorStatus : LiveData<ResultState> = _passwordErrorStatus
//
//    var _emailErrorStatus = MutableLiveData<ResultState>()
//    var emailErrorStatus : LiveData<ResultState> = _emailErrorStatus

    fun onClickCancel(){
       _isCancel.value = true
    }

    fun onClickReset(){
            try{
                Log.e("PASSWORD RESET","Email: ${email.value} And New Password: ${newPassword.value}")

                when{
                    email.value.isNullOrBlank() && newPassword.value.isNullOrBlank() ->{
                        _email.value = ""
                        _newPassword.value = ""
                        _resetStatus.value = ResultState1.fail(R.string.bothFieldsEmpty)
                    }

                    email.value.isNullOrBlank()  -> {
                        _email.value = ""
                        _resetStatus.value=ResultState1.fail(R.string.forget_EmailFieldEmpty)
                    }

                    Global.fnIsEmailValid(email.value) == false -> {
                        _email.value = ""
                        _resetStatus.value = ResultState1.fail(R.string.forget_InvalidEmail)
                    }

                    newPassword.value.isNullOrBlank() -> {
                        _newPassword.value = ""
                        _resetStatus.value= ResultState1.fail(R.string.forget_PasswordFieldEmpty)
                    }

                    newPassword.value?.length !=6 ->{
                        _newPassword.value = ""
                        _resetStatus.value= ResultState1.fail(R.string.passwordAtleast6Chars)
                    }

                    else -> {
                        fnReset()
                    }
                }
            }
            catch (e : Exception){
                Log.e("PASSWORD RESET","Password Reset: ${e.message}")
                _resetStatus.postValue(ResultState1.fail(R.string.forget_PasswordResetFailed))
            }
    }

    private fun fnReset()
    {
        viewModelScope.launch {
            try{
                var updateStatus = userRepository.fnResetLoginUserPassword(newPassword =newPassword.value?.trim(),email =email.value?.trim())

                if (updateStatus)
                    _resetStatus.postValue(ResultState1.success(R.string.forget_PasswordResetSuccess))
                else
                    _resetStatus.postValue(ResultState1.fail(R.string.forget_PasswordResetFailed))
            }
            catch (e : Exception){
                Log.e("PASSWORD RESET","Password Reset: ${e.message}")
                _resetStatus.postValue(ResultState1.fail(R.string.forget_PasswordResetFailed))
            }
        }
    }
}