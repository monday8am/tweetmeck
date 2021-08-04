package com.monday8am.tweetmeck.data.mappers

import blue.starry.penicillin.models.Status
import com.monday8am.tweetmeck.data.models.TwitterUser

class StatusToTwitterUser : Mapper<Status, TwitterUser> {
    override fun map(from: Status): TwitterUser {
        return from.user.mapWith(UserToTwitterUser().asLambda())
    }
}
