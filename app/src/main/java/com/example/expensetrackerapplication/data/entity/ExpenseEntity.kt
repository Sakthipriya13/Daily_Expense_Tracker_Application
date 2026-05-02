package com.example.expensetrackerapplication.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ExpenseTable",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["UserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["UserId"])]
    )
data class ExpenseEntity(

    @ColumnInfo(name = "UserId")
    var userId : Int =0,

    @ColumnInfo(name="CloudId")
    var cloudId : String = "",

    @ColumnInfo(name="IsSynced")
    var isSynced : Int=0,

    @PrimaryKey(autoGenerate = true)
    var expenseId:Int =0,

    @ColumnInfo(name = "ExpenseDate")
    var expenseDate : String="",

    @ColumnInfo(name = "ExpenseAmountt")
    var expenseAmt : Float=0.00f,

    @ColumnInfo(name="ExpenseCategoryId")
    var expenseCategoryId: Int=0,

    @ColumnInfo(name="ExpenseCategoryName")
    var expenseCategoryName: String?="",

    @ColumnInfo(name="PaymentType")
    var paymentType: Int=0,

    @ColumnInfo(name="ExpenseAmtInCash")
    var expenseAmtInCash: Float=0.00f,

    @ColumnInfo(name="ExpenseAmtInCard")
    var expenseAmtInCard : Float=0.00f,

    @ColumnInfo(name="ExpenseAmtInUpi")
    var expenseAmtInUpi : Float=0.00f,

    @ColumnInfo(name="ExpenseAmtInOthers")
    var expenseAmtInOthers : Float=0.00f,

    @ColumnInfo(name = "ExpenseRemarks")
    var expenseRemarks:String="",

    @ColumnInfo(name = "ExpenseStatus")
    var expenseStatus:Int =0
)
