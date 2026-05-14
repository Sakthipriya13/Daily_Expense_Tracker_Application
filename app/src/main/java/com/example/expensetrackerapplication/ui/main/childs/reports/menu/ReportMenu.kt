package com.example.expensetrackerapplication.ui.main.childs.reports.menu

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
import com.example.expensetrackerapplication.databinding.ReportMenuBinding
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.utils.Report_Menu
import com.example.expensetrackerapplication.viewmodel.ReportMenuViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReportMenu.newInstance] factory method to
 * create an instance of this fragment.
 */
open class ReportMenu : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }
    private lateinit var reportMenuBinding: ReportMenuBinding
    val reportMenuViewModel : ReportMenuViewModel by viewModels{
        appViewModelFactory
    }

    private lateinit var logger : FileLogger
//        FileLogger(requireContext().applicationContext)

    val LOG_TAG = "REPORT_MENU"

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

        reportMenuBinding = DataBindingUtil.inflate(inflater,R.layout.report_menu, container, false)
        reportMenuBinding.reportMenuViewModel=reportMenuViewModel
        reportMenuBinding.lifecycleOwner=viewLifecycleOwner

        return reportMenuBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reportMenuViewModel.selectedReportType.observe(viewLifecycleOwner){ menu ->
            try {
                menu?.let {
                    when (menu){
                        is Report_Menu.DayWiseReport ->{
                            findNavController().navigate(R.id.action_report_menu_to_day_wise_report)
                            reportMenuViewModel.clearReportSelection()
                        }
                        is Report_Menu.MonthlyReport ->{
                            findNavController().navigate(R.id.idMonthlySummaryReport)
                            reportMenuViewModel.clearReportSelection()
                        }
                        is Report_Menu.YearlyReport ->{
                            findNavController().navigate(R.id.idYearlyReport)
                            reportMenuViewModel.clearReportSelection()
                        }
                        is Report_Menu.CategoryReport ->{
                            findNavController().navigate(R.id.idCategoryWiseReport)
                            reportMenuViewModel.clearReportSelection()
                        }
                        is Report_Menu.PaymentTypeReport ->{
                            findNavController().navigate(R.id.idPaymentTypeReport)
                            reportMenuViewModel.clearReportSelection()
                        }
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Selected Report Type: ${e.message}")
                Log.e("REPORT_MENU","Selected Report Type: ${e.message}")
            }
        }

//        reportMenuViewModel.showDayWiseReport.observe(viewLifecycleOwner){ isShow ->
//            if (isShow==true)
//            {
//                Log.i("SHOW DAY_WISE REPORT: ","Show Day_Wise Report: VISIBLE")
//                findNavController().navigate(R.id.action_report_menu_to_day_wise_report)
//            }
//            else
//            {
//                findNavController().navigate(R.id.action_day_wise_report_to_report_menu)
//                Log.i("SHOW DAY_WISE REPORT: ","Show Day_Wise Report: GONE")
//            }
//        }

//        reportMenuViewModel.showPaymentTypeReport.observe(viewLifecycleOwner){ isShow ->
//            if (isShow==true){
//                findNavController().navigate(R.id.idPaymentTypeReport)
//            }
//        }

//        reportMenuViewModel.showMonthlySummaryReport.observe(viewLifecycleOwner){ isShow ->
//            if (isShow==true){
//                findNavController().navigate(R.id.idMonthlySummaryReport)
//            }
//        }

//        reportMenuViewModel.showCategoryReport.observe(viewLifecycleOwner){ isShow ->
//            if (isShow==true){
//                findNavController().navigate(R.id.idCategoryWiseReport)
//            }
//        }

//        reportMenuViewModel.showYearlyReport.observe(viewLifecycleOwner){ isShow ->
//            if (isShow==true){
//                findNavController().navigate(R.id.idYearlyReport)
//            }
//        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReportMenu.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReportMenu().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}