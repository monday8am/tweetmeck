package com.monday8am.tweetmeck.domain

import androidx.paging.PagedList

internal const val oauthVerifierConst = "oauth_verifier"
internal const val pageSize = 8
internal const val prefetchDistance = 10

internal val pagedListConfig = PagedList.Config.Builder()
    .setInitialLoadSizeHint(pageSize * 2)
    .setPageSize(pageSize)
    .setPrefetchDistance(prefetchDistance)
    .build()
