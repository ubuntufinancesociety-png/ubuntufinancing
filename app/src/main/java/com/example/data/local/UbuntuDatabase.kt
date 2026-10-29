package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [GroupEntity::class, MemberEntity::class, LedgerEntryEntity::class, LoanEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UbuntuDatabase : RoomDatabase() {
    abstract fun ubuntuDao(): UbuntuDao

    companion object {
        @Volatile
        private var INSTANCE: UbuntuDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): UbuntuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UbuntuDatabase::class.java,
                    "ubuntu_finance_db"
                )
                    .addCallback(UbuntuDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class UbuntuDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.ubuntuDao())
                }
            }
        }

        suspend fun populateInitialData(dao: UbuntuDao) {
            // Initial Groups
            val initialGroups = listOf(
                GroupEntity(
                    id = "g1",
                    name = "Soweto Stokvel",
                    type = "STOKVEL",
                    description = "Monthly community savings club for mutual investment.",
                    currency = "ZAR"
                ),
                GroupEntity(
                    id = "g2",
                    name = "Family Lending Pool",
                    type = "LENDING_POOL",
                    description = "Family emergency fund and interest-free assist loans.",
                    currency = "ZAR"
                )
            )
            dao.insertGroups(initialGroups)

            // Initial Members
            val initialMembers = listOf(
                MemberEntity(
                    id = "m1",
                    groupId = "g1",
                    name = "Thabo M.",
                    phone = "+27 82 555 0100",
                    role = "TREASURER",
                    status = "active"
                ),
                MemberEntity(
                    id = "m2",
                    groupId = "g1",
                    name = "Lerato K.",
                    phone = "+27 82 555 0101",
                    role = "MEMBER",
                    status = "active"
                ),
                MemberEntity(
                    id = "m3",
                    groupId = "g1",
                    name = "Sipho D.",
                    phone = "+27 82 555 0102",
                    role = "MEMBER",
                    status = "active"
                ),
                MemberEntity(
                    id = "m4",
                    groupId = "g2",
                    name = "Lerato K.",
                    phone = "+27 82 555 0101",
                    role = "TREASURER",
                    status = "active"
                ),
                MemberEntity(
                    id = "m5",
                    groupId = "g2",
                    name = "Thabo M.",
                    phone = "+27 82 555 0100",
                    role = "MEMBER",
                    status = "active"
                )
            )
            dao.insertMembers(initialMembers)

            // Initial Ledger
            val initialLedger = listOf(
                LedgerEntryEntity(
                    id = 1L,
                    groupId = "g1",
                    member = "Thabo M.",
                    amount = 500.0,
                    date = "2026-09-01",
                    reportedBy = "Thabo T.",
                    status = "active",
                    correctsId = null,
                    reason = null,
                    type = "CONTRIBUTION",
                    note = "Monthly Stokvel contribution"
                ),
                LedgerEntryEntity(
                    id = 2L,
                    groupId = "g1",
                    member = "Lerato K.",
                    amount = 200.0,
                    date = "2026-09-02",
                    reportedBy = "Thabo T.",
                    status = "active",
                    correctsId = null,
                    reason = null,
                    type = "CONTRIBUTION",
                    note = "Monthly Stokvel contribution"
                ),
                LedgerEntryEntity(
                    id = 3L,
                    groupId = "g1",
                    member = "Sipho D.",
                    amount = 500.0,
                    date = "2026-09-03",
                    reportedBy = "Thabo T.",
                    status = "superseded",
                    correctsId = null,
                    reason = null,
                    type = "CONTRIBUTION",
                    note = null
                ),
                LedgerEntryEntity(
                    id = 4L,
                    groupId = "g1",
                    member = "Sipho D.",
                    amount = -500.0,
                    date = "2026-09-04",
                    reportedBy = "Thabo T.",
                    status = "active",
                    correctsId = 3L,
                    reason = "Reversal: Typo in original amount",
                    type = "ADJUSTMENT",
                    note = "Reversal entry"
                ),
                LedgerEntryEntity(
                    id = 5L,
                    groupId = "g1",
                    member = "Sipho D.",
                    amount = 300.0,
                    date = "2026-09-04",
                    reportedBy = "Thabo T.",
                    status = "active",
                    correctsId = 3L,
                    reason = "Correction",
                    type = "CONTRIBUTION",
                    note = "Corrected contribution amount"
                )
            )
            dao.insertLedgerEntries(initialLedger)

            // Initial Loans
            val initialLoans = listOf(
                LoanEntity(
                    id = "l1",
                    groupId = "g2",
                    member = "Thabo M.",
                    amount = 1000.0,
                    purpose = "Emergency",
                    date = "2026-09-05",
                    amountRepaid = 0.0,
                    status = "active"
                )
            )
            dao.insertLoans(initialLoans)
        }
    }
}
