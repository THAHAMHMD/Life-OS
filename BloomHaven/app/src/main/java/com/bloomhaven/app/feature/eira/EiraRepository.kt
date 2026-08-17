package com.bloomhaven.app.feature.eira

import com.bloomhaven.app.core.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class EiraRepository(private val dao: EiraDao) {
    fun recentMessages(): Flow<List<EiraMessageEntity>> = dao.recent()

    /**
     * Eira greets only when the app is opened, once per greeting window per day —
     * never repeatedly while the app stays open (spec 4.6).
     */
    suspend fun ensureGreetingForNow(context: EiraContext) {
        val now = LocalDateTime.now()
        val window = DateUtils.greetingWindow(now.toLocalTime())
        val latest = dao.latest()
        val alreadyGreetedThisWindow = latest != null &&
            latest.kind == EiraMessageKind.GREETING &&
            latest.timestamp.toLocalDate() == now.toLocalDate() &&
            DateUtils.greetingWindow(latest.timestamp.toLocalTime()) == window
        if (alreadyGreetedThisWindow) return

        val greetingText = EiraContent.greeting(window, now.toLocalDate(), context)
        dao.insert(EiraMessageEntity(timestamp = now, text = greetingText, kind = EiraMessageKind.GREETING))

        val quoteText = EiraContent.quote(now.toLocalDate())
        dao.insert(EiraMessageEntity(timestamp = now.plusSeconds(1), text = quoteText, kind = EiraMessageKind.QUOTE))
    }
}
