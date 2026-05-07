package com.example.expensetrackerapplication.ui.main.fragments.reports

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.databinding.PaymentTypeReportBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.model.PaymentTypeChartModel
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.viewmodel.PaymentTypeReportViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PaymentTypeReport.newInstance] factory method to
 * create an instance of this fragment.
 */
class PaymentTypeReport : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }
    private lateinit var paymentTypeReportBinding: PaymentTypeReportBinding

    private val paymentTypeReportViewModel : PaymentTypeReportViewModel by viewModels{
        appViewModelFactory
    }

    private lateinit var logger : FileLogger
//        FileLogger(requireContext().applicationContext)
    val LOG_TAG = "PAYMENT_TYPE_REPORT"

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

        paymentTypeReportBinding = DataBindingUtil.inflate(inflater,
            R.layout.payment_type_report, container, false)
        paymentTypeReportBinding.paymentTypeReportViewModel = paymentTypeReportViewModel
        paymentTypeReportBinding.lifecycleOwner = viewLifecycleOwner

        // Inflate the layout for this fragment
        return paymentTypeReportBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentTypeReportViewModel.fnPreWarmExcelEngine()

        paymentTypeReportBinding.idBtnCalendar.setOnClickListener {
            try
            {
                if(Global.isCalendarSelected==false)
                {
                    Global.isCalendarSelected=true
                    val calendar = Calendar.getInstance()
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    val month = calendar.get(Calendar.MONTH)
                    val year = calendar.get(Calendar.YEAR)

                    val datePickerDialog = DatePickerDialog(requireContext(),
                        {_,y,m,d, ->
                            calendar.set(y,m,d)
                            val sdf1 = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                            val sdf = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            val date = sdf.format(calendar.time)
                            val dateUi = sdf1.format(calendar.time)

                            paymentTypeReportViewModel._selectedDate.value=date
                            paymentTypeReportViewModel._selectedDateUi.value=dateUi
                            Global.isCalendarSelected=false
                        },year,month,day
                    )

                    datePickerDialog.setCancelable(false)
                    datePickerDialog.setCanceledOnTouchOutside(false)
                    datePickerDialog.setOnCancelListener {
                        Global.isCalendarSelected=false
                        datePickerDialog.dismiss()
                    }
                    datePickerDialog.show()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Show Calendar: ${e.message}")
                Log.e(LOG_TAG,"Show Calendar: ${e.message}")
            }
        }
        
        paymentTypeReportViewModel.selectedDate.observe(viewLifecycleOwner){ date->
            try
            {
                paymentTypeReportViewModel.fnGetPaymentTypeList(date)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Selected Date Observed: ${e.message}")
                Log.e(LOG_TAG,"Selected Date Observed: ${e.message}")
            }
        }

        paymentTypeReportViewModel.isClosed.observe(viewLifecycleOwner){ status ->
            try
            {
                if(status)
                {
                    findNavController().navigate(R.id.action_payment_type_report_to_report_menu)
                    paymentTypeReportViewModel.resetCloseState()
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Close The Payment Type Report: ${e.message}")
                Log.e(LOG_TAG,"Close The Payment Type Report: ${e.message}")
            }
        }
        
        paymentTypeReportViewModel.paymentTypeList.observe(viewLifecycleOwner){ list ->
            try {
                if(list.isNotEmpty())
                {
                    paymentTypeReportBinding.idNoReportsText.visibility=View.GONE
                    paymentTypeReportBinding.idScrollView.visibility = View.VISIBLE
                    paymentTypeReportViewModel._isExportLoading.value = false
                    fnCreateChart(list)
                }
                else
                {
                    paymentTypeReportBinding.idNoReportsText.visibility=View.VISIBLE
                    paymentTypeReportBinding.idScrollView.visibility = View.GONE
                    paymentTypeReportViewModel._isExportLoading.value = false
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Payment Type List Observed: ${e.message}")
                Log.e(LOG_TAG,"Payment Type List Observed: ${e.message}")
            }
        }

        paymentTypeReportViewModel.isExportLoading.observe(viewLifecycleOwner){ status ->
            try
            {
                if(status)
                {
                    paymentTypeReportBinding.isExportLoading.visibility = View.VISIBLE
                }
                else
                {
                    paymentTypeReportBinding.isExportLoading.visibility = View.GONE
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"ProgressBar Loading: ${e.message}")
                Log.e(LOG_TAG,"ProgressBar Loading: ${e.message}")
            }
        }

        paymentTypeReportViewModel.exportStatus.observe(viewLifecycleOwner){ status ->
            try
            {
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
                logger.logError(LOG_TAG,"Export Status: ${e.message}")
                Log.e(LOG_TAG,"Export Status: ${e.message}")
            }
        }

    }
    
    fun fnCreateChart(list : List<PaymentTypeChartModel>)
    {
        try {
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
            paymentTypeReportBinding.idChart.data = barData

            // 🔹 X-Axis
            val xAxis = paymentTypeReportBinding.idChart.xAxis
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
            val yAxisLeft = paymentTypeReportBinding.idChart.axisLeft
            yAxisLeft.textSize = 15f
//        yAxisLeft.textColor = Color.BLACK
            yAxisLeft.textColor= MaterialColors.getColor(
                requireView(),
                com.google.android.material.R.attr.colorOnPrimary
            )

            paymentTypeReportBinding.idChart.axisRight.isEnabled = false
            paymentTypeReportBinding.idChart.axisLeft.axisMinimum = 0f

            // Legend
            val legend = paymentTypeReportBinding.idChart.legend
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
            paymentTypeReportBinding.idChart.setExtraOffsets(0f,0f,0f,25f)
            paymentTypeReportBinding.idChart.description.isEnabled = false
            paymentTypeReportBinding.idChart.legend.isEnabled = true
            paymentTypeReportBinding.idChart.setFitBars(true)
            paymentTypeReportBinding.idChart.animateY(1000)

            paymentTypeReportBinding.idChart.invalidate()
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Chart Creation: ${e.message}")
            Log.e(LOG_TAG,"Chart Creation: ${e.message}")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PaymentTypeReport.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PaymentTypeReport().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
