package com.mikeisesele.clearr.runtime

interface KeyValueStoreDriver {
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)

    fun getString(key: String): String?
    fun setString(key: String, value: String)

    fun getLong(key: String): Long?
    fun setLong(key: String, value: Long)

    fun getDouble(key: String, defaultValue: Double = 0.0): Double
    fun setDouble(key: String, value: Double)
}
