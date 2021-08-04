package com.monday8am.tweetmeck.domain.auth

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.di.IoDispatcher
import com.monday8am.tweetmeck.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class SignOutUseCase @Inject constructor(
    private val db: TwitterDatabase,
    @IoDispatcher defaultDispatcher: CoroutineDispatcher
) : UseCase<Unit, Unit>(defaultDispatcher) {

    override fun execute(parameters: Unit) {
        db.clearAllTables()
    }
}
