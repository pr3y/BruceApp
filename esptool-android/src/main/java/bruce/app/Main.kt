package bruce.app

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

private const val ACTION_USB_PERMISSION = "bruce.app.USB_PERMISSION"

class Main {
    fun uploadFirmware(
        context: Context,
        arguments: String
    ): String {


        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        // Get the list of connected USB devices
        val deviceList = usbManager.deviceList.values.toList()

        if (deviceList.isEmpty()) {
            return ("No USB devices connected.")
        }

        // Check for permissions and upload firmware
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (device in deviceList) {
            if (!usbManager.hasPermission(device)) {
                usbManager.requestPermission(device, permissionIntent)
                return ("Requesting permission for ${device.deviceName}...")
            }
        }

        // Initialize Python
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        val python = Python.getInstance()
        val pyScript = python.getModule("upload_firmware")


        try {
            // Call the Python function and get the return value
            val result = pyScript.callAttr("upload_firmware", context, arguments)
            
            return if (result != null) {
                result.toString()
            } else {
                "Success"
            }
        } catch (e: SecurityException) {
            usbManager.deviceList.values.forEach { device ->
                if (!usbManager.hasPermission(device)) {
                    usbManager.requestPermission(device, permissionIntent)
                }
            }
            return ("Requesting USB permission...")
        } catch (e: Exception) {
            Log.e("UploadError", "Error uploading firmware", e)
            return ("Upload failed: ${e.message}")
        }
    }

}




