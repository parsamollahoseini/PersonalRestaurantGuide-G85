package ca.gbc.g85.personalrestaurantguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import ca.gbc.g85.personalrestaurantguide.ui.theme.PersonalRestaurantGuideTheme

// ==================== DATA MODEL ====================
/**
 * Restaurant data model
 * @author Parsa Molahosseini
 */
data class Restaurant(
    val id: Long,
    val name: String,
    val address: String,
    val phone: String,
    val description: String,
    val tags: String,
    val rating: Int,
    val lat: Double,
    val lng: Double
)

/**
 * Sample restaurant data for testing
 * @author Parsa Molahosseini
 */
private val sample = listOf(
    Restaurant(1, "Sugo Pasta Bar", "760 College St, Toronto", "416-555-2345",
        "Cozy Italian spot with homemade pasta", "italian,pasta", 5, 43.653, -79.383),
    Restaurant(2, "Banh Mi Boys", "392 Queen St W, Toronto", "416-555-9123",
        "Vietnamese fusion sandwiches", "asian,fastfood", 4, 43.647, -79.395),
    Restaurant(3, "Seven Lives", "69 Kensington Ave, Toronto", "416-555-8888",
        "Fresh tacos & seafood", "mexican,seafood", 5, 43.654, -79.400),
)

// ==================== VIEWMODEL ====================
/**
 * ViewModel for managing restaurant data
 * Survives configuration changes and provides reactive state
 * @author Parsa Molahosseini
 */
class RestaurantVm : ViewModel() {
    private val _items = mutableStateListOf<Restaurant>().apply { addAll(sample) }
    val items: List<Restaurant> get() = _items

    fun byId(id: Long): Restaurant? = _items.firstOrNull { it.id == id }

    fun addOrUpdate(r: Restaurant) {
        val i = _items.indexOfFirst { it.id == r.id }
        if (i >= 0) _items[i] = r else _items.add(0, r)
    }

    fun remove(id: Long) { _items.removeIf { it.id == id } }
}

// ==================== VALIDATION ====================
/**
 * Validates phone number format
 * Accepts 10-11 digit numbers with optional formatting
 * @author Parsa Molahosseini
 */
private fun isValidPhone(phone: String): Boolean {
    val cleaned = phone.replace(Regex("[^0-9]"), "")
    return cleaned.length in 10..11
}

/**
 * Validates latitude coordinate
 * Must be between -90 and 90 degrees
 * @author Parsa Molahosseini
 */
private fun isValidLatitude(lat: Double): Boolean = lat in -90.0..90.0

/**
 * Validates longitude coordinate
 * Must be between -180 and 180 degrees
 * @author Parsa Molahosseini
 */
private fun isValidLongitude(lng: Double): Boolean = lng in -180.0..180.0

// ==================== MAIN ACTIVITY ====================
/**
 * Main Activity - Entry point
 * Navigation and screens will be added by team members
 * @author Parsa Molahosseini
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalRestaurantGuideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Placeholder - team will add navigation here
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "🍽️",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Text(
                                "Personal Restaurant Guide",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("Group G-85")
                            Text("Data Model: Ready ✅")
                            Text("ViewModel: Ready ✅")
                            Text("Validation: Ready ✅")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Waiting for team to add screens...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}