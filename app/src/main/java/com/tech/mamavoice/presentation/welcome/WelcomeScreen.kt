package com.tech.mamavoice.presentation.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tech.mamavoice.R

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.mama_voice_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(140.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to MamaVoice",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your maternal health companion.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Log In", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Create an Account", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
