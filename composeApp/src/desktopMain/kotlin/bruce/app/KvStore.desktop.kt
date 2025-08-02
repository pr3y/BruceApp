package bruce.app

import androidx.compose.runtime.Composable

actual class KvStore {
    @Composable
    actual fun write(key: String, value: String) {
    }

    @Composable
    actual fun read(key: String): String? {
        TODO("Not yet implemented")
    }
}