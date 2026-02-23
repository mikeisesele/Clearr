package com.mikeisesele.clearr.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.BudgetDao
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
}
