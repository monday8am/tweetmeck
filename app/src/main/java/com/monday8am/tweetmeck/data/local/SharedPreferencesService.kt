package com.monday8am.tweetmeck.data.local

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.monday8am.tweetmeck.data.remote.AccessToken

interface SharedPreferencesService {
    fun isOnboardingPresented(): Boolean
    fun getAccessToken(): AccessToken?
    suspend fun saveAccessToken(token: AccessToken)
    suspend fun deleteAccessToken()
    suspend fun saveOnboardingState(isPresented: Boolean)
}

class SharedPreferencesServiceImpl constructor(private val context: Context) : SharedPreferencesService {

    private val accessTokenKey = "accessToken"
    private val accessTokenSecretKey = "accessTokenSecret"
    private val onboardingPresentedKey = "onboardingPresented"

    private val pref: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun isOnboardingPresented(): Boolean = pref.getBoolean(onboardingPresentedKey, false)

    override suspend fun saveOnboardingState(isPresented: Boolean) {
        pref.edit().putBoolean(onboardingPresentedKey, isPresented).apply()
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
        pref.edit().putString(accessTokenKey, null).apply()
        pref.edit().putString(accessTokenSecretKey, null).apply()
    }
}
