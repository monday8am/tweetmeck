package com.monday8am.tweetmeck.data.mappers

import com.monday8am.tweetmeck.data.models.ListVisibilityMode
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.util.TweetDateUtils

class ListToTwitterList() : Mapper<blue.starry.penicillin.models.TwitterList, TwitterList> {
    override fun map(from: blue.starry.penicillin.models.TwitterList): TwitterList {
        return TwitterList(
            from.id,
            TweetDateUtils.apiTimeToLong(from.createdAtRaw),
            from.description,
            from.following,
            from.fullName,
            from.memberCount,
            ListVisibilityMode.valueOf(from.mode.name),
            from.name,
            from.slug,
            from.subscriberCount,
            from.uri,
            from.user.id
        )
    }
}
