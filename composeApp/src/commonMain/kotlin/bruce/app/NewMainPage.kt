package bruce.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
fun newMainPage(navController: NavController, bleConnection: BLEConnection) {
    val deviceInfo = remember { mutableStateOf(DeviceInfo("", "", "", "", "", "")) }

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
            }
        }
    }

    LaunchedEffect(Unit) {
        val deviceInfoRaw = bleConnection.queryDevice("f971c8aa-7c27-42f4-a718-83b97329130c", "e1884dc6-3d67-43fb-8be2-9ad88cc8ba7e").toString(Charsets.UTF_8)
        deviceInfo.value = Json.decodeFromString<DeviceInfo>(deviceInfoRaw)
        println(deviceInfo.value)
    }
}