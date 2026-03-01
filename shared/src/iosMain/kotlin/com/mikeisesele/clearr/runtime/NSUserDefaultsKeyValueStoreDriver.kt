package com.mikeisesele.clearr.runtime

import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults

class NSUserDefaultsKeyValueStoreDriver(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : KeyValueStoreDriver {
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        if (defaults.objectForKey(key) == null) defaultValue else defaults.boolForKey(key)

    override fun setBoolean(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun setString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun getLong(key: String): Long? =
        (defaults.objectForKey(key) as? NSNumber)?.longLongValue

    override fun setLong(key: String, value: Long) {
        defaults.setObject(value, forKey = key)
    }

    override fun getDouble(key: String, defaultValue: Double): Double =
        if (defaults.objectForKey(key) == null) defaultValue else defaults.doubleForKey(key)

    override fun setDouble(key: String, value: Double) {
        defaults.setDouble(value, forKey = key)
    }
}
