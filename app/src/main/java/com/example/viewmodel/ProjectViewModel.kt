package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectViewModel(private val repository: VideoFactoryRepository) : ViewModel() {

    // Input States
    private val _topicInput = MutableStateFlow("")
    val topicInput: StateFlow<String> = _topicInput.asStateFlow()

    private val _selectedCharacter = MutableStateFlow<CustomCharacter?>(null)
    val selectedCharacter: StateFlow<CustomCharacter?> = _selectedCharacter.asStateFlow()

    private val _language = MutableStateFlow("Urdu") // "Urdu" or "English"
    val language: StateFlow<String> = _language.asStateFlow()

    private val _videoDuration = MutableStateFlow("5 Minutes")
    val videoDuration: StateFlow<String> = _videoDuration.asStateFlow()

    // Loading / Status States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // Active Generation Outputs
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _story = MutableStateFlow("")
    val story: StateFlow<String> = _story.asStateFlow()

    private val _sceneBreakdown = MutableStateFlow("")
    val sceneBreakdown: StateFlow<String> = _sceneBreakdown.asStateFlow()

    private val _dialogues = MutableStateFlow("")
    val dialogues: StateFlow<String> = _dialogues.asStateFlow()

    private val _imagePrompt = MutableStateFlow("")
    val imagePrompt: StateFlow<String> = _imagePrompt.asStateFlow()

    private val _videoPrompt = MutableStateFlow("")
    val videoPrompt: StateFlow<String> = _videoPrompt.asStateFlow()

    private val _voicePrompt = MutableStateFlow("")
    val voicePrompt: StateFlow<String> = _voicePrompt.asStateFlow()

    private val _episodeNumber = MutableStateFlow(1)
    val episodeNumber: StateFlow<Int> = _episodeNumber.asStateFlow()

    // Character Builder States
    private val _charName = MutableStateFlow("")
    val charName: StateFlow<String> = _charName.asStateFlow()

    private val _charAge = MutableStateFlow("")
    val charAge: StateFlow<String> = _charAge.asStateFlow()

    private val _charPersonality = MutableStateFlow("")
    val charPersonality: StateFlow<String> = _charPersonality.asStateFlow()

    private val _charAppearance = MutableStateFlow("")
    val charAppearance: StateFlow<String> = _charAppearance.asStateFlow()

    // Room Database Flow Connections
    val savedCharacters: StateFlow<List<CustomCharacter>> = repository.allCharacters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedProjects: StateFlow<List<StoryProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State Mutation Methods
    fun updateTopicInput(input: String) { _topicInput.value = input }
    fun selectCharacter(character: CustomCharacter?) { _selectedCharacter.value = character }
    fun updateLanguage(lang: String) { _language.value = lang }
    fun updateVideoDuration(dur: String) { _videoDuration.value = dur }

    fun updateCharName(value: String) { _charName.value = value }
    fun updateCharAge(value: String) { _charAge.value = value }
    fun updateCharPersonality(value: String) { _charPersonality.value = value }
    fun updateCharAppearance(value: String) { _charAppearance.value = value }

    fun setEpisodeNumber(num: Int) { _episodeNumber.value = num }

    fun clearWorkspace() {
        _title.value = ""
        _story.value = ""
        _sceneBreakdown.value = ""
        _dialogues.value = ""
        _imagePrompt.value = ""
        _videoPrompt.value = ""
        _voicePrompt.value = ""
        _episodeNumber.value = 1
        _statusMessage.value = "Workspace cleared. تیار ہو جائيں!"
    }

    // Call Gemini API to generate Story and prompts
    fun generateStory(targetEpisode: Int = 1) {
        val topic = _topicInput.value.trim()
        if (topic.isEmpty()) {
            _statusMessage.value = "براہ کرم پہلے کوئی ٹاپک لکھیے۔ (Please enter a topic first)."
            return
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _statusMessage.value = "Gemini API Key missing! AI Studio کے Secrets پینل میں پلے ہولڈر کو اپنی اصلی کی سے تبدیل کریں۔"
            return
        }

        _isLoading.value = true
        _statusMessage.value = "AI کہانی تیار کر رہا ہے... (Generating story...)"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Character contexts
                val characterString = buildCharacterContext()
                val durationString = buildVideoDurationContext()
                
                // Formulate Prompt based on Episode vs Fresh Generation
                val prompt = if (targetEpisode == 1) {
                    """
                    Generate static content for a kids cartoon episode: Episode 1.
                    Topic/Concept: "$topic"
                    
                    $characterString
                    
                    $durationString
                    
                    You must fulfill the requested format exactly. 
                    Target Language: ${_language.value}.
                    Output should be kid-safe, visually descriptions for video engines, and emotional tone advice for voicework.
                    """.trimIndent()
                } else {
                    """
                    Generate static content for a kids cartoon episode: Episode $targetEpisode.
                    This is a successive sequel episode.
                    Topic/Concept: "$topic"
                    
                    $characterString
                    
                    $durationString
                    
                    Previous events / context:
                    Title: ${_title.value}
                    Story summary: ${_story.value}
                    
                    Generate Episode $targetEpisode. Continue the cartoon series smoothly and creatively!
                    Target Language: ${_language.value}.
                    Fulfill the exact output formatting requested.
                    """.trimIndent()
                }

                // Custom system command prompt
                val systemPrompt = """
                You are an expert AI Video Production Assistant (اردو اور انگریزی کارٹون پروڈکشن ماہر).
                Your output MUST compile ALL of the following 7 sections under explicit section brackets.
                Format the response body EXACTLY like this (make sure bracket tags are uppercase and on their own lines, no symbols like * inside the tag brackets, and do not format tags inside markdown blocks):

                [TITLE]
                <A catchy, imaginative title for this episode>

                [STORY]
                <An engaging story in the target language scaled for the selected duration format. Keep it full of wonder, lessons, and color>

                [SCENE_BREAKDOWN]
                <Step-by-step cartoon storyboard scenes scaled precisely to the selected time duration:
                 - For 1-5 minutes short format: 3-5 quick scenes.
                 - For 10-30 minutes medium format: 12-20 detailed scene cards.
                 - For 45-120 minutes movie format: 25-40 extensive scenes, background music notes, cinematic acts (Beginning, Middle, climax, end)>

                [DIALOGUES]
                <Cute, simple, animated character dialogues. Underline emotions next to speaker names. Make dialogues dense or concise based on the video length format>

                [IMAGE_PROMPT]
                <Highly detailed, cinematic image prompt for image generation models like Stable Diffusion or Midjourney. Write this section in ENGLISH for maximum AI compatibility. Must capture stylized Pakistani anime/cartoon boy/girl features, clothing, landscape colors, 4k ultra-detailed, playful look>

                [VIDEO_PROMPT]
                <A dynamic, movement-focused video generation prompt describing actions, panning camera work, smooth animations, moving backgrounds, and physics loops. Write this section in ENGLISH>

                [VOICE_PROMPT]
                <A detailed Urdu voice over tone prompt. E.g., 'Friendly Pakistani boy voice, speaking Urdu warmly and with enthusiastic speed, playful accent, organic echoes'. Write this section in ENGLISH>

                Output language: ${_language.value}. 
                Ensure Urdu text is correct, clear, and uses authentic regional spelling.
                The [IMAGE_PROMPT], [VIDEO_PROMPT], and [VOICE_PROMPT] MUST always be in English.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = GenerationConfig(temperature = 0.8, topP = 0.95)
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val rawOutput = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!rawOutput.isNullOrEmpty()) {
                    parseAndSetOutputs(rawOutput)
                    _episodeNumber.value = targetEpisode
                    _statusMessage.value = "Episode $targetEpisode successfully generated!"
                } else {
                    _statusMessage.value = "Error: Empty response from Gemini API."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Error: ${e.localizedMessage ?: "Network or parsing exception occurred"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildVideoDurationContext(): String {
        val duration = _videoDuration.value
        return when (duration) {
            "1 Minute", "2 Minutes", "3 Minutes", "5 Minutes" -> {
                "Target Video Duration: $duration (Short Video format). Please generate exactly 3 to 5 highly concise scenes with fast-paced storytelling, crisp dialogue, and rapid pacing suited for Shorts, Reels, or TikTok hooks."
            }
            "10 Minutes", "15 Minutes", "20 Minutes", "30 Minutes" -> {
                "Target Video Duration: $duration (Medium Video format). Please generate 12 to 20 comprehensive scenes, a fully structured narrative flow with solid character introduction, plot build-up, standard dialogue interactions, and background music change suggestions."
            }
            "45 Minutes", "60 Minutes", "90 Minutes", "120 Minutes" -> {
                "Target Video Duration: $duration (Long Video / Full Movie format). Please generate an extremely detailed movie-like script layout containing at least 25-40 detailed scene descriptions, multiple dramatic dialogue shifts, character development, distinct narrative acts (Beginning, Middle, Climax, Ending), and rich background music suggestions and sound effect markers."
            }
            else -> "Target Video Duration: $duration. Scale the narrative density and sequence complexity accordingly."
        }
    }

    private fun buildCharacterContext(): String {
        val activeChar = _selectedCharacter.value
        return if (activeChar != null) {
            """
            Primary Character Sheet:
            - Name: ${activeChar.name}
            - Age: ${activeChar.age}
            - Personality: ${activeChar.personality}
            - Styling & Looks: ${activeChar.appearance}
            Use this character as the main lead for the story. Ensure their appearance and qualities match!
            """.trimIndent()
        } else {
            "No specific predefined custom character. Choose/invent a child protagonist appropriate for kids cartoon scripts (e.g. Ayaan or Zainab)."
        }
    }

    // Direct parser targeting clean Demarcators robustly
    private fun parseAndSetOutputs(rawText: String) {
        fun extractSection(tag: String, otherTags: List<String>): String {
            val tagMarker = "[$tag]"
            val startIndex = rawText.indexOf(tagMarker)
            if (startIndex == -1) return ""
            
            val contentStart = startIndex + tagMarker.length
            var endIndex = rawText.length

            for (other in otherTags) {
                val nextMarker = "[$other]"
                val idx = rawText.indexOf(nextMarker, contentStart)
                if (idx != -1 && idx < endIndex) {
                    endIndex = idx
                }
            }

            return rawText.substring(contentStart, endIndex).trim()
        }

        val allTags = listOf("TITLE", "STORY", "SCENE_BREAKDOWN", "DIALOGUES", "IMAGE_PROMPT", "VIDEO_PROMPT", "VOICE_PROMPT")

        _title.value = extractSection("TITLE", allTags.filter { it != "TITLE" })
        _story.value = extractSection("STORY", allTags.filter { it != "STORY" })
        _sceneBreakdown.value = extractSection("SCENE_BREAKDOWN", allTags.filter { it != "SCENE_BREAKDOWN" })
        _dialogues.value = extractSection("DIALOGUES", allTags.filter { it != "DIALOGUES" })
        _imagePrompt.value = extractSection("IMAGE_PROMPT", allTags.filter { it != "IMAGE_PROMPT" })
        _videoPrompt.value = extractSection("VIDEO_PROMPT", allTags.filter { it != "VIDEO_PROMPT" })
        _voicePrompt.value = extractSection("VOICE_PROMPT", allTags.filter { it != "VOICE_PROMPT" })
    }

    // Room Database Operations
    fun saveCurrentProject() {
        if (_title.value.isEmpty()) {
            _statusMessage.value = "محفوظ کرنے کے لیے کوئی کہانی موجود نہیں ہے۔"
            return
        }

        val project = StoryProject(
            topic = _topicInput.value,
            characterName = _selectedCharacter.value?.name ?: "Protagonist",
            characterAge = _selectedCharacter.value?.age ?: "12",
            characterPersonality = _selectedCharacter.value?.personality ?: "Curious",
            characterAppearance = _selectedCharacter.value?.appearance ?: "Local style clothes",
            title = _title.value,
            story = _story.value,
            sceneBreakdown = _sceneBreakdown.value,
            dialogues = _dialogues.value,
            imagePrompt = _imagePrompt.value,
            videoPrompt = _videoPrompt.value,
            voicePrompt = _voicePrompt.value,
            episodeNumber = _episodeNumber.value,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                repository.insertProject(project)
                _statusMessage.value = "کہانی گیلری میں کامیابی سے محفوظ کر دی گئی ہے!"
            } catch (e: Exception) {
                _statusMessage.value = "محفوظ کرنے میں خرابی: ${e.localizedMessage}"
            }
        }
    }

    fun loadProject(project: StoryProject) {
        _topicInput.value = project.topic
        _title.value = project.title
        _story.value = project.story
        _sceneBreakdown.value = project.sceneBreakdown
        _dialogues.value = project.dialogues
        _imagePrompt.value = project.imagePrompt
        _videoPrompt.value = project.videoPrompt
        _voicePrompt.value = project.voicePrompt
        _episodeNumber.value = project.episodeNumber
        _statusMessage.value = "کہانی گیلری سے بحال کر لی گئی!"
    }

    fun deleteProject(project: StoryProject) {
        viewModelScope.launch {
            try {
                repository.deleteProject(project)
                _statusMessage.value = "کہانی کامیابی سے حذف کر دی گئی۔"
            } catch (e: Exception) {
                _statusMessage.value = "خرابی: ${e.localizedMessage}"
            }
        }
    }

    fun createAndSaveCharacter() {
        val name = _charName.value.trim()
        val age = _charAge.value.trim()
        val personality = _charPersonality.value.trim()
        val appearance = _charAppearance.value.trim()

        if (name.isEmpty() || age.isEmpty() || personality.isEmpty() || appearance.isEmpty()) {
            _statusMessage.value = "براہ کرم کردار کے تمام معلوماتی خانے پر کریں۔ (All details are required)."
            return
        }

        val char = CustomCharacter(
            name = name,
            age = age,
            personality = personality,
            appearance = appearance
        )

        viewModelScope.launch {
            try {
                repository.insertCharacter(char)
                _statusMessage.value = "نیا کردار '${name}' کامیابی سے بنا لیا گیا!"
                // Clear builders fields
                _charName.value = ""
                _charAge.value = ""
                _charPersonality.value = ""
                _charAppearance.value = ""
            } catch (e: Exception) {
                _statusMessage.value = "کردار بچانے میں ناکامی: ${e.localizedMessage}"
            }
        }
    }

    fun deleteCharacter(character: CustomCharacter) {
        viewModelScope.launch {
            try {
                if (_selectedCharacter.value?.id == character.id) {
                    _selectedCharacter.value = null
                }
                repository.deleteCharacter(character)
                _statusMessage.value = "کردار کامیابی سے خارج کر دیا گیا۔"
            } catch (e: Exception) {
                _statusMessage.value = "خرابی: ${e.localizedMessage}"
            }
        }
    }
}

// ViewModel Factory Creator
class ProjectViewModelFactory(private val repository: VideoFactoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
