package bruce.app

actual fun getFirmwareUpdater(): FirmwareUpdater {
    return DesktopFirmwareUpdater()
} 