## `app/src/main/java/com/mikeisesele/clearr/core/ai/ClearrEdgeAi.kt`

```kotlin
package com.mikeisesele.clearr.core.ai

import com.mikeisesele.clearr.ClearrApplication
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetStatus
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.derivedStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToLong
import org.json.JSONObject

data class TodoAiResult(
    val normalizedTitle: String,
    val normalizedNote: String?,
    val suggestedPriority: TodoPriority,
    val suggestedDueDate: LocalDate?
)

data class GoalAiResult(
    val normalizedTitle: String,
    val suggestedTarget: String?,
    val suggestedFrequency: GoalFrequency,
    val suggestedEmoji: String,
    val suggestedColorToken: String
)

data class SetupAiResult(
    val trackerName: String?,
    val trackerType: TrackerType?,
    val suggestedFrequency: Frequency?,
    val suggestedDefaultAmount: Double?
)

object ClearrEdgeAi {

    suspend fun inferTodoNanoAware(
        title: String,
        note: String?,
        selectedPriority: TodoPriority,
        selectedDueDate: LocalDate?
    ): TodoAiResult {
        val fallback = inferTodo(title, note, selectedPriority, selectedDueDate)
        return runCatching {
            val prompt = """
                Extract todo metadata. Respond strictly as JSON with keys:
                title, note, priority(HIGH|MEDIUM|LOW), dueDate(yyyy-MM-dd or null).
                Input title: "$title"
                Input note: "${note.orEmpty()}"
            """.trimIndent()
            val context = runCatching { ClearrApplication.appContext }.getOrNull() ?: return fallback
            val output = GeminiNanoEngine.generateText(context, prompt) ?: return fallback
            parseTodoJson(output) ?: fallback
        }.getOrElse { fallback }
    }

    suspend fun inferGoalNanoAware(
        title: String,
        target: String?,
        frequency: GoalFrequency,
        emoji: String,
        colorToken: String
    ): GoalAiResult {
        val fallback = inferGoal(title, target, frequency, emoji, colorToken)
        return runCatching {
            val prompt = """
                Suggest goal metadata. Respond strictly as JSON with keys:
                title,target,frequency(DAILY|WEEKLY),emoji,colorToken(Purple|Emerald|Blue|Amber|Coral).
                Title: "$title"
                Target: "${target.orEmpty()}"
                Infer based on goal intent, not UI defaults.
            """.trimIndent()
            val context = runCatching { ClearrApplication.appContext }.getOrNull() ?: return fallback
            val output = GeminiNanoEngine.generateText(context, prompt) ?: return fallback
            parseGoalJson(output) ?: fallback
        }.getOrElse { fallback }
    }

    suspend fun inferBudgetCategoryIdNanoAware(
        note: String?,
        categories: List<BudgetCategory>
    ): Long? {
        val heuristic = inferBudgetCategoryId(note, categories)
        if (categories.isEmpty()) return heuristic
        return runCatching {
            val context = runCatching { ClearrApplication.appContext }.getOrNull() ?: return heuristic
            val prompt = """
                Choose the best category name for this expense note.
                Respond strictly as JSON: {"categoryName":"<exact name from list or empty>"}
                Categories: ${categories.joinToString { it.name }}
                Note: "${note.orEmpty()}"
            """.trimIndent()
            val output = GeminiNanoEngine.generateText(context, prompt) ?: return heuristic
            val extractedName = extractJson(output)?.optString("categoryName")?.trim().orEmpty()
            val chosen = categories.firstOrNull { it.name.equals(extractedName, ignoreCase = true) }?.id
            chosen ?: heuristic
        }.getOrElse { heuristic }
    }

    suspend fun parseSetupIntentNanoAware(text: String): SetupAiResult {
        val fallback = parseSetupIntent(text)
        return runCatching {
            val context = runCatching { ClearrApplication.appContext }.getOrNull() ?: return fallback
            val prompt = """
                Parse setup intent. Respond strictly as JSON with keys:
                trackerType(DUES|GOALS|TODO|BUDGET|null),
                frequency(MONTHLY|WEEKLY|QUARTERLY|ANNUAL|null),
                defaultAmount(number|null),
                trackerName(string|null)
                Input: "$text"
            """.trimIndent()
            val output = GeminiNanoEngine.generateText(context, prompt) ?: return fallback
            parseSetupJson(output) ?: fallback
        }.getOrElse { fallback }
    }

    suspend fun todoInsightNanoAware(todos: List<TodoItem>): String? {
        val fallback = todoInsight(todos)
        if (todos.isEmpty()) return fallback
        return runCatching {
            val context = runCatching { ClearrApplication.appContext }.getOrNull() ?: return fallback
            val prompt = """
                Summarize this todo list in one short actionable sentence.
                pending=${todos.count { it.derivedStatus() == TodoStatus.PENDING }},
                overdue=${todos.count { it.derivedStatus() == TodoStatus.OVERDUE }},
                done=${todos.count { it.derivedStatus() == TodoStatus.DONE }}
            """.trimIndent()
            GeminiNanoEngine.generateText(context, prompt) ?: fallback
        }.getOrElse { fallback }
    }

    suspend fun goalsInsightNanoAware(summaries: List<GoalSummary>): String? {
        val fallback = goalsInsight(summaries)
        if (summaries.isEmpty()) return fallback
        return runCatching {
            val context = runCatching { ClearrApplication.appContext }.getOrNull() ?: return fallback
            val done = summaries.count { it.isDoneThisPeriod }
            val total = summaries.size
            val avgRate = (summaries.map { it.completionRate }.average() * 100).roundToLong()
            val prompt = """
                Summarize goals progress in one short actionable sentence.
                done=$done,total=$total,averageCompletionRatePercent=$avgRate
            """.trimIndent()
            GeminiNanoEngine.generateText(context, prompt) ?: fallback
        }.getOrElse { fallback }
    }

    suspend fun budgetInsightNanoAware(summaries: List<CategorySummary>): String? {
        val fallback = budgetInsight(summaries)
        if (summaries.isEmpty()) return fallback
        return runCatching {
            val context = runCatching { ClearrApplication.appContext }.getOrNull() ?: return fallback
            val over = summaries.count { it.status == BudgetStatus.OVER_BUDGET }
            val near = summaries.count { it.status == BudgetStatus.NEAR_LIMIT }
            val cleared = summaries.count { it.status == BudgetStatus.CLEARED }
            val onTrack = summaries.count { it.status == BudgetStatus.ON_TRACK }
            val prompt = """
                Summarize budget state in one short actionable sentence.
                overBudget=$over,nearLimit=$near,cleared=$cleared,onTrack=$onTrack
            """.trimIndent()
            GeminiNanoEngine.generateText(context, prompt) ?: fallback
        }.getOrElse { fallback }
    }

    fun normalizeTitle(input: String): String {
        val trimmed = input.trim().replace("\\s+".toRegex(), " ")
        if (trimmed.isBlank()) return trimmed
        return buildString(trimmed.length) {
            var firstLetterHandled = false
            for (ch in trimmed) {
                if (!firstLetterHandled && ch.isLetter()) {
                    append(ch.titlecaseChar())
                    firstLetterHandled = true
                } else {
                    append(ch)
                }
            }
        }
    }

    fun inferTodo(
        title: String,
        note: String?,
        selectedPriority: TodoPriority,
        selectedDueDate: LocalDate?
    ): TodoAiResult {
        val normalizedTitle = normalizeTitle(stripLeadingDateHints(title))
        val normalizedNote = note?.trim()?.ifBlank { null }
        val lower = "$title ${note.orEmpty()}".lowercase(Locale.getDefault())

        val inferredPriority = when {
            hasAny(lower, "urgent", "asap", "immediately", "important", "critical") -> TodoPriority.HIGH
            hasAny(lower, "later", "whenever", "low priority") -> TodoPriority.LOW
            else -> selectedPriority
        }

        val inferredDueDate = parseRelativeDate(lower) ?: selectedDueDate

        return TodoAiResult(
            normalizedTitle = normalizedTitle,
            normalizedNote = normalizedNote,
            suggestedPriority = if (selectedPriority == TodoPriority.MEDIUM) inferredPriority else selectedPriority,
            suggestedDueDate = inferredDueDate
        )
    }

    fun inferGoal(
        title: String,
        target: String?,
        frequency: GoalFrequency,
        emoji: String,
        colorToken: String
    ): GoalAiResult {
        val normalizedTitle = normalizeTitle(title)
        val lower = normalizedTitle.lowercase(Locale.getDefault())
        val suggestedTarget = if (target.isNullOrBlank()) defaultGoalTarget(lower) else target.trim()
        val suggestedFrequency = defaultGoalFrequency(lower) ?: frequency
        val (suggestedEmoji, suggestedColor) = defaultGoalIdentity(lower)

        return GoalAiResult(
            normalizedTitle = normalizedTitle,
            suggestedTarget = suggestedTarget,
            suggestedFrequency = suggestedFrequency,
            suggestedEmoji = if (emoji == "🎯") suggestedEmoji else emoji,
            suggestedColorToken = if (colorToken == "Purple") suggestedColor else colorToken
        )
    }

    fun inferBudgetCategoryId(note: String?, categories: List<BudgetCategory>): Long? {
        val text = note?.lowercase(Locale.getDefault())?.trim().orEmpty()
        if (text.isBlank()) return null
        val scored = categories.map { category ->
            val label = category.name.lowercase(Locale.getDefault())
            val score = categoryKeywords(label).count { keyword -> text.contains(keyword) } +
                if (text.contains(label)) 2 else 0
            category.id to score
        }
        return scored.maxByOrNull { it.second }?.takeIf { it.second > 0 }?.first
    }

    fun recommendBudgetAdjustments(
        categories: List<BudgetCategory>,
        entries: List<BudgetEntry>,
        periodId: Long?
    ): Map<Long, Long> {
        if (periodId == null) return emptyMap()
        val periodEntries = entries.filter { it.periodId == periodId }
        return categories.associate { category ->
            val spent = periodEntries.filter { it.categoryId == category.id }.sumOf { it.amountKobo }
            val proposed = when {
                spent <= 0L -> category.plannedAmountKobo
                spent > category.plannedAmountKobo -> (spent * 1.1).roundToLong()
                else -> max(category.plannedAmountKobo, (spent * 1.05).roundToLong())
            }
            category.id to proposed
        }
    }

    fun todoInsight(todos: List<TodoItem>): String? {
        if (todos.isEmpty()) return null
        val overdue = todos.count { it.derivedStatus() == TodoStatus.OVERDUE }
        val done = todos.count { it.derivedStatus() == TodoStatus.DONE }
        return when {
            overdue > 0 -> "$overdue task${if (overdue == 1) "" else "s"} overdue. Clear these first."
            done == todos.size -> "All tasks cleared. Keep momentum."
            else -> "$done of ${todos.size} completed."
        }
    }

    fun goalsInsight(summaries: List<GoalSummary>): String? {
        if (summaries.isEmpty()) return null
        val done = summaries.count { it.isDoneThisPeriod }
        val total = summaries.size
        val avgRate = summaries.map { it.completionRate }.average()
        return when {
            done == total -> "All goals cleared for this period."
            avgRate >= 0.7 -> "Consistency is strong. ${done}/$total cleared now."
            else -> "${total - done} goal${if (total - done == 1) "" else "s"} still pending."
        }
    }

    fun budgetInsight(summaries: List<CategorySummary>): String? {
        if (summaries.isEmpty()) return null
        val over = summaries.filter { it.status == BudgetStatus.OVER_BUDGET }
        val near = summaries.filter { it.status == BudgetStatus.NEAR_LIMIT }
        return when {
            over.isNotEmpty() -> "${over.size} category${if (over.size == 1) "" else "ies"} over budget."
            near.isNotEmpty() -> "${near.size} category${if (near.size == 1) "" else "ies"} close to limit."
            else -> "Budget is on track."
        }
    }

    fun remittanceRiskLabel(
        memberName: String,
        paidMonths: Int,
        expectedMonths: Int
    ): String? {
        if (expectedMonths <= 0) return null
        val ratio = paidMonths.toFloat() / expectedMonths
        return when {
            ratio < 0.5f -> "$memberName has high remittance risk."
            ratio < 0.75f -> "$memberName may miss upcoming remittance."
            else -> null
        }
    }

    fun prioritizeTrackers(list: List<TrackerSummary>): List<TrackerSummary> {
        return list.sortedWith(
            compareByDescending<TrackerSummary> { urgencyScore(it) }
                .thenByDescending { it.createdAt }
        )
    }

    fun parseSetupIntent(text: String): SetupAiResult {
        val lower = text.lowercase(Locale.getDefault())
        val type = when {
            hasAny(lower, "remittance", "dues", "fees", "payment", "clients") -> TrackerType.DUES
            hasAny(lower, "goal", "habit", "streak") -> TrackerType.GOALS
            hasAny(lower, "todo", "task", "checklist") -> TrackerType.TODO
            hasAny(lower, "budget", "expense", "spend") -> TrackerType.BUDGET
            else -> null
        }
        val frequency = when {
            lower.contains("weekly") -> Frequency.WEEKLY
            lower.contains("quarterly") -> Frequency.QUARTERLY
            lower.contains("annual") || lower.contains("yearly") -> Frequency.ANNUAL
            lower.contains("monthly") -> Frequency.MONTHLY
            else -> null
        }
        val amount = Regex("""(?:₦|ngn|n)\s?(\d{1,3}(?:,\d{3})*|\d+)""", RegexOption.IGNORE_CASE)
            .find(lower)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()

        val trackerName = when (type) {
            TrackerType.DUES -> "Remittance"
            TrackerType.EXPENSES -> "Remittance"
            TrackerType.GOALS -> "Goals"
            TrackerType.TODO -> "Todos"
            TrackerType.BUDGET -> "Budget"
            null -> null
        }
        return SetupAiResult(
            trackerName = trackerName,
            trackerType = type,
            suggestedFrequency = frequency,
            suggestedDefaultAmount = amount
        )
    }

    private fun parseRelativeDate(text: String, now: LocalDate = LocalDate.now()): LocalDate? {
        return when {
            text.contains("today") -> now
            text.contains("tomorrow") -> now.plusDays(1)
            text.contains("next week") -> now.plusWeeks(1)
            text.contains("this week") -> now.plusDays(3)
            else -> {
                val inDays = Regex("""in\s+(\d+)\s+day""").find(text)?.groupValues?.getOrNull(1)?.toLongOrNull()
                if (inDays != null) now.plusDays(inDays) else {
                    val weekday = dayOfWeekFromText(text)
                    if (weekday != null) {
                        val days = ChronoUnit.DAYS.between(now, now.with(java.time.temporal.TemporalAdjusters.next(weekday)))
                        now.plusDays(days)
                    } else null
                }
            }
        }
    }

    private fun stripLeadingDateHints(text: String): String {
        return text
            .replace(Regex("""\b(today|tomorrow|next week|this week)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\bin\s+\d+\s+days?\b""", RegexOption.IGNORE_CASE), "")
            .trim()
            .ifBlank { text.trim() }
    }

    private fun dayOfWeekFromText(text: String): java.time.DayOfWeek? {
        return when {
            text.contains("monday") -> java.time.DayOfWeek.MONDAY
            text.contains("tuesday") -> java.time.DayOfWeek.TUESDAY
            text.contains("wednesday") -> java.time.DayOfWeek.WEDNESDAY
            text.contains("thursday") -> java.time.DayOfWeek.THURSDAY
            text.contains("friday") -> java.time.DayOfWeek.FRIDAY
            text.contains("saturday") -> java.time.DayOfWeek.SATURDAY
            text.contains("sunday") -> java.time.DayOfWeek.SUNDAY
            else -> null
        }
    }

    private fun defaultGoalTarget(lower: String): String? = when {
        hasAny(lower, "exercise", "workout", "gym", "run") -> "30 mins"
        hasAny(lower, "read", "book", "study") -> "20 pages"
        hasAny(lower, "save", "money", "cash") -> "₦10,000"
        hasAny(lower, "water", "hydrate") -> "2 liters"
        else -> null
    }

    private fun defaultGoalFrequency(lower: String): GoalFrequency? = when {
        hasAny(lower, "daily", "every day", "habit") -> GoalFrequency.DAILY
        hasAny(lower, "weekly", "per week") -> GoalFrequency.WEEKLY
        hasAny(lower, "save", "budget") -> GoalFrequency.WEEKLY
        else -> null
    }

    private fun defaultGoalIdentity(lower: String): Pair<String, String> = when {
        hasAny(lower, "exercise", "gym", "run", "fitness") -> "🏃" to "Coral"
        hasAny(lower, "save", "money", "budget", "cash") -> "💰" to "Emerald"
        hasAny(lower, "read", "study", "learn") -> "📚" to "Blue"
        hasAny(lower, "food", "diet") -> "🥗" to "Amber"
        else -> "🎯" to "Purple"
    }

    private fun categoryKeywords(label: String): List<String> = when {
        label.contains("food") -> listOf("food", "restaurant", "lunch", "dinner", "breakfast", "groceries", "snack")
        label.contains("transport") -> listOf("uber", "bolt", "taxi", "fuel", "bus", "transport")
        label.contains("housing") -> listOf("rent", "house", "housing", "landlord")
        label.contains("savings") -> listOf("save", "savings", "investment")
        label.contains("health") -> listOf("hospital", "pharmacy", "drug", "health", "medical")
        label.contains("entertainment") -> listOf("movie", "cinema", "games", "show", "fun")
        else -> listOf(label)
    }

    private fun urgencyScore(summary: TrackerSummary): Int {
        val incomplete = max(summary.totalMembers - summary.completedCount, 0)
        val incompleteScore = if (summary.totalMembers > 0) (100 - summary.completionPercent) else 20
        val typeBias = when (summary.type) {
            TrackerType.DUES -> 35
            TrackerType.EXPENSES -> 35
            TrackerType.TODO -> 25
            TrackerType.BUDGET -> 20
            TrackerType.GOALS -> 10
        }
        val newBoost = if (summary.isNew) 5 else 0
        val loadScore = minOf(incomplete * 2, 30)
        return incompleteScore + typeBias + newBoost + loadScore
    }

    private fun hasAny(text: String, vararg needles: String): Boolean = needles.any { text.contains(it) }

    private fun parseTodoJson(output: String): TodoAiResult? {
        val json = extractJson(output) ?: return null
        val title = normalizeTitle(json.optString("title"))
        if (title.isBlank()) return null
        val note = json.optString("note").takeIf { it.isNotBlank() }
        val priority = runCatching { TodoPriority.valueOf(json.optString("priority")) }.getOrNull() ?: TodoPriority.MEDIUM
        val dueDate = json.optString("dueDate")
            .takeIf { it.isNotBlank() && it != "null" }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        return TodoAiResult(title, note, priority, dueDate)
    }

    private fun parseGoalJson(output: String): GoalAiResult? {
        val json = extractJson(output) ?: return null
        val title = normalizeTitle(json.optString("title"))
        if (title.isBlank()) return null
        val target = json.optString("target").takeIf { it.isNotBlank() && it != "null" }
        val frequency = runCatching { GoalFrequency.valueOf(json.optString("frequency")) }.getOrNull() ?: GoalFrequency.DAILY
        val emoji = json.optString("emoji").ifBlank { "🎯" }
        val token = json.optString("colorToken").ifBlank { "Purple" }
        return GoalAiResult(title, target, frequency, emoji, token)
    }

    private fun parseSetupJson(output: String): SetupAiResult? {
        val json = extractJson(output) ?: return null
        val trackerType = runCatching { TrackerType.valueOf(json.optString("trackerType")) }.getOrNull()
        val frequency = runCatching { Frequency.valueOf(json.optString("frequency")) }.getOrNull()
        val amount = if (json.has("defaultAmount")) json.optDouble("defaultAmount").takeIf { !it.isNaN() && it > 0 } else null
        val trackerName = json.optString("trackerName").takeIf { it.isNotBlank() && it != "null" }
        return SetupAiResult(trackerName, trackerType, frequency, amount)
    }

    private fun extractJson(raw: String): JSONObject? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) return null
        return runCatching { JSONObject(raw.substring(start, end + 1)) }.getOrNull()
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/data/dao/TrackerDao.kt`

```kotlin
package com.mikeisesele.clearr.data.dao

import androidx.room.*
import com.mikeisesele.clearr.data.model.RecordStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {

    // ── Trackers ──────────────────────────────────────────────────────────────

    @Query("SELECT * FROM trackers ORDER BY createdAt ASC")
    fun getAllTrackers(): Flow<List<Tracker>>

    @Query("SELECT * FROM trackers WHERE id = :id")
    suspend fun getTrackerById(id: Long): Tracker?

    @Query("SELECT * FROM trackers WHERE id = :id")
    fun getTrackerByIdFlow(id: Long): Flow<Tracker?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: Tracker): Long

    @Update
    suspend fun updateTracker(tracker: Tracker)

    @Query("DELETE FROM trackers WHERE id = :id")
    suspend fun deleteTrackerRow(id: Long)

    @Query("DELETE FROM tracker_members WHERE trackerId = :trackerId")
    suspend fun deleteMembersForTracker(trackerId: Long)

    @Query("DELETE FROM tracker_periods WHERE trackerId = :trackerId")
    suspend fun deletePeriodsForTracker(trackerId: Long)

    @Query("DELETE FROM tracker_records WHERE trackerId = :trackerId")
    suspend fun deleteRecordsForTracker(trackerId: Long)

    @Query("DELETE FROM budget_entries WHERE trackerId = :trackerId")
    suspend fun deleteBudgetEntriesForTracker(trackerId: Long)

    @Query("DELETE FROM budget_categories WHERE trackerId = :trackerId")
    suspend fun deleteBudgetCategoriesForTracker(trackerId: Long)

    @Query("DELETE FROM budget_periods WHERE trackerId = :trackerId")
    suspend fun deleteBudgetPeriodsForTracker(trackerId: Long)

    @Query("DELETE FROM todos WHERE trackerId = :trackerId")
    suspend fun deleteTodosForTracker(trackerId: Long)

    @Query("DELETE FROM goal_completions WHERE goalId IN (SELECT id FROM goals WHERE trackerId = :trackerId)")
    suspend fun deleteGoalCompletionsForTracker(trackerId: Long)

    @Query("DELETE FROM goals WHERE trackerId = :trackerId")
    suspend fun deleteGoalsForTracker(trackerId: Long)

    @Transaction
    suspend fun deleteTracker(trackerId: Long) {
        deleteRecordsForTracker(trackerId)
        deleteMembersForTracker(trackerId)
        deletePeriodsForTracker(trackerId)
        deleteBudgetEntriesForTracker(trackerId)
        deleteBudgetCategoriesForTracker(trackerId)
        deleteBudgetPeriodsForTracker(trackerId)
        deleteTodosForTracker(trackerId)
        deleteGoalCompletionsForTracker(trackerId)
        deleteGoalsForTracker(trackerId)
        deleteTrackerRow(trackerId)
    }

    /** Clear isNew flag after first open */
    @Query("UPDATE trackers SET isNew = 0 WHERE id = :id")
    suspend fun clearNewFlag(id: Long)

    // ── TrackerMembers ────────────────────────────────────────────────────────

    @Query("SELECT * FROM tracker_members WHERE trackerId = :trackerId AND isArchived = 0 ORDER BY name ASC")
    fun getActiveMembers(trackerId: Long): Flow<List<TrackerMember>>

    @Query("SELECT * FROM tracker_members WHERE trackerId = :trackerId ORDER BY name ASC")
    fun getAllMembers(trackerId: Long): Flow<List<TrackerMember>>

    @Query("SELECT COUNT(*) FROM tracker_members WHERE trackerId = :trackerId AND isArchived = 0")
    fun getActiveMemberCount(trackerId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: TrackerMember): Long

    @Update
    suspend fun updateMember(member: TrackerMember)

    @Query("UPDATE tracker_members SET isArchived = :archived WHERE id = :id")
    suspend fun setMemberArchived(id: Long, archived: Boolean)

    @Query("DELETE FROM tracker_records WHERE trackerId = :trackerId AND memberId = :memberId")
    suspend fun deleteRecordsForTrackerMember(trackerId: Long, memberId: Long)

    @Query("DELETE FROM tracker_members WHERE trackerId = :trackerId AND id = :memberId")
    suspend fun deleteTrackerMember(trackerId: Long, memberId: Long)

    // ── TrackerPeriods ────────────────────────────────────────────────────────

    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId ORDER BY startDate ASC")
    fun getPeriodsForTracker(trackerId: Long): Flow<List<TrackerPeriod>>

    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND isCurrent = 1 LIMIT 1")
    suspend fun getCurrentPeriod(trackerId: Long): TrackerPeriod?

    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND isCurrent = 1 LIMIT 1")
    fun getCurrentPeriodFlow(trackerId: Long): Flow<TrackerPeriod?>

    @Query("SELECT * FROM tracker_periods WHERE id = :id LIMIT 1")
    suspend fun getPeriodById(id: Long): TrackerPeriod?

    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND label = :label LIMIT 1")
    suspend fun getPeriodByLabel(trackerId: Long, label: String): TrackerPeriod?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: TrackerPeriod): Long

    @Update
    suspend fun updatePeriod(period: TrackerPeriod)

    /** Mark all periods for a tracker as not current, then set the given one as current */
    @Query("UPDATE tracker_periods SET isCurrent = 0 WHERE trackerId = :trackerId")
    suspend fun clearCurrentPeriods(trackerId: Long)

    @Query("UPDATE tracker_periods SET isCurrent = 1 WHERE id = :periodId")
    suspend fun setCurrentPeriod(periodId: Long)

    // ── TrackerRecords ────────────────────────────────────────────────────────

    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId AND periodId = :periodId")
    fun getRecordsForPeriod(trackerId: Long, periodId: Long): Flow<List<TrackerRecord>>

    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId")
    fun getRecordsForTracker(trackerId: Long): Flow<List<TrackerRecord>>

    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId AND periodId = :periodId AND memberId = :memberId LIMIT 1")
    suspend fun getRecord(trackerId: Long, periodId: Long, memberId: Long): TrackerRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TrackerRecord): Long

    @Update
    suspend fun updateRecord(record: TrackerRecord)

    @Query("DELETE FROM tracker_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    /** Count completed records for a period (status IN (PAID, PRESENT, DONE)) */
    @Query("""
        SELECT COUNT(*) FROM tracker_records
        WHERE trackerId = :trackerId
        AND periodId = :periodId
        AND status IN ('PAID', 'PRESENT', 'DONE')
    """)
    suspend fun getCompletedCountForPeriod(trackerId: Long, periodId: Long): Int
}
```

## `app/src/main/java/com/mikeisesele/clearr/data/model/AppConfig.kt`

```kotlin
package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton configuration table – always id = 1.
 * Stores all settings from the Setup Wizard and can be edited later via Settings.
 */
@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,

    // Group identity
    val groupName: String = "JSS Durumi Brothers",
    val adminName: String = "",
    val adminPhone: String = "",

    // Tracker behavior
    val trackerType: TrackerType = TrackerType.DUES,
    val frequency: Frequency = Frequency.MONTHLY,

    // Amount
    val defaultAmount: Double = 5000.0,

    // Custom frequency config (JSON arrays stored as strings)
    val customPeriodLabels: String = "[]",  // e.g. ["Term 1","Term 2","Term 3"]
    val variableAmounts: String = "[]",     // e.g. [5000,7000,6000] per period

    // UI Layout style
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,

    // Reminder notifications
    val remindersEnabled: Boolean = true,
    val reminderDayOfPeriod: Int = 5,  // day number within period to send reminder

    // Wizard
    val setupComplete: Boolean = false
)

enum class TrackerType {
    DUES,       // Group financial obligations
    EXPENSES,   // Legacy value kept for backward compatibility with old DB rows
    GOALS,      // Personal goals / recurring habits
    TODO,       // Personal to-do / task list
    BUDGET      // Planned vs actual spending
}

enum class Frequency {
    MONTHLY,
    WEEKLY,
    QUARTERLY,   // 4 periods/year (Jan, Apr, Jul, Oct)
    TERMLY,      // 3 periods/year (school terms)
    BIANNUAL,    // 2 periods/year
    ANNUAL,      // 1 period/year
    CUSTOM       // user-defined period labels
}

enum class LayoutStyle {
    GRID,     // default horizontal scrolling grid
    KANBAN,   // columns per period, members as cards
    CARDS,    // member cards with period chips
    RECEIPT   // ledger / receipt style
}
```

## `app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt`

```kotlin
package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A Tracker is an independent tracking unit.
 * Each tracker owns its own member list (TrackerMember) and period records (TrackerPeriod + TrackerRecord).
 * It is completely isolated from other trackers.
 */
@Entity(tableName = "trackers")
data class Tracker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TrackerType = TrackerType.DUES,
    val frequency: Frequency = Frequency.MONTHLY,
    /** Per-tracker layout style (independent from other trackers). */
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    /** Default amount per period (only relevant for DUES type) */
    val defaultAmount: Double = 5000.0,
    /** True until the tracker is first opened after creation */
    val isNew: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A member belonging to exactly one tracker.
 * Member lists are per-tracker and do not share with other trackers.
 */
@Entity(tableName = "tracker_members")
data class TrackerMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val name: String,
    val phone: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A Period represents one cycle of tracking for a given tracker.
 * e.g. "February 2026", "Week 8, 2026", "Q1 2026", "Term 1 2026"
 * Periods are generated automatically based on tracker frequency.
 */
@Entity(tableName = "tracker_periods")
data class TrackerPeriod(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    /** Human-readable label: "February 2026", "Week 8, 2026", etc. */
    val label: String,
    val startDate: Long,
    val endDate: Long,
    /** True = this is the period currently active based on the date */
    val isCurrent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A Record is one member's status for one period.
 * status depends on tracker type:
 *  DUES       → PAID / PARTIAL / UNPAID
 *  ATTENDANCE → PRESENT / ABSENT
 *  TASKS      → DONE / PENDING
 *  EVENTS     → PRESENT / ABSENT
 */
@Entity(tableName = "tracker_records")
data class TrackerRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val periodId: Long,
    val memberId: Long,
    val status: RecordStatus = RecordStatus.UNPAID,
    /** Amount paid — only meaningful for DUES type */
    val amountPaid: Double = 0.0,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RecordStatus {
    /** DUES */
    PAID, PARTIAL, UNPAID,
    /** ATTENDANCE / EVENTS */
    PRESENT, ABSENT,
    /** TASKS */
    DONE, PENDING
}

/** Aggregated summary emitted by the DAO for the tracker list card */
data class TrackerSummary(
    val trackerId: Long,
    val name: String,
    val type: TrackerType,
    val frequency: Frequency,
    val currentPeriodLabel: String,
    val totalMembers: Int,
    val completedCount: Int,
    val completionPercent: Int,
    val amountCompletedKobo: Long = 0L,
    val amountTargetKobo: Long = 0L,
    val isNew: Boolean,
    val createdAt: Long
)
```

## `app/src/main/java/com/mikeisesele/clearr/di/DatabaseModule.kt`

```kotlin
package com.mikeisesele.clearr.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.BudgetDao
import com.mikeisesele.clearr.data.dao.GoalsDao
import com.mikeisesele.clearr.data.dao.MemberDao
import com.mikeisesele.clearr.data.dao.PaymentRecordDao
import com.mikeisesele.clearr.data.dao.TrackerDao
import com.mikeisesele.clearr.data.dao.TodoDao
import com.mikeisesele.clearr.data.dao.YearConfigDao
import com.mikeisesele.clearr.data.database.DuesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Calendar
import javax.inject.Singleton

private val SEED_MEMBERS = listOf(
    "Henry Nwazuru",
    "Chidubem",
    "Simon Boniface",
    "Ikechukwu Udeh",
    "Oluwatobi Majekodunmi",
    "Dare Oladunjoye",
    "Michael Isesele",
    "Faruk Umar"
)

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DuesDatabase {
        return Room.databaseBuilder(
            context,
            DuesDatabase::class.java,
            "dues_database"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val now = System.currentTimeMillis()
                    val year = Calendar.getInstance().get(Calendar.YEAR)
                    // Seed default members
                    SEED_MEMBERS.forEach { name ->
                        db.execSQL(
                            "INSERT INTO members (name, phone, isArchived, createdAt) VALUES (?, NULL, 0, ?)",
                            arrayOf(name, now)
                        )
                    }
                    // Seed current year config with default ₦5,000
                    db.execSQL(
                        "INSERT OR IGNORE INTO year_configs (year, dueAmountPerMonth, startedAt) VALUES (?, ?, ?)",
                        arrayOf(year, 5000.0, now)
                    )
                    // Note: app_config row is NOT seeded here – the Setup Wizard
                    // creates it on first launch so setupComplete = false triggers wizard.
                }
            })
            .build()
    }

    @Provides
    fun provideMemberDao(db: DuesDatabase): MemberDao = db.memberDao()

    @Provides
    fun providePaymentRecordDao(db: DuesDatabase): PaymentRecordDao = db.paymentRecordDao()

    @Provides
    fun provideYearConfigDao(db: DuesDatabase): YearConfigDao = db.yearConfigDao()

    @Provides
    fun provideAppConfigDao(db: DuesDatabase): AppConfigDao = db.appConfigDao()

    @Provides
    fun provideTrackerDao(db: DuesDatabase): TrackerDao = db.trackerDao()

    @Provides
    fun provideBudgetDao(db: DuesDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideTodoDao(db: DuesDatabase): TodoDao = db.todoDao()

    @Provides
    fun provideGoalsDao(db: DuesDatabase): GoalsDao = db.goalsDao()
}
```

## `app/src/main/java/com/mikeisesele/clearr/di/RepositoryModule.kt`

```kotlin
package com.mikeisesele.clearr.di

import com.mikeisesele.clearr.data.repository.DuesRepositoryImpl
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDuesRepository(impl: DuesRepositoryImpl): DuesRepository
}
```

## `app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt`

```kotlin
package com.mikeisesele.clearr.domain.trackers

import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.GoalPeriodKey
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.derivedStatus
import com.mikeisesele.clearr.domain.repository.DuesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ObserveTrackerSummariesUseCase @Inject constructor(
    private val repository: DuesRepository
) {
    operator fun invoke(): Flow<List<TrackerSummary>> =
        repository.getAllTrackers().flatMapLatest { trackers ->
            if (trackers.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(trackers.map(::summaryFlow)) { summaries -> summaries.toList() }
            }
        }

    private fun summaryFlow(tracker: Tracker): Flow<TrackerSummary> = when (tracker.type) {
        TrackerType.BUDGET -> {
            val budgetFrequency = when (tracker.frequency) {
                Frequency.WEEKLY -> BudgetFrequency.WEEKLY
                else -> BudgetFrequency.MONTHLY
            }
            repository.getBudgetPeriods(tracker.id, budgetFrequency)
                .flatMapLatest { periods ->
                    repository.getBudgetCategories(tracker.id, budgetFrequency)
                        .flatMapLatest { categories ->
                            repository.getBudgetEntriesForTracker(tracker.id)
                                .flatMapLatest { entries ->
                                    repository.getBudgetCategoryPlansForTracker(tracker.id)
                                        .map { plans ->
                                            val latestPeriod = periods.lastOrNull()
                                            val periodEntries = entries.filter { it.periodId == latestPeriod?.id }
                                            val periodPlans = plans.filter { it.periodId == latestPeriod?.id }
                                                .associateBy { it.categoryId }
                                            val totalPlannedKobo = categories.sumOf { category ->
                                                periodPlans[category.id]?.plannedAmountKobo ?: category.plannedAmountKobo
                                            }
                                            val totalSpentKobo = periodEntries.sumOf { it.amountKobo }
                                            val clearedCount = categories.count { category ->
                                                val spent = periodEntries
                                                    .asSequence()
                                                    .filter { it.categoryId == category.id }
                                                    .sumOf { it.amountKobo }
                                                val planned = periodPlans[category.id]?.plannedAmountKobo
                                                    ?: category.plannedAmountKobo
                                                planned > 0L && spent >= planned
                                            }
                                            TrackerSummary(
                                                trackerId = tracker.id,
                                                name = tracker.name,
                                                type = tracker.type,
                                                frequency = tracker.frequency,
                                                currentPeriodLabel = latestPeriod?.label ?: currentPeriodLabel(tracker.frequency),
                                                totalMembers = categories.size,
                                                completedCount = clearedCount,
                                                completionPercent = if (categories.isNotEmpty()) {
                                                    ((clearedCount.toDouble() / categories.size) * 100).toInt().coerceIn(0, 100)
                                                } else {
                                                    0
                                                },
                                                amountCompletedKobo = totalSpentKobo,
                                                amountTargetKobo = totalPlannedKobo,
                                                isNew = tracker.isNew,
                                                createdAt = tracker.createdAt
                                            )
                                        }
                                }
                        }
                }
        }

        TrackerType.TODO -> repository.getTodosForTracker(tracker.id).map { todos ->
            val doneCount = todos.count { it.derivedStatus() == TodoStatus.DONE }
            TrackerSummary(
                trackerId = tracker.id,
                name = tracker.name,
                type = tracker.type,
                frequency = tracker.frequency,
                currentPeriodLabel = "Todo List",
                totalMembers = todos.size,
                completedCount = doneCount,
                completionPercent = if (todos.isNotEmpty()) {
                    ((doneCount.toDouble() / todos.size) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                },
                isNew = tracker.isNew,
                createdAt = tracker.createdAt
            )
        }

        TrackerType.GOALS -> repository.getGoalsForTracker(tracker.id)
            .flatMapLatest { goals ->
                repository.getGoalCompletionsForTracker(tracker.id)
                    .map { completions ->
                        val doneCount = goals.count { goal ->
                            val currentKey = GoalPeriodKey.currentKey(goal.frequency)
                            completions.any { it.goalId == goal.id && it.periodKey == currentKey }
                        }
                        TrackerSummary(
                            trackerId = tracker.id,
                            name = tracker.name,
                            type = tracker.type,
                            frequency = tracker.frequency,
                            currentPeriodLabel = "Today",
                            totalMembers = goals.size,
                            completedCount = doneCount,
                            completionPercent = if (goals.isNotEmpty()) {
                                ((doneCount.toDouble() / goals.size) * 100).toInt().coerceIn(0, 100)
                            } else {
                                0
                            },
                            amountCompletedKobo = 0L,
                            amountTargetKobo = 0L,
                            isNew = tracker.isNew,
                            createdAt = tracker.createdAt
                        )
                    }
            }

        else -> repository.getActiveMembersForTracker(tracker.id)
            .flatMapLatest { members ->
                    repository.getCurrentPeriodFlow(tracker.id)
                        .flatMapLatest { period ->
                            if (period == null) {
                                flowOf(buildSummary(tracker, members, period, emptyList()))
                            } else {
                                repository.getRecordsForPeriod(tracker.id, period.id)
                                    .map { records -> buildSummary(tracker, members, period, records) }
                            }
                        }
            }
    }

    private fun buildSummary(
        tracker: Tracker,
        members: List<TrackerMember>,
        period: TrackerPeriod?,
        records: List<TrackerRecord>
    ): TrackerSummary {
        val total = members.size
        val completedCount = records.count { record ->
            record.status.name in completedStatuses(tracker.type)
        }
        val amountTargetKobo = when (tracker.type) {
            TrackerType.DUES,
            TrackerType.EXPENSES -> (tracker.defaultAmount * 100).toLong().coerceAtLeast(0L) * total
            else -> 0L
        }
        val amountCompletedKobo = when (tracker.type) {
            TrackerType.DUES,
            TrackerType.EXPENSES -> records.sumOf { (it.amountPaid * 100).toLong().coerceAtLeast(0L) }
            else -> 0L
        }
        val percent = when {
            total > 0 -> ((completedCount.toDouble() / total) * 100).toInt().coerceIn(0, 100)
            else -> 0
        }
        return TrackerSummary(
            trackerId = tracker.id,
            name = tracker.name,
            type = tracker.type,
            frequency = tracker.frequency,
            currentPeriodLabel = period?.label ?: currentPeriodLabel(tracker.frequency),
            totalMembers = total,
            completedCount = completedCount,
            completionPercent = percent,
            amountCompletedKobo = amountCompletedKobo,
            amountTargetKobo = amountTargetKobo,
            isNew = tracker.isNew,
            createdAt = tracker.createdAt
        )
    }

    private fun completedStatuses(type: TrackerType): Set<String> = when (type) {
        TrackerType.DUES -> setOf("PAID")
        TrackerType.EXPENSES -> setOf("PAID")
        TrackerType.GOALS -> setOf("DONE")
        TrackerType.TODO -> setOf("DONE")
        TrackerType.BUDGET -> emptySet()
    }

    private fun currentPeriodLabel(frequency: Frequency): String {
        val calendar = Calendar.getInstance()
        return when (frequency) {
            Frequency.MONTHLY -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            Frequency.WEEKLY -> "Week ${calendar.get(Calendar.WEEK_OF_YEAR)}, ${calendar.get(Calendar.YEAR)}"
            Frequency.QUARTERLY -> "Q${(calendar.get(Calendar.MONTH) / 3) + 1} ${calendar.get(Calendar.YEAR)}"
            Frequency.TERMLY -> "Term ${(calendar.get(Calendar.MONTH) / 4) + 1} ${calendar.get(Calendar.YEAR)}"
            Frequency.BIANNUAL -> "H${if (calendar.get(Calendar.MONTH) < 6) 1 else 2} ${calendar.get(Calendar.YEAR)}"
            Frequency.ANNUAL -> "${calendar.get(Calendar.YEAR)}"
            Frequency.CUSTOM -> "Current Period"
        }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/domain/trackers/TrackerBootstrapper.kt`

```kotlin
package com.mikeisesele.clearr.domain.trackers

import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.DuesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackerBootstrapper @Inject constructor(
    private val repository: DuesRepository
) {
    suspend fun ensureStaticTrackers() {
        val now = System.currentTimeMillis()
        val existing = repository.getAllTrackers().first()
        val existingTypes = existing.mapTo(mutableSetOf()) { it.type }

        suspend fun createIfMissing(type: TrackerType, name: String) {
            if (type in existingTypes) return
            val trackerId = repository.insertTracker(
                Tracker(
                    name = name,
                    type = type,
                    frequency = Frequency.MONTHLY,
                    layoutStyle = LayoutStyle.GRID,
                    defaultAmount = 0.0,
                    isNew = false,
                    createdAt = now
                )
            )
            if (type == TrackerType.BUDGET) {
                listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { budgetFrequency ->
                    repository.ensureBudgetPeriods(trackerId, budgetFrequency)
                }
            }
            existingTypes += type
        }

        createIfMissing(TrackerType.GOALS, "Goals")
        createIfMissing(TrackerType.TODO, "Todos")
        createIfMissing(TrackerType.BUDGET, "Budget")

        existing
            .filter { it.type == TrackerType.BUDGET }
            .forEach { tracker ->
                listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { budgetFrequency ->
                    repository.ensureBudgetPeriods(tracker.id, budgetFrequency)
                }
            }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/CompletionScreen.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme

/**
 * Completion Screen — shown after the last slide or skip.
 * Animates in on mount. Single CTA → SetupWizardScreen.
 * No back navigation (back-stack cleared before arriving here).
 */
@Composable
fun CompletionScreen(onCreateTracker: () -> Unit) {

    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "completion_alpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) com.mikeisesele.clearr.ui.theme.ClearrDimens.dp0 else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "completion_offset"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .alpha(alpha)
                .offset(y = offsetY)
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp36),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp80)
                    .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
                    .background(ClearrColors.EmeraldBg),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp36, color = ClearrColors.Emerald, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp28))

            Text(
                "You're all set.",
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24,
                fontWeight = FontWeight.Black,
                color = ClearrColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))

            Text(
                "Let's create your first tracker. It only takes a minute.",
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
                color = ClearrColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = (14 * 1.7).sp
            )

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp40))

            Button(
                onClick = onCreateTracker,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearrColors.Violet,
                    contentColor = ClearrColors.Surface
                ),
                contentPadding = PaddingValues(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
            ) {
                Text(
                    "Create First Tracker →",
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15,
                    fontWeight = FontWeight.ExtraBold,
                    color = ClearrColors.Surface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletionScreenPreview() {
    ClearrTheme {
        CompletionScreen(onCreateTracker = {})
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/components/OnboardingSlides.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import kotlinx.coroutines.delay

internal data class SlideData(
    val icon: String,
    val accentColor: Color,
    val bgColor: Color,
    val headline: String,
    val subtext: String
)

internal val slides = listOf(
    SlideData("◎", ClearrColors.Violet, ClearrColors.VioletBg, "Clear your obligations.", "with clarity and proof, clear remittance, goals, todos, and budget tracking in one app."),
    SlideData("◈", ClearrColors.Emerald, ClearrColors.EmeraldBg, "Every tracker,\nEvery period.", "Create independent trackers for remittance, goals, todos, or budget — with weekly, monthly, quarterly, or custom periods."),
    SlideData("⬡", ClearrColors.Amber, ClearrColors.AmberBg, "At a glance, always.", "See what’s cleared, pending, or overdue across your obligations — without guesswork.")
)

private val memberNames = listOf("Henry", "Simon", "Dare", "Tobi", "Michael")
private data class MockTracker(val name: String, val color: Color, val bg: Color, val icon: String, val paid: Int, val total: Int)
private val mockTrackers = listOf(
    MockTracker("Client Remittance Status", ClearrColors.Violet, ClearrColors.VioletBg, "₦", 7, 12),
    MockTracker("Weekly Goals Progress", ClearrColors.Emerald, ClearrColors.EmeraldBg, "✓", 18, 23),
    MockTracker("Todo Completion Tracker", ClearrColors.Amber, ClearrColors.AmberBg, "⬡", 4, 9)
)
private val slide3Names = listOf("John", "Simon", "Jessy", "Chelsea", "Mike", "Ola.")
private val slide3Cleared = setOf(0, 1, 3, 5)

@Composable
internal fun Slide1Visual() {
    var clearedIndices by remember { mutableStateOf(setOf(0, 2)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_200)
            clearedIndices = buildSet {
                memberNames.indices.forEach { i -> if ((i + clearedIndices.size) % 2 == 0) add(i) }
                if (size < 2) add(0)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), modifier = Modifier.fillMaxWidth()) {
        memberNames.forEachIndexed { i, name ->
            val cleared = i in clearedIndices
            val rowOffset by animateDpAsState(targetValue = if (cleared) (-2).dp else ClearrDimens.dp2, animationSpec = tween(400), label = "row_offset_$i")
            val avatarBg = if (cleared) ClearrColors.EmeraldBg else ClearrColors.Border
            val statusColor = if (cleared) ClearrColors.Emerald else ClearrColors.TextMuted

            Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(ClearrDimens.dp14), shadowElevation = ClearrDimens.dp1, modifier = Modifier.fillMaxWidth().offset(x = rowOffset)) {
                Row(modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)) {
                    Box(modifier = Modifier.size(ClearrDimens.dp30).clip(CircleShape).background(avatarBg), contentAlignment = Alignment.Center) {
                        Text(name.first().toString(), fontSize = ClearrTextSizes.sp13, color = if (cleared) ClearrColors.Emerald else ClearrColors.TextSecondary)
                    }
                    Text(name, fontSize = ClearrTextSizes.sp13, color = ClearrColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text(if (cleared) "Cleared ✓" else "Pending...", fontSize = ClearrTextSizes.sp11, color = statusColor)
                    Box(modifier = Modifier.size(ClearrDimens.dp7).clip(CircleShape).background(statusColor))
                }
            }
        }
    }
}

@Composable
internal fun Slide2Visual() {
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); animated = true }

    Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp8), modifier = Modifier.fillMaxWidth()) {
        mockTrackers.forEachIndexed { i, tracker ->
            val pct = tracker.paid.toFloat() / tracker.total
            val animatedPct by animateFloatAsState(targetValue = if (animated) pct else 0f, animationSpec = tween(1000, delayMillis = i * 100, easing = FastOutSlowInEasing), label = "bar_$i")
            val cardAlpha by animateFloatAsState(targetValue = if (animated) 1f else 0f, animationSpec = tween(300, delayMillis = i * 100), label = "card_alpha_$i")
            val cardOffset by animateDpAsState(targetValue = if (animated) ClearrDimens.dp0 else ClearrDimens.dp12, animationSpec = tween(300, delayMillis = i * 100), label = "card_offset_$i")

            Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(ClearrDimens.dp14), shadowElevation = ClearrDimens.dp1, modifier = Modifier.fillMaxWidth().alpha(cardAlpha).offset(y = cardOffset)) {
                Row(modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)) {
                    Box(modifier = Modifier.size(ClearrDimens.dp32).clip(RoundedCornerShape(ClearrDimens.dp10)).background(tracker.bg), contentAlignment = Alignment.Center) {
                        Text(tracker.icon, fontSize = ClearrTextSizes.sp15, color = tracker.color)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(tracker.name, fontSize = ClearrTextSizes.sp12, color = ClearrColors.TextPrimary)
                            Text("${tracker.paid}/${tracker.total}", fontSize = ClearrTextSizes.sp11, color = tracker.color)
                        }
                        Spacer(Modifier.height(ClearrDimens.dp5))
                        Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp5).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Border)) {
                            Box(modifier = Modifier.fillMaxWidth(animatedPct).height(ClearrDimens.dp5).clip(RoundedCornerShape(ClearrDimens.dp99)).background(tracker.color))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Slide3Visual() {
    val clearedCount = slide3Cleared.size
    val totalCount = slide3Names.size
    val pct = clearedCount.toFloat() / totalCount

    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); animated = true }
    val animatedPct by animateFloatAsState(targetValue = if (animated) pct else 0f, animationSpec = tween(1000, easing = FastOutSlowInEasing), label = "period_bar")

    Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(ClearrDimens.dp16), shadowElevation = ClearrDimens.dp2, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(ClearrDimens.dp16)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("February 2026", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = ClearrTextSizes.sp14, color = ClearrColors.TextPrimary)
                Text("${(pct * 100).toInt()}%", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = ClearrTextSizes.sp14, color = ClearrColors.Violet)
            }
            Spacer(Modifier.height(ClearrDimens.dp8))
            Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp6).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Border)) {
                Box(modifier = Modifier.fillMaxWidth(animatedPct).height(ClearrDimens.dp6).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Violet))
            }
            Spacer(Modifier.height(ClearrDimens.dp12))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp5)) {
                slide3Names.forEachIndexed { index, name ->
                    val cleared = index in slide3Cleared
                    Box(modifier = Modifier.clip(RoundedCornerShape(ClearrDimens.dp20)).background(if (cleared) ClearrColors.EmeraldBg else ClearrColors.CoralBg).padding(horizontal = ClearrDimens.dp10, vertical = ClearrDimens.dp4)) {
                        Text(name, fontSize = ClearrTextSizes.sp10, color = if (cleared) ClearrColors.Emerald else ClearrColors.Coral)
                    }
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp10))
            Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                OnboardingStatTile("Cleared", "$clearedCount", ClearrColors.Emerald, ClearrColors.EmeraldBg, Modifier.weight(1f))
                OnboardingStatTile("Pending", "${totalCount - clearedCount}", ClearrColors.Amber, ClearrColors.AmberBg, Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun OnboardingStatTile(label: String, value: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(ClearrDimens.dp10)).background(bg).padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp8)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = ClearrTextSizes.sp18, color = color)
            Text(label, fontSize = ClearrTextSizes.sp10, color = color.copy(alpha = 0.7f))
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun Slide1VisualPreview() {
    ClearrTheme { Slide1Visual() }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/AppShellViewModel.kt`

```kotlin
package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import com.mikeisesele.clearr.ui.feature.dashboard.utils.primarySummaryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class AppShellViewModel @Inject constructor(
    private val trackerBootstrapper: TrackerBootstrapper,
    private val observeTrackerSummaries: ObserveTrackerSummariesUseCase
) : BaseViewModel<AppShellUiState, AppShellAction, AppShellEvent>(
    initialState = AppShellUiState(isLoading = true)
) {

    init {
        onAction(AppShellAction.Observe)
    }

    override fun onAction(action: AppShellAction) {
        when (action) {
            AppShellAction.Observe -> observeTrackers()
        }
    }

    private fun observeTrackers() {
        launch {
            trackerBootstrapper.ensureStaticTrackers()
            observeTrackerSummaries().collectLatest { summaries ->
                updateState {
                    it.copy(
                        budgetTrackerId = summaries.primarySummaryOf(TrackerType.BUDGET)?.trackerId,
                        todoTrackerId = summaries.primarySummaryOf(TrackerType.TODO)?.trackerId,
                        goalsTrackerId = summaries.primarySummaryOf(TrackerType.GOALS)?.trackerId,
                        remittanceCount = summaries.count { summary ->
                            summary.type == TrackerType.DUES || summary.type == TrackerType.EXPENSES
                        },
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class AppShellUiState(
    val budgetTrackerId: Long? = null,
    val todoTrackerId: Long? = null,
    val goalsTrackerId: Long? = null,
    val remittanceCount: Int = 0,
    val isLoading: Boolean = true
) : BaseState

sealed interface AppShellAction {
    data object Observe : AppShellAction
}

sealed interface AppShellEvent : ViewEvent
```

## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/NavRoutes.kt`

```kotlin
package com.mikeisesele.clearr.ui.navigation

sealed class NavRoutes(val route: String) {
    object Setup : NavRoutes("setup")
    object Dashboard : NavRoutes("dashboard")
    object RemittanceHome : NavRoutes("remittance_home")
    object BudgetRoot : NavRoutes("budget_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "budget_root/$trackerId"
        const val baseRoute = "budget_root"
    }
    object TodoRoot : NavRoutes("todo_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "todo_root/$trackerId"
        const val baseRoute = "todo_root"
    }
    object GoalsRoot : NavRoutes("goals_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "goals_root/$trackerId"
        const val baseRoute = "goals_root"
    }
    object TrackerDetail : NavRoutes("tracker_detail/{trackerId}") {
        fun createRoute(trackerId: Long) = "tracker_detail/$trackerId"
    }
    object TodoAdd : NavRoutes("todo_add/{trackerId}") {
        fun createRoute(trackerId: Long) = "todo_add/$trackerId"
    }
    object GoalAdd : NavRoutes("goal_add/{trackerId}") {
        fun createRoute(trackerId: Long) = "goal_add/$trackerId"
    }
    object BudgetAddCategory : NavRoutes("budget_add_category/{trackerId}") {
        fun createRoute(trackerId: Long) = "budget_add_category/$trackerId"
    }
    object Settings : NavRoutes("settings")
    object Home : NavRoutes("dashboard")
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/components/AppBottomNav.kt`

```kotlin
package com.mikeisesele.clearr.ui.navigation.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

internal enum class AppBottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    HOME("Home", Icons.Filled.Home),
    BUDGET("Budget", Icons.Filled.AccountBalanceWallet),
    TODOS("Todos", Icons.Filled.Checklist),
    GOALS("Goals", Icons.Filled.CheckCircle),
    REMITTANCE("Remittance", Icons.Filled.Payments)
}

@Composable
internal fun AppBottomNav(
    selectedItem: AppBottomNavItem?,
    onSelect: (AppBottomNavItem) -> Unit
) {
    val colors = LocalDuesColors.current
    NavigationBar(
        containerColor = colors.surface,
        contentColor = colors.text,
        modifier = Modifier.navigationBarsPadding()
    ) {
        AppBottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = item == selectedItem,
                onClick = { onSelect(item) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.accent,
                    selectedTextColor = colors.accent,
                    unselectedIconColor = colors.muted,
                    unselectedTextColor = colors.muted,
                    indicatorColor = ClearrColors.BrandPrimary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/theme/Color.kt`

```kotlin
package com.mikeisesele.clearr.ui.theme

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.data.model.RecordStatus
import com.mikeisesele.clearr.data.model.TrackerType

/**
 * Clearr Brand Color System
 * Four semantic primaries — every color carries meaning, never decorative.
 * Rule: Never hardcode a hex anywhere in the UI. Always reference ClearrColors.*
 */
object ClearrColors {

    // ── BRAND PRIMARIES ───────────────────────────────────────────────────────
    val Violet  = Color(0xFF6C63FF)  // Primary / Dues
    val Emerald = Color(0xFF00A67E)  // Success / Attendance / Cleared
    val Amber   = Color(0xFFF59E0B)  // Caution / Tasks / Pending
    val Blue    = Color(0xFF3B82F6)  // Info / Budget
    val Coral   = Color(0xFFEF4444)  // Danger / Events / Unpaid / Absent
    val Orange  = Color(0xFFFF9500)  // Emphasis for due-today / medium priority

    // ── BRAND TOKENS (for logo/marketing + app-level theming) ───────────────
    val BrandPrimary   = Violet
    val BrandSecondary = Emerald
    val BrandAccent    = Amber
    val BrandDanger    = Coral
    val BrandBackground = Color(0xFFF7F7FB)
    val BrandText      = Color(0xFF1A1A2E)

    // ── TINTED BACKGROUNDS (12% opacity on white) ─────────────────────────────
    val VioletBg  = Color(0xFFEEF0FF)
    val EmeraldBg = Color(0xFFE6F7F3)
    val AmberBg   = Color(0xFFFEF3C7)
    val BlueBg    = Color(0xFFEFF6FF)
    val CoralBg   = Color(0xFFFEE2E2)

    // ── TINTED SURFACES (18% opacity — chips, badges, icon containers) ────────
    val VioletSurface  = Color(0xFFE8E6FF)
    val EmeraldSurface = Color(0xFFD1F5EA)
    val AmberSurface   = Color(0xFFFEF3C7)
    val BlueSurface    = Color(0xFFDCEEFF)
    val CoralSurface   = Color(0xFFFFE4E4)

    // ── NEUTRALS ──────────────────────────────────────────────────────────────
    val Background   = Color(0xFFF7F7FB)   // App background
    val Surface      = Color(0xFFFFFFFF)   // Cards, sheets
    val Border       = Color(0xFFF0F0F0)   // Dividers, inactive progress track
    val TextPrimary  = Color(0xFF1A1A2E)   // Headlines, primary text
    val TextSecondary= Color(0xFF888888)   // Subtitles, hints
    val TextMuted    = Color(0xFFBBBBBB)   // Placeholders, disabled
    val Inactive     = Color(0xFFDDDDDD)   // Inactive dots, empty bars
    val NavBg        = Color(0xFFEBEBF0)   // Back button background
    val Transparent  = Color.Transparent

    // ── DARK MODE VARIANTS ────────────────────────────────────────────────────
    val DarkBackground  = Color(0xFF0F0F1A)
    val DarkSurface     = Color(0xFF1A1A2E)
    val DarkCard        = Color(0xFF242438)
    val DarkBorder      = Color(0xFF2A2A3E)
    val DarkTextPrimary = Color(0xFFF0F0F8)
    val DarkTextMuted   = Color(0xFF888888)
    val DarkInactive    = Color(0xFF3A3A50)

    // ── MISC LEGACY (kept for WhatsApp share button, not brand palette) ───────
    val WhatsAppGreen = Color(0xFF25D366)
}

// ── TrackerType extensions ────────────────────────────────────────────────────

fun TrackerType.brandColor(): Color = when (this) {
    TrackerType.DUES     -> ClearrColors.Violet
    TrackerType.EXPENSES -> ClearrColors.Violet
    TrackerType.GOALS    -> ClearrColors.Emerald
    TrackerType.TODO     -> ClearrColors.Amber
    TrackerType.BUDGET   -> ClearrColors.Blue
}

fun TrackerType.brandBackground(): Color = when (this) {
    TrackerType.DUES     -> ClearrColors.VioletBg
    TrackerType.EXPENSES -> ClearrColors.VioletBg
    TrackerType.GOALS    -> ClearrColors.EmeraldBg
    TrackerType.TODO     -> ClearrColors.AmberBg
    TrackerType.BUDGET   -> ClearrColors.BlueBg
}

fun TrackerType.brandSurface(): Color = when (this) {
    TrackerType.DUES     -> ClearrColors.VioletSurface
    TrackerType.EXPENSES -> ClearrColors.VioletSurface
    TrackerType.GOALS    -> ClearrColors.EmeraldSurface
    TrackerType.TODO     -> ClearrColors.AmberSurface
    TrackerType.BUDGET   -> ClearrColors.BlueSurface
}

fun TrackerType.brandIcon(): String = when (this) {
    TrackerType.DUES     -> "₦"
    TrackerType.EXPENSES -> "₦"
    TrackerType.GOALS    -> "🎯"
    TrackerType.TODO     -> "☑"
    TrackerType.BUDGET   -> "💳"
}

data class BudgetColorScheme(
    val color: Color,
    val background: Color
)

fun ClearrColors.fromToken(token: String): BudgetColorScheme = when (token.lowercase()) {
    "teal" -> BudgetColorScheme(Emerald, EmeraldBg)
    "emerald" -> BudgetColorScheme(Emerald, EmeraldBg)
    "coral" -> BudgetColorScheme(Coral, CoralBg)
    "amber" -> BudgetColorScheme(Amber, AmberBg)
    "violet" -> BudgetColorScheme(Violet, VioletBg)
    "blue" -> BudgetColorScheme(Blue, BlueBg)
    "purple" -> BudgetColorScheme(Violet, VioletBg)
    "orange" -> BudgetColorScheme(Color(0xFFF97316), Color(0xFFFFF3E8))
    else -> BudgetColorScheme(Violet, VioletBg)
}

// ── RecordStatus extensions ───────────────────────────────────────────────────

fun RecordStatus.brandColor(): Color = when (this) {
    RecordStatus.PAID,
    RecordStatus.PRESENT,
    RecordStatus.DONE    -> ClearrColors.Emerald
    RecordStatus.PARTIAL,
    RecordStatus.PENDING -> ClearrColors.Amber
    RecordStatus.UNPAID,
    RecordStatus.ABSENT  -> ClearrColors.Coral
}

fun RecordStatus.brandBackground(): Color = when (this) {
    RecordStatus.PAID,
    RecordStatus.PRESENT,
    RecordStatus.DONE    -> ClearrColors.EmeraldBg
    RecordStatus.PARTIAL,
    RecordStatus.PENDING -> ClearrColors.AmberBg
    RecordStatus.UNPAID,
    RecordStatus.ABSENT  -> ClearrColors.CoralBg
}

fun RecordStatus.brandLabel(type: TrackerType): String = when (type) {
    TrackerType.DUES -> when (this) {
        RecordStatus.PAID    -> "Paid"
        RecordStatus.UNPAID  -> "Unpaid"
        RecordStatus.PARTIAL -> "Partial"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
    TrackerType.EXPENSES -> when (this) {
        RecordStatus.PAID    -> "Paid"
        RecordStatus.UNPAID  -> "Unpaid"
        RecordStatus.PARTIAL -> "Partial"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
    TrackerType.GOALS -> when (this) {
        RecordStatus.DONE    -> "Done"
        RecordStatus.PENDING -> "Pending"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
    TrackerType.TODO -> when (this) {
        RecordStatus.DONE    -> "Done"
        RecordStatus.PENDING -> "Pending"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
    TrackerType.BUDGET -> when (this) {
        RecordStatus.PAID -> "On Track"
        RecordStatus.PARTIAL -> "Near Limit"
        RecordStatus.UNPAID -> "Over"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

// ── Legacy aliases kept for backward compatibility with existing screens ───────
// These are the old Indigo/Green/Amber/Red tokens referenced in Theme.kt.
// Gradually migrate all call-sites to ClearrColors.*
val Violet  = ClearrColors.Violet
val Emerald = ClearrColors.Emerald
val Amber   = ClearrColors.Amber
val Coral   = ClearrColors.Coral

// Old Indigo aliases (map to Clearr Violet)
val Indigo400 = ClearrColors.Violet
val Indigo500 = ClearrColors.Violet
val Indigo600 = Color(0xFF5652D6)  // slightly darker shade, used in dark scheme

// Old semantic aliases
val Green400 = ClearrColors.Emerald
val Amber400 = ClearrColors.Amber
val Red400   = ClearrColors.Coral

// Old dark palette
val DarkBg      = ClearrColors.DarkBackground
val DarkSurface = ClearrColors.DarkSurface
val DarkCard    = ClearrColors.DarkCard
val DarkBorder  = ClearrColors.DarkBorder
val DarkText    = ClearrColors.DarkTextPrimary
val DarkMuted   = ClearrColors.DarkTextMuted
val DarkDim     = ClearrColors.DarkInactive

// Old light palette
val LightBg      = ClearrColors.Background
val LightSurface = ClearrColors.Surface
val LightCard    = Color(0xFFF1F5F9)
val LightBorder  = ClearrColors.Border
val LightText    = ClearrColors.TextPrimary
val LightMuted   = ClearrColors.TextSecondary
val LightDim     = ClearrColors.Inactive

val WhatsAppGreen = ClearrColors.WhatsAppGreen
```

## `app/src/main/java/com/mikeisesele/clearr/ui/theme/Theme.kt`

```kotlin
package com.mikeisesele.clearr.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mikeisesele.clearr.ui.commons.state.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary          = ClearrColors.BrandPrimary,
    onPrimary        = Color.White,
    primaryContainer = Indigo600,
    onPrimaryContainer = Color.White,
    secondary        = ClearrColors.BrandSecondary,
    onSecondary      = Color.White,
    background       = ClearrColors.DarkBackground,
    onBackground     = ClearrColors.DarkTextPrimary,
    surface          = ClearrColors.DarkSurface,
    onSurface        = ClearrColors.DarkTextPrimary,
    surfaceVariant   = ClearrColors.DarkCard,
    onSurfaceVariant = ClearrColors.DarkTextMuted,
    outline          = ClearrColors.DarkBorder,
    error            = ClearrColors.BrandDanger,
    onError          = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary          = ClearrColors.BrandPrimary,
    onPrimary        = Color.White,
    primaryContainer = ClearrColors.VioletBg,
    onPrimaryContainer = ClearrColors.BrandPrimary,
    secondary        = ClearrColors.BrandSecondary,
    onSecondary      = Color.White,
    background       = ClearrColors.BrandBackground,
    onBackground     = ClearrColors.BrandText,
    surface          = ClearrColors.Surface,
    onSurface        = ClearrColors.BrandText,
    surfaceVariant   = LightCard,
    onSurfaceVariant = ClearrColors.TextSecondary,
    outline          = ClearrColors.Border,
    error            = ClearrColors.BrandDanger,
    onError          = Color.White
)

/**
 * Theme-aware color bag used throughout the app via LocalDuesColors.current.
 * Maps Clearr brand tokens to semantic slots used by existing composables.
 */
data class DuesColors(
    val bg: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    /** Primary interactive accent — Clearr Violet */
    val accent: Color,
    /** Positive / cleared — Clearr Emerald */
    val green: Color,
    /** Caution / pending — Clearr Amber */
    val amber: Color,
    /** Danger / unpaid — Clearr Coral */
    val red: Color,
    val text: Color,
    val muted: Color,
    val dim: Color,
    val isDark: Boolean
)

val LocalDuesColors = staticCompositionLocalOf {
    DuesColors(
        bg      = ClearrColors.DarkBackground,
        surface = ClearrColors.DarkSurface,
        card    = ClearrColors.DarkCard,
        border  = ClearrColors.DarkBorder,
        accent  = ClearrColors.BrandPrimary,
        green   = ClearrColors.BrandSecondary,
        amber   = ClearrColors.BrandAccent,
        red     = ClearrColors.BrandDanger,
        text    = ClearrColors.DarkTextPrimary,
        muted   = ClearrColors.DarkTextMuted,
        dim     = ClearrColors.DarkInactive,
        isDark  = true
    )
}

/** Light-mode DuesColors instance using Clearr tokens */
private fun lightDuesColors() = DuesColors(
    bg      = ClearrColors.BrandBackground,
    surface = ClearrColors.Surface,
    card    = LightCard,
    border  = ClearrColors.Border,
    accent  = ClearrColors.BrandPrimary,
    green   = ClearrColors.BrandSecondary,
    amber   = ClearrColors.BrandAccent,
    red     = ClearrColors.BrandDanger,
    text    = ClearrColors.BrandText,
    muted   = ClearrColors.TextSecondary,
    dim     = ClearrColors.Inactive,
    isDark  = false
)

/** Dark-mode DuesColors instance using Clearr tokens */
private fun darkDuesColors() = DuesColors(
    bg      = ClearrColors.DarkBackground,
    surface = ClearrColors.DarkSurface,
    card    = ClearrColors.DarkCard,
    border  = ClearrColors.DarkBorder,
    accent  = ClearrColors.BrandPrimary,
    green   = ClearrColors.BrandSecondary,
    amber   = ClearrColors.BrandAccent,
    red     = ClearrColors.BrandDanger,
    text    = ClearrColors.DarkTextPrimary,
    muted   = ClearrColors.DarkTextMuted,
    dim     = ClearrColors.DarkInactive,
    isDark  = true
)

/** Root theme composable for the app. */
@Composable
fun ClearrTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Light mode is temporarily forced globally. Keep ThemeMode in the API so
    // dynamic behavior can be restored later without changing call sites.
    val darkTheme = false

    // Dynamic color on API 31+ (Material You), falls back to Clearr palette
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val duesColors = if (darkTheme) darkDuesColors() else lightDuesColors()

    CompositionLocalProvider(
        LocalDuesColors provides duesColors,
        LocalClearrSpacing provides ClearrSpacing(),
        LocalClearrRadii provides ClearrRadii(),
        LocalClearrSizes provides ClearrSizes()
    ) {
        val radii = ClearrRadii()
        val shapes = Shapes(
            small = RoundedCornerShape(radii.sm),
            medium = RoundedCornerShape(radii.md),
            large = RoundedCornerShape(radii.lg)
        )
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            shapes = shapes,
            content     = content
        )
    }
}
```

