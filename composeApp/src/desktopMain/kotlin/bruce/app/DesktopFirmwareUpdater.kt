package bruce.app

import kotlinx.coroutines.*
import java.io.*
import java.net.URL
import com.fazecast.jSerialComm.SerialPort

class DesktopFirmwareUpdater : FirmwareUpdater {
    private var updateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun downloadAndUpdate(
        onProgress: (String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        updateJob = scope.launch {
            try {
                onProgress("Starting firmware update...")
                
                // Check if Python and esptool are available
                if (!checkPythonAndEsptool(onProgress, onError)) {
                    return@launch
                }
                
                // Download firmware
                onProgress("Downloading firmware...")
                val firmwareFile = downloadFirmware()
                onProgress("Firmware downloaded to: ${firmwareFile.absolutePath}")
                
                // Find ESP32 port
                onProgress("Looking for ESP32 device...")
                val port = findESP32Port()
                
                if (port == null) {
                    onError("ESP32 device not found. Please connect the device and try again.")
                    return@launch
                }
                
                val portPath = getPortPath(port)
                onProgress("ESP32 device found at $portPath")
                
                // Execute esptool.py commands
                val command = listOf(
                    "python", "-m", "esptool",
                    "--chip", "esp32s3",
                    "--port", portPath,
                    "--baud", "460800",
                    "--before", "default_reset",
                    "--after", "hard_reset",
                    "write_flash",
                    "-z",
                    "--flash_mode", "dio",
                    "--flash_freq", "80m",
                    "--flash_size", "4MB",
                    "0x0", firmwareFile.absolutePath
                )
                
                onProgress("Executing flash command...")
                val process = ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start()
                
                // Read output in real-time
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    onProgress(line ?: "")
                }
                
                val exitCode = process.waitFor()
                if (exitCode != 0) {
                    throw Exception("Flash process failed with exit code $exitCode")
                }
                
                onSuccess()
                
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error during firmware update: ${e.message}")
            }
        }
    }
    
    private fun checkPythonAndEsptool(onProgress: (String) -> Unit, onError: (String) -> Unit): Boolean {
        try {
            // Check Python
            val pythonProcess = Runtime.getRuntime().exec("python --version")
            if (pythonProcess.waitFor() != 0) {
                onError("Python is not installed. Please install Python 3.x")
                return false
            }
            
            // Check esptool
            val esptoolProcess = Runtime.getRuntime().exec("python -m esptool version")
            if (esptoolProcess.waitFor() != 0) {
                onProgress("Installing esptool...")
                val pipProcess = Runtime.getRuntime().exec("pip install esptool")
                if (pipProcess.waitFor() != 0) {
                    onError("Failed to install esptool. Please install it manually with: pip install esptool")
                    return false
                }
            }
            
            return true
        } catch (e: Exception) {
            onError("Error checking Python/esptool: ${e.message}")
            return false
        }
    }
    
    override fun cancelUpdate() {
        updateJob?.cancel()
        updateJob = null
    }
    
    private suspend fun downloadFirmware(): File = withContext(Dispatchers.IO) {
        val url = URL(FirmwareUpdater.FIRMWARE_URL)
        val connection = url.openConnection()
        val inputStream = BufferedInputStream(connection.getInputStream())
        
        val tempDir = System.getProperty("java.io.tmpdir")
        val outputFile = File(tempDir, "firmware.bin")
        FileOutputStream(outputFile).use { output ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
        }
        
        return@withContext outputFile
    }
    
    private fun findESP32Port(): SerialPort? {
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        val isMac = System.getProperty("os.name").lowercase().contains("mac")
        
        return SerialPort.getCommPorts().find { port ->
            val portName = port.systemPortName
            val descriptiveName = port.descriptivePortName
            
            // Debug logging
            println("Found port: $portName (${port.descriptivePortName})")
            
            when {
                isWindows -> {
                    // Windows uses COM ports
                    portName.startsWith("COM")
                }
                isMac -> {
                    // macOS uses /dev/tty.* or /dev/cu.*
                    portName.contains("tty.") || portName.contains("cu.")
                }
                else -> {
                    // Linux - check for ttyUSB* or ttyACM* without /dev/ prefix
                    portName.contains("ttyUSB") || portName.contains("ttyACM")
                }
            } && (
                descriptiveName.contains("CP210", ignoreCase = true) ||
                descriptiveName.contains("CH340", ignoreCase = true) ||
                descriptiveName.contains("USB", ignoreCase = true) ||
                descriptiveName.contains("UART", ignoreCase = true) ||
                descriptiveName.contains("Serial", ignoreCase = true)
            )
        }?.also {
            println("Selected port: ${it.systemPortName} (${it.descriptivePortName})")
        }
    }

    private fun getPortPath(port: SerialPort): String {
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        val isMac = System.getProperty("os.name").lowercase().contains("mac")
        
        return when {
            isWindows -> port.systemPortName
            else -> "/dev/${port.systemPortName}"  // Add /dev/ prefix for Linux and macOS
        }
    }
} 