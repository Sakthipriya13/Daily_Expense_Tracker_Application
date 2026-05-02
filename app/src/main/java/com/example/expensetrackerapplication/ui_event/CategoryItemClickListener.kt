package com.example.expensetrackerapplication.ui_event

import com.example.expensetrackerapplication.model.CategoryModel

interface CategoryItemClickListener {
    fun onRemoveClick(category : CategoryModel)
}