package com.example.data.repository

import com.example.data.local.GroupEntity
import com.example.data.local.LedgerEntryEntity
import com.example.data.local.LoanEntity
import com.example.data.local.MemberEntity
import com.example.data.local.UbuntuDao
import com.example.data.model.EntryStatus
import com.example.data.model.EntryType
import com.example.data.model.Group
import com.example.data.model.GroupType
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.Member
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class UbuntuRepository(private val dao: UbuntuDao) {

    val allGroups: Flow<List<Group>> = dao.getAllGroups().map { list ->
        list.map { it.toDomain() }
    }

    fun getMembersByGroup(groupId: String): Flow<List<Member>> =
        dao.getMembersByGroup(groupId).map { list ->
            list.map { it.toDomain() }
        }

    fun getLedgerEntriesByGroup(groupId: String): Flow<List<LedgerEntry>> =
        dao.getLedgerEntriesByGroup(groupId).map { list ->
            list.map { it.toDomain() }
        }

    fun getLoansByGroup(groupId: String): Flow<List<Loan>> =
        dao.getLoansByGroup(groupId).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun createGroup(name: String, type: GroupType, description: String = ""): Group {
        val newId = "g" + UUID.randomUUID().toString().take(6)
        val entity = GroupEntity(
            id = newId,
            name = name,
            type = type.name,
            description = description.ifBlank { type.description },
            currency = "ZAR"
        )
        dao.insertGroup(entity)
        return entity.toDomain()
    }

    suspend fun inviteMember(
        groupId: String,
        name: String,
        phone: String,
        role: UserRole
    ): Member {
        val newId = "m" + UUID.randomUUID().toString().take(6)
        val entity = MemberEntity(
            id = newId,
            groupId = groupId,
            name = name,
            phone = phone,
            role = role.name,
            status = "active"
        )
        dao.insertMember(entity)
        return entity.toDomain()
    }

    suspend fun recordContribution(
        groupId: String,
        member: String,
        amount: Double,
        date: String,
        reportedBy: String,
        note: String? = null
    ): LedgerEntry {
        val nextId = (dao.getMaxLedgerId() ?: 0L) + 1L
        val entity = LedgerEntryEntity(
            id = nextId,
            groupId = groupId,
            member = member,
            amount = amount,
            date = date,
            reportedBy = reportedBy,
            status = "active",
            correctsId = null,
            reason = null,
            type = EntryType.CONTRIBUTION.name,
            note = note
        )
        dao.insertLedgerEntry(entity)
        return entity.toDomain()
    }

    suspend fun recordLoan(
        groupId: String,
        member: String,
        amount: Double,
        purpose: String,
        date: String
    ): Loan {
        val loanId = "l" + UUID.randomUUID().toString().take(6)
        val entity = LoanEntity(
            id = loanId,
            groupId = groupId,
            member = member,
            amount = amount,
            purpose = purpose,
            date = date,
            amountRepaid = 0.0,
            status = "active"
        )
        dao.insertLoan(entity)
        return entity.toDomain()
    }

    suspend fun recordLoanRepayment(
        loanId: String,
        amount: Double,
        date: String,
        reportedBy: String
    ): Boolean {
        val loanEntity = dao.getLoanById(loanId) ?: return false
        val newRepaid = loanEntity.amountRepaid + amount
        val newStatus = if (newRepaid >= loanEntity.amount) "repaid" else "active"
        dao.updateLoan(
            loanEntity.copy(
                amountRepaid = newRepaid,
                status = newStatus
            )
        )

        // Log repayment in the append-only ledger
        val nextId = (dao.getMaxLedgerId() ?: 0L) + 1L
        val ledgerEntity = LedgerEntryEntity(
            id = nextId,
            groupId = loanEntity.groupId,
            member = loanEntity.member,
            amount = amount,
            date = date,
            reportedBy = reportedBy,
            status = "active",
            correctsId = null,
            reason = "Loan Repayment (#$loanId)",
            type = EntryType.LOAN_REPAYMENT.name,
            note = "Repayment towards: ${loanEntity.purpose}"
        )
        dao.insertLedgerEntry(ledgerEntity)
        return true
    }

    suspend fun correctLedgerEntry(
        entryId: Long,
        newAmount: Double,
        reason: String,
        date: String,
        reportedBy: String
    ): Boolean {
        val original = dao.getLedgerEntryById(entryId) ?: return false
        if (original.status == "superseded") return false

        // 1. Mark original as superseded
        dao.updateLedgerEntry(original.copy(status = "superseded"))

        var currentMaxId = dao.getMaxLedgerId() ?: 0L

        // 2. Insert reversal entry
        val reversalId = ++currentMaxId
        val reversalEntry = LedgerEntryEntity(
            id = reversalId,
            groupId = original.groupId,
            member = original.member,
            amount = -original.amount,
            date = date,
            reportedBy = reportedBy,
            status = "active",
            correctsId = entryId,
            reason = "Reversal: $reason",
            type = EntryType.ADJUSTMENT.name,
            note = "Reversal of entry #$entryId"
        )
        dao.insertLedgerEntry(reversalEntry)

        // 3. Insert corrected entry
        val correctedId = ++currentMaxId
        val correctedEntry = LedgerEntryEntity(
            id = correctedId,
            groupId = original.groupId,
            member = original.member,
            amount = newAmount,
            date = date,
            reportedBy = reportedBy,
            status = "active",
            correctsId = entryId,
            reason = "Correction: $reason",
            type = EntryType.CONTRIBUTION.name,
            note = "Corrected replacement for entry #$entryId"
        )
        dao.insertLedgerEntry(correctedEntry)

        return true
    }
}
