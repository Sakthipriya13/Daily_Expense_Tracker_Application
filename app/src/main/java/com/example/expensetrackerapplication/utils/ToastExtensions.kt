package com.example.expensetrackerapplication.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensetrackerapplication.R



fun Fragment.fnShowMessage(msg: String, context: Context, bg: Int){
    try
    {
//        Log.i("TOAST_MESSAGE","Toast Message1")
//        val view = LayoutInflater.from(context).inflate(R.layout.custom_toast_message,null,false)
//
//        var toastMsg = view.findViewById<TextView>(R.id.success_Msg)
//        var layout = view.findViewById<LinearLayout>(R.id.idLayout)
//
//        toastMsg.text=msg
//        layout.setBackgroundResource(bg)
//
//        var toast = Toast(context.applicationContext).apply {
//            duration = Toast.LENGTH_LONG
////            setGravity(Gravity.TOP,0,100)
//            this.view =view
//        }
////        toast.duration=Toast.LENGTH_LONG
////        toast.setGravity(Gravity.TOP,0,0)
////        toast.view=view
//        Log.i("TOAST_MESSAGE","Toast Message2")
//
//        toast.show()
//
//        Log.i("TOAST_MESSAGE","Toast Message3")

        Toast.makeText(context.applicationContext,msg, Toast.LENGTH_LONG).show()

    }
    catch(e : Exception)
    {
        Log.i("TOAST_MESSAGE","Toast Message4")

        Log.e("FUNCTION SHOW ERROR MESSAGE","Function Show Error Message: $e")
    }
}

fun Activity.fnShowMessage(msg: String, context: Context, bg: Int){
    try
    {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_toast_message,null,false)

        var toastMsg = view.findViewById<TextView>(R.id.success_Msg)
        toastMsg.text=msg
        var layout = view.findViewById<LinearLayout>(R.id.idLayout)
        layout.setBackgroundResource(bg)
        var toast = Toast(context).apply {
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.TOP,0,100)
            this.view =view
        }
//        toast.duration=Toast.LENGTH_LONG
//        toast.setGravity(Gravity.TOP,0,0)
//        toast.view=view
        toast.show()

    }
    catch(e : Exception)
    {
        Log.e("FUNCTION SHOW ERROR MESSAGE","Function Show Error Message: $e")
    }
}