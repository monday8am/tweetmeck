package com.monday8am.tweetmeck

import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.UiTweet
import com.monday8am.tweetmeck.data.models.UiUser
import org.junit.Assert.assertEquals
import org.junit.Test

class TweetTest {

    @Test
    fun `test retweet to regular tweet`() {
        val tweet = getTweet()
        assertEquals(tweet.uiContent.retweeted, false)
        assertEquals(tweet.uiContent.retweetCount, 0)

        val newTweet = tweet.setRetweeted(true)
        assertEquals(newTweet.uiContent.retweeted, true)
        assertEquals(newTweet.uiContent.retweetCount, 1)
    }

    @Test
    fun `test set tweet as favorited`() {
        val tweet = getTweet()
        assertEquals(tweet.main.favorited, false)
        assertEquals(tweet.main.favoriteCount, 0)

        val newTweet = tweet.setFavorite(true)
        assertEquals(newTweet.main.favorited, true)
        assertEquals(newTweet.main.favoriteCount, 1)
    }

    @Test
    fun `test set tweet as retweeted`() {
        val tweet = getTweet()
        assertEquals(tweet.main.retweeted, false)
        assertEquals(tweet.main.retweetCount, 0)

        val newTweet = tweet.setRetweeted(true)
        assertEquals(newTweet.main.retweeted, true)
        assertEquals(newTweet.main.retweetCount, 1)
        assertEquals(newTweet.uiContent.retweeted, true)
        assertEquals(newTweet.uiContent.retweetCount, 1)
    }

    @Test
    fun `undo tweet set as favorited`() {
        val tweet = getTweet(alreadyFavorited = true)
        assertEquals(tweet.main.favorited, true)
        assertEquals(tweet.main.favoriteCount, 1)

        val newTweet = tweet.setFavorite(false)
        assertEquals(newTweet.main.favorited, false)
        assertEquals(newTweet.main.favoriteCount, 0)
    }

    @Test
    fun `undo tweet set as retweeted`() {
        val tweet = getTweet(alreadyRetweeted = true)
        assertEquals(tweet.main.retweeted, true)
        assertEquals(tweet.main.retweetCount, 1)

        val newTweet = tweet.setRetweeted(false)
        assertEquals(newTweet.main.retweeted, false)
        assertEquals(newTweet.main.retweetCount, 0)
    }

    @Test
    fun `test set tweet with retweeted content as favorite`() {
        val tweet = getTweet(withRetweeted = true)
        assertEquals(tweet.retweet?.favorited, false)
        assertEquals(tweet.retweet?.favoriteCount, 0)

        val newTweet = tweet.setFavorite(true)
        assertEquals(newTweet.retweet?.favorited, true)
        assertEquals(newTweet.retweet?.favoriteCount, 1)
    }

    @Test
    fun `retweet an already retweeted tweet`() {
        val tweet = getTweet(withRetweeted = true)
        assertEquals(tweet.retweet?.retweeted, false)
        assertEquals(tweet.retweet?.retweetCount, 0)

        val newTweet = tweet.setRetweeted(true)
        assertEquals(newTweet.retweet?.retweeted, true)
        assertEquals(newTweet.retweet?.retweetCount, 1)
    }

    @Test
    fun `undo a set tweet with retweeted content as favorite`() {
        val tweet = getTweet(withRetweeted = true, alreadyFavorited = true)
        assertEquals(tweet.retweet?.favorited, true)
        assertEquals(tweet.retweet?.favoriteCount, 1)

        val newTweet = tweet.setFavorite(false)
        assertEquals(newTweet.retweet?.favorited, false)
        assertEquals(newTweet.retweet?.favoriteCount, 0)
    }

    @Test
    fun `undo a retweet for an already rewtweeted tweet`() {
        val tweet = getTweet(withRetweeted = true, alreadyRetweeted = true)
        assertEquals(tweet.retweet?.retweeted, true)
        assertEquals(tweet.retweet?.retweetCount, 1)

        val newTweet = tweet.setRetweeted(false)
        assertEquals(newTweet.retweet?.retweeted, false)
        assertEquals(newTweet.retweet?.retweetCount, 0)
    }

    private fun getTweet(
        withRetweeted: Boolean = false,
        withQuoted: Boolean = false,
        alreadyRetweeted: Boolean = false,
        alreadyFavorited: Boolean = false
    ): Tweet {
        return Tweet(
            id = 1L,
            main = getUiTweet(1L, isRetweeted = alreadyRetweeted, isFavorited = alreadyFavorited),
            retweet = if (withRetweeted) getUiTweet(2L, isRetweeted = alreadyRetweeted, isFavorited = alreadyFavorited) else null,
            quote = if (withQuoted) getUiTweet(3L) else null,
            listId = 1,
            inReplyToScreenName = null,
            inReplyToStatusId = null,
            inReplyToUserId = null,
            truncated = false,
            source = "android"
        )
    }

    private fun getUiTweet(
        id: Long,
        isRetweeted: Boolean = false,
        isFavorited: Boolean = false
    ): UiTweet {
        return UiTweet(
            id = id,
            createdAt = 123,
            fullText = "",
            urlEntities = listOf(),
            mediaEntities = listOf(),
            user = getUiUser(id),
            favorited = isFavorited,
            favoriteCount = if (isFavorited) 1 else 0,
            retweeted = isRetweeted,
            retweetCount = if (isRetweeted) 1 else 0
        )
    }

    private fun getUiUser(id: Long): UiUser {
        return UiUser(
            id = id,
            name = "anton",
            profileImageUrl = "profileImage",
            screenName = "angel_anton",
            verified = true
        )
    }
}
