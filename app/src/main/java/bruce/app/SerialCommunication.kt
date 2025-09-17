package bruce.app

interface SerialCommunication {
    fun connect()
    fun disconnect()
    fun sendCommand(command: String)
    fun setBaudRate(baudRate: Int)
    fun setOutputListener(listener: (String) -> Unit)
}
