package com.example.pet.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pet.presentation.taskdetail.TaskDayStatus
import com.example.pet.presentation.taskdetail.formatTaskDay
import com.example.pet.presentation.taskdetail.getTaskDayStatus
import com.example.pet.ui.theme.PetTheme

/**
 * MD3-компонент задачи.
 *
 * @param title Название задачи
 * @param description Описание задачи (опционально)
 * @param day День выполнения задачи
 * @param isCompleted Статус выполнения
 * @param onCheckedChange Callback при изменении статуса
 * @param onClick Callback при нажатии
 * @param onLongClick Callback при долгом нажатии
 * @param onDeleteClick Callback при нажатии на удаление
 * @param modifier Modifier
 */
@Composable
fun TaskItem(
    title: String,
    description: String? = null,
    day: String,
    isCompleted: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onClickDeleteTaskById: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var actionsVisible by remember { mutableStateOf(false) }

    val formattedDay = remember(day) { formatTaskDay(day) }
    val dayStatus = remember(day) { getTaskDayStatus(day) }

    // Chip
    val isOverdue = dayStatus == TaskDayStatus.OVERDUE && !isCompleted

    val chipContainerColor = when {
        isOverdue                       -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        dayStatus == TaskDayStatus.TODAY    -> MaterialTheme.colorScheme.primaryContainer
        dayStatus == TaskDayStatus.TOMORROW -> MaterialTheme.colorScheme.secondaryContainer
        else                            -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val chipContentColor = when {
        isOverdue                       -> MaterialTheme.colorScheme.onErrorContainer
        dayStatus == TaskDayStatus.TODAY    -> MaterialTheme.colorScheme.onPrimaryContainer
        dayStatus == TaskDayStatus.TOMORROW -> MaterialTheme.colorScheme.onSecondaryContainer
        else                            -> MaterialTheme.colorScheme.onSurfaceVariant
    }


    Column(modifier = modifier.fillMaxWidth()) {
        // Основная карточка задачи
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    if (actionsVisible) {
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    } else {
                        RoundedCornerShape(16.dp)
                    }
                )
                .combinedClickable(
                    onClick = {
                        if (actionsVisible) actionsVisible = false
                        else onClick()
                    },
                    onLongClick = {
                        onLongClick()
                        actionsVisible = true
                    }
                ),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = if (actionsVisible) 4.dp else 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // MD3 Checkbox → заменяем на IconButton для лучшего touch target
                IconButton(
                    onClick = { onCheckedChange(!isCompleted) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.RadioButtonUnchecked
                        },
                        contentDescription = if (isCompleted) "Отметить невыполненной" else "Отметить выполненной",
                        tint = if (isCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Текстовый блок
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            isOverdue   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else        -> MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!description.isNullOrBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Chip с датой — MD3 Assist chip семантика
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = chipContainerColor,
                ) {
                    Text(
                        text = formattedDay,
                        style = MaterialTheme.typography.labelSmall,
                        color = chipContentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Action panel — появляется снизу при long press
        AnimatedVisibility(
            visible = actionsVisible,
            enter = expandVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                expandFrom = Alignment.Top
            ),
            exit = shrinkVertically(
                animationSpec = tween(150, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Top
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = {
                            actionsVisible = false
                            onClickDeleteTaskById()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(18.dp)
                        )
                        Text(
                            text = "Удалить",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBFE)
@Composable
private fun TaskItemPreview() {
    PetTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskItem(
                title = "Купить продукты",
                description = "Молоко, хлеб, яйца",
                day = "Сегодня",
                isCompleted = false
            )
            TaskItem(
                title = "Прочитать книгу",
                description = "Clean Architecture — Robert Martin",
                day = "Завтра",
                isCompleted = true
            )
            TaskItem(
                title = "Задача без описания",
                day = "Пятница",
                isCompleted = false
            )
        }
    }
}