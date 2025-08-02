package bruce.app

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(val version: String, val commit: String,
                      val sdk: String, val mac: String,
                      val device: String, val wifi_ip: String)
