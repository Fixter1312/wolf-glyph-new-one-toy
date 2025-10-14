package com.example.wilczektoy

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class WolfToyService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val running = AtomicBoolean(false)

    private var glod = 20
    private var energia = 80
    private var higiena = 80
    private var zabawa = 70
    private var spi = false

    // Refleksja – brak twardych importów z AAR
    private var gm: Any? = null
    private var gmSetFrame: java.lang.reflect.Method? = null
    private var frameBuilderClazz: Class<*>? = null
    private var frameBuild: java.lang.reflect.Method? = null
    private var objBuilderClazz: Class<*>? = null
    private var objSetImage: java.lang.reflect.Method? = null
    private var objSetPos: java.lang.reflect.Method? = null
    private var objBuild: java.lang.reflect.Method? = null
    private var frameAddTop: java.lang.reflect.Method? = null

    override fun onCreate() {
        super.onCreate()
        tryInitGlyphSdk()
        running.set(true)
        startRenderLoop()
    }

    override fun onDestroy() {
        running.set(false)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("event")) {
            "change" -> if (spi) spi = false else feed()
            "action_down" -> play()
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

    private fun tryInitGlyphSdk() {
        try {
            val gmClazz = Class.forName("com.nothing.glyph.matrix.GlyphMatrixManager")
            gm = gmClazz.getDeclaredConstructor().newInstance()

            runCatching {
                val cbClazz = Class.forName("com.nothing.glyph.matrix.GlyphMatrixManager\$Callback")
                val proxy = java.lang.reflect.Proxy.newProxyInstance(
                    cbClazz.classLoader, arrayOf(cbClazz)
                ) { _, _, _ -> null }
                gmClazz.getMethod("init", cbClazz).invoke(gm, proxy)
            }
            runCatching { gmClazz.getMethod("register").invoke(gm) }

            frameBuilderClazz = Class.forName("com.nothing.glyph.matrix.GlyphMatrixFrame\$Builder")
            objBuilderClazz   = Class.forName("com.nothing.glyph.matrix.GlyphMatrixObject\$Builder")

            gmSetFrame   = gmClazz.methods.firstOrNull { it.name == "setMatrixFrame" }
            frameAddTop  = frameBuilderClazz!!.methods.firstOrNull { it.name == "addTop" }
            frameBuild   = frameBuilderClazz!!.methods.firstOrNull { it.name == "build" }
            objSetImage  = objBuilderClazz!!.methods.firstOrNull { it.name == "setImageSource" }
            objSetPos    = objBuilderClazz!!.methods.firstOrNull { it.name == "setPosition" }
            objBuild     = objBuilderClazz!!.methods.firstOrNull { it.name == "build" }
        } catch (_: Throwable) { gm = null }
    }

    private fun pushBitmap(bmp: Bitmap) {
        val manager = gm ?: return
        try {
            val objBuilder = objBuilderClazz!!.getDeclaredConstructor().newInstance()
            objSetImage?.invoke(objBuilder, bmp)
            objSetPos?.invoke(objBuilder, 0, 0)
            val obj = objBuild?.invoke(objBuilder)

            val frameBuilder = frameBuilderClazz!!.getDeclaredConstructor().newInstance()
            frameAddTop?.invoke(frameBuilder, obj)
            val frame = frameBuild?.invoke(frameBuilder)

            gmSetFrame?.invoke(manager, frame)
        } catch (_: Throwable) { /* ignoruj pojedyncze różnice API */ }
    }
}
