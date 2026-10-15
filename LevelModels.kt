package com.example.model

enum class PuzzleType {
    DRAG_AND_DROP,
    TRICK_TAP,
    CODE_INPUT,
    ROTATION_PUZZLE,
    BALANCE_SCALE,
    SCRATCH_REVEAL,
    SWITCH_BOARD,
    SHAKE_TRIGGER,
    WORD_RIDDLE,
    SPOT_DIFFERENCE,
    SEQUENCE_ORDER
}

data class DraggableItem(
    val id: String,
    val label: String,
    val iconEmoji: String,
    val initialX: Float, // 0.0f .. 1.0f (relative position on canvas)
    val initialY: Float, // 0.0f .. 1.0f
    val widthDp: Int = 72,
    val heightDp: Int = 72,
    val colorHex: Long = 0xFFFFFFFF,
    val isTarget: Boolean = false,
    val isMovable: Boolean = true,
    val hiddenUnderId: String? = null,
    val combineWithId: String? = null,
    val combineResultEmoji: String? = null,
    val combineResultLabel: String? = null,
    val targetDropZoneId: String? = null
)

data class TapTarget(
    val id: String,
    val label: String,
    val subLabel: String? = null,
    val emoji: String,
    val isCorrect: Boolean = false,
    val sizeScale: Float = 1.0f,
    val failMessage: String = "Not quite! Think differently."
)

enum class PipeShape {
    STRAIGHT,
    CORNER,
    T_JUNCTION,
    CROSS
}

data class PipeTile(
    val id: Int,
    val shape: PipeShape,
    val currentRotation: Int = 0, // 0, 90, 180, 270
    val targetRotations: List<Int> // Valid solution angles
)

data class BalanceItem(
    val id: String,
    val name: String,
    val weight: Int,
    val emoji: String
)

data class SwitchConfig(
    val id: Int,
    val label: String,
    val togglesIndices: List<Int> // Which bulbs or switches this lever toggles
)

data class BrainLevel(
    val id: Int,
    val chapterIndex: Int,
    val chapterName: String,
    val question: String,
    val type: PuzzleType,
    val hint1: String,
    val hint2: String,
    val solutionExplanation: String,
    val funnySuccessComment: String,
    val questionKeywordTappable: String? = null,
    val draggables: List<DraggableItem> = emptyList(),
    val tapTargets: List<TapTarget> = emptyList(),
    val correctCode: String? = null,
    val correctOrderIds: List<String> = emptyList(),
    val scratchSecretText: String? = null,
    val scratchEmoji: String? = null,
    val pipeTiles: List<PipeTile> = emptyList(),
    val switchConfigs: List<SwitchConfig> = emptyList(),
    val initialSwitchStates: List<Boolean> = emptyList(),
    val balanceLeftTargetWeight: Int = 0,
    val balanceItems: List<BalanceItem> = emptyList(),
    val isShakeTriggered: Boolean = false,
    val shakeInstruction: String? = null
)

data class LevelProgress(
    val levelId: Int,
    val isCompleted: Boolean,
    val stars: Int, // 1 to 3
    val hintsUsedCount: Int
)

data class UserStats(
    val completedCount: Int,
    val totalStars: Int,
    val hintBulbs: Int,
    val currentLevelId: Int,
    val iqScore: Int,
    val iqTitle: String,
    val hintsUsedTotal: Int
)
