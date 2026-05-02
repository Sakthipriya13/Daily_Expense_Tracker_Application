package com.example.expensetrackerapplication.ui.main.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.expensetrackerapplication.R
import com.example.expensetrackerapplication.databinding.AddIncomeBinding
import com.example.expensetrackerapplication.databinding.ChangePasswordBinding
import com.example.expensetrackerapplication.databinding.ConfirmationPromptBinding
import com.example.expensetrackerapplication.databinding.ProfileBinding
import com.example.expensetrackerapplication.`object`.Global
import com.example.expensetrackerapplication.utils.fnShowMessage
import com.example.expensetrackerapplication.ui.auth.Auth
import com.example.expensetrackerapplication.utils.ResultState1
import com.example.expensetrackerapplication.viewmodel.AddInComeViewModel
import com.example.expensetrackerapplication.viewmodel.ChangePasswordViewModel
import com.example.expensetrackerapplication.viewmodel.ProfileViewModel
import com.example.expensetrackerapplication.viewmodel.SplashViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var profileBinding: ProfileBinding

    private val profileViewModel : ProfileViewModel by viewModels()

//    private val mainViewModel : MainViewModel by activityViewModels()

//    private  var profilePhotoUri : Uri? = null
//    private lateinit var cameraLauncher: ActivityResultLauncher<Uri?>
//    private lateinit var galleryLauncher: ActivityResultLauncher<String>


//    private var takePictureLauncher =
//        registerForActivityResult(ActivityResultContracts.TakePicture()){ success ->
//        if(success){
//            profileViewModel.fnUpdateUserProfilePhoto(profilePhotoUri)
//        }
//    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.title= resources.getString(R.string.profile_frag)
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

//        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//
//            if (success && profilePhotoUri != null) {
//
//                lifecycleScope.launch {
//
//                    val path = withContext(Dispatchers.IO) {
//                        Global.fnCopyImageToInternalStorage(
//                            requireContext(),
//                            profilePhotoUri!!
//                        )
//                    }
//
//                    profileViewModel.saveUriToDatabase(path)
//
//                    loadImage()
//                }
//            }
//        }

//        galleryLauncher =
//            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//
//                uri?.let {
//                    // ✅ STEP 1: Show instantly (no delay)
//                    Glide.with(this)
//                        .load(it)
//                        .placeholder(R.drawable.user)
//                        .into(profileBinding.idProfileImage)
//
//                    // ✅ STEP 2: Save in background
//                    lifecycleScope.launch {
//                        try {
//                            val path = withContext(Dispatchers.IO) {
//                                Global.fnCopyImageToInternalStorage(requireContext(), it)
//                            }
//
//                            profileViewModel.saveUriToDatabase(path)
//
//                            mainViewModel._triggerProfile.value = true
//
//                        } catch (e: Exception) {
//                            Log.e("GALLERY_ERROR", e.message.toString())
//                        }
//                    }


//                    lifecycleScope.launch {
//
//                        val path = withContext(Dispatchers.IO) {
//                            copyImageToInternalStorage(requireContext(), it)
//                        }
//                        profileViewModel.saveUriToDatabase(path)
//
//                        loadImage()
//
//                        //18-03-2026 add this line
//                        mainViewModel.fnGetUserProfilePhotoUri()
//                    }
//                }
//            }

        profileBinding= DataBindingUtil.inflate(inflater,R.layout.profile,container,false)
        profileBinding.profile=profileViewModel
        profileBinding.lifecycleOwner=viewLifecycleOwner

        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //28-03-2026
//        profileViewModel.fnGetUserProfilePhotoUri()

//        lifecycleScope.launch {
//
//            val path = withContext(Dispatchers.IO) {
//                profileViewModel.fnGetImage().await()
//            }
//
//            path.let {
//                Log.e("PROFILE URI STRING","Profile Uri String: $it")
//                if(it!=null && !it.equals("")){
//                    Glide.with(requireContext())
//                        .load(File(it))
//                        .into(profileBinding.idProfileImage)
//                }
//                else{
//                    profileBinding.idProfileImage.setImageResource(R.drawable.user)
//                }
//            }
//        }

//        profileViewModel.profilePath.observe(viewLifecycleOwner){ path ->
//            path.let {
//                Log.e("PROFILE URI STRING","Profile Uri String: $it")
//                if(it!=null && !it.equals("")){
//                    Glide.with(requireContext())
//                        .load(File(it))
//                        .into(profileBinding.idProfileImage)
//                }
//                else{
//                    profileBinding.idProfileImage.setImageResource(R.drawable.user)
//                }
//            }
//        }

        profileViewModel.isDelAccount.observe(viewLifecycleOwner){ isDelAc ->
            if(isDelAc){
                if(Global.isCalendarSelected==false)
                {
                    //Flag Value
                    Global.isCalendarSelected = true

                    //Confirmation Binding
                    val view = ConfirmationPromptBinding.inflate(layoutInflater)
                    view.tittle = getString(R.string.deleteAccount_Title)
                    view.message = getString(R.string.deleteAccount_Content)

                    //AlertDialog Screen Variable Initialization
                    var delAcPrompt = AlertDialog.Builder(requireContext())
                        .setView(view.root)
                        .setCancelable(false)
                        .create()

                    //Set Transparent Background
                    delAcPrompt.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    //On Click Ok Button
                    view.idBtnOk.setOnClickListener {
                        Global.isCalendarSelected = false
                        profileViewModel.fnDeleteUserAccount()
                    }

                    //On Click Cancel Button
                    view.idBtnCancel.setOnClickListener {
                        Global.isCalendarSelected = false
                        delAcPrompt.dismiss()
                    }

                    //Display Dialog Screen
                    delAcPrompt.show()
                }
            }
        }

        profileViewModel.deleteUserAcStatus.observe(viewLifecycleOwner){ isDeleted ->
            if(isDeleted){
                fnShowMessage(getString(R.string.deleteAccount_Success),requireContext(),R.drawable.bg_success)
                var intent = Intent(requireContext(),Auth::class.java)
                startActivity(intent)
            }
            else{
                fnShowMessage(getString(R.string.deleteAccount_Failed),requireContext(),R.drawable.bg_success)
            }
    }

        profileViewModel.isChangePassword.observe(viewLifecycleOwner){ status ->
            if(status){
                if(Global.isBottomSheetSelected==false) {
                    Global.isBottomSheetSelected = true
                    ChangePassword().show(parentFragmentManager, "ChangePasswordBottomSheet")
                }
            }
        }

//        profileViewModel.isEdit.observe(viewLifecycleOwner){ status ->
//            if(status == true ){
//                if(Global.isCalendarSelected==false) {
//                    Global.isCalendarSelected = true
//                    val view = EditProfilePhotoBinding.inflate(layoutInflater)
//
//                    var delAcPrompt = AlertDialog.Builder(requireContext())
//                        .setView(view.root)
//                        .setCancelable(false)
//                        .create()
//                    delAcPrompt.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//                    view.idGalleryFlow.setOnClickListener {
//                        try{
//                            galleryLauncher.launch("image/*")
//                            Global.isCalendarSelected = false
//                            delAcPrompt.dismiss()
//                        }
//                        catch (e : Exception) {
//                            Log.e("GET IMAGE FROM GALLERY","Get Image From Gallery: ${e.message}")
//                            fnShowMessage("Something To Wrong To Get Image From Gallery",requireContext(),R.drawable.error_bg)
//                        }
//                    }
//
//                    view.idCameraFlow.setOnClickListener {
//                        try{
//                            checkCameraPermissions()
//                            Global.isCalendarSelected = false
//                            delAcPrompt.dismiss()
//                        }
//                        catch (e : Exception){
//                            Log.e("GET IMAGE FROM CAMERA","Get Image From Camera: ${e.message}")
//                            fnShowMessage("Get Image From Camera: ${e.message}",requireContext(),R.drawable.error_bg)
//                        }
//                    }
//
//                    view.idBtnDelProfilePhoto.setOnClickListener {
//                        profileBinding.idProfileImage.setImageResource(R.drawable.user)
//                        profileViewModel.fnUpdateUserProfilePhoto(null)
//                        Global.isCalendarSelected = false
//                        delAcPrompt.dismiss()
//                    }
//
//                    view.idBtnCancel.setOnClickListener {
//                        Global.isCalendarSelected = false
//                        delAcPrompt.dismiss()
//                    }
//                    delAcPrompt.show()
//                }
//            }
//        }


        profileViewModel.isAddIncome.observe(viewLifecycleOwner){ status ->
            if(status){
                if(Global.isBottomSheetSelected==false){
                    Global.isBottomSheetSelected=true
                    AddIncome().show(parentFragmentManager,"AddIncomeBottomSheet")
                }
            }
        }

//        profileViewModel.profileUri.observe(viewLifecycleOwner){ uri ->
//            if(uri!=null){
//                Log.i("PROFILE URI","Profile Uri: $uri")
//                profileBinding.idProfileImage.setImageURI(uri)
//                mainViewModel.fnGetUserProfilePhotoUri()
//            }
//            else{
//                profileBinding.idProfileImage.setImageResource(R.drawable.user)
//                fnShowMessage("Something Wrong , Can't Load Profile Photo",requireContext(),R.drawable.error_bg)
//            }
//        }
    }

//    fun loadImage() {
//        lifecycleScope.launch {
//
//            val path = withContext(Dispatchers.IO) {
//                profileViewModel.fnGetImage().await()
//            }
//
//            path?.let {
//
//                Glide.with(requireContext())
//                    .load(File(it))
//                    .into(profileBinding.idProfileImage)
//            }
//        }
//    }

//    fun checkCameraPermissions(){
//        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED )
//        {
//            openCamera()
//        }
//        else{
//            requestPermissions(arrayOf(Manifest.permission.CAMERA),100)
//        }
//    }

//    fun openCamera(){
//        profilePhotoUri=createImageUri()
//        cameraLauncher.launch(profilePhotoUri)
//    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if(requestCode==100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//            openCamera()
//        }
//    }

//    fun createImageUri(): Uri{
//        val file = File(
//            requireContext().cacheDir,
//            "camera_${System.currentTimeMillis()}.jpg"
//        )
//
//        return FileProvider.getUriForFile(
//            requireContext(),
//            "${requireContext().packageName}.provider",
//            file
//        )
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

class ChangePassword : BottomSheetDialogFragment(){
    private lateinit var changePasswordBinding : ChangePasswordBinding
    private val changePasswordViewModel : ChangePasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        changePasswordBinding= DataBindingUtil.inflate(inflater,R.layout.change_password,container,false)
        changePasswordBinding.changePassword=changePasswordViewModel
        changePasswordBinding.lifecycleOwner=viewLifecycleOwner

        return changePasswordBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        changePasswordViewModel.passwordErrorStatus.observe(viewLifecycleOwner){ state ->
//            when(state)
//            {
//                is ResultState.success -> {}
//                is ResultState.fail ->{
//                    changePasswordBinding.idENewPassword.isFocusable = true
//                    changePasswordBinding.idENewPassword.requestFocus()
//                    fnShowMessage(state.message,requireContext(),R.drawable.error_bg)
//                }
//            }
//        }

        changePasswordViewModel.isClosed.observe(viewLifecycleOwner){ isClosed ->
            if(isClosed){
                Global.isBottomSheetSelected = false
                dismiss()
            }
        }
        changePasswordViewModel.changePasswordStatus.observe(viewLifecycleOwner){ state ->
            when(state){
                is ResultState1.success  -> {
                    fnShowMessage(getString(state.message),requireContext(),R.drawable.bg_success)
                    Global.isBottomSheetSelected = false
                    dismiss()
                }
                is ResultState1.fail  -> {
                    fnShowMessage(getString(state.message),requireContext(),R.drawable.error_bg)

                    if(state.message == R.string.cp_NewPasswordFieldEmpty){
                        changePasswordBinding.idENewPassword.isFocusable = true
                        changePasswordBinding.idENewPassword.requestFocus()
                    }
                    else if(state.message == R.string.cp_PasswordChangeFailed){
                        Global.isBottomSheetSelected = false
                        dismiss()
                    }
                }
            }
        }
    }
}

class AddIncome : BottomSheetDialogFragment(){

    private lateinit var addIncomeBinding : AddIncomeBinding
    val addIncomeViewModel : AddInComeViewModel by viewModels()

    val splashViewModel : SplashViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addIncomeBinding = DataBindingUtil.inflate(inflater,R.layout.add_income,container,false)
        addIncomeBinding.addIncome = addIncomeViewModel
        addIncomeBinding.lifecycleOwner=viewLifecycleOwner

        return addIncomeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addIncomeBinding.idBtnCalendar.setOnClickListener {
            if(Global.isCalendarSelected==false)
            {
                Global.isCalendarSelected=true
                var calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    requireContext(), { _,y,m,d ->
                        calendar.set(y,m,d)
                        val sdf1 = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                        var date = sdf.format(calendar.time)
                        var dateUi = sdf1.format(calendar.time)

                        addIncomeViewModel._selectedDate.value=date
                        addIncomeViewModel._selectedDateUi.value=dateUi

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


//        lifecycleScope.launch {
//            addIncomeViewModel._firestoreCloudId.value = splashViewModel.cloudUserId.value
//        }

        addIncomeViewModel.isClosed.observe(viewLifecycleOwner){ isClosed ->
            if(isClosed){
                Global.isBottomSheetSelected=false
                dismiss()
            }
        }

        addIncomeViewModel.insertStatus.observe(viewLifecycleOwner){ state ->
            when(state){
                is ResultState1.success -> {
                    fnShowMessage(getString(state.message),requireContext(),R.drawable.bg_success)
                    Global.isBottomSheetSelected=false
                    dismiss()
                }

                is ResultState1.fail -> {
                    fnShowMessage(getString(state.message),requireContext(),R.drawable.error_bg)
                    Global.isBottomSheetSelected=false
                    dismiss()
                    if(state.message == R.string.income_IncomeFieldEmpty)
                    {
                        addIncomeBinding.idEIncome.isFocusable = true
                        addIncomeBinding.idEIncome.requestFocus()
                    }
                }
            }
        }
    }
}

//class EditProfile : BottomSheetDialogFragment(){
//
//    private lateinit var editProfilePhotoBinding : EditProfilePhotoBinding
//
//    val editProfilePhotoViewModel : EditProfilePhotoViewModel by viewModels()
//
//    val profileViewModel : ProfileViewModel by activityViewModels()
//
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        isCancelable = false
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        editProfilePhotoBinding = DataBindingUtil.inflate(inflater,R.layout.edit_profile_photo,container,false)
//        editProfilePhotoBinding.editProfilePhoto=editProfilePhotoViewModel
//        editProfilePhotoBinding.lifecycleOwner=viewLifecycleOwner
//
//
//        editProfilePhotoViewModel.isLeave.observe(viewLifecycleOwner){ isLeave ->
//            if(isLeave){
//                dismiss()
//            }
//        }
//
//        editProfilePhotoViewModel.isDelProfilePhoto.observe(viewLifecycleOwner){ isDel ->
//            if(isDel){
//                profileViewModel._isDelProfilePhoto.value =true
//            }
//        }
//
////        editProfilePhotoViewModel.editResult.observe(viewLifecycleOwner){ res ->
////            when(res){
////                is EditProfilePhoto.gallery -> galleryLauncher.launch("image/*")
////                is EditProfilePhoto.camera -> {
////
////                }
////            }
////        }
//
//        return editProfilePhotoBinding.root
//    }
//}