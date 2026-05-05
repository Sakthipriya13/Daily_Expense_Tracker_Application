package com.example.expensetrackerapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapplication.data.database.AppDatabase
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.data.logger.Logger
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository
import com.example.expensetrackerapplication.data.repositary.IncomeRepository
import com.example.expensetrackerapplication.model.CategoryChartModel
import com.example.expensetrackerapplication.model.PaymentTypeChartModel
import com.example.expensetrackerapplication.`object`.Global
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

class DashBoardViewModel(application: Application, logger: FileLogger) : AndroidViewModel(application = application)
{

    private var newExpenseRepository : ExpenseRepository

    private var incomeRepository: IncomeRepository


    init{
        val expenseDao = AppDatabase.getdatabase(application).ExpenseDao()
        newExpenseRepository = ExpenseRepository(expenseDao)
        
        val incomeDao = AppDatabase.getdatabase(application).IncomeDao()
        incomeRepository = IncomeRepository(incomeDao)
    }

    var _incomeAmt = MutableLiveData<String?>("0.00")
    var incomeAmt : LiveData<String?> = _incomeAmt

    var _expenseAmt = MutableLiveData<String?>("0.00")
    var expenseAmt : LiveData<String?> = _expenseAmt

    var _balanceAmt = MutableLiveData<String?>("0.00")
    var  balanceAmt : LiveData<String?> = _balanceAmt

    var _categoryChartList = MutableLiveData<List<CategoryChartModel>>(mutableListOf<CategoryChartModel>())
    var categoryChartList : LiveData<List<CategoryChartModel>> = _categoryChartList

    var _paymentTypeChartList = MutableLiveData<List<PaymentTypeChartModel>>(mutableListOf<PaymentTypeChartModel>())
    var paymentTypeChartList : LiveData<List<PaymentTypeChartModel>> = _paymentTypeChartList

    var _clickBtnThisMonth = MutableLiveData<Boolean>()
    val clickBtnThisMonth : LiveData<Boolean> = _clickBtnThisMonth

    var _clickBtnThisYear = MutableLiveData<Boolean>()
    val clickBtnThisYear : LiveData<Boolean> = _clickBtnThisYear

    var _isLoading = MutableLiveData<Boolean>()
    var isLoading : LiveData<Boolean> = _isLoading

//    var _syncRes = MutableLiveData<Result<String>>()
//    var syncRes : LiveData<Result<String>> = _syncRes

//    fun syncDemo(){
//        viewModelScope.launch {
//            try{
//                var res = incomeRepository.fnSyncDemo()
//                _syncRes.postValue(res)
//            }
//            catch(e:Exception){
//                Log.e("SYNC DEMO","Sync Demo: ${e.message}")
//                _syncRes.postValue(Result.failure(Exception("${e.message}")))
//            }
//        }
//    }


    fun onCLickBtnThisMonth(){
        viewModelScope.launch {
            try{

                _isLoading.postValue(true)

                _incomeAmt.postValue("0.00")
                _expenseAmt.postValue("0.00")
                _balanceAmt.postValue("0.00")

                _clickBtnThisMonth.postValue(true)
                _clickBtnThisYear.postValue(false)
                var income = incomeRepository.fnGetIncomePerMonth(Global.fnGetCurrentMonth())
                var expense = newExpenseRepository.fnGetMonthSummary(Global.fnGetCurrentMonth())
                var balance = income-expense

                Log.i("INCOME & EXPENSE & BALANCE DETAILS FOR CUR MONTH","Income:$income & Expense:$expense & Balance:$balance")

                if(income != 0.0f){
                    _incomeAmt.postValue(Global.fnFormatFloatTwoDigits(income))
                }
                if(expense != 0.0f){
                    _expenseAmt.postValue(Global.fnFormatFloatTwoDigits(expense))
                }
                if(balance != 0.0f){
                    _balanceAmt.postValue(Global.fnFormatFloatTwoDigits(abs(balance)))
                }
                else{
                    _balanceAmt.postValue(0.00f.toString())
                }


                var p_ChartRes = newExpenseRepository.fnGetPaymentTypeAmtSummaryPerMonth(Global.fnGetCurrentMonth())
                var p_ChartList : MutableList<PaymentTypeChartModel> = mutableListOf()
                if(p_ChartRes.isNotEmpty()){
                    p_ChartRes.forEach { ob ->
                        p_ChartList.add(
                            PaymentTypeChartModel(
                                userId = ob.userId,
                                paymentType_CashAmt = ob.paymentType_CashAmt,
                                paymentType_CardAmt = ob.paymentType_CardAmt,
                                paymentType_UpiAmt = ob.paymentType_UpiAmt,
                                paymentType_OthersAmt = ob.paymentType_OthersAmt
                            )
                        )
                    }
                    _paymentTypeChartList.postValue(p_ChartList)
                }
                else{
                    _paymentTypeChartList.postValue(mutableListOf<PaymentTypeChartModel>())
                }
            }
            catch (e : Exception){
                Log.e("GET INCOME PER MONTH","Get Income Per Month: ${e.message}")
            }
        }
    }

    fun onClickBtnThisYear(){
        viewModelScope.launch {
            try{

                _isLoading.postValue(true)

                _incomeAmt.postValue("0.00")
                _expenseAmt.postValue("0.00")
                _balanceAmt.postValue("0.00")

                _clickBtnThisYear.postValue(true)
                _clickBtnThisMonth.postValue(false)
                val income = incomeRepository.fnGetIncomePerYear(Global.fnGetCurrentYear())
                val expense = newExpenseRepository.fnGetYearSummary(Global.fnGetCurrentYear())
                val balance = income-expense

                Log.i("INCOME & EXPENSE & BALANCE DETAILS FOR CUR YEAR","Income:$income & Expense:$expense & Balance:$balance")


                if(income != 0.0f){
                    _incomeAmt.postValue(Global.fnFormatFloatTwoDigits(income))
                }
                if(expense != 0.0f){
                    _expenseAmt.postValue(Global.fnFormatFloatTwoDigits(expense))
                }
                if(balance != 0.0f){
                    _balanceAmt.postValue(Global.fnFormatFloatTwoDigits(abs(balance)))
                }
                else{
                    _balanceAmt.postValue(0.00f.toString())
                }

                var p_ChartRes = newExpenseRepository.fnGetPaymentTypeAmtSummaryPerYear(Global.fnGetCurrentYear())
                var p_ChartList : MutableList<PaymentTypeChartModel> = mutableListOf()
                if(p_ChartRes.isNotEmpty()){
                    p_ChartRes.forEach { ob ->
                        p_ChartList.add(
                            PaymentTypeChartModel(
                                userId = ob.userId,
                                paymentType_CashAmt = ob.paymentType_CashAmt,
                                paymentType_CardAmt = ob.paymentType_CardAmt,
                                paymentType_UpiAmt = ob.paymentType_UpiAmt,
                                paymentType_OthersAmt = ob.paymentType_OthersAmt
                            )
                        )
                    }
                    _paymentTypeChartList.postValue(p_ChartList)
                }
                else{
                    _paymentTypeChartList.postValue(mutableListOf<PaymentTypeChartModel>())
                }
            }
            catch (e : Exception){
                Log.e("GET INCOME PER YEAR","Get Income Per Year: ${e.message}")
            }
        }
    }

    fun fnGetCateDetailsPerDay(){
        viewModelScope.launch {
            try{
                var res= newExpenseRepository.fnGetCateDetailsPerDay(Global.fnGetCurrentDate())
                var list : MutableList<CategoryChartModel> = mutableListOf()
                if (res.isNotEmpty()) {
                    res.forEach { ob ->
                        Log.i(
                            "CATEGORY DETAILS PER DAY",
                            "Category Details per Day: Name: ${ob.categoryName} and Amt: ${ob.expenseAmt}"
                        )
                        list.add(
                            CategoryChartModel(
                                userId = ob.userId,
                                categoryId = ob.categoryId,
                                categoryName = ob.categoryName,
                                expenseAmt = ob.expenseAmt
                            )
                        )
                    }
                    _categoryChartList.postValue(list)
                }
                else{
                    _categoryChartList.postValue(mutableListOf<CategoryChartModel>())
                }
            }
            catch (e : Exception){
                Log.e("GET CATEGORY LIST PER DAY","Get Category Details Per Day: ${e.message}")
            }
        }
    }

//    fun fnGetCateDetailsPerMonth(){
//        viewModelScope.launch {
//            try{
//                var res= newExpenseRepository.fnGetCateDetailsPerMonth("02")
//                var list : MutableList<CategoryChartModel> = mutableListOf()
////                if (res.isNotEmpty()){
////                    res.forEach { ob ->
////                        Log.i("CATEGORY DETAILS PER MONTH","Category Details per Month: Name: ${ob.categoryName} and Amt: ${ob.expenseAmt}")
////                        list.add(
////                            CategoryChartModel(
////                                userId = ob.userId,
////                                categoryId = ob.categoryId,
////                                categoryName = ob.categoryName,
////                                expenseAmt= ob.expenseAmt
////                            )
////                        )
////                    }
////                    _categoryChartList.postValue(mutableListOf<CategoryChartModel>())
////                    _categoryChartList.postValue(list)
////                }
//                for(i in 1..3){
//                    list.add(
//                        CategoryChartModel(
//                            1,
//                            categoryId =1,
//                            categoryName = "Food $i",
//                            expenseAmt= (i*100).toFloat()
//                        )
//                    )
//                }
//                _categoryChartList.value=list
//
//            }
//            catch (e : Exception){
//                Log.e("GET CATEGORY LIST PER DAY","Get Category Details Per Day: ${e.message}")
//            }
//        }
//    }

//    fun fnGetCateDetailsPerYear(){
//        viewModelScope.launch {
//            try{
//                var res= newExpenseRepository.fnGetCateDetailsPerYear("2026")
//                var list : MutableList<CategoryChartModel> = mutableListOf()
////                if (res.isNotEmpty()){
////                    res.forEach { ob ->
////                        Log.i("CATEGORY DETAILS PER YEAR","Category Details per Year: Name: ${ob.categoryName} and Amt: ${ob.expenseAmt}")
////                        list.add(
////                            CategoryChartModel(
////                                userId = ob.userId,
////                                categoryId = ob.categoryId,
////                                categoryName = ob.categoryName,
////                                expenseAmt= ob.expenseAmt
////                            )
////                        )
////                    }
////                    _categoryChartList.postValue(mutableListOf<CategoryChartModel>())
////                    _categoryChartList.postValue(list)
////                }
//                for(i in 1..4){
//                    list.add(
//                        CategoryChartModel(hp
//                            1,
//                            categoryId =1,
//                            categoryName = "Food $i",
//                            expenseAmt= (i*100).toFloat()
//                        )
//                    )
//                }
//                _categoryChartList.value=list
//            }
//            catch (e : Exception){
//                Log.e("GET CATEGORY LIST PER DAY","Get Category Details Per Day: ${e.message}")
//            }
//        }
//    }
}