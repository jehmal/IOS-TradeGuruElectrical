package com.tradeguru.electrical

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.tradeguru.electrical.navigation.NavGraph
import com.tradeguru.electrical.ui.theme.TradeGuruTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appModule = (application as TradeGuruApp).appModule

        setContent {
            TradeGuruTheme {
                NavGraph(appModule = appModule)
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme == "tradeguru" && uri.host == "auth-callback") {
            val appModule = (application as TradeGuruApp).appModule
            lifecycleScope.launch {
                appModule.authManager.handleAuthCallback(uri)
            }
        }
    }
}
