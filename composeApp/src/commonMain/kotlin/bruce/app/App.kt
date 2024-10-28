package bruce.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.fazecast.jSerialComm.SerialPort
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.OutputStream

@Composable
fun optionsScreen(onSendCommand: (String) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { onSendCommand("say Hello") }) {
            Text("Say")
        }
        Button(onClick = { onSendCommand("music_player doom:d=4,o=5,b=112:16e4,16e4,16e,16e4,16e4,16d,16e4,16e4,16c,16e4,16e4,16a#4,16e4,16e4,16b4,16c,16e4,16e4,16e,16e4,16e4,16d,16e4,16e4,16c,16e4,16e4,a#4") }) {
            Text("Play Doom Song!")
        }
    }
}

@Composable
@Preview
fun App() {
    val darkBackgroundColor = Color(0xFF121212)

    MaterialTheme {
        var updateFirmware by remember { mutableStateOf(false) }
        var showSerialCmds by remember { mutableStateOf(false) }

        Box(
            Modifier
                .fillMaxSize()
                .background(darkBackgroundColor)
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { updateFirmware = !updateFirmware }) {
                    Text("Update Firmware")
                }
                Button(onClick = { showSerialCmds = !showSerialCmds }) {
                    Text("USB Serial")
                }
                AnimatedVisibility(updateFirmware) {
                    //TODO: Add GET request to .bin of Bruce
                    Text("Updating Bruce.")
                }
                if (showSerialCmds) {
                    optionsScreen { command ->
                        sendSerialCommand(command)
                    }
                }
            }
        }
    }
}

fun listAvailablePorts() {
    val ports = SerialPort.getCommPorts()
    if (ports.isEmpty()) {
        println("No serial ports found.")
    } else {
        ports.forEachIndexed { index, port ->
            println("${index + 1}: ${port.descriptivePortName} (System port: ${port.systemPortName})")
        }
    }
}

fun selectAndOpenPort(): SerialPort? {
    val ports = SerialPort.getCommPorts()
    listAvailablePorts()

    for ((index, port) in ports.withIndex()) {
        println("${index + 1}: ${port.descriptivePortName}")
        if (port.descriptivePortName.contains("USB JTAG/serial debug unit", ignoreCase = true)) {
            println("Attempting to open port: ${port.descriptivePortName}")

            port.setComPortParameters(9600, 8, 1, 0) // baud rate

            if (port.openPort()) {
                println("Opened port: ${port.descriptivePortName}")
                return port
            } else {
                println("Failed to open port: ${port.descriptivePortName}")
            }
        }
    }

    println("Could not find the specified USB JTAG/serial debug unit port.")
    return null
}


fun sendSerialCommand(command: String) {
    val port = selectAndOpenPort() // Get the selected port

    if (port != null) {
        try {
            val commandWithNewline = "$command\n"
            val outputStream: OutputStream = port.outputStream
            outputStream.write(commandWithNewline.toByteArray())
            outputStream.flush()
            println("Command: $commandWithNewline")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            port.closePort()
            println("Closed port.")
        }
    } else {
        println("Could not open the serial port.")
    }
}
