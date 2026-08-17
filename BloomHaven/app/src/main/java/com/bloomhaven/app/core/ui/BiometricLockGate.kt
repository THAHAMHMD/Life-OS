package com.bloomhaven.app.core.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Wraps app content behind a biometric prompt when the user has enabled the security
 * setting in My Haven. If the device has no usable biometric/credential enrolled, we
 * unlock automatically rather than lock the user out of their own data.
 */
@Composable
fun BiometricLockGate(enabled: Boolean, content: @Composable () -> Unit) {
    if (!enabled) {
        content()
        return
    }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var unlocked by remember { mutableStateOf(false) }

    val canAuthenticate = remember {
        val manager = BiometricManager.from(context)
        manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    LaunchedEffect(Unit) {
        if (!canAuthenticate || activity == null) {
            unlocked = true
        }
    }

    if (unlocked) {
        content()
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(48.dp),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Text("Bloom Haven is locked", style = MaterialTheme.typography.titleLarge)
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            Text(
                "Unlock with your fingerprint, face, or device passcode to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))
            BloomPrimaryButton(text = "Unlock", onClick = {
                if (activity != null) {
                    promptBiometric(activity, onSuccess = { unlocked = true })
                }
            })
        }
    }

    LaunchedEffect(activity) {
        if (activity != null && canAuthenticate) {
            promptBiometric(activity, onSuccess = { unlocked = true })
        }
    }
}

private fun promptBiometric(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Bloom Haven")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )
        .build()
    prompt.authenticate(info)
}
