package bruce.app

import com.fazecast.jSerialComm.SerialPort
import java.io.OutputStream

class DesktopSerialCommunication : SerialCommunication {
    private var serialPort: SerialPort? = null
    private var outputStream: OutputStream? = null
    private var outputListener: ((String) -> Unit)? = null

    override fun setOutputListener(listener: (String) -> Unit) {
        outputListener = listener
    }

    private fun notifyOutput(message: String) {
        outputListener?.invoke(message)
    }

    override fun connect() {
        notifyOutput("[Desktop] Attempting to connect...")
        if (serialPort?.isOpen == true) {
            return  // Already connected
        }

        val ports = SerialPort.getCommPorts()
        notifyOutput("Available ports:")
        ports.forEach { port ->
            notifyOutput("Port: ${port.descriptivePortName} (${port.systemPortName})")
        }

        for (port in ports) {
            if (isESP32Port(port)) {
                try {
                    port.setComPortParameters(9600, 8, 1, 0)
                    port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0)
                    
                    if (port.openPort()) {
                        serialPort = port
                        outputStream = port.outputStream
                        notifyOutput("Connected to: ${port.descriptivePortName}")
                        return
                    } else {
                        notifyOutput("Failed to open port: ${port.descriptivePortName}")
                    }
                } catch (e: Exception) {
                    notifyOutput("Error connecting to port ${port.descriptivePortName}: ${e.message}")
                }
            }
        }
        notifyOutput("Could not find or connect to ESP32 port.")
    }

    private fun isESP32Port(port: SerialPort): Boolean {
        val name = port.descriptivePortName.lowercase()
        return name.contains("usb") || 
               name.contains("serial") || 
               name.contains("uart") || 
               name.contains("jtag") ||
               name.contains("ttyusb") ||
               name.contains("ttyacm")
    }

    override fun sendCommand(command: String) {
        notifyOutput("> $command")
        try {
            if (serialPort?.isOpen != true) {
                notifyOutput("Port not open, attempting to connect...")
                connect()
            }

            if (serialPort?.isOpen == true && outputStream != null) {
                val commandWithNewline = "$command\n"
                outputStream?.write(commandWithNewline.toByteArray())
                outputStream?.flush()
                notifyOutput("Command sent successfully")
                
                // Read response
                val buffer = ByteArray(1024)
                val bytesRead = serialPort?.inputStream?.read(buffer)
                if (bytesRead != null && bytesRead > 0) {
                    val response = String(buffer, 0, bytesRead)
                    notifyOutput("< $response")
                }
            } else {
                notifyOutput("Failed to send command: Port not open")
            }
        } catch (e: Exception) {
            notifyOutput("Error sending command: ${e.message}")
            e.printStackTrace()
            // Try to reconnect on next attempt
            disconnect()
        }
    }

    override fun disconnect() {
        try {
            outputStream?.close()
            if (serialPort?.isOpen == true) {
                serialPort?.closePort()
                notifyOutput("Port closed.")
            }
        } catch (e: Exception) {
            notifyOutput("Error disconnecting: ${e.message}")
            e.printStackTrace()
        } finally {
            serialPort = null
            outputStream = null
        }
    }

    override fun setBaudRate(baudRate: Int) {
        notifyOutput("Setting baud rate to: $baudRate")
        if (serialPort?.isOpen == true) {
            serialPort?.baudRate = baudRate
            notifyOutput("Baud rate set successfully")
        } else {
            notifyOutput("Cannot set baud rate: Port not open")
        }
    }
}

actual fun getSerialCommunication(): SerialCommunication {
    return DesktopSerialCommunication()
} 