package com.example.expensetrackerapplication.ui.main.childs.reports

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.databinding.YearCalendarBinding
import com.example.expensetrackerapplication.databinding.YearlySummaryReportBinding
import com.example.expensetrackerapplication.databinding.YearlySummaryReportListItemViewBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.model.ExpenseDetailsPerMonth
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.viewmodel.CalendarYearViewModel
import com.example.expensetrackerapplication.viewmodel.YearlySummaryReportViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.color.MaterialColors
import kotlin.collections.forEach

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [YearlySummaryReport.newInstance] factory method to
 * create an instance of this fragment.
 */
class YearlySummaryReport : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }
    private lateinit var yearlySummaryReportBinding : YearlySummaryReportBinding

    private val yearlySummaryReportViewModel : YearlySummaryReportViewModel by viewModels{
        appViewModelFactory
    }

    private lateinit var adapter : YearlySummaryAdapter

    private lateinit var logger : FileLogger
//        FileLogger(requireContext().applicationContext)

    val LOG_TAG="YEARLY_SUMMARY_REPORT"

    val calendarYearViewModel : CalendarYearViewModel by viewModels {
        appViewModelFactory
    }

    private lateinit var yearCalendarBinding: YearCalendarBinding

    private lateinit var yearCalendarDialog : AlertDialog

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

        yearlySummaryReportBinding = DataBindingUtil.inflate(inflater,R.layout.yearly_summary_report, container, false)
        yearlySummaryReportBinding.yearlySummaryViewModel = yearlySummaryReportViewModel
        yearlySummaryReportBinding.lifecycleOwner = viewLifecycleOwner
        // Inflate the layout for this fragment
        return yearlySummaryReportBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yearlySummaryReportViewModel.fnPreWarmExcelEngine()

        try
        {
            yearCalendarBinding = YearCalendarBinding.inflate(layoutInflater)
            yearCalendarBinding.calendar = calendarYearViewModel
            yearCalendarBinding.lifecycleOwner = viewLifecycleOwner

            yearCalendarDialog = AlertDialog.Builder(requireContext())
                                            .setView(yearCalendarBinding.root)
                                            .setCancelable(false)
                                            .create()
            yearCalendarDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val currentYear = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.YEAR)

            yearCalendarBinding.idYearPicker.minValue = 2000
            yearCalendarBinding.idYearPicker.maxValue = currentYear + 100
            yearCalendarBinding.idYearPicker.value = currentYear
            yearCalendarBinding.idYearPicker.wrapSelectorWheel = false

            calendarYearViewModel._selectedYear.value = "${yearCalendarBinding.idYearPicker.value}"
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Year Calendar Dialog Creation: ${e.message}")
        }

        try{
            adapter = YearlySummaryAdapter(requireContext().applicationContext)
            yearlySummaryReportBinding.idYearlySummaryList.adapter = adapter
            yearlySummaryReportBinding.idYearlySummaryList.layoutManager = LinearLayoutManager(requireContext())
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Set Adapter: ${e.message}")
            Log.e(LOG_TAG,"Set Adapter: ${e.message}")
        }

        try {
            yearlySummaryReportViewModel._monthArray.value = resources.getStringArray(R.array.months)
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Assign Month Array: ${e.message}")
            Log.e(LOG_TAG,"Assign Month Array: ${e.message}")
        }

        yearlySummaryReportViewModel.isClosed.observe(viewLifecycleOwner) { status ->
            try {
                if(status)
                {
                    findNavController().navigate(R.id.action_yearly_report_to_report_menu)
                    yearlySummaryReportViewModel.resetCloseState()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Close The Monthly Summary Report: ${e.message}")
                Log.e(LOG_TAG,"Close The Monthly Summary Report: ${e.message}")
            }
        }

        yearlySummaryReportViewModel.isCalendarSelected.observe(viewLifecycleOwner){ isSelected ->
            try {
                if(isSelected){
                    if(Global.isCalendarSelected == false)
                    {
                        Global.isCalendarSelected = true

                        yearCalendarDialog.show()
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Calendar Selected: ${e.message}")
                Log.e(LOG_TAG,"Calendar Selected: ${e.message}")
            }
        }
        calendarYearViewModel.isConfirm.observe(viewLifecycleOwner){ isConfirm ->
            try
            {
                if(isConfirm)
                {
                    calendarYearViewModel._selectedYear.value = "${yearCalendarBinding.idYearPicker.value}"

                    Global.isCalendarSelected = false

                    yearlySummaryReportViewModel._selectedYear.value =
                        yearCalendarBinding.idYearPicker.value.toString()

                    yearCalendarDialog.dismiss()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Year Selected From YearCalendar: ${e.message}")
            }
        }

        calendarYearViewModel.isClose.observe(viewLifecycleOwner){ isClose ->
            try
            {
                if(isClose)
                {
                    Global.isCalendarSelected = false
                    yearCalendarDialog.dismiss()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Close The Year Calendar: ${e.message}")
            }
        }

        yearlySummaryReportViewModel.selectedYear.observe(viewLifecycleOwner){ year ->
            try {
                yearlySummaryReportViewModel.fnGetExpenseDetailsPerYear(year)
            }
            catch (e: Exception){
                logger.logError(LOG_TAG,"Selected Year: ${e.message}")
                Log.e(LOG_TAG,"Selected Year: ${e.message}")
            }
        }
        
        yearlySummaryReportViewModel.yearSummaryList.observe(viewLifecycleOwner){ list ->
            try {
                if(list.isNotEmpty())
                {
                    fnCreateChart(list)
                    adapter.fnSubmitList(list)
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Summary List Observed: ${e.message}")
                Log.e(LOG_TAG,"Summary List Observed: ${e.message}")
            }
        }

        yearlySummaryReportViewModel.exportStatus.observe(viewLifecycleOwner){ status ->
            try {
                when(status)
                {
                    is ResultState1.success ->{
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.bg_success,logger,LOG_TAG)
                    }
                    is ResultState1.fail ->{
                        fnShowMessage(getString(status.message),requireContext(),R.drawable.error_bg,logger,LOG_TAG)
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Export Status: ${e.message}")
                Log.e(LOG_TAG,"Export Status: ${e.message}")
            }
        }

        yearlySummaryReportViewModel.isExportLoading.observe(viewLifecycleOwner){ isLoading ->
            try {
                if(isLoading)
                {
                    yearlySummaryReportBinding.isExportLoading.visibility=View.VISIBLE
                }
                else
                {
                    yearlySummaryReportBinding.isExportLoading.visibility=View.GONE
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Display ProgressBar: ${e.message}")
                Log.e(LOG_TAG,"Display ProgressBar: ${e.message}")
            }
        }
    }

    fun fnCreateChart(list : List<ExpenseDetailsPerMonth>)
    {
        try {
            if (list.isEmpty()) return
            val ob = list[0]   // assuming single summary row

            // 🔹 Payment monthlyValues
            val labels = listOf("Jan","Feb","March",
                "April","May","June","July","Aug","Sep","Oct","Nov","Dec")
//        val labels = resources.getStringArray(R.array.months)

            val monthlyValues = MutableList(12) {0.0}
            var indexValue = 0

            list.forEach { obj ->
                var index = indexValue
                monthlyValues[index] = obj.expenseSummaryAmt.toDouble()
                indexValue++
            }

            // 🔹 Bar Entries
            val entries = ArrayList<BarEntry>()
            monthlyValues.forEachIndexed { index, value ->
                entries.add(BarEntry(index.toFloat(), value.toFloat()))
            }

            // 🔹 DataSet
            val dataSet = BarDataSet(entries, resources.getString(R.string.yearly_overview))
            dataSet.valueTextSize = 15f  //12
            dataSet.setDrawValues(true)
            dataSet.color = MaterialColors.getColor(
                requireView(),
                com.google.android.material.R.attr.colorOnPrimary
            )

            // 🔹 BarData
            val barData = BarData(dataSet)
            barData.barWidth = 0.6f

            // 🔹 Assign data
            yearlySummaryReportBinding.idBarChart.data = barData

            // 🔹 X-Axis
            val xAxis = yearlySummaryReportBinding.idBarChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.textSize= 20f
            xAxis.yOffset=12f
//        xAxis.textColor= ContextCompat.getColor(
//            requireContext(),
//            R.color.text_color_black
//        )
            xAxis.textColor= MaterialColors.getColor(
                requireView(),
                com.google.android.material.R.attr.colorOnPrimary
            )
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            // 🔹 Y-Axis
            val yAxisLeft = yearlySummaryReportBinding.idBarChart.axisLeft
            yAxisLeft.textSize = 15f
//        yAxisLeft.textColor = Color.BLACK
            yAxisLeft.textColor= MaterialColors.getColor(
                requireView(),
                com.google.android.material.R.attr.colorOnPrimary
            )

            yearlySummaryReportBinding.idBarChart.axisRight.isEnabled = false
            yearlySummaryReportBinding.idBarChart.axisLeft.axisMinimum = 0f

            // Legend
            val legend = yearlySummaryReportBinding.idBarChart.legend
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.yOffset = 10f
            legend.textSize = 15f
            legend.textColor = MaterialColors.getColor(
                requireView(),
                com.google.android.material.R.attr.colorOnPrimary
            )

            // 🔹 Chart Settings
            yearlySummaryReportBinding.idBarChart.setExtraOffsets(0f,0f,0f,25f)
            yearlySummaryReportBinding.idBarChart.description.isEnabled = false
            yearlySummaryReportBinding.idBarChart.legend.isEnabled = true
            yearlySummaryReportBinding.idBarChart.setFitBars(true)
            yearlySummaryReportBinding.idBarChart.animateY(1000)

            yearlySummaryReportBinding.idBarChart.invalidate()
        }
        catch (e: Exception){
            logger.logError(LOG_TAG,"Chart Creation: ${e.message}")
            Log.e(LOG_TAG,"Chart Creation: ${e.message}")
        }
    }
    
    @SuppressLint("SuspiciousIndentation")
    private fun showYearPicker()
    {
        try {
            if(Global.isCalendarSelected == false)
            {
                Global.isCalendarSelected = true

                var monthBinding = YearCalendarBinding.inflate(layoutInflater)
                monthBinding.calendar = calendarYearViewModel
                monthBinding.lifecycleOwner = viewLifecycleOwner

                val monthAlert = AlertDialog.Builder(requireContext())
                monthAlert.setView(monthBinding.root)
                monthAlert.setCancelable(false)

                val dialog = monthAlert.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()

                val currentYear = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.YEAR)

                monthBinding.idYearPicker.minValue = 2000
                monthBinding.idYearPicker.maxValue = currentYear + 100
                monthBinding.idYearPicker.value = currentYear
                monthBinding.idYearPicker.wrapSelectorWheel = false

                calendarYearViewModel._selectedYear.value = "${monthBinding.idYearPicker.value}"
//                monthBinding.idTextYear.text="${monthBinding.idYearPicker.value}"

//                monthBinding.idOkYear.setOnClickListener {
                    calendarYearViewModel.isConfirm.observe(viewLifecycleOwner){ isConfirm ->
                        try
                        {
                            if(isConfirm) {
//                                monthBinding.idTextYear.text =
//                                    "${monthBinding.idYearPicker.value}"
                                calendarYearViewModel._selectedYear.value = "${monthBinding.idYearPicker.value}"

                                Global.isCalendarSelected = false

                                yearlySummaryReportViewModel._selectedYear.value =
                                    monthBinding.idYearPicker.value.toString()

                                dialog.dismiss()
                            }
                        }
                        catch (e: Exception)
                        {
                            logger.logError(LOG_TAG,"Year Selected From YearCalendar: ${e.message}")
                        }
                    }
//                }

//                monthBinding.idCancelYear.setOnClickListener {
                    calendarYearViewModel.isClose.observe(viewLifecycleOwner){ isClose ->
                        try
                        {
                            if(isClose)
                            {
                                Global.isCalendarSelected = false
                                dialog.dismiss()
                            }
                        }
                        catch (e: Exception)
                        {
                            logger.logError(LOG_TAG,"Close The Year Calendar: ${e.message}")
                        }
                    }
//                }
            }
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Display Year Calendar Screen: ${e.message}")
            Log.e(LOG_TAG,"Display Year Selection Screen: ${e.message}")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment YearlySummaryReport.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            YearlySummaryReport().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


class YearlySummaryAdapter(applicationContext: Context) : RecyclerView.Adapter<YearlySummaryAdapter.ListViewHolder>(){

    var expenseList : List<ExpenseDetailsPerMonth> = emptyList<ExpenseDetailsPerMonth>()
    val LOG_TAG = "YEARLY_SUMMARY_ADAPTER"
    val logger = FileLogger(applicationContext)
    fun fnSubmitList(list : List<ExpenseDetailsPerMonth>)
    {
        try {
            expenseList = list
            notifyDataSetChanged()
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Submit List: ${e.message}")
            Log.e(LOG_TAG,"Submit List: ${e.message}")
        }
    }

    inner class ListViewHolder(val binding : YearlySummaryReportListItemViewBinding): RecyclerView.ViewHolder
        (binding.root){

        fun bind(ob : ExpenseDetailsPerMonth)
        {
            try {
                binding.expense = ob
                binding.executePendingBindings()
            }
            catch (e: Exception){
                logger.logError(LOG_TAG,"bind: ${e.message}")
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): YearlySummaryAdapter.ListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        var view = DataBindingUtil.inflate<YearlySummaryReportListItemViewBinding>(inflater,R.layout.yearly_summary_report_list_item_view,parent,false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: YearlySummaryAdapter.ListViewHolder,
        position: Int
    ) {
       holder.bind(expenseList[position])
    }

    override fun getItemCount(): Int = expenseList.size

}