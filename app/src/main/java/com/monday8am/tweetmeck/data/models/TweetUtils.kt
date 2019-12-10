package com.monday8am.tweetmeck.data.models

import android.net.Uri
import android.text.TextUtils
import com.monday8am.tweetmeck.data.models.entities.UrlEntity
import com.monday8am.tweetmeck.util.legacy.HtmlEntities
import java.util.*

object TweetUtils {

    private const val TWITTER_URL = "https://twitter.com/"
    private const val UNKNOWN_SCREEN_NAME = "twitter_unknown"
    private const val TWEET_URL = "$TWITTER_URL%s/status/%d"
    private const val HASHTAG_URL = TWITTER_URL + "hashtag/%s"
    private const val PROFILE_URL = "$TWITTER_URL%s"
    private const val SYMBOL_URL = TWITTER_URL + "search?q=%%24%s"

    /**
     * Builds a permalink url for the given screen name and Tweet id. If we don't have a
     * screen_name, use the constant UNKNOWN_SCREEN_NAME value and the app or the site will figure
     * out the redirect. The reason for using twitter_unknown is that only twitter official accounts
     * can have twitter in their screen name so we'd never be somehow pointing the user to something
     * potentially inflammatory (see twitter.com/unknown for an example).
     *
     * @param screenName The screen name to build the url with
     * @param tweetId    The id to build the url with
     * @return           Can be null, otherwise a resolvable permalink to a Tweet.
     */
    fun getPermalink(screenName: String, tweetId: Long): Uri? {
        if (tweetId <= 0) {
            return null
        }
        val permalink: String = if (TextUtils.isEmpty(screenName)) {
            String.format(Locale.US, TWEET_URL, UNKNOWN_SCREEN_NAME, tweetId)
        } else {
            String.format(Locale.US, TWEET_URL, screenName, tweetId)
        }
        return Uri.parse(permalink)
    }

    /**
     * Builds a permalink for the profile of a given screen name
     *
     * @param screenName The screen name to build the url with
     * @return           Can be null, otherwise a resolvable permalink to a Profile.
     */
    fun getProfilePermalink(screenName: String): String {
        return if (screenName.isEmpty()) {
            String.format(Locale.US, PROFILE_URL, UNKNOWN_SCREEN_NAME)
        } else {
            String.format(Locale.US, PROFILE_URL, screenName)
        }
    }

    /**
     * Builds a permalink for a hashtag entity
     * @param text
     * @return Formatted url string
     */
    fun getHashtagPermalink(text: String): String {
        return String.format(Locale.US, HASHTAG_URL, text)
    }

    /**
     * Builds a permalink for a symbol entity
     * @param text
     * @return Formatted url string
     */
    fun getSymbolPermalink(text: String): String {
        return String.format(Locale.US, SYMBOL_URL, text)
    }

    fun unescapeTweetContent(rawText: String): Pair<String, List<IntArray>> {
        val result = HtmlEntities.HTML40.unescape(rawText)
        return Pair(result.unescaped, result.indices)
    }

    fun getHighSurrogateIndices(content: String): List<Int> {
        val highSurrogateIndices = mutableListOf<Int>()
        val len = content.length - 1
        for (i in 0 until len) {
            if (Character.isHighSurrogate(content[i]) && Character.isLowSurrogate(content[i + 1])) {
                highSurrogateIndices.add(i)
            }
        }
        return highSurrogateIndices
    }
}

/**
 * Since the unescaping of html causes for example &amp; to turn into & we need to adjust
 * the entity indices after that by 4 characters. This loops through the entities and adjusts
 * them as necessary.
 *
 * @param indices The indices of where there were escaped html chars that we unescaped
 */
fun List<UrlEntity>.adjustIndicesForEscapedChars(indices: List<IntArray>): List<UrlEntity> {
    if (this.isEmpty() || indices.isEmpty())
        return this

    val size = indices.size
    var m = 0 // marker
    var diff = 0 // accumulated difference
    var inDiff: Int // end difference for escapes in range
    var len: Int // escaped length
    var start: Int // escaped start
    var end: Int // escaped end
    var i: Int // reusable index
    var index: IntArray
    // For each of the entities, update the start and end indices
    // Note: tweet entities are sorted.
    val result: MutableList<UrlEntity> = mutableListOf()

    for (entity in this) {
        inDiff = 0
        // Go through the escaped entities' indices
        i = m
        while (i < size) {
            index = indices[i]
            start = index[0]
            end = index[1]
            // len is actually (end - start + 1) - 1
            len = end - start
            if (end < entity.start) {
                // bump position of the next marker
                diff += len
                m++
            } else if (end < entity.end) {
                inDiff += len
            }
            i++
        }
        // Once we've accumulated diffs, calc the offset
        result.add(
            entity.copy(
                start = entity.start - (diff + inDiff),
                end =  entity.end - (diff + inDiff)
            )
        )
    }

    return result
}

fun List<UrlEntity>.adjustEntitiesWithOffsets(indices: List<Int>): List<UrlEntity> {
    if (this.isEmpty() || indices.isEmpty())
        return this

    val result: MutableList<UrlEntity> = mutableListOf()
    for (entity in this) {
        // find all indices <= start and update offsets by that much
        val start = entity.start
        var offset = 0
        for (index in indices) {
            if (index - offset <= start) {
                offset += 1
            } else {
                break
            }
        }
        result.add(
            entity.copy(
                start = entity.start + offset,
                end =  entity.end + offset
            )
        )
    }
    return result
}

fun List<UrlEntity>.sortByStartIndex(): List<UrlEntity> {
    return this.sortedBy { it.start }
}
