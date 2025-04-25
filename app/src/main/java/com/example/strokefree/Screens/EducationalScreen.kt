package com.example.strokefree.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.EducationalViewModel
import com.example.strokefree.ui.components.FilterDrawer
import kotlinx.coroutines.launch

@Composable
fun EducationalScreen(navController: NavController, viewModel: EducationalViewModel) {
    val contentList by viewModel.contentList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilters by viewModel.selectedFilters.collectAsState()
    val scrollState = viewModel.scrollState
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val typeColors = mapOf(
        "Video" to Color(0xFFFF5252),
        "Article" to Color(0xFF4CAF50),
        "Journal" to Color(0xFF42A5F5)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        scrimColor = Color.Black.copy(alpha = 0.3f),
        drawerContent = {
            FilterDrawer(
                selectedFilters = selectedFilters,
                onDismiss = { coroutineScope.launch { drawerState.close() } },
                onApplyFilters = { filters ->
                    viewModel.applyFilters(filters)
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            state = scrollState
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search for content...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        modifier = Modifier.clickable {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
            }

            if (selectedFilters.isNotEmpty()) {
                item {
                    Text(
                        "Filtered Results (${contentList.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        selectedFilters.forEach { filter ->
                            AssistChip(
                                onClick = { viewModel.removeFilter(filter) },
                                label = { Text(filter) },
                                trailingIcon = { Text("\u2716") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            val groupedContent = contentList.groupBy { it.type }

            groupedContent.forEach { (type, items) ->
                item {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.titleMedium,
                        color = typeColors[type] ?: Color.Black,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .background(
                                typeColors[type]?.copy(alpha = 0.3f) ?: Color.LightGray,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                }

                items(items) { content ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                            .clickable {
                                viewModel.selectContent(content)
                                navController.navigate("contentDetail/${content.title}/${content.type}/${content.category}")
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(content.title, style = MaterialTheme.typography.bodyMedium)
                            Text(content.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
