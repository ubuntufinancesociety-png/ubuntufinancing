package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EntryStatus
import com.example.data.model.EntryType
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.UserRole
import com.example.ui.components.BalanceCard
import com.example.ui.components.LedgerItemRow
import com.example.ui.components.LoanItemCard
import com.example.ui.components.MemberItemRow
import com.example.ui.components.UbuntuTopControls
import com.example.ui.dialogs.CorrectionModalDialog
import com.example.ui.dialogs.CreateGroupDialog
import com.example.ui.dialogs.InviteMemberDialog
import com.example.ui.dialogs.RecordContributionDialog
import com.example.ui.dialogs.RecordLoanDialog
import com.example.ui.dialogs.RecordRepaymentDialog
import com.example.ui.theme.UbuntuGold
import com.example.ui.theme.UbuntuNegative
import com.example.ui.theme.UbuntuTeal
import com.example.ui.theme.UbuntuTerracotta
import com.example.ui.viewmodel.UbuntuViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: UbuntuViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog Visibility States
    var showRecordContributionDialog by remember { mutableStateOf(false) }
    var showRecordLoanDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showInviteMemberDialog by remember { mutableStateOf(false) }
    var entryToCorrect by remember { mutableStateOf<LedgerEntry?>(null) }
    var loanToRepay by remember { mutableStateOf<Loan?>(null) }

    // Listen to ViewModel user messages for SnackBar
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = UbuntuTerracotta
            ) {
                val navItems = listOf(
                    Triple(0, "Overview", Icons.Default.Dashboard),
                    Triple(1, "Ledger", Icons.Default.HistoryEdu),
                    Triple(2, "Loans", Icons.Default.Handshake),
                    Triple(3, "Members", Icons.Default.People)
                )
                navItems.forEach { (index, title, icon) ->
                    val isSelected = uiState.activeTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(index) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title
                            )
                        },
                        label = { Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UbuntuTerracotta,
                            selectedTextColor = UbuntuTerracotta,
                            indicatorColor = UbuntuTerracotta.copy(alpha = 0.18f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_$index")
                    )
                }
            }
        },
        floatingActionButton = {
            when {
                uiState.currentRole == UserRole.TREASURER && (uiState.activeTab == 0 || uiState.activeTab == 1) -> {
                    ExtendedFloatingActionButton(
                        onClick = { showRecordContributionDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Record Contribution", fontWeight = FontWeight.Bold) },
                        containerColor = UbuntuTerracotta,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_record_contribution")
                    )
                }
                uiState.activeTab == 2 && uiState.currentRole == UserRole.TREASURER -> {
                    ExtendedFloatingActionButton(
                        onClick = { showRecordLoanDialog = true },
                        icon = { Icon(Icons.Default.Handshake, contentDescription = null) },
                        text = { Text("Issue Loan", fontWeight = FontWeight.Bold) },
                        containerColor = UbuntuTeal,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_issue_loan")
                    )
                }
                uiState.activeTab == 3 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showInviteMemberDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Invite Member", fontWeight = FontWeight.Bold) },
                        containerColor = UbuntuTerracotta,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_invite_member")
                    )
                }
                uiState.currentRole == UserRole.ADMINISTRATOR && uiState.activeTab == 0 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showCreateGroupDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Create Society", fontWeight = FontWeight.Bold) },
                        containerColor = UbuntuTerracotta,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_create_society")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Persistent Ubuntu Top Controls (Header, Group switcher, Role switcher)
            UbuntuTopControls(
                groups = uiState.groups,
                currentGroup = uiState.currentGroup,
                currentRole = uiState.currentRole,
                onSelectGroup = { viewModel.switchGroup(it) },
                onSelectRole = { viewModel.switchRole(it) },
                onCreateGroupClick = { showCreateGroupDialog = true },
                onShareAuditClick = { viewModel.shareLedgerSummary(context) }
            )

            // Main Content Area based on activeTab
            when (uiState.activeTab) {
                0 -> OverviewTabContent(
                    uiState = uiState,
                    onRecordContributionClick = { showRecordContributionDialog = true },
                    onRecordLoanClick = { showRecordLoanDialog = true },
                    onCreateGroupClick = { showCreateGroupDialog = true },
                    onInviteMemberClick = { showInviteMemberDialog = true },
                    onSelectMember = { viewModel.selectMember(it) },
                    onCorrectClick = { entryToCorrect = it },
                    onViewLedgerTab = { viewModel.setTab(1) },
                    onViewLoansTab = { viewModel.setTab(2) }
                )
                1 -> LedgerTabContent(
                    uiState = uiState,
                    onCorrectClick = { entryToCorrect = it },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onFilterTypeChange = { viewModel.setFilterType(it) }
                )
                2 -> LoansTabContent(
                    uiState = uiState,
                    onIssueLoanClick = { showRecordLoanDialog = true },
                    onRepayClick = { loanToRepay = it }
                )
                3 -> MembersTabContent(
                    uiState = uiState,
                    onInviteMemberClick = { showInviteMemberDialog = true }
                )
            }
        }
    }

    // Dialogs
    if (showRecordContributionDialog) {
        RecordContributionDialog(
            members = uiState.members,
            onDismiss = { showRecordContributionDialog = false },
            onConfirm = { member, amount, date, note ->
                viewModel.recordContribution(member, amount, date, note)
                showRecordContributionDialog = false
            }
        )
    }

    if (showRecordLoanDialog) {
        RecordLoanDialog(
            members = uiState.members,
            onDismiss = { showRecordLoanDialog = false },
            onConfirm = { borrower, amount, purpose, date ->
                viewModel.recordLoan(borrower, amount, purpose, date)
                showRecordLoanDialog = false
            }
        )
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name, type, description ->
                viewModel.createGroup(name, type, description)
                showCreateGroupDialog = false
            }
        )
    }

    if (showInviteMemberDialog) {
        InviteMemberDialog(
            onDismiss = { showInviteMemberDialog = false },
            onConfirm = { name, phone, role ->
                viewModel.inviteMember(name, phone, role)
                showInviteMemberDialog = false
            }
        )
    }

    entryToCorrect?.let { entry ->
        CorrectionModalDialog(
            entry = entry,
            onDismiss = { entryToCorrect = null },
            onSubmitCorrection = { newAmount, reason ->
                viewModel.correctLedgerEntry(entry.id, newAmount, reason)
                entryToCorrect = null
            }
        )
    }

    loanToRepay?.let { loan ->
        RecordRepaymentDialog(
            loan = loan,
            onDismiss = { loanToRepay = null },
            onConfirm = { amount, date ->
                viewModel.recordLoanRepayment(loan.id, amount, date)
                loanToRepay = null
            }
        )
    }
}

// ---------------- OVERVIEW TAB ----------------

@Composable
fun OverviewTabContent(
    uiState: com.example.ui.viewmodel.UbuntuUiState,
    onRecordContributionClick: () -> Unit,
    onRecordLoanClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onInviteMemberClick: () -> Unit,
    onSelectMember: (String) -> Unit,
    onCorrectClick: (LedgerEntry) -> Unit,
    onViewLedgerTab: () -> Unit,
    onViewLoansTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Balance Hero Card (visible for Treasurer and Admin)
        if (uiState.currentRole != UserRole.MEMBER) {
            item {
                BalanceCard(
                    netBalance = uiState.netBalance,
                    currentGroup = uiState.currentGroup,
                    memberCount = uiState.members.size,
                    outstandingLoans = uiState.totalLoansOutstanding
                )
            }
        }

        // ROLE-SPECIFIC SECTIONS:

        // 1. ADMIN ACTIONS VIEW
        if (uiState.currentRole == UserRole.ADMINISTRATOR) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, UbuntuTerracotta.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_action_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = UbuntuTerracotta,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Administrator Actions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create new societies, configure governance, and manage membership rolls.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onCreateGroupClick,
                                colors = ButtonDefaults.buttonColors(containerColor = UbuntuTerracotta),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ New Society", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onInviteMemberClick,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ Invite Member", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // 2. TREASURER ACTIONS VIEW
        if (uiState.currentRole == UserRole.TREASURER) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Record Contribution Button
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = UbuntuTerracotta),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onRecordContributionClick() }
                            .testTag("action_record_contribution")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Record Contribution",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Add to ledger",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Issue Assist Loan Button
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = UbuntuTeal),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onRecordLoanClick() }
                            .testTag("action_issue_loan")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Community Assist",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Issue loan to member",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // 3. MEMBER (READ-ONLY) VIEW
        if (uiState.currentRole == UserRole.MEMBER) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_personal_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Contribution History",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = UbuntuTerracotta
                            )

                            // Member profile selector
                            var profileMenuExpanded by remember { mutableStateOf(false) }
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { profileMenuExpanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = uiState.selectedMemberName,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = profileMenuExpanded,
                                    onDismissRequest = { profileMenuExpanded = false }
                                ) {
                                    uiState.members.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m.name) },
                                            onClick = {
                                                onSelectMember(m.name)
                                                profileMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "View-only access. Contact your Treasurer to record or correct entries.",
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Personal Net Contributed Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = UbuntuTeal.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "My Net Contributions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "R ${String.format(Locale.US, "%,.2f", uiState.memberPersonalTotal)}",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = UbuntuTeal
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = UbuntuTeal,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "Active Member",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Personal Entries List
                        Text(
                            text = "Personal Record",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (uiState.memberPersonalEntries.isEmpty()) {
                            Text(
                                text = "No contributions recorded yet for this member.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            uiState.memberPersonalEntries.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${entry.date} • ID #${entry.id}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!entry.reason.isNullOrBlank()) {
                                            Text(
                                                text = entry.reason,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = UbuntuGold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "R ${String.format(Locale.US, "%.2f", entry.amount)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (entry.amount >= 0) UbuntuTeal else UbuntuNegative
                                    )
                                }
                            }
                        }

                        // Personal Loans if any
                        if (uiState.memberPersonalLoans.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "My Community Assist Loans",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            uiState.memberPersonalLoans.forEach { loan ->
                                LoanItemCard(
                                    loan = loan,
                                    currentRole = UserRole.MEMBER,
                                    onRepayClick = {}
                                )
                            }
                        }
                    }
                }
            }
        }

        // RECENT LEDGER PREVIEW SECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Contribution Ledger",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Append-only record. Corrections create linked entries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onViewLedgerTab,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View All", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Show first 5 recent ledger entries
        val previewEntries = uiState.ledgerEntries.take(5)
        if (previewEntries.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No records yet in this society ledger.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(previewEntries, key = { it.id }) { entry ->
                LedgerItemRow(
                    entry = entry,
                    currentRole = uiState.currentRole,
                    onCorrectClick = onCorrectClick
                )
            }
        }
    }
}

// ---------------- LEDGER TAB ----------------

@Composable
fun LedgerTabContent(
    uiState: com.example.ui.viewmodel.UbuntuUiState,
    onCorrectClick: (LedgerEntry) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterTypeChange: (EntryType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredEntries = uiState.ledgerEntries.filter { entry ->
        val matchesQuery = uiState.searchQuery.isBlank() ||
                entry.member.contains(uiState.searchQuery, ignoreCase = true) ||
                entry.id.toString() == uiState.searchQuery ||
                (entry.reason ?: "").contains(uiState.searchQuery, ignoreCase = true)
        val matchesType = uiState.selectedFilterType == null || entry.type == uiState.selectedFilterType
        matchesQuery && matchesType
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📜 Immutable Society Ledger",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = UbuntuTerracotta
                    )
                    Text(
                        text = "Financial entries are append-only. Corrections post a reversal entry and a linked corrected entry to guarantee audit transparency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Field
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search by member, ID, or reason...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ledger_search_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedFilterType == null,
                            onClick = { onFilterTypeChange(null) },
                            label = { Text("All (${uiState.ledgerEntries.size})") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UbuntuTerracotta.copy(alpha = 0.2f))
                        )
                        FilterChip(
                            selected = uiState.selectedFilterType == EntryType.CONTRIBUTION,
                            onClick = { onFilterTypeChange(EntryType.CONTRIBUTION) },
                            label = { Text("Contributions") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UbuntuTeal.copy(alpha = 0.2f))
                        )
                        FilterChip(
                            selected = uiState.selectedFilterType == EntryType.ADJUSTMENT,
                            onClick = { onFilterTypeChange(EntryType.ADJUSTMENT) },
                            label = { Text("Reversals") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UbuntuNegative.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }

        if (filteredEntries.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "No records match \"${uiState.searchQuery}\"" else "No ledger records found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredEntries, key = { it.id }) { entry ->
                LedgerItemRow(
                    entry = entry,
                    currentRole = uiState.currentRole,
                    onCorrectClick = onCorrectClick
                )
            }
        }
    }
}

// ---------------- LOANS TAB ----------------

@Composable
fun LoansTabContent(
    uiState: com.example.ui.viewmodel.UbuntuUiState,
    onIssueLoanClick: () -> Unit,
    onRepayClick: (Loan) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🤝 Community Assist (Loans)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = UbuntuTeal
                            )
                            Text(
                                text = "Emergency & mutual assist pool for society members.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (uiState.currentRole == UserRole.TREASURER) {
                            Button(
                                onClick = onIssueLoanClick,
                                colors = ButtonDefaults.buttonColors(containerColor = UbuntuTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("issue_loan_button")
                            ) {
                                Text("+ Issue Loan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = UbuntuTeal.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Total Active Loans",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "R ${String.format(Locale.US, "%,.2f", uiState.totalLoansOutstanding)}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = UbuntuTeal
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Total Assisted",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${uiState.loans.size} Members",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.loans.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No loans currently recorded for this society.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.loans, key = { it.id }) { loan ->
                LoanItemCard(
                    loan = loan,
                    currentRole = uiState.currentRole,
                    onRepayClick = onRepayClick
                )
            }
        }
    }
}

// ---------------- MEMBERS TAB ----------------

@Composable
fun MembersTabContent(
    uiState: com.example.ui.viewmodel.UbuntuUiState,
    onInviteMemberClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "👥 Society Members (${uiState.members.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = UbuntuTerracotta
                        )
                        Text(
                            text = "Active participants in ${uiState.currentGroup?.name ?: "Society"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onInviteMemberClick,
                        colors = ButtonDefaults.buttonColors(containerColor = UbuntuTerracotta),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("invite_member_button")
                    ) {
                        Text("+ Invite", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (uiState.members.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No members yet. Tap '+ Invite' to add participants.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.members, key = { it.id }) { member ->
                // Calculate member's total contribution in this group
                val memberTotal = uiState.ledgerEntries
                    .filter { it.member == member.name && it.status == EntryStatus.ACTIVE && it.type == EntryType.CONTRIBUTION }
                    .sumOf { it.amount }

                MemberItemRow(
                    member = member,
                    totalContributed = memberTotal
                )
            }
        }
    }
}
