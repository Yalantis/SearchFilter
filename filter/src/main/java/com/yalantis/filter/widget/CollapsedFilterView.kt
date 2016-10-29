package com.yalantis.filter.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import com.yalantis.filter.Constant
import com.yalantis.filter.R
import com.yalantis.filter.listener.CollapseListener

/**
 * Created by galata on 08.09.16.
 */
class CollapsedFilterView : ViewGroup {

    internal var margin: Int = dpToPx(getDimen(R.dimen.margin))
    internal var isBusy = false
    internal var scrollListener: CollapseListener? = null

    private var mStartX = 0f
    private var mStartY = 0f
    private var mRealMargin: Int = margin

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes)

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        for (i in 0..childCount - 1) {
            val child: FilterItem = getChildAt(i) as FilterItem
            child.layout(0, 0, child.collapsedSize / 2 + child.measuredWidth / 2 + 1, child.measuredHeight)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount > 0) {
            val child: FilterItem = getChildAt(0) as FilterItem
            child.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            mRealMargin = calculateMargin((parent as ViewGroup).measuredWidth, child.collapsedSize, margin)

            val width = childCount * child.collapsedSize + childCount * mRealMargin + mRealMargin

            setMeasuredDimension(width, calculateSize(margin * 2 + child.collapsedSize, LayoutParams.MATCH_PARENT))
        } else {
            setMeasuredDimension(0, 0)
        }
    }

    internal fun removeItem(child: FilterItem): Boolean {
        if (isBusy) {
            return false
        }

        val index = indexOfChild(child)
        isBusy = true

        ValueAnimator.ofFloat(0f, Constant.ANIMATION_DURATION / 2.toFloat()).setDuration(Constant.ANIMATION_DURATION / 2).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / (Constant.ANIMATION_DURATION / 2)
                for (i in index + 1..childCount - 1) {
                    val item = getChildAt(i) as FilterItem

                    if (ratio == 0f) {
                        item.startX = item.x
                    }

                    item.translationX = item.startX + (-child.collapsedSize - mRealMargin) * ratio
                    child.alpha = 1 - ratio
                }

                if (ratio == 1f) {
                    child.translationX = child.startX + (-child.collapsedSize - mRealMargin) * ratio
                    isBusy = false
                }
            }
        }.start()
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = childCount > 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = event.x
                mStartY = event.y
            }
            MotionEvent.ACTION_UP -> {
                if (!isBusy && isClick(mStartX, mStartY, event.x, event.y)) {
                    findViewByCoord(event.x)?.dismiss()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(mStartX - event.x) < 20 && event.y - mStartY > 20) {
                    if (!isBusy) {
                        scrollListener?.expand()
                    }
                    mStartX = 0f
                    mStartY = 0f
                }
            }
        }

        return true
    }


    private fun findViewByCoord(x: Float): FilterItem? {
        for (i in 0..childCount - 1) {
            val item: FilterItem = getChildAt(i) as FilterItem

            if (containsCoord(item, x)) {
                return item
            }
        }

        return null
    }

    private fun containsCoord(item: FilterItem, x: Float): Boolean
            = item.x + item.fullSize / 2 - item.collapsedSize / 2 <= x && x <= item.x + item.fullSize / 2 + item.collapsedSize / 2

}