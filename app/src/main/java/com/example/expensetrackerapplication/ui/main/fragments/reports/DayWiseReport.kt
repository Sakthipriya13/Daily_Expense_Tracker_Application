package com.example.expensetrackerapplication.ui.main.fragments.reports

import android.app.AlertDialog
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.entity.CategoryEntitty
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.databinding.ConfirmationPromptBinding
import com.example.expensetrackerapplication.databinding.DayWiseReportBinding
import com.example.expensetrackerapplication.databinding.DayWiseReportListItemBinding
import com.example.expensetrackerapplication.databinding.EditExpenseBinding
import com.example.expensetrackerapplication.databinding.SplitDialogueBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.model.CurrentDayReportModel
import com.example.expensetrackerapplication.model.PaymentType
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.ui_event.DayWiseReportClickListener
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.DayWiseReportViewModel
import com.example.expensetrackerapplication.viewmodel.EditExpenseViewModel

import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.SplitViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DayWiseReport.newInstance] factory method to
 * create an instance of this fragment.
 */
class DayWiseReport : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }
    public lateinit var DayWiseReportBinding : DayWiseReportBinding
    public val DayWiseReportViewModel: DayWiseReportViewModel by activityViewModels{
        appViewModelFactory
    }

//    val reportMenuViewModel : ReportMenuViewModel by activityViewModels()

//    val mainViewModel : MainViewModel by activityViewModels()
    lateinit var listAdapter : ListAdapter

//    private lateinit var mainViewBinding : MainBinding

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
        DayWiseReportBinding = DataBindingUtil.inflate(inflater,R.layout.day_wise_report, container, false)
        DayWiseReportBinding.currentDayReportViewModel=DayWiseReportViewModel

        DayWiseReportBinding.lifecycleOwner = viewLifecycleOwner

        return DayWiseReportBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DayWiseReportViewModel.fnPreWarmExcelEngine()

        listAdapter = ListAdapter()
        DayWiseReportBinding.idDayWiseReportView.adapter = listAdapter
        DayWiseReportBinding.idDayWiseReportView.layoutManager = LinearLayoutManager(requireContext())

        DayWiseReportViewModel.isClosed.observe(viewLifecycleOwner){ isClose ->
            try {
                if(isClose==true)
                {
                    findNavController().navigate(R.id.action_day_wise_report_to_report_menu)
                    DayWiseReportViewModel.resetCloseState()
                }
            }
            catch (e: Exception){
                Log.e("DAY_WISE_REPORT","Close The Day Wise Report: ${e.message}")
            }
        }
        
        DayWiseReportBinding.idCalendarButton.setOnClickListener {
            try {
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

                            DayWiseReportViewModel._selectedDate.value=date
                            DayWiseReportViewModel._selectedDateUi.value=dateUi
                            Global.isCalendarSelected=false

                        },year,month,day)
                    datePickerDialog.setCancelable(false)
                    datePickerDialog.setCanceledOnTouchOutside(false)

                    datePickerDialog.setOnCancelListener {
                        Global.isCalendarSelected=false
                    }

                    datePickerDialog.show()
                }
            }
            catch (e: Exception){
                Log.e("DAY_WISE_REPORT","Show The Calendar Prompt: ${e.message}")
            }
        }

        DayWiseReportViewModel.selectedDate.observe(viewLifecycleOwner){ date ->
            try {
                DayWiseReportViewModel.fnClearAllFields()
                DayWiseReportViewModel.fnGetExpenseDetails(date)
            }
            catch (e: Exception){
                Log.e("DAY_WISE_REPORT","Select Date: ${e.message}")
            }
        }

        DayWiseReportViewModel.expenseList.observe(viewLifecycleOwner){ list ->
            try {
                if(list.isNotEmpty())
                {
                    DayWiseReportViewModel._isExportLoading.value = false

                    DayWiseReportBinding.idNoReportsText.visibility = View.GONE
                    DayWiseReportBinding.idContentLayout.visibility = View.VISIBLE

                    listAdapter.fnSubmitList(list, object : DayWiseReportClickListener {
                        override fun onDeleteClick(expense: CurrentDayReportModel) {
                            if(!expense.isDelete.equals("DELETED"))
                                fnShowDeletePrompt(expense)
                            else
                                fnShowMessage(getString(R.string.dayWiseReport_ExpenseWasAlreadyDeleted),requireContext(),R.drawable.bg_info)
                        }

                        override fun onClickEdit(expense: CurrentDayReportModel) {
                            if(!expense.isDelete.equals("DELETED"))
                            {
                                if(Global.isBottomSheetSelected==false)
                                {
                                    Global.isBottomSheetSelected=true
                                    EditExpense(expense).show(parentFragmentManager,"EditExpenseBottomSheet")
                                }
                            }
                            else
                            {
                                fnShowMessage(getString(R.string.dayWiseReport_EditNotAllowed),requireContext(),R.drawable.error_bg)
                            }
                        }

                    })
                    listAdapter.notifyDataSetChanged()
                }
                else
                {
                    DayWiseReportViewModel._isExportLoading.value = false
                    DayWiseReportBinding.idNoReportsText.visibility = View.VISIBLE
                    DayWiseReportBinding.idContentLayout.visibility = View.GONE
                }
            }
            catch (e: Exception){
                Log.e("DAY_WISE_REPORT","Expense List Observed: ${e.message}")
            }
        }

        DayWiseReportViewModel.exportStatus.observe(viewLifecycleOwner){ status ->
            try {
                when(status)
                {
                    is ResultState1.success ->{
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.bg_success)
                    }
                    is ResultState1.fail ->{
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.error_bg)
                    }
                }
            }
            catch (e: Exception)
            {
                Log.e("DAY_WISE_REPORT","Export Status: ${e.message}")
            }
        }

        DayWiseReportViewModel.expenseDeleteStatus.observe(viewLifecycleOwner){ status ->
            try {
                when(status)
                {
                    is ResultState1.success ->{
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.bg_success)
                    }
                    is ResultState1.fail ->{
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.error_bg)
                    }
                }
            }
            catch (e: Exception){
                Log.e("DAY_WISE_REPORT","Expense Delete Status: ${e.message}")
            }
        }

        DayWiseReportViewModel.isExportLoading.observe(viewLifecycleOwner){ isLoading ->
            try {
                if(isLoading)
                {
                    DayWiseReportBinding.isExportLoading.visibility=View.VISIBLE
                }
                else
                {
                    DayWiseReportBinding.isExportLoading.visibility=View.GONE
                }
            }
            catch (e: Exception){
                Log.e("DAY_WISE_REPORT","Display Progress Bar : ${e.message}")
            }
        }

    }

    fun fnShowDeletePrompt(expense : CurrentDayReportModel)
    {
        try
        {
            // PromptBinding Variable Initialization
            var promptBinding = ConfirmationPromptBinding.inflate(layoutInflater)
            // Assign Title
            promptBinding.tittle = getString(R.string.warning)
            // Assign Content
            promptBinding.message = getString(R.string.do_you_want_to_delete_the_expense)
            // AlertDialog Variable Initialization
            val deletePrompt = AlertDialog.Builder(requireContext())
                .setView(promptBinding.root)
                .setCancelable(false)
                .create()
            // Set Transparent Background
            deletePrompt.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // On Click Ok Button Operation
            promptBinding.idBtnOk.setOnClickListener {
                DayWiseReportViewModel.fnDeleteExpense(expense.expenseId)
                deletePrompt.dismiss()
            }
            // On Click Cancel Button Operation
            promptBinding.idBtnCancel.setOnClickListener {
                deletePrompt.dismiss()
            }
            // Display Prompt
            deletePrompt.show()
        }
        catch (e: Exception)
        {
            Log.e("DAY_WISE_REPORT","Show Delete Prompt: ${e.message}")
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DayWiseReport.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DayWiseReport().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

// RecyclerView Adapter
class ListAdapter() : RecyclerView.Adapter<ListAdapter.ListViewHolder>()
{
    // Expense List Variable Initialization
    private  var expenseList : List<CurrentDayReportModel> = emptyList()
    // Day-Wise Report Click Listener Variable Initialization
    lateinit var deleteClickListener : DayWiseReportClickListener

    // SubmitList Function Definition
    fun fnSubmitList(
        list: List<CurrentDayReportModel>,
        listener: DayWiseReportClickListener
    ){
        try {
            expenseList=list
            deleteClickListener = listener
            notifyDataSetChanged()
        }
        catch (e: Exception){
            Log.e("DAY_WISE_REPORT","List Adapter_SubmitList Function: ${e.message}")
        }
    }
    // Inner Class
    inner class ListViewHolder (val binding: DayWiseReportListItemBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(item: CurrentDayReportModel)
        {
            try {
                binding.dayWiseReportListItem=item
                binding.deleteClickListener=deleteClickListener
                binding.executePendingBindings()
            }
            catch (e: Exception){
                Log.e("DAY_WISE_REPORT","bind: ${e.message}")
            }
        }

    }
    //
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder
    {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<DayWiseReportListItemBinding>(inflater,R.layout.day_wise_report_list_item,parent,false)
            return ListViewHolder(binding)

    }

    override fun onBindViewHolder(
        holder: ListViewHolder,
        position: Int
    ) {
        holder.bind(expenseList[position])
    }

    // Return Item Count
    override fun getItemCount(): Int {
       return expenseList.size
    }

}

class EditExpense(var expense : CurrentDayReportModel) : BottomSheetDialogFragment()
{
    private lateinit var editExpenseBinding : EditExpenseBinding
    val editExpenseViewModel : EditExpenseViewModel by viewModels()

    val settingsViewModel : SettingsViewModel by activityViewModels()

    var categoryList =emptyList<CategoryEntitty>()

    private lateinit var splitBinding : SplitDialogueBinding

    val splitViewModel : SplitViewModel by viewModels()

    var splitDialog : AlertDialog? = null

    var selectedCategoryId : Int =-1

    val DayWiseReportViewModel: DayWiseReportViewModel by activityViewModels ()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        editExpenseBinding= DataBindingUtil.inflate(inflater,R.layout.edit_expense,container,false)
        editExpenseBinding.edit=editExpenseViewModel
        editExpenseBinding.lifecycleOwner=viewLifecycleOwner

        return editExpenseBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            try {
                editExpenseViewModel.fnGetExpenseDetailsPerId(expense.expenseId)
            }
            catch (e: Exception){
                Log.e("EDIT_EXPENSE","Get Expense Details Per Id: ${e.message}")
            }
        }

        editExpenseViewModel.expenseList.observe(viewLifecycleOwner){ list ->
            try {
                if(list.isNotEmpty())
                {
                    for(ex in list)
                    {
                        editExpenseViewModel._editedExpenseId.value = ex.expenseId
                        editExpenseViewModel._editedExpenseDate.value = ex.expenseDate

                        editExpenseViewModel._selectedDate.value = ex.expenseDate
                        var uiDate = "${ex.expenseDate.substring(8)}-${ex.expenseDate.substring(5,7)}-${ex.expenseDate.substring(0,4)}"
                        editExpenseViewModel._selectedDateUi.value = uiDate

                        editExpenseViewModel._expenseAmt.value = ex.expenseAmt.toString()

                        editExpenseBinding.idENewExpense.post {
                            editExpenseBinding.idENewExpense.requestFocus()
                            editExpenseBinding.idENewExpense.setSelection(0,
                                editExpenseBinding.idENewExpense.text?.length ?: 0
                            )
                        }

                        selectedCategoryId = ex.expenseCategoryId

                        editExpenseViewModel._remarks.value = ex.expenseRemarks

                        val paymentType = PaymentType(
                            cash = ex.expenseAmtInCash,
                            card = ex.expenseAmtInCard,
                            upi = ex.expenseAmtInUpi,
                            others = ex.expenseAmtInOthers
                        )
                        editExpenseViewModel._selectedPaymentTypeAmt.value = paymentType
//                        editExpenseViewModel._amtInCash.value = ex.expenseAmtInCash
//                        editExpenseViewModel._amtInCard.value = ex.expenseAmtInCard
//                        editExpenseViewModel._amtInUpi.value = ex.expenseAmtInUpi
//                        editExpenseViewModel._amtInOthers.value = ex.expenseAmtInOthers

//                        editExpenseViewModel._paymentType.value = ex.paymentType

//                        Log.i("PAYMENT TYPE","Payment Type: ${ex.paymentType}")

                        if( ex.paymentType == Global.PAYMENT_TYPE_CASH)
                        {
                            editExpenseViewModel._selectedPaymentType.value = R.id.idCashPayment
                        }
                        if(ex.paymentType  == Global.PAYMENT_TYPE_CARD)
                        {
                            editExpenseViewModel._selectedPaymentType.value = R.id.idCardPayment
                        }
                        if(ex.paymentType == Global.PAYMENT_TYPE_UPI)
                        {
                            editExpenseViewModel._selectedPaymentType.value = R.id.idUpiPayment
                        }
                        if(ex.paymentType == Global.PAYMENT_TYPE_SPLIT)
                        {
                            editExpenseViewModel._selectedPaymentType.value = R.id.idSplitPayment
                        }
                        if(ex.paymentType  == Global.PAYMENT_TYPE_OTHER)
                        {
                            editExpenseViewModel._selectedPaymentType.value = R.id.idOthersPayment
                        }
                    }
                    settingsViewModel.fnGetAllCategories()
                }
            }
            catch (e: Exception){
                Log.e("EDIT_EXPENSE","Expense List Observed: ${e.message}")
            }
        }

        settingsViewModel.categoryList.observe(viewLifecycleOwner){ list ->
            try {
                categoryList = list
                val categoryNameList= list.map {it.categoryName}

                val autoCompleteAdapter= ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item,
                    categoryNameList)

                editExpenseBinding.idDCategories.setAdapter(autoCompleteAdapter)

                if(selectedCategoryId!=-1)
                {
                    var selected = list.find { it.categoryId == selectedCategoryId }
                    selected?.let { it ->
                        editExpenseBinding.idDCategories.setText(it.categoryName, false)

                        editExpenseViewModel._selectedCategoryId.value = it.categoryId
                        editExpenseViewModel._selectedCategoryName.value = it.categoryName
                    }
                }
            }
            catch (e: Exception){
                Log.e("EDIT_EXPENSE","Category List Observed: ${e.message}")
            }
        }

        editExpenseViewModel.clearAllFields.observe(viewLifecycleOwner){ ob ->
            try {
                if(ob)
                {
                    editExpenseBinding.idENewExpense.isFocusable=true
                    editExpenseBinding.idENewExpense.requestFocus()
                }
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Clear All Fields Value And Set Focus To New Expense Field: ${e.message}")
            }
        }

        editExpenseViewModel.isClosed.observe(viewLifecycleOwner){ status ->
            try {
                if(status)
                {
                    Global.isBottomSheetSelected = false
                    dismiss()
                }
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Close Edit Expense Screen: ${e.message}")
            }
        }

        editExpenseBinding.idCalendarButton.setOnClickListener {
            try {
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

                            editExpenseViewModel._selectedDate.value=date
                            editExpenseViewModel._selectedDateUi.value=dateUi
                            Global.isCalendarSelected=false

                        },year,month,day)
                    datePickerDialog.setCancelable(false)
                    datePickerDialog.setCanceledOnTouchOutside(false)

                    datePickerDialog.setOnCancelListener {
                        Global.isCalendarSelected=false
                    }

                    datePickerDialog.show()
                }
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Show Calendar: ${e.message}")
            }
        }

        editExpenseBinding.idDCategories.setOnItemClickListener { parent,_,position,_ ->
            try
            {
                var selectedCategoryName = parent.getItemAtPosition(position).toString()
                editExpenseViewModel._selectedCategoryName.value=selectedCategoryName
                editExpenseViewModel._selectedCategoryId.value=categoryList[position].categoryId
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Select Category: ${e.message}")
            }
        }

        splitViewModel.amtInCash.observe(viewLifecycleOwner){ amt ->
            try
            {
                val currentPaymentType =
                    editExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                editExpenseViewModel._selectedPaymentTypeAmt.value =
                    currentPaymentType.copy(
                        cash = amt?.toFloat() ?: 0f
                    )
                splitViewModel.fnUpdateFromCash()
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Amount In Cash: ${e.message}")
            }
        }

        splitViewModel.amtInCard.observe(viewLifecycleOwner){ amt ->
            try
            {
                val currentPaymentType =
                    editExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                editExpenseViewModel._selectedPaymentTypeAmt.value =
                    currentPaymentType.copy(
                        card = amt?.toFloat() ?: 0f
                    )
                splitViewModel.fnUpdateFromCard()
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Amount In Card: ${e.message}")
            }
        }

        splitViewModel.amtInUpi.observe(viewLifecycleOwner){ amt ->
            try
            {
                val currentPaymentType =
                    editExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                editExpenseViewModel._selectedPaymentTypeAmt.value = currentPaymentType.copy(
                    upi = amt?.toFloat() ?: 0.00f
                )
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Amount In Upi: ${e.message}")
            }
        }

        splitViewModel.splitStatus.observe(viewLifecycleOwner){ status ->
            try {
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
            catch (e: Exception){
                Log.e("EDIT_EXPENSE","Split Status: ${e.message}")
            }
        }

        splitViewModel.okSplit.observe(viewLifecycleOwner){ amt ->
            try {
                editExpenseBinding.idERemarks.requestFocus()
                editExpenseBinding.idERemarks.isFocusable=true
                splitDialog?.dismiss()
            }
            catch (e: Exception) {
                Log.e("EDIT_EXPENSE", "Ok Split: ${e.message}")
            }
        }

        splitViewModel.isClosed.observe(viewLifecycleOwner) { close ->
            try {
                if(close)
                {
//                editExpenseViewModel._paymentType.value=-1
                    editExpenseViewModel._selectedPaymentType.value=-1
                    splitDialog?.dismiss()
                }
            }
            catch (e: Exception){
                Log.e("EDIT_EXPENSE","Close The Split Screen: ${e.message}")
            }
        }
        editExpenseViewModel.showSplitDialog.observe(viewLifecycleOwner){ isChecked ->
            try {
                if(isChecked)
                {
                    fnShowDialog()
                    editExpenseViewModel._showSplitDialog.value = false
                }
                else
                {
                    fnShowMessage(getString(R.string.enter_expense),requireContext(),R.drawable.error_bg)
                    editExpenseBinding.idENewExpense.isFocusable=true
                    editExpenseBinding.idENewExpense.requestFocus()
                }
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Show Split Dialog: ${e.message}")
            }
        }

        editExpenseViewModel.expenseInsertStatus.observe(viewLifecycleOwner){ status ->
            try {
                when(status){
                    is ResultState1.success ->{
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.bg_success)
                    }
                    is ResultState1.fail ->{
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.error_bg)

                        if(status.message == R.string.newEx_AllFieldsAreEmpty)
                        {
                            editExpenseBinding.idENewExpense.isFocusable = true
                            editExpenseBinding.idENewExpense.requestFocus()
                        }
                        else if(status.message == R.string.newEx_ExpenseAmountMissing){
                            editExpenseBinding.idENewExpense.isFocusable = true
                            editExpenseBinding.idENewExpense.requestFocus()
                        }
                    }
                }
            }
            catch (e: Exception)
            {
                Log.e("EDIT_EXPENSE","Expense Insert Expense: ${e.message}")
            }
        }

    }

    fun fnShowDialog()
    {
        try {
            splitBinding = DataBindingUtil.inflate(layoutInflater,R.layout.split_dialogue,null,false)
            splitBinding.split = splitViewModel
            splitBinding.lifecycleOwner = viewLifecycleOwner

            splitDialog = AlertDialog.Builder(requireContext())
                .setView(splitBinding.root)
                .setCancelable(false)
                .create()

            splitDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            splitDialog?.show()

            splitViewModel._totAmtUi.value = "Total: ${editExpenseViewModel.expenseAmt.value}"

            splitViewModel._totAmt.value = editExpenseViewModel.expenseAmt.value.toString()
            splitViewModel._amtInCash.value=editExpenseViewModel.expenseAmt.value.toString()

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
        catch (e: Exception){
            Log.e("EDIT_EXPENSE","Show Split Screen: ${e.message}")
        }
    }
}

