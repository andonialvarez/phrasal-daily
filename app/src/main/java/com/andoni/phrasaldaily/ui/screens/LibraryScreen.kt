package com.andoni.phrasaldaily.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.andoni.phrasaldaily.data.FalseFriend
import com.andoni.phrasaldaily.data.PhrasalVerb
import com.andoni.phrasaldaily.data.StudyItem
import com.andoni.phrasaldaily.data.UserData
import com.andoni.phrasaldaily.ui.AppViewModel

private enum class LibraryFilter(val label: String) {
    ALL("Todo"), PHRASAL("Phrasal verbs"), FALSE_FRIENDS("False friends"), FAVORITES("Favoritos"), PENDING("Por aprender")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: AppViewModel, user: UserData) {
    val context = LocalContext.current
    var filter by rememberSaveable { mutableStateOf(LibraryFilter.ALL) }
    var query by rememberSaveable { mutableStateOf("") }
    var openId by remember { mutableStateOf<String?>(null) }

    val seenItems = remember(user.seen) {
        user.seen.mapNotNull(vm.content::byId)
            .sortedWith(compareBy<StudyItem>({ it is FalseFriend }, { it.headword }))
    }
    val shown = seenItems.filter { item ->
        when (filter) {
            LibraryFilter.ALL -> true
            LibraryFilter.PHRASAL -> item is PhrasalVerb
            LibraryFilter.FALSE_FRIENDS -> item is FalseFriend
            LibraryFilter.FAVORITES -> item.id in user.favorites
            LibraryFilter.PENDING -> item.id !in user.learned
        }
    }.filter {
        query.isBlank() || it.headword.contains(query.trim(), ignoreCase = true) ||
            it.translation.contains(query.trim(), ignoreCase = true)
    }
    val totalContent = vm.content.phrasalVerbs.size + vm.content.falseFriends.size

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Biblioteca", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Has descubierto ${seenItems.size} de $totalContent palabras · ${user.learned.size} aprendidas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar en inglés o español") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                LibraryFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }
        }

        if (shown.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("📭", style = MaterialTheme.typography.displaySmall)
                Text(
                    if (seenItems.isEmpty()) "Tu biblioteca se irá llenando día a día."
                    else "Nada por aquí con ese filtro.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                items(shown, key = { it.id }) { item ->
                    LibraryRow(
                        item = item,
                        favorite = item.id in user.favorites,
                        learned = item.id in user.learned,
                        onClick = { openId = item.id },
                    )
                }
            }
        }
    }

    val open = openId?.let(vm.content::byId)
    if (open != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { openId = null }, sheetState = sheetState) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                StudyCard(
                    item = open,
                    isFavorite = open.id in user.favorites,
                    isLearned = open.id in user.learned,
                    onSpeak = vm::speak,
                    onSpeakSlow = { vm.speaker.speak(it, 0.45f) },
                    onToggleFavorite = { vm.setFavorite(open.id, open.id !in user.favorites) },
                    onToggleLearned = { vm.setLearned(open.id, open.id !in user.learned) },
                    onShare = { shareItem(context, open) },
                )
            }
        }
    }
}

@Composable
private fun LibraryRow(item: StudyItem, favorite: Boolean, learned: Boolean, onClick: () -> Unit) {
    val accent = if (item is PhrasalVerb) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(8.dp), color = accent) {
                Text(
                    if (item is PhrasalVerb) "PV" else "FF",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.headword, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            if (favorite) Icon(Icons.Filled.Favorite, "Favorito", tint = Color(0xFFE11D48))
            if (learned) {
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.CheckCircle, "Aprendido", tint = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}
