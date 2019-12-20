package com.monday8am.tweetmeck.data.mappers

import com.monday8am.tweetmeck.data.models.Session
import jp.nephy.penicillin.endpoints.oauth.AccessTokenResponse

fun AccessTokenResponse.mapToSession(): Session {
    return Session(userId = this.userId,
        screenName = this.screenName,
        accessToken = this.accessToken,
        accessTokenSecret = this.accessTokenSecret)
}
