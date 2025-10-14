com.example.wilczektoy        glod = (glod + 7).coerceAtMost(100)
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
