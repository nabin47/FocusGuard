package com.focusguard.domain.util

import java.time.ZoneId

/** Abstraction over the system clock so time-based logic stays testable. */
interface TimeProvider {
    fun now(): Long
    fun zone(): ZoneId = ZoneId.systemDefault()
}

object SystemTimeProvider : TimeProvider {
    override fun now(): Long = System.currentTimeMillis()
}
