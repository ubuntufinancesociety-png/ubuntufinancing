package com.example.data.model

enum class GroupType(val displayName: String, val description: String) {
    STOKVEL("Savings Circle (Stokvel)", "Rotating or pooled regular savings"),
    SAVINGS_CLUB("Savings Club", "Targeted collective accumulation"),
    LENDING_POOL("Community Assist (Lending Pool)", "Mutual aid and emergency lending")
}

enum class UserRole(val displayName: String, val badgeText: String) {
    ADMINISTRATOR("Administrator", "Admin"),
    TREASURER("Treasurer", "Treasurer"),
    MEMBER("Member (Read-Only)", "Member")
}

enum class EntryType(val displayName: String) {
    CONTRIBUTION("Contribution"),
    ADJUSTMENT("Adjustment / Reversal"),
    LOAN_DISBURSEMENT("Loan Disbursed"),
    LOAN_REPAYMENT("Loan Repayment")
}

enum class EntryStatus {
    ACTIVE,
    SUPERSEDED
}

data class Group(
    val id: String,
    val name: String,
    val type: GroupType,
    val description: String = "",
    val currency: String = "ZAR"
)

data class Member(
    val id: String,
    val groupId: String,
    val name: String,
    val phone: String,
    val role: UserRole,
    val status: String = "active"
)

data class LedgerEntry(
    val id: Long,
    val groupId: String,
    val member: String,
    val amount: Double,
    val date: String,
    val reportedBy: String = "Treasurer",
    val status: EntryStatus = EntryStatus.ACTIVE,
    val correctsId: Long? = null,
    val reason: String? = null,
    val type: EntryType = EntryType.CONTRIBUTION,
    val note: String? = null
)

data class Loan(
    val id: String,
    val groupId: String,
    val member: String,
    val amount: Double,
    val purpose: String,
    val date: String,
    val amountRepaid: Double = 0.0,
    val status: String = "active"
)
