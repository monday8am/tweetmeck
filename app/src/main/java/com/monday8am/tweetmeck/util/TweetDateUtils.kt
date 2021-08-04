package com.monday8am.tweetmeck.util

import android.content.Context
import android.text.format.DateUtils
import com.monday8am.tweetmeck.R
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TweetDateUtils {

    private val DATE_TIME_RFC822 = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
    private const val INVALID_DATE: Long = -1

    fun apiTimeToLong(apiTime: String): Long {
        return try {
            DATE_TIME_RFC822.parse(apiTime)?.time ?: INVALID_DATE
        } catch (e: ParseException) {
            INVALID_DATE
        }
    }

    fun isValidTimestamp(timestamp: String): Boolean {
        return apiTimeToLong(timestamp) != INVALID_DATE
    }

    /**
     * @return the given timestamp with a prepended "•"
     */
    fun dotPrefix(timestamp: String): String {
        return "• $timestamp"
    }

    /**
     * @param context resource
     * @param currentTimeMillis timestamp for offset
     * @param timestamp timestamp
     * @return the relative time string
     */
    fun getRelativeTimeString(context: Context, currentTimeMillis: Long, timestamp: Long): String {
        val diff = currentTimeMillis - timestamp
        if (diff >= 0) {
            when {
                diff < DateUtils.MINUTE_IN_MILLIS -> { // Less than a minute ago
                    val secs = (diff / 1000).toInt()
                    return context.resources.getQuantityString(R.plurals.tw__time_secs, secs, secs)
                }
                diff < DateUtils.HOUR_IN_MILLIS -> { // Less than an hour ago
                    val mins = (diff / DateUtils.MINUTE_IN_MILLIS).toInt()
                    return context.resources.getQuantityString(R.plurals.tw__time_mins, mins, mins)
                }
                diff < DateUtils.DAY_IN_MILLIS -> { // Less than a day ago
                    val hours = (diff / DateUtils.HOUR_IN_MILLIS).toInt()
                    return context.resources.getQuantityString(R.plurals.tw__time_hours, hours, hours)
                }
                else -> {
                    val now = Calendar.getInstance()
                    now.timeInMillis = currentTimeMillis
                    val c = Calendar.getInstance()
                    c.timeInMillis = timestamp
                    val d = Date(timestamp)

                    return if (now.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
                        // Same year
                        formatShortDateString(context, d)
                    } else {
                        // Outside of our year
                        formatLongDateString(context, d)
                    }
                }
            }
        }
        return formatLongDateString(context, Date(timestamp))
    }

    private fun formatLongDateString(context: Context, date: Date): String {
        return getDateFormat(context, R.string.tw__relative_date_format_long).format(date)
    }

    private fun formatShortDateString(context: Context, date: Date): String {
        return getDateFormat(context, R.string.tw__relative_date_format_short).format(date)
    }

    private fun getDateFormat(context: Context, patternId: Int): DateFormat {
        return SimpleDateFormat(context.getString(patternId), Locale.getDefault())
    }
}
