package com.example.expensetrackerapplication.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Patterns
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.expensetrackerapplication.SyncWorker
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Global {
    const val PAYMENT_TYPE_CASH=1
    const val PAYMENT_TYPE_CARD=2
    const val PAYMENT_TYPE_UPI=3
    const val PAYMENT_TYPE_SPLIT=4
    const val PAYMENT_TYPE_OTHER=5

    const val EXPENSE_STATUS_ADDED=0

    const val EXPENSE_STATUS_DELETED=1

    const val EXPENSE_STATUS_EDITED=2

    var lUserId =-1
    var lUserName=""

    var lUserPassword=""

    var lUserMobileNo =""

    var lUssrEmail = ""

    var sUserId =-1

    var sUserName=""

    var sUserPassword=""

    var sUserMobileNo =""

    var sUssrEmail = ""

    var isCalendarSelected : Boolean = false

    var isBottomSheetSelected : Boolean = false

    var displayDialogPrompt : Boolean = false

    val defaultCategories = listOf<String>("Food","Healthcare","Utilities","Groceries","Transportation")

    val COLOR_CODE1 = 1
    val COLOR_CODE2 = 2
    val COLOR_CODE3 = 3
    val COLOR_CODE4 = 4
    val COLOR_CODE5 = 5

    val THEME_DARK = 1

    val THEME_LIGHT = 2

    val THEME_SYSTEM = 3

    val DAY_WISE = 0

//    val WEEKLY_WISE = 1

    val MONTHLY_WISE = 1

    val YEARLY_WISE = 2


    var isDashboardSelected = false
    var isReportSelected = false
    var isAddExpenseSelected = false
    var isSettingsSelected = false
    var isProfileSelected = false

    var cloudUserId : String = ""

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun fnGetCurrentDate() : String {

//        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val currentDate = sdf.format(Date())

        return currentDate
    }

    fun fnGetCurrentDateUi() : String {

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val currentDate = sdf.format(Date())

        return currentDate
    }

    fun fnGetCurrentMonth() : String {

        val sdf = SimpleDateFormat("MM", Locale.getDefault())
        val currentDate = sdf.format(Date())

        return currentDate
    }

    fun fnGetCurrentYear() : String {

        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        return currentDate
    }

    fun fnGetCurrentTime() : String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date())
        return currentTime
    }

    fun fnRoundTheFloatingValues(amt : Float) : Float
    {
        return amt.toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toFloat()
    }

    fun fnFormatFloatTwoDigits(amt: Float?): String
    {
        val df = DecimalFormat("0.00")
        return df.format(amt)
    }

    fun fnHeaderFont(workBook: XSSFWorkbook) : Font {
        return workBook.createFont().apply {
            bold = true
            fontHeightInPoints = 15
            color = IndexedColors.MAROON.index
        }
    }

    fun fnSubHeaderFont(workBook: XSSFWorkbook) : Font {
        return workBook.createFont().apply {
            bold = true
            fontHeightInPoints = 15
            color = IndexedColors.OLIVE_GREEN.index
        }
    }


    fun fnSummaryFont(workBook: XSSFWorkbook) : Font {
        return workBook.createFont().apply {
            bold = true
            fontHeightInPoints = 13
            color = IndexedColors.DARK_GREEN.index
        }
    }

    fun fnSummaryStyle(workBook: XSSFWorkbook, summaryFont: Font) : XSSFCellStyle {
        return workBook.createCellStyle().apply {
            setFont(summaryFont)
            alignment= HorizontalAlignment.LEFT
        }
    }

    fun fnHeaderStyle(workBook: XSSFWorkbook, headerFont: Font) : XSSFCellStyle {
        return workBook.createCellStyle().apply {
            setFont(headerFont)
            alignment= HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }
    }

    fun fnTableHeaderStyle(workBook: XSSFWorkbook) : XSSFCellStyle {
        return  workBook.createCellStyle().apply {
            borderTop = BorderStyle.THICK
            borderBottom = BorderStyle.THICK
            borderLeft = BorderStyle.THICK
            borderRight = BorderStyle.THICK

            setBottomBorderColor(IndexedColors.BLACK.index)
            setTopBorderColor(IndexedColors.BLACK.index)
            setLeftBorderColor(IndexedColors.BLACK.index)
            setRightBorderColor(IndexedColors.BLACK.index)

            alignment = HorizontalAlignment.CENTER
            fillForegroundColor = IndexedColors.LAVENDER.index
//            fillForegroundColor = MaterialColors.getColor(
//                requireView(),
//                com.google.android.material.R.attr.colorOnPrimary
//            )
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workBook.createFont().apply { bold = true })
        }
    }


    fun fnTableDateStyle(workBook: XSSFWorkbook) : XSSFCellStyle {
        return workBook.createCellStyle().apply {
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN

            setBottomBorderColor(IndexedColors.BLACK.index)
            setTopBorderColor(IndexedColors.BLACK.index)
            setLeftBorderColor(IndexedColors.BLACK.index)
            setRightBorderColor(IndexedColors.BLACK.index)

            alignment = HorizontalAlignment.LEFT
        }
    }

    fun fnIsEmailValid(email: String?):Boolean
    {
        return !email.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun fnCopyImageToInternalStorage(context: Context, uri: Uri): String {

        val inputStream = context.contentResolver.openInputStream(uri)

        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        return file.absolutePath
    }

    fun startSyncWork(context: Context) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

        val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniqueWork(
            "sync_data",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

}