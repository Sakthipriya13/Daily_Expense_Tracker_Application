package com.example.expensetrackerapplication.ui.main.fragments.reports

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
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
import com.example.expensetrackerapplication.data.entity.ExpenseEntity
import com.example.expensetrackerapplication.databinding.AlertSheetMonthBinding
import com.example.expensetrackerapplication.databinding.MonthYearDialogItemBinding
import com.example.expensetrackerapplication.databinding.MonthlyReportBinding
import com.example.expensetrackerapplication.databinding.YearlySummaryReportListItemViewBinding
import com.example.expensetrackerapplication.model.ExpenseDetailsPerMonth
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.reusefiles.fnShowMessage
import com.example.expensetrackerapplication.viewmodel.MonthlySummaryViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
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

    private lateinit var monthlySummaryBinding : MonthlyReportBinding

    private val monthlySummaryViewModel : MonthlySummaryViewModel by viewModels()

    private var selectedYearMonth : YearMonth = YearMonth.now()

    private lateinit var monthlySummaryReportAdapter : MonthlySummaryReportAdapter

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
        monthlySummaryBinding = DataBindingUtil.inflate(inflater,R.layout.monthly_report, container, false)
        monthlySummaryBinding.monthlySummaryViewModel = monthlySummaryViewModel
        monthlySummaryBinding.lifecycleOwner = viewLifecycleOwner

        // Inflate the layout for this fragment
        return monthlySummaryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monthlySummaryViewModel.fnPreWarmExcelEngine()

        monthlySummaryReportAdapter = MonthlySummaryReportAdapter()
        monthlySummaryBinding.idMonthlySummaryReportView.adapter = monthlySummaryReportAdapter
        monthlySummaryBinding.idMonthlySummaryReportView.layoutManager = LinearLayoutManager(requireContext())

        monthlySummaryBinding.idBtnCalendar.setOnClickListener{
            showMonthPicker()
        }
        monthlySummaryViewModel.closeReport.observe(viewLifecycleOwner){ status ->
            if(status){
                findNavController().navigate(R.id.action_monthly_summary_report_to_report_menu)
            }
        }

        monthlySummaryViewModel.selectedMonthAndYear.observe(viewLifecycleOwner){ monthAndYear ->
            monthlySummaryViewModel.fnGetExpenseDetailsPerMonth(
                monthlySummaryViewModel.selectedMonth.value ?:"",
                monthlySummaryViewModel.selectedYear.value ?:""
            )
        }

        monthlySummaryViewModel.monthlySummaryReportList.observe(viewLifecycleOwner){ list ->
            if(list.isNotEmpty()){
                monthlySummaryReportAdapter.fnSubmitList(list)
                monthlySummaryViewModel._isExportLoading.value = false
//                monthlySummaryBinding.idNoReportsText.visibility = View.GONE
//                monthlySummaryBinding.idTransactionsView.visibility = View.VISIBLE
            }
            else{
                monthlySummaryViewModel._isExportLoading.value = false
//                monthlySummaryBinding.idNoReportsText.visibility = View.VISIBLE
//                monthlySummaryBinding.idTransactionsView.visibility = View.GONE
            }
        }

        monthlySummaryViewModel.exportStatus.observe(viewLifecycleOwner){ status ->
            if(status){
                fnShowMessage("Report Successfully Exported",requireContext(),R.drawable.bg_success)
            }
            else{
                fnShowMessage("Report Export Failed",requireContext(),R.drawable.error_bg)
            }
        }

        monthlySummaryViewModel.isExportLoading.observe(viewLifecycleOwner){ isLoading ->
            if(isLoading){
                monthlySummaryBinding.isExportLoading.visibility=View.VISIBLE
            }
            else{
                monthlySummaryBinding.isExportLoading.visibility=View.GONE
            }
        }

    }


    private fun showMonthPicker() {
        if(Global.isCalendarSelected == false){
            Global.isCalendarSelected = true
            val monthBinding = AlertSheetMonthBinding.inflate(layoutInflater)

            val monthAlert = AlertDialog.Builder(requireContext())
            monthAlert.setView(monthBinding.root)
            monthAlert.setCancelable(false)

            val dialog = monthAlert.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val monthAdapter = MonthAdapter { month ->
                selectedYearMonth = selectedYearMonth.withMonth(month)
                Log.i("SELECTED MONTH","Selected Month: ${selectedYearMonth.monthValue}&${ monthBinding.idTextYear.text}")
                var selectedMonth = if (month < 10) "0$month" else "$month"
                monthlySummaryViewModel._selectedMonth.value = selectedMonth
                monthlySummaryViewModel._selectedYear.value = "${monthBinding.idTextYear.text}"
                monthlySummaryViewModel._selectedMonthAndYear.value = "$selectedMonth/${monthBinding.idTextYear.text}"
                dialog.dismiss()
                Global.isCalendarSelected = false
            }

            monthBinding.idMonthPicker.apply {
                layoutManager = GridLayoutManager(requireContext(),3)
                adapter = monthAdapter
            }

            monthBinding.idCancelMonth.setOnClickListener {
                dialog.dismiss()
                Global.isCalendarSelected =false
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            monthBinding.idYearPicker.minValue = 2000
            monthBinding.idYearPicker.maxValue = currentYear + 100
            monthBinding.idYearPicker.value = currentYear
            monthBinding.idYearPicker.wrapSelectorWheel = false

            monthBinding.idTextYear.text="${monthBinding.idYearPicker.value}"

            monthBinding.idTextYear.setOnClickListener {
                monthBinding.idYearPicker.visibility = View.VISIBLE
                monthBinding.idMonthPicker.visibility = View.GONE

                monthBinding.idMonthLayout.visibility = View.GONE
                monthBinding.idYearLayout.visibility = View.VISIBLE

                monthBinding.idOkYear.setOnClickListener {
                    monthBinding.idTextYear.text="${monthBinding.idYearPicker.value}"

                    val selectedYear = java.time.YearMonth.of(
                        monthBinding.idYearPicker.value,
                        java.time.YearMonth.now().monthValue
                    )

                    monthAdapter.submitYearMonth(selectedYear)

                    monthBinding.idYearPicker.visibility = View.GONE
                    monthBinding.idMonthPicker.visibility = View.VISIBLE

                    monthBinding.idMonthLayout.visibility = View.VISIBLE
                    monthBinding.idYearLayout.visibility = View.GONE
                }

                monthBinding.idCancelYear.setOnClickListener {
                    monthBinding.idYearPicker.visibility = View.GONE
                    monthBinding.idMonthPicker.visibility = View.VISIBLE

                    monthBinding.idMonthLayout.visibility = View.VISIBLE
                    monthBinding.idYearLayout.visibility = View.GONE
                }
            }
        }
    }


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

class MonthlySummaryReportAdapter : RecyclerView.Adapter<MonthlySummaryReportAdapter.ListViewHolder>()
{
    var monthlySummaryList : List<ExpenseDetailsPerMonth> = emptyList()

    fun fnSubmitList(list  : List<ExpenseDetailsPerMonth>){
        monthlySummaryList = list
        notifyDataSetChanged()
    }

    inner class ListViewHolder(val binding : YearlySummaryReportListItemViewBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(ob : ExpenseDetailsPerMonth){
            binding.expense = ob
            binding.executePendingBindings()
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
    private val onClick:(Int)-> Unit
) : RecyclerView.Adapter<MonthAdapter.VH>() {

    private var selectedYearMonth: YearMonth = YearMonth.now()

    private val months = Month.values()

    fun submitYearMonth(yearMonth: YearMonth) {
        selectedYearMonth = yearMonth
        notifyDataSetChanged()
    }

    inner class VH(val binding: MonthYearDialogItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(month: Month) {

            binding.txtMonth.text = month.getDisplayName(TextStyle.SHORT,Locale.getDefault())

            val isSelected =
                month.value == selectedYearMonth.monthValue

            binding.root.isSelected = isSelected

            binding.root.setOnClickListener {
                onClick(month.value)
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

