package bruce.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.serialization.json.Json

@Composable
fun NewMainPage(navController: NavController, bleConnection: BLEConnection) {
    val deviceInfo = remember { mutableStateOf(DeviceInfo("", "", "", "", "", "")) }
    val shutdownPayload = byteArrayOf(0x00)
    val rebootPayload = byteArrayOf(0x01)

    MaterialTheme(
        colorScheme = Style.scheme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()      // fill the available space
                .wrapContentSize()  // make the box only as big as its children
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Text("Version: ${deviceInfo.value.version}")
                Text("Device: ${deviceInfo.value.device}")
                Text("SDK: ${deviceInfo.value.sdk}")
                Text("MAC Address: ${deviceInfo.value.mac}")
                Text("WiFi: ${deviceInfo.value.wifi_ip}")
                Row {
                    Button({
                        bleConnection.writeToDevice("0134b0a9-d14f-40b3-a595-4056062a33bd", "aa2095ec-e710-4462-b9af-93a133410a29", shutdownPayload)
                    }) {
                        Text("Shutdown")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button({
                        bleConnection.writeToDevice("0134b0a9-d14f-40b3-a595-4056062a33bd", "aa2095ec-e710-4462-b9af-93a133410a29", rebootPayload)
                    }) {
                        Text("Reboot")
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val deviceInfoRaw = bleConnection.queryDevice("f971c8aa-7c27-42f4-a718-83b97329130c", "e1884dc6-3d67-43fb-8be2-9ad88cc8ba7e").toString(Charsets.UTF_8)
        deviceInfo.value = Json.decodeFromString<DeviceInfo>(deviceInfoRaw)
        println(deviceInfo.value)
    }
}