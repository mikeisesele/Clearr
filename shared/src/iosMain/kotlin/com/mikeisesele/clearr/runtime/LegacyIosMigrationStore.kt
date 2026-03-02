package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import kotlinx.datetime.LocalDate

private const val IOS_GOALS_KEY = "clearr.goals"
private const val IOS_GOAL_COMPLETIONS_KEY = "clearr.goalCompletions"
private const val IOS_TODOS_KEY = "clearr.todos"
private const val IOS_BUDGET_PERIODS_KEY = "clearr.budget.periods"
private const val IOS_BUDGET_CATEGORIES_KEY = "clearr.budget.categories"
private const val IOS_BUDGET_PLANS_KEY = "clearr.budget.plans"
private const val IOS_BUDGET_ENTRIES_KEY = "clearr.budget.entries"
private const val RECORD_SEPARATOR = "\u001E"
private const val FIELD_SEPARATOR = "\u001F"
private const val NULL_TOKEN = "\u0000"

class LegacyIosMigrationStore(
    private val store: KeyValueStoreDriver = NSUserDefaultsKeyValueStoreDriver()
) {
    fun loadBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): List<BudgetPeriod> =
        decodeBudgetPeriods(store).filter { it.trackerId == trackerId && it.frequency == frequency }

    fun loadBudgetCategories(trackerId: Long, frequency: BudgetFrequency): List<BudgetCategory> =
        decodeBudgetCategories(store).filter { it.trackerId == trackerId && it.frequency == frequency }

    fun loadBudgetCategoryPlans(trackerId: Long): List<BudgetCategoryPlan> =
        decodeBudgetCategoryPlans(store).filter { it.trackerId == trackerId }

    fun loadBudgetEntries(trackerId: Long): List<BudgetEntry> =
        decodeBudgetEntries(store).filter { it.trackerId == trackerId }

    fun loadGoals(trackerId: Long): List<Goal> =
        decodeGoals(store).filter { it.trackerId == trackerId }

    fun loadGoalCompletionsForTracker(trackerId: Long): List<GoalCompletion> {
        val goalIds = loadGoals(trackerId).mapTo(mutableSetOf()) { it.id }
        return decodeGoalCompletions(store).filter { it.goalId in goalIds }
    }

    fun loadTodos(trackerId: Long): List<TodoItem> =
        decodeTodos(store).filter { it.trackerId == trackerId }
}

private fun decodeBudgetPeriods(store: KeyValueStoreDriver): List<BudgetPeriod> =
    store.getString(IOS_BUDGET_PERIODS_KEY)
        ?.takeIf { it.isNotEmpty() }
        ?.split(RECORD_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.map { record ->
            val fields = decodeRecord(record)
            BudgetPeriod(
                id = fields[0].toLong(),
                trackerId = fields[1].toLong(),
                frequency = BudgetFrequency.valueOf(fields[2]),
                label = fields[3],
                startDate = fields[4].toLong(),
                endDate = fields[5].toLong()
            )
        }
        ?: emptyList()

private fun decodeBudgetCategories(store: KeyValueStoreDriver): List<BudgetCategory> =
    store.getString(IOS_BUDGET_CATEGORIES_KEY)
        ?.takeIf { it.isNotEmpty() }
        ?.split(RECORD_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.map { record ->
            val fields = decodeRecord(record)
            BudgetCategory(
                id = fields[0].toLong(),
                trackerId = fields[1].toLong(),
                frequency = BudgetFrequency.valueOf(fields[2]),
                name = fields[3],
                icon = fields[4],
                colorToken = fields[5],
                plannedAmountKobo = fields[6].toLong(),
                sortOrder = fields[7].toInt(),
                createdAt = fields[8].toLong()
            )
        }
        ?: emptyList()

private fun decodeBudgetCategoryPlans(store: KeyValueStoreDriver): List<BudgetCategoryPlan> =
    store.getString(IOS_BUDGET_PLANS_KEY)
        ?.takeIf { it.isNotEmpty() }
        ?.split(RECORD_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.map { record ->
            val fields = decodeRecord(record)
            BudgetCategoryPlan(
                id = fields[0].toLong(),
                trackerId = fields[1].toLong(),
                categoryId = fields[2].toLong(),
                periodId = fields[3].toLong(),
                plannedAmountKobo = fields[4].toLong(),
                createdAt = fields[5].toLong()
            )
        }
        ?: emptyList()

private fun decodeBudgetEntries(store: KeyValueStoreDriver): List<BudgetEntry> =
    store.getString(IOS_BUDGET_ENTRIES_KEY)
        ?.takeIf { it.isNotEmpty() }
        ?.split(RECORD_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.map { record ->
            val fields = decodeRecord(record)
            BudgetEntry(
                id = fields[0].toLong(),
                trackerId = fields[1].toLong(),
                categoryId = fields[2].toLong(),
                periodId = fields[3].toLong(),
                amountKobo = fields[4].toLong(),
                note = fields[5].takeUnless { it == NULL_TOKEN },
                loggedAt = fields[6].toLong()
            )
        }
        ?: emptyList()

private fun decodeGoals(store: KeyValueStoreDriver): List<Goal> =
    store.getString(IOS_GOALS_KEY)
        ?.takeIf { it.isNotEmpty() }
        ?.split(RECORD_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.map { record ->
            val fields = decodeRecord(record)
            Goal(
                id = fields[0],
                trackerId = fields[1].toLong(),
                title = fields[2],
                emoji = fields[3],
                colorToken = fields[4],
                target = fields[5].takeUnless { it == NULL_TOKEN },
                frequency = GoalFrequency.valueOf(fields[6]),
                createdAt = fields[7].toLong()
            )
        }
        ?: emptyList()

private fun decodeGoalCompletions(store: KeyValueStoreDriver): List<GoalCompletion> =
    store.getString(IOS_GOAL_COMPLETIONS_KEY)
        ?.takeIf { it.isNotEmpty() }
        ?.split(RECORD_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.map { record ->
            val fields = decodeRecord(record)
            GoalCompletion(
                id = fields[0],
                goalId = fields[1],
                periodKey = fields[2],
                completedAt = fields[3].toLong()
            )
        }
        ?: emptyList()

private fun decodeTodos(store: KeyValueStoreDriver): List<TodoItem> =
    store.getString(IOS_TODOS_KEY)
        ?.takeIf { it.isNotEmpty() }
        ?.split(RECORD_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.map { record ->
            val fields = decodeRecord(record)
            TodoItem(
                id = fields[0],
                trackerId = fields[1].toLong(),
                title = fields[2],
                note = fields[3].takeUnless { it == NULL_TOKEN },
                priority = TodoPriority.valueOf(fields[4]),
                dueDate = fields[5].takeUnless { it == NULL_TOKEN }?.let(LocalDate::parse),
                status = TodoStatus.valueOf(fields[6]),
                createdAt = fields[7].toLong(),
                completedAt = fields[8].takeUnless { it == NULL_TOKEN }?.toLong()
            )
        }
        ?: emptyList()

private fun decodeRecord(record: String): List<String> {
    val fields = mutableListOf<String>()
    val current = StringBuilder()
    var escaped = false

    record.forEach { char ->
        when {
            escaped -> {
                current.append(
                    when (char) {
                        '\\' -> '\\'
                        'e' -> '\u001E'
                        'f' -> '\u001F'
                        else -> char
                    }
                )
                escaped = false
            }
            char == '\\' -> escaped = true
            char.toString() == FIELD_SEPARATOR -> {
                fields += current.toString()
                current.clear()
            }
            else -> current.append(char)
        }
    }
    fields += current.toString()
    return fields
}
