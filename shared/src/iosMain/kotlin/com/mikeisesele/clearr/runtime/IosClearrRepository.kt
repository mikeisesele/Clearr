package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.preview.InMemoryClearrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

class IosClearrRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val delegate: InMemoryClearrRepository = InMemoryClearrRepository.empty(loadAppConfig(defaults))
) : ClearrRepository by delegate {
    private val appConfigFlow = MutableStateFlow(loadAppConfig(defaults))

    override fun getAppConfigFlow(): Flow<AppConfig?> = appConfigFlow

    override suspend fun getAppConfig(): AppConfig? = appConfigFlow.value

    override suspend fun upsertAppConfig(config: AppConfig) {
        saveAppConfig(defaults, config)
        delegate.upsertAppConfig(config)
        appConfigFlow.value = config
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
