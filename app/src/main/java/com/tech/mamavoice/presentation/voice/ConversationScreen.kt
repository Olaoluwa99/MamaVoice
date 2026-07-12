package com.tech.mamavoice.presentation.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import android.text.format.DateUtils
import com.tech.mamavoice.R
import com.tech.mamavoice.data.remote.dto.ConversationSummaryDto
import com.tech.mamavoice.ui.theme.MamaTheme
import kotlinx.coroutines.launch

/**
 * Full-screen AI health conversation. Voice-first: record → upload → play native-language reply,
 * with a text fallback. Language is driven by the user's profile server-side (no in-chat selector).
 */
@Composable
fun ConversationScreen(
    onClose: () -> Unit,
    initialQuery: String? = null,
    viewModel: VoiceConversationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val amplitudes by viewModel.amplitudes.collectAsState()
    val playingId by viewModel.playingMessageId.collectAsState()
    val history by viewModel.history.collectAsState()
    val activeId by viewModel.conversationId.collectAsState()

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.startRecording() }

    fun recordWithPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startRecording()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Submit an initial query passed from a Home suggestion chip, exactly once.
    var initialHandled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!initialHandled && !initialQuery.isNullOrBlank()) {
            initialHandled = true
            viewModel.submitText(initialQuery)
        }
    }

    // Load the first page of history the first time the drawer is opened.
    LaunchedEffect(Unit) {
        snapshotFlow { drawerState.isOpen }.collect { open ->
            val h = viewModel.history.value
            if (open && h.items.isEmpty() && !h.isLoading) {
                viewModel.loadConversations(refresh = true)
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HistoryDrawer(
                history = history,
                activeId = activeId,
                onNewChat = {
                    scope.launch { drawerState.close() }
                    viewModel.startNewConversation()
                },
                onOpen = { id ->
                    scope.launch { drawerState.close() }
                    viewModel.openConversation(id)
                },
                onDelete = viewModel::deleteConversation,
                onLoadMore = { viewModel.loadConversations() }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                // Pad the bottom once, by whichever is larger: the keyboard or the nav bar.
                // The IME inset already spans the nav-bar region when the keyboard is open, so
                // taking the union avoids stacking nav-bar padding on top of the keyboard height.
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
        ) {
            // Top bar: history · status pill · close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.cd_history), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.weight(1f))
                StatusPill(state)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        viewModel.cancelRecording()
                        viewModel.stopAudio()
                        onClose()
                    }
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Conversation
            if (messages.isEmpty()) {
                EmptyConversation(
                    modifier = Modifier.weight(1f),
                    onSuggestion = { viewModel.submitText(it) }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message,
                            isPlaying = playingId == message.id,
                            onReplay = { viewModel.playMessage(message) },
                            onStop = { viewModel.stopAudio() },
                            onToggleEnglish = { viewModel.toggleEnglish(message.id) }
                        )
                    }
                }
            }

            // Bottom controls
            if (state == VoiceState.RECORDING) {
                RecordingControls(
                    amplitudes = amplitudes,
                    onStop = { viewModel.stopRecordingAndSend() }
                )
            } else {
                InputControls(
                    draft = draft,
                    isProcessing = state == VoiceState.PROCESSING,
                    onDraftChange = viewModel::updateDraft,
                    onSend = { viewModel.submitText(draft) },
                    onMic = { recordWithPermission() }
                )
            }
        }
    }
}

/** History drawer: a "new chat" action plus the paginated list of past conversations. */
@Composable
private fun HistoryDrawer(
    history: ConversationHistoryUiState,
    activeId: String?,
    onNewChat: () -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    ModalDrawerSheet {
        val listState = rememberLazyListState()

        // Load the next page when the user nears the end of the loaded rows.
        LaunchedEffect(listState) {
            snapshotFlow {
                val info = listState.layoutInfo
                (info.visibleItemsInfo.lastOrNull()?.index ?: -1) to info.totalItemsCount
            }.collect { (lastVisible, total) ->
                if (total > 0 && lastVisible >= total - 3) onLoadMore()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNewChat) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.history_new_chat), tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (history.items.isEmpty() && !history.isLoading) {
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(history.items, key = { it.id }) { item ->
                    ConversationRow(
                        item = item,
                        selected = item.id == activeId,
                        onClick = { onOpen(item.id) },
                        onDelete = { onDelete(item.id) }
                    )
                }
                if (history.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    item: ConversationSummaryDto,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title?.takeIf { it.isNotBlank() } ?: stringResource(R.string.history_untitled),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val time = formatRelativeTime(item.lastMessageAt ?: item.updatedAt)
            if (time.isNotBlank()) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

/** Formats an ISO-8601 timestamp as a localized relative time (e.g. "2 days ago"); "" if unparseable. */
private fun formatRelativeTime(iso: String?): String {
    val millis = parseIsoMillis(iso) ?: return ""
    return DateUtils.getRelativeTimeSpanString(
        millis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
    ).toString()
}

private fun parseIsoMillis(iso: String?): Long? {
    if (iso.isNullOrBlank()) return null
    // SimpleDateFormat rather than java.time — minSdk 24 has no core-library desugaring.
    val patterns = arrayOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
    for (pattern in patterns) {
        try {
            val format = java.text.SimpleDateFormat(pattern, java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            return format.parse(iso)?.time
        } catch (_: Exception) { /* try the next pattern */ }
    }
    return null
}

@Composable
private fun StatusPill(state: VoiceState) {
    val text = when (state) {
        VoiceState.RECORDING -> stringResource(R.string.voice_status_listening)
        VoiceState.PROCESSING -> stringResource(R.string.chat_thinking)
        VoiceState.PLAYING -> stringResource(R.string.voice_status_speaking)
        VoiceState.IDLE -> stringResource(R.string.voice_status_prompt)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyConversation(modifier: Modifier = Modifier, onSuggestion: (String) -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MamaTheme.colors.successContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.voice_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.voice_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SuggestionChip(stringResource(R.string.suggestion_foods_help), onSuggestion)
            SuggestionChip(stringResource(R.string.suggestion_signs_labour), onSuggestion)
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        // The label carries display quotes (e.g. "What foods help?"); send the query without them.
        modifier = Modifier.clickable { onClick(text.trim('"')) }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isPlaying: Boolean,
    onReplay: () -> Unit,
    onStop: () -> Unit,
    onToggleEnglish: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        when {
            message.isUser -> UserBubble(message.text.orEmpty())
            message.isLoading -> LoadingBubble()
            message.isError -> ErrorBubble(message.text ?: stringResource(R.string.voice_error_generic))
            else -> AssistantBubble(message, isPlaying, onReplay, onStop, onToggleEnglish)
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
        modifier = Modifier.widthIn(max = 300.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
    }
}

@Composable
private fun LoadingBubble() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(stringResource(R.string.chat_thinking), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorBubble(text: String) {
    Surface(
        color = MamaTheme.colors.accentContainer,
        contentColor = MamaTheme.colors.onAccentContainer,
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
    }
}

@Composable
private fun AssistantBubble(
    message: ChatMessage,
    isPlaying: Boolean,
    onReplay: () -> Unit,
    onStop: () -> Unit,
    onToggleEnglish: () -> Unit
) {
    val danger = message.isDangerSign || message.riskLevel.equals("HIGH", ignoreCase = true)
    val container = if (danger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val onContainer = if (danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface

    Surface(
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
        modifier = Modifier.fillMaxWidth(0.88f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (danger) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.cd_warning), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.voice_danger_label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }

            Text(text = message.displayText, style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!message.audioUrl.isNullOrBlank()) {
                    if (isPlaying) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onStop)
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = stringResource(R.string.cd_stop), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            SpeakingWaveform(modifier = Modifier.width(40.dp), color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.voice_tap_to_stop), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onReplay)
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.voice_replay), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (message.hasEnglishAlternative) {
                    Text(
                        text = if (message.showEnglish) stringResource(R.string.voice_show_original) else stringResource(R.string.voice_show_english),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onToggleEnglish)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingControls(amplitudes: List<Float>, onStop: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VoiceWaveform(
            levels = amplitudes,
            color = MaterialTheme.colorScheme.primary,
            height = 44.dp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onStop),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Stop, contentDescription = stringResource(R.string.cd_stop), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(stringResource(R.string.voice_tap_to_stop), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InputControls(
    draft: String,
    isProcessing: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            placeholder = { Text(stringResource(R.string.voice_type_placeholder)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            maxLines = 3,
            enabled = !isProcessing,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = !isProcessing) { if (draft.isNotBlank()) onSend() else onMic() },
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(
                    imageVector = if (draft.isNotBlank()) Icons.Filled.Send else Icons.Filled.Mic,
                    contentDescription = if (draft.isNotBlank()) stringResource(R.string.cd_send) else stringResource(R.string.cd_microphone),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
