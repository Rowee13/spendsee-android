package com.spendsee.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import com.spendsee.data.local.entities.Category

@Composable
fun AddEditCategoryDialog(
    category: Category?,
    type: String,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "grid") }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: "#007AFF") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val isEdit = category != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = if (isEdit) "Edit Category" else "Add Category",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                    }
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

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEdit) "Save" else "Add")
                    }
                }
            }
        }
    }
}

@Composable
fun IconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit
) {
    val icons = listOf(
        "grid" to FeatherIcons.Grid,
        "briefcase" to FeatherIcons.Briefcase,
        "coffee" to FeatherIcons.Coffee,
        "shoppingcart" to FeatherIcons.ShoppingCart,
        "film" to FeatherIcons.Film,
        "creditcard" to FeatherIcons.CreditCard,
        "heart" to FeatherIcons.Heart,
        "book" to FeatherIcons.Book,
        "mappin" to FeatherIcons.MapPin,
        "home" to FeatherIcons.Home,
        "smartphone" to FeatherIcons.Smartphone,
        "shield" to FeatherIcons.Shield,
        "bell" to FeatherIcons.Bell,
        "droplet" to FeatherIcons.Droplet,
        "gift" to FeatherIcons.Gift,
        "star" to FeatherIcons.Star,
        "truck" to FeatherIcons.Truck,
        "camera" to FeatherIcons.Camera,
        "music" to FeatherIcons.Music,
        "tool" to FeatherIcons.Tool,
        "activity" to FeatherIcons.Activity,
        "dollarsign" to FeatherIcons.DollarSign,
        "trendingup" to FeatherIcons.TrendingUp,
        "package" to FeatherIcons.Package
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(icons) { (iconName, iconVector) ->
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

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

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(colors) { colorHex ->
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                        imageVector = FeatherIcons.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
