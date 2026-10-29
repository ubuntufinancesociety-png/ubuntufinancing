package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EntryStatus
import com.example.data.model.EntryType
import com.example.data.model.LedgerEntry
import com.example.data.model.UserRole
import com.example.ui.theme.UbuntuGold
import com.example.ui.theme.UbuntuNegative
import com.example.ui.theme.UbuntuTeal
import com.example.ui.theme.UbuntuTerracotta
import java.util.Locale

@Composable
fun LedgerItemRow(
    entry: LedgerEntry,
    currentRole: UserRole,
    onCorrectClick: (LedgerEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuperseded = entry.status == EntryStatus.SUPERSEDED
    val isNegative = entry.amount < 0
    val isCorrection = entry.correctsId != null

    val rowAlpha = if (isSuperseded) 0.45f else 1.0f
    val textDecoration = if (isSuperseded) TextDecoration.LineThrough else TextDecoration.None

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSuperseded) 0.dp else 1.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(rowAlpha)
            .testTag("ledger_item_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Member name + Badges
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.member,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = textDecoration
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Badges
                        if (isSuperseded) {
                            BadgeChip(
                                text = "SUPERSEDED",
                                backgroundColor = UbuntuGold.copy(alpha = 0.25f),
                                textColor = Color(0xFF6E4E00)
                            )
                        } else if (entry.type == EntryType.ADJUSTMENT) {
                            BadgeChip(
                                text = "REVERSAL",
                                backgroundColor = UbuntuNegative.copy(alpha = 0.18f),
                                textColor = UbuntuNegative
                            )
                        } else if (isCorrection) {
                            BadgeChip(
                                text = "CORRECTED",
                                backgroundColor = UbuntuGold.copy(alpha = 0.25f),
                                textColor = Color(0xFF6E4E00)
                            )
                        } else if (entry.type == EntryType.LOAN_REPAYMENT) {
                            BadgeChip(
                                text = "REPAYMENT",
                                backgroundColor = UbuntuTeal.copy(alpha = 0.2f),
                                textColor = UbuntuTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${entry.date} • ID: #${entry.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Right Column: Amount
                Column(horizontalAlignment = Alignment.End) {
                    val amountColor = when {
                        isSuperseded -> MaterialTheme.colorScheme.onSurfaceVariant
                        isNegative -> UbuntuNegative
                        entry.type == EntryType.CONTRIBUTION || entry.type == EntryType.LOAN_REPAYMENT -> UbuntuTeal
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    val prefix = if (entry.amount < 0) "-R " else "R "
                    val formattedAmount = "$prefix${String.format(Locale.US, "%.2f", Math.abs(entry.amount))}"

                    Text(
                        text = formattedAmount,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = textDecoration
                        ),
                        color = amountColor
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = entry.type.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Reason or note if present
            if (!entry.reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = UbuntuGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = entry.reason,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Correct Button for Treasurer on active contribution items
            if (currentRole == UserRole.TREASURER && !isSuperseded && entry.type == EntryType.CONTRIBUTION) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onCorrectClick(entry) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = UbuntuTerracotta
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("correct_button_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Correct Entry",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Correct",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
