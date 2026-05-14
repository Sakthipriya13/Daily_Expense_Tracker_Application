package com.example.expensetrackerapplication.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LOG_TAG
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.databinding.CustomToastMessageBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.viewmodel.ToastViewModel


fun Fragment.fnShowMessage(msg: String, context: Context, bg: Int,logger: FileLogger,LOG_TAG : String){
    try
    {
        Log.i("TOAST_MESSAGE","Toast Message1")

        val appViewModelFactory : AppViewModelFactory by lazy {
            AppViewModelFactory(
                requireActivity().application,
                FileLogger(requireContext().applicationContext)
            )
        }

        val toastViewModel : ToastViewModel by viewModels {
            appViewModelFactory
        }

        val toastBinding : CustomToastMessageBinding = DataBindingUtil.inflate(layoutInflater,R.layout.custom_toast_message,null,false)
        toastBinding.toast = toastViewModel
        toastBinding.lifecycleOwner = viewLifecycleOwner

        toastViewModel._toastMsg.value = msg
        toastBinding.idRoot.setBackgroundResource(bg)

        var toast = Toast(context.applicationContext).apply {
            duration = Toast.LENGTH_LONG
//            setGravity(Gravity.TOP,0,150)
            this.view =toastBinding.root
        }
        Log.i("TOAST_MESSAGE","Toast Message2")

        toast.show()

//        Toast.makeText(context,msg, Toast.LENGTH_LONG).show()

        Log.i("TOAST_MESSAGE","Toast Message3")

    }
    catch(e : Exception)
    {
        logger.logError(LOG_TAG,"Show Message: ${e.message}")

    }
}

fun AppCompatActivity.fnShowMessage(msg: String, context: Context, bg: Int,logger: FileLogger,LOG_TAG: String,toastViewModel: ToastViewModel){
    try
    {
        Log.i("TOAST_MESSAGE","Toast Message1")

        val toastBinding : CustomToastMessageBinding = DataBindingUtil.inflate(layoutInflater,R.layout.custom_toast_message,null,false)
        toastBinding.toast = toastViewModel
        toastBinding.lifecycleOwner = this

        toastViewModel._toastMsg.value = msg
        toastBinding.idRoot.setBackgroundResource(bg)

        var toast = Toast(context.applicationContext).apply {
            duration = Toast.LENGTH_LONG
//            setGravity(Gravity.TOP,0,150)
            this.view =toastBinding.root
        }
        Log.i("TOAST_MESSAGE","Toast Message2")

        toast.show()

//        Toast.makeText(context,msg, Toast.LENGTH_LONG).show()


        Log.i("TOAST_MESSAGE","Toast Message3")


    }
    catch(e : Exception)
    {
        logger.logError(LOG_TAG,"Show Message: ${e.message}")

    }
}