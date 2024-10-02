package bruce.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box


import bruce.composeapp.generated.resources.Res
import bruce.composeapp.generated.resources.compose_multiplatform



@Composable
fun BLEScreen() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { /* handle say action */ }) {
            Text("Say")
        }
        Button(onClick = { /* handle ir action */ }) {
            Text("IR")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("RF")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("SUBGHZ")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("MUSIC_PLAYER")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("LED")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("POWER")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("TONE")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("GPIO")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("I2C")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("STORAGE")
        }
        Button(onClick = { /* handle rf action */ }) {
            Text("SETTINGS")
        }

    }
}

@Composable
@Preview
fun App() {
    val darkBackgroundColor = Color(0xFF121212) // Replace with your hex color

    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var showBLEScreen by remember { mutableStateOf(false) }

        Box(
            Modifier
                .fillMaxSize()
                .background(darkBackgroundColor) // Set the hex color here for the whole background
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { showContent = !showContent }) {
                    Text("Update Firmware")
                }
                Button(onClick = { showBLEScreen = !showBLEScreen }) {
                    Text("BLE Serial")
                }
                AnimatedVisibility(showContent) {
                    val greeting = remember { Greeting().greet() }
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painterResource(Res.drawable.compose_multiplatform), null)
                        Text("Compose: $greeting")
                    }
                }
                if (showBLEScreen) {
                    BLEScreen()
                }
            }
        }
    }
}
