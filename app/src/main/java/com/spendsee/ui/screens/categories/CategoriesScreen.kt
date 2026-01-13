package com.spendsee.ui.screens.categories

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import com.spendsee.data.local.entities.Category
import com.spendsee.managers.Feature
import com.spendsee.managers.PremiumManager
import com.spendsee.ui.screens.premium.PremiumPaywallScreen
import com.spendsee.viewmodels.CategoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBackClick: () -> Unit
) {
    val viewModel: CategoriesViewModel = viewModel()
    val context = LocalContext.current
    val premiumManager = remember { PremiumManager.getInstance(context) }
    val isPremium by premiumManager.isPremium.collectAsState()

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
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(FeatherIcons.Plus, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
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
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
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
                                FeatherIcons.X,
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
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            FeatherIcons.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Custom Categories",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Unlock premium to create custom categories",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(defaultCategories) { category ->
                        CategoryItem(
                            category = category,
                            onEdit = null, // Can't edit default categories
                            onDelete = null // Can't delete default categories
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
                            color = MaterialTheme.colorScheme.primary,
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
                            }
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    onDelete: (() -> Unit)?
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    fontWeight = FontWeight.Medium
                )
                if (category.isDefault) {
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Menu for custom categories
            if (onEdit != null || onDelete != null) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            FeatherIcons.MoreVertical,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    Icon(FeatherIcons.Edit2, contentDescription = null)
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
                                        FeatherIcons.Trash2,
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
        "briefcase" -> FeatherIcons.Briefcase
        "trendingup" -> FeatherIcons.TrendingUp
        "gift" -> FeatherIcons.Gift
        "rotateccw" -> FeatherIcons.RotateCcw
        "home" -> FeatherIcons.Home
        "dollarSign", "dollarsign" -> FeatherIcons.DollarSign
        "coffee" -> FeatherIcons.Coffee
        "shoppingcart" -> FeatherIcons.ShoppingCart
        "film" -> FeatherIcons.Film
        "creditcard" -> FeatherIcons.CreditCard
        "heart" -> FeatherIcons.Heart
        "book" -> FeatherIcons.Book
        "mappin" -> FeatherIcons.MapPin
        "smartphone" -> FeatherIcons.Smartphone
        "shield" -> FeatherIcons.Shield
        "bell" -> FeatherIcons.Bell
        "droplet" -> FeatherIcons.Droplet
        "tag" -> FeatherIcons.Tag
        "package" -> FeatherIcons.Package
        "music" -> FeatherIcons.Music
        "camera" -> FeatherIcons.Camera
        "car", "truck" -> FeatherIcons.Truck
        "activity" -> FeatherIcons.Activity
        "tool" -> FeatherIcons.Tool
        "file" -> FeatherIcons.File
        "star" -> FeatherIcons.Star
        else -> FeatherIcons.Grid
    }
}
