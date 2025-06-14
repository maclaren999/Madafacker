package com.bbuddies.madafaker.common_domain.utils

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityMonitor {
    val isConnected: Flow<Boolean>
    fun isCurrentlyConnected(): Boolean
}