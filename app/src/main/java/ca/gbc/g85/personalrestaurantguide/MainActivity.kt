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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.round
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


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
 * Main Activity with Navigation setup
 * @author Mehrad Bayat
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalRestaurantGuideTheme {
                val nav = rememberNavController()
                val vm: RestaurantVm = viewModel()

                NavHost(navController = nav, startDestination = "splash") {

                    composable("splash") {
                        SplashScreen {
                            nav.navigate("home") { popUpTo("splash") { inclusive = true } }
                        }
                    }

                    composable("home") {
                        HomeScreen(
                            items = vm.items,
                            onOpen = { nav.navigate("details/${it.id}") },
                            onAdd = { nav.navigate("addEdit/0") },
                            onAbout = { nav.navigate("about") }
                        )
                    }

                    composable("about") {
                        AboutScreen(
                            onBack = { nav.popBackStack() }
                        )
                    }

                    composable(
                        "details/{id}",
                        arguments = listOf(navArgument("id"){ type = NavType.LongType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments!!.getLong("id")
                        val r by remember { derivedStateOf { vm.byId(id) } }

                        if (r == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            DetailsScreen(
                                r = r!!,
                                onBack = {
                                    nav.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onEdit = { nav.navigate("addEdit/${r!!.id}") },
                                onDelete = {
                                    val restaurantId = r!!.id
                                    vm.remove(restaurantId)
                                    nav.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }

                    composable(
                        "addEdit/{id}",
                        arguments = listOf(navArgument("id"){ type = NavType.LongType })
                    ) {
                        val id = it.arguments!!.getLong("id")
                        val existing = vm.items.find { it.id == id }
                        AddEditScreen(
                            initial = existing,
                            onBack = { nav.popBackStack() },
                            onSave = { saved ->
                                vm.addOrUpdate(
                                    saved.copy(
                                        id = if (saved.id == 0L)
                                            (round(Math.random() * 1_000_000)).toLong()
                                        else saved.id
                                    )
                                )
                                nav.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    // ==================== ADD/EDIT SCREEN ====================
    /**
     * Form screen for creating and editing restaurants
     * @author Kevin George Buhain
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddEditScreen(
        initial: Restaurant?,
        onBack: () -> Unit,
        onSave: (Restaurant) -> Unit
    ) {
        var name by remember { mutableStateOf(initial?.name ?: "") }
        var address by remember { mutableStateOf(initial?.address ?: "") }
        var phone by remember { mutableStateOf(initial?.phone ?: "") }
        var desc by remember { mutableStateOf(initial?.description ?: "") }
        var tags by remember { mutableStateOf(initial?.tags ?: "") }
        var rating by remember { mutableStateOf(initial?.rating ?: 0) }
        var lat by remember { mutableStateOf(initial?.lat?.toString() ?: "43.65") }
        var lng by remember { mutableStateOf(initial?.lng?.toString() ?: "-79.38") }

        var nameError by remember { mutableStateOf(false) }
        var addressError by remember { mutableStateOf(false) }
        var phoneError by remember { mutableStateOf(false) }
        var latError by remember { mutableStateOf(false) }
        var lngError by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (initial == null) "Add Restaurant" else "Edit Restaurant") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { p ->
            LazyColumn(
                Modifier
                    .padding(p)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = it.isBlank()
                        },
                        label = { Text("Restaurant Name *") },
                        singleLine = true,
                        isError = nameError,
                        supportingText = {
                            if (nameError) Text("Name is required")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            addressError = it.isBlank()
                        },
                        label = { Text("Address *") },
                        isError = addressError,
                        supportingText = {
                            if (addressError) Text("Address is required")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = it.isNotBlank() && !isValidPhone(it)
                        },
                        label = { Text("Phone") },
                        singleLine = true,
                        isError = phoneError,
                        supportingText = {
                            if (phoneError) Text("Please enter a valid phone number")
                            else Text("Format: 416-555-1234")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags") },
                        supportingText = { Text("Comma-separated (e.g., italian, pasta, cozy)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Rating",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..5).forEach { i ->
                                FilterChip(
                                    selected = rating >= i,
                                    onClick = { rating = i },
                                    label = { Text("$i ★") }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Location Coordinates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = lat,
                            onValueChange = {
                                lat = it
                                val latValue = it.toDoubleOrNull()
                                latError = latValue == null || !isValidLatitude(latValue)
                            },
                            label = { Text("Latitude") },
                            isError = latError,
                            supportingText = {
                                if (latError) Text("Must be between -90 and 90")
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lng,
                            onValueChange = {
                                lng = it
                                val lngValue = it.toDoubleOrNull()
                                lngError = lngValue == null || !isValidLongitude(lngValue)
                            },
                            label = { Text("Longitude") },
                            isError = lngError,
                            supportingText = {
                                if (lngError) Text("Must be between -180 and 180")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val latValue = lat.toDoubleOrNull() ?: 0.0
                            val lngValue = lng.toDoubleOrNull() ?: 0.0

                            if (name.isNotBlank() &&
                                address.isNotBlank() &&
                                !phoneError &&
                                !latError &&
                                !lngError &&
                                isValidLatitude(latValue) &&
                                isValidLongitude(lngValue)) {
                                onSave(
                                    Restaurant(
                                        id = initial?.id ?: 0L,
                                        name = name.trim(),
                                        address = address.trim(),
                                        phone = phone.trim(),
                                        description = desc.trim(),
                                        tags = tags.trim().lowercase(),
                                        rating = rating,
                                        lat = latValue,
                                        lng = lngValue
                                    )
                                )
                            } else {
                                nameError = name.isBlank()
                                addressError = address.isBlank()
                            }
                        },
                        enabled = name.isNotBlank() &&
                                address.isNotBlank() &&
                                !phoneError &&
                                !latError &&
                                !lngError,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (initial == null) "Add Restaurant" else "Save Changes")
                    }
                }
            }
        }
    }

    // ==================== ABOUT SCREEN ====================
    /**
     * About screen with app information
     * @author Kevin George Buhain
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AboutScreen(onBack: () -> Unit) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("About") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { p ->
            Column(
                Modifier
                    .padding(p)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "🍽",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    "Personal Restaurant Guide",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Features:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("• Save your favorite restaurants with ratings and tags")
                Text("• Search by name, tags, or address")
                Text("• Get directions and view on maps")
                Text("• Share restaurants with friends")
                Text("• Organize with custom tags")

                Spacer(Modifier.weight(1f))

                Text(
                    "Made with ❤ for food lovers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Group G-85",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

}

// ==================== SPLASH SCREEN ====================
/**
 * Splash screen with auto-navigation
 * @author Mehrad Bayat
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        onDone()
    }
    Box(
        Modifier.fillMaxSize(),
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== HOME SCREEN ====================
/**
 * Main home screen with restaurant list and search
 * @author Mehrad Bayat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    items: List<Restaurant>,
    onOpen: (Restaurant) -> Unit,
    onAdd: () -> Unit,
    onAbout: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(items, query) {
        if (query.isBlank()) items
        else items.filter {
            val s = query.trim().lowercase()
            it.name.lowercase().contains(s) ||
                    it.tags.lowercase().contains(s) ||
                    it.address.lowercase().contains(s)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurants (${items.size})") },
                actions = {
                    TextButton(onClick = onAbout) { Text("About") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search by name, tag, or address") },
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            if (filtered.isEmpty() && query.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🔍",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            "No restaurants found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Try a different search term",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
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
                            "No restaurants yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap the + button to add your first restaurant",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(filtered, key = { it.id }) { restaurant ->
                        ListItem(
                            headlineContent = {
                                Text(restaurant.name, fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Text("${restaurant.tags} · ${"★".repeat(restaurant.rating)}")
                            },
                            overlineContent = { Text(restaurant.address) },
                            modifier = Modifier.clickable { onOpen(restaurant) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// ==================== DETAILS SCREEN ====================
/**
 * Restaurant details screen with actions
 * @author Mehrad Bayat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    r: Restaurant,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(r.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Rating",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "★".repeat(r.rating) + "☆".repeat(5 - r.rating),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("📍 Address", r.address)
                    DetailRow("📞 Phone", r.phone)
                    DetailRow("🏷️ Tags", r.tags)
                    if (r.description.isNotBlank()) {
                        DetailRow("📝 Description", r.description)
                    }
                    DetailRow("🗺️ Coordinates", "${String.format("%.4f", r.lat)}, ${String.format("%.4f", r.lng)}")
                }
            }

            Text(
                "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            FilledTonalButton(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Edit Restaurant") }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Restaurant?") },
            text = { Text("Are you sure you want to delete ${r.name}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Helper composable for displaying detail rows
 * @author Mehrad Bayat
 */
@Composable
fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}