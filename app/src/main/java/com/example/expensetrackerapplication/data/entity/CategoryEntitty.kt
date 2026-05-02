package com.example.expensetrackerapplication.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Categories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["UserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices =[Index(value = ["UserId"])]
    )
data class CategoryEntitty(

    @ColumnInfo(name = "UserId")
    var userId : Int = 0,
    @ColumnInfo(name ="CloudId")
    var cloudId : String = "",
    @ColumnInfo(name="IsSynced")
    var isSynced : Int=0,
    @PrimaryKey(autoGenerate = true)
    var categoryId : Int=0,
    @ColumnInfo(name = "SignUpDate")
    var signUpDate : String="",
    @ColumnInfo(name = "CategoryName")
    var categoryName : String?=""

)
