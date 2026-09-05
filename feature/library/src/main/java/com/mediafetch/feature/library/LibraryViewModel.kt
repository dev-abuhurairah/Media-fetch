package com.mediafetch.feature.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediafetch.core.database.MediaLibraryDao
import com.mediafetch.core.database.MediaLibraryEntity
import com.mediafetch.core.download.StorageHelper
import com.mediafetch.core.model.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibrarySort {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    SIZE_DESC
}

private data class ViewFilters(
    val category: MediaType?,
    val query: String,
    val sort: LibrarySort,
    val isGrid: Boolean
)

data class LibraryUiState(
    val items: List<MediaLibraryEntity> = emptyList(),
    val selectedCategory: MediaType? = null, // null = All
    val searchQuery: String = "",
    val sortOrder: LibrarySort = LibrarySort.DATE_DESC,
    val isGridView: Boolean = true,
    val selectedItemIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaLibraryDao: MediaLibraryDao,
    private val storageHelper: StorageHelper
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<MediaType?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(LibrarySort.DATE_DESC)
    private val _isGridView = MutableStateFlow(true)
    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())

    private val _filters = combine(
        _selectedCategory,
        _searchQuery,
        _sortOrder,
        _isGridView
    ) { category, query, sort, isGrid ->
        ViewFilters(category, query, sort, isGrid)
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        mediaLibraryDao.getAllLibraryItems(),
        _filters,
        _selectedItemIds
    ) { rawItems, filters, selectedIds ->
        var filtered = rawItems

        if (filters.category != null) {
            filtered = filtered.filter { it.mediaType == filters.category }
        }

        if (filters.query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(filters.query, ignoreCase = true) || it.author.contains(filters.query, ignoreCase = true)
            }
        }

        val sorted = when (filters.sort) {
            LibrarySort.DATE_DESC -> filtered.sortedByDescending { it.addedAt }
            LibrarySort.DATE_ASC -> filtered.sortedBy { it.addedAt }
            LibrarySort.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            LibrarySort.SIZE_DESC -> filtered.sortedByDescending { it.fileSizeBytes }
        }

        LibraryUiState(
            items = sorted,
            selectedCategory = filters.category,
            searchQuery = filters.query,
            sortOrder = filters.sort,
            isGridView = filters.isGrid,
            selectedItemIds = selectedIds,
            isSelectionMode = selectedIds.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun selectCategory(type: MediaType?) {
        _selectedCategory.value = type
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(sort: LibrarySort) {
        _sortOrder.value = sort
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun toggleItemSelection(id: String) {
        val current = _selectedItemIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedItemIds.value = current
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
    }

    fun deleteItem(item: MediaLibraryEntity) {
        viewModelScope.launch {
            if (item.fileUri.isNotBlank()) {
                storageHelper.deleteMedia(Uri.parse(item.fileUri))
            }
            mediaLibraryDao.deleteById(item.id)
        }
    }

    fun deleteSelectedItems() {
        viewModelScope.launch {
            val itemsToDelete = uiState.value.items.filter { _selectedItemIds.value.contains(it.id) }
            itemsToDelete.forEach { item ->
                if (item.fileUri.isNotBlank()) {
                    storageHelper.deleteMedia(Uri.parse(item.fileUri))
                }
                mediaLibraryDao.deleteById(item.id)
            }
            clearSelection()
        }
    }
}