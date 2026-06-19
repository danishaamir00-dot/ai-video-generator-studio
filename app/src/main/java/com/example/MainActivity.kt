package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.CustomCharacter
import com.example.data.StoryProject
import com.example.data.VideoFactoryRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ProjectViewModel
import com.example.viewmodel.ProjectViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room Database safely
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "video_factory_sqlite_db"
        ).fallbackToDestructiveMigration().build()

        val repository = VideoFactoryRepository(database.characterDao(), database.projectDao())
        val viewModelFactory = ProjectViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ProjectViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainContentScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainContentScreen(viewModel: ProjectViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe flow state variables safely
    val topicInput by viewModel.topicInput.collectAsState()
    val selectedCharacter by viewModel.selectedCharacter.collectAsState()
    val language by viewModel.language.collectAsState()
    val videoDuration by viewModel.videoDuration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    // Active generation state items
    val title by viewModel.title.collectAsState()
    val story by viewModel.story.collectAsState()
    val sceneBreakdown by viewModel.sceneBreakdown.collectAsState()
    val dialogues by viewModel.dialogues.collectAsState()
    val imagePrompt by viewModel.imagePrompt.collectAsState()
    val videoPrompt by viewModel.videoPrompt.collectAsState()
    val voicePrompt by viewModel.voicePrompt.collectAsState()
    val episodeNumber by viewModel.episodeNumber.collectAsState()

    val savedCharacters by viewModel.savedCharacters.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()

    // Character build forms
    val charName by viewModel.charName.collectAsState()
    val charAge by viewModel.charAge.collectAsState()
    val charPersonality by viewModel.charPersonality.collectAsState()
    val charAppearance by viewModel.charAppearance.collectAsState()

    // Premium simulated toggle & advertising state representation
    var isSubscribedToPremium by remember { mutableStateOf(false) }

    // Navigation segment state: 0 = Workspace, 1 = Characters, 2 = Gallery
    var selectedScreenIndex by remember { mutableStateOf(0) }
    
    // Feature Highlights modal view controller
    var showFeaturesDialog by remember { mutableStateOf(false) }

    // Preset topics for fast generation
    val presetTopics = listOf(
        Pair("اسلامی کارٹون", "Islamic Kid Cartoon Lesson"),
        Pair("جادوئی جنگل", "Magical Forest Adventure"),
        Pair("خلائی مہم علی کی", "Ali's Cosmic Space Voyage"),
        Pair("نیک ہمدرد بلی", "The Kind Helpful Cat"),
        Pair("عمران اور کمپیوٹر", "Imran & The Coding Computer")
    )

    // Dark cosmic brush background styling
    val cosmicSlateBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(cosmicSlateBrush)
    ) {
        // --- 1. Header Banner Zone ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = "AI Video Factory Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay background gradient for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            // Features info button on top-left
            TextButton(
                onClick = { showFeaturesDialog = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .testTag("app_features_info_button")
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Features Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "خصوصیات",
                            style = MaterialTheme.customTypography().labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Titles
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MovieCreation,
                        contentDescription = "Movie Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "AI Video Factory Free",
                        style = MaterialTheme.customTypography().headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "مفت کارٹون کہانیاں اور پرامپٹ بنائیں (100% Free AI Generator)",
                    style = MaterialTheme.customTypography().labelMedium,
                    color = Color.LightGray
                )
            }

            // Pro Badge
            TextButton(
                onClick = {
                    isSubscribedToPremium = !isSubscribedToPremium
                    val msg = if (isSubscribedToPremium) "Premium Mode Enabled (Simulated)" else "Switched to Free Ad-Supported Mode"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .testTag("premium_upgrade_button")
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSubscribedToPremium) Color(0xFFFFD700) else MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSubscribedToPremium) Icons.Filled.Star else Icons.Filled.Lock,
                            contentDescription = "Premium Mode",
                            tint = if (isSubscribedToPremium) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSubscribedToPremium) "PREMIUM" else "Go Premium",
                            style = MaterialTheme.customTypography().labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSubscribedToPremium) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // --- 2. Segmented Workspace Tabs ---
        TabRow(
            selectedTabIndex = selectedScreenIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedScreenIndex == 0,
                onClick = { selectedScreenIndex = 0 },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("workspace_tab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.VideoSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(text = "Workspace", style = MaterialTheme.customTypography().labelLarge)
                }
            }
            Tab(
                selected = selectedScreenIndex == 1,
                onClick = { selectedScreenIndex = 1 },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("characters_tab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Face, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(text = "Character Builder", style = MaterialTheme.customTypography().labelLarge)
                }
            }
            Tab(
                selected = selectedScreenIndex == 2,
                onClick = { selectedScreenIndex = 2 },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("gallery_tab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(text = "Gallery (${savedProjects.size})", style = MaterialTheme.customTypography().labelLarge)
                }
            }
        }

        // --- 3. Persistent Status Broadcast Ticker ---
        if (statusMessage.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Status",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.customTypography().bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearWorkspace() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // --- 4. Main Body Content Switcher ---
        Box(modifier = Modifier.weight(1f)) {
            when (selectedScreenIndex) {
                0 -> {
                    // TAB 0: WORKSPACE CREATOR
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Title / Header instructions
                        Text(
                            text = "اپنا پسندیدہ موضوع چنیں یا خود درج کریں:",
                            style = MaterialTheme.customTypography().titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Preset shortcuts
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetTopics.forEach { topic ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (topicInput == topic.first) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .clickable { viewModel.updateTopicInput(topic.first) }
                                ) {
                                    Text(
                                        text = topic.first,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.customTypography().labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Input Box for Topic
                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = { viewModel.updateTopicInput(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("topic_input_field"),
                            label = { Text("Topic: E.g., 'کہانی جادوئی سیب کی'") },
                            placeholder = { Text("کہانی کا موضوع درج کریں...") },
                            trailingIcon = {
                                if (topicInput.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateTopicInput("") }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Core Control Row (Character + Language) ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Generation Settings (ترتیبات)",
                                    style = MaterialTheme.customTypography().labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Character binding selector
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.AccountBox, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Lead Character: ",
                                        style = MaterialTheme.customTypography().bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    if (savedCharacters.isEmpty()) {
                                        Text(
                                            text = "(No character created yet. Defaulting to Random)",
                                            style = MaterialTheme.customTypography().bodySmall,
                                            color = Color.Gray
                                        )
                                    } else {
                                        var expandedCharDropdown by remember { mutableStateOf(false) }
                                        Box {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .clickable { expandedCharDropdown = !expandedCharDropdown }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = selectedCharacter?.name ?: "Random Protagonist",
                                                        style = MaterialTheme.customTypography().labelLarge,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = expandedCharDropdown,
                                                onDismissRequest = { expandedCharDropdown = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("None (Generic protagonist)") },
                                                    onClick = {
                                                        viewModel.selectCharacter(null)
                                                        expandedCharDropdown = false
                                                    }
                                                )
                                                savedCharacters.forEach { char ->
                                                    DropdownMenuItem(
                                                        text = { Text("${char.name} (Age: ${char.age})") },
                                                        onClick = {
                                                            viewModel.selectCharacter(char)
                                                            expandedCharDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Language Toggle
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Translate, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Story Language: ",
                                        style = MaterialTheme.customTypography().bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Row {
                                        // Urdu option
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (language == "Urdu") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                                            modifier = Modifier
                                                .clickable { viewModel.updateLanguage("Urdu") }
                                                .testTag("lang_urdu_button")
                                        ) {
                                            Text(
                                                text = "اردو (Urdu)",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                color = if (language == "Urdu") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.customTypography().labelMedium
                                            )
                                        }
                                        // English option
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (language == "English") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                            modifier = Modifier
                                                .clickable { viewModel.updateLanguage("English") }
                                                .testTag("lang_eng_button")
                                        ) {
                                            Text(
                                                text = "English",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                color = if (language == "English") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.customTypography().labelMedium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Video Duration / Length Selector
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Video Length: ",
                                        style = MaterialTheme.customTypography().bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    var expandedDurationDropdown by remember { mutableStateOf(false) }
                                    Box {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .clickable { expandedDurationDropdown = !expandedDurationDropdown }
                                                .testTag("video_duration_selector")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val suffix = when (videoDuration) {
                                                    "1 Minute", "2 Minutes", "3 Minutes", "5 Minutes" -> "⚡ Short"
                                                    "10 Minutes", "15 Minutes", "20 Minutes", "30 Minutes" -> "🎬 Medium"
                                                    else -> "🎥 Movie"
                                                }
                                                Text(
                                                    text = "$videoDuration ($suffix)",
                                                    style = MaterialTheme.customTypography().labelLarge,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = expandedDurationDropdown,
                                            onDismissRequest = { expandedDurationDropdown = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("⚡ Short (1-5 Min)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                                onClick = {},
                                                enabled = false
                                            )
                                            listOf("1 Minute", "2 Minutes", "3 Minutes", "5 Minutes").forEach { durationOpt ->
                                                DropdownMenuItem(
                                                    text = { Text("   • $durationOpt") },
                                                    onClick = {
                                                        viewModel.updateVideoDuration(durationOpt)
                                                        expandedDurationDropdown = false
                                                    }
                                                )
                                            }
                                            
                                            HorizontalDivider()
                                            
                                            DropdownMenuItem(
                                                text = { Text("🎬 Medium (10-30 Min)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                                onClick = {},
                                                enabled = false
                                            )
                                            listOf("10 Minutes", "15 Minutes", "20 Minutes", "30 Minutes").forEach { durationOpt ->
                                                DropdownMenuItem(
                                                    text = { Text("   • $durationOpt") },
                                                    onClick = {
                                                        viewModel.updateVideoDuration(durationOpt)
                                                        expandedDurationDropdown = false
                                                    }
                                                )
                                            }
                                            
                                            HorizontalDivider()
                                            
                                            DropdownMenuItem(
                                                text = { Text("🎥 Long (45-120 Min)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                                onClick = {},
                                                enabled = false
                                            )
                                            listOf("45 Minutes", "60 Minutes", "90 Minutes", "120 Minutes").forEach { durationOpt ->
                                                DropdownMenuItem(
                                                    text = { Text("   • $durationOpt") },
                                                    onClick = {
                                                        viewModel.updateVideoDuration(durationOpt)
                                                        expandedDurationDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- Primary Action: Generate Story & All Prompts ---
                        Button(
                            onClick = { viewModel.generateStory(targetEpisode = 1) },
                            enabled = !isLoading && topicInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("btn_build_story"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("مہربانی فرما کر انتظار کریں...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Generate")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == "Urdu") "نیا اسکرپٹ تلاش کریں" else "Build Video Script Suite",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- 5. Story Suite Output Cards Deck ---
                        if (title.isNotEmpty() || story.isNotEmpty()) {
                            Text(
                                text = "اسکرپٹ اور اسسٹنٹ پرامپٹ نتائج (Production Suite):",
                                style = MaterialTheme.customTypography().titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Title Block
                            ResultSectionCard(
                                headerIcon = Icons.Filled.TextFields,
                                headerTitle = "Episode title",
                                headerTitleUrdu = "کہانی کا نام",
                                bodyText = "Episode $episodeNumber: $title",
                                textDirection = if (language == "Urdu") TextDirection.Rtl else TextDirection.Ltr,
                                clipboardKey = "Title",
                                clipboardManager = clipboardManager,
                                context = context
                            )

                            // Narrative Script/Story Block
                            ResultSectionCard(
                                headerIcon = Icons.Filled.MenuBook,
                                headerTitle = "1. Story Script",
                                headerTitleUrdu = "کہانی کا حصہ",
                                bodyText = story,
                                textDirection = if (language == "Urdu") TextDirection.Rtl else TextDirection.Ltr,
                                clipboardKey = "Story Script",
                                clipboardManager = clipboardManager,
                                context = context
                            )

                            // Scene Storyboard Breakdown
                            ResultSectionCard(
                                headerIcon = Icons.Filled.Slideshow,
                                headerTitle = "2. Scene Breakdown",
                                headerTitleUrdu = "منظر نامہ (اسٹوری بورڈ)",
                                bodyText = sceneBreakdown,
                                textDirection = if (language == "Urdu") TextDirection.Rtl else TextDirection.Ltr,
                                clipboardKey = "Scene Storyboard",
                                clipboardManager = clipboardManager,
                                context = context
                            )

                            // Dialogues Deck
                            ResultSectionCard(
                                headerIcon = Icons.Filled.RecordVoiceOver,
                                headerTitle = "3. Character Dialogues",
                                headerTitleUrdu = "کرداروں کے مکالمے",
                                bodyText = dialogues,
                                textDirection = if (language == "Urdu") TextDirection.Rtl else TextDirection.Ltr,
                                clipboardKey = "Dialogues",
                                clipboardManager = clipboardManager,
                                context = context
                            )

                            // Midjourney/SD Image Prompt Description
                            ResultSectionCard(
                                headerIcon = Icons.Filled.Image,
                                headerTitle = "4. Image Prompt Generator",
                                headerTitleUrdu = "امیج جنریشن پرامپٹ (English)",
                                bodyText = imagePrompt,
                                textDirection = TextDirection.Ltr,
                                clipboardKey = "Image Prompt",
                                clipboardManager = clipboardManager,
                                context = context
                            )

                            // Sora/Luma Video Prompt Description
                            ResultSectionCard(
                                headerIcon = Icons.Filled.Videocam,
                                headerTitle = "5. Video Motion Prompt",
                                headerTitleUrdu = "ویڈیو موشن پرامپٹ (English)",
                                bodyText = videoPrompt,
                                textDirection = TextDirection.Ltr,
                                clipboardKey = "Video Prompt",
                                clipboardManager = clipboardManager,
                                context = context
                            )

                            // Voice Cues Accent prompt
                            ResultSectionCard(
                                headerIcon = Icons.Filled.SettingsVoice,
                                headerTitle = "6. Voice Over Styling Prompt",
                                headerTitleUrdu = "آواز کا انداز اور لہجہ (Urdu Voice)",
                                bodyText = voicePrompt,
                                textDirection = TextDirection.Ltr,
                                clipboardKey = "Voice Over Prompt",
                                clipboardManager = clipboardManager,
                                context = context
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // --- Sequential Episode generator ---
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Episode Automator (اگلی قسط بنائیں)",
                                        style = MaterialTheme.customTypography().titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "The AI remembers the previous story sequence. Generate sequence sequential series easily!",
                                        style = MaterialTheme.customTypography().bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = { viewModel.generateStory(targetEpisode = episodeNumber + 1) },
                                            enabled = !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "Episode ${episodeNumber + 1} بنائیں",
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.setEpisodeNumber(1) },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(Icons.Filled.RestartAlt, contentDescription = "Reset Episode counter", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // --- Export Suite Block ---
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_section_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "One-Click Export Suite",
                                        style = MaterialTheme.customTypography().titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Copy all
                                        Button(
                                            onClick = {
                                                val fullSuite = """
                                                    AI Video Factory - Episode $episodeNumber
                                                    Title: $title
                                                    =========================
                                                    STORY:
                                                    $story
                                                    
                                                    SCENES:
                                                    $sceneBreakdown
                                                    
                                                    DIALOGUES:
                                                    $dialogues
                                                    
                                                    SD IMAGE PROMPT:
                                                    $imagePrompt
                                                    
                                                    VEO VIDEO PROMPT:
                                                    $videoPrompt
                                                    
                                                    VO VOICE PROMPT:
                                                    $voicePrompt
                                                """.trimIndent()
                                                clipboardManager.setText(AnnotatedString(fullSuite))
                                                Toast.makeText(context, "Full script bundle copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy All Bundle", fontSize = 12.sp)
                                        }

                                        // Download TXT
                                        Button(
                                            onClick = {
                                                downloadAndShareTxtFile(
                                                    context = context,
                                                    title = title,
                                                    story = story,
                                                    scenes = sceneBreakdown,
                                                    dialogues = dialogues,
                                                    imgPrompt = imagePrompt,
                                                    vidPrompt = videoPrompt,
                                                    voicePrompt = voicePrompt,
                                                    episodeNo = episodeNumber
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onTertiaryContainer),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiaryContainer)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Download TXT File", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiaryContainer)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { viewModel.saveCurrentProject() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.Favorite, contentDescription = "Fav", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("کہانی گیلری میں محفوظ کریں", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // Empty State
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Movie,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "کوئی بھی موضوع لکھیئے اور 'نیا اسکرپٹ تلاش کریں' پر کلک کریں!",
                                        style = MaterialTheme.customTypography().bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Urdu or English Cartoon scripts will generate with images / storyboard prompts instantly.",
                                        style = MaterialTheme.customTypography().bodySmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // --- 6. Sponsor Ad Container Support (بعد میں) ---
                        if (!isSubscribedToPremium) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "SPONSORED AD",
                                                color = Color.Yellow,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "نئے کارٹونز بنانے کے لیے بہترین سستی مشینیں!",
                                            style = MaterialTheme.customTypography().labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Need 10x faster generations with zero limits? Join the high speed pipeline. Tap here to upgrade now.",
                                        style = MaterialTheme.customTypography().bodySmall,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            isSubscribedToPremium = true
                                            Toast.makeText(context, "Unlocked premium simulated plan!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Remove Ads (پرامپٹ پریمیم کریں)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: CHARACTER BUILDER (کردار ساز)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "نئے کردار کی تشکیل (Create Custom Protagonist):",
                            style = MaterialTheme.customTypography().titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "AI کہانی تیار کرتے وقت آپ کے کردار کی خصوصیات کو شامل رکھے گا۔",
                            style = MaterialTheme.customTypography().bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Character builder input desk
                        OutlinedTextField(
                            value = charName,
                            onValueChange = { viewModel.updateCharName(it) },
                            label = { Text("Character Name (E.g., عمران، ایان)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = charAge,
                            onValueChange = { viewModel.updateCharAge(it) },
                            label = { Text("Age (E.g., '12' or 'بچپنا')") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = charPersonality,
                            onValueChange = { viewModel.updateCharPersonality(it) },
                            label = { Text("Personality (E.g., بہادر، نٹکھٹ، ذہین)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = charAppearance,
                            onValueChange = { viewModel.updateCharAppearance(it) },
                            label = { Text("Appearance (E.g., کالا ہڈ، چمکتی آنکھیں، پیاری مسکراہٹ)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.createAndSaveCharacter() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("کردار محفوظ کریں (Save Character)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "آپ کے محفوظ کردہ کردار (Saved Characters):",
                            style = MaterialTheme.customTypography().titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (savedCharacters.isEmpty()) {
                            Text(
                                text = "محفوظ کردہ کا کوئی ریکارڈ موجود نہیں۔",
                                style = MaterialTheme.customTypography().bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            savedCharacters.forEach { character ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Face,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${character.name} (عمر: ${character.age} سال)",
                                                style = MaterialTheme.customTypography().bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "مزاج: ${character.personality}",
                                                style = MaterialTheme.customTypography().bodySmall
                                            )
                                            Text(
                                                text = "حلیہ: ${character.appearance}",
                                                style = MaterialTheme.customTypography().bodySmall,
                                                color = Color.Gray
                                            )
                                        }

                                        IconButton(onClick = { viewModel.selectCharacter(character) }) {
                                            Icon(
                                                imageVector = if (selectedCharacter?.id == character.id) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                                contentDescription = "Select",
                                                tint = if (selectedCharacter?.id == character.id) MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                        }

                                        IconButton(onClick = { viewModel.deleteCharacter(character) }) {
                                            Icon(
                                                imageVector = Icons.Filled.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: LIBRARY / HISTORIC GALLERY
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "کہانی گیلری (Saved Story Suites Library):",
                                style = MaterialTheme.customTypography().titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "اپنے گذشتہ اسکرپٹس کا ریکارڈ یہاں سے بحال کریں:",
                                style = MaterialTheme.customTypography().bodySmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (savedProjects.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "گیلری بالکل خالی ہے۔ کوئی کہانی محفوظ نہیں ہوئی۔",
                                            style = MaterialTheme.customTypography().labelLarge,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        } else {
                            items(savedProjects) { project ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "قسط ${project.episodeNumber}: ${project.title}",
                                                style = MaterialTheme.customTypography().titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )

                                            IconButton(onClick = { viewModel.deleteProject(project) }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Concept / Topic: ${project.topic}",
                                            style = MaterialTheme.customTypography().bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Character lead: ${project.characterName} (Age: ${project.characterAge})",
                                            style = MaterialTheme.customTypography().bodySmall,
                                            color = Color.Gray
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                viewModel.loadProject(project)
                                                selectedScreenIndex = 0 // Swap to workspace instantly
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("اسکرپٹ ایڈیٹر میں بحال کریں")
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

    if (showFeaturesDialog) {
        AlertDialog(
            onDismissRequest = { showFeaturesDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "🔒 Operational Features & Info",
                        style = MaterialTheme.customTypography().titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxHeight(0.8f)
                ) {
                    item {
                        Text(
                            text = "سہولیات اور خصوصیات (Operational Excellence)",
                            style = MaterialTheme.customTypography().titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Feature 1
                    item {
                        FeatureDialogRow(
                            icon = Icons.Filled.FlashOn,
                            titleEn = "⚡ Ultra-Fast Performance",
                            titleUr = "تیز ترین رفتار",
                            descEn = "Optimized architecture delivers fast response times for script, image prompt, video prompt, and voice-over generation.",
                            descUr = "بہترین آرکیٹیکچر کی بدولت اسکرپٹ، تصاویر اور ویڈیو پرامپٹس بہت تیزی سے تیار ہوتے ہیں۔"
                        )
                    }

                    // Feature 2
                    item {
                        FeatureDialogRow(
                            icon = Icons.Filled.NetworkCheck,
                            titleEn = "🌐 Network & Timeout Protection",
                            titleUr = "نیٹ ورک تحفظ",
                            descEn = "Built-in 60-second request timeout protection helps prevent connection failures during generation.",
                            descUr = "لمبی کہانیاں بناتے وقت کنکشن منقطع ہونے سے بچانے کے لیے 60 سیکنڈ کا خودکار حفاظتی ٹائم آؤٹ۔"
                        )
                    }

                    // Feature 3
                    item {
                        FeatureDialogRow(
                            icon = Icons.Filled.Storage,
                            titleEn = "💾 Local Data Persistence",
                            titleUr = "آف لائن محفوظ ڈیٹا بیس",
                            descEn = "Powered by SQLite Room Database with reactive Flow queries for instant UI updates and reliable offline storage.",
                            descUr = "روم اور ایس کیو لائٹ ڈیٹا بیس کی بدولت ڈیٹا مستقل محفوظ رہتا ہے اور فوراً لوڈ ہوتا ہے۔"
                        )
                    }

                    // Feature 4
                    item {
                        FeatureDialogRow(
                            icon = Icons.Filled.CloudOff,
                            titleEn = "📱 Offline Safety",
                            titleUr = "انٹرنیٹ کے بغیر رسائی",
                            descEn = "Previously generated stories, prompts, and character profiles remain fully accessible without an active connection.",
                            descUr = "پہلے سے بنی ہوئی کہانیاں اور کردار انٹرنیٹ نہ ہونے پر بھی گیلری میں مکمل دستیاب ہوتے ہیں۔"
                        )
                    }

                    // Feature 5
                    item {
                        FeatureDialogRow(
                            icon = Icons.Filled.Sync,
                            titleEn = "🔄 Real-Time Reactive Flow",
                            titleUr = "فوراً اپڈیٹ",
                            descEn = "Reactive data streams automatically refresh the user interface whenever workspace or gallery content changes.",
                            descUr = "جیسے ہی کوئی تبدیلی ہوتی ہے، پورا ایپ انٹرفیس بغیر ریفریش کیے فوراً اپڈیٹ ہو جاتا ہے۔"
                        )
                    }

                    // Feature 6
                    item {
                        FeatureDialogRow(
                            icon = Icons.Filled.Speed,
                            titleEn = "🚀 Smooth User Experience",
                            titleUr = "بہترین اور بلاتعطل ڈیزائن",
                            descEn = "Lightweight layout design ensures quick loading, minimal memory footprint, and responsive navigation.",
                            descUr = "ہلکا پھلکا انٹرفیس جو کم ریم استعمال کرتا ہے اور ایپ کو تیز اور ہموار چلاتا ہے۔"
                        )
                    }

                    // Feature 7
                    item {
                        FeatureDialogRow(
                            icon = Icons.Filled.Movie,
                            titleEn = "🎬 AI Content Generation Suite",
                            titleUr = "مکمل تخلیقی پیکیج",
                            descEn = "Generate story scripts, scene breakdown, characters, image briefs, kinetic prompts, and voice directions from one search.",
                            descUr = "ایک ہی کلک پر اسکرپٹ، مناظر، ڈائیلاگ، تصاویر کے خاکے اور ویڈیو موشن ہدایات تیار کریں۔"
                        )
                    }

                    // Feature 8
                    item {
                        FeatureDialogRow(
                            icon = Icons.Filled.Folder,
                            titleEn = "📂 Project & Folder Management",
                            titleUr = "عمدہ پروجیکٹ ترتیب",
                            descEn = "Organize AI-generated assets into separate projects and episode counters for smooth curation.",
                            descUr = "اپنی کہانیوں کو ترتیب سے قسط وار گیلری میں محفوظ اور منظم کریں۔"
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFeaturesDialog = false },
                    modifier = Modifier.testTag("close_features_dialog")
                ) {
                    Text(
                        text = "سمجھ گیا (Got It)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun FeatureDialogRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titleEn: String,
    titleUr: String,
    descEn: String,
    descUr: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "($titleUr)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descUr,
                    style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Rtl),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Custom reusable Card for presenting story blocks and individual copy features
@Composable
fun ResultSectionCard(
    headerIcon: androidx.compose.ui.graphics.vector.ImageVector,
    headerTitle: String,
    headerTitleUrdu: String,
    bodyText: String,
    textDirection: TextDirection,
    clipboardKey: String,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: Context
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(14.dp)
            ) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.customTypography().titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = headerTitleUrdu,
                        style = MaterialTheme.customTypography().labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(bodyText))
                        Toast.makeText(context, "$clipboardKey Copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy section",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
                    Text(
                        text = bodyText,
                        style = MaterialTheme.customTypography().bodyMedium.copy(textDirection = textDirection),
                        lineHeight = 22.sp,
                        textAlign = if (textDirection == TextDirection.Rtl) TextAlign.Right else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Helper extension on MaterialTheme to access the Typography standard parameters safely
@Composable
fun MaterialTheme.customTypography() = MaterialTheme.typography

// Helper utility writing full text content package and prompting native share system in Android
fun downloadAndShareTxtFile(
    context: Context,
    title: String,
    story: String,
    scenes: String,
    dialogues: String,
    imgPrompt: String,
    vidPrompt: String,
    voicePrompt: String,
    episodeNo: Int
) {
    val outputPackage = """
        ====================================================
        AI VIDEO FACTORY - CREATIVE PRODUCTION PACKAGE
        ====================================================
        Episode: $episodeNo
        Title: $title
        
        [STORY SCRIPT / تلاش کہانی]
        $story
        
        [SCENE REVEAL / کہانی کا منظر]
        $scenes
        
        [DIALOGUES / مکالمے]
        $dialogues
        
        [Midjourney / SD Image Generation Prompt]
        $imgPrompt
        
        [Sora / Luma Video Motion Prompt]
        $vidPrompt
        
        [TTS Urdu Accent Voiceover Prompt]
        $voicePrompt
        
        ====================================================
        Generated via 100% Free AI Video Factory Mobile
        ====================================================
    """.trimIndent()

    try {
        val fileName = "AIVideoFactory_Ep${episodeNo}.txt"
        val directory = context.getExternalFilesDir(null)
        val file = java.io.File(directory, fileName)
        file.writeText(outputPackage)

        // Launch native share so user can save directly to files or WhatsApp
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "AI Video Factory Ep $episodeNo")
            putExtra(android.content.Intent.EXTRA_TEXT, outputPackage)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Save or Export Script Suite (.TXT)"))
        Toast.makeText(context, "Saved to App Files successfully as $fileName!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
