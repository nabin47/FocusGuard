package com.focusguard.domain.usecase

import com.focusguard.domain.model.FocusSession
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class GetFocusStatsUseCaseTest {

    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 10, 5)
    private val minute = 60_000L

    private fun at(day: LocalDate, hour: Int) = day.atTime(hour, 0).toInstant(zone).toEpochMilli()

    private fun session(day: LocalDate, minutes: Long, blocked: Int = 0): FocusSession {
        val start = at(day, 10)
        return FocusSession(startedAt = start, endedAt = start + minutes * minute, blockedAttempts = blocked)
    }

    @Test
    fun `no sessions gives empty stats`() {
        val stats = GetFocusStatsUseCase.computeStats(emptyList(), at(today, 12), zone)
        assertEquals(0L, stats.todayFocusMillis)
        assertEquals(0, stats.currentStreakDays)
        assertEquals(7, stats.last7Days.size)
        assertEquals(today, stats.last7Days.last().date)
    }

    @Test
    fun `today, week and totals are summed per start day`() {
        val sessions = listOf(
            session(today, 30, blocked = 2),
            session(today, 15, blocked = 1),
            session(today.minusDays(2), 60),
            session(today.minusDays(10), 120)
        )
        val stats = GetFocusStatsUseCase.computeStats(sessions, at(today, 18), zone)

        assertEquals(45 * minute, stats.todayFocusMillis)
        assertEquals(2, stats.todaySessions)
        assertEquals(3, stats.todayBlockedAttempts)
        assertEquals(105 * minute, stats.weekFocusMillis)
        assertEquals(225 * minute, stats.totalFocusMillis)
        assertEquals(4, stats.totalSessions)
        assertEquals(60 * minute, stats.last7Days[4].focusMillis)
    }

    @Test
    fun `running session counts up to now`() {
        val start = at(today, 9)
        val stats = GetFocusStatsUseCase.computeStats(
            listOf(FocusSession(startedAt = start)),
            now = start + 20 * minute,
            zone = zone
        )
        assertEquals(20 * minute, stats.todayFocusMillis)
    }

    @Test
    fun `streak counts consecutive days and survives a day without a session yet`() {
        val sessions = listOf(
            session(today.minusDays(1), 10),
            session(today.minusDays(2), 10),
            session(today.minusDays(3), 10),
            session(today.minusDays(5), 10)
        )
        assertEquals(3, GetFocusStatsUseCase.computeStats(sessions, at(today, 8), zone).currentStreakDays)

        val withToday = sessions + session(today, 5)
        assertEquals(4, GetFocusStatsUseCase.computeStats(withToday, at(today, 18), zone).currentStreakDays)
    }
}
