package com.monday8am.tweetmeck.data.mappers

import com.monday8am.tweetmeck.data.models.TwitterUser
import jp.nephy.penicillin.models.Status

class StatusToTwitterUser : Mapper<Status, TwitterUser> {
    override fun map(from: Status): TwitterUser {
        return from.user.mapWith(UserToTwitterUser().asLambda())
    }
}
