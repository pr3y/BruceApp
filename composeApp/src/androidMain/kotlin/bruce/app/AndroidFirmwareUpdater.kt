package bruce.app

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.hardware.usb.*
import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

class AndroidFirmwareUpdater(private val context: Context) : FirmwareUpdater {
    private var updateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "FirmwareUpdater"
    
    // ESP32-S3 ROM bootloader commands
    private object ESP {
        const val SYNC = 0x08
        const val FLASH_BEGIN = 0x02
        const val FLASH_DATA = 0x03
        const val FLASH_END = 0x04
        const val MEM_BEGIN = 0x05
        const val MEM_END = 0x06
        const val MEM_DATA = 0x07
        const val SPI_SET_PARAMS = 0x0B
        const val SPI_ATTACH = 0x0D
        const val CHANGE_BAUDRATE = 0x0F
        const val FLASH_DEFL_BEGIN = 0x10
        const val FLASH_DEFL_DATA = 0x11
        const val FLASH_DEFL_END = 0x12
        
        // ESP32-S3 specific
        const val CHIP_DETECT = 0x09
        const val READ_REG = 0x0A
        
        const val FLASH_SECTOR_SIZE = 4096
        const val BOOTLOADER_FLASH_OFFSET = 0x0  // ESP32-S3 starts at 0x0
        const val FLASH_WRITE_SIZE = 0x400  // Write in 1K chunks
        
        // Sync pattern
        val SYNC_PATTERN = byteArrayOf(
            0xc0.toByte(), 0x00, 0x08, 0x24,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x07, 0x07, 0x12,
            0x20
        )
        
        // Response codes
        const val RESPONSE_OK = 0
        const val RESPONSE_FAILED = 1
    }
    
    override fun downloadAndUpdate(
        onProgress: (String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        updateJob = scope.launch {
            try {
                onProgress("Starting firmware update...")
                
                // Download firmware
                onProgress("Downloading firmware...")
                val firmwareFile = downloadFirmware()
                val firmwareBytes = firmwareFile.readBytes()
                onProgress("Firmware downloaded: ${firmwareBytes.size} bytes")
                
                // Find ESP32 device
                onProgress("Looking for ESP32-S3 device...")
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                val device = findESP32Device(usbManager)
                
                if (device == null) {
                    onError("ESP32-S3 device not found. Please connect the device and try again.")
                    return@launch
                }
                
                onProgress("ESP32-S3 device found. Requesting USB permissions...")
                if (!requestUsbPermission(usbManager, device)) {
                    onError("Failed to get USB permissions. Please grant USB access to the app.")
                    return@launch
                }
                
                onProgress("USB permissions granted. Starting flash process...")
                
                val connection = usbManager.openDevice(device)
                if (connection == null) {
                    onError("Failed to open USB connection")
                    return@launch
                }
                
                try {
                    // Find and claim interface
                    val intf = device.getInterface(0)
                    if (!connection.claimInterface(intf, true)) {
                        onError("Failed to claim interface")
                        return@launch
                    }
                    
                    // Find endpoints
                    val inEndpoint = intf.getEndpoint(0)
                    val outEndpoint = intf.getEndpoint(1)
                    
                    // Put ESP32-S3 in bootloader mode
                    onProgress("Putting ESP32-S3 in bootloader mode...")
                    connection.controlTransfer(
                        UsbConstants.USB_TYPE_VENDOR or UsbConstants.USB_DIR_OUT,
                        0x00,  // Reset command
                        0x00,
                        0x00,
                        null,
                        0,
                        5000
                    )
                    
                    delay(100)  // Wait for reset
                    
                    // Sync with bootloader
                    onProgress("Syncing with bootloader...")
                    if (!syncBootloader(connection, outEndpoint, inEndpoint)) {
                        onError("Failed to sync with bootloader")
                        return@launch
                    }
                    
                    // Attach SPI flash
                    onProgress("Attaching SPI flash...")
                    if (!sendCommand(connection, outEndpoint, inEndpoint, ESP.SPI_ATTACH, ByteArray(0))) {
                        onError("Failed to attach SPI flash")
                        return@launch
                    }
                    
                    // Set SPI parameters
                    onProgress("Setting SPI parameters...")
                    val spiParams = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN).apply {
                        putInt(0)      // id
                        putInt(40000000)  // clock speed Hz
                        putInt(0)      // mode
                        putInt(0xffff) // command length
                        putInt(0xffff) // address length
                        putInt(0xffff) // dummy length
                    }.array()
                    
                    if (!sendCommand(connection, outEndpoint, inEndpoint, ESP.SPI_SET_PARAMS, spiParams)) {
                        onError("Failed to set SPI parameters")
                        return@launch
                    }
                    
                    // Start flash process
                    val totalSize = firmwareBytes.size
                    val numSectors = (totalSize + ESP.FLASH_SECTOR_SIZE - 1) / ESP.FLASH_SECTOR_SIZE
                    
                    onProgress("Erasing flash...")
                    if (!sendFlashBegin(connection, outEndpoint, inEndpoint, totalSize, numSectors)) {
                        onError("Failed to begin flash process")
                        return@launch
                    }
                    
                    // Write firmware in chunks
                    onProgress("Writing firmware...")
                    var bytesWritten = 0
                    val chunkSize = ESP.FLASH_WRITE_SIZE
                    
                    while (bytesWritten < totalSize) {
                        val remaining = totalSize - bytesWritten
                        val currentChunkSize = minOf(chunkSize, remaining)
                        val chunk = firmwareBytes.copyOfRange(bytesWritten, bytesWritten + currentChunkSize)
                        
                        if (!sendFlashData(connection, outEndpoint, inEndpoint, chunk, bytesWritten)) {
                            onError("Failed to write firmware at offset $bytesWritten")
                            return@launch
                        }
                        
                        bytesWritten += currentChunkSize
                        val progress = (bytesWritten.toFloat() / totalSize * 100).toInt()
                        onProgress("Progress: $progress%")
                    }
                    
                    onProgress("Finishing up...")
                    if (!sendFlashEnd(connection, outEndpoint, inEndpoint)) {
                        onError("Failed to finish flash process")
                        return@launch
                    }
                    
                    onSuccess()
                    
                } finally {
                    connection.close()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during firmware update", e)
                onError("Error during firmware update: ${e.message}")
            }
        }
    }
    
    private fun syncBootloader(
        connection: UsbDeviceConnection,
        outEndpoint: UsbEndpoint,
        inEndpoint: UsbEndpoint
    ): Boolean {
        // Send sync pattern
        for (i in 0..7) {  // Try multiple times
            connection.bulkTransfer(outEndpoint, ESP.SYNC_PATTERN, ESP.SYNC_PATTERN.size, 5000)
            
            // Read response
            val response = ByteArray(8)
            val len = connection.bulkTransfer(inEndpoint, response, response.size, 1000)
            
            if (len > 0 && response[0] == 0x00.toByte()) {
                return true
            }
            delay(100)
        }
        return false
    }
    
    private fun sendCommand(
        connection: UsbDeviceConnection,
        outEndpoint: UsbEndpoint,
        inEndpoint: UsbEndpoint,
        command: Int,
        data: ByteArray
    ): Boolean {
        val buffer = ByteBuffer.allocate(8 + data.size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(0x00.toByte())  // direction
        buffer.put(command.toByte())
        buffer.putShort(data.size.toShort())
        buffer.putInt(checksum(data))  // checksum
        buffer.put(data)
        
        val request = buffer.array()
        if (connection.bulkTransfer(outEndpoint, request, request.size, 5000) < 0) {
            return false
        }
        
        // Read response
        val response = ByteArray(8)
        val len = connection.bulkTransfer(inEndpoint, response, response.size, 5000)
        return len > 0 && response[0] == ESP.RESPONSE_OK.toByte()
    }
    
    private fun sendFlashBegin(
        connection: UsbDeviceConnection,
        outEndpoint: UsbEndpoint,
        inEndpoint: UsbEndpoint,
        size: Int,
        numSectors: Int
    ): Boolean {
        val buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(size)
        buffer.putInt(numSectors)
        buffer.putInt(ESP.FLASH_SECTOR_SIZE)
        buffer.putInt(ESP.BOOTLOADER_FLASH_OFFSET)
        
        return sendCommand(connection, outEndpoint, inEndpoint, ESP.FLASH_BEGIN, buffer.array())
    }
    
    private fun sendFlashData(
        connection: UsbDeviceConnection,
        outEndpoint: UsbEndpoint,
        inEndpoint: UsbEndpoint,
        data: ByteArray,
        offset: Int
    ): Boolean {
        val buffer = ByteBuffer.allocate(16 + data.size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(data.size)
        buffer.putInt(offset / ESP.FLASH_SECTOR_SIZE)
        buffer.putInt(offset % ESP.FLASH_SECTOR_SIZE)
        buffer.putInt(0) // padding
        buffer.put(data)
        
        return sendCommand(connection, outEndpoint, inEndpoint, ESP.FLASH_DATA, buffer.array())
    }
    
    private fun sendFlashEnd(
        connection: UsbDeviceConnection,
        outEndpoint: UsbEndpoint,
        inEndpoint: UsbEndpoint
    ): Boolean {
        return sendCommand(connection, outEndpoint, inEndpoint, ESP.FLASH_END, ByteArray(0))
    }
    
    private fun checksum(data: ByteArray): Int {
        var sum = 0xef  // ESP32-S3 checksum magic value
        for (b in data) {
            sum = sum xor (b.toInt() and 0xff)
        }
        return sum
    }
    
    override fun cancelUpdate() {
        updateJob?.cancel()
        updateJob = null
    }
    
    private suspend fun downloadFirmware(): File = withContext(Dispatchers.IO) {
        val url = URL(FirmwareUpdater.FIRMWARE_URL)
        val connection = url.openConnection()
        val inputStream = BufferedInputStream(connection.getInputStream())
        
        val outputFile = File(context.cacheDir, "firmware.bin")
        FileOutputStream(outputFile).use { output ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
        }
        
        return@withContext outputFile
    }
    
    private fun findESP32Device(usbManager: UsbManager): UsbDevice? {
        val deviceList = usbManager.deviceList
        return deviceList.values.find { device ->
            // ESP32-S3 vendor IDs
            val supportedVendorIds = setOf(
                0x303a,  // ESP32-S3
                0x10c4,  // Silicon Labs (CP210x)
                0x1a86   // QinHeng Electronics (CH340)
            )
            device.vendorId in supportedVendorIds
        }
    }
    
    private fun requestUsbPermission(usbManager: UsbManager, device: UsbDevice): Boolean {
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        usbManager.requestPermission(device, permissionIntent)
        
        // Wait for permission (with timeout)
        var hasPermission = false
        var attempts = 0
        while (!hasPermission && attempts < 10) {
            hasPermission = usbManager.hasPermission(device)
            if (!hasPermission) {
                delay(500)
                attempts++
            }
        }
        
        return hasPermission
    }
    
    companion object {
        private const val ACTION_USB_PERMISSION = "bruce.app.USB_PERMISSION"
    }
    
    private fun delay(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            // Ignore
        }
    }
} 