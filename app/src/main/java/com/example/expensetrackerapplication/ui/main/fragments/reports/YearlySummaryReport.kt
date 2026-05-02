package com.example.expensetrackerapplication.ui.main.fragments.reports

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.databinding.AlertSheetMonthBinding
import com.example.expensetrackerapplication.databinding.AlertSheetYearBinding
import com.example.expensetrackerapplication.databinding.BottomsheetMonthPickerBinding
import com.example.expensetrackerapplication.databinding.MonthYearDialogItemBinding
import com.example.expensetrackerapplication.databinding.NewExpenseBinding
import com.example.expensetrackerapplication.databinding.YearlySummaryReportBinding
import com.example.expensetrackerapplication.databinding.YearlySummaryReportListItemViewBinding
import com.example.expensetrackerapplication.model.ExpenseDetailsPerMonth
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.reusefiles.fnShowMessage
import com.example.expensetrackerapplication.viewmodel.YearlySummaryReportViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
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

    private lateinit var yearlySummaryReportBinding : YearlySummaryReportBinding

    private val yearlySummaryReportViewModel : YearlySummaryReportViewModel by viewModels()

    private lateinit var adapter : YearlySummaryAdapter

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
        yearlySummaryReportBinding = DataBindingUtil.inflate(inflater,R.layout.yearly_summary_report, container, false)
        yearlySummaryReportBinding.yearlySummaryViewModel = yearlySummaryReportViewModel
        yearlySummaryReportBinding.lifecycleOwner = viewLifecycleOwner
        // Inflate the layout for this fragment
        return yearlySummaryReportBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yearlySummaryReportViewModel.fnPreWarmExcelEngine()

        adapter = YearlySummaryAdapter()
        yearlySummaryReportBinding.idYearlySummaryList.adapter = adapter
        yearlySummaryReportBinding.idYearlySummaryList.layoutManager = LinearLayoutManager(requireContext())

        yearlySummaryReportViewModel._monthArray.value = resources.getStringArray(R.array.months)

        yearlySummaryReportViewModel.closeReport.observe(viewLifecycleOwner) { status ->
            if (status) {
                findNavController().navigate(R.id.action_yearly_report_to_report_menu)
            }
        }

        yearlySummaryReportBinding.idCalendarButton.setOnClickListener {
            showYearPicker()
        }

        yearlySummaryReportViewModel.selectedYear.observe(viewLifecycleOwner){ year ->
            yearlySummaryReportViewModel.fnGetExpenseDetailsPerYear(year)
        }
        
        yearlySummaryReportViewModel.yearSummaryList.observe(viewLifecycleOwner){ list ->
            if(list.isNotEmpty()){
                fnCreateChart(list)
                adapter.fnSubmitList(list)
            }
        }

        yearlySummaryReportViewModel.exportStatus.observe(viewLifecycleOwner){ status ->
            if(status){
                fnShowMessage("Report Successfully Exported",requireContext(),R.drawable.bg_success)
            }
            else{
                fnShowMessage("Report Export Failed",requireContext(),R.drawable.error_bg)
            }
        }

        yearlySummaryReportViewModel.isExportLoading.observe(viewLifecycleOwner){ isLoading ->
            if(isLoading){
                yearlySummaryReportBinding.isExportLoading.visibility=View.VISIBLE
            }
            else{
                yearlySummaryReportBinding.isExportLoading.visibility=View.GONE
            }
        }
    }

    fun fnCreateChart(list : List<ExpenseDetailsPerMonth>)
    {

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
    
    @SuppressLint("SuspiciousIndentation")
    private fun showYearPicker() {
        if(Global.isCalendarSelected == false){
            Global.isCalendarSelected = true
            val monthBinding = AlertSheetYearBinding.inflate(layoutInflater)

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

            monthBinding.idTextYear.text="${monthBinding.idYearPicker.value}"

//            monthBinding.idTextYear.setOnClickListener {

                monthBinding.idOkYear.setOnClickListener {
                    monthBinding.idTextYear.text="${monthBinding.idYearPicker.value}"

//                    val selectedYear = java.time.YearMonth.of(
//                        monthBinding.idYearPicker.value,
//                        java.time.YearMonth.now().monthValue
//                    )

                    Global.isCalendarSelected = false

                    yearlySummaryReportViewModel._selectedYear.value = monthBinding.idYearPicker.value.toString()

                    dialog.dismiss()
                }

                monthBinding.idCancelYear.setOnClickListener {
                    Global.isCalendarSelected = false
                    dialog.dismiss()
                }
//            }
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


class YearlySummaryAdapter : RecyclerView.Adapter<YearlySummaryAdapter.ListViewHolder>(){

    var expenseList : List<ExpenseDetailsPerMonth> = emptyList<ExpenseDetailsPerMonth>()

    fun fnSubmitList(list : List<ExpenseDetailsPerMonth>){
        expenseList = list
        notifyDataSetChanged()
    }

    inner class ListViewHolder(val binding : YearlySummaryReportListItemViewBinding): RecyclerView.ViewHolder
        (binding.root){

        fun bind(ob : ExpenseDetailsPerMonth)
        {
            binding.expense = ob
            binding.executePendingBindings()
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