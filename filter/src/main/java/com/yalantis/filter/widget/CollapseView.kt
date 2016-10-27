package com.yalantis.filter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.yalantis.filter.R
import kotlinx.android.synthetic.main.view_collapse.view.*

/**
 * Created by galata on 20.09.16.
 */
class CollapseView : FrameLayout {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_collapse, this, true)
    }

    internal fun setText(text: String) {
        buttonOk.text = text
    }

    internal fun setHasText(hasText: Boolean) {
        buttonOk.visibility = if (hasText) View.VISIBLE else GONE
    }

    internal fun rotateArrow(rotation: Float): Unit {
        imageArrow.rotation = rotation
    }

    internal fun turnIntoOkButton(ratio: Float) {
        if (buttonOk.visibility != View.VISIBLE) return
        scale(getIncreasingScale(ratio), getDecreasingScale(ratio))
    }

    internal fun turnIntoArrow(ratio: Float) {
        if (buttonOk.visibility != View.VISIBLE) return
        scale(getDecreasingScale(ratio), getIncreasingScale(ratio))
    }

    private fun getIncreasingScale(ratio: Float): Float = if (ratio < 0.5f) 0f else 2 * ratio - 1

    private fun getDecreasingScale(ratio: Float): Float = if (ratio > 0.5f) 0f else 1 - 2 * ratio

    private fun scale(okScale: Float, arrowScale: Float) {
        buttonOk.scaleX = okScale
        buttonOk.scaleY = okScale
        imageArrow.scaleX = arrowScale
        imageArrow.scaleY = arrowScale
    }

    override fun setOnClickListener(l: OnClickListener?) {
        buttonOk.setOnClickListener(l)
        imageArrow.setOnClickListener(l)
    }

}