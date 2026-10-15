package com.andoni.phrasaldaily.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andoni.phrasaldaily.data.FalseFriend
import com.andoni.phrasaldaily.data.PhrasalVerb
import com.andoni.phrasaldaily.data.StudyItem
import com.andoni.phrasaldaily.domain.Achievements

/** The main study card shown on the home screen and in the library. */
@Composable
fun StudyCard(
    item: StudyItem,
    isFavorite: Boolean,
    isLearned: Boolean,
    onSpeak: (String) -> Unit,
    onSpeakSlow: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleLearned: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPhrasal = item is PhrasalVerb
    val accent = if (isPhrasal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val accentContainer =
        if (isPhrasal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
    val onAccentContainer =
        if (isPhrasal) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        // Header band
        Column(
            Modifier
                .fillMaxWidth()
                .background(accentContainer)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isPhrasal) "PHRASAL VERB" else "FALSE FRIEND",
                    style = MaterialTheme.typography.labelMedium,
                    color = onAccentContainer,
                    letterSpacing = 1.5.sp,
                )
                Spacer(Modifier.weight(1f))
                if (item is PhrasalVerb) {
                    Surface(shape = RoundedCornerShape(50), color = accent) {
                        Text(
                            item.level,
                            Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.headword,
                    style = MaterialTheme.typography.displaySmall,
                    color = onAccentContainer,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalIconButton(
                    onClick = { onSpeak(item.headword) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = accent),
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Escuchar", tint = Color.White)
                }
                IconButton(onClick = { onSpeakSlow(item.headword) }) {
                    Icon(Icons.Outlined.Speed, "Escuchar despacio", tint = onAccentContainer)
                }
            }
            Text(item.ipa, style = MaterialTheme.typography.titleMedium, color = onAccentContainer)
            Text(
                "Se pronuncia: «${item.sayAs}»",
                style = MaterialTheme.typography.bodyMedium,
                color = onAccentContainer.copy(alpha = 0.8f),
            )
        }

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (item) {
                is PhrasalVerb -> {
                    LabeledText("Traducción", item.translation, big = true)
                    LabeledText("Significado", item.definition)
                }
                is FalseFriend -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Parece…", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                item.looksLike,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("…pero significa", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(item.translation, style = MaterialTheme.typography.titleLarge, color = accent)
                        }
                    }
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(12.dp),
                    ) {
                        Icon(Icons.Outlined.Lightbulb, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.width(8.dp))
                        Text(item.tip, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            HorizontalDivider()

            Text("Ejemplo", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "“${item.example}”",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.exampleEs,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onSpeak(item.example) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Escuchar ejemplo", tint = accent)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = onToggleLearned,
                    label = { Text(if (isLearned) "Aprendido" else "Marcar aprendido") },
                    leadingIcon = {
                        Icon(
                            if (isLearned) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            null,
                            Modifier.size(AssistChipDefaults.IconSize),
                        )
                    },
                    colors = if (isLearned) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        leadingIconContentColor = MaterialTheme.colorScheme.tertiary,
                    ) else AssistChipDefaults.assistChipColors(),
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                        tint = if (isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, "Compartir", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun LabeledText(label: String, text: String, big: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = if (big) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

private val weekLetters = listOf("L", "M", "X", "J", "V", "S", "D")

/** Seven circles, Monday to Sunday, filled for visited days. */
@Composable
fun WeekStrip(days: List<Boolean>, todayIndex: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEachIndexed { i, visited ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                visited -> MaterialTheme.colorScheme.secondary
                                i == todayIndex -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (visited) "🔥" else weekLetters[i],
                        color = if (visited) MaterialTheme.colorScheme.onSecondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = if (visited) 16.sp else 13.sp,
                        fontWeight = if (i == todayIndex) FontWeight.Bold else FontWeight.Normal,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(weekLetters[i], style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementUnlockedDialog(ids: List<String>, onDismiss: () -> Unit) {
    val items = ids.mapNotNull(Achievements::byId)
    if (items.isEmpty()) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("¡Genial!") } },
        title = {
            Text(
                if (items.size == 1) "¡Nuevo logro desbloqueado!" else "¡${items.size} logros desbloqueados!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items.forEach { a ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(120.dp),
                    ) {
                        Text(a.emoji, fontSize = 44.sp)
                        Text(a.title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                        Text(
                            a.description,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    )
}

fun shareItem(context: Context, item: StudyItem) {
    val text = buildString {
        when (item) {
            is PhrasalVerb -> append("📘 Phrasal verb del día: ${item.headword} ${item.ipa}\n= ${item.translation}\n")
            is FalseFriend -> append("⚠️ False friend: ${item.headword} no es «${item.looksLike}», significa «${item.translation}»\n")
        }
        append("\n“${item.example}”\n${item.exampleEs}\n\nAprendido con Phrasal Daily")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir"))
}
