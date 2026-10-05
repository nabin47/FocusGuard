package com.focusguard.domain.usecase

import com.focusguard.domain.model.DailyFocus
import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.model.FocusStats
import com.focusguard.domain.repository.SessionHistoryRepository
import com.focusguard.domain.util.TimeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetFocusStatsUseCase @Inject constructor(
    private val sessionHistoryRepository: SessionHistoryRepository,
    private val timeProvider: TimeProvider
) {
    /** Stats are recomputed whenever sessions change and every [refreshMillis] so a running session counts. */
    operator fun invoke(refreshMillis: Long = 30_000L): Flow<FocusStats> =
        combine(sessionHistoryRepository.getSessions(), ticker(refreshMillis)) { sessions, _ ->
            computeStats(sessions, timeProvider.now(), timeProvider.zone())
        }

    private fun ticker(intervalMillis: Long): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(intervalMillis)
        }
    }

    companion object {
        private const val RECENT_SESSIONS = 20

        /** Sessions are attributed to the day they started on. */
        fun computeStats(sessions: List<FocusSession>, now: Long, zone: ZoneId): FocusStats {
            val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
            val byDay = sessions.groupBy { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() }
            fun focusOn(day: LocalDate) = byDay[day].orEmpty().sumOf { it.durationMillis(now) }

            val todaySessions = byDay[today].orEmpty()
            val last7Days = (6 downTo 0).map { offset ->
                val day = today.minusDays(offset.toLong())
                DailyFocus(day, focusOn(day))
            }

            // A streak may still be alive if today has no session yet.
            var day = if (byDay.containsKey(today)) today else today.minusDays(1)
            var streak = 0
            while (byDay.containsKey(day)) {
                streak++
                day = day.minusDays(1)
            }

            return FocusStats(
                todayFocusMillis = todaySessions.sumOf { it.durationMillis(now) },
                todaySessions = todaySessions.size,
                todayBlockedAttempts = todaySessions.sumOf { it.blockedAttempts },
                weekFocusMillis = last7Days.sumOf { it.focusMillis },
                totalFocusMillis = sessions.sumOf { it.durationMillis(now) },
                totalSessions = sessions.size,
                currentStreakDays = streak,
                last7Days = last7Days,
                recentSessions = sessions.sortedByDescending { it.startedAt }.take(RECENT_SESSIONS)
            )
        }
    }
}
