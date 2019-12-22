package com.monday8am.tweetmeck.data.mappers

import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.util.TweetDateUtils
import jp.nephy.penicillin.models.User

class UserToTwitterUser : Mapper<User, TwitterUser> {
    override fun map(from: User): TwitterUser {
        return TwitterUser(
            from.id,
            TweetDateUtils.apiTimeToLong(from.createdAtRaw),
            from.name,
            from.screenName,
            from.location,
            from.description,
            from.url,
            from.verified,
            from.followersCount,
            from.friendsCount,
            from.listedCount,
            from.favouritesCount,
            from.statusesCount,
            from.profileBackgroundColor,
            from.profileBackgroundImageUrl,
            from.profileBackgroundTile,
            from.profileImageUrl,
            from.defaultProfileImage,
            from.following,
            from.followRequestSent)
    }
}
