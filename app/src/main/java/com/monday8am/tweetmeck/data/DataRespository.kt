package com.monday8am.tweetmeck.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.*
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.data.remote.SearchDataSourceFactory
import com.monday8am.tweetmeck.data.remote.TimelineDbBoundaryCallback
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.util.switchMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface DataRepository {
    val session: Flow<Session?>
    val lists: LiveData<List<TwitterList>>
    suspend fun getTweet(tweetId: Long): Result<Tweet>
    suspend fun getUser(screenName: String): Result<TwitterUser>
    suspend fun refreshListTimeline(listId: Long): Result<Unit>
    suspend fun refreshLists(screenName: String): Result<Unit>
    suspend fun refreshLoggedUserLists(session: Session): Result<Unit>
    suspend fun likeTweet(tweet: Tweet, session: Session): Result<Unit>
    suspend fun retweetTweet(tweet: Tweet, session: Session): Result<Unit>
    fun getTimeline(query: TimelineQuery): TimelineContent
}

class DataRepositoryImpl(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataRepository {

    private val pageSize = 8
    private val prefetchDistance = 10
    private val pagedListConfig = PagedList.Config.Builder()
        .setInitialLoadSizeHint(pageSize * 2)
        .setPageSize(pageSize)
        .setPrefetchDistance(prefetchDistance)
        .build()

    override val lists: LiveData<List<TwitterList>>
        get() = db.twitterListDao().getAll()

    override val session: Flow<Session?>
        get() = db.sessionDao().currentSessionFlow().map { it.firstOrNull() }

    override suspend fun refreshLists(screenName: String): Result<Unit> {
        return asResult {
            val listsFromRemote = remoteClient.getLists(screenName).map { it.mapWith(ListToTwitterList().asLambda()) }
            db.twitterListDao().updateAll(listsFromRemote)
        }
    }

    override suspend fun refreshLoggedUserLists(session: Session): Result<Unit> {
        return asResult {
            val listsFromRemote = remoteClient.getLoggedUserLists(session)
                .map { it.mapWith(ListToTwitterList().asLambda()) }
            db.twitterListDao().updateAll(listsFromRemote)
        }
    }

    override suspend fun refreshListTimeline(listId: Long): Result<Unit> {
        return asResult {
            val response = remoteClient.getListTimeline(listId, count = pageSize * 2)
            val tweets = response.map { it.mapWith(StatusToTweet(listId).asLambda()) }
            val users = response.map { it.mapWith(StatusToTwitterUser().asLambda()) }
            db.tweetDao().refreshTweetsFromList(listId, tweets)
            db.twitterUserDao().insert(users)
        }
    }

    override suspend fun likeTweet(tweet: Tweet, session: Session): Result<Unit> {
        return asResult {
            val response = remoteClient.likeTweet(tweet.id, !tweet.uiContent.favorited, session)
                                       .mapWith(StatusToTweet(tweet.listId).asLambda())
            db.tweetDao().insert(tweet.setFavorite(response.uiContent.favorited))
        }
    }

    override suspend fun retweetTweet(tweet: Tweet, session: Session): Result<Unit> {
        return asResult {
            val newTweet = remoteClient.retweetTweet(tweet.id, !tweet.uiContent.retweeted, session)
                                        .mapWith(StatusToTweet(tweet.listId).asLambda())
            val existingTweets = db.tweetDao().getRelatedTweets(tweet.uiContent.id)
                                              .map { it.setRetweeted(!tweet.uiContent.retweeted) }
                                              .toMutableList()
            val retweetUndone = tweet.uiContent.retweeted
            if (retweetUndone) {
                val tweetedByMe = existingTweets.find { it.main.user.id == session.userId }
                if (tweetedByMe != null) {
                    existingTweets.removeIf { it.id == tweetedByMe.id }
                    db.tweetDao().deleteAndUpdateTweets(tweetedByMe.id, existingTweets)
                }
            } else {
                db.tweetDao().insert(existingTweets)
                db.tweetDao().insert(newTweet)
            }
        }
    }

    private suspend fun loadMoreForTimeline(listId: Long, maxTweetId: Long): Result<Unit> {
        return asResult {
            val result = remoteClient.getListTimeline(listId, maxTweetId = maxTweetId, count = pageSize * 2)
            db.tweetDao().insert(result.map { it.mapWith(StatusToTweet(listId).asLambda()) })
            db.twitterUserDao().insert(result.map { it.mapWith(StatusToTwitterUser().asLambda()) })
        }
    }

    override fun getTimeline(query: TimelineQuery): TimelineContent {
        return when (query) {
            is TimelineQuery.List -> getTimeline(query.listId)
            is TimelineQuery.Hashtag -> {
                val source = SearchDataSourceFactory(query.hashtag, remoteClient)
                val requestState = source.sourceLiveData.switchMap { it.requestState }
                TimelineContent(
                    pagedList = source.toLiveData(pagedListConfig),
                    loadMoreState = requestState
                )
            }
            else -> throw Exception("Unsupported query!")
        }
    }

    override suspend fun getTweet(tweetId: Long): Result<Tweet> =
        getItemFromDb(tweetId, db.tweetDao()::getItemById
    )

    override suspend fun getUser(screenName: String): Result<TwitterUser> =
        getItemFromDb(screenName, db.twitterUserDao()::getItemByScreenName
    )

    private fun getTimeline(listId: Long): TimelineContent {
        val boundaryCallback = TimelineDbBoundaryCallback(
            listId = listId,
            refreshCallback = ::refreshListTimeline,
            loadMoreCallback = ::loadMoreForTimeline
        )

        val livePagedList = db.tweetDao().getTweetsByListId(listId).toLiveData(
            config = pagedListConfig,
            boundaryCallback = boundaryCallback)
        return TimelineContent(
            pagedList = livePagedList,
            loadMoreState = boundaryCallback.requestState
        )
    }

    private suspend fun <E, T> getItemFromDb(itemId: E, dbCall: suspend (E) -> T?): Result<T> {
        return withContext(ioDispatcher) {
            try {
                val value: T? = dbCall.invoke(itemId)
                if (value != null) {
                    Success(value)
                } else {
                    Error(Exception("Item not found with id: $itemId"))
                }
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    private suspend fun <T> asResult(block: suspend() -> T): Result<T> {
        return withContext(ioDispatcher) {
            try {
                val result = block.invoke()
                Success(result)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
}

data class TimelineContent(
    val pagedList: LiveData<PagedList<Tweet>>,
    val loadMoreState: LiveData<Result<Unit>>
)
