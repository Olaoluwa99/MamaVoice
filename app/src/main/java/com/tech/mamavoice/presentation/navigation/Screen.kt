package com.tech.mamavoice.presentation.navigation

/**
 * Sealed class representing all navigation routes in the app.
 * Using sealed class for type safety and exhaustive when-expressions.
 */
sealed class Screen(val route: String) {

    // Auth flow
    data object Splash : Screen("splash")
    data object LanguageSelection : Screen("language_selection")
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object SignUp : Screen("sign_up")
    data object ForgotPassword : Screen("forgot_password")
    data object Otp : Screen("otp/{email}/{otpId}") {
        fun createRoute(email: String, otpId: String) = "otp/$email/$otpId"
    }
    data object ProfileSetup : Screen("profile_setup")

    // Main app flow — the bottom-nav host (Home · Food · Speak · Vaccines · Health)
    data object Main : Screen("main")
    data object Profile : Screen("profile_screen")

    /** Full-screen AI voice/text conversation. Optional [ARG_QUERY] pre-submits a text question. */
    data object Conversation : Screen("conversation?query={query}") {
        const val ARG_QUERY = "query"
        fun createRoute(query: String? = null): String =
            if (query.isNullOrBlank()) "conversation"
            else "conversation?query=${android.net.Uri.encode(query)}"
    }

    // Individual feature destinations (also reachable inside the Main host's tabs)
    data object FoodDirectory : Screen("food_directory_screen")
    data object ImmunizationTimeline : Screen("immunization_timeline")
    data object HealthTracker : Screen("health_tracker")
}

/**
 * Tabs hosted inside the [Screen.Main] bottom-navigation scaffold.
 * "Speak" is an action (opens the voice overlay), not a navigable destination.
 */
enum class MainTab {
    HOME, FOOD, VACCINES, HEALTH
}
