package com.example.expensetrackerapplication.ui.main.fragments

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.data.logger.FileLogger
import com.example.expensetrackerapplication.databinding.CategoryListItemBinding
import com.example.expensetrackerapplication.databinding.ConfirmationPromptBinding
import com.example.expensetrackerapplication.databinding.SettingsBinding
import com.example.expensetrackerapplication.datastore.LanguageDataStore
import com.example.expensetrackerapplication.datastore.LoginDataStore
import com.example.expensetrackerapplication.datastore.ThemeColorDataStore
import com.example.expensetrackerapplication.datastore.ThemeDataStore
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.model.CategoryModel
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.ui_event.CategoryItemClickListener
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Settings.newInstance] factory method to
 * create an instance of this fragment.
 */
class Settings : Fragment(){
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val appViewModelFactory by lazy {
        AppViewModelFactory(
            requireActivity().application,
            FileLogger(requireContext().applicationContext)
        )
    }

    // Binding Variable Declaration
    lateinit var settingsBinding: SettingsBinding

    // ViewModel Variable Declaration
    val settingsViewModel : SettingsViewModel by viewModels{
        appViewModelFactory
    }

    //Adapter Of Category
    lateinit  var categoryAdapter : CategoryAdapter

    //DataStore Variables Declaration
    private lateinit var languageDataStore: LanguageDataStore
    private lateinit var themeColorDataStore : ThemeColorDataStore
    private lateinit var themeDataStore: ThemeDataStore
    private lateinit var loginDataStore : LoginDataStore

    // Splash Viewmodel Variable Initialization
    val splashViewModel : SplashViewModel by viewModels{
        appViewModelFactory
    }


    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.title= resources.getString(R.string.setting_frag)
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
        settingsBinding= DataBindingUtil.inflate(inflater,R.layout.settings, container, false)
        settingsBinding.settingsViewModel=settingsViewModel
        settingsBinding.lifecycleOwner=viewLifecycleOwner
        return settingsBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginDataStore= LoginDataStore(requireContext())
        languageDataStore= LanguageDataStore(requireContext())
        themeColorDataStore = ThemeColorDataStore(requireContext())
        themeDataStore = ThemeDataStore(requireContext())

//        settingsViewModel.fnGetUnSyncedcategories()
//        settingsViewModel.fnGetDefaultCategories()

        settingsViewModel.fnGetAllCategories()


        lifecycleScope.launch {
            loginDataStore.userDatas?.collect { user ->
                Global.lUserId = user.userId
                Global.lUserName = user.userName ?:""
                Global.lUssrEmail = user.userEmail ?:""
                Global.lUserPassword = user.userPassword ?:""
                Global.lUserMobileNo = user.userMobileNo ?:""
                Global.cloudUserId = user.cloudId
            }

            val themeCode = themeDataStore.fnGetTheme()
            settingsViewModel._btnThemeCode.value = themeCode

        }

//        settingsViewModel.recreateActivity.observe(viewLifecycleOwner){ isRecreate ->
//            if(isRecreate){
//                try {
//                    requireActivity().recreate()
//                }
//                catch (e: Exception){
//                    Log.e("RECREATE_ACTIVITY","Recreate Activity: ${e.message}")
//                }
//            }
//        }

        lifecycleScope.launchWhenStarted {
            settingsViewModel.uiEvent.collect { event ->
                when(event){
                    SettingsViewModel.UiEvent.RecreateActivity ->{
                        requireActivity().recreate()
                    }
                }
            }
        }

        settingsViewModel.internetStatus.observe(viewLifecycleOwner) { isAvailable ->
            when (isAvailable) {
                is ResultState1.success ->{}
                is ResultState1.fail ->{
                    settingsViewModel._isLoading.value = false
                    fnShowMessage(getString(isAvailable.message),requireContext(),R.drawable.error_bg)
                }
            }

        }

        // In Activity
        settingsViewModel.sendEmailEvent.observe(viewLifecycleOwner) { intent ->
            intent?.let {
                settingsViewModel._isLoading.value = false
                startActivity(intent) // Activity context avoids crashes
            }
        }

//      settingsViewModel._firestoreCloudId.value = splashViewModel.cloudUserId.value

        categoryAdapter = CategoryAdapter()
        settingsBinding.idAddedCategoryList.adapter=categoryAdapter
        settingsBinding.idAddedCategoryList.layoutManager= LinearLayoutManager(requireContext())


        settingsViewModel.categoryList.observe(viewLifecycleOwner){ list ->
            var categoryNameList : List<CategoryModel> = list.map { name ->
                CategoryModel(userId = name.userId,categoryId = name.categoryId,categoryName = name.categoryName)
            }
            categoryAdapter.fnSubmitList(categoryNameList, object : CategoryItemClickListener{
                override fun onRemoveClick(category: CategoryModel) {
                    fnShowDeletePrompt(category)
                }
            }
            )
        }

        settingsViewModel.insertCategoryStatus.observe(viewLifecycleOwner){ state ->
            state?.let {
                when(state){
                    is ResultState1.success ->
                    {
                        fnShowMessage(getString(state.message),requireContext(),R.drawable.bg_success)
                    }
                    is ResultState1.fail ->
                    {
                        fnShowMessage(getString(state.message),requireContext(),R.drawable.error_bg)

                        if(state.message == R.string.set_NewCategoryFieldEmpty){
                            settingsBinding.idECat.isFocusable = true
                            settingsBinding.idECat.requestFocus()
                        }
                    }
                }
            }
        }

        settingsViewModel.deleteCategoryStatus.observe(viewLifecycleOwner){ state ->
            state?.let {
                when(state){
                    is ResultState1.success -> {
                        fnShowMessage(getString(state.message),requireContext(),R.drawable.bg_success)
                    }
                    is ResultState1.fail ->
                    {
                        fnShowMessage(getString(state.message),requireContext(),R.drawable.error_bg)
                    }
                }
            }
        }

        settingsBinding.idLanGroup.setOnCheckedChangeListener { _,checkedId ->
            when(checkedId){
                R.id.idRbEnglish -> {
                    settingsViewModel.fnUpdateLan("en")
                }
                R.id.idRbTamil -> {
                    settingsViewModel.fnUpdateLan("ta")
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val lang = languageDataStore.fnGetLanguage()
            when(lang){
                "en" ->{
//                    fnShowMessage("Language: English",requireContext(),R.drawable.bg_success)
                    settingsBinding.idRbEnglish.isChecked = true
                }
                "ta" -> {
//                    fnShowMessage("Language: Tamil",requireContext(),R.drawable.bg_success)
                    settingsBinding.idRbTamil.isChecked = true
                }
            }
        }

        settingsViewModel.isLoading.observe(viewLifecycleOwner){ status ->
            if(status){
                settingsBinding.idLoading.visibility = View.VISIBLE
            }
            else{
                settingsBinding.idLoading.visibility = View.GONE
            }
        }

        settingsViewModel.btnThemeCode.observe(viewLifecycleOwner){ themeCode ->
            try {
                settingsBinding.idBtnSysTheme.setBackgroundColor(
                    ContextCompat.getColor(requireContext(),R.color.color_grey)
                )
                settingsBinding.idBtnDarkTheme.setBackgroundColor(
                    ContextCompat.getColor(requireContext(),R.color.color_grey)
                )
                settingsBinding.idBtnLightTheme.setBackgroundColor(
                    ContextCompat.getColor(requireContext(),R.color.color_grey)
                )
                when(themeCode){
                    Global.THEME_DARK -> {
                        settingsBinding.idBtnDarkTheme.setBackgroundColor(
                            MaterialColors.getColor(
                                requireView(),
                                com.google.android.material.R.attr.colorOnPrimary
                            )
                        )
                        settingsBinding.idBtnDarkTheme.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.text_color_white
                            )
                        )
                        settingsBinding.idBtnDarkTheme.iconTint = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.text_color_white
                            )
                        )

                    }
                    Global.THEME_LIGHT -> { settingsBinding.idBtnLightTheme.setBackgroundColor(
                        MaterialColors.getColor(
                            requireView(),
                            com.google.android.material.R.attr.colorOnPrimary
                        )
                    )
                        settingsBinding.idBtnLightTheme.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.text_color_white
                            )
                        )
                        settingsBinding.idBtnLightTheme.iconTint = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.text_color_white
                            )
                        )

                    }
                    else -> {
                        settingsBinding.idBtnSysTheme.setBackgroundColor(
                            MaterialColors.getColor(
                                requireView(),
                                com.google.android.material.R.attr.colorOnPrimary
                            )
                        )
                        settingsBinding.idBtnSysTheme.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.text_color_white
                            )
                        )
                        settingsBinding.idBtnSysTheme.iconTint = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.text_color_white
                            )
                        )
                    }
                }
            }
            catch (e: Exception){
                Log.e("UPDATE_THEME","Update Theme From Ui: ${e.message}")
            }
        }

        settingsBinding.idColor1.setOnClickListener {
            settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE1)
        }

        settingsBinding.idColor2.setOnClickListener {
            settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE2)
        }

        settingsBinding.idColor3.setOnClickListener {
            settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE3)
        }

        settingsBinding.idColor4.setOnClickListener {
            settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE4)
        }

        settingsBinding.idColor5.setOnClickListener {
            settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE5)
        }


        settingsBinding.idBtnSysTheme.setOnClickListener {
            settingsViewModel.fnUpdateTheme(Global.THEME_SYSTEM)
        }
        settingsBinding.idBtnDarkTheme.setOnClickListener {
            settingsViewModel.fnUpdateTheme(Global.THEME_DARK)

        }
        settingsBinding.idBtnLightTheme.setOnClickListener {
            settingsViewModel.fnUpdateTheme(Global.THEME_LIGHT)
        }
    }

//    suspend fun fnGetCloudUserId():String{
//        val auth = FirebaseAuth.getInstance()
//
//        if(auth.currentUser == null){
//            auth.signInAnonymously().await()
//        }
//
//        return auth.currentUser!!.uid
//    }

//    fun fnUpdateBtnTheme(themeCode : Int){
//        settingsBinding.idBtnSysTheme.setBackgroundColor(
//            ContextCompat.getColor(requireContext(),R.color.color_grey)
//        )
//        settingsBinding.idBtnDarkTheme.setBackgroundColor(
//            ContextCompat.getColor(requireContext(),R.color.color_grey)
//        )
//        settingsBinding.idBtnLightTheme.setBackgroundColor(
//            ContextCompat.getColor(requireContext(),R.color.color_grey)
//        )
//        when(themeCode){
//            Global.THEME_DARK -> {
//                settingsBinding.idBtnDarkTheme.setBackgroundColor(
//                    MaterialColors.getColor(
//                        requireView(),
//                        com.google.android.material.R.attr.colorOnPrimary
//                    )
//                )
//                settingsBinding.idBtnDarkTheme.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.text_color_white
//                    )
//                )
//                settingsBinding.idBtnDarkTheme.iconTint = ColorStateList.valueOf(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.text_color_white
//                    )
//                )
//
//            }
//            Global.THEME_LIGHT -> { settingsBinding.idBtnLightTheme.setBackgroundColor(
//                MaterialColors.getColor(
//                    requireView(),
//                    com.google.android.material.R.attr.colorOnPrimary
//                    )
//                )
//                settingsBinding.idBtnLightTheme.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.text_color_white
//                    )
//                )
//                settingsBinding.idBtnLightTheme.iconTint = ColorStateList.valueOf(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.text_color_white
//                    )
//                )
//
//            }
//            else -> {
//                settingsBinding.idBtnSysTheme.setBackgroundColor(
//                    MaterialColors.getColor(
//                        requireView(),
//                        com.google.android.material.R.attr.colorOnPrimary
//                    )
//                )
//                settingsBinding.idBtnSysTheme.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.text_color_white
//                    )
//                )
//                settingsBinding.idBtnSysTheme.iconTint = ColorStateList.valueOf(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.text_color_white
//                    )
//                )
//            }
//        }
//    }

    fun fnShowDeletePrompt(category : CategoryModel)
    {
        try {
            //Check If Prompt Already Display Or Not
            if(Global.displayDialogPrompt==false)
            {
                // Assign True: Now Prompt Displayed
                Global.displayDialogPrompt=true

                // PromptBinding Variable Initialization
                var promptBinding = ConfirmationPromptBinding.inflate(layoutInflater)
                // Assign Title
                promptBinding.tittle = getString(R.string.warning)
                //Assign Content
                promptBinding.message = getString(R.string.do_you_want_to_delete_the_category)
                // AlertDialog Variable Initialization
                val deletePrompt = AlertDialog.Builder(requireContext())
                    .setView(promptBinding.root)
                    .setCancelable(false)
                    .create()
                //Set Transparent Background
                deletePrompt.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // On Click OkBtn
                promptBinding.idBtnOk.setOnClickListener {
                    settingsViewModel.fnDeleteCategory(category.categoryId,category.userId)
                    Global.displayDialogPrompt=false
                    deletePrompt.dismiss()
                }
                // On Click CancelBtn
                promptBinding.idBtnCancel.setOnClickListener {
                    Global.displayDialogPrompt=false
                    deletePrompt.dismiss()
                }
                // Show Prompt
                deletePrompt.show()
            }
        }
        catch (e: Exception){
            Log.e("DISPLAY_WARNING_DELETE_CATEGORY","Display Warning - Delete Category: ${e.message}")
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Settings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Settings().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

class CategoryAdapter() : RecyclerView.Adapter<CategoryAdapter.ListViewHolder>()
{
    private var categoryNameList : List<CategoryModel> = emptyList()
    private lateinit var onCLickRemove : CategoryItemClickListener
    fun fnSubmitList(
        list: List<CategoryModel>,
        listener : CategoryItemClickListener){
        categoryNameList=list
        onCLickRemove=listener
        notifyDataSetChanged()
    }

    inner class ListViewHolder(val binding: CategoryListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(name : CategoryModel){
            binding.category=name
            binding.onClickRemove=onCLickRemove
            binding.executePendingBindings()
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryAdapter.ListViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        var view = DataBindingUtil.inflate<CategoryListItemBinding>(inflater,R.layout.category_list_item,parent,false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ListViewHolder, position: Int) {
        holder.bind(categoryNameList[position])
    }

    override fun getItemCount(): Int {
        return categoryNameList.size
    }

}