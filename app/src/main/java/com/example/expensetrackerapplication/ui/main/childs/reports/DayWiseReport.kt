package com.example.expensetrackerapplication.ui.main.childs.reports

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
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
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.databinding.ConfirmationPromptBinding
import com.example.expensetrackerapplication.databinding.DayWiseReportBinding
import com.example.expensetrackerapplication.databinding.DayWiseReportListItemBinding
import com.example.expensetrackerapplication.databinding.EditExpenseBinding
import com.example.expensetrackerapplication.databinding.SplitDialogueBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.model.CurrentDayReportModel
import com.example.expensetrackerapplication.model.PaymentType
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.ui.listeners.DayWiseReportClickListener
import com.example.expensetrackerapplication.utils.ResultState
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.DayWiseReportViewModel
import com.example.expensetrackerapplication.viewmodel.DeletePromptViewModel
import com.example.expensetrackerapplication.viewmodel.EditExpenseViewModel

import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel
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
    lateinit var DayWiseReportBinding: DayWiseReportBinding
    val DayWiseReportViewModel: DayWiseReportViewModel by activityViewModels {
        appViewModelFactory
    }

    //   val reportMenuViewModel : ReportMenuViewModel by activityViewModels()
//   val mainViewModel : MainViewModel by activityViewModels()
    lateinit var listAdapter: ListAdapter

//    private lateinit var mainViewBinding : MainBinding

    private lateinit var logger: FileLogger
//        FileLogger(requireContext().applicationContext)

    val LOG_TAG = "DAY_WISE_REPORT"

    private lateinit var splitBinding: SplitDialogueBinding
    var splitDialog: AlertDialog? = null

    val splashViewModel: SplashViewModel by viewModels {
        appViewModelFactory
    }

    private lateinit var deletePromptBinding: ConfirmationPromptBinding

    private lateinit var deletePromptDialog: android.app.AlertDialog

    val deletePromptViewModel: DeletePromptViewModel by viewModels {
        appViewModelFactory
    }

    var deleteExpense: CurrentDayReportModel? = null

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
        logger = FileLogger(requireContext().applicationContext)

        DayWiseReportBinding =
            DataBindingUtil.inflate(inflater, R.layout.day_wise_report, container, false)
        DayWiseReportBinding.currentDayReportViewModel = DayWiseReportViewModel

        DayWiseReportBinding.lifecycleOwner = viewLifecycleOwner

        return DayWiseReportBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            deletePromptBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.confirmation_prompt, null, false)
            deletePromptBinding.prompt = deletePromptViewModel
            deletePromptBinding.lifecycleOwner = viewLifecycleOwner

            deletePromptDialog = android.app.AlertDialog.Builder(requireContext())
                .setView(deletePromptBinding.root)
                .setCancelable(false)
                .create()
            deletePromptDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: Exception) {
            logger.logError(LOG_TAG, "Delete Prompt Screen Creation: ${e.message}")
        }

        deletePromptViewModel.isClose.observe(viewLifecycleOwner) { isClose ->
            try {
                when (isClose) {
                    is ResultState.Success -> {
                        Global.isCalendarSelected = false
                        deletePromptDialog.dismiss()
                    }

                    is ResultState.fail -> {
                        Global.isCalendarSelected = false
                        DayWiseReportViewModel.fnDeleteExpense(deleteExpense?.expenseId)
                        deletePromptDialog.dismiss()
                    }
                }
            } catch (e: Exception) {
                logger.logError(
                    LOG_TAG,
                    "Is Close Value Observed From DeletePromptViewModel: ${e.message}"
                )
            }
        }


        lifecycleScope.launch {
            Global.fnPreWarmExcelEngine(logger)
        }
        try {
            listAdapter = ListAdapter(requireContext().applicationContext)
            DayWiseReportBinding.idDayWiseReportView.adapter = listAdapter
            DayWiseReportBinding.idDayWiseReportView.layoutManager =
                LinearLayoutManager(requireContext())
        } catch (e: Exception) {
            logger.logError(LOG_TAG, "List Adapter Initialization: ${e.message}")
        }



        DayWiseReportViewModel.isClosed.observe(viewLifecycleOwner) { isClose ->
            try {
                if (isClose == true) {
                    findNavController().navigate(R.id.action_day_wise_report_to_report_menu)
                    DayWiseReportViewModel.resetCloseState()
                }
            } catch (e: Exception) {
                logger.logError(LOG_TAG, "Close The Day Wise Report: ${e.message}")
                Log.e("DAY_WISE_REPORT", "Close The Day Wise Report: ${e.message}")
            }
        }

        DayWiseReportBinding.idCalendarButton.setOnClickListener {
            try {
                if (Global.isCalendarSelected == false) {
                    Global.isCalendarSelected = true
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        { _, y, m, d ->

                            calendar.set(y, m, d)
                            val sdf1 = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val date = sdf.format(calendar.time)
                            val dateUi = sdf1.format(calendar.time)

                            DayWiseReportViewModel._selectedDateUi.value = dateUi
                            DayWiseReportViewModel._selectedDate.value = date
                            Global.isCalendarSelected = false

                            Log.i(LOG_TAG,"3Selected Date: ${DayWiseReportViewModel.selectedDate.value}")
                            Log.i(LOG_TAG,"4Selected DateUi: ${DayWiseReportViewModel.selectedDateUi.value}")

                        }, year, month, day
                    )
                    datePickerDialog.setCancelable(false)
                    datePickerDialog.setCanceledOnTouchOutside(false)

                    datePickerDialog.setOnCancelListener {
                        Global.isCalendarSelected = false
                    }

                    datePickerDialog.show()
                }
            } catch (e: Exception) {
                logger.logError(LOG_TAG, "Show The Calendar Prompt: ${e.message}")
                Log.e("DAY_WISE_REPORT", "Show The Calendar Prompt: ${e.message}")
            }
        }

        DayWiseReportViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            try {
                Log.i(LOG_TAG,"1Selected Date: ${DayWiseReportViewModel.selectedDate.value}")
                Log.i(LOG_TAG,"2Selected DateUi: ${DayWiseReportViewModel.selectedDateUi.value}")

                DayWiseReportViewModel.fnClearAllFields()
                DayWiseReportViewModel.fnGetExpenseDetails(date)
            } catch (e: Exception) {
                logger.logError(LOG_TAG, "Select Date: ${e.message}")
                Log.e("DAY_WISE_REPORT", "Select Date: ${e.message}")
            }
        }

        DayWiseReportViewModel.expenseList.observe(viewLifecycleOwner) { list ->
            try {
                if (list.isNotEmpty()) {
                    DayWiseReportViewModel._isExportLoading.value = false

                    DayWiseReportBinding.idNoReportsText.visibility = View.GONE
                    DayWiseReportBinding.idContentLayout.visibility = View.VISIBLE

                    listAdapter.fnSubmitList(list, object : DayWiseReportClickListener {
                        override fun onDeleteClick(expense: CurrentDayReportModel) {
                            try {
                                if (!expense.isDelete.equals("DELETED")) {
                                    if (Global.isCalendarSelected == false) {
                                        deleteExpense = expense

                                        Global.isCalendarSelected = true

                                        deletePromptViewModel._title.value =
                                            getString(R.string.warning)

                                        deletePromptViewModel._message.value =
                                            getString(R.string.do_you_want_to_delete_the_expense)

                                        deletePromptDialog.show()
                                    }
                                } else
                                    fnShowMessage(
                                        getString(R.string.dayWiseReport_ExpenseWasAlreadyDeleted),
                                        requireContext(),
                                        R.drawable.bg_info,
                                        logger,
                                        LOG_TAG
                                    )
                            } catch (e: Exception) {
                                logger.logError(
                                    LOG_TAG,
                                    "Display Delete Prompt Screen: ${e.message}"
                                )
                            }
                        }

                        override fun onClickEdit(expense: CurrentDayReportModel) {
                            try {
                                if (!expense.isDelete.equals("DELETED")) {
                                    if (Global.isBottomSheetSelected == false)
                                    {
                                        Global.isBottomSheetSelected = true
                                        EditExpense(expense).show(
                                            parentFragmentManager,
                                            "EditExpenseBottomSheet"
                                        )
                                    }
                                }
                                else
                                {
                                    fnShowMessage(
                                        getString(R.string.dayWiseReport_EditNotAllowed),
                                        requireContext(),
                                        R.drawable.error_bg,
                                        logger,
                                        LOG_TAG
                                    )
                                }
                            }
                            catch (e: Exception)
                            {
                                logger.logError(
                                    LOG_TAG,
                                    "On Click Edit Expense Button: ${e.message}"
                                )
                            }
                        }

                    })
                    listAdapter.notifyDataSetChanged()
                } else {
                    DayWiseReportViewModel._isExportLoading.value = false
                    DayWiseReportBinding.idNoReportsText.visibility = View.VISIBLE
                    DayWiseReportBinding.idContentLayout.visibility = View.GONE
                }
            } catch (e: Exception) {
                logger.logError(LOG_TAG, "Expense List Observed: ${e.message}")
                Log.e("DAY_WISE_REPORT", "Expense List Observed: ${e.message}")
            }
        }

        DayWiseReportViewModel.exportStatus.observe(viewLifecycleOwner) { status ->
            try {
                when (status) {
                    is ResultState1.success -> {
                        fnShowMessage(
                            getString(status.message),
                            requireContext(),
                            R.drawable.bg_success,
                            logger,
                            LOG_TAG
                        )
                    }

                    is ResultState1.fail -> {
                        fnShowMessage(
                            getString(status.message),
                            requireContext(),
                            R.drawable.error_bg,
                            logger,
                            LOG_TAG
                        )
                    }
                }
            } catch (e: Exception) {
                logger.logError(LOG_TAG, "Export Status: ${e.message}")
                Log.e("DAY_WISE_REPORT", "Export Status: ${e.message}")
            }
        }

        DayWiseReportViewModel.expenseDeleteStatus.observe(viewLifecycleOwner) { status ->
            try {
                when (status) {
                    is ResultState1.success -> {
                        fnShowMessage(
                            getString(status.message),
                            requireContext(),
                            R.drawable.bg_success,
                            logger,
                            LOG_TAG
                        )
                    }

                    is ResultState1.fail -> {
                        fnShowMessage(
                            getString(status.message),
                            requireContext(),
                            R.drawable.error_bg,
                            logger,
                            LOG_TAG
                        )
                    }
                }
            } catch (e: Exception) {
                logger.logError(LOG_TAG, "Expense Delete Status: ${e.message}")
                Log.e("DAY_WISE_REPORT", "Expense Delete Status: ${e.message}")
            }
        }

        DayWiseReportViewModel.isExportLoading.observe(viewLifecycleOwner) { isLoading ->
            try {
                if (isLoading) {
                    DayWiseReportBinding.isExportLoading.visibility = View.VISIBLE
                } else {
                    DayWiseReportBinding.isExportLoading.visibility = View.GONE
                }
            } catch (e: Exception) {
                logger.logError(LOG_TAG, "Display Progress Bar : ${e.message}")
                Log.e("DAY_WISE_REPORT", "Display Progress Bar : ${e.message}")
            }
        }

    }

//    fun fnShowDeletePrompt(expense : CurrentDayReportModel)
//    {
//        try
//        {
//            // PromptBinding Variable Initialization
////            var promptBinding = ConfirmationPromptBinding.inflate(layoutInflater)
//            // Assign Title
//            promptBinding.tittle = getString(R.string.warning)
//            // Assign Content
//            promptBinding.message = getString(R.string.do_you_want_to_delete_the_expense)
//            // AlertDialog Variable Initialization
//            val deletePrompt = AlertDialog.Builder(requireContext())
//                .setView(promptBinding.root)
//                .setCancelable(false)
//                .create()
//            // Set Transparent Background
//            deletePrompt.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            // On Click Ok Button Operation
//            promptBinding.idBtnOk.setOnClickListener {
//                DayWiseReportViewModel.fnDeleteExpense(expense.expenseId)
//                deletePrompt.dismiss()
//            }
//            // On Click Cancel Button Operation
//            promptBinding.idBtnCancel.setOnClickListener {
//                deletePrompt.dismiss()
//            }
//            // Display Prompt
//            deletePrompt.show()
//        }
//        catch (e: Exception)
//        {
//            logger.logError(LOG_TAG,"Show Delete Prompt: ${e.message}")
//            Log.e("DAY_WISE_REPORT","Show Delete Prompt: ${e.message}")
//        }


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


    // RecyclerView Adapter
    class ListAdapter(applicationContext: Context) :
        RecyclerView.Adapter<ListAdapter.ListViewHolder>() {
        // Expense List Variable Initialization
        private var expenseList: List<CurrentDayReportModel> = emptyList()

        // Day-Wise Report Click Listener Variable Initialization
        lateinit var deleteClickListener: DayWiseReportClickListener

        val logger = FileLogger(applicationContext)

        val LOG_TAG = "LIST_ADAPTER"

        // SubmitList Function Definition
        fun fnSubmitList(
            list: List<CurrentDayReportModel>,
            listener: DayWiseReportClickListener
        ) {
            try {
                expenseList = list
                deleteClickListener = listener
                notifyDataSetChanged()
            } catch (e: Exception) {
                logger.logError(LOG_TAG, "SubmitList Function: ${e.message}")
                Log.e("DAY_WISE_REPORT", "List Adapter_SubmitList Function: ${e.message}")
            }
        }

        // Inner Class
        inner class ListViewHolder(val binding: DayWiseReportListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(item: CurrentDayReportModel) {
                try {
                    binding.dayWiseReportListItem = item
                    binding.deleteClickListener = deleteClickListener
                    binding.executePendingBindings()
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "bind: ${e.message}")
                    Log.e("DAY_WISE_REPORT", "bind: ${e.message}")
                }
            }

        }

        //
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<DayWiseReportListItemBinding>(
                inflater,
                R.layout.day_wise_report_list_item,
                parent,
                false
            )
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

    class EditExpense(var expense: CurrentDayReportModel) : BottomSheetDialogFragment() {
        val appViewModelFactory by lazy {
            AppViewModelFactory(
                requireActivity().application,
                FileLogger(requireContext().applicationContext)
            )
        }
        private lateinit var editExpenseBinding: EditExpenseBinding
        val editExpenseViewModel: EditExpenseViewModel by viewModels {
            appViewModelFactory
        }

        val settingsViewModel: SettingsViewModel by activityViewModels {
            appViewModelFactory
        }

        var categoryList = emptyList<CategoryEntitty>()

        private lateinit var splitBinding: SplitDialogueBinding

        val splitViewModel: SplitViewModel by viewModels {
            appViewModelFactory
        }

        var splitDialog: AlertDialog? = null

        var selectedCategoryId: Int = -1

        val DayWiseReportViewModel: DayWiseReportViewModel by activityViewModels {
            appViewModelFactory
        }

        private lateinit var logger: FileLogger

        val LOG_TAG = "EDIT_EXPENSE"

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            isCancelable = false
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            logger = FileLogger(requireContext().applicationContext)

            editExpenseBinding =
                DataBindingUtil.inflate(inflater, R.layout.edit_expense, container, false)
            editExpenseBinding.edit = editExpenseViewModel
            editExpenseBinding.lifecycleOwner = viewLifecycleOwner

            return editExpenseBinding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

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

            }
            catch (e : Exception)
            {
                logger.logError(LOG_TAG,"Split Screen Creation: ${e.message}")
            }

            lifecycleScope.launch {
                try {
                    editExpenseViewModel.fnGetExpenseDetailsPerId(expense.expenseId)
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Get Expense Details Per Id: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Get Expense Details Per Id: ${e.message}")
                }
            }

            lifecycleScope.launchWhenStarted {
                editExpenseViewModel.uiEvent.collect { event ->
                    try
                    {
                        when(event)
                        {
                            SettingsViewModel.UiEvent.RecreateActivity ->{
                                requireActivity().recreate()
                            }
                        }
                    }
                    catch (e: Exception)
                    {
                        logger.logError(LOG_TAG,"Recreate Activity: ${e.message}")
                    }
                }
            }

            editExpenseViewModel.expenseList.observe(viewLifecycleOwner) { list ->
                try {
                    if (list.isNotEmpty()) {
                        for (ex in list) {
                            editExpenseViewModel._editedExpenseId.value = ex.expenseId
                            editExpenseViewModel._editedExpenseDate.value = ex.expenseDate
                            editExpenseViewModel._editedExpenseCloudId.value = ex.cloudId

                            editExpenseViewModel._selectedDate.value = ex.expenseDate
                            var uiDate = "${ex.expenseDate.substring(8)}-${
                                ex.expenseDate.substring(
                                    5,
                                    7
                                )
                            }-${ex.expenseDate.substring(0, 4)}"
                            editExpenseViewModel._selectedDateUi.value = uiDate

                            editExpenseViewModel._expenseAmt.value = ex.expenseAmt.toString()

                            editExpenseBinding.idENewExpense.post {
                                editExpenseBinding.idENewExpense.requestFocus()
                                editExpenseBinding.idENewExpense.setSelection(
                                    0,
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

                            if (ex.paymentType == Global.PAYMENT_TYPE_CASH) {
                                editExpenseViewModel._selectedPaymentType.value = R.id.idCashPayment
                            }
                            if (ex.paymentType == Global.PAYMENT_TYPE_CARD) {
                                editExpenseViewModel._selectedPaymentType.value = R.id.idCardPayment
                            }
                            if (ex.paymentType == Global.PAYMENT_TYPE_UPI) {
                                editExpenseViewModel._selectedPaymentType.value = R.id.idUpiPayment
                            }
                            if (ex.paymentType == Global.PAYMENT_TYPE_SPLIT) {
                                editExpenseViewModel._selectedPaymentType.value =
                                    R.id.idSplitPayment
                            }
                            if (ex.paymentType == Global.PAYMENT_TYPE_OTHER) {
                                editExpenseViewModel._selectedPaymentType.value =
                                    R.id.idOthersPayment
                            }
                        }
                        settingsViewModel.fnGetAllCategories()
                    }
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Expense List Observed: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Expense List Observed: ${e.message}")
                }
            }

            settingsViewModel.categoryList.observe(viewLifecycleOwner) { list ->
                try {
                    categoryList = list
                    val categoryNameList = list.map { it.categoryName }

                    val autoCompleteAdapter = ArrayAdapter(
                        requireContext(),
                        R.layout.dropdown_item,
                        categoryNameList
                    )

                    editExpenseBinding.idDCategories.setAdapter(autoCompleteAdapter)

                    if (selectedCategoryId != -1) {
                        var selected = list.find { it.categoryId == selectedCategoryId }
                        selected?.let { it ->
                            editExpenseBinding.idDCategories.setText(it.categoryName, false)

                            editExpenseViewModel._selectedCategoryId.value = it.categoryId
                            editExpenseViewModel._selectedCategoryName.value = it.categoryName
                        }
                    }
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Category List Observed: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Category List Observed: ${e.message}")
                }
            }

            editExpenseViewModel.clearAllFields.observe(viewLifecycleOwner) { ob ->
                try {
                    if (ob) {
                        editExpenseBinding.idENewExpense.isFocusable = true
                        editExpenseBinding.idENewExpense.requestFocus()
                    }
                } catch (e: Exception) {
                    logger.logError(
                        LOG_TAG,
                        "Clear All Fields Value And Set Focus To New Expense Field: ${e.message}"
                    )
                    Log.e(
                        "EDIT_EXPENSE",
                        "Clear All Fields Value And Set Focus To New Expense Field: ${e.message}"
                    )
                }
            }

            editExpenseViewModel.isClosed.observe(viewLifecycleOwner) { status ->
                try {
                    if (status)
                    {
                        Global.isBottomSheetSelected = false
                        dismiss()
                    }
                }
                catch (e: Exception)
                {
                    logger.logError(LOG_TAG, "Close Edit Expense Screen: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Close Edit Expense Screen: ${e.message}")
                }
            }

            editExpenseBinding.idCalendarButton.setOnClickListener {
                try {
                    if (Global.isCalendarSelected == false) {
                        Global.isCalendarSelected = true
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        val datePickerDialog = DatePickerDialog(
                            requireContext(),
                            { _, y, m, d ->

                                calendar.set(y, m, d)
                                val sdf1 = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val date = sdf.format(calendar.time)
                                val dateUi = sdf1.format(calendar.time)

                                editExpenseViewModel._selectedDate.value = date
                                editExpenseViewModel._selectedDateUi.value = dateUi
                                Global.isCalendarSelected = false

                            }, year, month, day
                        )
                        datePickerDialog.setCancelable(false)
                        datePickerDialog.setCanceledOnTouchOutside(false)

                        datePickerDialog.setOnCancelListener {
                            Global.isCalendarSelected = false
                        }

                        datePickerDialog.show()
                    }
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Show Calendar: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Show Calendar: ${e.message}")
                }
            }

            editExpenseBinding.idDCategories.setOnItemClickListener { parent, _, position, _ ->
                try {
                    var selectedCategoryName = parent.getItemAtPosition(position).toString()
                    editExpenseViewModel._selectedCategoryName.value = selectedCategoryName
                    editExpenseViewModel._selectedCategoryId.value =
                        categoryList[position].categoryId
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Select Category: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Select Category: ${e.message}")
                }
            }

            splitViewModel.amtInCash.observe(viewLifecycleOwner) { amt ->
                try {
                    val currentPaymentType =
                        editExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                    editExpenseViewModel._selectedPaymentTypeAmt.value =
                        currentPaymentType.copy(
                            cash = amt?.toFloat() ?: 0f
                        )
                    splitViewModel.fnUpdateTotalAmtFromCash()
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Amount In Cash: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Amount In Cash: ${e.message}")
                }
            }

            splitViewModel.amtInCard.observe(viewLifecycleOwner) { amt ->
                try {
                    val currentPaymentType =
                        editExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                    editExpenseViewModel._selectedPaymentTypeAmt.value =
                        currentPaymentType.copy(
                            card = amt?.toFloat() ?: 0f
                        )
                    splitViewModel.fnUpdateTotalAmtFromCard()
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Amount In Card: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Amount In Card: ${e.message}")
                }
            }

            splitViewModel.amtInUpi.observe(viewLifecycleOwner) { amt ->
                try {
                    val currentPaymentType =
                        editExpenseViewModel.selectedPaymentTypeAmt.value ?: PaymentType()

                    editExpenseViewModel._selectedPaymentTypeAmt.value = currentPaymentType.copy(
                        upi = amt?.toFloat() ?: 0.00f
                    )
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Amount In Upi: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Amount In Upi: ${e.message}")
                }
            }

            splitViewModel.splitStatus.observe(viewLifecycleOwner) { status ->
                try {
                    when (status) {
                        is ResultState1.success -> {}
                        is ResultState1.fail -> {
                            fnShowMessage(
                                getString(status.message),
                                requireContext(),
                                R.drawable.error_bg,
                                logger,
                                LOG_TAG
                            )
                            splitBinding.idEAmtInCash.selectAll()
                            splitBinding.idEAmtInCash.requestFocus()
                        }
                    }
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Split Status: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Split Status: ${e.message}")
                }
            }

            splitViewModel.okSplit.observe(viewLifecycleOwner) { amt ->
                try {
                    editExpenseBinding.idERemarks.requestFocus()
                    editExpenseBinding.idERemarks.isFocusable = true
                    splitDialog?.dismiss()
                    Global.isCalendarSelected = false
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Ok Split: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Ok Split: ${e.message}")
                }
            }

            splitViewModel.isClosed.observe(viewLifecycleOwner) { close ->
                try {
                    if (close) {
                        editExpenseViewModel._selectedPaymentType.value = -1
                        Global.isCalendarSelected = false
                        splitDialog?.dismiss()
                    }
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Close The Split Screen: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Close The Split Screen: ${e.message}")
                }
            }
            editExpenseViewModel.showSplitDialog.observe(viewLifecycleOwner) { isChecked ->
                try {
                    if (isChecked)
                    {
                        if(Global.isCalendarSelected == false)
                        {
                            Global.isCalendarSelected = true

                            var totAmt = Global.fnFormatFloatTwoDigits(editExpenseViewModel.expenseAmt.value?.toFloat(),logger)
                            splitViewModel._totAmtUi.value = "Total: ${totAmt}"
                            splitViewModel._totAmt.value = totAmt
                            splitViewModel._amtInCash.value=totAmt
                            splitViewModel._amtInCard.value = "0.00"
                            splitViewModel._amtInUpi.value = "0.00"

                            splitBinding.idEAmtInCash.post{
                                splitBinding.idEAmtInCard.clearFocus()
                                splitBinding.idEAmtInUpi.clearFocus()

                                splitBinding.idEAmtInCash.isFocusableInTouchMode = true
                                splitBinding.idEAmtInCash.requestFocus()
                                splitBinding.idEAmtInCash.selectAll()
                            }

                            splitDialog?.show()
                        }
                    }
                    else
                    {
                        fnShowMessage(
                            getString(R.string.enter_expense),
                            requireContext(),
                            R.drawable.error_bg,
                            logger,
                            LOG_TAG
                        )
                        editExpenseBinding.idENewExpense.isFocusable = true
                        editExpenseBinding.idENewExpense.requestFocus()
                    }
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Show Split Dialog: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Show Split Dialog: ${e.message}")
                }
            }

            editExpenseViewModel.expenseInsertStatus.observe(viewLifecycleOwner) { status ->
                try {
                    when (status) {
                        is ResultState1.success -> {
                            fnShowMessage(
                                getString(status.message),
                                requireContext(),
                                R.drawable.bg_success,
                                logger,
                                LOG_TAG
                            )
                            Global.isBottomSheetSelected = false
                            dismiss()
                        }

                        is ResultState1.fail -> {
                            fnShowMessage(
                                getString(status.message),
                                requireContext(),
                                R.drawable.error_bg,
                                logger,
                                LOG_TAG
                            )

                            if (status.message == R.string.newEx_AllFieldsAreEmpty)
                            {
                                editExpenseBinding.idENewExpense.isFocusable = true
                                editExpenseBinding.idENewExpense.requestFocus()
                            }
                            else if (status.message == R.string.newEx_ExpenseAmountMissing)
                            {
                                editExpenseBinding.idENewExpense.isFocusable = true
                                editExpenseBinding.idENewExpense.requestFocus()
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.logError(LOG_TAG, "Expense Insert Expense: ${e.message}")
                    Log.e("EDIT_EXPENSE", "Expense Insert Expense: ${e.message}")
                }
            }

        }

//    fun fnShowDialog()
//    {
//        try {
//            splitBinding = DataBindingUtil.inflate(layoutInflater,R.layout.split_dialogue,null,false)
//            splitBinding.split = splitViewModel
//            splitBinding.lifecycleOwner = viewLifecycleOwner
//
//            splitDialog = AlertDialog.Builder(requireContext())
//                .setView(splitBinding.root)
//                .setCancelable(false)
//                .create()
//
//            splitDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            splitDialog?.show()
//
//            splitViewModel._totAmtUi.value = "Total: ${editExpenseViewModel.expenseAmt.value}"
//
//            splitViewModel._totAmt.value = editExpenseViewModel.expenseAmt.value.toString()
//            splitViewModel._amtInCash.value=editExpenseViewModel.expenseAmt.value.toString()
//
//            splitBinding.idEAmtInCash.post{
//                splitBinding.idEAmtInCash.selectAll()
//                splitBinding.idEAmtInCash.requestFocus()
//            }
//
//            splitBinding.idEAmtInCash.setOnFocusChangeListener{ view,hasFocus ->
//                if(!hasFocus){
//                    splitBinding.idEAmtInCard.selectAll()
//                    splitBinding.idEAmtInCard.requestFocus()
//                }
//            }
//
//            splitBinding.idEAmtInCard.setOnFocusChangeListener { view, hasFocus ->
//                if (!hasFocus){
//                    splitBinding.idEAmtInUpi.selectAll()
//                    splitBinding.idEAmtInUpi.requestFocus()
//                }
//            }
//
//            splitBinding.idEAmtInUpi.setOnFocusChangeListener { view, hasFocus ->
//                if (!hasFocus){
//                    splitBinding.idBtnOk.requestFocus()
//                }
//            }
//        }
//        catch (e: Exception)
//        {
//            logger.logError(LOG_TAG,"Show Split Screen: ${e.message}")
//            Log.e("EDIT_EXPENSE","Show Split Screen: ${e.message}")
//        }
//    }
    }
}

