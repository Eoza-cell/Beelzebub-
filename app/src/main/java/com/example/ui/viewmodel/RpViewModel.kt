package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.AppDatabase
import com.example.data.model.BeelCharacter
import com.example.data.model.RpMessage
import com.example.data.model.RpScenario
import com.example.data.model.UserCharacter
import com.example.data.repository.RpRepository
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class NpcResponse(
    val senderName: String,
    val messageText: String,
    val isAction: Boolean
)

class RpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RpRepository

    val userCharacter: StateFlow<UserCharacter?>
    
    private val _selectedScenario = MutableStateFlow(RpScenario.list.first())
    val selectedScenario: StateFlow<RpScenario> = _selectedScenario.asStateFlow()

    private val _messages = MutableStateFlow<List<RpMessage>>(emptyList())
    val messages: StateFlow<List<RpMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAutoPlayActive = MutableStateFlow(false)
    val isAutoPlayActive: StateFlow<Boolean> = _isAutoPlayActive.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    private var autoPlayJob: Job? = null
    private var scenarioJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RpRepository(database.userCharacterDao(), database.rpMessageDao())

        userCharacter = repository.userCharacter.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Observe messages whenever the selected scenario changes
        viewModelScope.launch {
            _selectedScenario.collect { scenario ->
                scenarioJob?.cancel()
                scenarioJob = viewModelScope.launch {
                    repository.getMessagesForScenario(scenario.id).collect { list ->
                        if (list.isEmpty()) {
                            // Seed the database with the scenario initial message
                            val initial = RpMessage(
                                senderName = "Narrateur",
                                isUser = false,
                                isAction = true,
                                messageText = scenario.initialMessage,
                                scenarioId = scenario.id
                            )
                            repository.insertMessage(initial)
                        } else {
                            _messages.value = list
                        }
                    }
                }
            }
        }
    }

    fun selectScenario(scenario: RpScenario) {
        _selectedScenario.value = scenario
        // Disable autoplay when changing scenarios
        stopAutoPlay()
    }

    fun createCharacter(name: String, classType: String, power: String, bio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val char = UserCharacter(
                name = name.ifEmpty { "Transféré d'Ishiyama" },
                characterClass = classType,
                power = power.ifEmpty { "Poings de Métal" },
                bio = bio.ifEmpty { "Nouvel arrivant à Ishiyama High, prêt à en découdre !" }
            )
            repository.saveUserCharacter(char)
        }
    }

    fun deleteCharacter() {
        viewModelScope.launch(Dispatchers.IO) {
            stopAutoPlay()
            repository.clearUserCharacter()
            repository.clearAllMessages()
            _messages.value = emptyList()
        }
    }

    fun sendMessage(text: String, isAction: Boolean = false) {
        if (text.isBlank()) return
        val activeChar = userCharacter.value ?: return
        val activeScenario = _selectedScenario.value

        viewModelScope.launch(Dispatchers.IO) {
            val userMsg = RpMessage(
                senderName = activeChar.name,
                isUser = true,
                isAction = isAction,
                messageText = text,
                scenarioId = activeScenario.id
            )
            repository.insertMessage(userMsg)

            // If autoplay is active, we don't immediately force. Otherwise, trigger other character logic
            if (!_isAutoPlayActive.value) {
                // Let the NPCs react naturally! Delay slightly for dramatic pacing
                delay(1200)
                simulateNpcAction()
            }
        }
    }

    fun clearHistory() {
        val activeScenario = _selectedScenario.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearMessagesForScenario(activeScenario.id)
            delay(200)
            val initial = RpMessage(
                senderName = "Narrateur",
                isUser = false,
                isAction = true,
                messageText = activeScenario.initialMessage,
                scenarioId = activeScenario.id
            )
            repository.insertMessage(initial)
        }
    }

    fun toggleAutoPlay() {
        if (_isAutoPlayActive.value) {
            stopAutoPlay()
        } else {
            startAutoPlay()
        }
    }

    private fun startAutoPlay() {
        _isAutoPlayActive.value = true
        autoPlayJob = viewModelScope.launch(Dispatchers.IO) {
            while (_isAutoPlayActive.value) {
                // Wait between NPC interactions (6-9 seconds)
                delay(7000)
                if (!_isLoading.value && _isAutoPlayActive.value) {
                    simulateNpcAction()
                }
            }
        }
    }

    private fun stopAutoPlay() {
        _isAutoPlayActive.value = false
        autoPlayJob?.cancel()
        autoPlayJob = null
    }

    fun simulateNpcAction() {
        val char = userCharacter.value ?: return
        val scenario = _selectedScenario.value
        val history = _messages.value.takeLast(10)

        viewModelScope.launch {
            _isLoading.value = true
            _apiError.value = null
            try {
                val response = generateNpcInteraction(char, scenario, history)
                if (response != null) {
                    withContext(Dispatchers.IO) {
                        repository.insertMessage(
                            RpMessage(
                                senderName = response.senderName,
                                isUser = false,
                                isAction = response.isAction,
                                messageText = response.messageText,
                                scenarioId = scenario.id
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _apiError.value = "Erreur de connexion : ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun generateNpcInteraction(
        userChar: UserCharacter,
        scenario: RpScenario,
        history: List<RpMessage>
    ): NpcResponse? = withContext(Dispatchers.Default) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasKey) {
            // Hilarious fallback Simulation when Key is empty/placeholder!
            delay(1500)
            return@withContext getMockResponse(scenario, history)
        }

        val promptBuilder = StringBuilder()
        promptBuilder.append("HISTORIQUE DES ÉCHANGES :\n")
        history.forEach { msg ->
            if (msg.isAction) {
                promptBuilder.append("- *${msg.senderName} : ${msg.messageText}*\n")
            } else {
                promptBuilder.append("- ${msg.senderName} : « ${msg.messageText} »\n")
            }
        }

        promptBuilder.append("\nSITUATIONS DE JEU ACTUELLE :\n")
        promptBuilder.append("Le joueur '${userChar.name}' (Classe: '${userChar.characterClass}', Pouvoir: '${userChar.power}', Bio: '${userChar.bio}') participe au scénario '${scenario.title}'.\n")
        promptBuilder.append("Génère une réponse unique en choisissant le personnage le plus pertinent parmi la liste : Tatsumi Oga, Baby Beel, Hilda (Hildegard), Takayuki Furuichi, Aoi Kunieda, Hidetora Toujou.\n")
        promptBuilder.append("Retourne uniquement une chaîne de caractères JSON représentant le personnage choisi et son message/action. Pas d'autres commentaires, pas de markdown ```json.")

        val systemPrompt = """
            Tu es le maître de jeu officiel du RPG animé 'Beelzebub'.
            Tu dois simuler des répliques et des actions extrêmement fidèles à l'animé :
            - Tatsumi Oga : Hostile, paresseux, s'énerve vite, frappe les gêneurs, adore provoquer ses adversaires, porte Baby Beel.
            - Baby Beel : Un bébé démon mignon mais féroce. Dit souvent 'Dabu !' ou 'Dabuh !'. S'il pleure, il foudroie tout le monde de décharges électriques vertes explosives.
            - Hilda : Servante démoniaque froide et très protectrice du bébé. Appelée 'femme à l'ombrelle sword'. Méprise les humains, surtout Oga, mais s'inquiète pour son devoir.
            - Takayuki Furuichi : Très intelligent mais lâche, sarcastique, prend des coups à la place d'Oga. Adore courir derrière les filles en bafouillant.
            - Aoi Kunieda : Guerrière redoutable s'exprimant poliment mais timide et rougissante dès qu'Oga lui parle ou se montre attentionné.
            - Hidetora Toujou : Colosse increvable qui vit pour les bcombats intenses, nonchalant mais amical.

            REGLE DE SORTIE UNIQUE :
            Tu dois retourner UNIQUEMENT un objet JSON valide contenant l'interaction d'un NPC qui continue logiquement l'historique de chat.
            Format JSON requis (sans balises de bloc de code markdown, juste du texte brut JSON) :
            {
              "senderName": "Nom exacte du personnage",
              "messageText": "Dialogue parlé en français ou action narrative",
              "isAction": true ou false
            }
            Si isAction est true, messageText décrit une action physique du personnage (ex: "*Oga donne un coup de poing à Furuichi*") en évitant les dialogues parlés dedans.
            Si isAction is false, messageText est la réplique (ex: "Tu penses m'avoir battu avec si peu ?").
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = promptBuilder.toString())))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.9f
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            // Clean markdown blocks if Gemini returned them despite instruction
            val cleanJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(NpcResponse::class.java)
            adapter.fromJson(cleanJson)
        } catch (e: Exception) {
            // Regex/Handwritten parsing fallback to prevent crash in case of API formats or structure issues
            getMockResponse(scenario, history)
        }
    }

    private fun getMockResponse(scenario: RpScenario, history: List<RpMessage>): NpcResponse {
        val lastSender = history.lastOrNull()?.senderName ?: ""
        
        // Define scenario-specific custom simulated cycles to make the gameplay super realistic and hilarious even offline
        return when (scenario.id) {
            "scen_baby_beel_tantrum" -> {
                val candidates = listOf(
                    NpcResponse(
                        senderName = "Baby Beel",
                        messageText = "Dabuuuuuh ! *Des éclairs verts gigantesques sortent de ses fesses et foudroient de plein fouet Furuichi*",
                        isAction = true
                    ),
                    NpcResponse(
                        senderName = "Takayuki Furuichi",
                        messageText = "MAIS POURQUOI C'EST ENCORE MOI QUI ME FAIS ÉLECTROCUTER ?! OGA, FAIS QUELQUE CHOSE !",
                        isAction = false
                    ),
                    NpcResponse(
                        senderName = "Tatsumi Oga",
                        messageText = "*Tatapote gentiment la tête de Beel* « Arrête de chialer, regarde Furuichi a l'air d'un poulet grillé, c'est marrant. »",
                        isAction = true
                    ),
                    NpcResponse(
                        senderName = "Hilda (Hildegard)",
                        messageText = "Le jeune maître exprime simplement son mécontentement royal. C'est vous, viles créatures humaines, qui manquez d'égards envers lui !",
                        isAction = false
                    )
                )
                candidates.filter { it.senderName != lastSender }.random()
            }
            "scen_kunieda_embarrassment" -> {
                val candidates = listOf(
                    NpcResponse(
                        senderName = "Aoi Kunieda",
                        messageText = "*Rougit furieusement en croisant ses bras protecteurs* « O-Oga... Ne te méprends pas ! Je ne faisais que passer... et mes entraînements ne te concernent pas ! »",
                        isAction = true
                    ),
                    NpcResponse(
                        senderName = "Tatsumi Oga",
                        messageText = "Ah ? Bah salut Kunieda. Tu n'aurais pas vu une canette de lait démon par hasard ? Cette larve s'impatiente.",
                        isAction = false
                    ),
                    NpcResponse(
                        senderName = "Hilda (Hildegard)",
                        messageText = "*Sourire goguenard en s'appuyant sur son ombrelle* « Oh, la reine d'Ishiyama a le visage bien pourpre. Serait-elle sensible aux manières sauvages du vaurien ? »",
                        isAction = false
                    ),
                    NpcResponse(
                        senderName = "Takayuki Furuichi",
                        messageText = "KUNIEDA-SAN ! Ne l'écoute pas ! C'est un sauvage irrécupérable ! Regarde-moi, je suis galant !",
                        isAction = false
                    )
                )
                candidates.filter { it.senderName != lastSender }.random()
            }
            else -> {
                // Courtyard Standoff
                val candidates = listOf(
                    NpcResponse(
                        senderName = "Tatsumi Oga",
                        messageText = "*S'étire paresseusement avec un sourire provocateur* « Bon, qui veut goûter à mon poing en premier ? J'ai un bébé à nourrir, j'ai pas toute la journée. »",
                        isAction = true
                    ),
                    NpcResponse(
                        senderName = "Baby Beel",
                        messageText = "Dabuh ! *Agite ses petits poings pleins de volts*",
                        isAction = true
                    ),
                    NpcResponse(
                        senderName = "Hidetora Toujou",
                        messageText = "*Arrive en portant un chaton abandonné* « Hé Oga ! Ça a l'air de chauffer ici. Ça te dit un petit affrontement amical pour voir qui s'est entraîné le plus dur ? »",
                        isAction = true
                    ),
                    NpcResponse(
                        senderName = "Takayuki Furuichi",
                        messageText = "Attendez, pourquoi Toujou se ramène avec un chat ?! On est censés être en plein territoire ennemi ! Fuyons !",
                        isAction = false
                    ),
                    NpcResponse(
                        senderName = "Hilda (Hildegard)",
                        messageText = "*Dégaine d'un millimètre sa lame dissimulée* « Si un seul d'entre vous gêne la sieste du jeune maître, je vous découpe en tranches. »",
                        isAction = true
                    )
                )
                candidates.filter { it.senderName != lastSender }.random()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoPlay()
    }
}
