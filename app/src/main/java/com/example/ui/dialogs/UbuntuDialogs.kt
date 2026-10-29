package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroupType
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.Member
import com.example.data.model.UserRole
import com.example.ui.theme.UbuntuGold
import com.example.ui.theme.UbuntuTeal
import com.example.ui.theme.UbuntuTerracotta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordContributionDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (member: String, amount: Double, date: String, note: String?) -> Unit
) {
    var selectedMember by remember { mutableStateOf(members.firstOrNull()?.name ?: "") }
    var memberMenuExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var dateText by remember { mutableStateOf(todayDate) }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Contribution",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = UbuntuTerracotta
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Log a verified member deposit into the immutable society ledger.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Member Dropdown
                Text(
                    text = "Member",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = selectedMember.ifBlank { "Select Member" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { memberMenuExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { memberMenuExpanded = true }
                            .testTag("contribution_member_field")
                    )
                    DropdownMenu(
                        expanded = memberMenuExpanded,
                        onDismissRequest = { memberMenuExpanded = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.name} (${m.role.badgeText})") },
                                onClick = {
                                    selectedMember = m.name
                                    memberMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount (ZAR)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (ZAR)") },
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contribution_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contribution_date_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Reference / Note (Optional)") },
                    placeholder = { Text("e.g. September Stokvel cycle") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (selectedMember.isNotBlank() && amount > 0.0) {
                        onConfirm(selectedMember, amount, dateText, noteText.ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuTerracotta),
                modifier = Modifier.testTag("submit_contribution_button")
            ) {
                Text("Save to Ledger")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecordLoanDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (borrower: String, amount: Double, purpose: String, date: String) -> Unit
) {
    var selectedBorrower by remember { mutableStateOf(members.firstOrNull()?.name ?: "") }
    var borrowerMenuExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var purposeText by remember { mutableStateOf("") }
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var dateText by remember { mutableStateOf(todayDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Community Assist (Issue Loan)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = UbuntuTeal
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Record loans issued to society members for emergencies or education. Repayments will be recorded directly in the ledger.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Borrower
                Text(
                    text = "Borrower",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = selectedBorrower.ifBlank { "Select Borrower" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { borrowerMenuExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { borrowerMenuExpanded = true }
                            .testTag("loan_borrower_field")
                    )
                    DropdownMenu(
                        expanded = borrowerMenuExpanded,
                        onDismissRequest = { borrowerMenuExpanded = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = {
                                    selectedBorrower = m.name
                                    borrowerMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Loan Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Loan Amount (ZAR)") },
                    placeholder = { Text("e.g. 1000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Purpose
                OutlinedTextField(
                    value = purposeText,
                    onValueChange = { purposeText = it },
                    label = { Text("Purpose") },
                    placeholder = { Text("e.g. School fees, Emergency medical") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_purpose_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date Issued (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (selectedBorrower.isNotBlank() && amount > 0.0) {
                        onConfirm(selectedBorrower, amount, purposeText.ifBlank { "Mutual aid" }, dateText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuTeal),
                modifier = Modifier.testTag("submit_loan_button")
            ) {
                Text("Record Loan Issued")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecordRepaymentDialog(
    loan: Loan,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, date: String) -> Unit
) {
    val remaining = (loan.amount - loan.amountRepaid).coerceAtLeast(0.0)
    var amountText by remember { mutableStateOf(remaining.toString()) }
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var dateText by remember { mutableStateOf(todayDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Loan Repayment",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = UbuntuTeal
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Borrower: ${loan.member}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Purpose: ${loan.purpose}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Remaining Balance: R ${String.format(Locale.US, "%.2f", remaining)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = UbuntuTerracotta,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Repayment Amount (ZAR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repayment_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) {
                        onConfirm(amount, dateText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuTeal)
            ) {
                Text("Confirm Repayment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CorrectionModalDialog(
    entry: LedgerEntry,
    onDismiss: () -> Unit,
    onSubmitCorrection: (newAmount: Double, reason: String) -> Unit
) {
    var newAmountText by remember { mutableStateOf(entry.amount.toString()) }
    var reasonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = UbuntuGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Correct Ledger Entry",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = UbuntuGold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Original entries cannot be deleted or edited. This marks entry #${entry.id} as superseded and posts an audit reversal entry plus the corrected amount.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6E4E00),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Original: ${entry.member} • R ${String.format(Locale.US, "%.2f", entry.amount)} (${entry.date})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newAmountText,
                    onValueChange = { newAmountText = it },
                    label = { Text("Correct Amount (ZAR)") },
                    placeholder = { Text("e.g. 300") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("correct_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason (required)") },
                    placeholder = { Text("e.g. Typo in original receipt, bank slip verified") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("correct_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newAmount = newAmountText.toDoubleOrNull() ?: 0.0
                    if (newAmount > 0.0 && reasonText.isNotBlank()) {
                        onSubmitCorrection(newAmount, reasonText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuTerracotta),
                modifier = Modifier.testTag("submit_correction_button")
            ) {
                Text("Submit Correction")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: GroupType, description: String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GroupType.STOKVEL) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Society",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = UbuntuTerracotta
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Establish a new Stokvel savings circle, savings club, or community mutual assist lending pool.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group / Society Name") },
                    placeholder = { Text("e.g. Khayelitsha Savings Club") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_group_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Group Type",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { typeMenuExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeMenuExpanded = true }
                            .testTag("new_group_type_field")
                    )
                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        GroupType.values().forEach { gt ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(gt.displayName, fontWeight = FontWeight.SemiBold)
                                        Text(gt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedType = gt
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Rules (Optional)") },
                    placeholder = { Text("e.g. Monthly contribution of R500 on 1st of month") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        onConfirm(groupName, selectedType, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuTerracotta),
                modifier = Modifier.testTag("confirm_create_group_button")
            ) {
                Text("Create Group")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InviteMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, role: UserRole) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+27 ") }
    var selectedRole by remember { mutableStateOf(UserRole.MEMBER) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Invite New Member",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = UbuntuTerracotta
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add a verified participant to the current society.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Nandi Z.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+27 82 555 0103") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Assign Role",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = selectedRole.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { roleMenuExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { roleMenuExpanded = true }
                            .testTag("invite_role_field")
                    )
                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        listOf(UserRole.MEMBER, UserRole.TREASURER).forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.displayName) },
                                onClick = {
                                    selectedRole = r
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, selectedRole)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuTerracotta),
                modifier = Modifier.testTag("confirm_invite_button")
            ) {
                Text("Send Invite")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
