package com.example.data

import com.example.model.*

object LevelCatalog {

    const val TOTAL_LEVELS = 500
    const val LEVELS_PER_CHAPTER = 50

    val CHAPTER_NAMES = listOf(
        "Outside The Box & Tricky Taps",      // Ch 1: 1-50
        "Physics, Food & Curious Creatures",  // Ch 2: 51-100
        "Tricky Numbers & Secret Codes",      // Ch 3: 101-150
        "Spatial Mazes & Pipe Networks",      // Ch 4: 151-200
        "Riddles & Lateral Paradoxes",        // Ch 5: 201-250
        "Keen Eyes & Spot The Trick",         // Ch 6: 251-300
        "Logic Gates & Switchboards",         // Ch 7: 301-350
        "Memory & Reflex Trials",             // Ch 8: 351-400
        "Detective Deduction & Alibis",       // Ch 9: 401-450
        "Galaxy Brain Genius Trials"          // Ch 10: 451-500
    )

    private val levelCache = mutableMapOf<Int, BrainLevel>()

    fun getLevel(id: Int): BrainLevel {
        val safeId = id.coerceIn(1, TOTAL_LEVELS)
        return levelCache.getOrPut(safeId) { generateLevel(safeId) }
    }

    fun getLevelsForChapter(chapterIndex: Int): List<BrainLevel> {
        val startId = (chapterIndex - 1) * LEVELS_PER_CHAPTER + 1
        val endId = (chapterIndex * LEVELS_PER_CHAPTER).coerceAtMost(TOTAL_LEVELS)
        return (startId..endId).map { getLevel(it) }
    }

    private fun generateLevel(id: Int): BrainLevel {
        val chapterIdx = ((id - 1) / LEVELS_PER_CHAPTER) + 1
        val chapterName = CHAPTER_NAMES.getOrElse(chapterIdx - 1) { "Mind Challenge" }

        return when (id) {
            1 -> BrainLevel(
                id = 1,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Which animal is actually the BIGGEST?",
                type = PuzzleType.TRICK_TAP,
                hint1 = "Don't just look at the drawings. Consider the real world!",
                hint2 = "An elephant in real life is vastly bigger than a tiny mouse or lion cub.",
                solutionExplanation = "The elephant is the largest animal in the real world, regardless of how small it was drawn!",
                funnySuccessComment = "You didn't fall for visual illusions! Massive IQ!",
                tapTargets = listOf(
                    TapTarget("lion", "Lion", "Big drawing", "🦁", false, 1.3f, "Lions are big, but not the biggest!"),
                    TapTarget("zebra", "Zebra", "Medium drawing", "🦓", false, 1.1f, "A zebra is majestic, but not the biggest!"),
                    TapTarget("mouse", "Mouse", "Huge drawing", "🐭", false, 1.5f, "A mouse drawn huge is still just a mouse!"),
                    TapTarget("elephant", "Elephant", "Small drawing", "🐘", true, 0.9f, "")
                )
            )

            2 -> BrainLevel(
                id = 2,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Feed the hungry kitty! 🐱",
                type = PuzzleType.DRAG_AND_DROP,
                hint1 = "The cat wants to eat the tasty fish.",
                hint2 = "Drag the delicious fish directly over to the cat's mouth!",
                solutionExplanation = "Cats love fish! Simply drag the fish onto the cat.",
                funnySuccessComment = "Purr-fect! The kitty purrs with delight!",
                draggables = listOf(
                    DraggableItem("cat", "Hungry Kitty", "🐱", 0.75f, 0.45f, 80, 80, isTarget = true, isMovable = false),
                    DraggableItem("fish", "Tasty Tuna", "🐟", 0.2f, 0.45f, 68, 68, isMovable = true, targetDropZoneId = "cat"),
                    DraggableItem("bone", "Dog Bone", "🦴", 0.5f, 0.75f, 60, 60, isMovable = true)
                )
            )

            3 -> BrainLevel(
                id = 3,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "It's too dark! Turn on the room lamp.",
                type = PuzzleType.DRAG_AND_DROP,
                hint1 = "The lamp isn't plugged in yet.",
                hint2 = "Drag the power plug into the wall socket!",
                solutionExplanation = "Electric lamps need electricity! Plugging it in turns it on.",
                funnySuccessComment = "Let there be light! An illuminating thought!",
                draggables = listOf(
                    DraggableItem("lamp", "Lamp", "💡", 0.5f, 0.25f, 80, 80, isTarget = false, isMovable = false),
                    DraggableItem("socket", "Wall Outlet", "🔌", 0.78f, 0.65f, 70, 70, isTarget = true, isMovable = false),
                    DraggableItem("plug", "Power Cable", "⚡", 0.22f, 0.65f, 65, 65, isMovable = true, targetDropZoneId = "socket")
                )
            )

            4 -> BrainLevel(
                id = 4,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Put the Elephant inside the refrigerator!",
                type = PuzzleType.DRAG_AND_DROP,
                hint1 = "First open the fridge door, then place the elephant in.",
                hint2 = "Tap or drag the elephant straight into the fridge box!",
                solutionExplanation = "The classic riddle: open the door, put the elephant in, close the door!",
                funnySuccessComment = "Chilly elephant! How did it even fit?!",
                draggables = listOf(
                    DraggableItem("fridge", "Refrigerator", "🧊", 0.75f, 0.45f, 96, 110, isTarget = true, isMovable = false),
                    DraggableItem("elephant", "Elephant", "🐘", 0.25f, 0.45f, 80, 80, isMovable = true, targetDropZoneId = "fridge")
                )
            )

            5 -> BrainLevel(
                id = 5,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Find the hidden gold coin!",
                type = PuzzleType.DRAG_AND_DROP,
                hint1 = "Things often slip under or behind comfortable furniture.",
                hint2 = "Drag the cozy sofa aside to reveal what's underneath!",
                solutionExplanation = "You moved the sofa and discovered the shiny coin hiding behind it!",
                funnySuccessComment = "Found it! Couch cushions are an endless goldmine!",
                draggables = listOf(
                    DraggableItem("coin", "Gold Coin", "🪙", 0.5f, 0.45f, 60, 60, isTarget = false, isMovable = true),
                    DraggableItem("sofa", "Couch", "🛋️", 0.5f, 0.45f, 90, 80, isTarget = false, isMovable = true)
                )
            )

            6 -> BrainLevel(
                id = 6,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Help the flower bloom! 🌸",
                type = PuzzleType.DRAG_AND_DROP,
                hint1 = "Flowers need sunlight and raindrops to grow.",
                hint2 = "Drag the raincloud directly over the wilted flower seed!",
                solutionExplanation = "Watering the flower brought it to full bloom!",
                funnySuccessComment = "A green thumb in action! Beautiful blossom!",
                draggables = listOf(
                    DraggableItem("flower", "Sprout", "🌱", 0.5f, 0.65f, 70, 70, isTarget = true, isMovable = false),
                    DraggableItem("cloud", "Rain Cloud", "🌧️", 0.2f, 0.25f, 80, 70, isMovable = true, targetDropZoneId = "flower"),
                    DraggableItem("sun", "Bright Sun", "☀️", 0.8f, 0.25f, 70, 70, isMovable = true)
                )
            )

            7 -> BrainLevel(
                id = 7,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Which number is the odd one out?",
                type = PuzzleType.TRICK_TAP,
                hint1 = "Look at the properties of each number: even, prime, or digit curves.",
                hint2 = "Number 2 is the ONLY even prime number in mathematics!",
                solutionExplanation = "2 is the only even prime number! All other primes are strictly odd.",
                funnySuccessComment = "Math genius! Prime logic indeed!",
                tapTargets = listOf(
                    TapTarget("n3", "3", "Prime", "3️⃣", false, 1.0f, "3 is an odd prime, just like 5 and 7."),
                    TapTarget("n5", "5", "Prime", "5️⃣", false, 1.0f, "5 is an odd prime, just like 3 and 7."),
                    TapTarget("n2", "2", "Even Prime", "2️⃣", true, 1.0f, ""),
                    TapTarget("n7", "7", "Prime", "7️⃣", false, 1.0f, "7 is an odd prime, just like 3 and 5.")
                )
            )

            8 -> BrainLevel(
                id = 8,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Wake up the sleeping sleepyhead! 😴",
                type = PuzzleType.SHAKE_TRIGGER,
                hint1 = "Tapping him doesn't work; he sleeps like a rock.",
                hint2 = "Give your device a good shake, or tap the SHAKE button below!",
                solutionExplanation = "A little earthquake woke him right up!",
                funnySuccessComment = "Rise and shine! Coffee time!",
                shakeInstruction = "Shake the phone or tap Shake!"
            )

            9 -> BrainLevel(
                id = 9,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Crack the 3-digit safe code!",
                type = PuzzleType.CODE_INPUT,
                hint1 = "Count the sides of a triangle, a square, and a hexagon.",
                hint2 = "Triangle has 3 sides, Square has 4 sides, Hexagon has 6 sides.",
                solutionExplanation = "Sides count: Triangle (3), Square (4), Hexagon (6) = 346!",
                funnySuccessComment = "Click! Safe unlocked! Geometry pays off!",
                correctCode = "346"
            )

            10 -> BrainLevel(
                id = 10,
                chapterIndex = 1,
                chapterName = chapterName,
                question = "Rub the magic lamp to summon the genie!",
                type = PuzzleType.SCRATCH_REVEAL,
                hint1 = "Rub your finger across the dusty magic lamp.",
                hint2 = "Keep scrubbing back and forth until the dust clears!",
                solutionExplanation = "Cleaning the brass lamp released the genie!",
                funnySuccessComment = "Poof! Your wish for high IQ has been granted!",
                scratchSecretText = "🧞 GENIE UNLEASHED!",
                scratchEmoji = "🪔"
            )

            // Dynamic algorithmic generation for remaining levels 11..500
            else -> generateDynamicLevel(id, chapterIdx, chapterName)
        }
    }

    private fun generateDynamicLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        val mod10 = id % 10
        val mod7 = id % 7
        val mod5 = id % 5

        return when {
            // DRAG_AND_DROP Combinations & Object interactions
            mod10 in listOf(1, 2) -> generateDragLevel(id, chapterIdx, chapterName)

            // TRICK_TAP / Lateral Choices
            mod10 in listOf(3, 4) -> generateTrickTapLevel(id, chapterIdx, chapterName)

            // CODE_INPUT / Math & Sequences
            mod10 in listOf(5, 6) -> generateCodeLevel(id, chapterIdx, chapterName)

            // ROTATION_PUZZLE / Pipe connections
            mod10 == 7 -> generatePipeLevel(id, chapterIdx, chapterName)

            // BALANCE_SCALE / Weight reasoning
            mod10 == 8 -> generateBalanceLevel(id, chapterIdx, chapterName)

            // SWITCH_BOARD / Logic Circuits
            mod10 == 9 -> generateSwitchLevel(id, chapterIdx, chapterName)

            // SCRATCH_REVEAL or SHAKE_TRIGGER
            else -> {
                if (id % 2 == 0) {
                    generateScratchLevel(id, chapterIdx, chapterName)
                } else {
                    generateShakeOrOrderLevel(id, chapterIdx, chapterName)
                }
            }
        }
    }

    private fun generateDragLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        val variants = listOf(
            Triple("Unlock the ancient chest!", "🗝️", "📦") to ("Drag the golden key into the chest lock" to "Open sesame! Treasure secured!"),
            Triple("Light the campfire!", "🔥", "🪵") to ("Drag the burning torch onto the dry wood logs" to "Warm and cozy fire started!"),
            Triple("Catch the mischievous mouse!", "🧀", "🪤") to ("Put the cheese into the mousetrap" to "Snap! Got the cheese lover!"),
            Triple("Charge the smartphone battery!", "⚡", "📱") to ("Connect the charging cable to the phone" to "100% Battery recharged!"),
            Triple("Make a yummy cup of coffee!", "☕", "🥛") to ("Pour the warm milk into the espresso cup" to "Delicious barista latte made!"),
            Triple("Help the bee pollinate the flower!", "🐝", "🌻") to ("Direct the buzzing bee to the sunny sunflower" to "Sweet honey in the making!"),
            Triple("Feed the hungry rabbit!", "🥕", "🐰") to ("Give the crunchy orange carrot to the rabbit" to "Crunch crunch! Happy bunny!"),
            Triple("Fuel up the rocket ship for launch!", "⛽", "🚀") to ("Drag the jet fuel tank to the rocket booster" to "3.. 2.. 1.. Blastoff! 🚀")
        )
        val variant = variants[(id + chapterIdx) % variants.size]
        val (theme, items) = variant
        val (title, dragEmoji, targetEmoji) = theme
        val (actionHint, winComment) = items

        return BrainLevel(
            id = id,
            chapterIndex = chapterIdx,
            chapterName = chapterName,
            question = "$title (Level $id)",
            type = PuzzleType.DRAG_AND_DROP,
            hint1 = "Drag the active element onto the receiver.",
            hint2 = actionHint,
            solutionExplanation = "Combining $dragEmoji with $targetEmoji completes the reaction!",
            funnySuccessComment = winComment,
            draggables = listOf(
                DraggableItem("target_$id", "Receiver", targetEmoji, 0.72f, 0.50f, 85, 85, isTarget = true, isMovable = false),
                DraggableItem("drag_$id", "Tool", dragEmoji, 0.22f, 0.50f, 75, 75, isMovable = true, targetDropZoneId = "target_$id"),
                DraggableItem("decoy_$id", "Decoy", "🪨", 0.48f, 0.78f, 60, 60, isMovable = true)
            )
        )
    }

    private fun generateTrickTapLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        val questions = listOf(
            "Which one moves faster than sound?" to listOf(
                TapTarget("cheetah", "Cheetah", "Fast runner", "🐆", false, failMessage = "Fast on land, but not faster than sound!"),
                TapTarget("fighter_jet", "Supersonic Jet", "Mach 2", "✈️", true),
                TapTarget("sports_car", "Sports Car", "300 km/h", "🏎️", false, failMessage = "Still much slower than sound!"),
                TapTarget("bullet_train", "Bullet Train", "Shinkansen", "🚅", false, failMessage = "Fast train, but sound is 1235 km/h!")
            ),
            "Which fruit has seeds on the outside?" to listOf(
                TapTarget("apple", "Apple", "Core seeds", "🍎", false, failMessage = "Apple seeds are securely hidden inside!"),
                TapTarget("watermelon", "Watermelon", "Inside seeds", "🍉", false, failMessage = "Watermelon seeds are inside the fruit!"),
                TapTarget("strawberry", "Strawberry", "Seeds outside", "🍓", true),
                TapTarget("banana", "Banana", "Tiny inside", "🍌", false, failMessage = "Bananas don't show seeds outside!")
            ),
            "Which planet is known as the Morning Star?" to listOf(
                TapTarget("mars", "Mars", "Red Planet", "🔴", false, failMessage = "Mars is the red warrior planet!"),
                TapTarget("venus", "Venus", "Bright glow", "⭐", true),
                TapTarget("jupiter", "Jupiter", "Giant", "🪐", false, failMessage = "Jupiter is huge, but not the Morning Star!"),
                TapTarget("mercury", "Mercury", "Closest", "🌑", false, failMessage = "Mercury is swift, but Venus shines brightest!")
            ),
            "How many holes are in this T-shirt? 👕 (Neck, 2 sleeves, bottom, plus 2 ripped holes front & back)" to listOf(
                TapTarget("h4", "4", "Standard holes", "4️⃣", false, failMessage = "Did you forget the rip goes through both sides?"),
                TapTarget("h6", "6", "Only 1 rip?", "6️⃣", false, failMessage = "If you can see through the rip, that's front AND back!"),
                TapTarget("h8", "8", "Total openings", "8️⃣", true),
                TapTarget("h7", "7", "Odd count", "7️⃣", false, failMessage = "Recount: Neck + 2 sleeves + bottom + 2 front + 2 back!")
            ),
            "If you overtake the 2nd person in a race, what place are you in?" to listOf(
                TapTarget("p1", "1st Place", "First?", "🥇", false, failMessage = "Nope! The leader is still ahead of you!"),
                TapTarget("p2", "2nd Place", "Second!", "🥈", true),
                TapTarget("p3", "3rd Place", "Third?", "🥉", false, failMessage = "You were 3rd, now you passed 2nd!"),
                TapTarget("p4", "Last Place", "Behind?", "🏃", false, failMessage = "Not at all!")
            )
        )
        val selected = questions[(id + chapterIdx) % questions.size]
        return BrainLevel(
            id = id,
            chapterIndex = chapterIdx,
            chapterName = chapterName,
            question = selected.first,
            type = PuzzleType.TRICK_TAP,
            hint1 = "Think carefully about the wording and physical facts.",
            hint2 = "The most obvious answer is usually the trap in Brain Test!",
            solutionExplanation = "Analyzing the exact technical terms and logic reveals the right answer.",
            funnySuccessComment = "Boom! That's how lateral thinkers roll!",
            tapTargets = selected.second
        )
    }

    private fun generateCodeLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        val a = (id % 9) + 2
        val b = ((id * 3) % 8) + 2
        val codeType = (id / 5) % 4

        val (questionText, answerCode, hintExplanation) = when (codeType) {
            0 -> {
                val ans = "${a * b}"
                Triple("If 1 = 5, 2 = 10, 3 = 15, what does $a equal?", ans, "Pattern multiplies by 5, or simply $a * $b = $ans")
            }
            1 -> {
                val sum = a + b + (id % 5)
                val code = "$sum"
                Triple("Count the total stars and sparkles: $a + $b + ${id % 5} = ?", code, "Simple sum: $a + $b + ${id % 5} = $code")
            }
            2 -> {
                val c1 = (id % 9) + 1
                val c2 = ((id + 3) % 9) + 1
                val c3 = ((id + 7) % 9) + 1
                val code = "$c1$c2$c3"
                Triple("Enter passcode where: First=$c1, Second=$c2, Third=$c3", code, "Direct digits: $c1, $c2, and $c3")
            }
            else -> {
                val square = a * a
                Triple("What is $a squared ($a × $a)?", "$square", "Square calculation: $a × $a = $square")
            }
        }

        return BrainLevel(
            id = id,
            chapterIndex = chapterIdx,
            chapterName = chapterName,
            question = questionText,
            type = PuzzleType.CODE_INPUT,
            hint1 = "Solve the equation using standard math rules or pattern detection.",
            hint2 = "The answer is: $answerCode",
            solutionExplanation = hintExplanation,
            funnySuccessComment = "Calculated to absolute perfection! Big brain energy!",
            correctCode = answerCode
        )
    }

    private fun generatePipeLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        // 4 rotatable pipe segments (2x2 grid)
        val tiles = listOf(
            PipeTile(0, PipeShape.CORNER, (id * 90) % 360, listOf(90)),
            PipeTile(1, PipeShape.STRAIGHT, ((id + 1) * 90) % 360, listOf(0, 180)),
            PipeTile(2, PipeShape.CORNER, ((id + 2) * 90) % 360, listOf(180)),
            PipeTile(3, PipeShape.STRAIGHT, ((id + 3) * 90) % 360, listOf(90, 270))
        )

        return BrainLevel(
            id = id,
            chapterIndex = chapterIdx,
            chapterName = chapterName,
            question = "Rotate the pipe valves to connect the water pipe! 🚰",
            type = PuzzleType.ROTATION_PUZZLE,
            hint1 = "Tap each pipe tile to rotate it 90 degrees.",
            hint2 = "Ensure water can flow continuously from top-left to bottom-right.",
            solutionExplanation = "All pipe channels align to create a seamless water connection!",
            funnySuccessComment = "Water is flowing smoothly! Master plumber certified!",
            pipeTiles = tiles
        )
    }

    private fun generateBalanceLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        val targetWeight = 10 + (id % 15)
        val item1Weight = targetWeight / 2
        val item2Weight = targetWeight - item1Weight
        val decoyWeight = targetWeight + 3

        val items = listOf(
            BalanceItem("item_a", "Gold Ingot", item1Weight, "🥇"),
            BalanceItem("item_b", "Ruby Gem", item2Weight, "💎"),
            BalanceItem("item_c", "Heavy Boulder", decoyWeight, "🪨")
        )

        return BrainLevel(
            id = id,
            chapterIndex = chapterIdx,
            chapterName = chapterName,
            question = "Balance the scale! Target weight on left pan is $targetWeight kg ⚖️",
            type = PuzzleType.BALANCE_SCALE,
            hint1 = "Select the items whose combined weights add up exactly to $targetWeight kg.",
            hint2 = "Item 1 ($item1Weight kg) + Item 2 ($item2Weight kg) = $targetWeight kg!",
            solutionExplanation = "$item1Weight kg + $item2Weight kg perfectly equals the $targetWeight kg counterweight!",
            funnySuccessComment = "Perfect equilibrium achieved! Law of physics respected!",
            balanceLeftTargetWeight = targetWeight,
            balanceItems = items
        )
    }

    private fun generateSwitchLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        val configs = listOf(
            SwitchConfig(0, "Switch A", listOf(0, 1)),
            SwitchConfig(1, "Switch B", listOf(1, 2)),
            SwitchConfig(2, "Switch C", listOf(0, 2))
        )
        return BrainLevel(
            id = id,
            chapterIndex = chapterIdx,
            chapterName = chapterName,
            question = "Turn ON all 3 neon security lights! 💡",
            type = PuzzleType.SWITCH_BOARD,
            hint1 = "Each switch toggles more than one light simultaneously.",
            hint2 = "Try toggling Switch A, then Switch B, then Switch C.",
            solutionExplanation = "By balancing the cross-wiring, all lights now shine bright!",
            funnySuccessComment = "All circuits operational! Power grid energized!",
            switchConfigs = configs,
            initialSwitchStates = listOf(false, true, false)
        )
    }

    private fun generateScratchLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        val secrets = listOf(
            "🔑 SECRET KEY: 777" to "🗝️",
            "🏆 GRANDMASTER IQ" to "👑",
            "🛸 ALIEN INVASION" to "👽",
            "🌈 POT OF GOLD" to "🍯",
            "🎯 DIRECT HIT" to "🎯"
        )
        val (secret, emoji) = secrets[id % secrets.size]
        return BrainLevel(
            id = id,
            chapterIndex = chapterIdx,
            chapterName = chapterName,
            question = "Wipe away the thick frost/fog to reveal the secret! ❄️",
            type = PuzzleType.SCRATCH_REVEAL,
            hint1 = "Drag your finger across the frosted card.",
            hint2 = "Scratch until at least 60% of the surface is revealed!",
            solutionExplanation = "Clearing the screen exposed the hidden message!",
            funnySuccessComment = "Crystal clear! Your eagle eyes missed nothing!",
            scratchSecretText = secret,
            scratchEmoji = emoji
        )
    }

    private fun generateShakeOrOrderLevel(id: Int, chapterIdx: Int, chapterName: String): BrainLevel {
        return BrainLevel(
            id = id,
            chapterIndex = chapterIdx,
            chapterName = chapterName,
            question = "Shake the tree to collect ripe red apples! 🍎",
            type = PuzzleType.SHAKE_TRIGGER,
            hint1 = "Apples cling to the branches until the wind or a shake knocks them loose.",
            hint2 = "Tap the 'SHAKE' button or give your phone a firm shake!",
            solutionExplanation = "Vibrations and gravity brought all the ripe apples tumbling down!",
            funnySuccessComment = "Gravity never fails! Isaac Newton would be proud!",
            shakeInstruction = "Shake phone or tap button to shake the tree!"
        )
    }
}
