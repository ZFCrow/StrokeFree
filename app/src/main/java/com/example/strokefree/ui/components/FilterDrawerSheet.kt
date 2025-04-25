package com.example.strokefree.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun FilterDrawer(
    selectedFilters: Set<String>,
    onDismiss: () -> Unit,
    onApplyFilters: (Set<String>) -> Unit
) {
    val contentTypeFilters = listOf("Video", "Article", "Journal") // Matches "Type" in dataset
    val learningCategoryFilters = listOf(
        "Stroke Awareness", "Rehabilitation", "Emergency Response", "Medical Knowledge"
    ) // Matches "Category" in dataset


    val tempFilters = remember { mutableStateListOf<String>() }


    // Sync state when drawer opens
    LaunchedEffect(selectedFilters) {
        tempFilters.clear()
        tempFilters.addAll(selectedFilters)
    }

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Filter", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Content Type Section
            Text("Content Type", style = MaterialTheme.typography.titleMedium)
            contentTypeFilters.forEach { filter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tempFilters.contains(filter),
                        onCheckedChange = { isChecked ->
                            if (isChecked) tempFilters.add(filter)
                            else tempFilters.remove(filter)
                        }
                    )
                    Text(filter, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Learning Category Section
            Text("Learning Category", style = MaterialTheme.typography.titleMedium)
            learningCategoryFilters.forEach { filter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tempFilters.contains(filter),
                        onCheckedChange = { isChecked ->
                            if (isChecked) tempFilters.add(filter)
                            else tempFilters.remove(filter)
                        }
                    )
                    Text(filter, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ✅ Clears both tempFilters and selectedFilters properly
                Button(
                    onClick = {
                        tempFilters.clear() // Reset state correctly
                        onApplyFilters(emptySet()) // Ensures UI updates properly
                    }
                ) { Text("Clear") }

                // ✅ Applies filters and updates the state
                Button(
                    onClick = {
                        onApplyFilters(tempFilters.toSet())
                        onDismiss()
                    }
                ) { Text("Apply Filter") }
            }
        }
    }
}
