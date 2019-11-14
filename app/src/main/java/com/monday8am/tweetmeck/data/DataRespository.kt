package com.monday8am.tweetmeck.data

import com.monday8am.tweetmeck.data.local.LocalStorageService
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DataRepository {
    suspend fun getLists()
}

class DefaultDataRepository(
    private val twitterClient: TwitterClient,
    private val localStorageService: LocalStorageService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataRepository {

    override suspend fun getLists() {
        return withContext(ioDispatcher) {
            twitterClient.getUserLists()
        }
    }
}
