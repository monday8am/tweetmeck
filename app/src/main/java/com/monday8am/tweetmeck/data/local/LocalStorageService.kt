package com.monday8am.tweetmeck.data.local

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.monday8am.tweetmeck.data.remote.AccessToken

interface LocalStorageService {
    fun getAccessToken(): AccessToken?
    suspend fun saveAccessToken(token: AccessToken)
    suspend fun deleteAccessToken()
}

class LocalStorageServiceImpl constructor(private val context: Context) : LocalStorageService {

    private val accessTokenKey = "accessToken"
    private val accessTokenSecretKey = "accessTokenSecret"

    private val pref: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun getAccessToken(): AccessToken? {
        val token = pref.getString(accessTokenKey, null)
        val secret = pref.getString(accessTokenSecretKey, null)
        if (token != null && secret != null) {
            return AccessToken(token, secret)
        }
        return null
    }

    override suspend fun saveAccessToken(token: AccessToken) {
        pref.edit().putString(accessTokenKey, token.token).apply()
        pref.edit().putString(accessTokenSecretKey, token.secret).apply()
    }

    override suspend fun deleteAccessToken() {
        pref.edit().clear().apply()
    }
}
