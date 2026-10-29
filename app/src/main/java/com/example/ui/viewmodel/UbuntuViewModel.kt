package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UbuntuDatabase
import com.example.data.model.EntryStatus
import com.example.data.model.EntryType
import com.example.data.model.Group
import com.example.data.model.GroupType
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.Member
import com.example.data.model.UserRole
import com.example.data.repository.UbuntuRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UbuntuUiState(
    val groups: List<Group> = emptyList(),
    val currentGroupId: String = "g1",
    val currentGroup: Group? = null,
    val currentRole: UserRole = UserRole.TREASURER,
    val selectedMemberName: String = "Lerato K.",
    val members: List<Member> = emptyList(),
    val ledgerEntries: List<LedgerEntry> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val activeTab: Int = 0,
    val searchQuery: String = "",
    val selectedFilterType: EntryType? = null,
    val netBalance: Double = 0.0,
    val totalLoansOutstanding: Double = 0.0,
    val totalContributions: Double = 0.0,
    val memberPersonalTotal: Double = 0.0,
    val memberPersonalEntries: List<LedgerEntry> = emptyList(),
    val memberPersonalLoans: List<Loan> = emptyList()
)

class UbuntuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UbuntuRepository

    init {
        val db = UbuntuDatabase.getDatabase(application, viewModelScope)
        repository = UbuntuRepository(db.ubuntuDao())
    }

    private val _currentGroupId = MutableStateFlow("g1")
    private val _currentRole = MutableStateFlow(UserRole.TREASURER)
    private val _selectedMemberName = MutableStateFlow("Lerato K.")
    private val _activeTab = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilterType = MutableStateFlow<EntryType?>(null)

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val groups: StateFlow<List<Group>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val members: StateFlow<List<Member>> = _currentGroupId
        .flatMapLatest { groupId -> repository.getMembersByGroup(groupId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val ledgerEntries: StateFlow<List<LedgerEntry>> = _currentGroupId
        .flatMapLatest { groupId -> repository.getLedgerEntriesByGroup(groupId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val loans: StateFlow<List<Loan>> = _currentGroupId
        .flatMapLatest { groupId -> repository.getLoansByGroup(groupId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class ContextConfig(
        val groups: List<Group>,
        val currentGroupId: String,
        val currentRole: UserRole,
        val selectedMemberName: String
    )

    private val contextConfig = combine(
        groups,
        _currentGroupId,
        _currentRole,
        _selectedMemberName
    ) { groupList, curGroupId, role, selMember ->
        ContextConfig(groupList, curGroupId, role, selMember)
    }

    private data class FilterConfig(
        val activeTab: Int,
        val searchQuery: String,
        val selectedFilterType: EntryType?
    )

    private val filterConfig = combine(
        _activeTab,
        _searchQuery,
        _selectedFilterType
    ) { tab, query, filterType ->
        FilterConfig(tab, query, filterType)
    }

    // Combined UiState
    val uiState: StateFlow<UbuntuUiState> = combine(
        contextConfig,
        filterConfig,
        members,
        ledgerEntries,
        loans
    ) { config, filter, memberList, entries, loanList ->
        val groupList = config.groups
        val curGroupId = config.currentGroupId
        val role = config.currentRole
        val selMember = config.selectedMemberName
        val curGroup = groupList.find { it.id == curGroupId } ?: groupList.firstOrNull()

        // Calculate Net Recorded Balance: ignore superseded entries
        val activeEntries = entries.filter { it.status == EntryStatus.ACTIVE }
        val net = activeEntries.sumOf { it.amount }

        val totalContrib = activeEntries
            .filter { it.type == EntryType.CONTRIBUTION && it.amount > 0 }
            .sumOf { it.amount }

        val activeLoans = loanList.filter { it.status == "active" }
        val loansOutstanding = activeLoans.sumOf { (it.amount - it.amountRepaid).coerceAtLeast(0.0) }

        // Member's personal data
        val myEntries = activeEntries.filter { it.member == selMember }
        val myTotal = myEntries.sumOf { it.amount }
        val myLoans = loanList.filter { it.member == selMember }

        UbuntuUiState(
            groups = groupList,
            currentGroupId = curGroupId,
            currentGroup = curGroup,
            currentRole = role,
            selectedMemberName = selMember,
            members = memberList,
            ledgerEntries = entries,
            loans = loanList,
            activeTab = filter.activeTab,
            searchQuery = filter.searchQuery,
            selectedFilterType = filter.selectedFilterType,
            netBalance = net,
            totalLoansOutstanding = loansOutstanding,
            totalContributions = totalContrib,
            memberPersonalTotal = myTotal,
            memberPersonalEntries = myEntries,
            memberPersonalLoans = myLoans
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UbuntuUiState()
    )

    fun switchGroup(groupId: String) {
        _currentGroupId.value = groupId
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun selectMember(memberName: String) {
        _selectedMemberName.value = memberName
    }

    fun setTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: EntryType?) {
        _selectedFilterType.value = type
    }

    private fun getCurrentDateFormatted(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun createGroup(name: String, type: GroupType, description: String = "") {
        viewModelScope.launch {
            if (name.isBlank()) {
                _userMessage.emit("Please enter a group name")
                return@launch
            }
            val newGroup = repository.createGroup(name.trim(), type, description)
            _currentGroupId.value = newGroup.id
            _userMessage.emit("✓ Group \"${newGroup.name}\" created successfully!")
        }
    }

    fun inviteMember(name: String, phone: String, role: UserRole) {
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank()) {
                _userMessage.emit("Name and phone number are required")
                return@launch
            }
            val newMember = repository.inviteMember(_currentGroupId.value, name.trim(), phone.trim(), role)
            _userMessage.emit("✓ ${newMember.name} added as ${role.badgeText}!")
        }
    }

    fun recordContribution(member: String, amount: Double, date: String, note: String?) {
        viewModelScope.launch {
            if (amount <= 0.0 || member.isBlank()) {
                _userMessage.emit("Valid amount and member required")
                return@launch
            }
            val recordDate = if (date.isBlank()) getCurrentDateFormatted() else date
            val entry = repository.recordContribution(
                groupId = _currentGroupId.value,
                member = member,
                amount = amount,
                date = recordDate,
                reportedBy = "Treasurer",
                note = note
            )
            _userMessage.emit("✓ Contribution #$entry.id of R ${String.format(Locale.US, "%.2f", amount)} saved to ledger!")
        }
    }

    fun recordLoan(member: String, amount: Double, purpose: String, date: String) {
        viewModelScope.launch {
            if (amount <= 0.0 || member.isBlank()) {
                _userMessage.emit("Valid amount and borrower required")
                return@launch
            }
            val loanDate = if (date.isBlank()) getCurrentDateFormatted() else date
            repository.recordLoan(
                groupId = _currentGroupId.value,
                member = member,
                amount = amount,
                purpose = purpose.ifBlank { "Emergency assist" },
                date = loanDate
            )
            _userMessage.emit("✓ Community Assist Loan of R ${String.format(Locale.US, "%.2f", amount)} issued to $member!")
        }
    }

    fun recordLoanRepayment(loanId: String, amount: Double, date: String) {
        viewModelScope.launch {
            if (amount <= 0.0) {
                _userMessage.emit("Valid repayment amount required")
                return@launch
            }
            val repDate = if (date.isBlank()) getCurrentDateFormatted() else date
            val success = repository.recordLoanRepayment(
                loanId = loanId,
                amount = amount,
                date = repDate,
                reportedBy = "Treasurer"
            )
            if (success) {
                _userMessage.emit("✓ Repayment of R ${String.format(Locale.US, "%.2f", amount)} recorded and logged to ledger!")
            } else {
                _userMessage.emit("Failed to record repayment")
            }
        }
    }

    fun correctLedgerEntry(entryId: Long, newAmount: Double, reason: String) {
        viewModelScope.launch {
            if (newAmount <= 0.0 || reason.isBlank()) {
                _userMessage.emit("Correction requires amount and audit reason")
                return@launch
            }
            val date = getCurrentDateFormatted()
            val success = repository.correctLedgerEntry(
                entryId = entryId,
                newAmount = newAmount,
                reason = reason.trim(),
                date = date,
                reportedBy = "Treasurer"
            )
            if (success) {
                _userMessage.emit("✓ Correction applied! Reversal & corrected entries logged.")
            } else {
                _userMessage.emit("Unable to correct this entry (already superseded or not found)")
            }
        }
    }

    fun shareLedgerSummary(context: Context) {
        val state = uiState.value
        val groupName = state.currentGroup?.name ?: "Ubuntu Finance Society"
        val balanceFormatted = String.format(Locale.US, "%.2f", state.netBalance)
        val sb = StringBuilder()
        sb.append("📋 UBUNTU FINANCE AUDIT SUMMARY\n")
        sb.append("Society: $groupName\n")
        sb.append("Type: ${state.currentGroup?.type?.displayName ?: "Stokvel"}\n")
        sb.append("Net Recorded Balance: R $balanceFormatted\n")
        sb.append("Total Contributions: R ${String.format(Locale.US, "%.2f", state.totalContributions)}\n")
        sb.append("Active Members: ${state.members.size}\n")
        sb.append("Date: ${getCurrentDateFormatted()}\n\n")
        sb.append("--- RECENT LEDGER ENTRIES ---\n")
        state.ledgerEntries.take(15).forEach { entry ->
            val statusTag = if (entry.status == EntryStatus.SUPERSEDED) " [SUPERSEDED]" else ""
            val corrTag = if (entry.correctsId != null) " (Ref #${entry.correctsId}: ${entry.reason})" else ""
            val amountFormatted = String.format(Locale.US, "%.2f", entry.amount)
            sb.append("#${entry.id} | ${entry.date} | ${entry.member} | R $amountFormatted | ${entry.type.displayName}$statusTag$corrTag\n")
        }
        sb.append("\n*The group keeps the money. We keep the record.")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$groupName - Financial Ledger Audit")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Share Ledger Summary"))
    }
}
