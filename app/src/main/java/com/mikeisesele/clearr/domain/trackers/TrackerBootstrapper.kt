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
