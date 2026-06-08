package com.example.model

data class Exercise(
    val name: String,
    val durationSeconds: Int = 30,
    val focusArea: String,
    val instructions: List<String>,
    val animationType: ExerciseAnimation
)

enum class ExerciseAnimation {
    JUMPING_JACKS,
    SQUAT,
    PUSH_UP,
    PLANK,
    MOUNTAIN_CLIMBER,
    LUNGES,
    CRUNCHES,
    RUSSIAN_TWIST,
    FLUTTER_KICKS,
    GLUTE_BRIDGE,
    DONKEY_KICK,
    CALF_RAISE,
    REST,
    GENERIC
}

data class WorkoutProgram(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String, // "Beginner" | "Intermediate" | "Advanced"
    val durationMinutes: Int,
    val estimatedCalories: Int,
    val exercises: List<Exercise>
)

object WorkoutData {
    val programs = listOf(
        WorkoutProgram(
            id = "full_body_burn",
            name = "Full Body Burn",
            description = "High-energy workout designed to activate all major muscle groups and get your heart pumping without any equipment.",
            difficulty = "Intermediate",
            durationMinutes = 8,
            estimatedCalories = 105,
            exercises = listOf(
                Exercise(
                    name = "Jumping Jacks",
                    durationSeconds = 30,
                    focusArea = "Cardio & Full Body",
                    instructions = listOf(
                        "Stand straight with feet together, hands resting by your sides.",
                        "Jump, spreading your legs wider than shoulder-width while raising your arms fully straight above your head.",
                        "Jump again to return back to the starting standing position. Keep your core tight."
                    ),
                    animationType = ExerciseAnimation.JUMPING_JACKS
                ),
                Exercise(
                    name = "Bodyweight Squats",
                    durationSeconds = 30,
                    focusArea = "Quads, Glutes & Core",
                    instructions = listOf(
                        "Stand with feet shoulder-width apart, chest proud and back flat.",
                        "Lower your hips backward as if sitting down into a chair. Keep knees aligned with your toes.",
                        "Lower down until thighs are parallel to the floor, then push through your heels to rise up."
                    ),
                    animationType = ExerciseAnimation.SQUAT
                ),
                Exercise(
                    name = "Push-ups",
                    durationSeconds = 30,
                    focusArea = "Chest, Shoulders & Triceps",
                    instructions = listOf(
                        "Start in a high plank position with hands slightly wider than shoulder-width.",
                        "Lower your chest down by bending your elbows. Keep a straight line from head to heels.",
                        "Press firmly through your palms to return to the starting plank position."
                    ),
                    animationType = ExerciseAnimation.PUSH_UP
                ),
                Exercise(
                    name = "Plank Hold",
                    durationSeconds = 30,
                    focusArea = "Core, Lower Back & Shoulders",
                    instructions = listOf(
                        "Place your forearms flat on the floor, elbows aligned directly under your shoulders.",
                        "Extend legs straight behind you, tucking toes. Keep your neck parallel to the floor.",
                        "Squeeze your glutes and hold a perfectly flat spine. Breathe deeply."
                    ),
                    animationType = ExerciseAnimation.PLANK
                ),
                Exercise(
                    name = "Mountain Climbers",
                    durationSeconds = 30,
                    focusArea = "Cardio, Abdominals & Shoulders",
                    instructions = listOf(
                        "Start in a standard push-up plank position with straight arms.",
                        "Drive your right knee up towards your chest as far as possible.",
                        "Swap legs rapidly in a running motion, keeping your hips down and level."
                    ),
                    animationType = ExerciseAnimation.MOUNTAIN_CLIMBER
                ),
                Exercise(
                    name = "Alternating Lunges",
                    durationSeconds = 30,
                    focusArea = "Quads, Hamstrings & Glutes",
                    instructions = listOf(
                        "Stand with feet hip-width apart. Keep hands on hips for balance.",
                        "Take a large step forward with your right leg and bend both knees to 90 degrees.",
                        "Push back forcefully through your front heel, alternate and step forward with your left leg."
                    ),
                    animationType = ExerciseAnimation.LUNGES
                )
            )
        ),
        WorkoutProgram(
            id = "core_crusher",
            name = "Core Crusher",
            description = "Intense, targeted abdominal routine to sculpt your midsection, strengthen your lower back, and improve posture.",
            difficulty = "Advanced",
            durationMinutes = 6,
            estimatedCalories = 80,
            exercises = listOf(
                Exercise(
                    name = "Abdominal Crunches",
                    durationSeconds = 30,
                    focusArea = "Upper Abdominals",
                    instructions = listOf(
                        "Lie flat on your back with knees bent and feet flat on the floor.",
                        "Place your fingertips lightly behind your ears, avoiding pulling your neck.",
                        "Contract your abs to lift your shoulder blades off the flat mat. Exhale on the way up."
                    ),
                    animationType = ExerciseAnimation.CRUNCHES
                ),
                Exercise(
                    name = "Russian Twists",
                    durationSeconds = 30,
                    focusArea = "Obliques & Lower Abs",
                    instructions = listOf(
                        "Sit on the floor, leaning back at a 45-degree angle with knees bent and heels lifted off the ground.",
                        "Clasp your hands together in front of your chest.",
                        "Rotate your trunk completely to side-to-side, tapping your hands near the floor on each side."
                    ),
                    animationType = ExerciseAnimation.RUSSIAN_TWIST
                ),
                Exercise(
                    name = "Bicycle Crunches",
                    durationSeconds = 30,
                    focusArea = "Entire Core & Obliques",
                    instructions = listOf(
                        "Lie on your back, knees lifted to a 90-degree tabletop. Fingertips behind ears.",
                        "Extend left leg straight while twisting trunk to touch left elbow to right knee.",
                        "Switch rapidly to right leg extended and right elbow to left knee in a pedaling motion."
                    ),
                    animationType = ExerciseAnimation.CRUNCHES
                ),
                Exercise(
                    name = "Flutter Kicks",
                    durationSeconds = 30,
                    focusArea = "Lower Abdominals & Hip Flexors",
                    instructions = listOf(
                        "Lie flat on your back, placing hands under your glutes for lower-back support.",
                        "Raise both legs 6 inches off the floor, keeping knees extended straight.",
                        "Rapidly alternate lifting and lowering legs in small, swift scissor motions."
                    ),
                    animationType = ExerciseAnimation.FLUTTER_KICKS
                ),
                Exercise(
                    name = "Plank Hold",
                    durationSeconds = 30,
                    focusArea = "Full Core Stability",
                    instructions = listOf(
                        "Rest on your elbows directly below shoulders. Keep body fully horizontal.",
                        "Contract your stomach deeply as if pulling belly button towards the spine.",
                        "Hold and stabilize, avoiding dipping your pelvis or raising your buttocks."
                    ),
                    animationType = ExerciseAnimation.PLANK
                )
            )
        ),
        WorkoutProgram(
            id = "lower_body_sculpt",
            name = "Lower Body Sculpt",
            description = "A powerful workout focusing entirely on toning your glutes, strengthening thighs, and raising hamstring athletic power.",
            difficulty = "Beginner",
            durationMinutes = 7,
            estimatedCalories = 90,
            exercises = listOf(
                Exercise(
                    name = "Bodyweight Squats",
                    durationSeconds = 30,
                    focusArea = "Thighs & Buttocks",
                    instructions = listOf(
                        "Hold your arms out straight in front of you. Stand with feet slightly wider than shoulders.",
                        "Drop your hips back as if trying to sit on a low stool.",
                        "Drive upright through your middle heels while breathing out."
                    ),
                    animationType = ExerciseAnimation.SQUAT
                ),
                Exercise(
                    name = "Glute Bridges",
                    durationSeconds = 30,
                    focusArea = "Gluteal Muscles & Lower Back",
                    instructions = listOf(
                        "Lie flat on back, bend knees, and place feet flat hip-width apart.",
                        "Squeeze your buttocks and lift your pelvis upward off the floor.",
                        "Form a straight line from knees to chest, hold for 1 second, and lower back down."
                    ),
                    animationType = ExerciseAnimation.GLUTE_BRIDGE
                ),
                Exercise(
                    name = "Donkey Kicks (Left)",
                    durationSeconds = 30,
                    focusArea = "Glutes & Hamstrings",
                    instructions = listOf(
                        "Start on overall fours (all-fours table position) with back flat.",
                        "Keep your left knee bent at a 90-degree angle and kick your left sole up towards the ceiling.",
                        "Squeeze your glute at the top, then slowly lower the knee back to the matching start."
                    ),
                    animationType = ExerciseAnimation.DONKEY_KICK
                ),
                Exercise(
                    name = "Donkey Kicks (Right)",
                    durationSeconds = 30,
                    focusArea = "Glutes & Hamstrings",
                    instructions = listOf(
                        "Stay on all fours in the table position with spine flat.",
                        "Keep right knee bent at 90-degrees and lift your right heel up to the ceiling.",
                        "Squeeze right buttock intensely, then slowly lower right knee back down."
                    ),
                    animationType = ExerciseAnimation.DONKEY_KICK
                ),
                Exercise(
                    name = "Calf Raises",
                    durationSeconds = 30,
                    focusArea = "Calf Muscles",
                    instructions = listOf(
                        "Stand with feet shoulder-width apart. Balance naturally.",
                        "Slowly lift up on your balls of the feet, raising heels as high as possible.",
                        "Squeeze your calf muscles for a split second, then slowly descend back to the ground."
                    ),
                    animationType = ExerciseAnimation.CALF_RAISE
                )
            )
        )
    )
}
