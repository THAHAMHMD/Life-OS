package com.bloomhaven.app.feature.eira

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.DateUtils
import androidx.compose.foundation.layout.height

@Composable
fun EiraScreen(viewModel: EiraViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()

    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp),
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                androidx.compose.foundation.layout.Spacer(Modifier.height(10.dp))
                Text("Eira", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "A gentle presence — a greeting when you arrive, never a demand for your attention.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (messages.isEmpty()) {
            item { EmptyState(title = "Eira is quiet for now", message = "Open the app again a little later and she'll say hello.") }
        } else {
            items(messages, key = { it.id }) { message ->
                BloomCard {
                    Text(message.text, style = MaterialTheme.typography.bodyLarge)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))
                    Text(
                        DateUtils.friendlyDate(message.timestamp.toLocalDate()) + " · " + DateUtils.friendlyTime(message.timestamp.toLocalTime()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
