package com.example.strokefree.Screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Memory Matching, 1 = Tic-Tac-Toe AI
    val backgroundColor = Color(0xFFE1E2EC) // New background color

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Game Center", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Memory Matching", modifier = Modifier.padding(16.dp), fontSize = 18.sp)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Tic-Tac-Toe AI", modifier = Modifier.padding(16.dp), fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the selected game
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White) // Making the game play area white
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedTab) {
                    0 -> MemoryMatchingGame()
                    1 -> TicTacToeAI()
                }
            }
        }
    }
}

@Composable
fun MemoryMatchingGame() {
    val emojis = listOf("🐶", "🐱", "🐵", "🐸", "🐰", "🐼", "🦊", "🐯")
    val shuffledPairs = remember { (emojis + emojis).shuffled() }

    var selectedCards by remember { mutableStateOf<List<Int>>(emptyList()) }
    var matchedCards by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var totalMoves by remember { mutableStateOf(0) }
    var gameWon by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun flipCard(index: Int) {
        if (selectedCards.size < 2 && !selectedCards.contains(index) && !matchedCards.contains(index)) {
            selectedCards = selectedCards + index
        }

        if (selectedCards.size == 2) {
            val first = selectedCards[0]
            val second = selectedCards[1]

            coroutineScope.launch {
                delay(500)
                if (shuffledPairs[first] == shuffledPairs[second]) {
                    matchedCards = matchedCards + first + second
                }
                selectedCards = emptyList()
                totalMoves++

                if (matchedCards.size == shuffledPairs.size) {
                    gameWon = true
                }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (gameWon) {
            Text("🎉 You Win! 🎉", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Moves Taken: $totalMoves", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Button(onClick = { selectedCards = emptyList(); matchedCards = emptySet(); gameWon = false; totalMoves = 0 }) {
                Text("Restart Game")
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Match the Pairs!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                // Grid Layout
                Column {
                    for (row in 0 until 4) {
                        Row {
                            for (col in 0 until 4) {
                                val index = row * 4 + col
                                if (index < shuffledPairs.size) {
                                    MemoryCard(
                                        emoji = shuffledPairs[index],
                                        isFlipped = selectedCards.contains(index) || matchedCards.contains(index),
                                        onClick = { flipCard(index) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Pairs Found: ${matchedCards.size / 2}", fontSize = 18.sp, color = Color.Black)
                Text("Total Moves: $totalMoves", fontSize = 18.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun MemoryCard(emoji: String, isFlipped: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isFlipped) 1f else 0f, label = "")

    Box(
        modifier = Modifier
            .size(80.dp)
            .padding(8.dp)
            .background(
                if (isFlipped) Color(0xFFFFF59D) // Yellow for flipped cards
                else Color(0xFF90CAF9), // Light Blue for unflipped cards
                RoundedCornerShape(8.dp)
            )
            .clickable { if (!isFlipped) onClick() }
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isFlipped) emoji else "❓",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}



@Composable
fun TicTacToeAI() {
    var board by remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }
    var gamesPlayed by remember { mutableStateOf(0) }
    var xWins by remember { mutableStateOf(0) }
    var oWins by remember { mutableStateOf(0) }
    var draws by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    fun resetGame() {
        board = List(3) { MutableList(3) { "" } }
        currentPlayer = "X"
        winner = null
    }

    fun checkWinner() {
        val winningLines = listOf(
            listOf(board[0][0], board[0][1], board[0][2]),
            listOf(board[1][0], board[1][1], board[1][2]),
            listOf(board[2][0], board[2][1], board[2][2]),
            listOf(board[0][0], board[1][0], board[2][0]),
            listOf(board[0][1], board[1][1], board[2][1]),
            listOf(board[0][2], board[1][2], board[2][2]),
            listOf(board[0][0], board[1][1], board[2][2]),
            listOf(board[0][2], board[1][1], board[2][0])
        )
        for (line in winningLines) {
            if (line.all { it == "X" }) { winner = "X"; xWins++; gamesPlayed++; return }
            if (line.all { it == "O" }) { winner = "O"; oWins++; gamesPlayed++; return }
        }
        if (board.all { row -> row.all { it.isNotEmpty() } }) { draws++; gamesPlayed++; winner = "Draw" }
    }

    fun aiMove(makeMove: (Int, Int) -> Unit) {
        val availableMoves = mutableListOf<Pair<Int, Int>>()
        for (row in 0..2) {
            for (col in 0..2) {
                if (board[row][col].isEmpty()) availableMoves.add(Pair(row, col))
            }
        }
        if (availableMoves.isNotEmpty()) {
            val (row, col) = availableMoves[Random.nextInt(availableMoves.size)]
            makeMove(row, col)
        }
    }

    fun makeMove(row: Int, col: Int) {
        if (board[row][col].isEmpty() && winner == null) {
            board = board.toMutableList().also { it[row] = it[row].toMutableList().also { it[col] = currentPlayer } }
            checkWinner()
            if (winner == null) {
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                if (currentPlayer == "O") {
                    coroutineScope.launch {
                        delay(500) // AI Thinking Time
                        aiMove(::makeMove)
                    }
                }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when {
                winner == "Draw" -> "It's a Draw! 🤝"
                winner != null -> "Winner: $winner! 🎉"
                else -> "Your Turn: $currentPlayer"
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tic-Tac-Toe Grid
        for (row in 0..2) {
            Row {
                for (col in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(4.dp, shape = RoundedCornerShape(8.dp))
                            .background(
                                when (board[row][col]) {
                                    "X" -> Color(0xFF42A5F5) // Blue for X
                                    "O" -> Color(0xFFEF5350) // Red for O
                                    else -> Color.White
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { if (board[row][col].isEmpty() && winner == null) makeMove(row, col) }, // ✅ Fix: Now cells are clickable
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = board[row][col],
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Game Stats
        Text("Games Played: $gamesPlayed", fontSize = 18.sp)
        Text("X Wins: $xWins | O Wins: $oWins | Draws: $draws", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Restart Game Button
        Button(
            onClick = { resetGame() },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
        ) {
            Text("Restart Game", fontSize = 18.sp, color = Color.White)
        }
    }
}

