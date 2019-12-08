package com.monday8am.tweetmeck.util

import android.content.res.Resources
import android.text.format.DateUtils
import com.monday8am.tweetmeck.R
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


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
     * @param res resource
     * @param currentTimeMillis timestamp for offset
     * @param timestamp timestamp
     * @return the relative time string
     */
    fun getRelativeTimeString(res: Resources, currentTimeMillis: Long, timestamp: Long): String {
        val diff = currentTimeMillis - timestamp
        if (diff >= 0) {
            when {
                diff < DateUtils.MINUTE_IN_MILLIS -> { // Less than a minute ago
                    val secs = (diff / 1000).toInt()
                    return res.getQuantityString(R.plurals.tw__time_secs, secs, secs)
                }
                diff < DateUtils.HOUR_IN_MILLIS -> { // Less than an hour ago
                    val mins = (diff / DateUtils.MINUTE_IN_MILLIS).toInt()
                    return res.getQuantityString(R.plurals.tw__time_mins, mins, mins)
                }
                diff < DateUtils.DAY_IN_MILLIS -> { // Less than a day ago
                    val hours = (diff / DateUtils.HOUR_IN_MILLIS).toInt()
                    return res.getQuantityString(R.plurals.tw__time_hours, hours, hours)
                }
                else -> {
                    val now = Calendar.getInstance()
                    now.timeInMillis = currentTimeMillis
                    val c = Calendar.getInstance()
                    c.timeInMillis = timestamp
                    val d = Date(timestamp)

                    return if (now.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
                        // Same year
                        formatShortDateString(res, d)
                    } else {
                        // Outside of our year
                        formatLongDateString(res, d)
                    }
                }
            }
        }
        return formatLongDateString(res, Date(timestamp))
    }

    private fun formatLongDateString(res: Resources, date: Date): String {
        return getDateFormat(res, R.string.tw__relative_date_format_long).format(date)
}

    private fun formatShortDateString(res: Resources, date: Date): String {
        return getDateFormat(res, R.string.tw__relative_date_format_short).format(date)
    }

    private fun getDateFormat(res: Resources, patternId: Int): DateFormat {
        return SimpleDateFormat(res.getString(patternId), Locale.getDefault())
    }
}
