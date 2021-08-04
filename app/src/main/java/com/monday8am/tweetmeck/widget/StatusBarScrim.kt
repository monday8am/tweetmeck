package com.monday8am.tweetmeck.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets

class StatusBarScrim @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var lastWindowInsets: WindowInsets? = null

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (insets != lastWindowInsets) {
            lastWindowInsets = insets
            // Set this view as invisible if there isn't a top inset
            visibility = if (insets.systemWindowInsetTop > 0) VISIBLE else INVISIBLE
            // Request a layout to change size
            requestLayout()
        }
        return insets
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        if (hMode != MeasureSpec.EXACTLY) {
            val newHeightSpec = MeasureSpec.makeMeasureSpec(
                lastWindowInsets?.systemWindowInsetTop ?: 0,
                MeasureSpec.EXACTLY
            )
            super.onMeasure(widthMeasureSpec, newHeightSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}
