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
                trackerType(GOALS|TODO|BUDGET|null),
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

    fun prioritizeTrackers(list: List<TrackerSummary>): List<TrackerSummary> {
        return list.sortedWith(
            compareByDescending<TrackerSummary> { urgencyScore(it) }
                .thenByDescending { it.createdAt }
        )
    }

    fun parseSetupIntent(text: String): SetupAiResult {
        val lower = text.lowercase(Locale.getDefault())
        val type = when {
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
