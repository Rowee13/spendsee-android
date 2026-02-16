package com.spendsee.ui.screens.categories

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.spendsee.data.local.entities.Category
import com.spendsee.managers.Feature
import com.spendsee.managers.PremiumManager
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.screens.premium.PremiumPaywallScreen
import com.spendsee.ui.theme.ThemeColors
import com.spendsee.viewmodels.CategoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBackClick: () -> Unit
) {
    val viewModel: CategoriesViewModel = viewModel()
    val context = LocalContext.current
    val view = LocalView.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val premiumManager = remember { PremiumManager.getInstance(context) }
    val isPremium by premiumManager.isPremium.collectAsState()

    // Set status bar color to match header
    SideEffect {
        val window = (view.context as? Activity)?.window
        val headerColor = currentTheme.getSurface(isDarkMode)
        window?.statusBarColor = android.graphics.Color.argb(
            (headerColor.alpha * 255).toInt(),
            (headerColor.red * 255).toInt(),
            (headerColor.green * 255).toInt(),
            (headerColor.blue * 255).toInt()
        )
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    // Reset status bar color when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            val window = (view.context as? Activity)?.window
            val bgColor = currentTheme.getBackground(isDarkMode)
            window?.statusBarColor = android.graphics.Color.argb(
                (bgColor.alpha * 255).toInt(),
                (bgColor.red * 255).toInt(),
                (bgColor.green * 255).toInt(),
                (bgColor.blue * 255).toInt()
            )
        }
    }

    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPremiumPaywall by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    val tabs = listOf("Expense", "Income")
    val currentCategories = if (selectedTabIndex == 0) expenseCategories else incomeCategories
    val currentType = if (selectedTabIndex == 0) "expense" else "income"

    Scaffold(
        containerColor = currentTheme.getBackground(isDarkMode),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Categories",
                        color = currentTheme.getText(isDarkMode)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = currentTheme.getText(isDarkMode)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = currentTheme.getSurface(isDarkMode)
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (premiumManager.hasFeature(Feature.CUSTOM_CATEGORIES)) {
                        showAddDialog = true
                    } else {
                        showPremiumPaywall = true
                    }
                },
                containerColor = currentTheme.getAccent(isDarkMode)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Category", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(currentTheme.getBackground(isDarkMode))
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = currentTheme.getSurface(isDarkMode),
                contentColor = currentTheme.getAccent(isDarkMode)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Premium notice for free users
            if (!isPremium) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = currentTheme.getSurface(isDarkMode)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            tint = currentTheme.getAccent(isDarkMode),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Custom Categories",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.getText(isDarkMode)
                            )
                            Text(
                                text = "Unlock premium to create custom categories",
                                style = MaterialTheme.typography.bodySmall,
                                color = currentTheme.getInactive(isDarkMode)
                            )
                        }
                    }
                }
            }

            // Categories list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Default categories section
                val defaultCategories = currentCategories.filter { it.isDefault }
                if (defaultCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Default Categories",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getAccent(isDarkMode),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(defaultCategories) { category ->
                        CategoryItem(
                            category = category,
                            onEdit = null, // Can't edit default categories
                            onDelete = null, // Can't delete default categories
                            currentTheme = currentTheme,
                            isDarkMode = isDarkMode
                        )
                    }
                }

                // Custom categories section
                val customCategories = currentCategories.filter { !it.isDefault }
                if (customCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Custom Categories",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getAccent(isDarkMode),
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 0.dp)
                        )
                    }
                    items(customCategories) { category ->
                        CategoryItem(
                            category = category,
                            onEdit = {
                                categoryToEdit = category
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.deleteCategory(category)
                            },
                            currentTheme = currentTheme,
                            isDarkMode = isDarkMode
                        )
                    }
                }

                // Empty state
                if (currentCategories.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No categories yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = currentTheme.getInactive(isDarkMode)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add category dialog
    if (showAddDialog) {
        AddEditCategoryDialog(
            category = null,
            type = currentType,
            onDismiss = { showAddDialog = false },
            onSave = { name, icon, color ->
                viewModel.addCategory(name, icon, color, currentType)
                showAddDialog = false
            }
        )
    }

    // Edit category dialog
    if (showEditDialog && categoryToEdit != null) {
        AddEditCategoryDialog(
            category = categoryToEdit,
            type = currentType,
            onDismiss = {
                showEditDialog = false
                categoryToEdit = null
            },
            onSave = { name, icon, color ->
                categoryToEdit?.let { category ->
                    viewModel.updateCategory(
                        category.copy(
                            name = name,
                            icon = icon,
                            colorHex = color
                        )
                    )
                }
                showEditDialog = false
                categoryToEdit = null
            }
        )
    }

    // Premium paywall
    if (showPremiumPaywall) {
        PremiumPaywallScreen(
            onDismiss = { showPremiumPaywall = false },
            onPurchaseSuccess = {
                showPremiumPaywall = false
            }
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.getSurface(isDarkMode)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onEdit != null) {
                    if (onEdit != null) {
                        showMenu = true
                    }
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Color(android.graphics.Color.parseColor(category.colorHex)).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.icon),
                    contentDescription = null,
                    tint = Color(android.graphics.Color.parseColor(category.colorHex)),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Category name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = currentTheme.getText(isDarkMode)
                )
                if (category.isDefault) {
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                }
            }

            // Menu for custom categories
            if (onEdit != null || onDelete != null) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "More options",
                            tint = currentTheme.getInactive(isDarkMode)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (onEdit != null) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Edit, contentDescription = null)
                                }
                            )
                        }
                        if (onDelete != null) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to get icon by name (same as in other screens)
fun getCategoryIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName.lowercase()) {
        // General
        "category" -> Icons.Outlined.Category

        // Food & Dining
        "restaurant" -> Icons.Outlined.Restaurant
        "fastfood" -> Icons.Outlined.Fastfood
        "localcafe" -> Icons.Outlined.LocalCafe
        "localpizza" -> Icons.Outlined.LocalPizza
        "shoppingcart" -> Icons.Outlined.ShoppingCart
        "icecream" -> Icons.Outlined.Icecream

        // Shopping & Retail
        "shoppingbag" -> Icons.Outlined.ShoppingBag
        "store" -> Icons.Outlined.Store
        "localmall" -> Icons.Outlined.LocalMall
        "checkroom" -> Icons.Outlined.Checkroom

        // Transportation
        "directionscar" -> Icons.Outlined.DirectionsCar
        "directionstransit" -> Icons.Outlined.DirectionsTransit
        "localgasstation" -> Icons.Outlined.LocalGasStation
        "localparking" -> Icons.Outlined.LocalParking
        "twowheeler" -> Icons.Outlined.TwoWheeler
        "localshipping" -> Icons.Outlined.LocalShipping

        // Entertainment & Lifestyle
        "movie" -> Icons.Outlined.Movie
        "theaters" -> Icons.Outlined.Theaters
        "sportsesports" -> Icons.Outlined.SportsEsports
        "headphones" -> Icons.Outlined.Headphones
        "celebration" -> Icons.Outlined.Celebration
        "cameraalt" -> Icons.Outlined.CameraAlt

        // Bills & Utilities
        "receipt" -> Icons.Outlined.Receipt
        "bolt" -> Icons.Outlined.Bolt
        "waterdrop" -> Icons.Outlined.WaterDrop
        "wifi" -> Icons.Outlined.Wifi
        "phone" -> Icons.Outlined.Phone
        "tv" -> Icons.Outlined.Tv

        // Health & Fitness
        "localhospital" -> Icons.Outlined.LocalHospital
        "medication" -> Icons.Outlined.Medication
        "fitnesscenter" -> Icons.Outlined.FitnessCenter
        "spa" -> Icons.Outlined.Spa
        "selfimprovement" -> Icons.Outlined.SelfImprovement

        // Education & Work
        "school" -> Icons.Outlined.School
        "menubook" -> Icons.Outlined.MenuBook
        "work" -> Icons.Outlined.Work
        "businesscenter" -> Icons.Outlined.BusinessCenter
        "laptop" -> Icons.Outlined.Laptop

        // Travel
        "flight" -> Icons.Outlined.Flight
        "luggage" -> Icons.Outlined.Luggage
        "hotel" -> Icons.Outlined.Hotel
        "beachaccess" -> Icons.Outlined.BeachAccess

        // Home & Family
        "home" -> Icons.Outlined.Home
        "homerepairservice" -> Icons.Outlined.HomeRepairService
        "chair" -> Icons.Outlined.Chair
        "pets" -> Icons.Outlined.Pets
        "childcare" -> Icons.Outlined.ChildFriendly

        // Financial & Income
        "payments" -> Icons.Outlined.Payments
        "trendingup" -> Icons.Outlined.TrendingUp
        "showchart" -> Icons.Outlined.ShowChart
        "accountbalance" -> Icons.Outlined.AccountBalance
        "creditcard" -> Icons.Outlined.CreditCard

        // Gifts & Personal
        "cardgiftcard" -> Icons.Outlined.CardGiftcard
        "redeem" -> Icons.Outlined.Redeem
        "favorite" -> Icons.Outlined.Favorite
        "volunteeractivism" -> Icons.Outlined.VolunteerActivism
        "emojievents" -> Icons.Outlined.EmojiEvents
        "assignmentreturn" -> Icons.Outlined.AssignmentReturn

        // Insurance & Security
        "shield" -> Icons.Outlined.Shield
        "healthandsafety" -> Icons.Outlined.HealthAndSafety
        "lock" -> Icons.Outlined.Lock

        // Miscellaneous
        "subscriptions" -> Icons.Outlined.Subscriptions
        "build" -> Icons.Outlined.Build
        "star" -> Icons.Outlined.Star
        "morehorizontal" -> Icons.Outlined.MoreHoriz
        "calculate" -> Icons.Outlined.Calculate

        // Legacy Feather icon names (backward compatibility)
        "briefcase" -> Icons.Outlined.Work
        "gift" -> Icons.Outlined.CardGiftcard
        "rotateccw" -> Icons.Outlined.Refresh
        "dollarSign", "dollarsign" -> Icons.Outlined.Payments
        "coffee" -> Icons.Outlined.LocalCafe
        "film" -> Icons.Outlined.Movie
        "heart" -> Icons.Outlined.Favorite
        "book" -> Icons.Outlined.MenuBook
        "mappin" -> Icons.Outlined.LocationOn
        "smartphone" -> Icons.Outlined.PhoneAndroid
        "bell" -> Icons.Outlined.Notifications
        "droplet" -> Icons.Outlined.WaterDrop
        "tag" -> Icons.Outlined.Label
        "package" -> Icons.Outlined.Inventory
        "music" -> Icons.Outlined.Headphones
        "camera" -> Icons.Outlined.CameraAlt
        "car", "truck" -> Icons.Outlined.DirectionsCar
        "activity" -> Icons.Outlined.FitnessCenter
        "tool" -> Icons.Outlined.Build
        "file" -> Icons.Outlined.Description
        "grid" -> Icons.Outlined.Category

        else -> Icons.Outlined.Category
    }
}
