package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_character")
data class UserCharacter(
    @PrimaryKey val id: Int = 1, // Single active user character
    val name: String,
    val characterClass: String, // Delinquent, Maid, Normal Student, Mage Priest
    val power: String,
    val bio: String,
    val level: Int = 1
)

@Entity(tableName = "rp_messages")
data class RpMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val isUser: Boolean,
    val isAction: Boolean, // If true, it represents a roleplay action like "*Oga hits Furuichi*"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val scenarioId: String
)

data class BeelCharacter(
    val name: String,
    val roleTitle: String,
    val description: String,
    val auraColorHex: String,
    val iconEmoji: String
) {
    companion object {
        val oga = BeelCharacter(
            name = "Tatsumi Oga",
            roleTitle = "Le Démon d'Ishiyama",
            description = "Un délinquant féroce et bagarreur qui a été choisi pour élever le fils du Roi des Démons. Reste calme face au danger mais a un tempérament explosif.",
            auraColorHex = "#2E7D32", // Green
            iconEmoji = "👊"
        )
        val babyBeel = BeelCharacter(
            name = "Baby Beel",
            roleTitle = "Kaiser de Emperana Beelzebub IV",
            description = "Le bébé fils du Roi des Démons. Très attaché à Oga, il pleure facilement et déclenche des décharges électriques dévastatrices de couleur verte.",
            auraColorHex = "#4CAF50", // Light Green
            iconEmoji = "👶"
        )
        val hilda = BeelCharacter(
            name = "Hilda (Hildegarde)",
            roleTitle = "La Servante Démoniaque",
            description = "Une servante démoniaque extrêmement dévouée à Baby Beel. Froide, arrogante mais redoutable au combat avec son épée cachée dans son ombrelle.",
            auraColorHex = "#6A1B9A", // Dark Purple
            iconEmoji = "☂️"
        )
        val furuichi = BeelCharacter(
            name = "Takayuki Furuichi",
            roleTitle = "Le Stratège (Normal)",
            description = "Le meilleur ami d'Oga. Seul élève lucide et normal, constamment effrayé par la puissance des délinquants et victime des chocs de Baby Beel.",
            auraColorHex = "#1565C0", // Blue
            iconEmoji = "🤓"
        )
        val kunieda = BeelCharacter(
            name = "Aoi Kunieda",
            roleTitle = "Chef des Red Tails",
            description = "Guerrière d'arts martiaux incroyable et leader du gang des filles 'Red Tails'. Secrètement amoureuse de Oga, elle rougit dès qu'il l'approche.",
            auraColorHex = "#AD1457", // Deep Pink
            iconEmoji = "🌸"
        )
        val toujou = BeelCharacter(
            name = "Hidetora Toujou",
            roleTitle = "Colosse du Tohoshinki",
            description = "La force brute suprême d'com.aistudio d'Ishiyama. Il adore se battre contre des adversaires ultra-forts, est très nonchalant et protecteur des animaux.",
            auraColorHex = "#D84315", // Orange-Red
            iconEmoji = "🦁"
        )

        val allNpcs = listOf(oga, babyBeel, hilda, furuichi, kunieda, toujou)
    }
}

data class RpScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val initialMessage: String,
    val scenarioBackgroundDrawableName: String = "img_ishiyama_high"
) {
    companion object {
        val list = listOf(
            RpScenario(
                id = "scen_ishiyama_courtyard",
                title = "Guerre des Clans à Ishiyama",
                subtitle = "Cour de récréation délabrée",
                description = "Les délinquants s'attroupent dans la cour déglinguée d'Ishiyama. Oga baille avec Baby Beel sur le dos. Hilda surveille la scène, tandis que Furuichi tremble pour sa peau.",
                initialMessage = "*La tension monte dans la cour d'Ishiyama. Des délinquants rivaux entourent le groupe.* Oga : « Encore des têtes à claques... Furuichi, porte le sac. » Baby Beel : « Dabuh ! » Hilda : « Ne salis pas les vêtements du jeune maître, vaurien. »"
            ),
            RpScenario(
                id = "scen_baby_beel_tantrum",
                title = "Tantrum Électrique !",
                subtitle = "Toits de l'école ou Salon d'Oga",
                description = "Baby Beel est inconsolable à cause d'une compote volée. Des éclairs verts crépitent autour de lui. Oga panique car le rayon de foudroiement va raser la ville !",
                initialMessage = "*Baby Beel fronce les sourcils, les larmes aux yeux ! Des arcs électriques verts s'abattent partout !* Oga : « Oh non... Furuichi, fais une grimace ou on va griller ! » Furuichi : « Pourquoi moi ?! AAAAAH ! »"
            ),
            RpScenario(
                id = "scen_kunieda_embarrassment",
                title = "Rencontre Explosive avec Kunieda",
                subtitle = "Parc de la Ville",
                description = "Kunieda s'entraine au sabre de bois avec ses Red Tails. Elle aperçoit Oga et commence à bafouiller, pensant qu'il veut la provoquer (ou l'inviter au temple).",
                initialMessage = "*Kunieda brandit son sabre d'entrainement, le visage tout rouge d'embarras.* Kunieda : « O-Oga ! Qu'est-ce que tu fais ici avec ce bébé ?! Si c'est pour un duel, je suis prête ! » Hilda : « Tiens, la petite cheffe des Red Tails veut jouer... »"
            )
        )
    }
}
