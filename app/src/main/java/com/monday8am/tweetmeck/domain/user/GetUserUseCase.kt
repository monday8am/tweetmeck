package com.monday8am.tweetmeck.domain.user

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.domain.SuspendUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Like Tweet use case
 */
open class GetUserUseCase constructor(
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<String, TwitterUser>(defaultDispatcher) {

    override suspend fun execute(parameters: String): TwitterUser {
        return db.twitterUserDao()
            .getItemByScreenName(parameters) ?: throw Exception("Item not found with screenName: $parameters")
    }
}
