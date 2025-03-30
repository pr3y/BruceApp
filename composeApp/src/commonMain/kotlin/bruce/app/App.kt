package bruce.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TerminalWindow(
    modifier: Modifier = Modifier,
    output: String
) {
    Box(
        modifier = modifier
            .background(Color.Black)
            .padding(8.dp)
    ) {
        Text(
            text = output,
            color = Color.LightGray,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
fun BaudrateDialog(
    onDismiss: () -> Unit,
    onBaudrateSelected: (Int) -> Unit,
    selectedBaudRate: Int
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Serial Settings") },
        text = {
            Column {
                Text("Baud Rate:")
                val baudRates = listOf(9600, 19200, 38400, 57600, 115200)
                baudRates.forEach { rate ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedBaudRate == rate,
                            onClick = { onBaudrateSelected(rate) }
                        )
                        Text("$rate")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CustomCommandDialog(
    onDismiss: () -> Unit,
    onSendCommand: (String) -> Unit
) {
    var commandText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Command") },
        text = {
            OutlinedTextField(
                value = commandText,
                onValueChange = { commandText = it },
                label = { Text("Enter command") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF6200EE),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF6200EE),
                    unfocusedLabelColor = Color.Gray
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commandText.isNotBlank()) {
                        onSendCommand(commandText)
                        onDismiss()
                    }
                },
                enabled = commandText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6200EE),
                    contentColor = Color.White
                )
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6200EE),
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun optionsScreen(onSendCommand: (String) -> Unit) {
    var showCustomCommandDialog by remember { mutableStateOf(false) }
    
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { onSendCommand("say Hello") },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE),
                contentColor = Color.White
            )
        ) {
            Text("Say")
        }
        Button(
            onClick = { onSendCommand("music_player doom:d=4,o=5,b=112:16e4,16e4,16e,16e4,16e4,16d,16e4,16e4,16c,16e4,16e4,16a#4,16e4,16e4,16b4,16c,16e4,16e4,16e,16e4,16e4,16d,16e4,16e4,16c,16e4,16e4,a#4") },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE),
                contentColor = Color.White
            )
        ) {
            Text("Play Doom Song!")
        }
        Button(
            onClick = { showCustomCommandDialog = true },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE),
                contentColor = Color.White
            )
        ) {
            Text("Custom Command")
        }
    }

    if (showCustomCommandDialog) {
        CustomCommandDialog(
            onDismiss = { showCustomCommandDialog = false },
            onSendCommand = onSendCommand
        )
    }
}

@Composable
fun TerminalOutput(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
            .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Terminal Output",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                val clipboardManager = LocalClipboardManager.current
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(text))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy to clipboard",
                        tint = Color.White
                    )
                }
            }
            Divider(color = Color.Gray, thickness = 1.dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun App() {
    val darkBackgroundColor = Color(0xFF121212)
    val purpleColor = Color(0xFF6200EE)
    val serialCommunication = remember { getSerialCommunication() }
    val firmwareUpdater = remember { getFirmwareUpdater() }
    var showSettings by remember { mutableStateOf(false) }
    var selectedBaudRate by remember { mutableStateOf(9600) }
    var terminalOutput by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var showSerialCmds by remember { mutableStateOf(false) }
    var updateFirmware by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        serialCommunication.setOutputListener { output ->
            terminalOutput += "$output\n"
        }
        onDispose {
            serialCommunication.disconnect()
        }
    }

    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = purpleColor,
            primaryVariant = purpleColor,
            secondary = purpleColor,
            secondaryVariant = purpleColor,
            background = darkBackgroundColor,
            surface = darkBackgroundColor,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(darkBackgroundColor)
        ) {
            // Settings button in top-right corner
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }

            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top section with buttons
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { 
                            if (!isUpdating) {
                                isUpdating = true
                                updateFirmware = true
                                terminalOutput = "" // Clear terminal
                                firmwareUpdater.downloadAndUpdate(
                                    onProgress = { message ->
                                        terminalOutput += "$message\n"
                                    },
                                    onError = { error ->
                                        terminalOutput += "ERROR: $error\n"
                                        isUpdating = false
                                        updateFirmware = false
                                    },
                                    onSuccess = {
                                        terminalOutput += "Firmware update completed successfully!\n"
                                        isUpdating = false
                                        updateFirmware = false
                                    }
                                )
                            }
                        },
                        enabled = !isUpdating,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = purpleColor,
                            contentColor = Color.White
                        )
                    ) {
                        if (isUpdating) {
                            Text("Updating...")
                        } else {
                            Text("Update Firmware")
                        }
                    }
                    Button(
                        onClick = { showSerialCmds = !showSerialCmds },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = purpleColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("USB Serial")
                    }
                    if (updateFirmware) {
                        Text("Updating Bruce.")
                    }
                    if (showSerialCmds) {
                        optionsScreen { command ->
                            serialCommunication.sendCommand(command)
                            terminalOutput += "> $command\n"
                        }
                    }
                }

                // Terminal window at the bottom
                TerminalOutput(terminalOutput)
            }
            
            if (showSettings) {
                BaudrateDialog(
                    onDismiss = { showSettings = false },
                    onBaudrateSelected = { baudRate ->
                        selectedBaudRate = baudRate
                        serialCommunication.setBaudRate(baudRate)
                        terminalOutput += "Setting baud rate to $baudRate\n"
                    },
                    selectedBaudRate = selectedBaudRate
                )
            }
        }
    }
}
