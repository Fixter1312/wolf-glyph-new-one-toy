
package com.example.wilczektoy

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

// Importy z Nothing Glyph Matrix SDK (.aar w app/libs)
import com.nothing.glyph.matrix.GlyphMatrixManager
import com.nothing.glyph.matrix.GlyphMatrixFrame
import com.nothing.glyph.matrix.GlyphMatrixObject
import com.nothing.glyph.matrix.Glyph

class WolfToyService : Service() {
    private lateinit var gm: GlyphMatrixManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Stan
    private var glod = 20
    private var energia = 80
    private var higiena = 80
    private var zabawa = 70
    private var spi = false

    private val running = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        gm = GlyphMatrixManager()
        gm.init(object : GlyphMatrixManager.Callback {})
        // Uwaga: identyfikator urządzenia może wymagać sprawdzenia w SDK/dokach
        gm.register(Glyph.DEVICE_23112)
        running.set(true)
        startRenderLoop()
    }

    override fun onDestroy() {
        running.set(false)
        gm.unInit()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ev = intent?.getStringExtra("event")
        when (ev) {
            "change" -> { if (spi) spi = false else feed() }
            "action_down" -> play()
            "action_up" -> Unit
        }
        return START_STICKY
    }

    private fun feed() { glod = (glod - 35).coerceAtLeast(0); higiena = (higiena - 2).coerceAtLeast(0) }
    private fun play() { zabawa = (zabawa + 28).coerceAtMost(100); energia = (energia - 10).coerceAtLeast(0); glod = (glod + 6).coerceAtMost(100) }
    private fun clean() { higiena = (higiena + 40).coerceAtMost(100) }

    private fun tickDecay() {
        glod = (glod + 7).coerceAtMost(100)
        energia = (energia - 5).coerceAtLeast(0)
        higiena = (higiena - 4).coerceAtLeast(0)
        zabawa = (zabawa - 4).coerceAtLeast(0)
        if (energia <= 0) spi = true
    }

    private fun szczescie(): Int = ((100 - glod) * 0.35 + energia * 0.25 + higiena * 0.2 + zabawa * 0.2).toInt()

    private fun startRenderLoop() {
        scope.launch {
            var t = 0
            var sinceDecay = 0
            while (running.get()) {
                if (sinceDecay >= 16) { tickDecay(); sinceDecay = 0 }
                if (spi) { energia = (energia + 1).coerceAtMost(100); glod = (glod + 1).coerceAtMost(100) }

                val blink = (t % 12 == 0)
                val bmp = WolfSprites.renderFrame(
                    blink = blink,
                    sleep = spi,
                    happy = szczescie() > 85,
                    dirty = higiena < 40
                )
                pushBitmap(bmp)

                t++; sinceDecay++
                delay(250)
            }
        }
    }

    private fun pushBitmap(bmp: Bitmap) {
        val obj = GlyphMatrixObject.Builder().apply {
            setImageSource(bmp)
            setPosition(0, 0)
        }.build()

        val frame = GlyphMatrixFrame.Builder().apply {
            addTop(obj)
        }.build()

        gm.setMatrixFrame(frame)
    }
}
