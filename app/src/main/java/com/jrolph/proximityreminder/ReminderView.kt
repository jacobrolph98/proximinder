package com.jrolph.proximityreminder

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class ReminderView(context: Context, attributes: AttributeSet?): FrameLayout(context, attributes) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, (measuredWidth * 1.4f).toInt())
    }
}