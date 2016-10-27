package com.yalantis.filter.widget

import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup

/**
 * Created by galata on 13.09.16.
 */

internal fun calculateSize(measureSpec: Int, desiredSize: Int): Int {
    val mode = View.MeasureSpec.getMode(measureSpec)
    val size = View.MeasureSpec.getSize(measureSpec)

    val actualSize = when (mode) {
        View.MeasureSpec.EXACTLY -> size
        View.MeasureSpec.AT_MOST -> Math.min(desiredSize, size)
        else -> desiredSize
    }

    return actualSize
}

internal fun ViewGroup.dpToPx(dp: Int): Int {
    val displayMetrics = context.resources.displayMetrics
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

internal fun ViewGroup.dpToPx(dp: Float): Int {
    val displayMetrics = context.resources.displayMetrics
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

internal fun ViewGroup.getDimen(res: Int): Int = context.resources.getDimensionPixelOffset(res)

internal fun calculateX(position: Int, size: Int, minMargin: Int, itemSize: Int): Int {
    val realMargin = calculateMargin(size, itemSize, minMargin)
    return position * itemSize + position * realMargin + realMargin
}

internal fun calculateMargin(width: Int, itemWidth: Int, margin: Int): Int {
    val count = calculateCount(width, itemWidth, margin)
    return if (count > 0) (width - count * itemWidth) / count else 0
}

internal fun calculateCount(width: Int, itemWidth: Int, margin: Int): Int = width / (itemWidth + margin)

internal fun isClick(startX: Float, startY: Float, x: Float, y: Float): Boolean = Math.abs(x - startX) < 20 && Math.abs(y - startY) < 20