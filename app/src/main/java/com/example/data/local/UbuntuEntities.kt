package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EntryStatus
import com.example.data.model.EntryType
import com.example.data.model.Group
import com.example.data.model.GroupType
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.Member
import com.example.data.model.UserRole

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val description: String,
    val currency: String
) {
    fun toDomain(): Group = Group(
        id = id,
        name = name,
        type = try { GroupType.valueOf(type) } catch (e: Exception) { GroupType.STOKVEL },
        description = description,
        currency = currency
    )
}

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val name: String,
    val phone: String,
    val role: String,
    val status: String
) {
    fun toDomain(): Member = Member(
        id = id,
        groupId = groupId,
        name = name,
        phone = phone,
        role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.MEMBER },
        status = status
    )
}

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey val id: Long,
    val groupId: String,
    val member: String,
    val amount: Double,
    val date: String,
    val reportedBy: String,
    val status: String,
    val correctsId: Long?,
    val reason: String?,
    val type: String,
    val note: String?
) {
    fun toDomain(): LedgerEntry = LedgerEntry(
        id = id,
        groupId = groupId,
        member = member,
        amount = amount,
        date = date,
        reportedBy = reportedBy,
        status = if (status == "superseded") EntryStatus.SUPERSEDED else EntryStatus.ACTIVE,
        correctsId = correctsId,
        reason = reason,
        type = try { EntryType.valueOf(type) } catch (e: Exception) { EntryType.CONTRIBUTION },
        note = note
    )
}

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val member: String,
    val amount: Double,
    val purpose: String,
    val date: String,
    val amountRepaid: Double,
    val status: String
) {
    fun toDomain(): Loan = Loan(
        id = id,
        groupId = groupId,
        member = member,
        amount = amount,
        purpose = purpose,
        date = date,
        amountRepaid = amountRepaid,
        status = status
    )
}
