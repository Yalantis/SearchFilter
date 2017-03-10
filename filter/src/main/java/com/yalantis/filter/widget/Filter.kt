package com.yalantis.filter.widget

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.yalantis.filter.Constant
import com.yalantis.filter.R
import com.yalantis.filter.adapter.FilterAdapter
import com.yalantis.filter.listener.CollapseListener
import com.yalantis.filter.listener.FilterItemListener
import com.yalantis.filter.listener.FilterListener
import com.yalantis.filter.model.Coord
import com.yalantis.filter.model.FilterModel
import kotlinx.android.synthetic.main.collapsed_container.view.*
import kotlinx.android.synthetic.main.filter.view.*
import java.io.Serializable
import java.util.*
import android.content.res.TypedArray
import android.graphics.Color
import android.support.annotation.ColorInt


/**
 * Created by galata on 08.09.16.
 */
class Filter<T : FilterModel> : FrameLayout, FilterItemListener, CollapseListener {

    var adapter: FilterAdapter<T>? = null
    var listener: FilterListener<T>? = null
    var margin = dpToPx(getDimen(R.dimen.margin))
    var noSelectedItemText: String = ""
        set(value) {
            collapsedText.text = value
        }
    var textToReplaceArrow: String = ""
        set(value) {
            collapseView.setText(value)
        }

    var replaceArrowByText: Boolean = false
        set(value) {
            collapseView.setHasText(value)
        }

    var collapsedBackground: Int = Color.WHITE
        set(value) {
            field = value
            collapsedContainer.containerBackground = value
            collapsedContainer.invalidate()
        }

    var expandedBackground: Int = Color.WHITE
        set(value) {
            field = value
            expandedFilter.setBackgroundColor(value)
            expandedFilter.invalidate()
        }

    private var mIsBusy = false

    private var isCollapsed: Boolean? = null

    private val STATE_SUPER = "state_super"
    private val STATE_SELECTED = "state_selected"
    private val STATE_REMOVED = "state_removed"
    private val STATE_COLLAPSED = "state_collapsed"

    private val mSelectedFilters: LinkedHashMap<FilterItem, Coord> = LinkedHashMap()
    private val mRemovedFilters: LinkedHashMap<FilterItem, Coord> = LinkedHashMap()
    private val mItems: LinkedHashMap<FilterItem, T> = LinkedHashMap()
    private var mSelectedItems: ArrayList<T> = ArrayList()
    private var mRemovedItems: ArrayList<T> = ArrayList()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.filter, this, true)
        collapseView.setOnClickListener { toggle() }
        collapsedFilter.scrollListener = this
        collapsedContainer.listener = this
        expandedFilter.listener = this
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.Filter, 0, 0)
        try {
            collapsedContainer.containerBackground = attributes.getColor(R.styleable.Filter_collapsedBackground, Color.WHITE)
            expandedFilter.setBackgroundColor(attributes.getColor(R.styleable.Filter_expandedBackground, Color.WHITE))
        } finally {
            attributes.recycle()
        }
    }

    fun build() {
        if (!validate()) {
            return
        }

        mItems.clear()
        expandedFilter.post {
            adapter?.items?.forEachIndexed { i, item ->
                val view: FilterItem = adapter?.createView(i, item)!!
                view.listener = this
                expandedFilter.addView(view)
                mItems.put(view, item)
            }
            if (isCollapsed == null) {
                collapse(1)
            }
        }
        expandedFilter.margin = margin
        collapsedFilter.margin = margin
    }

    private fun validate(): Boolean = adapter != null && adapter?.items != null && !adapter?.items?.isEmpty()!!

    override fun collapse() {
        collapse(Constant.ANIMATION_DURATION)
    }

    private fun collapse(duration: Long) {
        if (mIsBusy || collapsedFilter.isBusy) return
        mIsBusy = true
        mRemovedFilters.clear()

        isCollapsed = true

        removeItemsFromParent()
        container.bringToFront()
        container.requestFocus()

        ValueAnimator.ofFloat(0f, duration.toFloat()).setDuration(duration).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / duration

                collapseView.rotateArrow(180 * (1 - ratio))
                collapseView.turnIntoArrow(ratio)

                mSelectedFilters.keys.forEachIndexed { index, filterItem ->
                    val x = calculateX(index, collapsedFilterScroll.measuredWidth, margin, filterItem.collapsedSize)

                    filterItem.decrease(ratio)

                    if (index >= calculateCount(collapsedFilterScroll.measuredWidth, filterItem.collapsedSize, margin)) {
                        filterItem.alpha = 1 - ratio * 3
                    } else {
                        filterItem.translationX = filterItem.startX + (x - filterItem.startX
                                - filterItem.measuredWidth / 2 + filterItem.collapsedSize / 2) * ratio
                        filterItem.translationY = filterItem.startY + (dpToPx(getDimen(R.dimen.margin)).toFloat() / 4
                                - filterItem.startY) * ratio
                    }

                    if (ratio == 1f) {
                        filterItem.removeFromParent()
                        collapsedFilter.addView(filterItem)
                        filterItem.translationX = (x - filterItem.measuredWidth / 2 + filterItem.collapsedSize / 2).toFloat()
                        filterItem.translationY = dpToPx(getDimen(R.dimen.margin)).toFloat() / 4
                        filterItem.alpha = 1f
                        filterItem.bringToFront()
                    }
                }

                collapsedContainer.translationY = ratio * (-measuredHeight + collapsedContainer.height)
                dividerTop.alpha = 1 - 2 * ratio
                expandedFilterScroll.translationY = ratio * (-measuredHeight + collapsedContainer.height)

                if (mSelectedFilters.isEmpty()) {
                    collapsedText.visibility = View.VISIBLE
                    collapsedText.alpha = ratio
                } else {
                    collapsedText.visibility = View.GONE
                    collapsedText.alpha = 1 - ratio
                }

                if (ratio == 1f) {
                    collapsedContainer.bringToFront()
                    collapsedContainer.requestFocus()
                    mIsBusy = false
                }
            }
        }.start()

        notifyListener()
    }

    override fun expand() {
        if (collapsedFilter.isBusy || mIsBusy) return

        mIsBusy = true

        isCollapsed = false

        removeItemsFromParent()
        container.bringToFront()
        container.requestFocus()

        ValueAnimator.ofFloat(0f, Constant.ANIMATION_DURATION.toFloat()).setDuration(Constant.ANIMATION_DURATION).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / Constant.ANIMATION_DURATION

                collapseView.rotateArrow(180 * ratio)
                collapseView.turnIntoOkButton(ratio)

                mSelectedFilters.keys.forEachIndexed { index, filterItem ->
                    val x = mSelectedFilters[filterItem]?.x
                    val y = mSelectedFilters[filterItem]?.y

                    if (index < calculateCount(collapsedFilterScroll.measuredWidth, filterItem.collapsedSize, margin)) {
                        filterItem.translationX = filterItem.startX + (x!! - filterItem.startX) * ratio
                        filterItem.translationY = filterItem.startY + (y!! - filterItem.startY) * ratio
                    } else {
                        filterItem.translationX = x!!.toFloat()
                        filterItem.translationY = y!!.toFloat()
                        filterItem.alpha = ratio
                    }
                    filterItem.increase(ratio)

                    if (ratio == 1f) {
                        filterItem.removeFromParent()
                        expandedFilter.addView(filterItem)
                        filterItem.translationX = 0f
                        filterItem.translationY = 0f
                    }
                }

                mRemovedFilters.keys.forEach { filterItem ->
                    filterItem.alpha = ratio

                    filterItem.removeFromParent()
                    expandedFilter.addView(filterItem)
                    filterItem.translationX = mRemovedFilters[filterItem]?.x!! * (1 - ratio)
                    filterItem.translationY = mRemovedFilters[filterItem]?.y!! * (1 - ratio)
                }
                collapsedText.alpha = 1 - ratio
                dividerTop.alpha = 2 * ratio
                collapsedContainer.translationY = -container.height.toFloat() * (1 - ratio)
                expandedFilterScroll.translationY = -container.height.toFloat() * (1 - ratio)

                if (ratio == 1f) {
                    expandedFilterScroll.bringToFront()
                    expandedFilterScroll.requestFocus()
                    collapsedText.visibility = View.GONE
                    mIsBusy = false
                }
            }
        }.start()

        mRemovedFilters.keys.forEach { filterItem ->
            val x = mRemovedFilters[filterItem]?.x
            val y = mRemovedFilters[filterItem]?.y

            filterItem.translationX = x!!.toFloat()
            filterItem.translationY = y!!.toFloat()
            filterItem.increase(1f)
            filterItem.deselect()
        }
    }

    private fun removeItemsFromParent() {
        mSelectedFilters.keys.forEach { item ->
            remove(item)
        }
    }

    private fun remove(item: FilterItem) {
        val x = item.x
        val y = item.y
        item.removeFromParent()
        container.addView(item)
        item.translationX = x
        item.translationY = y
        item.startX = x
        item.startY = y
        item.bringToFront()
    }

    override fun onItemSelected(item: FilterItem) {
        val filter = mItems[item]!!
        if (mItems.contains(item)) {
            mSelectedItems.add(filter)
        }
        mSelectedFilters.put(item, Coord(item.x.toInt(), item.y.toInt()))
        listener?.onFilterSelected(filter)
    }

    override fun onItemDeselected(item: FilterItem) {
        val filter = mItems[item]!!
        if (mItems.contains(item)) {
            mSelectedItems.remove(filter)
        }
        mSelectedFilters.remove(item)
        listener?.onFilterDeselected(filter)
    }

    override fun onItemRemoved(item: FilterItem) {
        val coord = mSelectedFilters[item]
        if (coord != null && collapsedFilter.removeItem(item)) {
            mSelectedFilters.remove(item)
            mSelectedItems.remove(mItems[item])
            mRemovedFilters.put(item, coord)

            postDelayed({
                remove(item)

                if (mSelectedFilters.isEmpty()) {
                    collapsedText.visibility = View.VISIBLE
                    collapsedText.alpha = 1f
                }
            }, Constant.ANIMATION_DURATION / 2)

            notifyListener()
        }
    }

    private fun notifyListener() {
        if (mSelectedFilters.isEmpty()) {
            listener?.onNothingSelected()
        } else {
            listener?.onFiltersSelected(getSelectedItems())
        }
    }

    private fun getSelectedItems(): ArrayList<T> {
        val items: ArrayList<T> = ArrayList()
        mSelectedFilters.keys.forEach { filter ->
            val item: T? = mItems[filter]

            if (item != null) {
                items.add(item)
            }
        }

        return items
    }

    fun deselectAll() {
        mSelectedFilters.keys.forEach { item -> item.deselect(false) }
        mSelectedFilters.clear()
        mSelectedItems.clear()
        listener?.onNothingSelected()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return Bundle().apply {
            putParcelable(STATE_SUPER, superState)
            putBoolean(STATE_COLLAPSED, isCollapsed!!)
            val selected = mSelectedItems
            val removed = mRemovedItems
            if (selected is Serializable) {
                putSerializable(STATE_SELECTED, selected)
            }
            if (removed is Serializable) {
                putSerializable(STATE_REMOVED, removed)
            }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
            val selected = state.getSerializable(STATE_SELECTED) as? List<T>
            val removed = state.getSerializable(STATE_REMOVED) as? List<T>
            isCollapsed = state.getBoolean(STATE_COLLAPSED)
            if (selected is ArrayList<T> && removed is ArrayList<T>) {
                mSelectedItems = selected
                mRemovedItems = removed
                expandedFilter.post {
                    restore(expandedFilter.filters)
                }
            }
        }
    }

    private fun restore(filters: LinkedHashMap<FilterItem, Coord>) {
        mSelectedFilters.clear()
        expandedFilter.post {
            filters.keys.forEach { filterItem ->
                filters[filterItem]?.let { coord ->
                    val item = { item: T -> filterItem.text == item.getText() }

                    if (mSelectedItems.any(item)) {
                        mSelectedFilters.put(filterItem, coord)
                        filterItem.select(false)
                    } else if (mRemovedItems.any(item)) {
                        mRemovedFilters.put(filterItem, coord)
                        filterItem.deselect(false)
                    }
                }
            }

            if (isCollapsed == null || isCollapsed as Boolean) {
                collapse(1)
            } else {
                expand()
            }
        }
    }

    override fun toggle() {
        if (collapsedFilter.isBusy || mIsBusy) return

        if (isCollapsed != null && isCollapsed as Boolean) expand() else collapse()
    }

}
