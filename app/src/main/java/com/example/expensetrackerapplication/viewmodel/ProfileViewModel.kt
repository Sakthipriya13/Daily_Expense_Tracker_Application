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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    application: Application,
    private val logger: FileLogger) : AndroidViewModel(application = application)
{
    // User Repository Variable Initialization
    private lateinit var userRepository  : UserRepository

    init{
        var userDao = AppDatabase.getdatabase(application,logger)?.userDao()
        userDao?.let {
            userRepository= UserRepository(userDao,application,logger)
        }
    }

    // User Name Variable Initialization
    var _lUserName = MutableLiveData<String>(Global.lUserName)
    var lUserName : LiveData<String> = _lUserName

    // Mobile No Variable Initialization
    var _lUserMobileNo = MutableLiveData<String>(Global.lUserMobileNo)
    var lUserMobileNo : LiveData<String> = _lUserMobileNo

    // Email Variable Initialization
    var _lUserEmail = MutableLiveData<String>(Global.lUssrEmail)
    var lUserEmail : LiveData<String> = _lUserEmail

    // Change Password Flag Variable Initialization
    var _isChangePassword = MutableLiveData<Boolean>()
    var isChangePassword : LiveData<Boolean> = _isChangePassword

    // Add Income Flag Variable Initialization
    var _isAddIncome = MutableLiveData<Boolean>()
    var isAddIncome : LiveData<Boolean> = _isAddIncome

//    var _isEdit = MutableLiveData<Boolean>()
//    var isEdit : LiveData<Boolean> = _isEdit

    // Delete Account Flag Variable Initialization
    var _isDelAccount = MutableLiveData<Boolean>()
    var isDelAccount : LiveData<Boolean> = _isDelAccount

    // Delete User Status Variable Initialization
    var _deleteUserAcStatus = MutableLiveData<ResultState1>()
    var deleteUserAcStatus : LiveData<ResultState1> = _deleteUserAcStatus

    val LOG_TAG ="PROFILE_VIEW_MODEL"

//    var _profileUri = MutableLiveData<Uri?>()
//    var profileUri : LiveData<Uri?> = _profileUri

//    var _isDelProfilePhoto = MutableLiveData<Boolean>()
//    var isDelProfilePhoto : LiveData<Boolean> = _isDelProfilePhoto

//    var _profilePath = MutableLiveData<String?>(null)
//    var profilePath : LiveData<String?> = _profilePath

//    fun onClickEditProfilePicture(){
//        _isEdit.value=true
//    }

    fun onClickAddIncome()
    {
        try
        {
            _isAddIncome.value=true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Click Add Income Button: ${e.message}")
        }
    }

    fun onClickChangePassword()
    {
        try
        {
            _isChangePassword.value=true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Click Change Password Button: ${e.message}")
        }
    }

    fun onClickDeleteAccount()
    {
        try
        {
            _isDelAccount.value=true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Click Delete Account Button: ${e.message}")
        }
    }

//    fun fnUpdateUserProfilePhoto(imgUri : Uri?)
//    {
//        viewModelScope.launch {
//            try{
//                var updateStatus = userRepository.fnUpdateLoginUserProfilePhoto(imgUri.toString(),Global.lUserId)
//
//                if(updateStatus){
//                    _profileUri.value = imgUri
//                }
//                else{
//                    _profileUri.value = null
//                }
//            }
//            catch (e : Exception){
//                Log.e("UPDATE USER PROFILE PHOTO","Update Profile Photo: ${e.message}")
//                _profileUri.value = null
//            }
//        }
//    }

//    fun fnGetUserProfilePhotoUri(){
//        viewModelScope.launch {
//            try {
//                var imgUri = userRepository.fnGetLoginUserProfilePhotoUri(Global.lUserId)
//                _profileUri.value = Uri.parse(imgUri)
//            }
//            catch (e : Exception)
//            {
//                Log.i("GET USER PROFILE PHOTO","Get User Profile Uri: ${e.message}")
//            }
//        }
//    }

    fun fnDeleteUserAccount()
    {
        viewModelScope.launch {
            try
            {
                val result = withContext(Dispatchers.IO){
                    userRepository.fnDeleteUser(Global.lUserId)
                }
                if (result)
                {
                    _deleteUserAcStatus.postValue(ResultState1.success(R.string.deleteAccount_Success))
                }
                else
                {
                    _deleteUserAcStatus.postValue(ResultState1.fail(R.string.deleteAccount_Failed))
                }
            }
            catch (e : Exception)
            {
                logger.logError(LOG_TAG,"Delete User Account: ${e.message}")
                Log.e("DELETE USER ACCOUNT","Delete User Account: ${e.message}")
                _deleteUserAcStatus.postValue(ResultState1.fail(R.string.somethingWrong))
            }
        }
    }

//    fun saveUriToDatabase(imageUri: String) {
//        viewModelScope.launch {
//            Log.i("PICK IMAGE VIA GALLERY","Pick Image Via Gallery4")
//            userRepository.fnAddImageUri(imageUri)
//        }
//    }

//    fun fnGetImage(): Deferred<String> {
//        return viewModelScope.async{
//            try {
//                userRepository.fnGetProfileImageUri()
//            }
//            catch (e:Exception){
//                Log.e("GET PROFILE PATH FROM DB","Get Profile path From Db: ${e.message}")
//                ""
//            }
//        }
//    }

//    fun fnGetImageFromDb() {
//        viewModelScope.launch{
//            try{
//                _profilePath.value =  userRepository.fnGetProfileImageUri()
//            }
//            catch(e:Exception){
//                Log.e("GET PROFILE PATH FROM DB","Get Profile path From Db: ${e.message}")
//            }
//        }
//    }

}