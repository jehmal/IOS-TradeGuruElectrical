package com.tradeguru.electrical

import android.app.Application
import com.tradeguru.electrical.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TradeGuruApp : Application() {

    lateinit var appModule: AppModule
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this
        appModule = AppModule(this)

        applicationScope.launch {
            appModule.authManager.restoreSession()
        }
    }

    companion object {
        @Volatile
        private lateinit var instance: TradeGuruApp

        fun getInstance(): TradeGuruApp = instance
    }
}
