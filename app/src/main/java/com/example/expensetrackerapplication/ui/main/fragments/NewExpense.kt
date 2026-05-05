package com.example.expensetrackerapplication.ui.main.fragments

import android.app.AlertDialog
import android.app.Application
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.entity.CategoryEntitty
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.databinding.NewExpenseBinding
import com.example.expensetrackerapplication.databinding.SplitDialogueBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.model.PaymentType
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.NewExpenseViewModel
import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel
import com.example.expensetrackerapplication.viewmodel.SplitViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.log

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewExpense.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewExpense : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var newExpenseBinding : NewExpenseBinding
    var categoryList =emptyList<CategoryEntitty>()
    private lateinit var splitBinding : SplitDialogueBinding
    var splitDialog : AlertDialog? = null

    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }

    val newExpenseViewModel : NewExpenseViewModel by viewModels{
        appViewModelFactory
    }
    val settingsViewModel : SettingsViewModel by activityViewModels{
        appViewModelFactory
    }

    val splashViewModel : SplashViewModel by viewModels{
        appViewModelFactory
    }
    val splitViewModel : SplitViewModel by viewModels{
        appViewModelFactory
    }

    val logger = FileLogger(requireContext().applicationContext)

    val LOG_TAG ="NEW_EXPENSE"

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.title= resources.getString(R.string.add_expense_frag)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        newExpenseBinding = DataBindingUtil.inflate(inflater,R.layout.new_expense, container, false)
        newExpenseBinding.newExpenseViewModel=newExpenseViewModel
        newExpenseBinding.lifecycleOwner=viewLifecycleOwner

        return newExpenseBinding.root 
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        newExpenseViewModel._fireStoreCloudId.value = splashViewModel.cloudUserId.value

        newExpenseViewModel.clearAllFields.observe(viewLifecycleOwner){ ob ->
            if(ob)
            {
                try
                {
                    newExpenseBinding.idENewExpense.isFocusable=true
                    newExpenseBinding.idENewExpense.requestFocus()
                }
                catch (e : Exception)
                {
                    logger.logError(LOG_TAG,"Clear All Fields Value: ${e.message}")
                    Log.e("CLEAR_ALL_FIELDS","Clear_All_Fields: ${e.message}")
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                try
                {
                    settingsViewModel.fnGetAllCategories()
                }
                catch (e: Exception)
                {
                    logger.logError(LOG_TAG,"Get All Categories: ${e.message}")
                    Log.e("GET_ALL_CATEGORIES","Get All Categories: ${e.message}")
                }
            }
        }

        newExpenseBinding.idCalendarButton.setOnClickListener {
            try
            {
                if(Global.isCalendarSelected==false)
                {
                    Global.isCalendarSelected=true

                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    val datePickerDialog = DatePickerDialog(requireContext(),
                        { _,y,m,d ->

                            calendar.set(y,m,d)
                            val sdf1 = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val date = sdf.format(calendar.time)
                            val dateUi = sdf1.format(calendar.time)
                            newExpenseViewModel._selectedDate.value=date
                            newExpenseViewModel._selectedDateUi.value=dateUi
                            Global.isCalendarSelected=false
                        } , year,month,day)

                    datePickerDialog.setCancelable(false)
                    datePickerDialog.setCanceledOnTouchOutside(false)

                    datePickerDialog.setOnCancelListener {
                        Global.isCalendarSelected=false
                    }
                    datePickerDialog.show()
                }
            }
            catch (e : Exception)
            {
                logger.logError(LOG_TAG,"Show Calendar: ${e.message}")
                Log.e("SHOW_CALENDAR","Show Calendar: ${e.message}")
            }
        }

        settingsViewModel.categoryList.observe(viewLifecycleOwner){ list ->
            try
            {
                categoryList = list
                val categoryNameList= list.map {it.categoryName}

                val autoCompleteAdapter= ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item,
                    categoryNameList)

                newExpenseBinding.idDCategories.setAdapter(autoCompleteAdapter)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Set Category Adapter: ${e.message}")
                Log.e("SET_CATEGORY_ADAPTER","Set Category Adapter: ${e.message}")
            }
        }

        newExpenseBinding.idDCategories.setOnItemClickListener { parent,_,position,_ ->
            try{
                var selectedCategoryName = parent.getItemAtPosition(position).toString()
                newExpenseViewModel._selectedCategoryName.value=selectedCategoryName
                newExpenseViewModel._selectedCategoryId.value=categoryList[position].categoryId
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Select Category From List: ${e.message}")
                Log.e("SELECT_CATEGORY_FROM_LIST","Select Category From List: ${e.message}")
            }
        }

        splitViewModel.amtInCash.observe(viewLifecycleOwner){ amt ->
            try
            {
                val currentPaymentType =
                    newExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                newExpenseViewModel._selectedPaymentTypeAmt.value =
                    currentPaymentType.copy(
                        cash = amt?.toFloat() ?: 0f
                    )
                splitViewModel.fnUpdateFromCash()
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Amount In Cash: ${e.message}")
                Log.e("AMOUNT_IN_CASH","Amount In Cash: ${e.message}")
            }
        }

        splitViewModel.amtInCard.observe(viewLifecycleOwner){ amt ->
            try
            {
                val currentPaymentType =
                    newExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                newExpenseViewModel._selectedPaymentTypeAmt.value =
                    currentPaymentType.copy(
                        card = amt?.toFloat() ?: 0f
                    )
                splitViewModel.fnUpdateFromCard()
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Amount In Card: ${e.message}")
                Log.e("AMOUNT_IN_CARD","Amount In Card: ${e.message}")
            }
        }

        splitViewModel.amtInUpi.observe(viewLifecycleOwner){ amt ->
            try
            {
                val currentPaymentType =
                    newExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                newExpenseViewModel._selectedPaymentTypeAmt.value = currentPaymentType.copy(
                    upi = amt?.toFloat() ?: 0.00f
                )
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Amount In Upi: ${e.message}")
                Log.e("AMOUNT_IN_UPI","Amount In Upi: ${e.message}")
            }
        }

        splitViewModel.splitStatus.observe(viewLifecycleOwner){ status ->
            try
            {
                when(status)
                {
                    is ResultState1.success -> {}
                    is ResultState1.fail -> {
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.error_bg)
                        splitBinding.idEAmtInCash.selectAll()
                        splitBinding.idEAmtInCash.requestFocus()
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Split Status: ${e.message}")
                Log.e("SPLIT_STATUS","Split Status: ${e.message}")
            }
        }

        splitViewModel.okSplit.observe(viewLifecycleOwner){ amt ->
            try
            {
                newExpenseBinding.idERemarks.requestFocus()
                newExpenseBinding.idERemarks.isFocusable=true
                splitDialog?.dismiss()
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Split: ${e.message}")
                Log.e("SPLIT","Split: ${e.message}")
            }
        }

        splitViewModel.isClosed.observe(viewLifecycleOwner) { close ->
            try {
                if(close)
                {
//                newExpenseViewModel._paymentType.value=-1
                    newExpenseViewModel._selectedPaymentType.value=-1
                    splitDialog?.dismiss()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Close Split Dialog: ${e.message}")
                Log.e("CLOSE_SPLIT_DIALOG","Close Split Dialog: ${e.message}")
            }
        }
        newExpenseViewModel.showSplitDialog.observe(viewLifecycleOwner){ isChecked ->
            try {
                if(isChecked)
                {
                    fnShowDialog()
                    newExpenseViewModel._showSplitDialog.value = false
                }
                else
                {
                    fnShowMessage(getString(R.string.enter_expense),requireContext(),R.drawable.error_bg)
                    newExpenseBinding.idENewExpense.isFocusable=true
                    newExpenseBinding.idENewExpense.requestFocus()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Show Split Screen: ${e.message}")
                Log.e("SHOW_SPLIT_DIALOG","Show Split Dialog: ${e.message}")
            }
//            {
//                val view = layoutInflater.inflate(R.layout.split_dialogue,null)
//                var amtInCash = view.findViewById<TextInputEditText>(R.id.idEAmtInCash)
//                var amtInCard = view.findViewById<TextInputEditText>(R.id.idEAmtInCard)
//                var amtInUpi = view.findViewById<TextInputEditText>(R.id.idEAmtInUpi)
//
//                var btnOk=view.findViewById<Button>(R.id.idBtnOk)
//                var btnCancel=view.findViewById<TextView>(R.id.idBtnCancel)
//
//                var dialog = AlertDialog.Builder(requireContext())
//                    .setView(view)
//                    .setCancelable(false)
//                    .create()
//
//                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                dialog.show()
//
//                btnOk.setOnClickListener {
//                    newExpenseViewModel._amtInCash.value=amtInCash.text.toString().toFloatOrNull() ?:0.0f
//                    newExpenseViewModel._amtInCard.value=amtInCard.text.toString().toFloatOrNull() ?:0.0f
//                    newExpenseViewModel._amtInUpi.value=amtInUpi.text.toString().toFloatOrNull() ?:0.0f
//
//                    val totAmt = newExpenseViewModel._amtInCash.value!! +
//                            newExpenseViewModel._amtInCard.value!! +
//                            newExpenseViewModel._amtInUpi.value!!
//
//                    newExpenseViewModel._expenseAmt.value=totAmt.toString()
//
//                    newExpenseBinding.idERemarks.requestFocus()
//                    newExpenseBinding.idERemarks.isFocusable=true
//
//
//                    Log.v("AMT IN CASH","Amt In Cash: ${newExpenseViewModel.amtInCash.value}")
//                    Log.v("AMT IN CARD","Amt In Card: ${newExpenseViewModel.amtInCard.value}")
//                    Log.v("AMT IN UPI","Amt In Upi: ${newExpenseViewModel.amtInUpi.value}")
//                    Log.v("TOTAL EXPENSE AMT","Total Expense Amt: ${newExpenseViewModel.expenseAmt.value}")
//
//
//                    dialog.dismiss()
//
////                     Log.v("PAYMENT TYPE","Payment Type: SPLIT")
////                     newExpenseViewModel._paymentType.value=Global.PAYMENT_TYPE_SPLIT
////                     newExpenseViewModel._selectedpaymentType.value=R.id.idSplitPayment
//
//                }
//
//                btnCancel.setOnClickListener {
////                     newExpenseViewModel.fnCashPayment()
//                    newExpenseViewModel._paymentType.value=-1
//                    newExpenseViewModel._selectedpaymentType.value=-1
//                    dialog.dismiss()
//                }
//            }
        }

//        newExpenseViewModel.valueMissingError.observe(viewLifecycleOwner){ errMsg ->
//            if(!errMsg.isNullOrBlank())
//            {
//                fnShowMessage(errMsg,requireContext(),R.drawable.error_bg)
//            }
//        }

        newExpenseViewModel.newExpenseInsertStatus.observe(viewLifecycleOwner){ state ->
            try {
                when(state){
                    is ResultState1.success ->{
                        fnShowMessage(getString(state.message),requireContext(),R.drawable.bg_success)
                    }
                    is ResultState1.fail ->{
                        fnShowMessage(getString(state.message),requireContext(),R.drawable.error_bg)

                        if(state.message == R.string.newEx_AllFieldsAreEmpty)
                        {
                            newExpenseBinding.idENewExpense.isFocusable = true
                            newExpenseBinding.idENewExpense.requestFocus()
                        }
                        else if(state.message == R.string.newEx_ExpenseAmountMissing){
                            newExpenseBinding.idENewExpense.isFocusable = true
                            newExpenseBinding.idENewExpense.requestFocus()
                        }
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Expense Insert Expense: ${e.message}")
                Log.e("EXPENSE_INSERT_STATUS","Expense Insert Expense: ${e.message}")
            }
        }

    }

    fun fnShowDialog()
    {
        try
        {
            splitBinding = DataBindingUtil.inflate(layoutInflater,R.layout.split_dialogue,null,false)
            splitBinding.split = splitViewModel
            splitBinding.lifecycleOwner = viewLifecycleOwner

            splitDialog = AlertDialog.Builder(requireContext())
                .setView(splitBinding.root)
                .setCancelable(false)
                .create()

            splitDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            splitDialog?.show()

            var totAmt = Global.fnFormatFloatTwoDigits(newExpenseViewModel.expenseAmt.value?.toFloat())
            splitViewModel._totAmtUi.value = "Total: ${totAmt}"

            splitViewModel._totAmt.value = totAmt
            splitViewModel._amtInCash.value=totAmt

            splitBinding.idEAmtInCash.post{
                splitBinding.idEAmtInCash.selectAll()
                splitBinding.idEAmtInCash.requestFocus()
            }

            splitBinding.idEAmtInCash.setOnFocusChangeListener{ view,hasFocus ->
                if(!hasFocus){
                    splitBinding.idEAmtInCard.selectAll()
                    splitBinding.idEAmtInCard.requestFocus()
                }
            }

            splitBinding.idEAmtInCard.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus){
                    splitBinding.idEAmtInUpi.selectAll()
                    splitBinding.idEAmtInUpi.requestFocus()
                }
            }

            splitBinding.idEAmtInUpi.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus){
                    splitBinding.idBtnOk.requestFocus()
                }
            }
        }
        catch (e : Exception)
        {
            logger.logError(LOG_TAG,"Show Split Screen: ${e.message}")
            Log.e("SHOW_SPLIT_DIALOG","Show Split Screen: ${e.message}")
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewExpense.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewExpense().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
            }
        }
    }
}

//   suspend fun fnGetCloudUserId(): String {
//       var auth = FirebaseAuth.getInstance()
//       if(auth.currentUser == null)
//       {
//            auth.signInAnonymously().await()
//       }
//       return auth.currentUser!!.uid
//   }

//private fun NewExpense.fnShowCategoryList(it: View) {
//    val inflater = LayoutInflater.from(requireContext())
//    val popupView = inflater.inflate(R.layout.category_list, null)
//
//
//
//    val popupWindow = PopupWindow(
//        popupView,
//        LinearLayout.LayoutParams.WRAP_CONTENT,
//        LinearLayout.LayoutParams.WRAP_CONTENT,
//        true
//    )
//
//    popupWindow.elevation = 8f
//
//    // Show below the button
//    popupWindow.showAsDropDown(it)
//}
