package com.example.sonusync.ui.settings

import android.os.Bundle
import androidx.media3.exoplayer.ExoPlayer
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.example.sonusync.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val volumePreference: SeekBarPreference? = findPreference("volume")
        volumePreference?.setOnPreferenceChangeListener { _, newValue ->
            val volume = (newValue as Int) / 10f
            exoPlayer.volume = volume
            true
        }
    }
}