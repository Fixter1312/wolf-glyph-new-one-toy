
package com.example.wilczektoy

import android.graphics.Bitmap
import android.graphics.Color

object WolfSprites {
    private const val W = 25
    private const val H = 25

    private val OPEN = arrayOf(
        "000001111111111000000",
        "000111111111111110000",
        "001111111111111111000",
        "011111101110111011100",
        "111111111111111111110",
        "111110000010000001110",
        "111110011010110011110",
        "111110000010000001110",
        "111111111111111111110",
        "011111010000100111100",
        "001111111111111111000",
        "000111110011111110000",
        "000011111111111100000",
        "000001111111111000000",
        "000000111111110000000",
        "000000011111100000000",
        "000000001111000000000",
        "000000000110000000000",
        "000000000110000000000",
        "000000001111000000000",
        "000000011111100000000",
        "000000111111110000000",
        "000001111111111000000",
        "000011111111111100000",
        "000111111111111110000",
    )

    private val BLINK = arrayOf(
        "000001111111111000000",
        "000111111111111110000",
        "001111111111111111000",
        "011111101110111011100",
        "111111111111111111110",
        "111110000010000001110",
        "111110000000000011110",
        "111110000010000001110",
        "111111111111111111110",
        "011111010000100111100",
        "001111111111111111000",
        "000111110011111110000",
        "000011111111111100000",
        "000001111111111000000",
        "000000111111110000000",
        "000000011111100000000",
        "000000001111000000000",
        "000000000110000000000",
        "000000000110000000000",
        "000000001111000000000",
        "000000011111100000000",
        "000000111111110000000",
        "000001111111111000000",
        "000011111111111100000",
        "000111111111111110000",
    )

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
