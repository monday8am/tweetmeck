package com.monday8am.tweetmeck.domain.tweet

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.StatusToTweet
import com.monday8am.tweetmeck.data.mappers.asLambda
import com.monday8am.tweetmeck.data.mappers.mapWith
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.domain.SuspendUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Retweet Tweet use case
 */
open class RetweetUseCase constructor(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<Pair<Tweet, Session>, Unit>(defaultDispatcher) {

    override suspend fun execute(parameters: Pair<Tweet, Session>) {
        val (tweet, session) = parameters
        val newTweet = remoteClient.retweetTweet(tweet.id, !tweet.uiContent.retweeted, session)
            .mapWith(StatusToTweet(tweet.listId).asLambda())
        val existingTweets = db.tweetDao().getRelatedTweets(tweet.uiContent.id)
            .map { it.setRetweeted(!tweet.uiContent.retweeted) }
            .toMutableList()
        val retweetUndone = tweet.uiContent.retweeted
        if (retweetUndone) {
            val tweetedByMe = existingTweets.find { it.main.user.id == session.userId }
            if (tweetedByMe != null) {
                existingTweets.removeIf { it.id == tweetedByMe.id }
                db.tweetDao().deleteAndUpdateTweets(tweetedByMe.id, existingTweets)
            }
        } else {
            db.tweetDao().insert(existingTweets)
            db.tweetDao().insert(newTweet)
        }
    }
}
