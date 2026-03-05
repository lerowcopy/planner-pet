package com.example.pet.presentation.home

import android.util.Log
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.presentation.ui.VoiceMicButton
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
    modifier: Modifier = Modifier
) {
    // Состояния
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
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
    val animatedImeHeight by animateIntAsState(
        targetValue = imeHeight,
        animationSpec = tween(20, easing = FastOutLinearInEasing),
        label = "ime"
    )

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

    Box(modifier = modifier.fillMaxSize()) {

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 100.dp,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(16.dp)
                ) {
                    Text("Sheet Content")
                }
            }
        ) { innerPadding ->
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { scaffoldPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(scaffoldPadding)
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        when (val state = uiState.value) {
                            is HomeUiState.Loading -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(32.dp)
                                    )
                                }
                            }

                            is HomeUiState.Success -> {
                                if (state.tasks.isEmpty()) {
                                    Text(
                                        text = "Задачи отсутствуют",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        state.tasks.forEach { task ->
                                            TaskItem(
                                                title = task.title,
                                                description = task.description,
                                                day = task.day,
                                                isCompleted = task.isCompleted,
                                                onCheckedChange = { isCompleted ->
                                                    viewModel.updateTaskCompletion(task, isCompleted)
                                                },
                                                onClick = { onTaskClick(task) },
                                                onClickDeleteTaskById = { viewModel.deleteTaskById(task.id) }
                                            )
                                        }
                                    }
                                }
                            }

                            is HomeUiState.RefreshSuccess -> {}

                            is HomeUiState.Error -> {
                                Text(
                                    text = "Ошибка: ${state.message}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                )
                            }
                        }
                    }
                }

                // ModalBottomSheet для создания задачи
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

        // Card двигается вместе со sheet
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
                        y = currentOffset.roundToInt() - 80.dp.roundToPx() - animatedImeHeight
                    )
                }
                .height(80.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(50.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
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
                        viewModel.createQuickTask(quickTaskTitle)
                        quickTaskTitle = ""
                    },
                    onStartVoice = {
                        if (permissionState.status.isGranted) viewModel.startVoiceInput()
                        else permissionState.launchPermissionRequest()
                    },
                    onStopVoice = { viewModel.stopVoiceInput() }
                )
            }
        }
    }
}