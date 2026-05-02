package com.example.expensetrackerapplication.ui.main.fragments.reports

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.text.intl.Locale
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.databinding.CategoryChartListItemBinding
import com.example.expensetrackerapplication.databinding.CategoryListItemBinding
import com.example.expensetrackerapplication.databinding.CategoryWiseReportBinding
import com.example.expensetrackerapplication.model.CategoryChartModel
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.reusefiles.fnShowMessage
import com.example.expensetrackerapplication.viewmodel.CategoryWiseReportViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CategoryWiseReport.newInstance] factory method to
 * create an instance of this fragment.
 */
class CategoryWiseReport : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var categoryWiseReportBinding : CategoryWiseReportBinding

    private val categoryWiseReportViewModel : CategoryWiseReportViewModel by viewModels()
    
    private lateinit var categoryAdapter : CategoryAdapter

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
        categoryWiseReportBinding = DataBindingUtil.inflate(inflater,R.layout.category_wise_report, container, false)
        categoryWiseReportBinding.categoryWiseReportViewModel = categoryWiseReportViewModel
        categoryWiseReportBinding.lifecycleOwner = viewLifecycleOwner
        // Inflate the layout for this fragment
        return categoryWiseReportBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryWiseReportViewModel.fnPreWarmExcelEngine()
        
        categoryAdapter = CategoryAdapter()
        categoryWiseReportBinding.idCategoryListView.adapter = categoryAdapter
        categoryWiseReportBinding.idCategoryListView.layoutManager = LinearLayoutManager(requireContext())

        categoryWiseReportBinding.idBtnCalendar.setOnClickListener {
            if(Global.isCalendarSelected==false)
            {
                Global.isCalendarSelected=true
                val caledar = Calendar.getInstance()
                val day = caledar.get(Calendar.DAY_OF_MONTH)
                val month = caledar.get(Calendar.MONTH)
                val year = caledar.get(Calendar.YEAR)

                var datePickerDialog = DatePickerDialog(requireContext(),
                    { _,y,m,d ->
                        caledar.set(y,m,d)
                        val sdf1 = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                        val sdf = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val date = sdf.format(caledar.time)
                        val dateUi = sdf1.format(caledar.time)
                        categoryWiseReportViewModel._selectedDate.value = date
                        categoryWiseReportViewModel._selectedDateUi.value = date

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

        categoryWiseReportViewModel.selectedDate.observe(viewLifecycleOwner){ date ->
            categoryWiseReportViewModel.fnGetCategoryDetailsPerDay(date)
        }

        categoryWiseReportViewModel.categoryList.observe(viewLifecycleOwner){ list ->
            if(list.isNotEmpty()){
                categoryWiseReportBinding.idScrollView.visibility = View.VISIBLE
                categoryWiseReportBinding.idNoReportsText.visibility = View.GONE

                categoryWiseReportViewModel._isExportLoading.value = false
//                fnRenderPieChart(list)
                categoryAdapter.fnSubmitList(list)
            }
            else{
                categoryWiseReportBinding.idScrollView.visibility = View.GONE
                categoryWiseReportBinding.idNoReportsText.visibility = View.VISIBLE

                categoryWiseReportViewModel._isExportLoading.value = false
            }
        }

        categoryWiseReportViewModel.closeCategoryReport.observe(viewLifecycleOwner){ status ->
            if(status){
                findNavController().navigate(R.id.action_category_wise_report_to_report_menu)
            }
        }

        categoryWiseReportViewModel.isExportLoading.observe(viewLifecycleOwner){ status ->
            if(status){
                categoryWiseReportBinding.isExportLoading.visibility = View.VISIBLE
            }
            else{
                categoryWiseReportBinding.isExportLoading.visibility = View.GONE
            }
        }

        categoryWiseReportViewModel.exportStatus.observe(viewLifecycleOwner){ status ->
            if(status){
                fnShowMessage("Report Successfully Exported",requireContext(),R.drawable.bg_success)
            }
            else{
                fnShowMessage("Report Export Failed",requireContext(),R.drawable.error_bg)
            }
        }
    }

//    fun fnRenderPieChart(list : List<CategoryChartModel>)
//    {
//        val pie = AnyChart.pie()
//
//        // STEP 2.3 – Prepare fresh data
//        val data = mutableListOf<DataEntry>()
//
//        list.forEach {
//            Log.i("CATEGORY NAME","Category Name: ${it.categoryName} And Amt: ${it.expenseAmt}")
//            data.add(
//                ValueDataEntry(
//                    it.categoryName,
//                    it.expenseAmt
//                )
//            )
//        }
//
//        // STEP 2.4 – Set data to Pie
//        pie.data(data)
//        pie.title(getString(R.string.category_overview))
//
//        // STEP 2.5 – Set chart
//        categoryWiseReportBinding.idChartView.setChart(pie)
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CategoryWiseReport.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CategoryWiseReport().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

class CategoryAdapter(): RecyclerView.Adapter<CategoryAdapter.ListViewHolder>()
{
    private var categoryList : List<CategoryChartModel> = emptyList()

    fun fnSubmitList(
        list : List<CategoryChartModel>
    ){
        categoryList = list
        notifyDataSetChanged()
    }

    inner class ListViewHolder(val binding : CategoryChartListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: CategoryChartModel){
            binding.category=item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryAdapter.ListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<CategoryChartListItemBinding>(inflater,R.layout.category_chart_list_item,parent,false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ListViewHolder, position: Int) {
        holder.bind(categoryList[position])
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

}