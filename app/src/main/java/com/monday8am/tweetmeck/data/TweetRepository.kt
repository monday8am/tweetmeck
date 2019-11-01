package com.monday8am.tweetmeck.data

import com.monday8am.tweetmeck.data.models.TwitterList

interface TweetRepository {

    suspend fun getLists() : List<TwitterList>

}

