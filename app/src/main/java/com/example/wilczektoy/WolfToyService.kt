package com.example.wilczektoy

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.IBinder
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class WolfToyService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val running = AtomicBoolean(false)

    // stan gry
    private var glod = 20
    private var energia = 80
    private var higiena = 80
    private var zabawa = 70
    private var spi = false

    // SDK przez refleksję
    private var gm: Any? = null                    // GlyphMatrixManager
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
        // Od razu wyślij widoczną „planszę testową”, żeby potwierdzić połączenie.
        pushBitmap(makeTestPattern())
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

    // ───── logika gry ──────────────────────────────────────────────────────────
    private fun feed() { glod = (glod - 35).coerceAtLeast(0); higiena = (higiena - 2).coerceAtLeast(0) }
    private fun play() { zabawa = (zabawa + 28).coerceAtMost(100); energia = (energia - 10).coerceAtLeast(0); glod = (glod + 6).coerceAtMost(100) }
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

    // ───── inicjalizacja SDK (szukamy różnych nazw klas) ──────────────────────
    private fun tryInitGlyphSdk() {
        fun findClass(vararg names: String): Class<*>? =
            names.firstNotNullOfOrNull { n -> runCatching { Class.forName(n) }.getOrNull() }

        try {
            val gmClazz = findClass(
                "com.nothing.glyph.matrix.GlyphMatrixManager",
                "com.nothing.glyph.matrix.sdk.GlyphMatrixManager",
                "com.nothing.glyph.GlyphMatrixManager"
            ) ?: throw ClassNotFoundException("GlyphMatrixManager not found")
            gm = gmClazz.getDeclaredConstructor().newInstance()

            // init(callback) – w niektórych wersjach wymagane
            runCatching {
                val cbClazz = findClass(
                    "com.nothing.glyph.matrix.GlyphMatrixManager\$Callback",
                    "com.nothing.glyph.matrix.sdk.GlyphMatrixManager\$Callback"
                )!!
                val proxy = java.lang.reflect.Proxy.newProxyInstance(
                    cbClazz.classLoader, arrayOf(cbClazz)
                ) { _, _, _ -> null }
                gmClazz.getMethod("init", cbClazz).invoke(gm, proxy)
            }

            // register() – niektóre wersje wymagają wywołania
            runCatching { gmClazz.getMethod("register").invoke(gm) }

            frameBuilderClazz = findClass(
                "com.nothing.glyph.matrix.GlyphMatrixFrame\$Builder",
                "com.nothing.glyph.matrix.sdk.GlyphMatrixFrame\$Builder"
            )
            objBuilderClazz = findClass(
                "com.nothing.glyph.matrix.GlyphMatrixObject\$Builder",
                "com.nothing.glyph.matrix.sdk.GlyphMatrixObject\$Builder"
            )

            gmSetFrame = gmClazz.methods.firstOrNull { it.name == "setMatrixFrame" }
            frameAddTop = frameBuilderClazz?.methods?.firstOrNull { it.name == "addTop" }
            frameBuild  = frameBuilderClazz?.methods?.firstOrNull { it.name == "build" }
            objSetImage = objBuilderClazz?.methods?.firstOrNull { it.name == "setImageSource" }
            objSetPos   = objBuilderClazz?.methods?.firstOrNull { it.name == "setPosition" }
            objBuild    = objBuilderClazz?.methods?.firstOrNull { it.name == "build" }
        } catch (_: Throwable) {
            gm = null
        }
    }

    // ───── wysyłka bitmapy do Glyph Matrix ────────────────────────────────────
    private fun pushBitmap(bmp: Bitmap) {
        val manager = gm ?: return   // jeśli SDK nie znalezione – pomijamy
        try {
            val objBuilder = objBuilderClazz!!.getDeclaredConstructor().newInstance()
            objSetImage?.invoke(objBuilder, bmp)
            objSetPos?.invoke(objBuilder, 0, 0)
            val obj = objBuild?.invoke(objBuilder)

            val frameBuilder = frameBuilderClazz!!.getDeclaredConstructor().newInstance()
            frameAddTop?.invoke(frameBuilder, obj)
            val frame = frameBuild?.invoke(frameBuilder)

            gmSetFrame?.invoke(manager, frame)
        } catch (_: Throwable) {
            // ciche pominięcie pojedynczych niezgodności API
        }
    }

    // bardzo jasny wzór testowy – powinien być widoczny natychmiast
    private fun makeTestPattern(size: Int = 25): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint().apply { style = Paint.Style.FILL }
        for (y in 0 until size) {
            for (x in 0 until size) {
                p.color = if ((x + y) % 2 == 0) Color.WHITE else Color.BLACK
                c.drawRect(x.toFloat(), y.toFloat(), (x+1).toFloat(), (y+1).toFloat(), p)
            }
        }
        return bmp
    }
}            objSetPos    = objBuilderClazz!!.methods.firstOrNull { it.name == "setPosition" }
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
