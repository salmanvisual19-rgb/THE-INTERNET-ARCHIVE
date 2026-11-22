package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.SoundArtifact
import com.example.data.model.SoundSynthPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SynthesizedSoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playJob: Job? = null
    private var activeTrack: AudioTrack? = null

    private val _playingSoundId = MutableStateFlow<String?>(null)
    val playingSoundId: StateFlow<String?> = _playingSoundId.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _waveformBars = MutableStateFlow(List(28) { 0.12f })
    val waveformBars: StateFlow<List<Float>> = _waveformBars.asStateFlow()

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    fun setVolume(newVolume: Float) {
        val clamped = newVolume.coerceIn(0f, 1f)
        _volume.value = clamped
        runCatching {
            activeTrack?.setVolume(clamped)
        }
    }

    fun togglePlay(sound: SoundArtifact) {
        if (_playingSoundId.value == sound.id) {
            stop()
            return
        }
        play(sound)
    }

    fun play(sound: SoundArtifact) {
        stop()
        _playingSoundId.value = sound.id
        _progress.value = 0f

        playJob = scope.launch {
            val sampleRate = 22050
            val samples = synthesizePreset(sound.preset, sound.durationMs, sampleRate)
            val minBuffer = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuffer)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            activeTrack = track
            try {
                track.setVolume(_volume.value)
                track.play()

                val chunkSize = 512
                var offset = 0
                while (isActive && offset < samples.size) {
                    val count = (samples.size - offset).coerceAtMost(chunkSize)
                    track.write(samples, offset, count)
                    updateWaveformFromChunk(samples, offset, count)
                    offset += count
                    _progress.value = offset.toFloat() / samples.size.toFloat()
                }
            } catch (_: Exception) {
                // Gracefully degrade if audio hardware is unavailable in headless environment
            } finally {
                runCatching {
                    track.stop()
                    track.release()
                }
                if (activeTrack === track) {
                    activeTrack = null
                }
                if (_playingSoundId.value == sound.id) {
                    _playingSoundId.value = null
                    _progress.value = 0f
                    _waveformBars.value = List(28) { 0.12f }
                }
            }
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
        runCatching {
            activeTrack?.pause()
            activeTrack?.flush()
            activeTrack?.release()
        }
        activeTrack = null
        _playingSoundId.value = null
        _progress.value = 0f
        _waveformBars.value = List(28) { 0.12f }
    }

    fun release() {
        stop()
        scope.cancel()
    }

    private fun updateWaveformFromChunk(samples: ShortArray, offset: Int, count: Int) {
        val barCount = 28
        val step = (count / barCount).coerceAtLeast(1)
        val bars = List(barCount) { idx ->
            val sampleIndex = (offset + idx * step).coerceAtMost(samples.lastIndex)
            val normalized = abs(samples[sampleIndex].toFloat()) / Short.MAX_VALUE
            (0.14f + normalized * 0.86f * _volume.value.coerceAtLeast(0.25f)).coerceIn(0.1f, 1f)
        }
        _waveformBars.value = bars
    }

    private fun synthesizePreset(
        preset: SoundSynthPreset,
        durationMs: Long,
        sampleRate: Int
    ): ShortArray {
        val totalSamples = ((durationMs / 1000.0) * sampleRate).toInt().coerceAtLeast(sampleRate / 2)
        val output = ShortArray(totalSamples)
        val rng = Random(42)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val norm = i.toDouble() / totalSamples
            val raw: Double = when (preset) {
                SoundSynthPreset.DIALUP_HANDSHAKE -> {
                    when {
                        norm < 0.25 -> sin(2.0 * PI * 1200.0 * t) * 0.45 + sin(2.0 * PI * 2400.0 * t) * 0.35
                        norm < 0.55 -> {
                            val freq = if (((t * 18).toInt() % 2) == 0) 1650.0 else 2100.0
                            sin(2.0 * PI * freq * t) * 0.6
                        }
                        else -> (rng.nextDouble() * 2.0 - 1.0) * 0.35 + sin(2.0 * PI * 1950.0 * t) * 0.25
                    }
                }
                SoundSynthPreset.VINE_BOOM -> {
                    val freq = 140.0 * exp(-t * 4.5) + 36.0
                    val env = exp(-t * 3.8)
                    (sin(2.0 * PI * freq * t) * 0.85 + sin(2.0 * PI * freq * 0.5 * t) * 0.4) * env
                }
                SoundSynthPreset.EIGHT_BIT_LEVELUP -> {
                    val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98)
                    val noteIdx = ((norm * notes.size).toInt()).coerceIn(0, notes.lastIndex)
                    val square = if (sin(2.0 * PI * notes[noteIdx] * t) >= 0.0) 1.0 else -1.0
                    square * 0.45 * (1.0 - norm * 0.35)
                }
                SoundSynthPreset.CRT_DEGAUSS -> {
                    val env = exp(-t * 2.3)
                    (sin(2.0 * PI * 60.0 * t) * 0.55 + sin(2.0 * PI * 180.0 * t) * 0.3 + sin(2.0 * PI * 3200.0 * t) * 0.12) * env
                }
                SoundSynthPreset.MODEM_GLITCH -> {
                    val step = (t * 24.0).toInt()
                    val freq = 220.0 + ((step * 173) % 1400)
                    val pulse = if (sin(2.0 * PI * freq * t) > 0) 0.5 else -0.5
                    pulse * (if ((step % 5) == 0) 0.2 else 0.7)
                }
                SoundSynthPreset.AIRHORN_FANFARE -> {
                    val burst = if (norm < 0.18 || (norm in 0.24..0.42) || norm > 0.48) 1.0 else 0.0
                    val saw1 = ((t * 440.0) % 1.0) * 2.0 - 1.0
                    val saw2 = ((t * 444.0) % 1.0) * 2.0 - 1.0
                    val saw3 = ((t * 554.37) % 1.0) * 2.0 - 1.0
                    (saw1 + saw2 + saw3) * 0.24 * burst
                }
                SoundSynthPreset.DRAMATIC_STING -> {
                    val freq = when {
                        norm < 0.28 -> 392.0
                        norm < 0.56 -> 369.99
                        else -> 349.23
                    }
                    val env = if (norm > 0.56) exp(-(t - 0.84).coerceAtLeast(0.0) * 1.5) else 1.0
                    (sin(2.0 * PI * freq * t) * 0.5 + sin(2.0 * PI * freq * 1.5 * t) * 0.25) * env
                }
                SoundSynthPreset.SYNTHWAVE_CHORD -> {
                    val env = sin(PI * norm).coerceAtLeast(0.05)
                    val f1 = sin(2.0 * PI * 220.0 * t)
                    val f2 = sin(2.0 * PI * 261.63 * t)
                    val f3 = sin(2.0 * PI * 329.63 * t)
                    val f4 = sin(2.0 * PI * 440.0 * t)
                    (f1 + f2 + f3 + f4) * 0.2 * env
                }
            }

            val fadeOut = if (norm > 0.94) ((1.0 - norm) / 0.06) else 1.0
            val sample = (raw * fadeOut * 0.85 * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            output[i] = sample.toShort()
        }
        return output
    }
}
