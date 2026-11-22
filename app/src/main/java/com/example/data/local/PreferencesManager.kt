package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.archiveDataStore: DataStore<Preferences> by preferencesDataStore(name = "internet_archive_prefs")

class PreferencesManager(private val context: Context) {
    private object Keys {
        val SKIP_INTRO_ON_LAUNCH = booleanPreferencesKey("skip_intro_on_launch")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    }

    val skipIntroOnLaunch: Flow<Boolean> = context.archiveDataStore.data.map { prefs ->
        prefs[Keys.SKIP_INTRO_ON_LAUNCH] ?: false
    }

    val reducedMotion: Flow<Boolean> = context.archiveDataStore.data.map { prefs ->
        prefs[Keys.REDUCED_MOTION] ?: false
    }

    suspend fun setSkipIntroOnLaunch(skip: Boolean) {
        context.archiveDataStore.edit { prefs ->
            prefs[Keys.SKIP_INTRO_ON_LAUNCH] = skip
        }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.archiveDataStore.edit { prefs ->
            prefs[Keys.REDUCED_MOTION] = enabled
        }
    }
}
