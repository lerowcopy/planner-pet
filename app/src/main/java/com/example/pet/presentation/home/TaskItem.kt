package com.example.pet.presentation.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pet.ui.theme.PetTheme

/**
 * Компонент задачи для отображения в списке.
 * Соответствует принципам Clean Architecture - переиспользуемый UI компонент в presentation слое.
 *
 * @param title Название задачи
 * @param description Описание задачи (опционально)
 * @param day День выполнения задачи
 * @param isCompleted Статус выполнения задачи
 * @param onCheckedChange Callback при изменении статуса выполнения
 * @param onClick Callback при нажатии на задачу
 * @param modifier Modifier для настройки внешнего вида
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
    var showAdditionalTaskComponent by remember { mutableStateOf(false) }

    val offsetY by animateDpAsState(
        targetValue = if (showAdditionalTaskComponent) 60.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 200,          // дольше = плавнее на вид
            easing = FastOutSlowInEasing   // плавное торможение
        ),
        label = "slideDown"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (showAdditionalTaskComponent)
            Box(
                modifier = Modifier
                    .padding(top = offsetY)
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(30, 31, 37, 255))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = 20.dp)
                            .fillMaxWidth()
                            .height(45.dp)
                            .background(Color(40, 31, 37, 255))
                            .padding(horizontal = 28.dp)
                            .clickable(
                                onClick = onClickDeleteTaskById
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxSize()


                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "")
                            Text("Удалить задачу")
                        }
                    }
                }
            }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = {
                        if(showAdditionalTaskComponent) showAdditionalTaskComponent = false
                        else onClick()
                    },
                    onLongClick = {
                        onLongClick()
                        showAdditionalTaskComponent = true
                    }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Checkbox(
                checked = isCompleted,
                onCheckedChange = { newValue ->
                    // Останавливаем распространение события клика при нажатии на чекбокс
                    onCheckedChange(newValue)
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemPreview() {
    PetTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskItem(
                title = "Пример задачи",
                description = "Описание задачи",
                day = "Сегодня",
                isCompleted = false
            )

            TaskItem(
                title = "Выполненная задача",
                description = "Эта задача уже выполнена",
                day = "Вчера",
                isCompleted = true
            )

            TaskItem(
                title = "Задача без описания",
                day = "Завтра",
                isCompleted = false
            )
        }
    }
}

