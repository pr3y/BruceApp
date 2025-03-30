package bruce.app

actual fun getFirmwareUpdater(): FirmwareUpdater {
    return AndroidFirmwareUpdater(MainActivity.instance)
} 