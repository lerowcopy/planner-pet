package com.example.pet.presentation.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {

    val listState = rememberLazyListState()
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    val TOTAL_WEEKS = 200        // всего недель
    val START_WEEK = 100         // центр — текущая неделя

    val calendar = remember { java.util.Calendar.getInstance() }

    val today = remember {
        (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
    }

    // ✅ Генерируем даты для любой недели относительно текущей
    fun getWeekDates(weekOffset: Int): List<Pair<Int, Int>> {
        val startOfWeek = calendar.clone() as java.util.Calendar
        val diff = (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
        startOfWeek.add(java.util.Calendar.DAY_OF_MONTH, -diff + weekOffset * 7)
        return List(7) { dayIndex ->
            val d = startOfWeek.clone() as java.util.Calendar
            d.add(java.util.Calendar.DAY_OF_MONTH, dayIndex)
            Pair(
                d.get(java.util.Calendar.DAY_OF_MONTH),
                d.get(java.util.Calendar.MONTH)
            )
        }
    }

    fun getWeekYear(weekOffset: Int): Int {
        val c = calendar.clone() as java.util.Calendar
        c.add(java.util.Calendar.DAY_OF_MONTH, weekOffset * 7)
        return c.get(java.util.Calendar.YEAR)
    }

    val currentMonth = remember { mutableIntStateOf(calendar.get(java.util.Calendar.MONTH)) }
    val currentYear = remember { mutableIntStateOf(calendar.get(java.util.Calendar.YEAR)) }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val weekOffset = listState.firstVisibleItemIndex - START_WEEK
        val week = getWeekDates(weekOffset)
        currentMonth.value = week[0].second
        currentYear.value = getWeekYear(weekOffset)
    }

// ✅ Стартуем с центра
    LaunchedEffect(Unit) {
        listState.scrollToItem(START_WEEK)
    }

    var selectedDay by remember { mutableStateOf(Pair(START_WEEK, today)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val monthNames = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            // ✅ Заголовок с выбором месяца
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showMonthPicker = !showMonthPicker }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${monthNames[currentMonth.value]} ${currentYear.value}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (showMonthPicker)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ✅ Пикер месяца
            AnimatedVisibility(visible = showMonthPicker) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    itemsIndexed(monthNames) { index, month ->
                        FilterChip(
                            selected = index == currentMonth.value,
                            onClick = {
                                // Ищем ближайшую неделю с нужным месяцем
                                val currentWeekOffset = listState.firstVisibleItemIndex - START_WEEK
                                var targetWeek = START_WEEK
                                for (offset in -52..52) {
                                    val week = getWeekDates(offset)
                                    if (week.any { it.second == index }) {
                                        targetWeek = START_WEEK + offset
                                        break
                                    }
                                }
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetWeek)
                                }
                                showMonthPicker = false
                            },
                            label = { Text(month) }
                        )
                    }
                }
            }

            // ✅ Календарная строка
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(TOTAL_WEEKS) { itemIndex ->
                    val weekOffset = itemIndex - START_WEEK
                    val weekDates = getWeekDates(weekOffset)

                    Row(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(88.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        daysOfWeek.forEachIndexed { index, day ->
                            val isToday = index == today && weekOffset == 0
                            val isSelected = selectedDay == Pair(itemIndex, index)
                            val (date, _) = weekDates[index]

                            val backgroundColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday    -> MaterialTheme.colorScheme.secondaryContainer
                                else       -> Color.Transparent
                            }

                            val textColor = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday    -> MaterialTheme.colorScheme.onSecondaryContainer
                                else       -> MaterialTheme.colorScheme.onSurface
                            }

                            Column(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(backgroundColor)
                                    .clickable { selectedDay = Pair(itemIndex, index) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = date.toString(),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected || isToday)
                                            FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = if (isSelected) 1f else 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isToday && !isSelected)
                                                MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}