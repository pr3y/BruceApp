package bruce.app

interface FirmwareUpdater {
    fun downloadAndUpdate(
        onProgress: (String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    )
    
    fun cancelUpdate()
    
    companion object {
        const val FIRMWARE_URL = "http://bruce.computer/LastRelease/Bruce-m5stack-cardputer.bin"
    }
} 