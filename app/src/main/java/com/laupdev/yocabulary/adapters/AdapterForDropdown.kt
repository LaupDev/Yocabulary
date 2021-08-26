package com.laupdev.yocabulary.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.laupdev.yocabulary.R

/*
* This class is used for fixing bug connected
* with AutoCompleteTextView in TextInputLayout
* that clears dropdown options after rotating device
* */
class AdapterForDropdown(context: Context, items: List<String>)
    : ArrayAdapter<String>(context, R.layout.view_pos_list_item, items) {

    private val noOpFilter = object : Filter() {
        private val noOpResult = FilterResults()
        override fun performFiltering(constraint: CharSequence?) = noOpResult
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
    }

    override fun getFilter() = noOpFilter
}