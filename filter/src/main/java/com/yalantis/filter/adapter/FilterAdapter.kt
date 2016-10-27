package com.yalantis.filter.adapter

import com.yalantis.filter.widget.FilterItem

/**
 * Created by galata on 30.08.16.
 */
abstract class FilterAdapter<T>(open val items: List<T>) {

    abstract fun createView(position: Int, item: T): FilterItem

}