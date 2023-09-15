package com.kash4me.utils

import android.widget.Filter

abstract class CustomFilter<T>(private val mItems: List<T>) : Filter() {

    override fun performFiltering(query: CharSequence?): FilterResults {

        val result = arrayListOf<T>()

        val userQuery = query.toString()
        if (userQuery.isBlank()) {

            result.addAll(mItems)

        } else {

            val filteredResult = getFilteredResult(userQuery)
            result.addAll(filteredResult)

        }

        return FilterResults().apply {
            count = result.size
            values = result
        }

    }

    private fun getFilteredResult(query: String): ArrayList<T> {

        val result = arrayListOf<T>()

        mItems.forEach { item ->
            if (matchesCriteria(query, item)) result.add(item)
        }

        return result

    }

    abstract fun matchesCriteria(userQuery: String, item: T): Boolean

    abstract override fun publishResults(userQuery: CharSequence?, results: FilterResults?)

}