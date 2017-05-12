package com.yalantis.filter.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yalantis.filter.R
import com.yalantis.filter.listener.FilterItemListener
import kotlinx.android.synthetic.main.item_filter.view.*
import java.io.Serializable

/**
 * Created by galata on 30.08.16.
 */
class FilterItem : FrameLayout, Serializable {

    var isFilterSelected: Boolean = false
    var isIncreased: Boolean = false
    var startX: Float = 0f
    var startY: Float = 0f
    @ColorInt var cancelIconTint: Int = android.R.color.white
    @DrawableRes var cancelIcon: Int = R.drawable.ic_cancel
    @ColorInt var color: Int? = null
    @ColorInt var checkedColor: Int? = null
    @ColorInt var strokeColor: Int? = null
    @ColorInt var checkedTextColor: Int? = null
    @ColorInt var textColor: Int? = null
    var typeface: Typeface? = null
        set(value) {
            textView.typeface = value
        }
    var text: String
        get() = textView.text.toString()
        set(value) {
            mText = value
            textView.text = value
        }

    var circlePosition: Float = 0f
        get() = (textBackground.width / 2 + 1).toFloat()

    var collapsedSize: Int = 0
        get() = viewLeft.width

    var cornerRadius: Float = 100f
        set(value) {
            field = value
            updateBackground()
        }
    internal var fullSize: Int = 0
    internal var listener: FilterItemListener? = null

    private var mText: String? = null
    private var mStrokeWidth: Int = dpToPx(1.25f)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleRes: Int) : super(context, attrs, defStyleRes)

    init {
        LayoutInflater.from(context).inflate(R.layout.item_filter, this, true)
        viewLeft.setOnClickListener {
            if (isIncreased) {
                if (isFilterSelected) {
                    deselect()
                } else {
                    select()
                }
            } else {
                dismiss()
            }
        }
        viewRight.setOnClickListener { viewLeft.performClick() }
        textBackground.setOnClickListener { textView.performClick() }
        textView.setOnClickListener {
            if (isIncreased) {
                if (isFilterSelected) {
                    deselect()
                } else {
                    select()
                }
            }
        }

        buttonCancel.setOnClickListener {
            if (!isIncreased) {
                dismiss()
            } else {
                viewLeft.performClick()
            }
        }
        buttonCancel.supportBackgroundTintList = ColorStateList.valueOf(getColor(cancelIconTint))
        isIncreased = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        buttonCancel.setBackgroundResource(cancelIcon)
        if (fullSize == 0) {
            fullSize = measuredWidth
        }
    }

    fun select(notify: Boolean = true) {
        isIncreased = true
        isFilterSelected = true
        updateView()

        if (notify) {
            listener?.onItemSelected(this)
        }
    }

    fun select() = select(true)

    fun deselect() = deselect(true)

    fun deselect(notify: Boolean = true) {
        isFilterSelected = false
        updateView()

        if (notify) {
            listener?.onItemDeselected(this)
        }
    }

    fun dismiss() {
        listener?.onItemRemoved(this)
    }

    fun decrease(ratio: Float) {
        textView.scaleX = 1 - 0.2f * ratio
        textView.alpha = 1 - ratio
        buttonCancel.alpha = ratio
        textBackground.scaleX = 1 - ratio
        viewLeft.translationX = circlePosition * ratio
        viewRight.translationX = -circlePosition * ratio

        if (ratio == 0f) {
            buttonCancel.visibility = View.VISIBLE
            buttonCancel.alpha = 0f
        }

        if (ratio == 1f) {
            textView.scaleX = 0f
        }

        isIncreased = false
    }

    fun increase(ratio: Float) {
        textView.scaleX = 1f
        textView.alpha = ratio
        buttonCancel.alpha = 1 - alpha
        textBackground.scaleX = ratio
        viewLeft.translationX = circlePosition * (1 - ratio)
        viewRight.translationX = -circlePosition * (1 - ratio)

        if (ratio == 1f) {
            buttonCancel.visibility = View.GONE
            fullSize = measuredWidth
        }

        isIncreased = true
    }

    private fun updateView() {
        updateTextColor()
        updateBackground()
    }

    private fun updateTextColor() {
        @ColorInt val color: Int? = if (isFilterSelected) checkedTextColor else textColor

        if (color != null) {
            textView.setTextColor(color)
        }
    }

    private fun updateBackground() {
        @ColorInt var color: Int? = if (isFilterSelected) checkedColor else color
        color = removeAlpha(color)
        val strokeColor = if (isFilterSelected) color else removeAlpha(strokeColor)

        val drawable: GradientDrawable = GradientDrawable()
        drawable.cornerRadius = cornerRadius

        if (color != null) {
            drawable.setColor(color)
            textBackground.setBackgroundColor(color)
        } else {
            drawable.setColor(getColor(android.R.color.white))
            textBackground.setBackgroundColor(getColor(android.R.color.white))
        }

        if (strokeColor != null) {
            drawable.setStroke(mStrokeWidth, strokeColor)
            topStroke.setBackgroundColor(strokeColor)
            bottomStroke.setBackgroundColor(strokeColor)
        }

        viewLeft.setBackgroundDrawable(drawable)
        viewRight.setBackgroundDrawable(drawable)

    }

    private fun getColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    private fun removeAlpha(@ColorInt color: Int?): Int? = color?.or(0xff000000.toInt())

    fun removeFromParent() {
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = when (parent) {
        is CollapsedFilterView -> false
        else -> super.onInterceptTouchEvent(ev)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FilterItem) return false

        if (mText != other.mText) return false

        return true
    }

    override fun hashCode(): Int {
        return mText?.hashCode() ?: 0
    }


}