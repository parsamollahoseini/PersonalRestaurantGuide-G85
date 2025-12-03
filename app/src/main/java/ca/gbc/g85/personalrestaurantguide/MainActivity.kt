package ca.gbc.g85.personalrestaurantguide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import ca.gbc.g85.personalrestaurantguide.data.AppDatabase
import ca.gbc.g85.personalrestaurantguide.data.Restaurant
import ca.gbc.g85.personalrestaurantguide.ui.theme.PersonalRestaurantGuideTheme
import ca.gbc.g85.personalrestaurantguide.viewmodel.RestaurantVm
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Add

// =====================================================
// MAIN ACTIVITY – wires Room + ViewModel + Navigation
// =====================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Build Room database and ViewModel
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "restaurants.db"
        ).fallbackToDestructiveMigration().build()
        val vm = RestaurantVm(db.restaurantDao())

        setContent {
            PersonalRestaurantGuideTheme {
                val nav = rememberNavController()
                val items by vm.items.collectAsState()

                NavHost(navController = nav, startDestination = "splash") {

                    composable("splash") {
                        SplashScreen {
                            nav.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }

                    composable("home") {
                        HomeScreen(
                            items = items,
                            onOpen = { r -> nav.navigate("details/${r.id}") },
                            onAdd = { nav.navigate("addEdit/0") },
                            onAbout = { nav.navigate("about") }
                        )
                    }

                    composable("about") {
                        AboutScreen(onBack = { nav.popBackStack() })
                    }

                    composable(
                        route = "details/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments!!.getLong("id")
                        val r = items.firstOrNull { it.id == id }

                        if (r != null) {
                            DetailsScreen(
                                r = r,
                                onBack = { nav.popBackStack() },
                                onMap = { openMap(this@MainActivity, r, nav = false) },
                                onDirections = { origin -> openMap(this@MainActivity, r, nav = true, origin = origin) },
                                onShare = { shareRestaurant(this@MainActivity, r) },
                                onEdit = { nav.navigate("addEdit/${r.id}") },
                                onDelete = {
                                    vm.remove(r)
                                    nav.popBackStack()
                                }
                            )
                        }
                    }

                    composable(
                        route = "addEdit/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments!!.getLong("id")
                        val existing = items.firstOrNull { it.id == id }

                        AddEditScreen(
                            initial = existing,
                            onBack = { nav.popBackStack() },
                            onSave = { saved ->
                                // New restaurant -> id = 0 so Room auto-generates
                                val toSave =
                                    if (id == 0L) saved.copy(id = 0L) else saved.copy(id = id)
                                vm.addOrUpdate(toSave)
                                nav.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// VALIDATION HELPERS
// =====================================================
private fun isValidPhone(phone: String): Boolean {
    val cleaned = phone.replace(Regex("[^0-9]"), "")
    return cleaned.length in 10..11
}

// =====================================================
// ANDROID INTENTS (Share + Maps)
// =====================================================
private fun shareRestaurant(ctx: ComponentActivity, r: Restaurant) {
    try {
        val text = """
            ${r.name}
            ${"★".repeat(r.rating)} | Tags: ${r.tags}
            ${r.address} | ${r.phone}
            https://www.google.com/maps/search/?api=1&query=${Uri.encode(r.address)}
        """.trimIndent()
        val intent = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            "Share restaurant"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openMap(ctx: ComponentActivity, r: Restaurant, nav: Boolean, origin: String = "") {
    try {
        val encodedAddress = Uri.encode(r.address)
        val uri = if (nav) {
            if (origin.isNotBlank()) {
                val encodedOrigin = Uri.encode(origin)
                "https://www.google.com/maps/dir/?api=1&origin=$encodedOrigin&destination=$encodedAddress"
            } else {
                "https://www.google.com/maps/dir/?api=1&destination=$encodedAddress"
            }
        } else {
            "https://www.google.com/maps/search/?api=1&query=$encodedAddress"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// =====================================================
// SPLASH SCREEN
// =====================================================
@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
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
            Text("🍽️", style = MaterialTheme.typography.displayLarge)
            Text(
                "Personal Restaurant Guide",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =====================================================
// HOME SCREEN
// =====================================================
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
        else {
            val s = query.trim().lowercase()
            items.filter {
                it.name.lowercase().contains(s) ||
                        it.tags.lowercase().contains(s) ||
                        it.address.lowercase().contains(s)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurants") },
                actions = {
                    TextButton(onClick = onAbout) {
                        Text("About")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search restaurants…") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, "Search")
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // CONTENT LOGIC
            when {
                items.isEmpty() -> {
                    EmptyState(
                        emoji = "🍽️",
                        title = "No restaurants yet",
                        subtitle = "Tap + to add your first restaurant"
                    )
                }

                filtered.isEmpty() -> {
                    EmptyState(
                        emoji = "🔍",
                        title = "No matches found",
                        subtitle = "Try a different search term"
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filtered, key = { it.id }) { restaurant ->
                            RestaurantCard(restaurant, onOpen)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RestaurantCard(r: Restaurant, onOpen: (Restaurant) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(r) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                r.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                r.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "⭐ ${r.rating}  •  ${r.tags}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            emoji,
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(Modifier.height(12.dp))

        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NoResults() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔎", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(12.dp))
        Text("No restaurants found", style = MaterialTheme.typography.titleMedium)
        Text(
            "Try a different search",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// =====================================================
// DETAILS SCREEN
// =====================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    r: Restaurant,
    onBack: () -> Unit,
    onMap: () -> Unit,
    onDirections: (String) -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDirectionsDialog by remember { mutableStateOf(false) }
    var startingLocation by remember { mutableStateOf("") }

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
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
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

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow("📍 Address", r.address)
                    DetailRow("📞 Phone", r.phone)
                    DetailRow("🏷️ Tags", r.tags)
                    if (r.description.isNotBlank()) {
                        DetailRow("📝 Description", r.description)
                    }
                }
            }

            Text(
                "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onMap, modifier = Modifier.weight(1f)) { Text("Map") }
                Button(onClick = { showDirectionsDialog = true }, modifier = Modifier.weight(1f)) { Text("Directions") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Text("Share")
                }
                FilledTonalButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
            }
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

    if (showDirectionsDialog) {
        AlertDialog(
            onDismissRequest = { showDirectionsDialog = false },
            title = { Text("Get Directions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your starting location (optional):")
                    OutlinedTextField(
                        value = startingLocation,
                        onValueChange = { startingLocation = it },
                        placeholder = { Text("e.g., 123 Main St, Toronto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Leave blank to use your current location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDirectionsDialog = false
                        onDirections(startingLocation)
                        startingLocation = ""
                    }
                ) { Text("Get Directions") }
            },
            dismissButton = {
                TextButton(onClick = { showDirectionsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

// =====================================================
// ADD / EDIT SCREEN
// =====================================================
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

    var nameError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

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
    ) { padding ->
        LazyColumn(
            Modifier
                .padding(padding)
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
                    supportingText = { if (nameError) Text("Name is required") },
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
                    supportingText = { if (addressError) Text("Address is required") },
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
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank() &&
                            address.isNotBlank() &&
                            !phoneError
                        ) {
                            onSave(
                                Restaurant(
                                    id = initial?.id ?: 0L,
                                    name = name.trim(),
                                    address = address.trim(),
                                    phone = phone.trim(),
                                    description = desc.trim(),
                                    tags = tags.trim().lowercase(),
                                    rating = rating
                                )
                            )
                        } else {
                            nameError = name.isBlank()
                            addressError = address.isBlank()
                        }
                    },
                    enabled = name.isNotBlank() &&
                            address.isNotBlank() &&
                            !phoneError,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (initial == null) "Add Restaurant" else "Save Changes")
                }
            }
        }
    }
}



// =====================================================
// ABOUT SCREEN
// =====================================================
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
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🍽️", style = MaterialTheme.typography.displayMedium)
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
                "Made with ❤️ for food lovers",
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

