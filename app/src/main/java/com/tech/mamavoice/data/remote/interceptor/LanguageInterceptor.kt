package com.tech.mamavoice.data.remote.interceptor

import com.tech.mamavoice.data.local.SettingsManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Appends the user's selected app language as a `lang` query param on the localized read
 * endpoints, so the backend returns content in that language.
 *
 * `lang` is only added once the user has explicitly chosen a language; before then the request
 * omits it and the server falls back to the profile language (then English). Languages the API
 * doesn't localize into (e.g. Pidgin, see [com.tech.mamavoice.data.local.AppLanguage.apiLang])
 * are likewise omitted. An existing `lang` on the request is never overwritten.
 */
class LanguageInterceptor @Inject constructor(
    private val settingsManager: SettingsManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val path = request.url.encodedPath
        if (LOCALIZED_PATHS.none { path.endsWith(it) } || request.url.queryParameter("lang") != null) {
            return chain.proceed(request)
        }

        val apiLang = runBlocking {
            if (settingsManager.isLanguageSelected.firstOrNull() == true) {
                settingsManager.appLanguage.firstOrNull()?.apiLang
            } else {
                null
            }
        } ?: return chain.proceed(request)

        val url = request.url.newBuilder().addQueryParameter("lang", apiLang).build()
        return chain.proceed(request.newBuilder().url(url).build())
    }

    private companion object {
        val LOCALIZED_PATHS = listOf("api/dashboard", "api/foods", "api/vaccines")
    }
}
