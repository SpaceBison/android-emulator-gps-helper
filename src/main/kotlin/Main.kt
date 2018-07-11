import org.apache.commons.net.telnet.TelnetClient
import org.jxmapviewer.JXMapViewer
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.input.CenterMapListener
import org.jxmapviewer.input.PanKeyListener
import org.jxmapviewer.input.PanMouseInputListener
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor
import org.jxmapviewer.viewer.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JFrame

fun main(args: Array<String>) {
    val mapViewer = JXMapViewer()

    // Create a TileFactoryInfo for OpenStreetMap
    val info = OSMTileFactoryInfo()
    val tileFactory = DefaultTileFactory(info)
    mapViewer.tileFactory = tileFactory

    // Use 8 threads in parallel to load the tiles
    tileFactory.setThreadPoolSize(8)

    // Set the focus
    val frankfurt = GeoPosition(50.11, 8.68)

    mapViewer.apply {
        zoom = 7
        addressLocation = frankfurt
    }

    // Add interactions

    val emulatorLocation =
            EmulatorLocation(TelnetClient().apply { connect("localhost", 5554) })

    mapViewer.apply {
        val mia = PanMouseInputListener(mapViewer)

        addMouseListener(mia)
        addMouseMotionListener(mia)
        addMouseListener(CenterMapListener(mapViewer))
        addMouseWheelListener(ZoomMouseWheelListenerCursor(mapViewer))
        addKeyListener(PanKeyListener(mapViewer))

        val waypointPainter = WaypointPainter<Waypoint>()
        addMouseListener(WaypointClickListener(this, waypointPainter, emulatorLocation))
        setOverlayPainter(waypointPainter)
    }
    
    // Display the viewer in a JFrame
    val frame = JFrame("Android Emulator Location Helper")
    frame.contentPane.add(mapViewer)
    frame.setSize(800, 600)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}

class WaypointClickListener(
        private val mapViewer: JXMapViewer,
        private val waypointPainter: WaypointPainter<Waypoint>,
        private val emulatorLocation: EmulatorLocation) : MouseListener {
    override fun mouseReleased(p0: MouseEvent?) = Unit
    override fun mouseEntered(p0: MouseEvent?) = Unit

    override fun mouseClicked(event: MouseEvent?) {
        if (event?.button == MouseEvent.BUTTON3) {
            val geoPosition = mapViewer.convertPointToGeoPosition(event.point)
            waypointPainter.waypoints = setOf(DefaultWaypoint(geoPosition))
            emulatorLocation.setLocation(geoPosition.longitude, geoPosition.latitude)
            mapViewer.repaint()
        }
    }

    override fun mouseExited(p0: MouseEvent?) = Unit
    override fun mousePressed(p0: MouseEvent?) = Unit
}