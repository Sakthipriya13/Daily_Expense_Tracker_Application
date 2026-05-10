package com.example.expensetrackerapplication.ui.main.fragments

import android.app.AlertDialog
import android.content.Context
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
import com.example.expensetrackerapplication.logger.FileLogger
import com.example.expensetrackerapplication.databinding.CategoryListItemBinding
import com.example.expensetrackerapplication.databinding.ConfirmationPromptBinding
import com.example.expensetrackerapplication.databinding.SettingsBinding
import com.example.expensetrackerapplication.datastore.LanguageDataStore
import com.example.expensetrackerapplication.datastore.LoginDataStore
import com.example.expensetrackerapplication.datastore.ThemeColorDataStore
import com.example.expensetrackerapplication.datastore.ThemeDataStore
import com.example.expensetrackerapplication.factory.AppViewModelFactory
import com.example.expensetrackerapplication.model.CategoryModel
import com.example.expensetrackerapplication.utils.Global
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.ui.listeners.CategoryItemClickListener
import com.example.expensetrackerapplication.utils.ResultState
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.DeletePromptViewModel
import com.example.expensetrackerapplication.viewmodel.SettingsViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Settings.newInstance] factory method to
 * create an instance of this fragment.
 */
class Settings : Fragment()
{
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
    val LOG_TAG = "SETTINGS"

    private lateinit var logger : FileLogger
//        FileLogger(requireContext().applicationContext)

    private lateinit var deletePromptBinding : ConfirmationPromptBinding

    private lateinit var deletePromptDialog : AlertDialog

    var deleteCategory : CategoryModel? = null

    val deletePromptViewModel : DeletePromptViewModel by viewModels {
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
    ): View?
    {
        logger = FileLogger(requireContext().applicationContext)

        settingsBinding= DataBindingUtil.inflate(inflater,R.layout.settings, container, false)
        settingsBinding.settingsViewModel=settingsViewModel
        settingsBinding.lifecycleOwner=viewLifecycleOwner
        return settingsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {

            deletePromptBinding = DataBindingUtil.inflate(layoutInflater,R.layout.confirmation_prompt,null,false)
            deletePromptBinding.prompt = deletePromptViewModel
            deletePromptBinding.lifecycleOwner = viewLifecycleOwner

            deletePromptDialog = AlertDialog.Builder(requireContext())
                .setView(deletePromptBinding.root)
                .setCancelable(false)
                .create()
            deletePromptDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Delete Prompt Screen Creation: ${e.message}")
        }

        deletePromptViewModel.isClose.observe(viewLifecycleOwner){ isClose ->
            try
            {
                when(isClose){
                    is ResultState.Success ->{
                        Global.displayDialogPrompt=false
                        deletePromptDialog.dismiss()
                    }
                    is ResultState.fail ->{
                        settingsViewModel.fnDeleteCategory(deleteCategory?.categoryId,deleteCategory?.userId)
                        Global.displayDialogPrompt=false
                        deletePromptDialog.dismiss()
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Is Close Value Observed From DeletePromptViewModel: ${e.message}")
            }
        }

        // Datastore Variables Initialization
        loginDataStore= LoginDataStore(requireContext())
        languageDataStore= LanguageDataStore(requireContext())
        themeColorDataStore = ThemeColorDataStore(requireContext())
        themeDataStore = ThemeDataStore(requireContext())

//        settingsViewModel.fnGetUnSyncedcategories()
//        settingsViewModel.fnGetDefaultCategories()
        try
        {
            categoryAdapter = CategoryAdapter(requireContext().applicationContext)
            settingsBinding.idAddedCategoryList.adapter=categoryAdapter
            settingsBinding.idAddedCategoryList.layoutManager= LinearLayoutManager(requireContext())
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Set Category Adapter: ${e.message}")
        }

        try
        {
            settingsViewModel.fnGetAllCategories()
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Get All Categories: ${e.message}")
        }

        lifecycleScope.launch {
            launch {
                try {
                    val user = loginDataStore.userDatas?.first()

                    user?.let {
                        Global.lUserId = it.userId
                        Global.lUserName = it.userName ?: ""
                        Global.lUssrEmail = it.userEmail ?: ""
                        Global.lUserPassword = it.userPassword ?: ""
                        Global.lUserMobileNo = it.userMobileNo ?: ""
                        Global.cloudUserId = it.cloudId
                    }
                }
                catch (e: CancellationException) {
                    logger.logError(LOG_TAG, "Coroutine cancelled: ${e.message}")
//                    throw e
                }
                catch (e: Exception) {
                    logger.logError(LOG_TAG, "Store Login User Details: ${e.message}")
                }
            }
//            launch {
//                try
//                {
//                    loginDataStore.userDatas?.collect { user ->
//                        Global.lUserId = user.userId
//                        Global.lUserName = user.userName ?:""
//                        Global.lUssrEmail = user.userEmail ?:""
//                        Global.lUserPassword = user.userPassword ?:""
//                        Global.lUserMobileNo = user.userMobileNo ?:""
//                        Global.cloudUserId = user.cloudId
//                    }
//                }
//                catch (e: Exception)
//                {
//                    logger.logError(LOG_TAG,"Store Login User Details: ${e.message}")
//                }
//            }
            launch {
                try
                {
                    val themeCode = themeDataStore.fnGetTheme()
                    settingsViewModel._btnThemeCode.value = themeCode
                }
                catch (e: Exception)
                {
                    logger.logError(LOG_TAG,"Get Theme From Theme DataStore: ${e.message}")
                }
            }
            launch {
                try
                {
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
                catch (e: Exception)
                {
                    logger.logError(LOG_TAG,"Get Language From Language DataStore: ${e.message}")
                }
            }
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

        settingsViewModel.internetStatus.observe(viewLifecycleOwner) { isAvailable ->
            try
            {
                when (isAvailable)
                {
                    is ResultState1.success ->{}
                    is ResultState1.fail ->{
                        settingsViewModel._isLoading.value = false
                        fnShowMessage(getString(isAvailable.message),requireContext(),R.drawable.error_bg)
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Internet Status: ${e.message}")
            }
        }

        // In Activity
        settingsViewModel.sendEmailEvent.observe(viewLifecycleOwner) { intent ->
            try
            {
                intent?.let {
                    settingsViewModel._isLoading.value = false
                    startActivity(intent) // Activity context avoids crashes
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Send Email Event: ${e.message}")
            }
        }

//      settingsViewModel._firestoreCloudId.value = splashViewModel.cloudUserId.value


        settingsViewModel.categoryList.observe(viewLifecycleOwner){ list ->
            try
            {
                var categoryNameList : List<CategoryModel> = list.map { name ->
                    CategoryModel(userId = name.userId,categoryId = name.categoryId,categoryName = name.categoryName)
                }
                categoryAdapter.fnSubmitList(categoryNameList, object : CategoryItemClickListener{
                    override fun onRemoveClick(category: CategoryModel) {
                        try
                        {
//                            fnShowDeletePrompt(category)
                            deleteCategory = category
                            // Assign Title
                            deletePromptViewModel._title.value = getString(R.string.warning)
                            //Assign Content
                            deletePromptViewModel._message.value = getString(R.string.do_you_want_to_delete_the_category)
                            deletePromptDialog.show()
                        }
                        catch(e: Exception)
                        {
                            logger.logError(LOG_TAG,"Display Category Delete Confirmation Prompt: ${e.message}")
                        }
                    }
                }
                )
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Category List Observed: ${e.message}")
            }
        }

        settingsViewModel.insertCategoryStatus.observe(viewLifecycleOwner){ state ->
            try
            {
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
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Insert Category Status: ${e.message}")
            }
        }

        settingsViewModel.deleteCategoryStatus.observe(viewLifecycleOwner){ state ->
            try
            {
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
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Delete Category Status: ${e.message}")
            }
        }

        settingsBinding.idLanGroup.setOnCheckedChangeListener { _,checkedId ->
            try
            {
                when(checkedId)
                {
                    R.id.idRbEnglish -> {
                        settingsViewModel.fnUpdateLan("en")
                    }
                    R.id.idRbTamil -> {
                        settingsViewModel.fnUpdateLan("ta")
                    }
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Select Language: ${e.message}")
            }
        }

        settingsViewModel.isLoading.observe(viewLifecycleOwner){ status ->
            try
            {
                if(status)
                {
                    settingsBinding.idLoading.visibility = View.VISIBLE
                }
                else
                {
                    settingsBinding.idLoading.visibility = View.GONE
                }
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Display ProgressBar Based On Condition: ${e.message}")
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
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Update Theme: ${e.message}")
                Log.e("UPDATE_THEME","Update Theme From Ui: ${e.message}")
            }
        }

        settingsBinding.idColor1.setOnClickListener {
            try {
                settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE1)
            }
            catch (e: Exception){
                logger.logError(LOG_TAG,"Select Color1: ${e.message}")
            }
        }

        settingsBinding.idColor2.setOnClickListener {
            try
            {
                settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE2)
            }
            catch (e: Exception){
                logger.logError(LOG_TAG,"Select Color2: ${e.message}")
            }
        }

        settingsBinding.idColor3.setOnClickListener {
            try
            {
                settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE3)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Select Color3: ${e.message}")
            }
        }

        settingsBinding.idColor4.setOnClickListener {
            try
            {
                settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE4)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Select Color4: ${e.message}")
            }
        }

        settingsBinding.idColor5.setOnClickListener {
            try
            {
                settingsViewModel.fnUpdateThemeColor(Global.COLOR_CODE5)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Select Color5: ${e.message}")
            }
        }


        settingsBinding.idBtnSysTheme.setOnClickListener {
            try
            {
                settingsViewModel.fnUpdateTheme(Global.THEME_SYSTEM)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Select System Theme: ${e.message}")
            }
        }
        settingsBinding.idBtnDarkTheme.setOnClickListener {
            try
            {
                settingsViewModel.fnUpdateTheme(Global.THEME_DARK)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Select Dark Theme: ${e.message}")
            }
        }
        settingsBinding.idBtnLightTheme.setOnClickListener {
            try {
                settingsViewModel.fnUpdateTheme(Global.THEME_LIGHT)
            }
            catch (e: Exception)
            {
                logger.logError(LOG_TAG,"Select Light Theme: ${e.message}")
            }
        }
    }

//    fun fnShowDeletePrompt(category : CategoryModel)
//    {
//        try {
//            //Check If Prompt Already Display Or Not
//            if(Global.displayDialogPrompt==false)
//            {
//                // Assign True: Now Prompt Displayed
//                Global.displayDialogPrompt=true
//
//                // PromptBinding Variable Initialization
//                var promptBinding = ConfirmationPromptBinding.inflate(layoutInflater)
//                // Assign Title
//                promptBinding.tittle = getString(R.string.warning)
//                //Assign Content
//                promptBinding.message = getString(R.string.do_you_want_to_delete_the_category)
//                // AlertDialog Variable Initialization
//                val deletePrompt = AlertDialog.Builder(requireContext())
//                    .setView(promptBinding.root)
//                    .setCancelable(false)
//                    .create()
//                //Set Transparent Background
//                deletePrompt.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                // On Click OkBtn
//                promptBinding.idBtnOk.setOnClickListener {
//                    settingsViewModel.fnDeleteCategory(category.categoryId,category.userId)
//                    Global.displayDialogPrompt=false
//                    deletePrompt.dismiss()
//                }
//                // On Click CancelBtn
//                promptBinding.idBtnCancel.setOnClickListener {
//                    Global.displayDialogPrompt=false
//                    deletePrompt.dismiss()
//                }
//                // Show Prompt
//                deletePrompt.show()
//            }
//        }
//        catch (e: Exception)
//        {
//            logger.logError(LOG_TAG,"Display Category Delete Confirmation Prompt: ${e.message}")
//            Log.e("DISPLAY_WARNING_DELETE_CATEGORY","Display Warning - Delete Category: ${e.message}")
//        }
//    }
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

class CategoryAdapter(applicationContext: Context) : RecyclerView.Adapter<CategoryAdapter.ListViewHolder>()
{
    private var categoryNameList : List<CategoryModel> = emptyList()
    private lateinit var onCLickRemove : CategoryItemClickListener

    val logger = FileLogger(applicationContext)

    val LOG_TAG = "CATEGORY_ADAPTER"
    fun fnSubmitList(
        list: List<CategoryModel>,
        listener : CategoryItemClickListener)
    {
        try
        {
            categoryNameList=list
            onCLickRemove=listener
            notifyDataSetChanged()
        }
        catch (e: Exception)
        {
            logger.logError(LOG_TAG,"Submit List: ${e.message}")
        }
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