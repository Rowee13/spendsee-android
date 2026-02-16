package com.spendsee.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.spendsee.data.local.entities.Category
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.theme.ThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    category: Category?,
    type: String,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String) -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "category") }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: "#007AFF") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val isEdit = category != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = currentTheme.getBackground(isDarkMode)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Bar
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEdit) "Edit Category" else "Add Category",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.getText(isDarkMode)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = currentTheme.getText(isDarkMode))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = currentTheme.getSurface(isDarkMode)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                // Category name input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(nameError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = currentTheme.getText(isDarkMode),
                        unfocusedTextColor = currentTheme.getText(isDarkMode),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = currentTheme.getAccent(isDarkMode),
                        unfocusedBorderColor = currentTheme.getBorder(isDarkMode),
                        focusedLabelColor = currentTheme.getAccent(isDarkMode),
                        unfocusedLabelColor = currentTheme.getInactive(isDarkMode),
                        cursorColor = currentTheme.getAccent(isDarkMode),
                        focusedPlaceholderColor = currentTheme.getInactive(isDarkMode),
                        unfocusedPlaceholderColor = currentTheme.getInactive(isDarkMode)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Preview
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Color(android.graphics.Color.parseColor(selectedColor))
                                        .copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(selectedIcon),
                                contentDescription = null,
                                tint = Color(android.graphics.Color.parseColor(selectedColor)),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = name.ifEmpty { "Category Name" },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (name.isEmpty())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon picker
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                IconPicker(
                    selectedIcon = selectedIcon,
                    onIconSelected = { selectedIcon = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color picker
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
                }

                // Bottom Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = currentTheme.getSurface(isDarkMode),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = currentTheme.getText(isDarkMode)
                            )
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                when {
                                    name.isBlank() -> {
                                        nameError = "Name is required"
                                    }
                                    else -> {
                                        onSave(name.trim(), selectedIcon, selectedColor)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.getAccent(isDarkMode),
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (isEdit) "Save" else "Add")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit
) {
    val icons = listOf(
        // General
        "category" to Icons.Outlined.Category,                    // Default/General category

        // Food & Dining
        "restaurant" to Icons.Outlined.Restaurant,                // Dining/Food
        "fastfood" to Icons.Outlined.Fastfood,                    // Fast Food
        "localcafe" to Icons.Outlined.LocalCafe,                  // Coffee/Cafe
        "localpizza" to Icons.Outlined.LocalPizza,                // Pizza
        "shoppingcart" to Icons.Outlined.ShoppingCart,            // Groceries
        "icecream" to Icons.Outlined.Icecream,                    // Dessert

        // Shopping & Retail
        "shoppingbag" to Icons.Outlined.ShoppingBag,              // Shopping
        "store" to Icons.Outlined.Store,                          // Store/Retail
        "localmall" to Icons.Outlined.LocalMall,                  // Mall/Shopping Center
        "checkroom" to Icons.Outlined.Checkroom,                  // Clothing

        // Transportation
        "directionscar" to Icons.Outlined.DirectionsCar,          // Car
        "directionstransit" to Icons.Outlined.DirectionsTransit,  // Public Transit
        "localgasstation" to Icons.Outlined.LocalGasStation,      // Gas/Fuel
        "localparking" to Icons.Outlined.LocalParking,            // Parking
        "twowheeler" to Icons.Outlined.TwoWheeler,                // Motorcycle/Bike
        "localshipping" to Icons.Outlined.LocalShipping,          // Delivery/Shipping

        // Entertainment & Lifestyle
        "movie" to Icons.Outlined.Movie,                          // Movies
        "theaters" to Icons.Outlined.Theaters,                    // Theater/Cinema
        "sportsesports" to Icons.Outlined.SportsEsports,          // Gaming
        "headphones" to Icons.Outlined.Headphones,                // Music/Audio
        "celebration" to Icons.Outlined.Celebration,              // Party/Events
        "cameraalt" to Icons.Outlined.CameraAlt,                  // Photography

        // Bills & Utilities
        "bolt" to Icons.Outlined.Bolt,                            // Electricity
        "waterdrop" to Icons.Outlined.WaterDrop,                  // Water
        "wifi" to Icons.Outlined.Wifi,                            // Internet
        "phone" to Icons.Outlined.Phone,                          // Phone Bill
        "tv" to Icons.Outlined.Tv,                                // TV/Cable/Streaming

        // Health & Fitness
        "localhospital" to Icons.Outlined.LocalHospital,          // Medical/Health
        "medication" to Icons.Outlined.Medication,                // Medicine
        "fitnesscenter" to Icons.Outlined.FitnessCenter,          // Gym/Fitness
        "spa" to Icons.Outlined.Spa,                              // Wellness/Spa
        "selfimprovement" to Icons.Outlined.SelfImprovement,      // Yoga/Meditation

        // Education & Work
        "school" to Icons.Outlined.School,                        // Education
        "menubook" to Icons.Outlined.MenuBook,                    // Books/Learning
        "work" to Icons.Outlined.Work,                            // Work/Business
        "businesscenter" to Icons.Outlined.BusinessCenter,        // Business

        // Travel
        "flight" to Icons.Outlined.Flight,                        // Flight/Air Travel
        "luggage" to Icons.Outlined.Luggage,                      // Travel/Luggage
        "hotel" to Icons.Outlined.Hotel,                          // Hotel/Accommodation
        "beachaccess" to Icons.Outlined.BeachAccess,              // Beach/Vacation

        // Home & Family
        "home" to Icons.Outlined.Home,                            // Home
        "homerepairservice" to Icons.Outlined.HomeRepairService,  // Home Repair
        "chair" to Icons.Outlined.Chair,                          // Furniture
        "pets" to Icons.Outlined.Pets,                            // Pets
        "childcare" to Icons.Outlined.ChildFriendly,                  // Kids/Childcare

        // Financial & Income
        "payments" to Icons.Outlined.Payments,                    // Income/Salary
        "trendingup" to Icons.Outlined.TrendingUp,                // Investment
        "showchart" to Icons.Outlined.ShowChart,                  // Stocks/Trading
        "accountbalance" to Icons.Outlined.AccountBalance,        // Bank/Finance
        "creditcard" to Icons.Outlined.CreditCard,                // Credit/Payment

        // Gifts & Personal
        "cardgiftcard" to Icons.Outlined.CardGiftcard,            // Gifts
        "redeem" to Icons.Outlined.Redeem,                        // Rewards/Cashback
        "favorite" to Icons.Outlined.Favorite,                    // Favorite/Love
        "volunteeractivism" to Icons.Outlined.VolunteerActivism,  // Charity/Donation

        // Insurance & Security
        "shield" to Icons.Outlined.Shield,                        // Insurance/Protection
        "healthandsafety" to Icons.Outlined.HealthAndSafety,      // Health Insurance
        "lock" to Icons.Outlined.Lock,                            // Security

        // Miscellaneous
        "subscriptions" to Icons.Outlined.Subscriptions,          // Subscriptions
        "build" to Icons.Outlined.Build,                          // Tools/Maintenance
        "star" to Icons.Outlined.Star,                            // Favorite/Special
        "morehorizontal" to Icons.Outlined.MoreHoriz              // Other/Misc
    )

    // Use FlowRow for better wrapping and distribution
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 6
    ) {
        icons.forEach { (iconName, iconVector) ->
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedIcon == iconName)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        width = if (selectedIcon == iconName) 2.dp else 0.dp,
                        color = if (selectedIcon == iconName)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onIconSelected(iconName) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = iconName,
                    tint = if (selectedIcon == iconName)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#007AFF", // Blue
        "#34C759", // Green
        "#FF9500", // Orange
        "#FF3B30", // Red
        "#AF52DE", // Purple
        "#FF2D55", // Pink
        "#5AC8FA", // Light Blue
        "#FFCC00", // Yellow
        "#FF6482", // Coral
        "#32ADE6", // Cyan
        "#BF5AF2", // Violet
        "#00C7BE"  // Teal
    )

    // Use FlowRow for better distribution
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 6
    ) {
        colors.forEach { colorHex ->
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .border(
                        width = if (selectedColor == colorHex) 3.dp else 0.dp,
                        color = if (selectedColor == colorHex)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedColor == colorHex) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
