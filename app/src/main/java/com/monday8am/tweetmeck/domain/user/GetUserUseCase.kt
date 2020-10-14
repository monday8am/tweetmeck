package com.monday8am.tweetmeck.domain.user

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.di.IoDispatcher
import com.monday8am.tweetmeck.domain.SuspendUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Like Tweet use case
 */
open class GetUserUseCase @Inject constructor(
    private val db: TwitterDatabase,
    @IoDispatcher defaultDispatcher: CoroutineDispatcher
) : SuspendUseCase<String, TwitterUser>(defaultDispatcher) {

    override suspend fun execute(parameters: String): TwitterUser {
        return db.twitterUserDao()
            .getItemByScreenName(parameters) ?: throw Exception("Item not found with screenName: $parameters")
    }
}
