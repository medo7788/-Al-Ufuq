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
                    mp.start()
                    _audioState.value = AudioState(isPlaying = true, currentTitle = title, isLoading = false)
                }
                setOnCompletionListener {
                    _audioState.value = AudioState(isPlaying = false, currentTitle = "")
                }
                setOnErrorListener { _, _, _ ->
                    _audioState.value = AudioState(
                        isPlaying = false,
                        currentTitle = "",
                        isLoading = false,
                        error = "تعذر تشغيل الصوت"
                    )
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
            if (player.isPlaying) {
                player.pause()
                _audioState.value = _audioState.value.copy(isPlaying = false)
            } else {
                player.start()
                _audioState.value = _audioState.value.copy(isPlaying = true)
            }
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _audioState.value = AudioState(isPlaying = false, currentTitle = "")
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error stopping audio: ${e.message}")
        }
    }
}
