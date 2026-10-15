package com.andoni.phrasaldaily.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.andoni.phrasaldaily.BuildConfig
import com.andoni.phrasaldaily.data.ContentMode
import com.andoni.phrasaldaily.data.ThemeMode
import com.andoni.phrasaldaily.data.UserData
import com.andoni.phrasaldaily.notify.Notifications
import com.andoni.phrasaldaily.ui.AppViewModel

val contentModeLabels = mapOf(
    ContentMode.PHRASAL to ("Solo phrasal verb" to "Un phrasal verb al día. Podrás cambiarlo por un false friend cuando quieras."),
    ContentMode.FALSE_FRIEND to ("Solo false friend" to "Un false friend al día. Podrás cambiarlo por un phrasal verb cuando quieras."),
    ContentMode.BOTH to ("Phrasal verb + false friend" to "Las dos cosas cada día, para ir a tope."),
)

fun formatTime(hour: Int, minute: Int) = "%02d:%02d".format(hour, minute)

@Composable
fun SettingsScreen(vm: AppViewModel, user: UserData) {
    val context = LocalContext.current
    var editingName by remember { mutableStateOf(false) }
    var pickingTime by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var rate by remember(user.speechRate) { mutableFloatStateOf(user.speechRate) }
    var canNotify by remember { mutableStateOf(Notifications.canPost(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        canNotify = it
        if (it) vm.setNotificationsEnabled(true)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)

        SettingsCard("Perfil") {
            Row(
                Modifier.fillMaxWidth().clickable { editingName = true }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Tu nombre", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user.name.ifBlank { "Sin nombre" }, style = MaterialTheme.typography.titleMedium)
                }
                Icon(Icons.Outlined.Edit, "Editar nombre")
            }
        }

        SettingsCard("Contenido diario") {
            ContentModePicker(user.contentMode) { vm.setContentMode(it) }
        }

        SettingsCard("Notificaciones") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Recordatorio diario", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Una frase motivadora distinta cada día. Si ya has estudiado, no te molesto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = user.notificationsEnabled && canNotify,
                    onCheckedChange = { on ->
                        if (on && !canNotify && Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            vm.setNotificationsEnabled(on)
                        }
                    },
                )
            }
            if (user.notificationsEnabled) {
                Row(
                    Modifier.fillMaxWidth().clickable { pickingTime = true }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Hora del recordatorio", modifier = Modifier.weight(1f))
                    Text(
                        formatTime(user.reminderHour, user.reminderMinute),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedButton(onClick = { vm.sendTestNotification() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Enviar una notificación de prueba")
                }
            }
            if (!canNotify && Build.VERSION.SDK_INT >= 26) {
                TextButton(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                    )
                }) { Text("Abrir ajustes de notificaciones de Android") }
            }
        }

        SettingsCard("Pronunciación") {
            Text("Velocidad de la voz: ${"%.1f".format(rate)}x", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = rate,
                onValueChange = { rate = it },
                onValueChangeFinished = { vm.setSpeechRate(rate) },
                valueRange = 0.5f..1.3f,
                steps = 7,
            )
            OutlinedButton(onClick = { vm.speaker.speak("Don't give up, you're almost there!", rate) },
                modifier = Modifier.fillMaxWidth()) {
                Text("Probar voz")
            }
            if (!vm.speaker.available) {
                Text(
                    "Tu móvil no tiene voz en inglés instalada. Instálala en Ajustes > Sistema > Idioma > Texto a voz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        SettingsCard("Apariencia") {
            ThemePicker(user.theme) { vm.setTheme(it) }
        }

        SettingsCard("Datos") {
            OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Reiniciar progreso", color = MaterialTheme.colorScheme.error)
            }
        }

        Text(
            "Phrasal Daily ${BuildConfig.VERSION_NAME} · ${vm.content.phrasalVerbs.size} phrasal verbs y " +
                "${vm.content.falseFriends.size} false friends",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
    }

    if (editingName) {
        var draft by remember { mutableStateOf(user.name) }
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("¿Cómo te llamas?") },
            text = {
                OutlinedTextField(value = draft, onValueChange = { draft = it.take(30) }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = { vm.setName(draft); editingName = false }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editingName = false }) { Text("Cancelar") } },
        )
    }

    if (pickingTime) {
        TimePickerDialog(
            hour = user.reminderHour,
            minute = user.reminderMinute,
            onDismiss = { pickingTime = false },
            onConfirm = { h, m ->
                vm.setReminderTime(h, m)
                pickingTime = false
            },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("¿Reiniciar progreso?") },
            text = { Text("Se borrarán tu racha, logros, favoritos y palabras aprendidas. Tu nombre y ajustes se mantienen.") },
            confirmButton = {
                TextButton(onClick = { vm.resetProgress(); confirmReset = false }) {
                    Text("Reiniciar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun ContentModePicker(selected: ContentMode, onSelect: (ContentMode) -> Unit) {
    Column {
        ContentMode.entries.forEach { mode ->
            val (title, desc) = contentModeLabels.getValue(mode)
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = mode == selected, onClick = { onSelect(mode) }, role = Role.RadioButton)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = mode == selected, onClick = null)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(desc, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val labels = mapOf(ThemeMode.SYSTEM to "Sistema", ThemeMode.LIGHT to "Claro", ThemeMode.DARK to "Oscuro")
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        ThemeMode.entries.forEachIndexed { i, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = ThemeMode.entries.size),
            ) { Text(labels.getValue(mode)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(hour: Int, minute: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hora del recordatorio") },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("Aceptar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
