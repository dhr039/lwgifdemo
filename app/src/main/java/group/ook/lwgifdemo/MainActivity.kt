package group.ook.lwgifdemo

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberImagePainter
import group.ook.lwgifdemo.ui.theme.LwgifdemoTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LwgifdemoTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val file = File(context.filesDir, "gif_wallpaper.gif")

    val painter = if (file.exists()) {
        rememberImagePainter(data = file)
    } else {
        rememberImagePainter(data = Constants.GIF_URL)
    }

    LaunchedEffect(Unit) {
        downloadAndSaveGif(context)
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, GifWallpaperService::class.java)
            )
            context.startActivity(intent)
        }) {
            Text("Set as Live Wallpaper")
        }
        Image(
            painter = painter,
            contentDescription = "Live Wallpaper Image",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun downloadAndSaveGif(context: Context) {
    val url = Constants.GIF_URL
    val client = OkHttpClient()

    val request = Request.Builder().url(url).build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("downloadAndSaveGif", e.message.toString())
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                response.body?.let { responseBody ->
                    val inputStream = responseBody.byteStream()
                    val bufferedBytes = inputStream.readBytes()
                    saveGifToLocalFile(bufferedBytes, context)
                }
            }
        }
    })
}

fun saveGifToLocalFile(bufferedBytes: ByteArray, context: Context) {
    val file = File(context.filesDir, Constants.LOCAL_GIF_FILENAME)
    file.writeBytes(bufferedBytes)
}