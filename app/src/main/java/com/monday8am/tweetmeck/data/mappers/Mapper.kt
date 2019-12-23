package com.monday8am.tweetmeck.data.mappers

import jp.nephy.penicillin.models.PenicillinModel

interface Mapper<F, T> {
    fun map(from: F): T
}

interface SuspendMapper<F, T> {
    suspend fun map(from: F): T
}

fun <F, T> Mapper<F, T>.asLambda(): (F) -> T {
    return { map(it) }
}

fun <F, T> PenicillinModel.mapWith(mapper: (F) -> T): T {
    return mapper.invoke(this as F)
}
