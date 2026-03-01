package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.preview.InMemoryClearrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import platform.Foundation.NSUserDefaults

private const val IOS_APP_CONFIG_PRESENT_KEY = "clearr.appconfig.present"
private const val IOS_APP_CONFIG_GROUP_NAME_KEY = "clearr.appconfig.groupName"
private const val IOS_APP_CONFIG_ADMIN_NAME_KEY = "clearr.appconfig.adminName"
private const val IOS_APP_CONFIG_ADMIN_PHONE_KEY = "clearr.appconfig.adminPhone"
private const val IOS_APP_CONFIG_TRACKER_TYPE_KEY = "clearr.appconfig.trackerType"
private const val IOS_APP_CONFIG_FREQUENCY_KEY = "clearr.appconfig.frequency"
private const val IOS_APP_CONFIG_DEFAULT_AMOUNT_KEY = "clearr.appconfig.defaultAmount"
private const val IOS_APP_CONFIG_CUSTOM_PERIOD_LABELS_KEY = "clearr.appconfig.customPeriodLabels"
private const val IOS_APP_CONFIG_VARIABLE_AMOUNTS_KEY = "clearr.appconfig.variableAmounts"
private const val IOS_APP_CONFIG_LAYOUT_STYLE_KEY = "clearr.appconfig.layoutStyle"
private const val IOS_APP_CONFIG_REMINDERS_ENABLED_KEY = "clearr.appconfig.remindersEnabled"
private const val IOS_APP_CONFIG_REMINDER_DAY_KEY = "clearr.appconfig.reminderDayOfPeriod"
private const val IOS_APP_CONFIG_SETUP_COMPLETE_KEY = "clearr.appconfig.setupComplete"
private const val IOS_TRACKERS_KEY = "clearr.trackers"
private const val IOS_GOALS_KEY = "clearr.goals"
private const val IOS_GOAL_COMPLETIONS_KEY = "clearr.goalCompletions"
private const val IOS_TODOS_KEY = "clearr.todos"
private const val RECORD_SEPARATOR = "\u001E"
private const val FIELD_SEPARATOR = "\u001F"
private const val NULL_TOKEN = "\u0000"

class IosClearrRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val delegate: InMemoryClearrRepository = InMemoryClearrRepository.create(
        trackers = loadTrackers(defaults),
        budgetPeriods = emptyList(),
        budgetCategories = emptyList(),
        budgetPlans = emptyList(),
        budgetEntries = emptyList(),
        goals = loadGoals(defaults),
        goalCompletions = loadGoalCompletions(defaults),
        todos = loadTodos(defaults),
        appConfig = loadAppConfig(defaults)
    )
) : ClearrRepository by delegate {
    private val appConfigFlow = MutableStateFlow(loadAppConfig(defaults))

    override fun getAppConfigFlow(): Flow<AppConfig?> = appConfigFlow

    override suspend fun getAppConfig(): AppConfig? = appConfigFlow.value

    override suspend fun upsertAppConfig(config: AppConfig) {
        saveAppConfig(defaults, config)
        delegate.upsertAppConfig(config)
        appConfigFlow.value = config
    }

    override suspend fun insertTracker(tracker: Tracker): Long {
        val id = delegate.insertTracker(tracker)
        persistTrackers()
        return id
    }

    override suspend fun updateTracker(tracker: Tracker) {
        delegate.updateTracker(tracker)
        persistTrackers()
    }

    override suspend fun deleteTracker(id: Long) {
        delegate.deleteTracker(id)
        persistTrackers()
        persistGoals()
        persistGoalCompletions()
        persistTodos()
    }

    override suspend fun clearTrackerNewFlag(id: Long) {
        delegate.clearTrackerNewFlag(id)
        persistTrackers()
    }

    override suspend fun insertTodo(todo: TodoItem) {
        delegate.insertTodo(todo)
        persistTodos()
    }

    override suspend fun updateTodo(todo: TodoItem) {
        delegate.updateTodo(todo)
        persistTodos()
    }

    override suspend fun markTodoDone(id: String, completedAt: Long) {
        delegate.markTodoDone(id, completedAt)
        persistTodos()
    }

    override suspend fun deleteTodo(id: String) {
        delegate.deleteTodo(id)
        persistTodos()
    }

    override suspend fun insertGoal(goal: Goal) {
        delegate.insertGoal(goal)
        persistGoals()
    }

    override suspend fun addGoalCompletion(completion: GoalCompletion) {
        delegate.addGoalCompletion(completion)
        persistGoalCompletions()
    }

    override suspend fun deleteGoal(goalId: String) {
        delegate.deleteGoal(goalId)
        persistGoals()
        persistGoalCompletions()
    }

    private fun persistTrackers() {
        defaults.setObject(encodeTrackers(delegate.snapshotTrackers()), forKey = IOS_TRACKERS_KEY)
    }

    private fun persistGoals() {
        defaults.setObject(encodeGoals(delegate.snapshotGoals()), forKey = IOS_GOALS_KEY)
    }

    private fun persistGoalCompletions() {
        defaults.setObject(
            encodeGoalCompletions(delegate.snapshotGoalCompletions()),
            forKey = IOS_GOAL_COMPLETIONS_KEY
        )
    }

    private fun persistTodos() {
        defaults.setObject(encodeTodos(delegate.snapshotTodos()), forKey = IOS_TODOS_KEY)
    }
}

private fun loadAppConfig(defaults: NSUserDefaults): AppConfig? {
    if (!defaults.boolForKey(IOS_APP_CONFIG_PRESENT_KEY)) return null

    return AppConfig(
        groupName = defaults.stringForKey(IOS_APP_CONFIG_GROUP_NAME_KEY) ?: "Clearr",
        adminName = defaults.stringForKey(IOS_APP_CONFIG_ADMIN_NAME_KEY) ?: "",
        adminPhone = defaults.stringForKey(IOS_APP_CONFIG_ADMIN_PHONE_KEY) ?: "",
        trackerType = defaults.stringForKey(IOS_APP_CONFIG_TRACKER_TYPE_KEY)
            ?.let { runCatching { TrackerType.valueOf(it) }.getOrNull() }
            ?: TrackerType.BUDGET,
        frequency = defaults.stringForKey(IOS_APP_CONFIG_FREQUENCY_KEY)
            ?.let { runCatching { Frequency.valueOf(it) }.getOrNull() }
            ?: Frequency.MONTHLY,
        defaultAmount = defaults.doubleForKey(IOS_APP_CONFIG_DEFAULT_AMOUNT_KEY),
        customPeriodLabels = defaults.stringForKey(IOS_APP_CONFIG_CUSTOM_PERIOD_LABELS_KEY) ?: "[]",
        variableAmounts = defaults.stringForKey(IOS_APP_CONFIG_VARIABLE_AMOUNTS_KEY) ?: "[]",
        layoutStyle = defaults.stringForKey(IOS_APP_CONFIG_LAYOUT_STYLE_KEY)
            ?.let { runCatching { LayoutStyle.valueOf(it) }.getOrNull() }
            ?: LayoutStyle.GRID,
        remindersEnabled = defaults.boolForKey(IOS_APP_CONFIG_REMINDERS_ENABLED_KEY),
        reminderDayOfPeriod = defaults.integerForKey(IOS_APP_CONFIG_REMINDER_DAY_KEY).toInt().takeIf { it > 0 } ?: 5,
        setupComplete = defaults.boolForKey(IOS_APP_CONFIG_SETUP_COMPLETE_KEY)
    )
}

private fun saveAppConfig(defaults: NSUserDefaults, config: AppConfig) {
    defaults.setBool(true, forKey = IOS_APP_CONFIG_PRESENT_KEY)
    defaults.setObject(config.groupName, forKey = IOS_APP_CONFIG_GROUP_NAME_KEY)
    defaults.setObject(config.adminName, forKey = IOS_APP_CONFIG_ADMIN_NAME_KEY)
    defaults.setObject(config.adminPhone, forKey = IOS_APP_CONFIG_ADMIN_PHONE_KEY)
    defaults.setObject(config.trackerType.name, forKey = IOS_APP_CONFIG_TRACKER_TYPE_KEY)
    defaults.setObject(config.frequency.name, forKey = IOS_APP_CONFIG_FREQUENCY_KEY)
    defaults.setDouble(config.defaultAmount, forKey = IOS_APP_CONFIG_DEFAULT_AMOUNT_KEY)
    defaults.setObject(config.customPeriodLabels, forKey = IOS_APP_CONFIG_CUSTOM_PERIOD_LABELS_KEY)
    defaults.setObject(config.variableAmounts, forKey = IOS_APP_CONFIG_VARIABLE_AMOUNTS_KEY)
    defaults.setObject(config.layoutStyle.name, forKey = IOS_APP_CONFIG_LAYOUT_STYLE_KEY)
    defaults.setBool(config.remindersEnabled, forKey = IOS_APP_CONFIG_REMINDERS_ENABLED_KEY)
    defaults.setInteger(config.reminderDayOfPeriod.toLong(), forKey = IOS_APP_CONFIG_REMINDER_DAY_KEY)
    defaults.setBool(config.setupComplete, forKey = IOS_APP_CONFIG_SETUP_COMPLETE_KEY)
}

private fun loadTrackers(defaults: NSUserDefaults): List<Tracker> =
    defaults.stringForKey(IOS_TRACKERS_KEY)
        ?.takeIf { it.isNotEmpty() }
        ?.split(RECORD_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.map { record ->
            val fields = decodeRecord(record)
            Tracker(
                id = fields[0].toLong(),
                name = fields[1],
                type = TrackerType.valueOf(fields[2]),
                frequency = Frequency.valueOf(fields[3]),
                layoutStyle = LayoutStyle.valueOf(fields[4]),
                defaultAmount = fields[5].toDouble(),
                isNew = fields[6].toBoolean(),
                createdAt = fields[7].toLong()
            )
        }
        ?: emptyList()

private fun loadGoals(defaults: NSUserDefaults): List<Goal> =
    defaults.stringForKey(IOS_GOALS_KEY)
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

private fun loadGoalCompletions(defaults: NSUserDefaults): List<GoalCompletion> =
    defaults.stringForKey(IOS_GOAL_COMPLETIONS_KEY)
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

private fun loadTodos(defaults: NSUserDefaults): List<TodoItem> =
    defaults.stringForKey(IOS_TODOS_KEY)
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

private fun encodeTrackers(trackers: List<Tracker>): String =
    trackers.joinToString(RECORD_SEPARATOR) { tracker ->
        encodeRecord(
            listOf(
                tracker.id.toString(),
                tracker.name,
                tracker.type.name,
                tracker.frequency.name,
                tracker.layoutStyle.name,
                tracker.defaultAmount.toString(),
                tracker.isNew.toString(),
                tracker.createdAt.toString()
            )
        )
    }

private fun encodeGoals(goals: List<Goal>): String =
    goals.joinToString(RECORD_SEPARATOR) { goal ->
        encodeRecord(
            listOf(
                goal.id,
                goal.trackerId.toString(),
                goal.title,
                goal.emoji,
                goal.colorToken,
                goal.target ?: NULL_TOKEN,
                goal.frequency.name,
                goal.createdAt.toString()
            )
        )
    }

private fun encodeGoalCompletions(completions: List<GoalCompletion>): String =
    completions.joinToString(RECORD_SEPARATOR) { completion ->
        encodeRecord(
            listOf(
                completion.id,
                completion.goalId,
                completion.periodKey,
                completion.completedAt.toString()
            )
        )
    }

private fun encodeTodos(todos: List<TodoItem>): String =
    todos.joinToString(RECORD_SEPARATOR) { todo ->
        encodeRecord(
            listOf(
                todo.id,
                todo.trackerId.toString(),
                todo.title,
                todo.note ?: NULL_TOKEN,
                todo.priority.name,
                todo.dueDate?.toString() ?: NULL_TOKEN,
                todo.status.name,
                todo.createdAt.toString(),
                todo.completedAt?.toString() ?: NULL_TOKEN
            )
        )
    }

private fun encodeRecord(fields: List<String>): String =
    fields.joinToString(FIELD_SEPARATOR) { field ->
        buildString {
            field.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '\u001E' -> append("\\e")
                    '\u001F' -> append("\\f")
                    else -> append(char)
                }
            }
        }
    }

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
