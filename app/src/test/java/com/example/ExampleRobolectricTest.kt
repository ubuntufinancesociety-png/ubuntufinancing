package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EntryStatus
import com.example.data.model.EntryType
import com.example.data.model.LedgerEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ubuntu Finance", appName)
  }

  @Test
  fun `test ledger balance excludes superseded entries`() {
    val entries = listOf(
      LedgerEntry(1, "g1", "Thabo M.", 500.0, "2026-09-01", status = EntryStatus.ACTIVE),
      LedgerEntry(2, "g1", "Lerato K.", 200.0, "2026-09-02", status = EntryStatus.ACTIVE),
      LedgerEntry(3, "g1", "Sipho D.", 500.0, "2026-09-03", status = EntryStatus.SUPERSEDED),
      LedgerEntry(4, "g1", "Sipho D.", -500.0, "2026-09-04", status = EntryStatus.ACTIVE, type = EntryType.ADJUSTMENT),
      LedgerEntry(5, "g1", "Sipho D.", 300.0, "2026-09-04", status = EntryStatus.ACTIVE, type = EntryType.CONTRIBUTION)
    )

    // Calculate net recorded balance
    val netBalance = entries.filter { it.status == EntryStatus.ACTIVE }.sumOf { it.amount }
    // 500 + 200 + (-500) + 300 = 500.0
    assertEquals(500.0, netBalance, 0.001)
  }
}
