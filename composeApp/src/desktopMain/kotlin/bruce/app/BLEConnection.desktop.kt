package bruce.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

actual  class BLEConnection {
    @Composable
    actual  fun Setup(navController: NavController) {}
    actual  fun scanDevices() {}
    actual  fun getScanResult(): List<BLEDevice> {
        return emptyList()
    }
    actual fun StopScan() {}
    @Composable
    actual fun ConnectToDevice(address: String) {}
}

actual fun initBLEConnection(): BLEConnection {
    TODO("Not yet implemented")
}