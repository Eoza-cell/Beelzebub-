package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.BeelCharacter
import com.example.data.model.RpMessage
import com.example.data.model.RpScenario
import com.example.data.model.UserCharacter
import com.example.ui.viewmodel.RpViewModel

// Safe hex conversion utility
fun String.toComposeColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (_: Exception) {
        Color(0xFF91F48F)
    }
}

@Composable
fun BeelRpAppContent(
    viewModel: RpViewModel,
    modifier: Modifier = Modifier
) {
    val userChar by viewModel.userCharacter.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0F1113) // Artistic background [#0F1113]
    ) {
        if (userChar == null) {
            CharacterCreationScreen(
                onCreateCharacter = { name, classType, power, bio ->
                    viewModel.createCharacter(name, classType, power, bio)
                }
            )
        } else {
            RoleplayMainScreen(
                viewModel = viewModel,
                userChar = userChar!!
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    onCreateCharacter: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var selectedClassIndex by remember { mutableIntStateOf(0) }
    var power by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    val classes = listOf(
        Triple("👊 Délinquant d'Ishiyama", "Force brute, poings dévastateurs. Idéal pour coller des mandales !", "Aura rouge flamme"),
        Triple("☂️ Serviteur Démoniaque", "Maîtrise les pactes magiques ou l'ombrelle sword à la Hilda.", "Aura violette ténébreuse"),
        Triple("🌸 Prêtresse Red Tails", "Formidable escrimeuse, fière et respectée à la Kunieda.", "Aura rose fuchsia"),
        Triple("🤓 Compagnon lambda", "Le lâche de service à la Furuichi, survit par la tchatche.", "Aura bleue lucide")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1113))
    ) {
        // App Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_beelzebub_banner),
                contentDescription = "Beelzebub Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xBB000000), Color(0xFF0F1113))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "CRÉATION DE PERSO",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF91F48F), // Theme Accent Green [#91F48F]
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Entre dans la bagarre d'Ishiyama High !",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E2E6).copy(alpha = 0.8f)
                )
            }
        }

        // Form fields and selectable classes
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Ton blase de voyou",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ex: Ryuji le templier, Maïssa, etc.", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1F2023),
                        unfocusedContainerColor = Color(0xFF1F2023),
                        focusedBorderColor = Color(0xFF91F48F),
                        unfocusedBorderColor = Color(0xFF35353A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Class selection cards
            item {
                Text(
                    text = "Ta réputation / Archétype",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(classes.size) { index ->
                val cls = classes[index]
                val isSelected = selectedClassIndex == index

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedClassIndex = index }
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF91F48F) else Color(0xFF35353A),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1F2E22) else Color(0xFF1F2023)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cls.first,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF91F48F) else Color.White
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color(0xFF91F48F), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cls.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E2E6).copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Power Input
            item {
                Text(
                    text = "Aptitude de Combat / Technique culte",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = power,
                    onValueChange = { power = it },
                    placeholder = { Text("Ex: Coup de boule nucléaire, Éclairs d'amour", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1F2023),
                        unfocusedContainerColor = Color(0xFF1F2023),
                        focusedBorderColor = Color(0xFF91F48F),
                        unfocusedBorderColor = Color(0xFF35353A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Bio Input
            item {
                Text(
                    text = "Comment es-tu arrivé là ? (Description comique)",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    placeholder = { Text("Raconte comment tu t'es retrouvé à devoir esquiver les décharges explosives d'un bébé démon...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1F2023),
                        unfocusedContainerColor = Color(0xFF1F2023),
                        focusedBorderColor = Color(0xFF91F48F),
                        unfocusedBorderColor = Color(0xFF35353A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Validation submit button with glowing theme color
        Button(
            onClick = {
                onCreateCharacter(
                    name.trim().ifEmpty { "Nouveau Rebelle" },
                    classes[selectedClassIndex].first,
                    power.trim().ifEmpty { "Fonce dans le tas" },
                    bio.trim().ifEmpty { "Un lycéen qui passait par là et a croisé la route de Tatsumi Oga." }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF91F48F),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "S'ENGAGER DANS L'AVENTURE",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleplayMainScreen(
    viewModel: RpViewModel,
    userChar: UserCharacter,
    modifier: Modifier = Modifier
) {
    // Local Tab state: "aventure", "equipe", "personnage"
    var currentTab by remember { mutableStateOf("aventure") }

    val activeScenario by viewModel.selectedScenario.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isAutoPlayActive by viewModel.isAutoPlayActive.collectAsStateWithLifecycle()
    val apiError by viewModel.apiError.collectAsStateWithLifecycle()

    var userMessageText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val lazyListState = rememberLazyListState()

    // Automatically scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F1113),
        // Overridden custom clean outer header card instead of standard TopAppBar
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1113))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header layout as in the "Artistic Flair" mockups
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2023), shape = RoundedCornerShape(28.dp))
                        .border(1.dp, Color(0xFF35353A), shape = RoundedCornerShape(28.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF91F48F), Color(0xFF2D6C31))
                                ),
                                shape = CircleShape
                            )
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👶", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (currentTab == "aventure") "Ishiyama: ${activeScenario.title}" else "Beelzebub AI RP",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF91F48F), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SESSION ACTIVE • ${userChar.name.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF91F48F),
                                    letterSpacing = 1.sp
                                ),
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Compact setting dropdown block trigger button
                    var showResetDialog by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2D2F33), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (showResetDialog) {
                        AlertDialog(
                            onDismissRequest = { showResetDialog = false },
                            containerColor = Color(0xFF1F2023),
                            title = {
                                Text(
                                    "Options de Roleplay",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            text = {
                                Text(
                                    "Veux-tu réinitialiser l'historique du chat ou carrément recommencer un nouveau personnage ?",
                                    color = Color(0xFFE2E2E6).copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showResetDialog = false
                                        viewModel.clearHistory()
                                    }
                                ) {
                                    Text("Réinitialiser chat", color = Color(0xFF91F48F), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                Row {
                                    TextButton(
                                        onClick = {
                                            showResetDialog = false
                                            viewModel.deleteCharacter()
                                        }
                                    ) {
                                        Text("Nouveau personnage", color = Color.Red)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = { showResetDialog = false }) {
                                        Text("Fermer", color = Color.Gray)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Typing interface and tabs block as a highly polished single visual footer
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF0F1113), Color(0xFF0F1113))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Interactive message input panel shown ONLY if "aventure" active tab
                if (currentTab == "aventure") {
                    // Quick response action buttons styled layout presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            Pair("👊 Bagarre !", "*Provoque les délinquants aux alentours pour lancer une baston générale !*"),
                            Pair("🍼 Lait démon", "*Lui tend rapidement un biberon géant pour calmer Baby Beel !*"),
                            Pair("🌸 Enquêter", "*Observe Kunieda en train d'esquiver Oga d'un air outré...*"),
                            Pair("🤓 Protection", "*Attrape Furuichi par le col d'un air machiavélique pour s'en servir de bouclier !*")
                        )

                        // Scrolling horizontal assistant row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(presets) { preset ->
                                Card(
                                    onClick = { viewModel.sendMessage(preset.second, isAction = true) },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                                    border = BorderStroke(1.dp, Color(0xFF35353A)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = preset.first,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Main chat input panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userMessageText,
                            onValueChange = { userMessageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            placeholder = { Text("Écris une parole ou une action...", color = Color.Gray, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (userMessageText.isNotBlank()) {
                                    viewModel.sendMessage(userMessageText)
                                    userMessageText = ""
                                    keyboardController?.hide()
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1F2023),
                                unfocusedContainerColor = Color(0xFF1F2023),
                                focusedBorderColor = Color(0xFF91F48F),
                                unfocusedBorderColor = Color(0xFF35353A)
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )

                        FloatingActionButton(
                            onClick = {
                                if (userMessageText.isNotBlank()) {
                                    viewModel.sendMessage(userMessageText)
                                    userMessageText = ""
                                    keyboardController?.hide()
                                }
                            },
                            containerColor = Color(0xFF91F48F),
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Enposer", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Interactive Bottom Navigation Board
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF35353A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 10.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab AVENTURE
                        Column(
                            modifier = Modifier
                                .clickable { currentTab = "aventure" }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val isActive = currentTab == "aventure"
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (isActive) Color(0xFF91F48F) else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AVENTURE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isActive) Color(0xFF91F48F) else Color.Gray
                            )
                        }

                        // Tab EQUIPE
                        Column(
                            modifier = Modifier
                                .clickable { currentTab = "equipe" }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val isActive = currentTab == "equipe"
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (isActive) Color(0xFF91F48F) else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PERSONNAGES",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isActive) Color(0xFF91F48F) else Color.Gray
                            )
                        }

                        // Tab PERSONNAGE
                        Column(
                            modifier = Modifier
                                .clickable { currentTab = "personnage" }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val isActive = currentTab == "personnage"
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (isActive) Color(0xFF91F48F) else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "MON PERSO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isActive) Color(0xFF91F48F) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F1113))
        ) {
            // Displays pages contents recursively depending on state
            when (currentTab) {
                "aventure" -> {
                    // SCENARIOS SELECTOR
                    Surface(
                        color = Color(0xFF131416),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)) {
                            Text(
                                text = "CHOISIS LE SCÉNARIO ACTUEL :",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF91F48F)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(RpScenario.list) { scenario ->
                                    val isSelected = scenario.id == activeScenario.id
                                    Card(
                                        onClick = { viewModel.selectScenario(scenario) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFF1b231c) else Color(0xFF1F2023)
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF91F48F) else Color(0xFF35353A)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.width(160.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = scenario.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = scenario.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) Color(0xFF91F48F) else Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SCENARIO DESCRIPTION CAPTION BAR
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18191B)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeScenario.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E2E6).copy(alpha = 0.8f),
                                    maxLines = 2,
                                    lineHeight = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Compact auto-simulation trigger toggle
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Auto-Chat",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAutoPlayActive) Color(0xFF91F48F) else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Switch(
                                    checked = isAutoPlayActive,
                                    onCheckedChange = { viewModel.toggleAutoPlay() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = Color(0xFF91F48F),
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }
                    }

                    // CHAT SCREEN WITH EXCLUSIVE VISUALS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ishiyama_high),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xE60F1113))
                        )

                        // Messages scrolling box
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(messages) { msg ->
                                RoleplayMessageBubble(message = msg)
                            }

                            // Dynamic loading state indicator
                            if (isLoading) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2F33)),
                                            border = BorderStroke(1.dp, Color(0xFF3D3F43)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    color = Color(0xFF91F48F),
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Un personnage prépare une action...",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                                    color = Color(0xFFE2E2E6).copy(alpha = 0.7f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Static floating prompt button if autoplay deactivated
                        if (!isAutoPlayActive) {
                            ExtendedFloatingActionButton(
                                onClick = { viewModel.simulateNpcAction() },
                                icon = { Icon(Icons.Default.FlashOn, null, tint = Color.Black) },
                                text = { Text("SIMULER IA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
                                containerColor = Color(0xFF91F48F),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .height(38.dp)
                            )
                        }
                    }
                }

                "equipe" -> {
                    // DIRECTORY OF NPCS SCREEN
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "PERSONNAGES DE L'ANIMÉ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF91F48F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ils interagissent automatiquement même si tu restes silencieux !",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(BeelCharacter.allNpcs) { character ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                                    border = BorderStroke(1.dp, Color(0xFF35353A)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    character.auraColorHex
                                                        .toComposeColor()
                                                        .copy(alpha = 0.2f), CircleShape
                                                )
                                                .border(
                                                    2.dp,
                                                    character.auraColorHex.toComposeColor(),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(character.iconEmoji, fontSize = 22.sp)
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = character.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            character.auraColorHex
                                                                .toComposeColor()
                                                                .copy(alpha = 0.2f),
                                                                RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "AURA",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = character.auraColorHex.toComposeColor(),
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }
                                            Text(
                                                text = character.roleTitle,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFF91F48F),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = character.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFE2E2E6).copy(alpha = 0.8f),
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "personnage" -> {
                    // USER PROFILE CHARACTER SHEET AND TRAINER
                    var expCount by remember { mutableIntStateOf(0) }
                    var hasTrainedMessage by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "FICHE PERSONNAGE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF91F48F)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Character summary board
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                            border = BorderStroke(1.dp, Color(0xFF35353A)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(Color(0xFF91F48F).copy(alpha = 0.15f), CircleShape)
                                            .border(2.dp, Color(0xFF91F48F), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🥋", fontSize = 28.sp)
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = userChar.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Text(
                                            text = userChar.characterClass,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF91F48F),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFF35353A))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "APTITUDE SPÉCIALE :",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = userChar.power,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "BIOGRAPHIE :",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = userChar.bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E2E6).copy(alpha = 0.8f),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Training minigame simulator
                        Text(
                            text = "ENTRAINEMENT DE VOYOU d'ISHIYAMA",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131416)),
                            border = BorderStroke(1.dp, Color(0xFF2E3A2E)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Réputation d'Escrime : ${expCount} XP",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Niveau Voyou : ${1 + (expCount / 5)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF91F48F)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            expCount += 1
                                            hasTrainedMessage = when {
                                                expCount % 5 == 0 -> {
                                                    val replies = listOf(
                                                        "Oga vous regarde d'un air endormi : « Pas mal. Même si Hilda fait mieux avec son plumeau. »",
                                                        "Baby Beel s'excite : « Dabuh, dabu ! » *Des mini-éclairs verts vous frôlent*",
                                                        "Kunieda rougit : « Tu... tu t'entraînes sérieusement, on dirait. Impressionnant. »",
                                                        "Furuichi pleurniche : « Arrêtez d'augmenter votre puissance alors que je reste une brindille ! »"
                                                    )
                                                    replies.random()
                                                }
                                                else -> "Tu enchaînes les pompes et les coups de poing sur un pneu suspendu !"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2D6C31),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("S'ENTRAINER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                AnimatedVisibility(visible = hasTrainedMessage.isNotEmpty()) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = hasTrainedMessage,
                                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                            color = if (hasTrainedMessage.contains("Oga") || hasTrainedMessage.contains("Baby")) Color(0xFF91F48F) else Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleplayMessageBubble(message: RpMessage) {
    val isNarrator = message.senderName == "Narrateur"
    val isUser = message.isUser

    // 1. Narrator layout custom rendering Centered layout with thin divider lines
    if (isNarrator) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            Text(
                text = message.messageText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontStyle = FontStyle.Italic,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFFE2E2E6).copy(alpha = 0.45f),
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
        }
        return
    }

    // Attempt to locate NPC configurations to adapt avatar aura/badges
    val npc = BeelCharacter.allNpcs.find { it.name.lowercase() == message.senderName.lowercase() }
    val emoji = npc?.iconEmoji ?: (if (isUser) "🥋" else "👹")
    val auraColor = npc?.auraColorHex?.toComposeColor() ?: (if (isUser) Color(0xFF91F48F) else Color(0xFF64748B))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // NPC avatar block (on Left)
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(auraColor.copy(alpha = 0.2f))
                    .border(1.5.dp, auraColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        // Active bubble message blocks
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Identity row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isUser) "Toi" else message.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color(0xFF91F48F) else auraColor
                )
                if (!isUser && npc != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• ${npc.roleTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.alpha(0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Bubble background styling depending on sender and message type
            val bubbleBg = if (isUser) {
                // User active bubble in radiant accent neon green
                Color(0xFF91F48F)
            } else {
                // NPCs/System in fallback grey card base
                Color(0xFF2D2F33)
            }

            val borderStroke = if (isUser) {
                null
            } else {
                BorderStroke(1.dp, Color(0xFF3D3F43))
            }

            val textFontColor = if (isUser) {
                Color.Black // High contrast black text on neon background
            } else {
                Color(0xFFE2E2E6)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = bubbleBg),
                shape = RoundedCornerShape(
                    topStart = if (!isUser) 0.dp else 16.dp,
                    topEnd = if (isUser) 0.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                border = borderStroke,
                modifier = if (isUser) Modifier.shadow(4.dp, RoundedCornerShape(16.dp)) else Modifier
            ) {
                Text(
                    text = message.messageText,
                    style = if (message.isAction) {
                        MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = if (isUser) FontWeight.Medium else FontWeight.Light
                        )
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    color = textFontColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            // Compact timestamp detail info
            Text(
                text = if (isUser) "12:43" else "12:44",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 8.sp,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp).alpha(0.6f)
            )
        }

        // User avatar block (on Right)
        if (isUser) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF91F48F).copy(alpha = 0.2f))
                    .border(1.5.dp, Color(0xFF91F48F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🥋", fontSize = 18.sp)
            }
        }
    }
}
