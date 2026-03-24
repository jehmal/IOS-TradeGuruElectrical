package com.tradeguru.electrical.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tradeguru.electrical.data.PreferencesManager
import com.tradeguru.electrical.data.db.TradeGuruDatabase
import com.tradeguru.electrical.services.APIConfig
import com.tradeguru.electrical.services.AuthManager
import com.tradeguru.electrical.services.DeviceManager
import com.tradeguru.electrical.services.KeychainHelper
import com.tradeguru.electrical.viewmodels.ChatEngine
import com.tradeguru.electrical.viewmodels.ChatViewModel
import com.tradeguru.electrical.viewmodels.ConversationManager

class AppModule(private val context: Context) {
    val database: TradeGuruDatabase by lazy { TradeGuruDatabase.getInstance(context) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(context) }
    val keychainHelper: KeychainHelper by lazy { KeychainHelper(context) }
    val deviceManager: DeviceManager by lazy { DeviceManager(keychainHelper) }
    val authManager: AuthManager by lazy { AuthManager(keychainHelper, APIConfig) }
    val conversationManager: ConversationManager by lazy { ConversationManager(database) }

    fun createChatEngine(): ChatEngine {
        return ChatEngine(deviceManager, authManager)
    }
}

class ChatViewModelFactory(private val appModule: AppModule) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(
            engine = appModule.createChatEngine(),
            conversationManager = appModule.conversationManager
        ) as T
    }
}
