package com.monday8am.tweetmeck.widget

import android.content.Context
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

class SpannedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    init {
        linksClickable = true
        movementMethod = LinkMovementMethod.getInstance()
    }

    override fun hasFocusable() = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val spannable = Spannable.Factory.getInstance().newSpannable(text)
        val pressedSpan = getPressedSpan(this, spannable, event) ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Selection.setSelection(spannable, spannable.getSpanStart(pressedSpan),
                    spannable.getSpanEnd(pressedSpan))
            }
            MotionEvent.ACTION_UP -> {
                pressedSpan.onClick(this)
                Selection.removeSelection(spannable)
            }
            else -> {
                Selection.removeSelection(spannable)
                super.onTouchEvent(event)
            }
        }
        return true
    }

    private fun getPressedSpan(widget: TextView, spannable: Spannable, event: MotionEvent): ClickableSpan? {
        var x = event.x.toInt()
        var y = event.y.toInt()
        x -= widget.totalPaddingLeft
        y -= widget.totalPaddingTop
        x += widget.scrollX
        y += widget.scrollY

        val layout = widget.layout
        val line = layout.getLineForVertical(y)

        val off = try {
            layout.getOffsetForHorizontal(line, x.toFloat())
        } catch (e: IndexOutOfBoundsException) {
            return null
        }

        val end = layout.getLineEnd(line)
        if (off != end && off != end - 1) {
            val link = spannable.getSpans(off, off, ClickableSpan::class.java)
            if (link.isNotEmpty())
                return link[0]
        }
        return null
    }
}
