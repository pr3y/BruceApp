package bruce.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.HttpAuthHandler
import android.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.ViewGroup
import android.text.InputType
import android.graphics.Color
import android.view.Gravity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class AndroidWebViewActivity : ComponentActivity() {
    private val darkBackgroundColor = Color.parseColor("#101010")
    private val purpleColor = Color.parseColor("#6200EE")
    private val whiteColor = Color.parseColor("#FFFFFF")
    private var triedDefaultAuth = false
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebViewScreen()
        }
    }

    @Composable
    fun WebViewScreen() {
        var showInstructionDialog by remember { mutableStateOf(true) }
        var showCredentialDialog by remember { mutableStateOf(false) }
        var username by remember { mutableStateOf("admin") }
        var password by remember { mutableStateOf("bruce") }

        if (showInstructionDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { 
                    Text(
                        "Bruce Device WebUI",
                        color = ComposeColor(0xFF6200EE),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = { 
                    Text(
                        "Go to Files > WebUI on your Bruce Device and Connect your phone to its WiFi.",
                        color = ComposeColor(0xFFFFFFFF),
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            showInstructionDialog = false
                            showCredentialDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ComposeColor(0xFF6200EE),
                            contentColor = ComposeColor(0xFFFFFFFF)
                        )
                    ) {
                        Text("Continue")
                    }
                },
                backgroundColor = ComposeColor(0xFF101010),
                contentColor = ComposeColor(0xFFFFFFFF)
            )
        }

        if (showCredentialDialog) {
            var tempUsername by remember { mutableStateOf(username) }
            var tempPassword by remember { mutableStateOf(password) }

            AlertDialog(
                onDismissRequest = { },
                title = { 
                    Text(
                        "HTTP Basic Authentication",
                        color = ComposeColor(0xFF6200EE),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = { 
                    Column {
                        OutlinedTextField(
                            value = tempUsername,
                            onValueChange = { tempUsername = it },
                            label = { Text("Username") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = ComposeColor(0xFFFFFFFF),
                                cursorColor = ComposeColor(0xFFFFFFFF),
                                focusedBorderColor = ComposeColor(0xFF6200EE),
                                unfocusedBorderColor = ComposeColor(0xFF666666),
                                focusedLabelColor = ComposeColor(0xFF6200EE),
                                unfocusedLabelColor = ComposeColor(0xFF666666)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempPassword,
                            onValueChange = { tempPassword = it },
                            label = { Text("Password") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = ComposeColor(0xFFFFFFFF),
                                cursorColor = ComposeColor(0xFFFFFFFF),
                                focusedBorderColor = ComposeColor(0xFF6200EE),
                                unfocusedBorderColor = ComposeColor(0xFF666666),
                                focusedLabelColor = ComposeColor(0xFF6200EE),
                                unfocusedLabelColor = ComposeColor(0xFF666666)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            if (tempUsername.isNotBlank() && tempPassword.isNotBlank()) {
                                username = tempUsername
                                password = tempPassword
                                showCredentialDialog = false
                                showWebViewWithUserAuth(username, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ComposeColor(0xFF6200EE),
                            contentColor = ComposeColor(0xFFFFFFFF)
                        )
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { finish() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ComposeColor(0xFF6200EE),
                            contentColor = ComposeColor(0xFFFFFFFF)
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                backgroundColor = ComposeColor(0xFF101010),
                contentColor = ComposeColor(0xFFFFFFFF)
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWebViewWithUserAuth(username: String, password: String) {
        triedDefaultAuth = false
        webView = WebView(this)
        webView?.webViewClient = object : WebViewClient() {
            override fun onReceivedHttpAuthRequest(
                view: WebView,
                handler: HttpAuthHandler,
                host: String,
                realm: String
            ) {
                if (host == "bruce.local") {
                    if (!triedDefaultAuth) {
                        triedDefaultAuth = true
                        handler.proceed("admin", "bruce")
                    } else {
                        handler.cancel()
                        // Show credential dialog again if needed
                        setContent {
                            WebViewScreen()
                        }
                    }
                } else {
                    handler.cancel()
                }
            }
        }
        webView?.settings?.javaScriptEnabled = true
        webView?.loadUrl("http://bruce.local")
        setContentView(webView)
    }
} 