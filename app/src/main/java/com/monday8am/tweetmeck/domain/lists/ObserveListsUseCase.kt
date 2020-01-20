package com.monday8am.tweetmeck.domain.lists

import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.domain.FlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class ObserveListsUseCase constructor(
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FlowUseCase<Unit, List<TwitterList>>(defaultDispatcher) {

    override fun execute(parameters: Unit): Flow<Result<List<TwitterList>>> {
        return db.twitterListDao().getAll().map { Result.Success(it) }
    }
}
