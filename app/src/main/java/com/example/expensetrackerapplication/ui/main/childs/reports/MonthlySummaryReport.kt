package com.example.expensetrackerapplication.ui.main.childs.reports

import android.app.AlertDialog
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.databinding.MonthCalendarBinding
import com.example.expensetrackerapplication.databinding.MonthYearDialogItemBinding
import com.example.expensetrackerapplication.databinding.MonthlyReportBinding
import com.example.expensetrackerapplication.databinding.YearlySummaryReportListItemViewBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.model.ExpenseDetailsPerMonth
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.viewmodel.CalendarMonthViewModel
import com.example.expensetrackerapplication.viewmodel.MonthlySummaryViewModel
import kotlinx.coroutines.launch
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MonthlyReport.newInstance] factory method to
 * create an instance of this fragment.
 */
class MonthlyReport : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }
    private lateinit var monthlySummaryBinding : MonthlyReportBinding
    private val monthlySummaryViewModel : MonthlySummaryViewModel by viewModels{
        appViewModelFactory
    }
    private var selectedYearMonth : YearMonth = YearMonth.now()
    private lateinit var monthlySummaryReportAdapter : MonthlySummaryReportAdapter

    val LOG_TAG = "MONTHLY_SUMMARY_REPORT"

    private lateinit var logger : FileLogger
//        FileLogger(requireContext().applicationContext)

    val calendarMonthViewModel : CalendarMonthViewModel by viewModels {
        appViewModelFactory
    }

    private lateinit var monthCalendarDialog : AlertDialog
    private lateinit var monthBinding : MonthCalendarBinding
    private lateinit var monthAdapter : MonthAdapter

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

        monthlySummaryBinding = DataBindingUtil.inflate(inflater,R.layout.monthly_report, container, false)
        monthlySummaryBinding.monthlySummaryViewModel = monthlySummaryViewModel
        monthlySummaryBinding.lifecycleOwner = viewLifecycleOwner

        // Inflate the layout for this fragment
        return monthlySummaryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            Global.fnPreWarmExcelEngine(logger)
        }
        try
        {
            monthBinding = DataBindingUtil.inflate(layoutInflater,R.layout.month_calendar,null,false)
            monthBinding.calendar = calendarMonthViewModel
            monthBinding.lifecycleOwner = viewLifecycleOwner

            monthCalendarDialog = AlertDialog.Builder(requireContext())
                .setView(monthBinding?.root)
                .setCancelable(false)
                .create()

            monthCalendarDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            monthAdapter = MonthAdapter ({ month ->
                selectedYearMonth = selectedYearMonth.withMonth(month)
                var selectedMonth = if (month < 10) "0$month" else "$month"
                monthlySummaryViewModel._selectedMonth.value = selectedMonth
                monthlySummaryViewModel._selectedYear.value = "${calendarMonthViewModel.selectedYear.value}"
//                monthlySummaryViewModel._selectedMonthAndYear.value = "$selectedMonth/${monthBinding.idTextYear.text}"
                monthlySummaryViewModel._selectedMonthAndYear.value = "$selectedMonth/${calendarMonthViewModel.selectedYear.value}"
                monthCalendarDialog?.dismiss()
                Global.isCalendarSelected = false
            },requireContext().applicationContext)

            monthBinding?.idMonthPicker.apply {
                try
                {
                    this?.layoutManager = GridLayoutManager(requireContext(),3)
                    this?.adapter = monthAdapter
                }
                catch (e: Exception)
                {
                    logger.logError(LOG_TAG,"Set Month Adapter For Month Picker: ${e.message}")
                }
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            monthBinding.idYearPicker.minValue = 2000
            monthBinding.idYearPicker.maxValue = currentYear + 100
            monthBinding.idYearPicker.value = currentYear
            monthBinding.idYearPicker.wrapSelectorWheel = false

            calendarMonthViewModel._selectedYear.value = "${monthBinding.idYearPicker.value}"

        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Month Calendar Dialog Creation: ${e.message}")
        }

        try
        {
            monthlySummaryReportAdapter = MonthlySummaryReportAdapter(requireContext().applicationContext)
            monthlySummaryBinding.idMonthlySummaryReportView.adapter = monthlySummaryReportAdapter
            monthlySummaryBinding.idMonthlySummaryReportView.layoutManager = LinearLayoutManager(requireContext())
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Set Adapter: ${e.message}")
            Log.e("MONTHLY_SUMMARY_REPORT","Set Adapter: ${e.message}")
        }

        monthlySummaryViewModel.isCalendarSelected.observe(viewLifecycleOwner){ isSelected ->
            try
            {
                if(isSelected)
                {
                    Log.i("MONTHLY_SUMMARY_REPORT","Is Calendar Selected: ${monthlySummaryViewModel.isCalendarSelected.value}")
                    if(Global.isCalendarSelected == false)
                    {
                        Global.isCalendarSelected = true
                        monthCalendarDialog?.show()
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Calendar Selected: ${e.message}")
                Log.e("MONTHLY_SUMMARY_REPORT","Calendar Selected: ${e.message}")
            }
        }

        calendarMonthViewModel.isClose.observe(viewLifecycleOwner) { isClose ->
            try
            {
                monthCalendarDialog?.dismiss()
                Global.isCalendarSelected =false
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Close The Month Calendar: ${e.message}")
            }
        }

        calendarMonthViewModel.isYearClicked.observe(viewLifecycleOwner) { isClicked ->
            try
            {
                if (isClicked)
                {
                    monthBinding.idYearPicker.visibility = View.VISIBLE
                    monthBinding.idMonthPicker.visibility = View.GONE

                    monthBinding.idMonthLayout.visibility = View.GONE
                    monthBinding.idYearLayout.visibility = View.VISIBLE
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Is Year Clicked Value Observed: ${e.message}")
            }
        }

        calendarMonthViewModel.isYearSelected.observe(viewLifecycleOwner) { isConfirm ->
            try {
                when (isConfirm) {
                    is ResultState.Success -> {
                        try {
                            calendarMonthViewModel._selectedYear.value =
                                "${monthBinding.idYearPicker.value}"

                            val selectedYear = java.time.YearMonth.of(
                                monthBinding.idYearPicker.value,
                                java.time.YearMonth.now().monthValue
                            )

                            monthAdapter.submitYearMonth(selectedYear)

                            monthBinding.idYearPicker.visibility = View.GONE
                            monthBinding.idMonthPicker.visibility = View.VISIBLE

                            monthBinding.idMonthLayout.visibility = View.VISIBLE
                            monthBinding.idYearLayout.visibility = View.GONE

                        } catch (e: Exception) {
                            logger.logError(
                                LOG_TAG,
                                "Year Selected From Year Calendar: ${e.message}"
                            )
                        }
                    }

                    is ResultState.fail -> {
                        try {
                            monthBinding.idYearPicker.visibility = View.GONE
                            monthBinding.idMonthPicker.visibility = View.VISIBLE

                            monthBinding.idMonthLayout.visibility = View.VISIBLE
                            monthBinding.idYearLayout.visibility = View.GONE

                        } catch (e: Exception) {
                            logger.logError(
                                LOG_TAG,
                                "Close The Year Calendar: ${e.message}"
                            )
                        }
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG, "Is Year Selected: ${e.message}")
            }
        }

        monthlySummaryViewModel.isClosed.observe(viewLifecycleOwner){ status ->
            try
            {
                if(status)
                {
                    findNavController().navigate(R.id.action_monthly_summary_report_to_report_menu)
                    monthlySummaryViewModel.resetCloseState()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Close The Monthly Summary Report: ${e.message}")
                Log.e("MONTHLY_SUMMARY_REPORT","Close The Monthly Summary Report: ${e.message}")
            }
        }

        monthlySummaryViewModel.selectedMonthAndYear.observe(viewLifecycleOwner){ monthAndYear ->
            try
            {
                monthlySummaryViewModel.fnGetExpenseDetailsPerMonth(
                    monthlySummaryViewModel.selectedMonth.value ?:"",
                    monthlySummaryViewModel.selectedYear.value ?:""
                )
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Month And Year Observed: ${e.message}")
                Log.e("MONTHLY_SUMMARY_REPORT","Month And Year Observed: ${e.message}")
            }
        }

        monthlySummaryViewModel.monthlySummaryReportList.observe(viewLifecycleOwner){ list ->
            try
            {
                if(list.isNotEmpty())
                {
                    monthlySummaryReportAdapter.fnSubmitList(list)
                    monthlySummaryViewModel._isExportLoading.value = false
                }
                else
                {
                    monthlySummaryViewModel._isExportLoading.value = false
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Monthly Summary Report List Observed: ${e.message}")
                Log.e("MONTHLY_SUMMARY_REPORT","Monthly Summary Report List Observed: ${e.message}")
            }
        }

        monthlySummaryViewModel.exportStatus.observe(viewLifecycleOwner){ status ->
            try
            {
                when(status) {
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
                        fnShowMessage(getString(status.message), requireContext(),
                            R.drawable.error_bg,
                            logger,
                            LOG_TAG)
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Export Status: ${e.message}")
                Log.e("MONTHLY_SUMMARY_REPORT","Export Status: ${e.message}")
            }
        }

        monthlySummaryViewModel.isExportLoading.observe(viewLifecycleOwner){ isLoading ->
            try
            {
                if(isLoading)
                {
                    monthlySummaryBinding.isExportLoading.visibility=View.VISIBLE
                }
                else
                {
                    monthlySummaryBinding.isExportLoading.visibility=View.GONE
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"ProgressBar Loading: ${e.message}")
                Log.e("MONTHLY_SUMMARY_REPORT","ProgressBar Loading: ${e.message}")
            }
        }

    }


//    fun showMonthPicker()
//    {
//        try {
//            if(Global.isCalendarSelected == false)
//            {
//                Global.isCalendarSelected = true
//
//                monthBinding = DataBindingUtil.inflate(layoutInflater,R.layout.month_calendar,null,false)
//                monthBinding?.calendar = calendarMonthViewModel
//                monthBinding?.lifecycleOwner = viewLifecycleOwner
//
//                monthCalendarDialog = AlertDialog.Builder(requireContext())
//                                    .setView(monthBinding?.root)
//                                    .setCancelable(false)
//                                    .create()
//
//                monthCalendarDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                monthCalendarDialog?.show()
//
//                val monthAdapter = MonthAdapter ({ month ->
//                    selectedYearMonth = selectedYearMonth.withMonth(month)
//                    var selectedMonth = if (month < 10) "0$month" else "$month"
//                    monthlySummaryViewModel._selectedMonth.value = selectedMonth
//                    monthlySummaryViewModel._selectedYear.value = "${monthBinding?.idTextYear?.text}"
//                    monthlySummaryViewModel._selectedMonthAndYear.value = "$selectedMonth/${monthBinding.idTextYear.text}"
//                    monthCalendarDialog?.dismiss()
//                    Global.isCalendarSelected = false
//                },requireContext().applicationContext)
//
//                monthBinding?.idMonthPicker.apply {
//                    try
//                    {
//                        this?.layoutManager = GridLayoutManager(requireContext(),3)
//                        this?.adapter = monthAdapter
//                    }
//                    catch (e: Exception)
//                    {
//                        logger.logError(LOG_TAG,"Set Month Adapter For Month Picker: ${e.message}")
//                    }
//                }
//
////                calendarMonthViewModel.isClose.observe(viewLifecycleOwner) { isClose ->
////                    try
////                    {
////                        monthCalendarDialog?.dismiss()
////                        Global.isCalendarSelected =false
////                        monthlySummaryViewModel._isCalendarSelected.value = false
////                        calendarMonthViewModel._isClose.value = false
////                    }
////                    catch (e: Exception)
////                    {
////                        logger.logError(LOG_TAG,"Close The Month Calendar: ${e.message}")
////                    }
////                }
//
//                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
//
//                monthBinding.idYearPicker.minValue = 2000
//                monthBinding.idYearPicker.maxValue = currentYear + 100
//                monthBinding.idYearPicker.value = currentYear
//                monthBinding.idYearPicker.wrapSelectorWheel = false
//
//                calendarMonthViewModel._selectedYear.value = "${monthBinding.idYearPicker.value}"
//
////                monthBinding.idTextYear.setOnClickListener {
////                calendarMonthViewModel.isYearClicked.observe(viewLifecycleOwner){ isClicked ->
////                    try
////                    {
////                        if(isClicked)
////                        {
////                            monthBinding.idYearPicker.visibility = View.VISIBLE
////                            monthBinding.idMonthPicker.visibility = View.GONE
////
////                            monthBinding.idMonthLayout.visibility = View.GONE
////                            monthBinding.idYearLayout.visibility = View.VISIBLE
////
////                            calendarMonthViewModel.isYearSelected.observe(viewLifecycleOwner){ isConfirm ->
////                                  try
////                                  {
////                                      when(isConfirm)
////                                      {
////                                          is ResultState.Success -> {
////                                              try
////                                              {
////                                                  calendarMonthViewModel._selectedYear.value = "${monthBinding.idYearPicker.value}"
////
////                                                  val selectedYear = java.time.YearMonth.of(
////                                                      monthBinding.idYearPicker.value,
////                                                      java.time.YearMonth.now().monthValue
////                                                  )
////
////                                                  monthAdapter.submitYearMonth(selectedYear)
////
////                                                  monthBinding.idYearPicker.visibility = View.GONE
////                                                  monthBinding.idMonthPicker.visibility = View.VISIBLE
////
////                                                  monthBinding.idMonthLayout.visibility = View.VISIBLE
////                                                  monthBinding.idYearLayout.visibility = View.GONE
////
////                                                  calendarMonthViewModel._isYearSelected.value =ResultState.fail
////
////                                              }
////                                              catch (e: Exception)
////                                              {
////                                                  logger.logError(LOG_TAG,"Year Selected From Year Calendar: ${e.message}")
////                                              }
////                                          }
////
////                                          is ResultState.fail ->{
////                                              try
////                                              {
////                                                  monthBinding.idYearPicker.visibility = View.GONE
////                                                  monthBinding.idMonthPicker.visibility = View.VISIBLE
////
////                                                  monthBinding.idMonthLayout.visibility = View.VISIBLE
////                                                  monthBinding.idYearLayout.visibility = View.GONE
////
////                                                  calendarMonthViewModel._isYearSelected.value =ResultState.Success
////
////                                              }
////                                              catch (e: Exception)
////                                              {
////                                                  logger.logError(LOG_TAG,"Close The Year Calendar: ${e.message}")
////                                              }
////                                          }
////                                      }
////                                  }
////                                  catch (e: Exception)
////                                  {
////                                      logger.logError(LOG_TAG,"Is Year Selected: ${e.message}")
////                                  }
////                            }
////
//////                            monthBinding.idOkYear.setOnClickListener {
//////
//////                            }
//////
//////                            monthBinding.idCancelYear.setOnClickListener {
//////
//////                            }
////                        }
////                    }
////                    catch (e: Exception)
////                    {
////                        logger.logError(LOG_TAG,"Is Year Clicked Value Observed: ${e.message}")
////                    }
////                }
//            }
//        }
//        catch (e: Exception)
//        {
//            logger.logError(LOG_TAG,"Show Monthly Calendar: ${e.message}")
//            Log.e("MONTHLY_SUMMARY_REPORT","Show Monthly Calendar: ${e.message}")
//        }
//    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MonthlyReport.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MonthlyReport().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

class MonthlySummaryReportAdapter(applicationContext: Context) : RecyclerView.Adapter<MonthlySummaryReportAdapter.ListViewHolder>()
{
    var monthlySummaryList : List<ExpenseDetailsPerMonth> = emptyList()

    val logger = FileLogger(applicationContext)

    val LOG_TAG ="MONTHLY_SUMMARY_REPORT_ADAPTER"
    fun fnSubmitList(list  : List<ExpenseDetailsPerMonth>)
    {
        try
        {
            monthlySummaryList = list
            notifyDataSetChanged()
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Submit List: ${e.message}")
            Log.e("MONTHLY_SUMMARY_REPORT","Submit List: ${e.message}")
        }
    }

    inner class ListViewHolder(val binding : YearlySummaryReportListItemViewBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(ob : ExpenseDetailsPerMonth)
        {
            try {
                binding.expense = ob
                binding.executePendingBindings()
            }
            catch (e: Exception){
                logger.logError(LOG_TAG,"Bind: ${e.message}")
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MonthlySummaryReportAdapter.ListViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        var view = DataBindingUtil.inflate<YearlySummaryReportListItemViewBinding>(inflater,R.layout.yearly_summary_report_list_item_view,parent,false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MonthlySummaryReportAdapter.ListViewHolder,
        position: Int
    ) {
       holder.bind(monthlySummaryList[position])
    }

    override fun getItemCount(): Int = monthlySummaryList.size

}

class MonthAdapter(
    private val onClick:(Int)-> Unit,
    applicationContext: Context
) : RecyclerView.Adapter<MonthAdapter.VH>() {

    private var selectedYearMonth: YearMonth = YearMonth.now()

    private val months = Month.values()

    val logger = FileLogger(applicationContext)

    val LOG_TAG = "MONTH_ADAPTER"
    fun submitYearMonth(yearMonth: YearMonth)
    {
        try {
            selectedYearMonth = yearMonth
            notifyDataSetChanged()
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Submit Year And Month: ${e.message}")
            Log.e("MONTHLY_SUMMARY_REPORT","Submit Year And Month: ${e.message}")
        }
    }

    inner class VH(val binding: MonthYearDialogItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(month: Month)
        {
            try {
                binding.txtMonth.text = month.getDisplayName(TextStyle.SHORT,Locale.getDefault())

                val isSelected =
                    month.value == selectedYearMonth.monthValue

                binding.root.isSelected = isSelected

                binding.root.setOnClickListener {
                    onClick(month.value)
                }
            }
            catch (e: Exception){
                logger.logError(LOG_TAG,"bind: ${e.message}")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = MonthYearDialogItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun getItemCount() = months.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(months[position])
    }
}

