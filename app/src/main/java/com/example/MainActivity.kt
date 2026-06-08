package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WorkoutDatabase
import com.example.data.WorkoutRepository
import com.example.data.WorkoutSession
import com.example.model.Exercise
import com.example.model.ExerciseAnimation
import com.example.model.WorkoutData
import com.example.model.WorkoutProgram
import com.example.ui.components.ExerciseIllustration
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ActiveWorkoutSession
import com.example.viewmodel.DayStatus
import com.example.viewmodel.WorkoutUiState
import com.example.viewmodel.WorkoutViewModel
import com.example.viewmodel.WorkoutViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current.applicationContext as Application
                val database = remember { WorkoutDatabase.getDatabase(context) }
                val repository = remember { WorkoutRepository(database.workoutSessionDao) }
                
                val viewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModelFactory(context, repository)
                )

                val state by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = state.activeSession != null,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "MainWorkoutTransition"
                        ) { isWorkoutActive ->
                            if (isWorkoutActive) {
                                state.activeSession?.let { activeSession ->
                                    ActiveWorkoutScreen(
                                        session = activeSession,
                                        onPauseToggle = { viewModel.pauseWorkout() },
                                        onSkip = { viewModel.skipCurrentStep() },
                                        onExit = { viewModel.exitWorkout() }
                                    )
                                }
                            } else {
                                GymDashboard(
                                    state = state,
                                    onSelectProgram = { viewModel.selectProgram(it) },
                                    onStartWorkout = { viewModel.startWorkout(it) },
                                    onClearData = { viewModel.clearAllHistory() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GymDashboard(
    state: WorkoutUiState,
    onSelectProgram: (WorkoutProgram?) -> Unit,
    onStartWorkout: (WorkoutProgram) -> Unit,
    onClearData: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Identity Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HOME SWEAT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your Home Trainer",
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Active Flame Streak Count Bubble
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (state.streakCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (state.streakCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${state.streakCount} DAYS",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (state.streakCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Weekly Activity Tracker Grid Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "WEEKLY ACTIVITY LOG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.lastSevenDays.forEach { day ->
                            val borderModifier = if (day.isToday) {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                            } else Modifier

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = day.dayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (day.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .then(borderModifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Done",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (state.isCompletedToday) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✅ Today's workout completed! Streak active.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Subtitle section
        item {
            Text(
                text = "Workout Routines (No Equipment)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Lists of home workout routines
        items(state.programs) { program ->
            val isSelected = state.selectedProgram?.id == program.id
            
            WorkoutProgramCard(
                program = program,
                isSelected = isSelected,
                onClick = {
                    if (isSelected) onSelectProgram(null) else onSelectProgram(program)
                },
                onStart = { onStartWorkout(program) }
            )
        }

        // History Log summary segment
        if (state.history.isNotEmpty()) {
            item {
                Text(
                    text = "Workout History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            
            items(state.history.take(5)) { log ->
                val sdf = remember { java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm", java.util.Locale.US) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = log.programName,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sdf.format(java.util.Date(log.timestamp)),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${log.durationSeconds / 60}m ${log.durationSeconds % 60}s",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Duration",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${log.caloriesBurned} kcal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "Burned",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Workout History", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset History") },
            text = { Text("Are you absolutely sure you want to delete all historical logs and reset your active fitness streak? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WorkoutProgramCard(
    program: WorkoutProgram,
    isSelected: Boolean,
    onClick: () -> Unit,
    onStart: () -> Unit
) {
    val difficultyColor = when (program.difficulty) {
        "Beginner" -> MaterialTheme.colorScheme.secondary
        "Intermediate" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Program Info Title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.name,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${program.exercises.size} steps • No equipment",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Difficulty Level Indicator pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(difficultyColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = program.difficulty,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = difficultyColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = program.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isSelected) 5 else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "⏱", fontSize = 16.sp)
                    Text(text = "${program.durationMinutes} mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Text(text = "${program.estimatedCalories} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Expanded exercise specifications drawer list
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = "EXERCISE SEQUENCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    program.exercises.forEachIndexed { i, exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${i + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${exercise.durationSeconds}s • Active Target: ${exercise.focusArea}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_workout_button_${program.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START EQUIPMENT-FREE WORKOUT",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveWorkoutScreen(
    session: ActiveWorkoutSession,
    onPauseToggle: () -> Unit,
    onSkip: () -> Unit,
    onExit: () -> Unit
) {
    var showDialogConfirmExit by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Trigger physical haptic feedback when transition occurs or countdown finishes
    LaunchedEffect(session.secondsRemaining) {
        if (session.secondsRemaining <= 3 && session.secondsRemaining > 0 && !session.isFinished) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    if (session.isFinished) {
        CelebrationScreen(session = session, onExit = onExit)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Top Header Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showDialogConfirmExit = true }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
                }

                Text(
                    text = session.program.name.uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val progressText = if (session.currentExerciseIndex == -1) {
                        "WARMUP"
                    } else {
                        "${session.currentExerciseIndex + 1} OF ${session.program.exercises.size}"
                    }
                    Text(
                        text = progressText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sophisticated Dark Top Progress Bar segment
            val currentProgIndex = session.currentExerciseIndex
            val totalExercises = session.program.exercises.size
            val progressPercentFraction = if (currentProgIndex == -1) 0.05f else (currentProgIndex + 1).toFloat() / totalExercises.toFloat()
            
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressPercentFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Active Skeleton canvas animation tile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface, // #2B2930
                                    MaterialTheme.colorScheme.background // #1C1B1F
                                )
                            )
                        )
                        .padding(14.dp)
                ) {
                    val animType = if (session.currentExerciseIndex == -1) {
                        ExerciseAnimation.REST
                    } else if (session.isResting) {
                        ExerciseAnimation.REST
                    } else {
                        session.program.exercises[session.currentExerciseIndex].animationType
                    }

                    ExerciseIllustration(
                        animationType = animType,
                        isPaused = session.isPaused,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Beautiful numeric circular timer wheel
            val currentExercise = if (session.currentExerciseIndex >= 0) session.program.exercises[session.currentExerciseIndex] else null
            val maxSeconds = if (session.currentExerciseIndex == -1) 5 else if (session.isResting) 10 else currentExercise?.durationSeconds ?: 30
            val pctFraction = session.secondsRemaining.toFloat() / maxSeconds.toFloat()

            // Resolve color mappings outside Canvas scopes
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            val traceColor = if (session.isResting) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = trackColor,
                        radius = size.minDimension / 2f - 10f,
                        style = Stroke(width = 12f)
                    )
                }
                // Custom sweeping linear arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = traceColor,
                        startAngle = -90f,
                        sweepAngle = 360f * pctFraction,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${session.secondsRemaining}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "seconds",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Active exercise title and prompt lines
            if (session.currentExerciseIndex == -1) {
                Text(
                    text = "GET READY!",
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Prepare yourself for standard equipment-free workout moves.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            } else if (session.isResting) {
                Text(
                    text = "REST INTERVAL",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val nextExercise = session.program.exercises.getOrNull(session.currentExerciseIndex + 1)
                if (nextExercise != null) {
                    Text(
                        text = "NEXT UP: ${nextExercise.name}\n${nextExercise.durationSeconds} seconds target",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                currentExercise?.let { ex ->
                    Text(
                        text = ex.name,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TARGETS: ${ex.focusArea}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Detailed step instructions matching Sophisticated Dark specification
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "METHOD STEPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        ex.instructions.forEachIndexed { idx, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (idx == 0) MaterialTheme.colorScheme.surface 
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (idx == 0) MaterialTheme.colorScheme.outline else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Left vertical bar color accent for active instruction step
                                if (idx == 0) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Text(
                                    text = "%02d".format(idx + 1),
                                    color = if (idx == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                Text(
                                    text = step,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = if (idx == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // Routine Player Actions bar (play, pause, skip)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PAUSE/RESUME
                OutlinedButton(
                    onClick = onPauseToggle,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (session.isPaused) "▶" else "⏸",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (session.isPaused) "RESUME" else "PAUSE",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // SKIP / FORWARD
                if (session.currentExerciseIndex != -1) {
                    Button(
                        onClick = onSkip,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("skip_exercise_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "SKIP MOVE",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⏭",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showDialogConfirmExit) {
        AlertDialog(
            onDismissRequest = { showDialogConfirmExit = false },
            title = { Text("Quit Workout?") },
            text = { Text("Sure you want to quit? If you exit now, this completed routine log will not be saved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExit()
                        showDialogConfirmExit = false
                    }
                ) {
                    Text("Quit", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogConfirmExit = false }) {
                    Text("Stay Active")
                }
            }
        )
    }
}

@Composable
fun CelebrationScreen(
    session: ActiveWorkoutSession,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing active checkmark trophy
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large display metrics
        Text(
            text = "Workout Complete!",
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Spectacular workout! You completed the entire equipment-free routine.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Analytical summary Metrics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⏱ ${session.totalDurationSeconds / 60}m ${session.totalDurationSeconds % 60}s",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Time Trained",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔥 ${session.program.estimatedCalories} kcal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Energy Burned",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))

        // Return button
        Button(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("celebration_done_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "LOG SESSION & RETURN",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}
