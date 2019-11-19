package com.monday8am.tweetmeck.data.models

import java.util.*

data class Tweet(val id: Long,
                 val title: String,
                 val content: String,
                 val date: Date,
                 val savedForLater: Boolean
)