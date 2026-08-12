package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioState(
    val isPlaying: Boolean = false,
    val currentTitle: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    private val _audioState = MutableStateFlow(AudioState())
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    fun playAudio(url: String, title: String) {
        try {
            stopAudio()
            _audioState.value = AudioState(isPlaying = false, currentTitle = title, isLoading = true)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    try {
                        mp.start()
                        _audioState.value = AudioState(isPlaying = true, currentTitle = title, isLoading = false)
                    } catch (e: Exception) {
                        Log.e("AudioPlayerManager", "Error starting player: ${e.message}")
                        _audioState.value = AudioState(isPlaying = false, currentTitle = "", isLoading = false, error = "تعذر بدء الصوت")
                    }
                }
                setOnCompletionListener {
                    _audioState.value = AudioState(isPlaying = false, currentTitle = "")
                    stopAudio()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioPlayerManager", "MediaPlayer error: what=$what, extra=$extra")
                    _audioState.value = AudioState(
                        isPlaying = false,
                        currentTitle = "",
                        isLoading = false,
                        error = "تعذر تشغيل الصوت"
                    )
                    try {
                        mp.reset()
                        mp.release()
                    } catch (e: Exception) {
                        Log.w("AudioPlayerManager", "Error resetting player on error: ${e.message}")
                    }
                    if (mediaPlayer == mp) {
                        mediaPlayer = null
                    }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error playing audio: ${e.message}")
            _audioState.value = AudioState(isPlaying = false, isLoading = false, error = e.localizedMessage)
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                    _audioState.value = _audioState.value.copy(isPlaying = false)
                } else {
                    player.start()
                    _audioState.value = _audioState.value.copy(isPlaying = true)
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Error toggling play/pause: ${e.message}")
            }
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let { player ->
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                if (player.isPlaying) {
                    try {
                        player.stop()
                    } catch (e: Exception) {
                        Log.w("AudioPlayerManager", "Exception calling stop(): ${e.message}")
                    }
                }
                try {
                    player.reset()
                } catch (e: Exception) {
                    Log.w("AudioPlayerManager", "Exception calling reset(): ${e.message}")
                }
                try {
                    player.release()
                } catch (e: Exception) {
                    Log.w("AudioPlayerManager", "Exception calling release(): ${e.message}")
                }
            }
            mediaPlayer = null
            _audioState.value = AudioState(isPlaying = false, currentTitle = "")
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error in stopAudio: ${e.message}")
        }
    }
}
