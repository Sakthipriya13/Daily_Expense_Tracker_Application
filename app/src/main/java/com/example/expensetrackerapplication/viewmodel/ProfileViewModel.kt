package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.data.dao.UserDao
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.repositary.UserRepository
import com.example.expensetrackerapplication.`object`.Global
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ProfileViewModel(application : Application) : AndroidViewModel(application = application)
{
    private  var userRepository  : UserRepository

    init{
        var userDao = AppDatabase.getdatabase(application).userDao()
        userRepository= UserRepository(userDao,application)
    }
    var _lUserName = MutableLiveData<String>(Global.lUserName)
    var lUserName : LiveData<String> = _lUserName

    var _lUserMobileNo = MutableLiveData<String>(Global.lUserMobileNo)
    var lUserMobileNo : LiveData<String> = _lUserMobileNo

    var _lUserEmail = MutableLiveData<String>(Global.lUssrEmail)
    var lUserEmail : LiveData<String> = _lUserEmail

    var _isChangePassword = MutableLiveData<Boolean>()
    var isChangePassword : LiveData<Boolean> = _isChangePassword

    var _isAddIncome = MutableLiveData<Boolean>()
    var isAddIncome : LiveData<Boolean> = _isAddIncome

    var _isEdit = MutableLiveData<Boolean>()
    var isEdit : LiveData<Boolean> = _isEdit

    var _isDelAccount = MutableLiveData<Boolean>()
    var isDelAccount : LiveData<Boolean> = _isDelAccount

    var _deleteUserAcStatus = MutableLiveData<Boolean>()
    var deleteUserAcStatus : LiveData<Boolean> = _deleteUserAcStatus

    var _profileUri = MutableLiveData<Uri?>()
    var profileUri : LiveData<Uri?> = _profileUri

    var _isDelProfilePhoto = MutableLiveData<Boolean>()
    var isDelProfilePhoto : LiveData<Boolean> = _isDelProfilePhoto

    var _profilePath = MutableLiveData<String?>(null)
    var profilePath : LiveData<String?> = _profilePath

    fun onClickEditProfilePicture(){
        _isEdit.value=true
    }

    fun onClickAddIncome(){
        _isAddIncome.value=true
    }

    fun onClickChangePassword(){
        _isChangePassword.value=true
    }

    fun onClickDeleteAccount(){
        _isDelAccount.value=true
    }




    fun fnUpdateUserProfilePhoto(imgUri : Uri?){
        viewModelScope.launch {
            try{
                var updateStatus = userRepository.fnUpdateLoginUserProfilePhoto(imgUri.toString(),Global.lUserId)

                if(updateStatus){
                    _profileUri.value = imgUri
                }
                else{
                    _profileUri.value = null
                }
            }
            catch (e : Exception){
                Log.e("UPDATE USER PROFILE PHOTO","Update Profile Photo: ${e.message}")
                _profileUri.value = null
            }
        }
    }

    fun fnGetUserProfilePhotoUri(){
        viewModelScope.launch {
            try {
                var imgUri = userRepository.fnGetLoginUserProfilePhotoUri(Global.lUserId)
                _profileUri.value = Uri.parse(imgUri)
            }
            catch (e : Exception)
            {
                Log.i("GET USER PROFILE PHOTO","Get User Profile Uri: ${e.message}")
            }
        }
    }

    fun fnDeleteUserAccount()  {
        viewModelScope.launch {
            try{
                _deleteUserAcStatus.value= userRepository.fnDeleteUser(Global.lUserId)
            }
            catch (e : Exception){
                Log.e("DELETE USER ACCOUNT","Delete User Account: ${e.message}")
                _deleteUserAcStatus.value = false
            }
        }
    }

    fun saveUriToDatabase(imageUri: String) {
        viewModelScope.launch {
            Log.i("PICK IMAGE VIA GALLERY","Pick Image Via Gallery4")
            userRepository.fnAddImageUri(imageUri)
        }
    }

    fun fnGetImage(): Deferred<String> {
        return viewModelScope.async{
            try {
                userRepository.fnGetProfileImageUri()
            }
            catch (e:Exception){
                Log.e("GET PROFILE PATH FROM DB","Get Profile path From Db: ${e.message}")
                ""
            }
        }
    }

    fun fnGetImageFromDb() {
        viewModelScope.launch{
            try{
                _profilePath.value =  userRepository.fnGetProfileImageUri()
            }
            catch(e:Exception){
                Log.e("GET PROFILE PATH FROM DB","Get Profile path From Db: ${e.message}")
            }
        }
    }

}