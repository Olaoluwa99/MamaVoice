package com.tech.mamavoice.presentation.navigation

/**
 * Sealed class representing all navigation routes in the app.
 * Using sealed class for type safety and exhaustive when-expressions.
 */
sealed class Screen(val route: String) {

    // Auth flow
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object SignUp : Screen("sign_up")
    data object Otp : Screen("otp/{email}") {
        fun createRoute(email: String) = "otp/$email"
    }
    data object ProfileSetup : Screen("profile_setup")

    // Main app flow
    data object Dashboard : Screen("dashboard")
    data object FoodDirectory : Screen("food_directory")
    data object ImmunizationTimeline : Screen("immunization_timeline")
    data object HealthTracker : Screen("health_tracker")
}
