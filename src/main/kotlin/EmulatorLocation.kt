import org.apache.commons.net.telnet.TelnetClient
import java.io.Closeable
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.Executors

class EmulatorLocation(telnetClient: TelnetClient) : Closeable {
    private val writer = OutputStreamWriter(telnetClient.outputStream)
    private val reader = InputStreamReader(telnetClient.inputStream)
    private val writeExecutor = Executors.newSingleThreadScheduledExecutor()
    private val readExecutor = Executors.newSingleThreadExecutor()

    init {
        assert(telnetClient.isConnected) { "Telnet client must be already connected!" }
        val authToken = File(System.getProperty("user.home"), ".emulator_console_auth_token")
                .inputStream().reader().use { it.readText() }

        readExecutor.execute {
            reader.forEachLine(System.out::println)
            System.err.println("Closed reader.")
        }

        System.err.println("Auth token: $authToken")
        sendLine("auth $authToken")
    }

    fun setLocation(longitude: Double, latitude: Double) {
        sendLine("geo fix $longitude $latitude".replace('.', ','))
    }

    private fun sendLine(line: String) {
        System.err.println("> $line")
        writeExecutor.execute {
            writer.appendln(line)
            writer.flush()
        }
    }

    override fun close() {
        writer.close()
        reader.close()
    }
}