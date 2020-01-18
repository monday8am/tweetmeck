package com.monday8am.tweetmeck.domain.tweet

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.models.ModelId
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.domain.SuspendUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Like Tweet use case
 */
open class GetTweetUseCase constructor(
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<ModelId, Tweet>(defaultDispatcher) {

    override suspend fun execute(parameters: ModelId): Tweet {
        return db.tweetDao().getItemById(parameters) ?: throw Exception("Item not found with id: $parameters")
    }
}
