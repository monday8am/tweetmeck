package com.monday8am.tweetmeck

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.DefaultAuthRepository
import com.monday8am.tweetmeck.data.DefaultDataRepository
import com.monday8am.tweetmeck.data.local.*
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.data.remote.TwitterClientImpl
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    private val lock = Any()
    private var database: TwitterDatabase? = null

    @Volatile
    var authRepository: AuthRepository? = null
        @VisibleForTesting set

    @Volatile
    var dataRepository: DataRepository? = null
        @VisibleForTesting set

    private var sharedPref: SharedPreferencesService? = null

    fun provideAuthRepository(context: Context): AuthRepository {
        synchronized(this) {
            return authRepository ?: authRepository ?: createAuthRepository(context)
        }
    }

    fun provideDataRepository(context: Context): DataRepository {
        synchronized(this) {
            return dataRepository ?: dataRepository ?: createDataRepository(context)
        }
    }

    private fun createAuthRepository(context: Context): AuthRepository {
        return DefaultAuthRepository(
            twitterClient = createTwitterClient(provideSharedPreferencesService(context)),
            sharedPreferencesService = provideSharedPreferencesService(context)
        )
    }

    private fun createDataRepository(context: Context): DataRepository {
        val database = database ?: createDataBase(context)
        return DefaultDataRepository(
            twitterClient = createTwitterClient(provideSharedPreferencesService(context)),
            localDataSource = TwitterDataSourceImpl(database.twitterListDao())
        )
    }

    private fun createTwitterClient(sharedPreferences: SharedPreferencesService): TwitterClient {
        val token = sharedPreferences.getAccessToken()
        return TwitterClientImpl(
            BuildConfig.apiKey,
            BuildConfig.apiSecret,
            token?.token ?: "",
            token?.secret ?: "",
            BuildConfig.callbackUrl)
    }

    private fun createDataBase(context: Context): TwitterDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            TwitterDatabase::class.java, "Tweetmeck.db"
        ).build()
        database = result
        return result
    }

    private fun provideSharedPreferencesService(context: Context): SharedPreferencesService {
        synchronized(this) {
            return sharedPref ?: sharedPref ?: SharedPreferencesServiceImpl(context)
        }
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            // Clear all data to avoid test pollution.
            runBlocking {
                sharedPref?.deleteAccessToken()
            }
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            dataRepository = null
        }
    }
}
