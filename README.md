# Set gif as a Live Wallpaper proof of concept

### How to run the project
Add the GIF_URL key to the *local.properties* file:

>```GIF_URL=your_gif_url_without_quotes```

### About
A proof of concept project showing how to set a gif file downloaded from Internet as a Live Wallpaper on Android.

#### Note
Not using `AnimatedImageDrawable` from the standard Android SDK since that would require **minSdk 28**. Using `pl.droidsonroids.gif.GifDrawable` from
https://github.com/koral--/android-gif-drawable allows having **minSdk 23**.
  
There is also the option of using the older and the deprecated `android.graphics.Movie` but it has been reported for the lack of control over scaling and for being less efficient. 
