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
    data object ForgotPassword : Screen("forgot_password")
    data object Otp : Screen("otp/{email}/{otpId}") {
        fun createRoute(email: String, otpId: String) = "otp/$email/$otpId"
    }
    data object ProfileSetup : Screen("profile_setup")

    // Main app flow
    data object Dashboard : Screen("dashboard")
    data object Profile : Screen("profile_screen")
    data object FoodDirectory : Screen("food_directory_screen")
    data object ImmunizationTimeline : Screen("immunization_timeline")
    data object HealthTracker : Screen("health_tracker")
}
