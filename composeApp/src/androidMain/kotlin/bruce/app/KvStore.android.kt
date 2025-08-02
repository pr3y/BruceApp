package bruce.app

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

actual class KvStore {
    private var prefs: SharedPreferences? = null

    @Composable
    fun InitPreferenceManager() {
        if(prefs == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
    }

    @Composable
    actual fun write(key: String, value: String) {
        InitPreferenceManager()
        prefs?.edit {
            putString(key, value)
        }
    }

    @Composable
    actual fun read(key: String): String? {
        InitPreferenceManager()
        return prefs?.getString(key, null)
    }
}