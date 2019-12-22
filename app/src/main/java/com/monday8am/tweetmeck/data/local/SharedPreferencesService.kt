package com.monday8am.tweetmeck.data.local

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

interface SharedPreferencesService {
    fun isOnboardingPresented(): Boolean
    suspend fun saveOnboardingState(isPresented: Boolean)
}

class SharedPreferencesServiceImpl constructor(private val context: Context) : SharedPreferencesService {

    private val onboardingPresentedKey = "onboardingPresented"

    private val pref: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun isOnboardingPresented(): Boolean = pref.getBoolean(onboardingPresentedKey, false)

    override suspend fun saveOnboardingState(isPresented: Boolean) {
        pref.edit().putBoolean(onboardingPresentedKey, isPresented).apply()
    }
}
