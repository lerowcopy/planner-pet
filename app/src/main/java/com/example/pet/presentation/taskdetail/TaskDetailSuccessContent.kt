package com.example.pet.presentation.taskdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.pet.domain.model.Task

@Composable
fun TaskDetailSuccessContent(
    task: Task,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val dayStatus = remember(task.day) { getTaskDayStatus(task.day) }
    val formattedDay = remember(task.day) { formatTaskDay(task.day) }
    val isOverdue = dayStatus == TaskDayStatus.OVERDUE && !task.isCompleted

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) {
                                    Icons.Rounded.CheckCircle
                                } else {
                                    Icons.Rounded.RadioButtonUnchecked
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = if (task.isCompleted) "Выполнена" else "В работе",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                if (!task.description.isNullOrBlank()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = when {
                isOverdue                           -> MaterialTheme.colorScheme.errorContainer
                dayStatus == TaskDayStatus.TODAY    -> MaterialTheme.colorScheme.primaryContainer
                dayStatus == TaskDayStatus.TOMORROW -> MaterialTheme.colorScheme.secondaryContainer
                else                                -> MaterialTheme.colorScheme.surfaceContainerHigh
            },
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Срок выполнения",
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            isOverdue                           -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            dayStatus == TaskDayStatus.TODAY    -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            dayStatus == TaskDayStatus.TOMORROW -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            else                                -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = if (isOverdue) "$formattedDay · просрочено" else formattedDay,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            isOverdue                           -> MaterialTheme.colorScheme.onErrorContainer
                            dayStatus == TaskDayStatus.TODAY    -> MaterialTheme.colorScheme.onPrimaryContainer
                            dayStatus == TaskDayStatus.TOMORROW -> MaterialTheme.colorScheme.onSecondaryContainer
                            else                                -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                if (isOverdue) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Text(
            text = "ID: ${task.id}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}