package com.monday8am.tweetmeck.data.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface PreferenceStorage {
    var onboardingCompleted: Boolean
    var initialTopic: String?
}

class SharedPreferencesServiceImpl @Inject constructor(private val application: Application) :
    PreferenceStorage {

    private val prefs: Lazy<SharedPreferences> = lazy { // Lazy to prevent IO access to main thread.
        application.getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE
        )
    }

    override var onboardingCompleted by BooleanPreference(prefs, PREF_ONBOARDING, false)
    override var initialTopic by StringPreference(prefs, PREF_TOPIC, null)

    companion object {
        const val PREFS_NAME = "tweetmeck"
        const val PREF_ONBOARDING = "pref_onboarding"
        const val PREF_TOPIC = "pref_topic"
    }
}

class BooleanPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any, Boolean> {

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return preferences.value.getBoolean(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        preferences.value.edit { putBoolean(name, value) }
    }
}

class StringPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: String?
) : ReadWriteProperty<Any, String?> {

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return preferences.value.getString(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.value.edit { putString(name, value) }
    }
}
