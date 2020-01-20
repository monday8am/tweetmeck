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
 * Like Tweet use case
 */
open class LikeTweetUseCase constructor(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<Pair<Tweet, Session>, Unit>(defaultDispatcher) {

    override suspend fun execute(parameters: Pair<Tweet, Session>) {
        val (tweet, session) = parameters
        val response = remoteClient.likeTweet(tweet.id, !tweet.uiContent.favorited, session)
            .mapWith(StatusToTweet(tweet.listId).asLambda())
        db.tweetDao().insert(tweet.setFavorite(response.uiContent.favorited))
    }
}
