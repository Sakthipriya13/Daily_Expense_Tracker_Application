package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.UserRepository
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    application: Application,
    private val logger: FileLogger) : AndroidViewModel(application = application)
{
    //User Repository Variable
    private var userRepository : UserRepository

    init{
        var userDao = AppDatabase.getdatabase(application).userDao()
        userRepository = UserRepository(userDao,application,logger)

    }

    //Current Password
    var _currentPassword = MutableLiveData<String>(""+Global.lUserPassword)
    var currentPassword : LiveData<String> = _currentPassword

    //New Password
    var _newPassword = MutableLiveData<String>()
    var newPassword : LiveData<String> = _newPassword

    //Change Password Status
    var _changePasswordStatus = MutableLiveData<ResultState1>()
    var changePasswordStatus : LiveData<ResultState1> = _changePasswordStatus

    //Close Change Password Screen
    var _isClosed = MutableLiveData<Boolean>()
    var isClosed : LiveData<Boolean> = _isClosed

//    var _passwordErrorStatus = MutableLiveData<ResultState>()
//    var passwordErrorStatus : LiveData<ResultState> = _passwordErrorStatus

//    var _emailErrorStatus = MutableLiveData<ResultState>()
//    var emailErrorStatus : LiveData<ResultState> = _emailErrorStatus

    val LOG_TAG ="CHANGE_PASSWORD_VIEW_MODEL"

    fun onClickCancel()
    {
        try
        {
            _isClosed.value = true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Close The Change Password Screen: ${e.message}")
        }
    }

    fun onClickConfirm()
    {
          try{
              when{
                  newPassword.value.isNullOrBlank() -> {
                      _newPassword.value = ""
                      _changePasswordStatus.value= ResultState1.fail(R.string.cp_NewPasswordFieldEmpty)
                  }

                  newPassword.value?.length !=6 ->{
                      _newPassword.value = ""
                      _changePasswordStatus.value= ResultState1.fail(R.string.passwordAtleast6Chars)
                  }

                  else -> {
                      fnChangePassword()
                  }
              }
          }
          catch (e : Exception)
          {
              logger.logError(LOG_TAG,"On CLick Confirm Button: ${e.message}")
              Log.e("CHANGE_PASSWORD","Change Password: ${e.message}")
              _changePasswordStatus.value = ResultState1.fail(R.string.cp_PasswordChangeFailed)
          }
    }

    fun fnChangePassword()
    {
        viewModelScope.launch {
            try{
                var updateStatus = userRepository.fnUpdateLoginUserPassword(newPassword =newPassword.value, userId =Global.lUserId, currentPassword = currentPassword.value)

                Log.i("CHANGE PASSWORD STATUS","Change password Status: $updateStatus")
                if (updateStatus)
                    _changePasswordStatus.value = ResultState1.success(R.string.cp_PasswordChangesSuccess)

                else
                    _changePasswordStatus.value = ResultState1.fail(R.string.cp_PasswordChangeFailed)

            }
            catch (e : Exception)
            {
                logger.logError(LOG_TAG,"Change Password Function: ${e.message}")
                Log.e("CHANGE_PASSWORD","Change Password: ${e.message}")
                _changePasswordStatus.value = ResultState1.fail(R.string.cp_PasswordChangeFailed)
            }
        }
    }

    fun fnClearAllFields()
    {
        try
        {
            _newPassword.value = ""
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Clear All Fields Value: ${e.message}")
        }
    }
}