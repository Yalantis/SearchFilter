package com.yalantis.filter.listener

import java.util.*

/**
 * Created by galata on 18.09.16.
 */
interface FilterListener<T> {

    fun onFiltersSelected(filters: ArrayList<T>)

    fun onNothingSelected()

    fun onFilterSelected(item: T)

    fun onFilterDeselected(item: T)

}