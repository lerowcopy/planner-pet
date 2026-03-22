package com.example.pet.presentation.home

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.pet.presentation.ui.DatePickerDialog
import com.example.pet.presentation.ui.TimePickerDialog
import com.example.pet.presentation.ui.VoiceMicButton
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onTaskClick: (com.example.pet.domain.model.Task) -> Unit = {},
    onCalendarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val pagedTasks = viewModel.pagedTasks.collectAsLazyPagingItems()

    var showCreateBottomSheet by remember { mutableStateOf(false) }
    val createBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    var quickTaskTitle by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    val permissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    val imeHeight = WindowInsets.ime.getBottom(LocalDensity.current)

    val imeDp = with(LocalDensity.current) { imeHeight.toDp() }
    val animatedPeekHeight by animateDpAsState(
        targetValue = if (imeHeight > 0) imeDp else 110.dp,
        animationSpec = tween(50, easing = FastOutSlowInEasing),
        label = "peekHeight"
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("") }

    selectedDate?.let {
        val formatted = remember(it) {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
        }
        Text("Выбрано: $formatted", color = Color.Black)
        Log.i("date", formatted)
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { millis -> selectedDate = millis }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { hour, minute ->
                selectedTime = "%02d:%02d".format(hour, minute)
            }
        )
    }

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) focusManager.clearFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowError -> snackbarHostState.showSnackbar(
                    message = event.message,
                    withDismissAction = true
                )

                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(
                    message = event.message
                )
            }
        }
    }

    fun timeStringToMinutes(time: String): Int {
        if (time.isEmpty()) return 0
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }


    Box(modifier = modifier.fillMaxSize()) {

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = animatedPeekHeight,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCalendarClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Открыть календарь")
                    }
                }
            }
        )

        { innerPadding ->
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { scaffoldPadding ->
                when (val state = uiState.value) {
                    is HomeUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(scaffoldPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is HomeUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(scaffoldPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ошибка: ${state.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }

                    is HomeUiState.RefreshSuccess -> {}

                    is HomeUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(scaffoldPadding)
                                .padding(vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(bottom = 160.dp)
                        ) {
                            item {
                                Text(
                                    text = "Tasks",
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            items(
                                count = pagedTasks.itemCount,
                                key = { index -> pagedTasks[index]?.id ?: index }
                            ) { index ->
                                val task = pagedTasks[index] ?: return@items
                                TaskItem(
                                    title = task.title,
                                    description = task.description,
                                    day = task.day,
                                    isCompleted = task.isCompleted,
                                    onCheckedChange = { isCompleted ->
                                        viewModel.updateTaskCompletion(task, isCompleted)
                                    },
                                    onClick = { onTaskClick(task) },
                                    onClickDeleteTaskById = {
                                        viewModel.deleteTaskById(task.id)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            item {
                                when (pagedTasks.loadState.append) {
                                    is LoadState.Loading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }

                                    is LoadState.Error -> {
                                        Text(
                                            text = "Ошибка загрузки",
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }

                if (showCreateBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showCreateBottomSheet = false },
                        sheetState = createBottomSheetState,
                        modifier = Modifier.imePadding()
                    ) {
                        CreateTaskBottomSheet(
                            onDismiss = { showCreateBottomSheet = false },
                            onSave = { title, day ->
                                viewModel.createTask(title = title, day = day)
                            }
                        )
                    }
                }
            }
        }
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset {
                    val currentOffset = runCatching {
                        scaffoldState.bottomSheetState.requireOffset()
                    }.getOrDefault(0f)

                    IntOffset(
                        x = 0,
                        y = currentOffset.roundToInt() - 150.dp.roundToPx()
                    )
                }
                .height(135.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quickTaskTitle,
                        onValueChange = { quickTaskTitle = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Добавить задачу...") },
                        singleLine = true,
                        shape = RoundedCornerShape(50.dp)
                    )

                    Spacer(modifier = Modifier.padding(4.dp))

                    VoiceMicButton(
                        quickTaskTitle = quickTaskTitle,
                        isRecording = viewModel.isRecording,
                        isModelLoading = viewModel.isModelLoading,
                        onSendText = {
                            val startMinutes = timeStringToMinutes(selectedTime)
                            val day = selectedDate?.let {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                            } ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


                            viewModel.createTask(
                                quickTaskTitle,
                                day = day,
                                startMinutes = startMinutes,
                                endMinutes = startMinutes + 60
                            )
                            quickTaskTitle = ""
                            selectedTime = ""
                            selectedDate = null
                        },
                        onStartVoice = {
                            if (permissionState.status.isGranted) viewModel.startVoiceInput()
                            else permissionState.launchPermissionRequest()
                        },
                        onStopVoice = { viewModel.stopVoiceInput() }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 15.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .height(40.dp)
                            .width(142.dp),
                        onClick = {
                            showDatePicker = true
                        }
                    ) {
                        Text(
                            text = selectedDate?.let {
                                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                            } ?: "Выбрать дату"
                        )
                    }

                    Button(
                        modifier = Modifier
                            .height(40.dp)
                            .width(154.dp),
                        onClick = {
                            showTimePicker = true
                        }
                    ) {
                        Text(
                            text = selectedTime.ifEmpty { "Выбрать время" }
                        )
                    }
                }
            }

        }
    }
}
