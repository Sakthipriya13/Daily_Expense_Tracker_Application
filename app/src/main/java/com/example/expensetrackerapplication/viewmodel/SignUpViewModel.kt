package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.entity.CategoryEntitty
import com.example.expensetrackerapplication.data.entity.UserEntity
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.data.repositary.CategoryRepository
import com.example.expensetrackerapplication.data.repositary.UserRepository
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState1
import kotlinx.coroutines.launch

class SignUpViewModel(
    application: Application,
    private val logger: FileLogger) : AndroidViewModel(application)
{
    var userRepository: UserRepository
    var categoryRepository : CategoryRepository

    init {
        val userDao= AppDatabase.getdatabase(application).userDao()
        userRepository= UserRepository(userDao,application)

        val categoryDao = AppDatabase.getdatabase(application).CategoryDao()
        categoryRepository= CategoryRepository(categoryDao)
    }

    //User Name Field
    var _name = MutableLiveData<String?>()
    var  name : LiveData<String?> = _name

    //User Email Field
    var _email = MutableLiveData<String?>()
    var email : LiveData<String?> = _email

    //User MobileNo Field
    var _mobileNo = MutableLiveData<String?>()
    var mobileNo : LiveData<String?> = _mobileNo

    //User Password Field
    var _password = MutableLiveData<String?>()
    var password : LiveData<String?> = _password

    //Navigate To Login
    var _navigateToLogin = MutableLiveData<Boolean>()
    var navigateToLogin : LiveData<Boolean> = _navigateToLogin

    //Insert User
    var _insertStatus = MutableLiveData<ResultState1>()
    var insertStatus : LiveData<ResultState1> = _insertStatus

    //Clear All Fields
    var _clearAllFields = MutableLiveData<Boolean>()
    var clearAllFields : LiveData<Boolean> = _clearAllFields

    // Display Loading Image
    var _isLoading = MutableLiveData<Boolean>(false)
    var isLoading : LiveData<Boolean> = _isLoading

    val LOG_TAG = "SIGN_UP_VIEW_MODEL"

    fun fnClearAllFields()
    {
        try
        {
            _clearAllFields.value=true
            _name.value=""
            _mobileNo.value=""
            _password.value=""
            _email.value=""
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Clear All Fields Value: ${e.message}")
        }

    }

    fun fnNavigateToLogin()
    {
        try
        {
            _navigateToLogin.value = true
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Navigate To Login: ${e.message}")
        }
    }

    fun clickSignupButton()
    {
        viewModelScope.launch {
            try
            {
                when {
                    name.value.isNullOrBlank() &&
                            mobileNo.value.isNullOrBlank() &&
                            email.value.isNullOrBlank() &&
                            password.value.isNullOrBlank() -> {
                        _insertStatus.value = ResultState1.fail(R.string.signup_AllFieldsEmpty)
                    }

                    name.value.isNullOrBlank() -> {
                        _insertStatus.value = ResultState1.fail(R.string.signup_nameFieldEmpty)
                    }

                    mobileNo.value.isNullOrBlank() -> {
                        _insertStatus.value = ResultState1.fail(R.string.signup_MobileNoFieldEmpty)
                    }

                    mobileNo.value?.length != 10 ->{
                        _mobileNo.value=""
                        _insertStatus.value = ResultState1.fail(R.string.signup_MobileNoMustBe10Chars)
                    }

                    email.value.isNullOrBlank()  -> {
                        _insertStatus.value = ResultState1.fail(R.string.signup_EmailFieldEmpty)
                    }

                    Global.fnIsEmailValid(email.value) == false -> {
                        _email.value = ""
                        _insertStatus.value = ResultState1.fail(R.string.signup_InvalidEmail)
                    }


                    isEmailExists(email.value) == true ->{
                        _email.value = ""
                        _insertStatus.value = ResultState1.fail(R.string.signup_AlreadyEmailWasUsed)
                    }

                    password.value.isNullOrBlank() -> {
                        _insertStatus.value = ResultState1.fail(R.string.signup_PasswordFieldEmpty)
                    }

                    password.value?.length !=6 ->{
                        _password.value = ""
                        _insertStatus.value= ResultState1.fail(R.string.passwordAtleast6Chars)
                    }

                    else -> {
                        _isLoading.value=true
                        val isNetworkAvail = Global.isNetworkAvailable(application)
                        if(isNetworkAvail)
                        {
                            Log.i("INTERNET","Internet was connected, User Creation Started")
                            logger.logInfo(LOG_TAG,"Internet was connected, User Creation Started")
                            fnInsert()
                        }
                        else
                        {
                            _insertStatus.value = ResultState1.fail(R.string.noInternet)
                            Log.i("INTERNET","Internet Is Needed")
                            logger.logInfo(LOG_TAG,"Internet Is Needed")
                            _isLoading.value=false
                        }

                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG, "Click Signup Button: ${e.message}")
            }
        }
    }

    fun fnInsert()
    {
        viewModelScope.launch {
            try
            {
                if(
                    !name.value.isNullOrBlank() &&
                    !mobileNo.value.isNullOrBlank() &&
                    !email.value.isNullOrBlank() &&
                    Global.fnIsEmailValid(email.value)==true &&
                    !password.value.isNullOrBlank()
                ) {
                    var usersCount = userRepository.fnUsersCount()

                    val user = UserEntity(
                        userName = name.value,
                        userMobileNo = mobileNo.value,
                        userEmail = email.value,
                        userPassword = password.value,
                        userId = usersCount + 1,
                        signUpDate = Global.fnGetCurrentDate(),
                        cloudId = "",
                        isSynced = 0
                    )

                    val userInsertStatus = userRepository.fnInsertUserDetails(user)

                    if (userInsertStatus.isSuccess) {
                        Log.i("New user INSERT STATUS", "New User Insert Status: $userInsertStatus")

                        var sUserId = userRepository.fnGetSignUpUserId(email.value)

                        var categoryEntities = Global.defaultCategories.map {
                            CategoryEntitty(
                                categoryId = 0,
                                categoryName = it,
                                userId = sUserId,
                                signUpDate = Global.fnGetCurrentDate(),
                                cloudId = Global.cloudUserId ?: "",
                                isSynced = 0
                            )
                        }

                        var categoryInsertStatus =
                            categoryRepository.fnInsertDefaultCategoriesToDb(categoryEntities)

                        if (categoryInsertStatus.isNotEmpty() && categoryInsertStatus.all { it > 0 })
                        {
                            logger.logError(LOG_TAG,"New User Creation: Success")
                            _insertStatus.value = ResultState1.success(R.string.signup_NewUserAccountCreated)
                            _isLoading.value=false
                        }
                        else
                        {
                            logger.logError(LOG_TAG,"New User Creation: Failed1")
                            _insertStatus.value = ResultState1.fail(R.string.signup_NewUserCreationFailed)
                            _isLoading.value=false
                        }
                    }
                    else
                    {
                        logger.logError(LOG_TAG,"New User Creation: Failed2")
                        _insertStatus.value = ResultState1.fail(R.string.signup_NewUserCreationFailed)
                        _isLoading.value=false
                    }
                }
            }
            catch(e : Exception)
            {
                logger.logError(LOG_TAG,"New User Creation: ${e.message}")
                Log.e("NEW USER CREATION","New User Creation: ${e.message}")
                _insertStatus.value = ResultState1.fail(R.string.signup_MayBeTheEmailAlreadyUsed)
                _isLoading.value=false
            }
        }
    }

    suspend fun isEmailExists(email: String?): Boolean {
        return try
        {
            return userRepository.isEmailExistsFun(email)
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Is Email Exists: ${e.message}")
            Log.e("IS EMAIL EXISTS","Is Email Exists: ${e.message}")
            false
        }
    }

}