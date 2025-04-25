package com.example.strokefree.ViewModels

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EducationalContent(
    val title: String = "",
    val type: String = "",
    val category: String = "",
    val url: String = ""
)

class EducationalViewModel : ViewModel() {

    private val _selectedContent = MutableStateFlow<EducationalContent?>(null)
    val selectedContent: StateFlow<EducationalContent?> = _selectedContent

    private val _selectedFilters = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilters: StateFlow<Set<String>> = _selectedFilters

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _contentList = MutableStateFlow<List<EducationalContent>>(emptyList())
    val contentList: StateFlow<List<EducationalContent>> = _contentList

    val scrollState = LazyListState()

    private val allContent = mutableListOf<EducationalContent>() // Holds all data from Firebase

    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadEducationalContent()
    }

    // Fetch data from Firebase
    private fun loadEducationalContent() {
        firestore.collection("education")
            .get()
            .addOnSuccessListener { documents ->
                allContent.clear()
                for (document in documents) {
                    val content = EducationalContent(
                        title = document.getString("title") ?: "",
                        type = document.getString("type") ?: "",
                        category = document.getString("category") ?: "",
                        url = document.getString("url") ?: ""
                    )
                    allContent.add(content)
                }
                filterContent()
            }
            .addOnFailureListener { exception ->
                println("Error fetching data: $exception")
            }
    }

    fun applyFilters(filters: Set<String>) {
        _selectedFilters.value = filters
        filterContent()
    }

    fun removeFilter(filter: String) {
        _selectedFilters.value -= filter
        filterContent()
    }

    fun clearFilters() {
        _selectedFilters.value = emptySet()
        filterContent()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterContent()
    }

    private fun filterContent() {
        viewModelScope.launch {
            val query = _searchQuery.value.lowercase().trim() // ✅ Normalize search query
            val filters = _selectedFilters.value

            println("Search Query: '$query' | Filters: $filters") // ✅ Debugging input

            val hasTypeFilter = filters.any { it in allContent.map { content -> content.type } }
            val hasCategoryFilter = filters.any { it in allContent.map { content -> content.category } }

            val filteredList = if (filters.isEmpty() && query.isEmpty()) {
                allContent // Show all content if no filters and no search query
            } else {
                allContent.filter { content ->
                    val matchesType = filters.contains(content.type)
                    val matchesCategory = filters.contains(content.category)
                    val matchesSearch = query.isEmpty() || content.title.lowercase().contains(query)

                    val isItemIncluded = when {
                        // ✅ If only Type(s) selected, match Type
                        hasTypeFilter && !hasCategoryFilter -> matchesType

                        // ✅ If only Category(s) selected, match Category
                        hasCategoryFilter && !hasTypeFilter -> matchesCategory

                        // ✅ If both Type(s) and Category(s) selected, must match both
                        hasTypeFilter && hasCategoryFilter -> matchesType && matchesCategory

                        // ✅ If no filters but search query exists, only match search query
                        filters.isEmpty() && query.isNotEmpty() -> matchesSearch

                        // Default case (shouldn't happen)
                        else -> false
                    } && matchesSearch // ✅ Ensure search applies to all cases

                    println("Checking: ${content.title}, Type: ${content.type}, Category: ${content.category} --> Included: $isItemIncluded") // ✅ Debugging each item

                    isItemIncluded
                }
            }

            println("Final Filtered Items: ${filteredList.size}") // ✅ Debug filtered results
            _contentList.value = filteredList
        }
    }




    fun selectContent(content: EducationalContent) {
        _selectedContent.value = content
    }
}
