package com.tradeguru.electrical.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tradeguru.electrical.di.AppModule
import com.tradeguru.electrical.di.ChatViewModelFactory
import com.tradeguru.electrical.models.AuthState
import com.tradeguru.electrical.ui.ChatScreen
import com.tradeguru.electrical.ui.views.legal.SafetyDisclaimerScreen
import com.tradeguru.electrical.ui.views.onboarding.OnboardingScreen
import com.tradeguru.electrical.ui.views.settings.SettingsScreen
import com.tradeguru.electrical.ui.views.settings.SignInView
import com.tradeguru.electrical.viewmodels.ChatViewModel
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

object Routes {
    const val ONBOARDING = "onboarding"
    const val DISCLAIMER = "disclaimer"
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val SIGN_IN = "signin"
}

@Composable
fun NavGraph(
    appModule: AppModule,
    navController: NavHostController = rememberNavController()
) {
    val hasCompletedOnboarding by appModule.preferencesManager.hasCompletedOnboarding
        .collectAsState(initial = null)
    val hasAcceptedDisclaimer by appModule.preferencesManager.hasAcceptedDisclaimer
        .collectAsState(initial = null)

    val onboardingDone = hasCompletedOnboarding
    val disclaimerDone = hasAcceptedDisclaimer

    if (onboardingDone == null || disclaimerDone == null) {
        return
    }

    val startDestination = when {
        !onboardingDone -> Routes.ONBOARDING
        !disclaimerDone -> Routes.DISCLAIMER
        else -> Routes.CHAT
    }

    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    scope.launch { appModule.preferencesManager.setOnboardingCompleted(true) }
                    navController.navigate(Routes.DISCLAIMER) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DISCLAIMER) {
            SafetyDisclaimerScreen(
                onAccept = {
                    scope.launch { appModule.preferencesManager.setDisclaimerAccepted(true) }
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.DISCLAIMER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CHAT) {
            val chatViewModel: ChatViewModel = viewModel(
                factory = ChatViewModelFactory(appModule)
            )
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            val context = LocalContext.current
            val authState by appModule.authManager.authState.collectAsState()
            val tier by appModule.authManager.tier.collectAsState()
            val isAuthenticated = authState is AuthState.Authenticated
            val currentUser = (authState as? AuthState.Authenticated)?.user

            val appVersion = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val version = pInfo.versionName ?: "1.0"
                val build = pInfo.longVersionCode
                "$version ($build)"
            } catch (_: PackageManager.NameNotFoundException) {
                "1.0 (1)"
            }

            SettingsScreen(
                isAuthenticated = isAuthenticated,
                currentUser = currentUser,
                tier = tier,
                appVersion = appVersion,
                onSignIn = { navController.navigate(Routes.SIGN_IN) },
                onSignOut = { appModule.authManager.signOut() },
                onClearConversations = {
                    scope.launch { appModule.conversationManager.clearAllConversations() }
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        composable(Routes.SIGN_IN) {
            val context = LocalContext.current
            SignInView(
                onGoogleSignIn = {
                    appModule.authManager.startSignIn(context, "GoogleOAuth")
                },
                onAppleSignIn = {
                    appModule.authManager.startSignIn(context, "AppleOAuth")
                },
                onEmailSignIn = {
                    appModule.authManager.startSignIn(context)
                },
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
