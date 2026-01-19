package com.spendsee.ui.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import compose.icons.FeatherIcons
import compose.icons.feathericons.Delete
import com.spendsee.managers.PasscodeManager
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.theme.ThemeColors

@Composable
fun PasscodeLockScreen(
    onUnlocked: () -> Unit,
    mode: PasscodeMode = PasscodeMode.ENTER,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val passcodeManager = remember { PasscodeManager.getInstance(context) }

    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConfirmStep by remember { mutableStateOf(false) }

    val activity = context as? FragmentActivity

    // Auto-validate when 6 digits entered
    LaunchedEffect(passcode) {
        if (passcode.length == 6) {
            when (mode) {
                PasscodeMode.ENTER -> {
                    if (passcodeManager.validatePasscode(passcode)) {
                        onUnlocked()
                    } else {
                        errorMessage = "Incorrect passcode"
                        passcode = ""
                    }
                }
                PasscodeMode.SET -> {
                    if (!isConfirmStep) {
                        // First entry, now ask for confirmation
                        isConfirmStep = true
                        confirmPasscode = passcode
                        passcode = ""
                    } else {
                        // Confirm step
                        if (passcode == confirmPasscode) {
                            passcodeManager.savePasscode(passcode)
                            onUnlocked()
                        } else {
                            errorMessage = "Passcodes don't match"
                            passcode = ""
                            confirmPasscode = ""
                            isConfirmStep = false
                        }
                    }
                }
                PasscodeMode.CHANGE -> {
                    if (!isConfirmStep) {
                        // Verify old passcode
                        if (passcodeManager.validatePasscode(passcode)) {
                            isConfirmStep = true
                            confirmPasscode = ""
                            passcode = ""
                        } else {
                            errorMessage = "Incorrect current passcode"
                            passcode = ""
                        }
                    } else {
                        if (confirmPasscode.isEmpty()) {
                            // First entry of new passcode
                            confirmPasscode = passcode
                            passcode = ""
                        } else {
                            // Confirm new passcode
                            if (passcode == confirmPasscode) {
                                passcodeManager.savePasscode(passcode)
                                onUnlocked()
                            } else {
                                errorMessage = "Passcodes don't match"
                                passcode = ""
                                confirmPasscode = ""
                            }
                        }
                    }
                }
            }
        }
    }

    // Show biometric on mount for ENTER mode
    LaunchedEffect(Unit) {
        if (mode == PasscodeMode.ENTER &&
            passcodeManager.isBiometricEnabled() &&
            activity != null) {
            passcodeManager.authenticateWithBiometrics(
                activity = activity,
                onSuccess = onUnlocked,
                onError = { error -> errorMessage = error },
                onCancel = { /* User wants to use passcode */ }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = currentTheme.getBackground(isDarkMode)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = when {
                    mode == PasscodeMode.ENTER -> "Enter Passcode"
                    mode == PasscodeMode.SET && !isConfirmStep -> "Set Passcode"
                    mode == PasscodeMode.SET && isConfirmStep -> "Confirm Passcode"
                    mode == PasscodeMode.CHANGE && !isConfirmStep -> "Enter Current Passcode"
                    mode == PasscodeMode.CHANGE && confirmPasscode.isEmpty() -> "Enter New Passcode"
                    else -> "Confirm New Passcode"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = currentTheme.getText(isDarkMode)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Passcode dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(6) { index ->
                    PasscodeDot(
                        filled = index < passcode.length,
                        currentTheme = currentTheme,
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Number pad
            NumberPad(
                onNumberClick = { number ->
                    if (passcode.length < 6) {
                        passcode += number
                        errorMessage = null
                    }
                },
                onBackspace = {
                    if (passcode.isNotEmpty()) {
                        passcode = passcode.dropLast(1)
                        errorMessage = null
                    }
                },
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
            )

            // Biometric button (only for ENTER mode)
            if (mode == PasscodeMode.ENTER &&
                passcodeManager.isBiometricEnabled() &&
                activity != null) {
                Spacer(modifier = Modifier.height(32.dp))
                IconButton(
                    onClick = {
                        passcodeManager.authenticateWithBiometrics(
                            activity = activity,
                            onSuccess = onUnlocked,
                            onError = { error -> errorMessage = error },
                            onCancel = { /* User wants to use passcode */ }
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Use Biometric Authentication",
                        modifier = Modifier.size(32.dp),
                        tint = currentTheme.getAccent(isDarkMode)
                    )
                }
            }

            // Cancel button (only for SET and CHANGE modes)
            if (onDismiss != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        color = currentTheme.getAccent(isDarkMode)
                    )
                }
            }
        }
    }
}

@Composable
private fun PasscodeDot(
    filled: Boolean,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(
                if (filled) currentTheme.getAccent(isDarkMode)
                else currentTheme.getBorder(isDarkMode)
            )
    )
}

@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        numbers.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    if (key.isNotEmpty()) {
                        NumberKey(
                            value = key,
                            onClick = {
                                if (key == "⌫") {
                                    onBackspace()
                                } else {
                                    onNumberClick(key)
                                }
                            },
                            currentTheme = currentTheme,
                            isDarkMode = isDarkMode
                        )
                    } else {
                        // Empty space
                        Spacer(modifier = Modifier.size(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberKey(
    value: String,
    onClick: () -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(currentTheme.getSurface(isDarkMode))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (value == "⌫") {
            Icon(
                imageVector = FeatherIcons.Delete,
                contentDescription = "Backspace",
                tint = currentTheme.getText(isDarkMode),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = currentTheme.getText(isDarkMode)
            )
        }
    }
}

enum class PasscodeMode {
    ENTER,  // Unlock app
    SET,    // First time setup
    CHANGE  // Change existing passcode
}
