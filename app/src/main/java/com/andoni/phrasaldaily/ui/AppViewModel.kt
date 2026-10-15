package com.andoni.phrasaldaily.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andoni.phrasaldaily.data.ContentMode
import com.andoni.phrasaldaily.data.ContentRepository
import com.andoni.phrasaldaily.data.FalseFriend
import com.andoni.phrasaldaily.data.ItemType
import com.andoni.phrasaldaily.data.PhrasalVerb
import com.andoni.phrasaldaily.data.StudyItem
import com.andoni.phrasaldaily.data.ThemeMode
import com.andoni.phrasaldaily.data.UserData
import com.andoni.phrasaldaily.data.UserRepository
import com.andoni.phrasaldaily.domain.Achievements
import com.andoni.phrasaldaily.domain.StatsCalculator
import com.andoni.phrasaldaily.notify.ReminderReceiver
import com.andoni.phrasaldaily.notify.ReminderScheduler
import com.andoni.phrasaldaily.tts.Speaker
import com.andoni.phrasaldaily.update.UpdateInfo
import com.andoni.phrasaldaily.update.Updater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class QuizQuestion(
    val item: StudyItem,
    val prompt: String,
    val hint: String,
    val options: List<String>,
    val correct: Int,
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    val content = ContentRepository.get(app)
    private val users = UserRepository.get(app)
    val speaker = Speaker(app)

    val user: StateFlow<UserData?> = users.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _newAchievements = MutableStateFlow<List<String>>(emptyList())
    val newAchievements: StateFlow<List<String>> = _newAchievements.asStateFlow()

    /** Newer release found on GitHub, shown as a dialog. */
    private val _update = MutableStateFlow<UpdateInfo?>(null)
    val update: StateFlow<UpdateInfo?> = _update.asStateFlow()

    /** Download progress 0..1 while an update downloads, null otherwise. */
    private val _updateProgress = MutableStateFlow<Float?>(null)
    val updateProgress: StateFlow<Float?> = _updateProgress.asStateFlow()

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    private var lastUpdateCheck = 0L

    fun checkForUpdates(manual: Boolean = false) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        if (!manual && now - lastUpdateCheck < 60 * 60 * 1000L) return@launch
        lastUpdateCheck = now
        if (manual) _updateMessage.value = "Buscando…"
        val found = Updater.check()
        if (found != null) {
            _update.value = found
            _updateMessage.value = null
        } else if (manual) {
            _updateMessage.value = "Ya tienes la última versión."
        }
    }

    fun startUpdate() = viewModelScope.launch {
        val info = _update.value ?: return@launch
        _updateProgress.value = 0f
        runCatching {
            val apk = Updater.download(getApplication(), info) { _updateProgress.value = it }
            Updater.install(getApplication(), apk)
        }.onFailure {
            _updateMessage.value = "No se pudo descargar la actualización. Revisa tu conexión."
        }
        _updateProgress.value = null
        _update.value = null
    }

    fun dismissUpdate() {
        _update.value = null
    }

    fun todayItems(user: UserData, today: LocalDate = LocalDate.now()): List<StudyItem> {
        val day = user.dayIndex(today)
        val pv = content.phrasalForDay(day)
        val ff = content.falseFriendForDay(day)
        val swapped = today in user.swappedDays
        return when (user.contentMode) {
            ContentMode.BOTH -> listOf(pv, ff)
            ContentMode.PHRASAL -> listOf(if (swapped) ff else pv)
            ContentMode.FALSE_FRIEND -> listOf(if (swapped) pv else ff)
        }
    }

    /** Called every time the app comes to the foreground. */
    fun onAppOpened() = viewModelScope.launch {
        checkForUpdates()
        val u = users.current()
        if (!u.onboarded) return@launch
        users.recordVisit()
        users.markSeen(todayItems(users.current()).map { it.id })
        checkAchievements()
        if (u.notificationsEnabled) ReminderScheduler.schedule(getApplication(), u.reminderHour, u.reminderMinute)
    }

    fun completeOnboarding(name: String, mode: ContentMode, hour: Int, minute: Int, notifications: Boolean) =
        viewModelScope.launch {
            users.completeOnboarding(name, mode, hour, minute, notifications)
            if (notifications) ReminderScheduler.schedule(getApplication(), hour, minute)
            onAppOpened()
        }

    private suspend fun checkAchievements() {
        val u = users.current()
        val stats = StatsCalculator.compute(u.visits)
        val earned = Achievements.newlyEarned(u, stats)
        if (earned.isNotEmpty()) {
            users.unlockAchievements(earned)
            _newAchievements.update { it + earned }
        }
    }

    fun dismissAchievements() = _newAchievements.update { emptyList() }

    fun speak(text: String) {
        speaker.speak(text, user.value?.speechRate ?: 0.9f)
    }

    fun setLearned(id: String, learned: Boolean) = viewModelScope.launch {
        users.setLearned(id, learned)
        checkAchievements()
    }

    fun setFavorite(id: String, favorite: Boolean) = viewModelScope.launch {
        users.setFavorite(id, favorite)
        checkAchievements()
    }

    fun swapToday() = viewModelScope.launch {
        users.toggleSwap()
        users.markSeen(todayItems(users.current()).map { it.id })
    }

    fun setName(name: String) = viewModelScope.launch { users.setName(name) }
    fun setContentMode(mode: ContentMode) = viewModelScope.launch {
        users.setContentMode(mode)
        users.markSeen(todayItems(users.current()).map { it.id })
    }
    fun setTheme(theme: ThemeMode) = viewModelScope.launch { users.setTheme(theme) }
    fun setSpeechRate(rate: Float) = viewModelScope.launch { users.setSpeechRate(rate) }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        users.setNotificationsEnabled(enabled)
        val u = users.current()
        if (enabled) ReminderScheduler.schedule(getApplication(), u.reminderHour, u.reminderMinute)
        else ReminderScheduler.cancel(getApplication())
    }

    fun setReminderTime(hour: Int, minute: Int) = viewModelScope.launch {
        users.setReminderTime(hour, minute)
        if (users.current().notificationsEnabled) ReminderScheduler.schedule(getApplication(), hour, minute)
    }

    fun sendTestNotification() = viewModelScope.launch {
        ReminderReceiver.remind(getApplication(), force = true)
    }

    fun resetProgress() = viewModelScope.launch {
        users.resetProgress()
        onAppOpened()
    }

    // ---- Quiz ----

    fun buildQuiz(user: UserData, size: Int = 6): List<QuizQuestion> {
        val pool = (user.seen.mapNotNull(content::byId) + todayItems(user)).distinctBy { it.id }
        if (pool.isEmpty()) return emptyList()
        // Prefer items not yet marked as learned, then mix in the rest.
        val ordered = pool.shuffled().sortedBy { it.id in user.learned }
        val questions = mutableListOf<QuizQuestion>()
        var round = 0
        while (questions.size < size && round < 2) {
            for (item in ordered) {
                if (questions.size >= size) break
                questions += makeQuestion(item, reverse = (round + questions.size) % 2 == 1)
            }
            round++
        }
        return questions.shuffled()
    }

    private fun makeQuestion(item: StudyItem, reverse: Boolean): QuizQuestion {
        val others: List<StudyItem> = when (item) {
            is PhrasalVerb -> content.phrasalVerbs
            is FalseFriend -> content.falseFriends
        }.filter { it.id != item.id }.shuffled().take(12)

        return if (!reverse) {
            val distractors = others.map { it.translation }.filter { it != item.translation }.distinct().take(3)
            val options = (distractors + item.translation).shuffled()
            val (prompt, hint) = when (item) {
                is FalseFriend -> "¿Qué significa «${item.headword}»?" to "Cuidado: no es «${item.looksLike}»."
                is PhrasalVerb -> "¿Qué significa «${item.headword}»?" to "Elige la traducción correcta."
            }
            QuizQuestion(item, prompt, hint, options, options.indexOf(item.translation))
        } else {
            val distractors = others.map { it.headword }.filter { it != item.headword }.distinct().take(3)
            val options = (distractors + item.headword).shuffled()
            val prompt = "¿Cómo se dice «${item.translation}» en inglés?"
            val hint = if (item.type == ItemType.PHRASAL) "Es un phrasal verb." else "Ojo con los falsos amigos."
            QuizQuestion(item, prompt, hint, options, options.indexOf(item.headword))
        }
    }

    fun finishQuiz(correct: Int, total: Int) = viewModelScope.launch {
        users.recordQuiz(correct, total)
        checkAchievements()
    }

    override fun onCleared() {
        speaker.shutdown()
        super.onCleared()
    }
}
