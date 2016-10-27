package com.yalantis.filter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.yalantis.filter.R
import com.yalantis.filter.listener.CollapseListener
import com.yalantis.filter.model.Coord
import java.util.*

/**
 * Created by galata on 30.08.16.
 */
class ExpandedFilterView : ViewGroup {

    private var mPrevItem: View? = null
    private var mPrevX: Int? = null
    private var mPrevY: Int? = null
    private var mPrevHeight = 0
    private var mStartX = 0f
    private var mStartY = 0f

    internal var listener: CollapseListener? = null
    internal var margin: Int = dpToPx(getDimen(R.dimen.margin))
    internal val filters: LinkedHashMap<FilterItem, Coord> = LinkedHashMap()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes)

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        if (!filters.isEmpty()) {
            for (i in 0..childCount - 1) {
                val child: View = getChildAt(i)
                val coord: Coord? = filters[child]

                if (coord != null) {
                    child.layout(coord.x, coord.y, coord.x + child.measuredWidth, coord.y + child.measuredHeight)
                }
            }
        }
    }

    private fun canPlaceOnTheSameLine(filterItem: View): Boolean {
        if (mPrevItem != null) {
            val occupiedWidth: Int = mPrevX!! + mPrevItem!!.measuredWidth + margin + filterItem.measuredWidth

            return occupiedWidth <= measuredWidth
        }

        return false
    }

    private fun calculateDesiredHeight(): Int {
        var height: Int = mPrevHeight

        if (filters.isEmpty()) {
            for (i in 0..childCount - 1) {
                val child: FilterItem = getChildAt(i) as FilterItem

                child.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

                if (mPrevItem == null) {
                    mPrevX = margin
                    mPrevY = margin
                    height = child.measuredHeight + margin
                } else if (canPlaceOnTheSameLine(child)) {
                    mPrevX = mPrevX!! + mPrevItem!!.measuredWidth + margin / 2
                } else {
                    mPrevX = margin
                    mPrevY = mPrevY!! + mPrevItem!!.measuredHeight + margin / 2
                    height += child.measuredHeight + margin / 2
                }

                mPrevItem = child

                if (filters.size < childCount) {
                    filters.put(child, Coord(mPrevX!!, mPrevY!!))
                }
            }
            height = if (height > 0) height + margin else 0
            mPrevHeight = height
        }

        return mPrevHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(calculateSize(widthMeasureSpec, LayoutParams.MATCH_PARENT),
                calculateSize(heightMeasureSpec, calculateDesiredHeight()))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = event.x
                mStartY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.y - mStartY < -20) {
                    listener?.collapse()
                    mStartX = 0f
                    mStartY = 0f
                }
            }
        }

        return true
    }
}
