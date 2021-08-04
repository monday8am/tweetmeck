package com.monday8am.tweetmeck.data.mappers

import blue.starry.penicillin.endpoints.oauth.AccessTokenResponse
import com.monday8am.tweetmeck.data.models.Session

fun AccessTokenResponse.mapToSession(): Session {
    return Session(
        userId = this.userId,
        screenName = this.screenName,
        accessToken = this.accessToken,
        accessTokenSecret = this.accessTokenSecret
    )
}
