package com.example.expensetrackerapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetrackerapplication.data.dao.CategoryDao
import com.example.expensetrackerapplication.data.dao.ExpenseDao
import com.example.expensetrackerapplication.data.dao.IncomeDao
import com.example.expensetrackerapplication.data.dao.UserDao
import com.example.expensetrackerapplication.data.entity.CategoryEntitty
import com.example.expensetrackerapplication.data.entity.ExpenseEntity
import com.example.expensetrackerapplication.data.entity.IncomeEntity
import com.example.expensetrackerapplication.data.entity.UserEntity
import com.example.expensetrackerapplication.data.repositary.ExpenseRepository

@Database(entities = [UserEntity::class, CategoryEntitty :: class, ExpenseEntity :: class, IncomeEntity :: class],
    version = 15,
    exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun CategoryDao() : CategoryDao

    abstract fun ExpenseDao() : ExpenseDao

    abstract fun IncomeDao() : IncomeDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getdatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,"App_Database")
                    .addCallback(object : RoomDatabase.Callback(){
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys=ON")
                        }
                    })
                    .fallbackToDestructiveMigration(true) // true = drop all tables on migration mismatch
                    .build()
                INSTANCE=instance
                instance
            }
        }
    }


}