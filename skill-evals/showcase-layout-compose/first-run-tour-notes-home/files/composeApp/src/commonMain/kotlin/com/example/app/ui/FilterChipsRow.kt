package com.example.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class NoteFilter(val label: String) { All("All"), Pinned("Pinned"), Shared("Shared") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    selected: NoteFilter,
    onSelect: (NoteFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NoteFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}
