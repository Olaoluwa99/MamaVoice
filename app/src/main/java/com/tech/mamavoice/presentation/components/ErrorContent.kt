package com.tech.mamavoice.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tech.mamavoice.R
import com.tech.mamavoice.domain.util.AppError

/**
 * Resolves a typed [AppError] to friendly, localized copy. Server-supplied validation text is shown
 * verbatim (it's specific and safe); every other category maps to our own string resource so the
 * user never sees a raw "HTTP 401". Pass [fallback] for the rare legacy call site with only a
 * message string.
 */
@Composable
fun appErrorMessage(error: AppError?, fallback: String? = null): String = when (error) {
    AppError.Network -> stringResource(R.string.error_network)
    AppError.Timeout -> stringResource(R.string.error_timeout)
    AppError.Unauthorized -> stringResource(R.string.error_session_expired)
    AppError.Forbidden -> stringResource(R.string.error_forbidden)
    AppError.NotFound -> stringResource(R.string.error_not_found)
    AppError.Server -> stringResource(R.string.error_server)
    AppError.Validation -> stringResource(R.string.error_validation)
    is AppError.Message -> error.text
    AppError.Unknown, null -> fallback?.takeIf { it.isNotBlank() } ?: stringResource(R.string.error_unknown)
}

private fun AppError?.icon(): ImageVector = when (this) {
    AppError.Network -> Icons.Filled.CloudOff
    AppError.Timeout -> Icons.Filled.LockClock
    AppError.NotFound -> Icons.Filled.SearchOff
    else -> Icons.Filled.ErrorOutline
}

/**
 * Full-area empty/error state: an icon, a friendly message, and an optional "Try again" button.
 * Drop this in wherever a screen would otherwise print a raw error line.
 */
@Composable
fun ErrorState(
    error: AppError?,
    modifier: Modifier = Modifier,
    fallbackMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = error.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = appErrorMessage(error, fallbackMessage),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    stringResource(R.string.action_try_again),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
