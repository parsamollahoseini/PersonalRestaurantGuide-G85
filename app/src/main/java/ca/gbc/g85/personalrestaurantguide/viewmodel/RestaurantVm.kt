package ca.gbc.g85.personalrestaurantguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.g85.personalrestaurantguide.data.Restaurant
import ca.gbc.g85.personalrestaurantguide.data.RestaurantDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RestaurantVm(
    private val dao: RestaurantDao
) : ViewModel() {

    private val _items = MutableStateFlow<List<Restaurant>>(emptyList())
    val items: StateFlow<List<Restaurant>> = _items

    init {
        viewModelScope.launch {
            dao.getAll().collectLatest { list ->
                _items.value = list
            }
        }
    }

    fun byId(id: Long): Restaurant? =
        _items.value.firstOrNull { it.id == id }

    fun addOrUpdate(r: Restaurant) {
        viewModelScope.launch {
            dao.insertOrUpdate(r)
        }
    }

    fun remove(r: Restaurant) {
        viewModelScope.launch {
            dao.delete(r)
        }
    }
}
