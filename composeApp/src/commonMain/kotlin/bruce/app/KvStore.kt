package bruce.app

import androidx.compose.runtime.Composable

expect class KvStore() {
    @Composable
    fun write(key: String, value: String)
    @Composable
    fun read(key: String): String?
}