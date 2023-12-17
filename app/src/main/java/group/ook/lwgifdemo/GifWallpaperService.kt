package group.ook.lwgifdemo

import android.graphics.Canvas
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import pl.droidsonroids.gif.GifDrawable
import java.io.File

class GifWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return GifWallpaperEngine()
    }

    inner class GifWallpaperEngine : WallpaperService.Engine() {
        private lateinit var gifDrawable: GifDrawable
        private val handler = Handler()
        private var visible = false
        private val frameDelay: Long = 16 /*for 60 fps*/

        private val drawGif = object : Runnable {
            override fun run() {
                draw()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            initializeGif()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(drawGif)
            } else {
                handler.removeCallbacks(drawGif)
            }
        }

        private fun initializeGif() {
            val file = File(applicationContext.filesDir, Constants.LOCAL_GIF_FILENAME)
            if (file.exists()) {
                gifDrawable = GifDrawable(file.path)
            }
        }

        private fun draw() {
            val surfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null && ::gifDrawable.isInitialized) {
                    gifDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    gifDrawable.draw(canvas)
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }

            /*schedule the next frame*/
            handler.removeCallbacks(drawGif)
            if (visible) {
                handler.postDelayed(drawGif, frameDelay)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawGif)
        }
    }

}
