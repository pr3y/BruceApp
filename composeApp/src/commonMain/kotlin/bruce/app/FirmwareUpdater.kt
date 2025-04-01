package bruce.app

interface FirmwareUpdater {
    fun downloadAndUpdate(
        onProgress: (String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    )
    
    fun cancelUpdate()
    
    companion object {
        const val FIRMWARE_URL = "https://bruce.computer/LastRelease/Bruce-m5stack-cardputer.bin"
    }
} 