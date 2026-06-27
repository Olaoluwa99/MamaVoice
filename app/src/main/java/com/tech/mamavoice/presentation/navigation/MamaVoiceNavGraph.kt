package com.tech.mamavoice.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tech.mamavoice.presentation.auth.LoginScreen
import com.tech.mamavoice.presentation.auth.SignUpScreen
import com.tech.mamavoice.presentation.language.LanguageSelectionScreen
import com.tech.mamavoice.presentation.main.MainScreen
import com.tech.mamavoice.presentation.profile.ProfileSetupScreen
import com.tech.mamavoice.presentation.splash.SplashScreen
import com.tech.mamavoice.presentation.welcome.WelcomeScreen

/**
 * Main Navigation Graph for MamaVoice.
 */
@Composable
fun MamaVoiceNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onContinue = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.LanguageSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onAuthSuccess = { isProfileCompleted ->
                    val destination = if (isProfileCompleted) Screen.Main.route else Screen.ProfileSetup.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNeedsVerification = { email, otpId ->
                    navController.navigate(Screen.Otp.createRoute(email, otpId)) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onAuthSuccess = { email, otpId ->
                    navController.navigate(Screen.Otp.createRoute(email, otpId)) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            com.tech.mamavoice.presentation.auth.ForgotPasswordScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.Otp.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val otpId = backStackEntry.arguments?.getString("otpId") ?: ""
            com.tech.mamavoice.presentation.auth.OtpScreen(
                email = email,
                otpId = otpId,
                onBack = { navController.navigateUp() },
                onSuccess = { isProfileCompleted ->
                    val destination = if (isProfileCompleted) Screen.Main.route else Screen.ProfileSetup.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Otp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Main App Flow (bottom-nav host: Home · Food · Speak · Vaccines · Health) ---
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Profile.route) {
            com.tech.mamavoice.presentation.profile.ProfileScreen(
                onBackClick = { navController.navigateUp() },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
