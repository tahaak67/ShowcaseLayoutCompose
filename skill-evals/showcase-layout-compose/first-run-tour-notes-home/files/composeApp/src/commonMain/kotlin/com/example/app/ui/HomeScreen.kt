package com.example.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.app.data.SettingsStore

data class Note(val id: Int, val title: String, val preview: String)

private val sampleNotes = List(30) { Note(it, "Note #${it + 1}", "Something worth remembering, item ${it + 1}") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settings: SettingsStore,
    onOpenNote: (Note) -> Unit,
    onCreateNote: () -> Unit,
    onSearch: () -> Unit,
) {
    var filter by remember { mutableStateOf(NoteFilter.All) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                actions = {
                    IconButton(onClick = onSearch) { Icon(Icons.Default.Search, contentDescription = "Search") }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = "More") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) { Icon(Icons.Default.Add, contentDescription = "New note") }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            FilterChipsRow(selected = filter, onSelect = { filter = it })
            LazyColumn(Modifier.fillMaxSize()) {
                items(sampleNotes, key = { it.id }) { note ->
                    ListItem(
                        headlineContent = { Text(note.title) },
                        supportingContent = { Text(note.preview) },
                        modifier = Modifier.clickable { onOpenNote(note) },
                    )
                }
            }
        }
    }
}
