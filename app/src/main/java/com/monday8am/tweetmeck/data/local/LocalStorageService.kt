package com.monday8am.tweetmeck.data.local

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.monday8am.tweetmeck.data.remote.TwitterAccessToken


interface LocalStorageService {
    suspend fun getAccessToken(): TwitterAccessToken?
    suspend fun saveAccessToken(token: TwitterAccessToken)
    suspend fun deleteAccessToken()
}

class LocalStorageServiceImpl constructor(private val context: Context): LocalStorageService {

    private val accessTokenKey = "accessToken"
    private val accessTokenSecretKey = "accessTokenSecret"

    private val pref: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override suspend fun getAccessToken(): TwitterAccessToken? {
        val token = pref.getString(accessTokenKey, null)
        val secret = pref.getString(accessTokenSecretKey, null)
        if (token != null && secret != null) {
            return TwitterAccessToken(token, secret)
        }
        return null
    }

    override suspend fun saveAccessToken(token: TwitterAccessToken) {
        pref.edit().putString(accessTokenKey, token.token).apply()
        pref.edit().putString(accessTokenSecretKey, token.secret).apply()
    }

    override suspend fun deleteAccessToken() {
        pref.edit().clear().apply()
    }
}
