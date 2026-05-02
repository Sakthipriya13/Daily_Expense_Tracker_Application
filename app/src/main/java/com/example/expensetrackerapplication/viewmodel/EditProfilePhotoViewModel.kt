package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expensetrackerapplication.ui_event.EditProfilePhoto

class EditProfilePhotoViewModel(application : Application) : AndroidViewModel(application = application)
{
    var _isLeave = MutableLiveData<Boolean>()
    var isLeave : LiveData<Boolean> = _isLeave

    var _isDelProfilePhoto = MutableLiveData<Boolean>()
    var isDelProfilePhoto : LiveData<Boolean> = _isDelProfilePhoto

    var _editResult = MutableLiveData<EditProfilePhoto>()
    var editResult : LiveData<EditProfilePhoto> = _editResult

    fun onClickLeave(){
        _isLeave.value = true
    }

    fun onClickDeleteProfilePhoto(){
        _isDelProfilePhoto.value = true
    }

    fun onClickGallery(){
        _editResult.value= EditProfilePhoto.gallery
    }

    fun onClickCamera(){
        _editResult.value= EditProfilePhoto.camera
    }
}