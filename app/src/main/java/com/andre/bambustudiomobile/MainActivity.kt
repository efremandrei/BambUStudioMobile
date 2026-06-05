package com.andre.bambustudiomobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BambuStudioMobileApp()
        }
    }
}

private enum class StudioTab(val label: String) {
    Monitor("Monitor"),
    Control("Control"),
    Queue("Queue"),
    Slicer("Slicer"),
    Settings("Settings")
}

private data class Printer(
    val name: String,
    val model: String,
    val state: String,
    val progress: Float,
    val job: String,
    val nozzle: Int,
    val bed: Int,
    val chamber: Int,
    val eta: String,
    val ams: List<Spool>
)

private data class Spool(
    val slot: String,
    val material: String,
    val color: Color,
    val remaining: Int
)

private data class PrintJob(
    val file: String,
    val printer: String,
    val status: String,
    val estimate: String
)

private interface PrinterCommandGateway {
    fun send(action: String)
}

private object MockCommandGateway : PrinterCommandGateway {
    override fun send(action: String) = Unit
}

@Composable
private fun BambuStudioMobileApp() {
    val printers = remember { samplePrinters() }
    val jobs = remember { sampleQueue() }
    var selectedPrinter by remember { mutableStateOf(printers.first()) }
    var selectedTab by remember { mutableStateOf(StudioTab.Monitor) }
    var lastAction by remember { mutableStateOf("Backend not connected") }
    val gateway = remember { MockCommandGateway }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFF167D6B),
            secondary = Color(0xFF345D7E),
            tertiary = Color(0xFF7A5C2E),
            surface = Color(0xFFF7F9F8),
            background = Color(0xFFEFF4F1),
            outline = Color(0xFFB8C6C0)
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                TopBar(lastAction)
                PrinterStrip(
                    printers = printers,
                    selected = selectedPrinter,
                    onSelect = { selectedPrinter = it }
                )
                TabBar(selectedTab, onSelect = { selectedTab = it })
                HorizontalDivider(color = Color(0xFFD7E0DC))
                Box(Modifier.weight(1f)) {
                    when (selectedTab) {
                        StudioTab.Monitor -> MonitorScreen(selectedPrinter)
                        StudioTab.Control -> ControlScreen(
                            printer = selectedPrinter,
                            onCommand = {
                                gateway.send(it)
                                lastAction = it
                            }
                        )
                        StudioTab.Queue -> QueueScreen(jobs)
                        StudioTab.Slicer -> SlicerScreen()
                        StudioTab.Settings -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(lastAction: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10231F))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF87D5C4))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Bambu Studio Mobile",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                lastAction,
                color = Color(0xFFB6C9C3),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
        }
    }
}

@Composable
private fun PrinterStrip(
    printers: List<Printer>,
    selected: Printer,
    onSelect: (Printer) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8EFEC))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        printers.forEach { printer ->
            val isSelected = printer == selected
            Surface(
                modifier = Modifier.weight(1f),
                onClick = { onSelect(printer) },
                color = if (isSelected) Color.White else Color(0xFFF1F5F3),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = if (isSelected) 2.dp else 0.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF167D6B) else Color(0xFFC9D5D0)
                )
            ) {
                Column(Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusDot(printer.state)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            printer.name,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(printer.model, color = Color(0xFF60716B), fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { printer.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF167D6B),
                        trackColor = Color(0xFFD9E3DF)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusDot(state: String) {
    val color = when (state) {
        "Printing" -> Color(0xFF167D6B)
        "Paused" -> Color(0xFFB77715)
        "Error" -> Color(0xFFB3261E)
        else -> Color(0xFF6F7975)
    }
    Canvas(Modifier.size(9.dp)) {
        drawCircle(color = color, radius = size.minDimension / 2)
    }
}

@Composable
private fun TabBar(selected: StudioTab, onSelect: (StudioTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F9F8))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StudioTab.entries.forEach { tab ->
            val active = tab == selected
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = { onSelect(tab) },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (active) Color(0xFF167D6B) else Color.Transparent,
                    contentColor = if (active) Color.White else Color(0xFF263531)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(tab.label, maxLines = 1, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MonitorScreen(printer: Printer) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CameraPanel(printer)
        }
        item {
            StatsPanel(printer)
        }
        item {
            AmsPanel(printer.ams)
        }
        item {
            EventPanel()
        }
    }
}

@Composable
private fun CameraPanel(printer: Printer) {
    Panel("Live View", Icons.Default.Videocam) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(Color(0xFF18211F), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF30423D), RoundedCornerShape(6.dp))
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val step = size.width / 7f
                for (i in 1..6) {
                    drawLine(
                        color = Color(0xFF253631),
                        start = Offset(i * step, 0f),
                        end = Offset(i * step, size.height),
                        strokeWidth = 1f
                    )
                }
                drawCircle(Color(0xFF167D6B), radius = 36f, center = Offset(size.width * .72f, size.height * .38f))
                drawLine(
                    color = Color(0xFF87D5C4),
                    start = Offset(size.width * .28f, size.height * .68f),
                    end = Offset(size.width * .74f, size.height * .68f),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
            }
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(printer.job, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("${printer.state} - ${printer.eta}", color = Color(0xFFC9D5D0), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatsPanel(printer: Printer) {
    Panel("Printer State", Icons.Default.Thermostat) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Metric("Progress", "${(printer.progress * 100).toInt()}%", Modifier.weight(1f))
            Metric("Nozzle", "${printer.nozzle} C", Modifier.weight(1f))
            Metric("Bed", "${printer.bed} C", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Metric("Chamber", "${printer.chamber} C", Modifier.weight(1f))
            Metric("Speed", "Standard", Modifier.weight(1f))
            Metric("Flow", "0.98", Modifier.weight(1f))
        }
    }
}

@Composable
private fun AmsPanel(spools: List<Spool>) {
    Panel("AMS", Icons.Default.Tune) {
        spools.forEach { spool ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(Modifier.size(18.dp)) {
                    drawCircle(spool.color)
                    drawCircle(Color.Black.copy(alpha = .18f), radius = size.minDimension / 2, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                }
                Spacer(Modifier.width(10.dp))
                Text(spool.slot, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(38.dp))
                Text("${spool.material} - ${spool.remaining}% remaining", modifier = Modifier.weight(1f))
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp)) {
                    Text("Map")
                }
            }
        }
    }
}

@Composable
private fun EventPanel() {
    Panel("HMS / Events", Icons.Default.ErrorOutline) {
        EventRow("No active HMS errors", "Printer telemetry is nominal")
        EventRow("First layer inspection", "Passed at layer 2")
        EventRow("Filament mapping", "AMS slot A3 mapped to model color 2")
    }
}

@Composable
private fun ControlScreen(printer: Printer, onCommand: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Panel("Print Job", Icons.Default.Print) {
                ActionGrid(
                    listOf(
                        ControlAction("Pause", Icons.Default.Pause, "pause_print"),
                        ControlAction("Resume", Icons.Default.PlayArrow, "resume_print"),
                        ControlAction("Stop", Icons.Default.Stop, "stop_print"),
                        ControlAction("Skip Object", Icons.Default.Tune, "skip_object"),
                    ),
                    onCommand
                )
            }
        }
        item {
            Panel("Thermal / Motion", Icons.Default.Thermostat) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Stepper("Nozzle", printer.nozzle, "C", Modifier.weight(1f))
                    Stepper("Bed", printer.bed, "C", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Stepper("Chamber", printer.chamber, "C", Modifier.weight(1f))
                    Stepper("Z Offset", 0, "mm", Modifier.weight(1f))
                }
            }
        }
        item {
            Panel("Machine Controls", Icons.Default.Speed) {
                ActionGrid(
                    listOf(
                        ControlAction("Silent", Icons.Default.Speed, "set_speed_silent"),
                        ControlAction("Standard", Icons.Default.Speed, "set_speed_standard"),
                        ControlAction("Sport", Icons.Default.Speed, "set_speed_sport"),
                        ControlAction("Ludicrous", Icons.Default.Speed, "set_speed_ludicrous"),
                        ControlAction("Light", Icons.Default.Lightbulb, "toggle_chamber_light"),
                        ControlAction("Airduct", Icons.Default.Tune, "toggle_airduct"),
                        ControlAction("Fans", Icons.Default.Tune, "set_fans"),
                        ControlAction("Power", Icons.Default.PowerSettingsNew, "power_control"),
                    ),
                    onCommand
                )
            }
        }
    }
}

private data class ControlAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val command: String
)

@Composable
private fun ActionGrid(actions: List<ControlAction>, onCommand: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { action ->
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        onClick = { onCommand(action.command) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF167D6B))
                    ) {
                        Icon(action.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(action.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, unit: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF1F5F3),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7E0DC))
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, color = Color(0xFF60716B), fontSize = 12.sp)
                Text("$value $unit", fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp)) { Text("-") }
            Spacer(Modifier.width(4.dp))
            OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp)) { Text("+") }
        }
    }
}

@Composable
private fun QueueScreen(jobs: List<PrintJob>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Panel("Upload / Print", Icons.Default.CloudUpload) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {},
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Upload sliced file")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {},
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("File manager")
                    }
                }
            }
        }
        items(jobs) { job ->
            Panel(job.file, Icons.Default.Print) {
                Text("${job.printer} - ${job.status}", fontWeight = FontWeight.SemiBold)
                Text("Estimate: ${job.estimate}", color = Color(0xFF60716B))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp)) { Text("Move") }
                    OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp)) { Text("Inspect") }
                    Button(onClick = {}, shape = RoundedCornerShape(6.dp)) { Text("Print") }
                }
            }
        }
    }
}

@Composable
private fun SlicerScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Panel("Slicer Workspace", Icons.Default.Tune) {
                Text("Server-side slicing target: OrcaSlicer/Bambu profiles", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Metric("Printer", "P2S 0.4", Modifier.weight(1f))
                    Metric("Plate", "Textured PEI", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Metric("Quality", "0.20 mm", Modifier.weight(1f))
                    Metric("Support", "Tree Auto", Modifier.weight(1f))
                }
            }
        }
        item {
            Panel("Profiles", Icons.Default.Settings) {
                EventRow("Printer profiles", "Synced from backend profile store")
                EventRow("Filament profiles", "Bambu PLA Basic, PETG HF, ABS GF")
                EventRow("Process profiles", "Draft, Standard, Strength, Fine")
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Panel("Backend Connection", Icons.Default.Settings) {
                EventRow("Mode", "Bambuddy API or custom LAN backend")
                EventRow("Endpoint", "http://bambu-console.local:8080")
                EventRow("Security", "Local tokens, VPN/Tailscale for remote access")
            }
        }
        item {
            Panel("Capability Map", Icons.Default.Tune) {
                EventRow("Printer protocol", "MQTT over TLS, FTPS, camera proxy")
                EventRow("Slicing", "Backend-hosted Orca/Bambu-compatible engine")
                EventRow("Limit", "Cloud/account-only Bambu services require official authorization")
            }
        }
    }
}

@Composable
private fun Panel(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7E0DC)),
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF167D6B), modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFF1F5F3), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFD7E0DC), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(label, color = Color(0xFF60716B), fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EventRow(title: String, body: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(body, color = Color(0xFF60716B), fontSize = 13.sp)
    }
}

private fun samplePrinters(): List<Printer> = listOf(
    Printer(
        name = "Workshop P2S",
        model = "Bambu Lab P2S AMS",
        state = "Printing",
        progress = .62f,
        job = "drawer-organizer-v12.gcode.3mf",
        nozzle = 218,
        bed = 55,
        chamber = 38,
        eta = "1h 14m left",
        ams = listOf(
            Spool("A1", "PLA Basic", Color(0xFF1F2937), 78),
            Spool("A2", "PLA Matte", Color(0xFFE9ECE6), 52),
            Spool("A3", "PETG HF", Color(0xFF0EA5A0), 63),
            Spool("A4", "Support W", Color(0xFFF7C948), 41)
        )
    ),
    Printer(
        name = "Desk A1",
        model = "Bambu Lab A1 mini",
        state = "Idle",
        progress = 0f,
        job = "Ready",
        nozzle = 29,
        bed = 26,
        chamber = 24,
        eta = "Idle",
        ams = listOf(
            Spool("A1", "PLA Silk", Color(0xFFB85C38), 90),
            Spool("A2", "PLA Basic", Color(0xFF2563EB), 37),
            Spool("A3", "PETG", Color(0xFF16A34A), 66),
            Spool("A4", "Empty", Color(0xFFCBD5E1), 0)
        )
    )
)

private fun sampleQueue(): List<PrintJob> = listOf(
    PrintJob("drawer-organizer-v12.gcode.3mf", "Workshop P2S", "Printing", "2h 48m"),
    PrintJob("spool-dry-box-clip.gcode.3mf", "Any 0.4 nozzle", "Queued", "42m"),
    PrintJob("phone-stand-p2s-pla.gcode.3mf", "Workshop P2S", "Ready", "1h 05m")
)
