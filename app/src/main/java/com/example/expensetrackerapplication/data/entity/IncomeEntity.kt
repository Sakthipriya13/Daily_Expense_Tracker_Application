package com.example.expensetrackerapplication.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "IncomeTable",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity :: class,
            parentColumns = ["userId"],
            childColumns = ["UserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["UserId"])])
data class IncomeEntity(

    @ColumnInfo(name = "UserId")
    var userId : Int=0,

    @ColumnInfo(name ="CloudId")
    var cloudId : String = "",

    @ColumnInfo(name="IsSynced")
    var isSynced : Int=0,

    @PrimaryKey(autoGenerate = true)
    var incomeId : Int=0,

    @ColumnInfo(name="Date")
    var date : String?="",

    @ColumnInfo(name="Income")
    var income : Float?=0.00f,



)
