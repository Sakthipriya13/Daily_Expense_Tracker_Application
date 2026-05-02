package com.example.expensetrackerapplication.ui_event

sealed class EditProfilePhoto {
    object gallery : EditProfilePhoto()
    object camera : EditProfilePhoto()
}