package bruce.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import kotlinx.coroutines.delay

expect class BLEConnection {
    @Composable
    fun Setup()

    @Composable
    fun Setup(navController: NavController)

    fun scanDevices()

    fun getScanResult() : List<BLEDevice>

    fun StopScan()

    @Composable
    fun ConnectToDevice(address: String)

    fun isBLEConnected(): Boolean

    suspend fun queryDevice(service: String, char: String): ByteArray

    fun writeToDevice(service: String, char: String, data: ByteArray)
}

data class BLEDevice (
    val name: String?,
    val address: String
)

expect fun initBLEConnection(): BLEConnection

@Composable
fun ConnectToBLEDevice(bleConnection: BLEConnection, navController: NavController, deviceAddress: String, showDialog: Boolean = true) {
    val isConnected = remember { mutableStateOf(false) }

    if(deviceAddress != "") {
        if(!isConnected.value && showDialog) {
            AlertDialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false,
                    dismissOnClickOutside = false),
                title = null,
                confirmButton = {},
                text = {
                    Column (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Please wait…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }

        bleConnection.ConnectToDevice(deviceAddress)

        val storeDevice = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            while(!isConnected.value) {
                isConnected.value = bleConnection.isBLEConnected()
                delay(300)
            }

            storeDevice.value = true
            navController.navigate(Pages.NewMainPage)
        }
    }
}