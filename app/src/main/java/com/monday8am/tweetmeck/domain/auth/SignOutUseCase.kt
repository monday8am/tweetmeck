package com.monday8am.tweetmeck.domain.auth

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

open class SignOutUseCase constructor(
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UseCase<Unit, Unit>(defaultDispatcher) {

    override fun execute(parameters: Unit) {
        db.clearAllTables()
    }
}
