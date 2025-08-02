package bruce.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.util.UUID

actual class BLEConnection {
    private lateinit var  bluetoothAdapter: BluetoothAdapter

    private val _scanResults = mutableStateListOf<BluetoothDevice>()
    private lateinit var gattDevice: BluetoothGatt
    private var valueRead = false
    private var readData: ByteArray = byteArrayOf()

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!_scanResults.contains(result.device)) {
                Log.d("BLE", "New device found ${result.device.name}")
                _scanResults.add(result.device)
            }
        }
    }

    private var connected = false

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Connected to ${gatt.device.name}")
                gattDevice = gatt
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d("BLE", "Services discovered")
            connected = true
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            Log.d("BLE", "VALUE READ: ${value.toString(Charsets.UTF_8)}")
            valueRead = true
            readData = value
            super.onCharacteristicRead(gatt, characteristic, value, status)
        }
    }

    private val permissions =
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }.toTypedArray()


    private fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter?.isEnabled == true
    }

    private fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    @Composable
    private fun EnableRequiredService() {
        if(!isBluetoothEnabled(LocalContext.current)) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(LocalContext.current, enableBtIntent, null)
        }

        if(!isGPSEnabled(LocalContext.current)) {
            val enableGPSIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(LocalContext.current, enableGPSIntent, null)
        }
    }

    @Composable
    private fun requestPermissions(callback: () -> Unit): ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { grantedMap ->
            val allGranted = grantedMap.values.all { it }
            if (allGranted) {
                  callback()
            }
        }

        return launcher
    }

    @Composable
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    actual fun Setup() {
        EnableRequiredService()
        val bluetoothManager = LocalContext.current.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        scanDevices()
    }

    @Composable
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    actual fun Setup(navController: NavController) {
        val showDialog = remember { mutableStateOf(true) }
        val context = LocalContext.current
        EnableRequiredService()

        val launcher = requestPermissions  {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter

            scanDevices()
        }

        if(showDialog.value) {
            AlertDialog(    // This dialog is mandatory since launcher.launch must be called from UI event
                onDismissRequest = { showDialog.value = false },
                title = { Text("Would you start BLE Scan?") },
                text = { Text("Press OK if you wanna activate BLE and start scanning") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            launcher.launch(permissions)
                            navController.navigate(Pages.BLEDevicesList)
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    private lateinit var scanner: BluetoothLeScanner
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    actual fun scanDevices() {
        scanner = bluetoothAdapter.bluetoothLeScanner
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val filters = emptyList<ScanFilter>()
        scanner.startScan(filters, settings, scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    actual fun getScanResult(): List<BLEDevice> {
        return _scanResults.map { BLEDevice(it.name, it.address) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    actual fun StopScan() {
        scanner.stopScan(scanCallback)
    }

    @Composable
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    actual fun ConnectToDevice(address: String) {
        val filteredDevice = _scanResults.filter { it.address == address }
        if(filteredDevice.isEmpty()) {
            Log.d("BLE", "No device found in list")
            return
        }

        filteredDevice[0].connectGatt(LocalContext.current, false, gattCallback)
    }

    actual fun isBLEConnected(): Boolean {
        return connected
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    actual suspend fun queryDevice(service: String, char: String): ByteArray {
        gattDevice.services.forEach {
            val characteristic = it.getCharacteristic(UUID.fromString(char))
            if(characteristic != null)
                Log.d("BLE", "SERVICE: ${it.uuid}, DATA: ${gattDevice.readCharacteristic(characteristic)}")
        }
        while(!valueRead) {
            delay(100)
        }
        return readData
    }
}

actual fun initBLEConnection(): BLEConnection {
    return BLEConnection()
}