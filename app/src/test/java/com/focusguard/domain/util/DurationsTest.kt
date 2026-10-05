package com.focusguard.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationsTest {

    @Test
    fun `formatDuration picks the largest useful unit`() {
        assertEquals("0s", formatDuration(0))
        assertEquals("45s", formatDuration(45_000))
        assertEquals("12m", formatDuration(12 * 60_000L + 30_000))
        assertEquals("1h 05m", formatDuration(65 * 60_000L))
        assertEquals("0s", formatDuration(-5_000))
    }

    @Test
    fun `formatClock shows a stopwatch`() {
        assertEquals("00:00", formatClock(0))
        assertEquals("02:03", formatClock(123_000))
        assertEquals("1:02:03", formatClock(3_723_000))
    }
}
