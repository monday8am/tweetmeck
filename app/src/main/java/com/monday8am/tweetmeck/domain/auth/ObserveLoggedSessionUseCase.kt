package com.monday8am.tweetmeck.domain.auth

import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.domain.FlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class ObserveLoggedSessionUseCase constructor(
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FlowUseCase<Unit, Session?>(defaultDispatcher) {

    override fun execute(parameters: Unit): Flow<Result<Session?>> {
        return db.sessionDao().currentSessionFlow().map { Result.Success(it.firstOrNull()) }
    }
}
