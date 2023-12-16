package group.ook.lwgifdemo

import android.graphics.Canvas
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import pl.droidsonroids.gif.GifDrawable
import java.io.ByteArrayInputStream
import java.io.IOException

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
            val url = Constants.GIF_URL
            val client = OkHttpClient()

            val request = Request.Builder().url(url).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("GifWallpaperEngine", e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        response.body?.let { responseBody ->
                            val inputStream = responseBody.byteStream()
                            val bufferedBytes = inputStream.readBytes() /*buffer the entire stream*/
                            val byteArrayInputStream = ByteArrayInputStream(bufferedBytes)

                            /*use ByteArrayInputStream to create GifDrawable*/
                            gifDrawable = GifDrawable(byteArrayInputStream)
                        }
                    } else {
                        Log.e("GifWallpaperEngine", "OkHttp response error")
                    }
                }
            })
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
