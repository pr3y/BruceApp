package bruce.app

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var instance: MainActivity
            private set
        private const val TAG = "MainActivity"
    }

    private lateinit var serialCommunication: SerialCommunication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        
        Log.d(TAG, "onCreate: Initializing app")
        serialCommunication = AndroidSerialCommunication(this)

        // List available USB devices
        val usbManager = getSystemService(UsbManager::class.java)
        val deviceList = usbManager.deviceList
        Log.d(TAG, "Available USB devices: ${deviceList.size}")
        deviceList.forEach { (name, device) ->
            Log.d(TAG, "USB Device: name=$name, vid=${device.vendorId}, pid=${device.productId}")
        }

        // Handle USB device attached intent
        if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            Log.d(TAG, "USB device attached via intent")
            serialCommunication.connect()
        }

        setContent {
            App()
        }

        // Explicitly try to connect when the app starts
        Log.d(TAG, "Attempting initial USB connection")
        serialCommunication.connect()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Attempting USB reconnection")
        serialCommunication.connect()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ${intent.action}")
        if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            Log.d(TAG, "USB device attached via onNewIntent")
            serialCommunication.connect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Disconnecting USB")
        serialCommunication.disconnect()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}