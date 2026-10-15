package com.andoni.phrasaldaily.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Thin wrapper over Android TextToSpeech, speaking US English. */
class Speaker(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var pending: Pair<String, Float>? = null

    /** False once we know the device has no English voice. */
    var available = true
        private set

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            available = false
            return
        }
        val result = tts.setLanguage(Locale.US)
        available = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        ready = available
        pending?.let { (text, rate) -> speak(text, rate) }
        pending = null
    }

    fun speak(text: String, rate: Float = 0.9f) {
        if (!ready) {
            pending = text to rate
            return
        }
        tts.setSpeechRate(rate)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
