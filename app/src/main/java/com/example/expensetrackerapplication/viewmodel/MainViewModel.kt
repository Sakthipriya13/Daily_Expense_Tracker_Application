package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.UserRepository

class MainViewModel(
    application: Application,
    private val log: FileLogger
) : AndroidViewModel(application)
{

    // User Repository Variable Initialization
    private  var userRepository  : UserRepository

    init{
        var userDao = AppDatabase.getdatabase(application).userDao()
        userRepository= UserRepository(userDao,application)
    }

    // Logout Status Variable Initialization
    var _logoutSatus = MutableLiveData<Boolean>()
    var logoutStatus : LiveData<Boolean> = _logoutSatus

    // Transparent Background Variable Initialization
    var _displayTransparentBg = MutableLiveData<Boolean>()
    var displayTransparentBg : LiveData<Boolean> = _displayTransparentBg

//    var _profileUri = MutableLiveData<Uri?>(null)
//    var profileUri : LiveData<Uri?> = _profileUri
//
//    var _profilePath = MutableLiveData<String?>(null)
//    var profilePath : LiveData<String?> = _profilePath
//
//    var _triggerProfile = MutableLiveData<Boolean>()
//    var triggerProfile : LiveData<Boolean> = _triggerProfile

    val LOG_TAG ="MAIN_VIEW_MODEL"
    fun fnLogOut()
    {
        try
        {
            _logoutSatus.value = true
        }
        catch (e: Exception)
        {
            log.logError(LOG_TAG,"Set Logout Status (True/False): ${e.message}")
            Log.e(LOG_TAG,"Set Logout Status (True/False): ${e.message}")
        }
    }

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

//    fun fnGetImage(): Deferred<String> {
//        return viewModelScope.async{
//           try {
//               userRepository.fnGetProfileImageUri()
//           }
//           catch (e:Exception){
//               Log.e("GET PROFILE PATH FROM DB","Get Profile path From Db: ${e.message}")
//               ""
//           }
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