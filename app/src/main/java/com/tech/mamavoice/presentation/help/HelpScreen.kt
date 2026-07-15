package com.tech.mamavoice.presentation.help

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tech.mamavoice.R
import com.tech.mamavoice.presentation.components.MamaVoiceLogoMark

/**
 * Static "how to reach us" page opened from Profile → Help.
 *
 * Placeholder contact details live in [Contacts] — swap them for the real handles when they exist.
 * Each row fires the matching intent (mail composer / browser / WhatsApp); if no app can handle it
 * we surface a short toast instead of crashing.
 */
private object Contacts {
    const val EMAIL = "hello@mamavoice.app"
    const val WHATSAPP_DISPLAY = "+234 800 000 0000"
    const val WHATSAPP_URL = "https://wa.me/2348000000000"
    const val INSTAGRAM_HANDLE = "@mamavoice"
    const val INSTAGRAM_URL = "https://instagram.com/mamavoice"
    const val X_HANDLE = "@mamavoice"
    const val X_URL = "https://x.com/mamavoice"
    const val FACEBOOK_HANDLE = "MamaVoice"
    const val FACEBOOK_URL = "https://facebook.com/mamavoice"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    fun launch(intent: Intent) {
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, it.localizedMessage ?: "No app found", Toast.LENGTH_SHORT).show()
            }
    }

    fun openUrl(url: String) = launch(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    fun sendEmail() = launch(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${Contacts.EMAIL}")))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.help_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Friendly header
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                MamaVoiceLogoMark(containerSize = 72.dp, logoSize = 56.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.help_header),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.help_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionLabel(stringResource(R.string.help_section_contact))
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ContactRow(
                        icon = Icons.Filled.Email,
                        title = stringResource(R.string.help_email_label),
                        value = Contacts.EMAIL,
                        onClick = ::sendEmail
                    )
                    ContactDivider()
                    ContactRow(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        title = stringResource(R.string.help_whatsapp_label),
                        value = Contacts.WHATSAPP_DISPLAY,
                        onClick = { openUrl(Contacts.WHATSAPP_URL) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel(stringResource(R.string.help_section_social))
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ContactRow(
                        icon = Icons.Filled.PhotoCamera,
                        title = "Instagram",
                        value = Contacts.INSTAGRAM_HANDLE,
                        onClick = { openUrl(Contacts.INSTAGRAM_URL) }
                    )
                    ContactDivider()
                    ContactRow(
                        icon = Icons.Filled.AlternateEmail,
                        title = "X (Twitter)",
                        value = Contacts.X_HANDLE,
                        onClick = { openUrl(Contacts.X_URL) }
                    )
                    ContactDivider()
                    ContactRow(
                        icon = Icons.Filled.Groups,
                        title = "Facebook",
                        value = Contacts.FACEBOOK_HANDLE,
                        onClick = { openUrl(Contacts.FACEBOOK_URL) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.help_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.5.sp
    )
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.OpenInNew,
            contentDescription = stringResource(R.string.cd_help_open),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ContactDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )
}
