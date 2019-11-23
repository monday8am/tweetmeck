package com.monday8am.tweetmeck.data.remote

enum class Status {
    Running,
    Success,
    Failed
}

@Suppress("DataClassPrivateConstructor")
data class RequestState private constructor(
    val status: Status,
    val msg: String? = null) {

    companion object {
        val loaded = RequestState(Status.Success)
        val loading = RequestState(Status.Running)
        fun error(msg: String?) = RequestState(Status.Failed, msg)
    }
}
