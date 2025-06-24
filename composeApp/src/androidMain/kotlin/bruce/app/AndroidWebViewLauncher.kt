package bruce.app

import android.content.Intent

actual fun launchAndroidWebView() {
    val context = MainActivity.instance
    val intent = Intent(context, AndroidWebViewActivity::class.java)
    context.startActivity(intent)
} 