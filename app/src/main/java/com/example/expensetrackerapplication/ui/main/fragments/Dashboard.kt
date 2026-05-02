package com.example.expensetrackerapplication.ui.main.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.example.expensetrackerapplication.R
//import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.databinding.DashboardBinding
import com.example.expensetrackerapplication.model.CategoryChartModel
import com.example.expensetrackerapplication.model.PaymentTypeChartModel
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.viewmodel.DashBoardViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Dashboard.newInstance] factory method to
 * create an instance of this fragment.
 */
class Dashboard : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var dashBoardBinding : DashboardBinding

    val dashBoardViewModel : DashBoardViewModel by viewModels()

    val months = listOf("Jan","Feb","March")
    val earnings = listOf(500,400,300)

    private val barChartAnimationDuration = 1000L

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.title= resources.getString(R.string.dashboard_frag)
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

        dashBoardBinding = DataBindingUtil.inflate(inflater, R.layout.dashboard, container, false)
        dashBoardBinding.dashBoard=dashBoardViewModel
        dashBoardBinding.lifecycleOwner=viewLifecycleOwner

        return dashBoardBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        try
        {
            Log.e("SYNC_WORKER","Start Sync Work Now1")
            Global.startSyncWork(requireContext())
            Log.e("SYNC_WORKER","Start Sync Work Now2")
        }
        catch (e: Exception)
        {
            Log.e("SYNC_WORKER","Start Sync Work Now: ${e.message}")
        }

        dashBoardViewModel.onCLickBtnThisMonth()

        dashBoardViewModel.fnGetCateDetailsPerDay()

        dashBoardViewModel._clickBtnThisMonth.observe(viewLifecycleOwner){ isClick ->
            if(isClick){
                fnUpdateBtnUi(dashBoardBinding.idBtnThisMonth,true)
            }
            else {
                fnUpdateBtnUi(dashBoardBinding.idBtnThisMonth,false)
            }
        }

        dashBoardViewModel._clickBtnThisYear.observe(viewLifecycleOwner){ isClick ->
            if(isClick){
                fnUpdateBtnUi(dashBoardBinding.idBtnThisYear,true)
            }
            else {
                fnUpdateBtnUi(dashBoardBinding.idBtnThisYear,false)
            }
        }

        dashBoardViewModel.paymentTypeChartList.observe(viewLifecycleOwner){ list ->
            if(list.isNotEmpty()){
                dashBoardViewModel._isLoading.value=false
                Log.i("PAYMENT TYPE CHART LIST","payment Type Chart List: List Was Observed")
                renderBarChart(list)
            }
            else{
                dashBoardViewModel._isLoading.value=false
            }
        }

        dashBoardViewModel.categoryChartList.observe(viewLifecycleOwner) { list ->
            if(list.isNotEmpty())
            {
                dashBoardViewModel._isLoading.value=false
                Log.i("CATEGORY LIST","Category List: List Was Observed")
                renderPieChart(list)
            }
            else{
                dashBoardViewModel._isLoading.value=false
            }
        }

        dashBoardViewModel.isLoading.observe(viewLifecycleOwner){ isLoading ->
            if(isLoading){
                dashBoardBinding.idLoading.visibility = View.VISIBLE
            }
            else{
                dashBoardBinding.idLoading.visibility = View.GONE
            }
        }
    }

    private fun renderBarChart(list : List<PaymentTypeChartModel>){

        if (list.isEmpty()) return
        val ob = list[0]   // assuming single summary row

        // 🔹 Payment values
        val labels = listOf(resources.getString(R.string.upi),
            resources.getString(R.string.cash),
            resources.getString(R.string.card),
            resources.getString(R.string.other))

        val values = listOf(
            ob.paymentType_UpiAmt,
            ob.paymentType_CashAmt,
            ob.paymentType_CardAmt,
            ob.paymentType_OthersAmt)

        // 🔹 Bar Entries
        val entries = ArrayList<BarEntry>()
        values.forEachIndexed { index, value ->
            entries.add(BarEntry(index.toFloat(), value))
        }

        // 🔹 DataSet
        val dataSet = BarDataSet(entries, resources.getString(R.string.payment_type))
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
        dashBoardBinding.barChart.data = barData

        // 🔹 X-Axis
        val xAxis = dashBoardBinding.barChart.xAxis
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
        val yAxisLeft = dashBoardBinding.barChart.axisLeft
        yAxisLeft.textSize = 15f
//        yAxisLeft.textColor = Color.BLACK
        yAxisLeft.textColor= MaterialColors.getColor(
            requireView(),
            com.google.android.material.R.attr.colorOnPrimary
        )

        dashBoardBinding.barChart.axisRight.isEnabled = false
        dashBoardBinding.barChart.axisLeft.axisMinimum = 0f

        // Legend
        val legend = dashBoardBinding.barChart.legend
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
        dashBoardBinding.barChart.setExtraOffsets(0f,0f,0f,25f)
        dashBoardBinding.barChart.description.isEnabled = false
        dashBoardBinding.barChart.legend.isEnabled = true
        dashBoardBinding.barChart.setFitBars(true)
        dashBoardBinding.barChart.animateY(1000)

        dashBoardBinding.barChart.invalidate()
    }


    private fun renderPieChart(list: List<CategoryChartModel>) {

        // STEP 2.1 – Clear old chart
//        dashBoardBinding.idCategoryPieChart.clear()
//        dashBoardBinding.idCategoryPieChart.invalidate()

        // 1️ BREAK old JS binding

        // STEP 2.2 – Create NEW Pie object every time
        val pie = AnyChart.pie()

        // STEP 2.3 – Prepare fresh data
        val data = mutableListOf<DataEntry>()

        list.forEach {
            Log.i("CATEGORY NAME","Category Name: ${it.categoryName} And Amt: ${it.expenseAmt}")
            data.add(
                ValueDataEntry(
                    it.categoryName,
                    it.expenseAmt
                )
            )
        }

        // STEP 2.4 – Set data to Pie
        pie.data(data)
        pie.title(getString(R.string.category_overview))

        // STEP 2.5 – Set chart
        dashBoardBinding.idCategoryPieChart.setChart(pie)
    }


    fun fnUpdateBtnUi(btnId : MaterialButton,isClick : Boolean){
        if(isClick){
            btnId.setBackgroundColor(
                MaterialColors.getColor(
                    requireView(),
                    com.google.android.material.R.attr.colorOnPrimary
                )
            )
            btnId.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.text_color_white
                )
            )
        }
        else{
            btnId.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.text_color_white
                )
            )
            btnId.setTextColor(
                MaterialColors.getColor(
                    requireView(),
                    com.google.android.material.R.attr.colorOnPrimary
                )
            )
            btnId.strokeColor= ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_grey
                )
            )
            btnId.strokeWidth=2
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Dashboard.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Dashboard().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}