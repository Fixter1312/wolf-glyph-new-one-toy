package com.example.wilczektoy

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.IBinder
import android.os.Looper

/**
 * Prosty serwis „zabawki” – co 1s generuje klatkę z WolfSprites.
 * W tym miejscu (TODO) możesz podłączyć wysyłkę bitmapy do Glyph API.
 */
class WolfToyService : Service() {

    private val handler = Handler(Looper.getMainLooper())

    // Przykładowe flagi stanu – możesz je zmieniać z innych miejsc aplikacji.
    private var isBlinking = false
    private var isSleeping = false
    private var isHappy = true
    private var isDirty = false

    private val tick = object : Runnable {
        override fun run() {
            // >>> NAJWAŻNIEJSZE: wywołanie BEZ 'ctx'
            val frame: Bitmap = WolfSprites.renderFrame(
                blink = isBlinking,
                sleep = isSleeping,
                happy = isHappy,
                dirty = isDirty
            )

            // TODO: tu prześlij 'frame' do Nothing Glyph (np. przez odpowiednie API),
            // albo narysuj gdzieś w Activity/Preview – zależnie od Twojej integracji.

            handler.postDelayed(this, 1000L) // odświeżaj co 1s
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(tick)
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
