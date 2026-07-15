package com.tech.mamavoice.presentation.language

import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.tech.mamavoice.data.local.AppLanguage
import java.util.Locale

/**
 * Applies [language] to the Compose tree by overriding [LocalContext] and [LocalConfiguration] with
 * a locale-adjusted context.
 *
 * `stringResource` resolves from `LocalContext.current.resources`, so the whole UI reads the chosen
 * language and re-reads whenever [language] changes — the text swaps in place with **no Activity
 * recreation**, which removes the window-recreation flash entirely (unlike
 * `AppCompatDelegate.setApplicationLocales`, which always recreates the Activity).
 *
 * The override *wraps* the Activity context rather than replacing it, so `LocalContext` still
 * unwraps to the Activity — `hiltViewModel()` and other APIs that resolve the Activity from
 * `LocalContext` keep working. The locale is layered onto the current [Configuration] (density,
 * size, dark mode, font scale preserved) and recomputed when the language or that config changes.
 */
@Composable
fun ProvideAppLocale(language: AppLanguage, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val (localizedContext, localizedConfiguration) = remember(language.code, configuration) {
        val locale = Locale.forLanguageTag(language.code)
        val localizedConfig = Configuration(configuration).apply { setLocale(locale) }
        val localizedResources = context.createConfigurationContext(localizedConfig).resources
        val wrapped = object : ContextWrapper(context) {
            override fun getResources(): Resources = localizedResources
        }
        wrapped to localizedConfig
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        content = content
    )
}
