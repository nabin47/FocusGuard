package com.focusguard.domain.model

data class BlockedApp(
    val packageName: String,
    val appName: String,
    val addedAt: Long = System.currentTimeMillis()
)
