package com.example.wilczektoy

import android.graphics.Bitmap
import android.graphics.Color

object WolfSprites {
    private const val SIZE = 25

    // ✅ Nowy sprite wilka — boczny profil, dopasowany do Nothing Glyph Matrix
    private val BASE = arrayOf(
        "0000000000000000000000000",
        "0000000000000000000000000",
        "0000000000000000000000000",
        "0000000000011110000000000",
        "0000000001111111000000000",
        "0000000011111111100000000",
        "0000000111111111110000000",
        "0000001111111111111000000",
        "0000011111100011111100000",
        "0000011111100011111100000",
        "0000011111111111111100000",
        "0000011111111111111100000",
        "0000001111111111111000000",
        "0000000111111111110000000",
        "0000000011111111100000000",
        "0000000001111111000000000",
        "0000000000111110000000000",
        "0000000000011100000000000",
        "0000000000001000000000000",
        "0000000000001000000000000",
        "0000000000001000000000000",
        "0000000000001000000000000",
        "0000000000001000000000000",
        "0000000000000000000000000",
        "0000000000000000000000000"
    )

    private val EYES = listOf(Pair(7, 6))  // oko wilka po boku

    private fun baseFrame(): Bitmap {
        val bmp = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        for (y in BASE.indices) {
            for (x in BASE[y].indices) {
                if (BASE[y][x] == '1') bmp.setPixel(x, y, Color.WHITE)
            }
        }
        return bmp
    }

    private fun addEyes(bmp: Bitmap) {
        EYES.forEach { (x, y) ->
            bmp.setPixel(x, y, Color.BLACK) // czarne oko, kontrast
        }
    }

    fun renderFrame(
        blink: Boolean = false,
        sleep: Boolean = false,
        happy: Boolean = false,
        dirty: Boolean = false
    ): Bitmap {
        val frame = baseFrame()
        if (!sleep) addEyes(frame)
        return frame
    }
}    )

    private val HEART = arrayOf(
        "01100110",
        "11111111",
        "11111111",
        "01111110",
        "00111100",
        "00011000",
        "00000000",
        "00000000",
    )

    private val ZED = arrayOf(
        "1110",
        "0010",
        "0100",
        "1110",
    )

    fun renderFrame(blink: Boolean, sleep: Boolean, happy: Boolean, dirty: Boolean): Bitmap {
        val b = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val base = if (blink || sleep) BLINK else OPEN

        for (y in 0 until H) for (x in 0 until W) b.setPixel(x, y, Color.TRANSPARENT)

        for (y in base.indices) {
            val row = base[y]
            for (x in row.indices) if (row[x] == '1') b.setPixel(x, y, Color.WHITE)
        }
        if (happy && !sleep) drawBitmap(b, HEART, W - 10, 2)
        if (sleep) drawBitmap(b, ZED, W - 6, 4)
        if (dirty) {
            for (i in 0 until 20) {
                val rx = (0 until W).random(); val ry = (0 until H).random()
                b.setPixel(rx, ry, Color.WHITE)
            }
        }
        return b
    }

    private fun drawBitmap(b: Bitmap, bmp: Array<String>, ox: Int, oy: Int) {
        for (y in bmp.indices) for (x in bmp[y].indices) {
            if (bmp[y][x] == '1') {
                val gx = ox + x; val gy = oy + y
                if (gx in 0 until W && gy in 0 until H) b.setPixel(gx, gy, Color.WHITE)
            }
        }
    }
}
