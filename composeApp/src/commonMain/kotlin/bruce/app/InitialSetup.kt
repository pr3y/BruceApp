package bruce.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import bruce.composeapp.generated.resources.Res
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import bruce.composeapp.generated.resources.bruce_aquarium

@Composable
fun InitialSetup(navController: NavController, bleConnection: BLEConnection) {
    val usbConnection = remember { mutableStateOf(false) }
    val bleClicked = remember { mutableStateOf(false) }


    MaterialTheme(
        colorScheme = Style.scheme

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()      // fill the available space
                .wrapContentSize()  // make the box only as big as its children
            ) {
                Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // centres children
                modifier = Modifier.align(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(Res.drawable.bruce_aquarium),
                    contentDescription = "Bruce Logo",
                    modifier = Modifier.size(300.dp)
                )
                Text("Welcome to Bruce App!", modifier = Modifier.padding(top=10.dp))
                Text("Please select the type of connection that you wanna use with your Bruce device.", modifier=Modifier.padding(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { usbConnection.value = true }) {
                        Text("USB")
                    }
                    if(getPlatform().name.contains("Android")) {
                        Button(onClick = {
                            bleClicked.value = true
                        }) {
                            Text("BLE")
                        }
                    }
                }
            }
        }
    }

    if(usbConnection.value) {
        App()
    }

    if(bleClicked.value) {
        bleConnection.Setup(navController)
    }
}