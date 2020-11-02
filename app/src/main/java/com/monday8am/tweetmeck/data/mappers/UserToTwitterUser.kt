package com.monday8am.tweetmeck.data.mappers

import blue.starry.penicillin.models.User
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.util.TweetDateUtils

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
