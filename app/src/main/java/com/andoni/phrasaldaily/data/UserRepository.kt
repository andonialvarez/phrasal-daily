package com.andoni.phrasaldaily.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

enum class ContentMode { PHRASAL, FALSE_FRIEND, BOTH }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserData(
    val onboarded: Boolean = false,
    val name: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val contentMode: ContentMode = ContentMode.PHRASAL,
    /** Days on which the user swapped the single daily card to the other type. */
    val swappedDays: Set<LocalDate> = emptySet(),
    val visits: Set<LocalDate> = emptySet(),
    val learned: Set<String> = emptySet(),
    val favorites: Set<String> = emptySet(),
    val seen: Set<String> = emptySet(),
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val speechRate: Float = 0.9f,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    /** Achievement id -> date unlocked. */
    val achievements: Map<String, LocalDate> = emptyMap(),
    val quizzesDone: Int = 0,
    val perfectQuizzes: Int = 0,
    val quizCorrectTotal: Int = 0,
    val lastPhraseIndex: Int = -1,
) {
    fun dayIndex(today: LocalDate = LocalDate.now()): Long =
        ChronoUnit.DAYS.between(startDate, today).coerceAtLeast(0)
}

class UserRepository(private val context: Context) {

    private object Keys {
        val onboarded = booleanPreferencesKey("onboarded")
        val name = stringPreferencesKey("name")
        val startDate = stringPreferencesKey("start_date")
        val contentMode = stringPreferencesKey("content_mode")
        val swappedDays = stringSetPreferencesKey("swapped_days")
        val visits = stringSetPreferencesKey("visits")
        val learned = stringSetPreferencesKey("learned")
        val favorites = stringSetPreferencesKey("favorites")
        val seen = stringSetPreferencesKey("seen")
        val notifEnabled = booleanPreferencesKey("notif_enabled")
        val notifHour = intPreferencesKey("notif_hour")
        val notifMinute = intPreferencesKey("notif_minute")
        val speechRate = floatPreferencesKey("speech_rate")
        val theme = stringPreferencesKey("theme")
        val achievements = stringSetPreferencesKey("achievements")
        val quizzesDone = intPreferencesKey("quizzes_done")
        val perfectQuizzes = intPreferencesKey("perfect_quizzes")
        val quizCorrectTotal = intPreferencesKey("quiz_correct_total")
        val lastPhrase = intPreferencesKey("last_phrase")
    }

    val data: Flow<UserData> = context.dataStore.data.map { p ->
        UserData(
            onboarded = p[Keys.onboarded] ?: false,
            name = p[Keys.name] ?: "",
            startDate = p[Keys.startDate]?.let(::parseDate) ?: LocalDate.now(),
            contentMode = p[Keys.contentMode]?.let { runCatching { ContentMode.valueOf(it) }.getOrNull() }
                ?: ContentMode.PHRASAL,
            swappedDays = p[Keys.swappedDays].orEmpty().mapNotNull(::parseDate).toSet(),
            visits = p[Keys.visits].orEmpty().mapNotNull(::parseDate).toSet(),
            learned = p[Keys.learned].orEmpty(),
            favorites = p[Keys.favorites].orEmpty(),
            seen = p[Keys.seen].orEmpty(),
            notificationsEnabled = p[Keys.notifEnabled] ?: true,
            reminderHour = p[Keys.notifHour] ?: 19,
            reminderMinute = p[Keys.notifMinute] ?: 0,
            speechRate = p[Keys.speechRate] ?: 0.9f,
            theme = p[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            achievements = p[Keys.achievements].orEmpty().mapNotNull { entry ->
                val parts = entry.split("|")
                val date = parts.getOrNull(1)?.let(::parseDate) ?: return@mapNotNull null
                parts[0] to date
            }.toMap(),
            quizzesDone = p[Keys.quizzesDone] ?: 0,
            perfectQuizzes = p[Keys.perfectQuizzes] ?: 0,
            quizCorrectTotal = p[Keys.quizCorrectTotal] ?: 0,
            lastPhraseIndex = p[Keys.lastPhrase] ?: -1,
        )
    }

    suspend fun current(): UserData = data.first()

    suspend fun completeOnboarding(name: String, mode: ContentMode, hour: Int, minute: Int, notifications: Boolean) {
        context.dataStore.edit { p ->
            p[Keys.name] = name.trim()
            p[Keys.contentMode] = mode.name
            p[Keys.notifHour] = hour
            p[Keys.notifMinute] = minute
            p[Keys.notifEnabled] = notifications
            if (p[Keys.startDate] == null) p[Keys.startDate] = LocalDate.now().toString()
            p[Keys.onboarded] = true
        }
    }

    suspend fun recordVisit(date: LocalDate = LocalDate.now()) {
        context.dataStore.edit { p ->
            p[Keys.visits] = p[Keys.visits].orEmpty() + date.toString()
            if (p[Keys.startDate] == null) p[Keys.startDate] = date.toString()
        }
    }

    suspend fun markSeen(ids: Collection<String>) {
        context.dataStore.edit { p -> p[Keys.seen] = p[Keys.seen].orEmpty() + ids }
    }

    suspend fun setLearned(id: String, learned: Boolean) {
        context.dataStore.edit { p ->
            val set = p[Keys.learned].orEmpty()
            p[Keys.learned] = if (learned) set + id else set - id
        }
    }

    suspend fun setFavorite(id: String, favorite: Boolean) {
        context.dataStore.edit { p ->
            val set = p[Keys.favorites].orEmpty()
            p[Keys.favorites] = if (favorite) set + id else set - id
        }
    }

    suspend fun toggleSwap(date: LocalDate = LocalDate.now()) {
        context.dataStore.edit { p ->
            val set = p[Keys.swappedDays].orEmpty()
            val key = date.toString()
            p[Keys.swappedDays] = if (key in set) set - key else set + key
        }
    }

    suspend fun setName(name: String) = context.dataStore.edit { it[Keys.name] = name.trim() }
    suspend fun setContentMode(mode: ContentMode) = context.dataStore.edit { it[Keys.contentMode] = mode.name }
    suspend fun setNotificationsEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.notifEnabled] = enabled }
    suspend fun setReminderTime(hour: Int, minute: Int) = context.dataStore.edit {
        it[Keys.notifHour] = hour
        it[Keys.notifMinute] = minute
    }
    suspend fun setSpeechRate(rate: Float) = context.dataStore.edit { it[Keys.speechRate] = rate }
    suspend fun setTheme(theme: ThemeMode) = context.dataStore.edit { it[Keys.theme] = theme.name }
    suspend fun setLastPhraseIndex(index: Int) = context.dataStore.edit { it[Keys.lastPhrase] = index }

    suspend fun recordQuiz(correct: Int, total: Int) {
        context.dataStore.edit { p ->
            p[Keys.quizzesDone] = (p[Keys.quizzesDone] ?: 0) + 1
            p[Keys.quizCorrectTotal] = (p[Keys.quizCorrectTotal] ?: 0) + correct
            if (total > 0 && correct == total) p[Keys.perfectQuizzes] = (p[Keys.perfectQuizzes] ?: 0) + 1
        }
    }

    suspend fun unlockAchievements(ids: Collection<String>, date: LocalDate = LocalDate.now()) {
        if (ids.isEmpty()) return
        context.dataStore.edit { p ->
            p[Keys.achievements] = p[Keys.achievements].orEmpty() + ids.map { "$it|$date" }
        }
    }

    /** Wipes progress but keeps the profile and settings. */
    suspend fun resetProgress() {
        context.dataStore.edit { p ->
            listOf(
                Keys.swappedDays, Keys.visits, Keys.learned, Keys.favorites, Keys.seen, Keys.achievements,
            ).forEach { p.remove(it) }
            p.remove(Keys.quizzesDone)
            p.remove(Keys.perfectQuizzes)
            p.remove(Keys.quizCorrectTotal)
            p[Keys.startDate] = LocalDate.now().toString()
        }
    }

    companion object {
        @Volatile private var instance: UserRepository? = null
        fun get(context: Context): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(context.applicationContext).also { instance = it }
            }
    }
}

private fun parseDate(s: String): LocalDate? = runCatching { LocalDate.parse(s) }.getOrNull()
