package com.sleepsounds.storytelling.data.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.ui.theme.LocalAppLanguage
import kotlin.math.abs

@Immutable
data class DreamSymbolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val emoji: String,
    val accentHex: Long,
    val summary: String,
    val psychologicalInsight: String,
    val calmingTip: String,
    val recommendedSoundId: String?,
    val recommendedSceneId: String?,
) {
    val psychologicalMeaning: String get() = summary
    val sleepReflection: String get() = calmingTip
}

typealias DreamInterpretationEntry = DreamSymbolItem

@Immutable
data class DailyQuoteItem(
    val id: String,
    val quote: String,
    val author: String,
) {
    val text: String get() = quote
}

typealias BedtimeQuote = DailyQuoteItem

data class LocalizedDreamText(
    val title: String,
    val subtitle: String,
    val summary: String,
    val insight: String,
    val tip: String,
)

@Composable
@ReadOnlyComposable
fun DreamSymbolItem.localized(): DreamSymbolItem =
    DreamInterpretationCatalog.localizeSymbol(this, LocalAppLanguage.current)

@Composable
@ReadOnlyComposable
fun DailyQuoteItem.localized(): DailyQuoteItem =
    DreamInterpretationCatalog.localizeQuote(this, LocalAppLanguage.current)

object DreamInterpretationCatalog {

    val SYMBOLS: List<DreamSymbolItem> = listOf(
        DreamSymbolItem(
            id = "fire",
            title = "Fire",
            subtitle = "Passion, transformation & emotional release",
            category = "Element",
            emoji = "🔥",
            accentHex = 0xFFFF7043,
            summary = "Dreaming of fire often reflects intense daytime emotions, creative energy, or a period of personal transformation. A warm, controlled hearth signals comfort and renewal, while an unruly blaze points to built-up stress that your mind is processing during REM sleep.",
            psychologicalInsight = "Fire archetypes appear when your subconscious is metabolizing strong ambition or emotional friction from waking life into new motivation.",
            calmingTip = "Before sleeping tonight, release any urgency from the day. Let your breath slow down and visualize gentle, glowing embers fading into a quiet night sky.",
            recommendedSoundId = "campfire_glow",
            recommendedSceneId = "sc_campfire",
        ),
        DreamSymbolItem(
            id = "losing_teeth",
            title = "Losing Teeth",
            subtitle = "Life transitions, self-image & unspoken worries",
            category = "Body",
            emoji = "🦷",
            accentHex = 0xFF90CAF9,
            summary = "One of the most universal dream themes, losing teeth commonly surfaces during major life transitions, career shifts, or moments when you feel a temporary loss of control in waking conversations.",
            psychologicalInsight = "Sleep researchers note that mild nocturnal jaw tension can also trigger tooth-related imagery during light REM stages.",
            calmingTip = "Remind yourself that change is natural and you are safe in the present moment. Unclench your jaw, relax your shoulders, and drift into restorative rest.",
            recommendedSoundId = "light_rain",
            recommendedSceneId = "sc_perfect_rain",
        ),
        DreamSymbolItem(
            id = "being_chased",
            title = "Being Chased",
            subtitle = "Avoidance, deadline pressure & heightened alertness",
            category = "Action",
            emoji = "🏃",
            accentHex = 0xFFB39DDB,
            summary = "Being pursued in a dream typically mirrors a task, emotion, or decision you have been putting off during the day. Your sympathetic nervous system remains slightly elevated, turning daytime pressure into a cinematic chase.",
            psychologicalInsight = "Rather than an external threat, the pursuer in dreams usually represents an unacknowledged responsibility asking for calm attention.",
            calmingTip = "Nothing requires your action right now. Tomorrow's tasks can wait until morning light; tonight belongs entirely to your peace.",
            recommendedSoundId = "light_rain",
            recommendedSceneId = "sc_rainy_day_comfort",
        ),
        DreamSymbolItem(
            id = "falling",
            title = "Falling",
            subtitle = "Letting go of control & transition into deep sleep",
            category = "Action",
            emoji = "🍃",
            accentHex = 0xFF64B5F6,
            summary = "Dreams of falling frequently occur during the hypnagogic transition between wakefulness and Stage 1 light sleep, accompanied by a gentle muscle twitch as your nervous system surrenders daytime tension.",
            psychologicalInsight = "Hypnic jerks are a healthy sign that your motor cortex is powering down for deep physical recovery.",
            calmingTip = "Sink deeply into your mattress and pillow. Imagine floating effortlessly on calm, supportive water.",
            recommendedSoundId = "calm_sea",
            recommendedSceneId = "sc_ocean_heartbeat",
        ),
        DreamSymbolItem(
            id = "flying",
            title = "Flying",
            subtitle = "Freedom, clarity & rising above obstacles",
            category = "Action",
            emoji = "🦅",
            accentHex = 0xFF4DD0E1,
            summary = "Soaring effortlessly through the sky is a classic lucid-leaning dream symbol of liberation, newfound perspective, and release from a recent burden or limitation.",
            psychologicalInsight = "Flying dreams correlate strongly with moments of creative breakthroughs and emotional relief.",
            calmingTip = "Carry that sense of weightlessness into your breathing—four seconds in, seven seconds hold, eight seconds out.",
            recommendedSoundId = "soft_piano",
            recommendedSceneId = "mu_soft_piano_night",
        ),
        DreamSymbolItem(
            id = "snake_bite",
            title = "Snake Bite",
            subtitle = "Awakening, hidden instincts & healing",
            category = "Nature",
            emoji = "🐍",
            accentHex = 0xFF81C784,
            summary = "Across classic dream psychology, a snake represents renewal (shedding old skin) and subconscious intuition. A bite acts as a wake-up call to pay attention to your boundaries and emotional well-being.",
            psychologicalInsight = "Your mind uses vivid instinctual symbols to highlight areas where your energy needs protection.",
            calmingTip = "Let go of lingering tension and focus on steady, rhythmic forest rain to ground your senses.",
            recommendedSoundId = "rain_forest",
            recommendedSceneId = "sc_perfect_rain",
        ),
        DreamSymbolItem(
            id = "fish",
            title = "Fish & Deep Water",
            subtitle = "Subconscious insights, abundance & tranquility",
            category = "Nature",
            emoji = "🐟",
            accentHex = 0xFF26C6DA,
            summary = "Fish swimming in clear water symbolize ideas, intuition, and emotional harmony emerging from the deeper layers of your subconscious mind.",
            psychologicalInsight = "Calm aquatic imagery reflects balanced parasympathetic activity and emotional clarity.",
            calmingTip = "Listen to the gentle rhythm of ocean waves and let your thoughts drift like calm currents.",
            recommendedSoundId = "water_wave",
            recommendedSceneId = "sc_ocean_heartbeat",
        ),
        DreamSymbolItem(
            id = "tornado",
            title = "Tornado & Storm",
            subtitle = "Emotional whirlwind & clearing the air",
            category = "Element",
            emoji = "⚡",
            accentHex = 0xFF9FA8DA,
            summary = "A storm or tornado in dreams reflects a flurry of rapid changes or competing priorities in your waking schedule that are seeking calm resolution.",
            psychologicalInsight = "After a storm dream, the mind often experiences a noticeable drop in cognitive clutter.",
            calmingTip = "Every storm passes into stillness. Wrap yourself in the cozy shelter of gentle rain on the window.",
            recommendedSoundId = "rain_on_windows",
            recommendedSceneId = "sc_rainy_day_comfort",
        ),
        DreamSymbolItem(
            id = "baby",
            title = "Baby",
            subtitle = "New beginnings, vulnerability & fresh ideas",
            category = "Body",
            emoji = "👶",
            accentHex = 0xFFF48FB1,
            summary = "Dreaming of a baby rarely predicts literal events; instead, it represents a new project, a fresh chapter in life, or a part of yourself that deserves gentle care and patience.",
            psychologicalInsight = "Nurturing symbols emerge when you are cultivating a new skill, relationship, or personal goal.",
            calmingTip = "Treat yourself with kindness tonight and allow your mind a full, uninterrupted cycle of deep rest.",
            recommendedSoundId = "soft_piano",
            recommendedSceneId = "mu_soft_piano_night",
        ),
        DreamSymbolItem(
            id = "dog",
            title = "Loyal Dog",
            subtitle = "Companionship, trust & inner protection",
            category = "Nature",
            emoji = "🐕",
            accentHex = 0xFFFFB74D,
            summary = "A calm, friendly dog in a dream embodies loyalty, trusted friendships, and your own protective instincts watching over your emotional space.",
            psychologicalInsight = "Companion animals in dreams reinforce feelings of safety and social connection.",
            calmingTip = "You are safe and supported. Let the quiet hum of the night carry you into deep sleep.",
            recommendedSoundId = "crickets",
            recommendedSceneId = "sc_campfire",
        ),
        DreamSymbolItem(
            id = "wedding",
            title = "Wedding",
            subtitle = "Harmony, commitment & uniting two paths",
            category = "Action",
            emoji = "💍",
            accentHex = 0xFFCE93D8,
            summary = "Weddings in dreams symbolize the integration of different aspects of your life—balancing work and rest, or committing to a healthier personal habit.",
            psychologicalInsight = "Union archetypes signify internal alignment between your conscious goals and emotional needs.",
            calmingTip = "Honor your commitment to restful sleep tonight as the foundation for a bright tomorrow.",
            recommendedSoundId = "meditation_bowl",
            recommendedSceneId = "mu_brain_massage",
        ),
    )

    val ENTRIES: List<DreamSymbolItem> get() = SYMBOLS

    private val CATEGORY_TRANSLATIONS: Map<String, Map<String, String>> = mapOf(
        "All" to mapOf(
            "vi" to "Tất cả", "pt-BR" to "Todos", "es" to "Todos", "fr" to "Tous",
            "de" to "Alle", "ja" to "すべて", "ko" to "전체", "it" to "Tutti",
            "ru" to "Все", "id" to "Semua", "tr" to "Tümü", "hi" to "सभी",
        ),
        "Element" to mapOf(
            "vi" to "Nguyên tố", "pt-BR" to "Elemento", "es" to "Elemento", "fr" to "Élément",
            "de" to "Element", "ja" to "自然要素", "ko" to "원소", "it" to "Elemento",
            "ru" to "Стихия", "id" to "Elemen", "tr" to "Element", "hi" to "तत्व",
        ),
        "Body" to mapOf(
            "vi" to "Cơ thể", "pt-BR" to "Corpo", "es" to "Cuerpo", "fr" to "Corps",
            "de" to "Körper", "ja" to "身体", "ko" to "신체", "it" to "Corpo",
            "ru" to "Тело", "id" to "Tubuh", "tr" to "Beden", "hi" to "शरीर",
        ),
        "Action" to mapOf(
            "vi" to "Hành động", "pt-BR" to "Ação", "es" to "Acción", "fr" to "Action",
            "de" to "Handlung", "ja" to "行動", "ko" to "행동", "it" to "Azione",
            "ru" to "Действие", "id" to "Tindakan", "tr" to "Eylem", "hi" to "क्रिया",
        ),
        "Nature" to mapOf(
            "vi" to "Thiên nhiên", "pt-BR" to "Natureza", "es" to "Naturaleza", "fr" to "Nature",
            "de" to "Natur", "ja" to "自然", "ko" to "자연", "it" to "Natura",
            "ru" to "Природа", "id" to "Alam", "tr" to "Doğa", "hi" to "प्रकृति",
        ),
    )

    fun localizeCategory(category: String, langCode: String): String {
        val norm = UserPreferencesRepository.normalizeLanguage(langCode)
        if (norm == UserPreferencesRepository.LANGUAGE_EN) return category
        return CATEGORY_TRANSLATIONS[category]?.get(norm) ?: category
    }

    private val SYMBOL_TRANSLATIONS: Map<String, Map<String, LocalizedDreamText>> = mapOf(
        "fire" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Ngọn lửa",
                subtitle = "Đam mê, sự chuyển mình & giải tỏa cảm xúc",
                summary = "Mơ thấy lửa thường phản ánh cảm xúc mãnh liệt ban ngày, nguồn năng lượng sáng tạo hoặc giai đoạn chuyển mình tích cực. Ngọn lửa ấm áp báo hiệu sự bình yên và đổi mới, trong khi đám cháy lớn cho thấy tâm trí đang giải tỏa căng thẳng trong giấc ngủ REM.",
                insight = "Biểu tượng ngọn lửa xuất hiện khi tiềm thức đang chuyển hóa khát vọng hoặc áp lực ban ngày thành động lực mới.",
                tip = "Trước khi ngủ tối nay, hãy buông bỏ mọi vội vã trong ngày. Hít thở chậm rãi và hình dung những đốm lửa ấm dịu dần dưới bầu trời đêm tĩnh lặng.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Fogo",
                subtitle = "Paixão, transformação e liberação emocional",
                summary = "Sonhar com fogo reflete emoções intensas, energia criativa ou um período de transformação pessoal.",
                insight = "Arquétipos de fogo surgem quando o subconsciente transforma ambição ou tensão diurna em nova motivação.",
                tip = "Antes de dormir hoje, solte qualquer pressa do dia e respire devagar.",
            ),
            "es" to LocalizedDreamText(
                title = "Fuego",
                subtitle = "Pasión, transformación y liberación emocional",
                summary = "Soñar con fuego refleja emociones intensas, energía creativa o una etapa de renovación personal.",
                insight = "El fuego aparece cuando el subconsciente transforma la presión diaria en nueva motivación.",
                tip = "Respira profundamente antes de dormir y deja que las preocupaciones se apaguen con calma.",
            ),
            "fr" to LocalizedDreamText(
                title = "Feu",
                subtitle = "Passion, transformation et libération émotionnelle",
                summary = "Rêver de feu reflète des émotions intenses, une énergie créative ou un renouveau intérieur.",
                insight = "Le subconscient transforme les tensions de la journée en énergie positive.",
                tip = "Ralentissez votre respiration ce soir et laissez votre esprit s'apaiser.",
            ),
            "de" to LocalizedDreamText(
                title = "Feuer",
                subtitle = "Leidenschaft, Wandel & emotionale Befreiung",
                summary = "Träume von Feuer spiegeln kreative Energie, starke Gefühle oder persönliche Erneuerung wider.",
                insight = "Das Unterbewusstsein verarbeitet Tagesstress im REM-Schlaf zu neuer Kraft.",
                tip = "Atmen Sie ruhig aus und lassen Sie die Hektik des Tages los.",
            ),
            "ja" to LocalizedDreamText(
                title = "炎・焚き火",
                subtitle = "情熱・変化・感情の解放",
                summary = "火の夢は、創造的なエネルギーや心の変化、日中のストレスの解放を表しています。",
                insight = "潜在意識が日中の緊張を新しい意欲へと変換しています。",
                tip = "今夜はゆっくりと深呼吸し、穏やかな焚き火の音を聴きながら眠りましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "불꽃과 모닥불",
                subtitle = "열정, 변화 및 감정 해소",
                summary = "불에 관한 꿈은 강한 열정, 창의적 에너지 또는 내면의 긍정적인 변화를 반영합니다.",
                insight = "잠재의식이 낮 동안의 긴장을 새로운 동기로 전환하고 있습니다.",
                tip = "잠들기 전 호흡을 가다듬고 평온한 밤하늘을 떠올려 보세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Fuoco",
                subtitle = "Passione, trasformazione e rilascio emotivo",
                summary = "Sognare il fuoco riflette energia creativa, emozioni intense o un periodo di rinnovamento.",
                insight = "Il subconscio trasforma la tensione diurna in nuova motivazione.",
                tip = "Rallenta il respiro prima di dormire e lascia andare ogni fretta.",
            ),
            "ru" to LocalizedDreamText(
                title = "Огонь",
                subtitle = "Страсть, обновление и освобождение эмоций",
                summary = "Сон об огне отражает творческую энергию, сильные эмоции или внутреннее обновление.",
                insight = "Подсознание перерабатывает дневное напряжение в новую мотивацию.",
                tip = "Перед сном замедлите дыхание и отпустите все заботы дня.",
            ),
            "id" to LocalizedDreamText(
                title = "Api",
                subtitle = "Semangat, transformasi & pelepasan emosi",
                summary = "Bermimpi tentang api mencerminkan energi kreatif, emosi mendalam, atau pembaruan diri.",
                insight = "Alam bawah sadar mengubah tekanan siang hari menjadi motivasi baru.",
                tip = "Tarik napas perlahan malam ini dan lepaskan semua ketegangan.",
            ),
            "tr" to LocalizedDreamText(
                title = "Ateş",
                subtitle = "Tutku, dönüşüm ve duygusal rahatlama",
                summary = "Rüyada ateş görmek yaratıcı enerjiyi, güçlü duyguları veya içsel yenilenmeyi simgeler.",
                insight = "Bilinçaltınız gündüz stresini yeni bir motivasyona dönüştürüyor.",
                tip = "Uyumadan önce nefesinizi yavaşlatın ve günün telaşını geride bırakın.",
            ),
            "hi" to LocalizedDreamText(
                title = "अग्नि",
                subtitle = "उत्साह, परिवर्तन और भावनात्मक शांति",
                summary = "सपने में आग देखना रचनात्मक ऊर्जा, गहरी भावनाओं या आत्म-नवीनीकरण का प्रतीक है।",
                insight = "अवचेतन मन दिन के तनाव को नई प्रेरणा में बदल रहा है।",
                tip = "सोने से पहले गहरी सांस लें और दिन की थकान को शांत होने दें।",
            ),
        ),
        "losing_teeth" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Rụng răng",
                subtitle = "Bước ngoặt cuộc sống, hình ảnh bản thân & lo âu",
                summary = "Mơ thấy rụng răng thường xuất hiện khi bạn đang trải qua thay đổi lớn trong công việc, cuộc sống hoặc cảm thấy áp lực trong giao tiếp.",
                insight = "Căng cơ hàm nhẹ ban đêm cũng dễ kích hoạt giấc mơ này trong giai đoạn REM.",
                tip = "Hãy tự nhủ rằng mọi sự thay đổi đều tự nhiên. Thả lỏng cơ hàm, hạ nhẹ hai vai và chìm vào giấc ngủ êm dịu.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Perda de Dentes",
                subtitle = "Transições de vida, autoimagem e preocupações",
                summary = "Sonhar com queda de dentes costuma surgir durante grandes mudanças de vida ou momentos de transição.",
                insight = "Uma leve tensão mandibular noturna também pode estimular esse sonho na fase REM.",
                tip = "Relaxe o maxilar e os ombros; você está seguro no momento presente.",
            ),
            "es" to LocalizedDreamText(
                title = "Caída de dientes",
                subtitle = "Cambios de vida, autoimagen y preocupaciones",
                summary = "Soñar con perder dientes suele aparecer durante transiciones importantes o cambios personales.",
                insight = "Relajar la mandíbula antes de dormir ayuda a reducir este tipo de sueños.",
                tip = "Relaja los hombros y la mandíbula y descansa con tranquilidad.",
            ),
            "fr" to LocalizedDreamText(
                title = "Perte de dents",
                subtitle = "Transitions de vie et confiance en soi",
                summary = "Ce rêve fréquent survient souvent lors de grands changements personnels ou professionnels.",
                insight = "Une légère tension de la mâchoire pendant le sommeil paradoxal peut aussi l'expliquer.",
                tip = "Détendez votre mâchoire et vos épaules avant de vous endormir.",
            ),
            "de" to LocalizedDreamText(
                title = "Zahnverlust",
                subtitle = "Lebenswandel, Selbstbild & innere Sorgen",
                summary = "Träume von ausfallenden Zähnen treten häufig in Phasen großer Veränderungen auf.",
                insight = "Leichte Kieferanspannung im Schlaf kann diese Traumbilder ebenfalls auslösen.",
                tip = "Lockern Sie Kiefer und Schultern und gleiten Sie entspannt in den Schlaf.",
            ),
            "ja" to LocalizedDreamText(
                title = "歯が抜ける夢",
                subtitle = "人生の転機・自己イメージ・変化への不安",
                summary = "環境の変化や新しいステージへの移行期に多く見られる普遍的な夢です。",
                insight = "睡眠中の軽い食いしばりが影響している場合もあります。",
                tip = "顎と肩の力を抜いて、安心感の中で深い眠りに入りましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "치아가 빠지는 꿈",
                subtitle = "삶의 전환기, 자아상 및 내면의 걱정",
                summary = "중요한 변화나 새로운 출발을 앞두고 있을 때 자주 나타나는 꿈입니다.",
                insight = "수면 중 가벼운 턱 긴장이 이러한 꿈을 유발하기도 합니다.",
                tip = "턱과 어깨의 긴장을 풀고 편안한 휴식에 빠져보세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Caduta dei denti",
                subtitle = "Transizioni di vita e autostima",
                summary = "Un tema onirico comune durante importanti cambiamenti personali o lavorativi.",
                insight = "Anche una lieve tensione mandibolare notturna può favorire questo sogno.",
                tip = "Rilassa mandibola e spalle e abbandonati a un sonno rigenerante.",
            ),
            "ru" to LocalizedDreamText(
                title = "Выпадение зубов",
                subtitle = "Жизненные перемены и внутренние волнения",
                summary = "Такой сон часто снится в периоды важных перемен и перехода на новый этап жизни.",
                insight = "Лёгкое напряжение челюсти во сне также может вызывать этот образ.",
                tip = "Расслабьте плечи и лицо, позвольте себе спокойный отдых.",
            ),
            "id" to LocalizedDreamText(
                title = "Gigi Tanggal",
                subtitle = "Transisi hidup, citra diri & kekhawatiran",
                summary = "Mimpi gigi copot sering muncul saat menghadapi perubahan besar dalam hidup atau karier.",
                insight = "Ketegangan rahang ringan saat tidur REM juga dapat memicu mimpi ini.",
                tip = "Rilekskan rahang dan bahu Anda, lalu nikmati istirahat malam yang tenang.",
            ),
            "tr" to LocalizedDreamText(
                title = "Diş Dökülmesi",
                subtitle = "Hayat geçişleri, öz imaj ve kaygılar",
                summary = "Önemli yaşam değişiklikleri veya yeni başlangıçlar sırasında sıkça görülen bir rüyadır.",
                insight = "Gece çene kaslarındaki hafif gerginlik de bu rüyayı tetikleyebilir.",
                tip = "Çenenizi ve omuzlarınızı gevşetin, huzurla uykuya dalın.",
            ),
            "hi" to LocalizedDreamText(
                title = "दांत टूटना",
                subtitle = "जीवन में बदलाव और आंतरिक चिंताएं",
                summary = "यह सपना अक्सर जीवन या करियर में बड़े बदलावों के समय आता है।",
                insight = "नींद में जबड़े का हल्का तनाव भी इस सपने का कारण बन सकता है।",
                tip = "अपने कंधों और चेहरे को ढीला छोड़ें और गहरी नींद का आनंद लें।",
            ),
        ),
        "being_chased" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Bị rượt đuổi",
                subtitle = "Sự trì hoãn, áp lực công việc & cảnh giác cao",
                summary = "Mơ thấy bị đuổi theo thường phản chiếu một công việc hoặc quyết định mà bạn đang tạm gác lại ban ngày.",
                insight = "Đại diện cho một trách nhiệm đang cần bạn giải quyết bình tĩnh vào ngày mai.",
                tip = "Không có việc gì cần xử lý ngay lúc này; đêm nay hoàn toàn thuộc về sự bình yên của bạn.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Ser Perseguido",
                subtitle = "Pressão de prazos e alerta elevado",
                summary = "Reflete uma tarefa ou decisão adiada durante o dia que pede atenção tranquila.",
                insight = "Representa uma responsabilidade pendente e não uma ameaça real.",
                tip = "Nada exige sua ação agora; esta noite pertence à sua paz.",
            ),
            "es" to LocalizedDreamText(
                title = "Ser perseguido",
                subtitle = "Presión diaria y tareas pendientes",
                summary = "Suele reflejar una tarea o emoción aplazada durante el día.",
                insight = "Representa una responsabilidad pendiente que podrás resolver con calma mañana.",
                tip = "Las tareas de mañana pueden esperar; esta noche es solo para descansar.",
            ),
            "fr" to LocalizedDreamText(
                title = "Être poursuivi",
                subtitle = "Pression quotidienne et vigilance",
                summary = "Reflète souvent une tâche ou une décision repoussée pendant la journée.",
                insight = "Symbolise une préoccupation qui demande une attention calme au réveil.",
                tip = "Rien ne presse ce soir ; accordez-vous un repos complet.",
            ),
            "de" to LocalizedDreamText(
                title = "Verfolgt werden",
                subtitle = "Termindruck & innere Anspannung",
                summary = "Spiegelt oft eine aufgeschobene Aufgabe oder Entscheidung aus dem Alltag wider.",
                insight = "Steht für eine unerledigte Angelegenheit, die morgen in Ruhe gelöst werden kann.",
                tip = "Heute Nacht müssen Sie nichts erledigen – genießen Sie Ihre Ruhe.",
            ),
            "ja" to LocalizedDreamText(
                title = "追いかけられる夢",
                subtitle = "プレッシャー・先延ばし・緊張感",
                summary = "日中に気になっていた課題やプレッシャーが夢に表れたものです。",
                insight = "外的な脅威ではなく、落ち着いて向き合いたい課題の象徴です。",
                tip = "明日の予定は朝まで忘れて大丈夫。今夜はゆっくり休みましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "쫓기는 꿈",
                subtitle = "마감 압박감 및 긴장 상태",
                summary = "낮 동안 미뤄둔 일이나 심리적 부담감이 꿈속에서 추격전으로 나타난 것입니다.",
                insight = "내일 차분히 해결할 수 있는 일시적인 스트레스 신호입니다.",
                tip = "지금 당장 해야 할 일은 없습니다. 오늘 밤은 온전히 휴식에 집중하세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Essere inseguiti",
                subtitle = "Pressione quotidiana e impegni",
                summary = "Riflette un impegno o una decisione rimandata durante la giornata.",
                insight = "Rappresenta una responsabilità che potrai affrontare con calma domani.",
                tip = "Gli impegni possono attendere il mattino; ora pensa solo a riposare.",
            ),
            "ru" to LocalizedDreamText(
                title = "Погоня во сне",
                subtitle = "Дневная спешка и отложенные дела",
                summary = "Отражает задачу или решение, которые вы откладывали в течение дня.",
                insight = "Напоминание о делах, которые можно спокойно решить завтра.",
                tip = "Сегодня ночью никуда не нужно спешить — время для глубокого отдыха.",
            ),
            "id" to LocalizedDreamText(
                title = "Dikejar",
                subtitle = "Tekanan tenggat waktu & kewaspadaan",
                summary = "Mencerminkan tugas atau keputusan yang tertunda di siang hari.",
                insight = "Melambangkan tanggung jawab yang bisa diselesaikan dengan tenang besok.",
                tip = "Tidak ada yang perlu dikerjakan malam ini; nikmati ketenangan Anda.",
            ),
            "tr" to LocalizedDreamText(
                title = "Kovalanmak",
                subtitle = "Günlük stres ve ertelenen işler",
                summary = "Gün içinde ertelediğiniz bir görevi veya stres kaynağını yansıtır.",
                insight = "Yarın sakinlikle çözebileceğiniz bir sorumluluğu temsil eder.",
                tip = "Şu anda yapmanız gereken hiçbir şey yok; bu gece sadece dinlenin.",
            ),
            "hi" to LocalizedDreamText(
                title = "पीछा किया जाना",
                subtitle = "दिनभर का दबाव और अधूरे कार्य",
                summary = "यह सपना किसी टाले गए काम या निर्णय के मानसिक दबाव को दर्शाता है।",
                insight = "यह किसी बाहरी खतरे का नहीं, बल्कि कल शांत मन से सुलझाए जाने वाले काम का संकेत है।",
                tip = "अभी कुछ भी करने की आवश्यकता नहीं है; यह रात आपकी शांति के लिए है।",
            ),
        ),
        "falling" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Rơi tự do",
                subtitle = "Buông bỏ kiểm soát & chuyển sang giấc ngủ sâu",
                summary = "Giấc mơ rơi từ trên cao thường xảy ra ở giai đoạn chuyển giao giữa lúc thức và giấc ngủ nông khi hệ thần kinh bắt đầu thả lỏng hoàn toàn.",
                insight = "Dấu hiệu tốt cho thấy vỏ não vận động đang nghỉ ngơi để phục hồi thể chất.",
                tip = "Hãy thả lỏng toàn thân chìm sâu vào nệm và gối.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Queda Livre",
                subtitle = "Soltar o controle e entrar no sono profundo",
                summary = "Ocorre frequentemente na transição entre a vigília e o sono leve enquanto os músculos relaxam.",
                insight = "Sinal saudável de que o sistema nervoso está desligando para recuperação profunda.",
                tip = "Afunde suavemente no travesseiro e imagine flutuar em águas calmas.",
            ),
            "es" to LocalizedDreamText(
                title = "Caída libre",
                subtitle = "Soltar el control y entrar en sueño profundo",
                summary = "Ocurre al pasar de la vigilia al sueño ligero mientras el cuerpo libera tensión.",
                insight = "Es una señal saludable de que tu sistema nervioso se prepara para descansar.",
                tip = "Húndete en tu almohada e imagina flotar sobre agua tranquila.",
            ),
            "fr" to LocalizedDreamText(
                title = "Chute libre",
                subtitle = "Lâcher-prise et transition vers le sommeil profond",
                summary = "Survient souvent lors de l'endormissement lorsque le système nerveux relâche les tensions.",
                insight = "Signe naturel que votre corps entre en récupération physique.",
                tip = "Laissez-vous porter par votre matelas comme sur une mer calme.",
            ),
            "de" to LocalizedDreamText(
                title = "Freier Fall",
                subtitle = "Loslassen & Übergang in den Tiefschlaf",
                summary = "Tritt häufig beim Einschlafen auf, wenn das Nervensystem die Tagesanspannung abbaut.",
                insight = "Ein gesundes Zeichen dafür, dass Ihr Körper in die Tiefenerholung wechselt.",
                tip = "Sinken Sie tief in Ihr Kissen und stellen Sie sich ruhiges Wasser vor.",
            ),
            "ja" to LocalizedDreamText(
                title = "落下する夢",
                subtitle = "緊張の解放・深い眠りへの移行",
                summary = "入眠時に神経系が日中の緊張を解き放つ際によく見られる夢です。",
                insight = "身体が深い休息モードへ切り替わっている健康的なサインです。",
                tip = "枕に身を委ね、穏やかな水面に浮かんでいるイメージをしてみましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "추락하는 꿈",
                subtitle = "긴장 완화 및 깊은 수면으로의 전환",
                summary = "잠들기 직전 신경계가 낮 동안의 긴장을 내려놓을 때 자주 나타납니다.",
                insight = "신체가 깊은 회복 단계로 들어가고 있다는 건강한 신호입니다.",
                tip = "매트리스에 몸을 편안히 맡기고 잔잔한 물 위에 떠 있는 상상을 해보세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Cadere nel vuoto",
                subtitle = "Lasciare andare il controllo e dormire profondamente",
                summary = "Si verifica spesso nella fase di addormentamento mentre il corpo scioglie le tensioni.",
                insight = "Segnale naturale che il sistema nervoso sta entrando in pieno recupero.",
                tip = "Sprofonda nel cuscino e immagina di galleggiare sull'acqua calma.",
            ),
            "ru" to LocalizedDreamText(
                title = "Падение",
                subtitle = "Снятие напряжения и переход в глубокий сон",
                summary = "Часто возникает при засыпании, когда нервная система отпускает дневное напряжение.",
                insight = "Естественный признак перехода организма к глубокому восстановлению.",
                tip = "Погрузитесь в мягкую подушку и представьте спокойную морскую гладь.",
            ),
            "id" to LocalizedDreamText(
                title = "Terjatuh",
                subtitle = "Melepas kendali & transisi ke tidur nyenyak",
                summary = "Sering terjadi saat transisi menuju tidur ketika sistem saraf melepaskan ketegangan.",
                insight = "Tanda sehat bahwa tubuh Anda sedang memulai pemulihan fisik mendalam.",
                tip = "Rebahkan tubuh dengan nyaman dan bayangkan mengapung di air tenang.",
            ),
            "tr" to LocalizedDreamText(
                title = "Düşmek",
                subtitle = "Kontrolü bırakma ve derin uykuya geçiş",
                summary = "Uykuya dalarken sinir sisteminin günün gerginliğini bırakmasıyla sıkça görülür.",
                insight = "Vücudunuzun derin dinlenme moduna geçtiğinin sağlıklı bir işaretidir.",
                tip = "Yastığınıza rahatça gömülün ve sakin sularda süzüldüğünüzü hayal edin.",
            ),
            "hi" to LocalizedDreamText(
                title = "ऊंचाई से गिरना",
                subtitle = "तनाव मुक्त होना और गहरी नींद में प्रवेश",
                summary = "यह सपना अक्सर नींद की शुरुआत में आता है जब तंत्रिका तंत्र तनाव छोड़ता है।",
                insight = "यह संकेत है कि आपका शरीर गहरी शारीरिक रिकवरी के लिए तैयार हो रहा है।",
                tip = "तकिए पर आराम से लेट जाएं और शांत पानी पर तैरने की कल्पना करें।",
            ),
        ),
        "flying" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Bay lượn",
                subtitle = "Sự tự do, minh mẫn & vượt qua trở ngại",
                summary = "Bay lượn nhẹ nhàng trên bầu trời là biểu tượng của sự giải phóng, góc nhìn mới mẻ và cảm giác trút bỏ gánh nặng.",
                insight = "Gắn liền với những bước đột phá sáng tạo và sự nhẹ nhõm trong tâm hồn.",
                tip = "Hãy mang cảm giác nhẹ bẫng ấy vào nhịp thở 4-7-8 đêm nay.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Voar",
                subtitle = "Liberdade, clareza e superação",
                summary = "Voar pelo céu simboliza libertação, nova perspectiva e alívio de um peso recente.",
                insight = "Associado a momentos de inspiração criativa e equilíbrio emocional.",
                tip = "Leve essa sensação de leveza para sua respiração 4-7-8 esta noite.",
            ),
            "es" to LocalizedDreamText(
                title = "Volar",
                subtitle = "Libertad, claridad y superación",
                summary = "Volar por el cielo simboliza liberación, nuevas perspectivas y alivio emocional.",
                insight = "Se asocia con creatividad y bienestar interior.",
                tip = "Lleva esa sensación de ligereza a tu respiración tranquila esta noche.",
            ),
            "fr" to LocalizedDreamText(
                title = "Voler dans les airs",
                subtitle = "Liberté, clarté et dépassement",
                summary = "S'élever dans le ciel symbolise la liberté, un regard neuf et l'apaisement.",
                insight = "Reflète un sentiment d'harmonie et d'inspiration créative.",
                tip = "Respirez selon le rythme 4-7-8 en gardant cette sensation de légèreté.",
            ),
            "de" to LocalizedDreamText(
                title = "Fliegen",
                subtitle = "Freiheit, Klarheit & Leichtigkeit",
                summary = "Müheloses Fliegen steht für Befreiung, neue Perspektiven und innere Ruhe.",
                insight = "Verbunden mit kreativen Impulsen und emotionaler Ausgeglichenheit.",
                tip = "Nehmen Sie dieses Gefühl von Schwerelosigkeit mit in Ihre 4-7-8-Atmung.",
            ),
            "ja" to LocalizedDreamText(
                title = "空を飛ぶ夢",
                subtitle = "自由・明晰さ・障害の克服",
                summary = "大空を自由に飛ぶ夢は、解放感や新しい視点、心の軽やかさを象徴しています。",
                insight = "創造性の高まりや前向きな気持ちの表れです。",
                tip = "その心地よい浮遊感のまま、4-7-8呼吸法でゆっくり眠りましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "하늘을 나는 꿈",
                subtitle = "자유, 명료함 및 장애물 극복",
                summary = "하늘을 자유롭게 나는 꿈은 해방감과 새로운 시각, 마음의 평온을 상징합니다.",
                insight = "창의적인 영감과 긍정적인 정서 상태와 연결됩니다.",
                tip = "가벼워진 마음으로 4-7-8 호흡을 따라 깊고 편안하게 잠들어 보세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Volare",
                subtitle = "Libertà, chiarezza e leggerezza",
                summary = "Volare nel cielo è simbolo di liberazione, nuove prospettive e serenità.",
                insight = "Riflette ispirazione creativa ed equilibrio interiore.",
                tip = "Porta questo senso di leggerezza nel tuo respiro lento prima di dormire.",
            ),
            "ru" to LocalizedDreamText(
                title = "Полёт",
                subtitle = "Свобода, ясность и преодоление преград",
                summary = "Парение в небе символизирует внутреннюю свободу, вдохновение и лёгкость.",
                insight = "Связан с творческим подъёмом и душевным равновесием.",
                tip = "Сохраните это ощущение невесомости, дыша в спокойном ритме 4-7-8.",
            ),
            "id" to LocalizedDreamText(
                title = "Terbang",
                subtitle = "Kebebasan, kejernihan & melampaui rintangan",
                summary = "Melayang di angkasa melambangkan kebebasan, perspektif baru, dan kelegaan hati.",
                insight = "Berkaitan dengan inspirasi kreatif dan ketenangan batin.",
                tip = "Bawa rasa ringan itu ke dalam napas 4-7-8 Anda malam ini.",
            ),
            "tr" to LocalizedDreamText(
                title = "Uçmak",
                subtitle = "Özgürlük, berraklık ve engelleri aşma",
                summary = "Gökyüzünde süzülmek özgürlüğü, yeni bir bakış açısını ve iç huzuru simgeler.",
                insight = "Yaratıcı ilham ve duygusal denge ile bağlantılıdır.",
                tip = "Bu hafiflik hissini 4-7-8 nefes egzersizinizle uykuya taşıyın.",
            ),
            "hi" to LocalizedDreamText(
                title = "आकाश में उड़ना",
                subtitle = "स्वतंत्रता, स्पष्टता और हल्कापन",
                summary = "आसमान में उड़ना मुक्ति, नए दृष्टिकोण और मानसिक शांति का प्रतीक है।",
                insight = "यह रचनात्मक ऊर्जा और आंतरिक संतुलन को दर्शाता है।",
                tip = "इस हल्केपन के एहसास के साथ 4-7-8 श्वास लेते हुए सो जाएं।",
            ),
        ),
        "snake_bite" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Rắn & Bản năng",
                subtitle = "Sự thức tỉnh, trực giác tiềm ẩn & chữa lành",
                summary = "Hình ảnh rắn tượng trưng cho sự tái sinh và trực giác nhạy bén, nhắc bạn chăm sóc sự cân bằng nội tâm.",
                insight = "Tâm trí sử dụng hình ảnh bản năng để nhắc bạn bảo vệ năng lượng cảm xúc.",
                tip = "Buông bỏ căng thẳng và lắng nghe tiếng mưa rừng đều đặn.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Serpente & Instinto",
                subtitle = "Despertar, intuição e cura interior",
                summary = "A serpente representa renovação, sabedoria intuitiva e proteção emocional.",
                insight = "Sua mente destaca áreas onde sua energia merece cuidado e descanso.",
                tip = "Solte a tensão e ouça o som constante da chuva na floresta.",
            ),
            "es" to LocalizedDreamText(
                title = "Serpiente e instinto",
                subtitle = "Renovación, intuición y sanación",
                summary = "Representa renovación interior e intuición subconsciente.",
                insight = "Tu mente te invita a cuidar tus límites y tu bienestar emocional.",
                tip = "Relájate escuchando el sonido suave de la lluvia en el bosque.",
            ),
            "fr" to LocalizedDreamText(
                title = "Serpent et intuition",
                subtitle = "Renouveau, instinct et guérison",
                summary = "Symbole classique de renouveau et d'écoute de votre intuition profonde.",
                insight = "Votre esprit vous rappelle de préserver votre énergie émotionnelle.",
                tip = "Laissez la pluie de forêt apaiser vos pensées ce soir.",
            ),
            "de" to LocalizedDreamText(
                title = "Schlange & Intuition",
                subtitle = "Erneuerung, Instinkt & Heilung",
                summary = "Steht in der Traumdeutung für Erneuerung, Heilung und feine Intuition.",
                insight = "Erinnert Sie daran, auf Ihre emotionale Balance zu achten.",
                tip = "Lassen Sie sich vom sanften Waldregen beruhigen und erden.",
            ),
            "ja" to LocalizedDreamText(
                title = "ヘビと直感",
                subtitle = "再生・潜在的な直感・癒やし",
                summary = "ヘビは再生と鋭い直感の象徴であり、心身のバランスを整えるサインです。",
                insight = "自分のエネルギーや心の境界線を大切にするよう伝えています。",
                tip = "穏やかな森の雨音に耳を傾け、心を落ち着かせましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "뱀과 직관",
                subtitle = "재탄생, 내면의 직관 및 치유",
                summary = "뱀은 변화와 치유, 그리고 깊은 직관을 일깨우는 상징입니다.",
                insight = "정서적 에너지를 보호하고 휴식을 취하라는 내면의 메시지입니다.",
                tip = "규칙적인 숲속 빗소리를 들으며 마음의 긴장을 내려놓으세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Serpente e intuito",
                subtitle = "Rinnovamento, istinto e guarigione",
                summary = "Rappresenta la rigenerazione interiore e la saggezza dell'intuito.",
                insight = "Invita a proteggere il proprio equilibrio emotivo.",
                tip = "Ascolta la pioggia nella foresta per ritrovare calma e serenità.",
            ),
            "ru" to LocalizedDreamText(
                title = "Змея и интуиция",
                subtitle = "Обновление, внутреннее чутьё и исцеление",
                summary = "Символ обновления, мудрости и внимания к своему внутреннему голосу.",
                insight = "Подсознание напоминает о важности бережного отношения к своим силам.",
                tip = "Сосредоточьтесь на спокойном шуме лесного дождя.",
            ),
            "id" to LocalizedDreamText(
                title = "Ular & Intuisi",
                subtitle = "Pembaruan, naluri & pemulihan batin",
                summary = "Melambangkan pembaruan diri dan kepekaan intuisi bawah sadar.",
                insight = "Mengingatkan Anda untuk menjaga keseimbangan dan energi emosional.",
                tip = "Dengarkan rintik hujan hutan yang menenangkan pikiran.",
            ),
            "tr" to LocalizedDreamText(
                title = "Yılan ve Sezgi",
                subtitle = "Yenilenme, içgüdü ve şifa",
                summary = "Yenilenmeyi ve içsel sezgilerinizi dinlemeniz gerektiğini simgeler.",
                insight = "Duygusal enerjinizi korumanız için nazik bir hatırlatmadır.",
                tip = "Orman yağmurunun huzurlu sesiyle zihninizi dinlendirin.",
            ),
            "hi" to LocalizedDreamText(
                title = "सर्प और अंतर्ज्ञान",
                subtitle = "नवीनीकरण, सहज ज्ञान और उपचार",
                summary = "यह आत्म-नवीनीकरण और गहरे अंतर्ज्ञान का प्रतीक है।",
                insight = "आपका मन आपको अपनी भावनात्मक ऊर्जा की रक्षा करने की याद दिलाता है।",
                tip = "शांत जंगल की बारिश की आवाज़ सुनते हुए तनाव मुक्त हो जाएं।",
            ),
        ),
        "fish" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Cá & Nước trong",
                subtitle = "Trực giác sâu sắc, sự sung túc & tĩnh lặng",
                summary = "Đàn cá bơi trong làn nước trong vắt tượng trưng cho những ý tưởng mới và bình yên từ sâu trong tiềm thức.",
                insight = "Phản ánh hệ thần kinh phó giao cảm đang thư giãn và cân bằng.",
                tip = "Lắng nghe nhịp sóng biển dịu êm và để suy nghĩ trôi nhẹ nhàng.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Peixes em Água Cristalina",
                subtitle = "Intuição, abundância e tranquilidade",
                summary = "Simboliza ideias claras, harmonia emocional e paz interior.",
                insight = "Imagens aquáticas calmas refletem equilíbrio e relaxamento profundo.",
                tip = "Ouça o ritmo suave das ondas do mar e relaxe.",
            ),
            "es" to LocalizedDreamText(
                title = "Peces en agua clara",
                subtitle = "Intuición, abundancia y serenidad",
                summary = "Simboliza armonía emocional e ideas claras que surgen del subconsciente.",
                insight = "El agua cristalina refleja calma y equilibrio interior.",
                tip = "Escucha el suave ritmo de las olas del mar para dormir mejor.",
            ),
            "fr" to LocalizedDreamText(
                title = "Poissons en eau claire",
                subtitle = "Intuition, abondance et sérénité",
                summary = "Symbolise l'harmonie émotionnelle et la clarté de l'esprit.",
                insight = "Les images d'eau calme reflètent une profonde détente nerveuse.",
                tip = "Écoutez le doux rythme des vagues pour vous endormir.",
            ),
            "de" to LocalizedDreamText(
                title = "Fische im klaren Wasser",
                subtitle = "Intuition, Fülle & Gelassenheit",
                summary = "Symbolisiert emotionale Harmonie und klare Gedanken aus dem Unterbewusstsein.",
                insight = "Ruhiges Wasser spiegelt tiefe Entspannung wider.",
                tip = "Lauschen Sie dem sanften Rhythmus der Meereswellen.",
            ),
            "ja" to LocalizedDreamText(
                title = "澄んだ水と魚",
                subtitle = "直感・豊かさ・心の平穏",
                summary = "澄んだ水の中を泳ぐ魚は、心の調和と穏やかな気づきを象徴しています。",
                insight = "副交感神経が優位になり、心が落ち着いているサインです。",
                tip = "優しい波の音に身を委ねて安らかに眠りましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "맑은 물과 물고기",
                subtitle = "깊은 직관, 풍요로움 및 평온",
                summary = "맑은 물속을 헤엄치는 물고기는 정서적 조화와 평화로운 마음을 상징합니다.",
                insight = "마음과 신경계가 안정되어 있음을 보여줍니다.",
                tip = "잔잔한 파도 소리를 들으며 편안한 밤을 보내세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Pesci in acqua limpida",
                subtitle = "Intuizione, abbondanza e tranquillità",
                summary = "Simboleggia armonia emotiva e chiarezza interiore.",
                insight = "L'acqua limpida riflette rilassamento e serenità.",
                tip = "Ascolta il dolce ritmo delle onde dell'oceano.",
            ),
            "ru" to LocalizedDreamText(
                title = "Рыбы в чистой воде",
                subtitle = "Интуиция, изобилие и умиротворение",
                summary = "Символизирует душевную гармонию и светлые мысли.",
                insight = "Чистая спокойная вода отражает внутреннее равновесие.",
                tip = "Прислушайтесь к мягкому шуму океанских волн.",
            ),
            "id" to LocalizedDreamText(
                title = "Ikan di Air Jernih",
                subtitle = "Intuisi, kelimpahan & ketenangan",
                summary = "Melambangkan harmoni emosional dan kejernihan pikiran.",
                insight = "Air yang tenang mencerminkan relaksasi batin yang mendalam.",
                tip = "Dengarkan alunan ombak laut yang lembut malam ini.",
            ),
            "tr" to LocalizedDreamText(
                title = "Berrak Suda Balık",
                subtitle = "Sezgi, bolluk ve huzur",
                summary = "Duygusal uyumu, berrak düşünceleri ve iç huzuru simgeler.",
                insight = "Sakin su imgeleri zihinsel dinginliği yansıtır.",
                tip = "Okyanus dalgalarının ritmiyle huzurla uykuya dalın.",
            ),
            "hi" to LocalizedDreamText(
                title = "साफ़ पानी में मछलियाँ",
                subtitle = "अंतर्ज्ञान, समृद्धि और शांति",
                summary = "यह भावनात्मक संतुलन और मन की शांति का प्रतीक है।",
                insight = "शांत जल मानसिक स्पष्टता और विश्राम को दर्शाता है।",
                tip = "समुद्र की लहरों की मधुर ध्वनि सुनते हुए सो जाएं।",
            ),
        ),
        "tornado" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Bão & Gió lốc",
                subtitle = "Cơn lốc cảm xúc & bầu trời quang đãng sau mưa",
                summary = "Phản ánh lịch trình bận rộn ban ngày đang cần được sắp xếp lại một cách êm dịu.",
                insight = "Sau giấc mơ về cơn bão, tâm trí thường giải tỏa được sự quá tải thông tin.",
                tip = "Mọi cơn bão rồi đều nhường chỗ cho sự tĩnh lặng.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Tempestade",
                subtitle = "Redemoinho emocional e calmaria após a chuva",
                summary = "Reflete mudanças rápidas na rotina que buscam resolução tranquila.",
                insight = "Após sonhos de tempestade, a mente costuma sentir alívio mental.",
                tip = "Toda tempestade passa; acolha o som suave da chuva na janela.",
            ),
            "es" to LocalizedDreamText(
                title = "Tormenta",
                subtitle = "Liberación emocional y calma tras la lluvia",
                summary = "Refleja días intensos que buscan orden y serenidad.",
                insight = "Ayuda a despejar el exceso de pensamientos acumulados.",
                tip = "Toda tormenta da paso a la calma; relájate con la lluvia suave.",
            ),
            "fr" to LocalizedDreamText(
                title = "Tempête et vent",
                subtitle = "Tourbillon émotionnel et retour au calme",
                summary = "Reflète un emploi du temps chargé qui appelle au repos.",
                insight = "Permet à l'esprit de libérer la surcharge mentale.",
                tip = "Après la pluie vient le calme ; laissez-vous bercer par la pluie sur la vitre.",
            ),
            "de" to LocalizedDreamText(
                title = "Sturm & Gewitter",
                subtitle = "Emotionale Klärung & Ruhe nach dem Sturm",
                summary = "Spiegelt viele gleichzeitige Eindrücke wider, die sich im Schlaf ordnen.",
                insight = "Hilft dem Geist, gedanklichen Ballast loszulassen.",
                tip = "Jeder Sturm zieht vorbei – entspannen Sie bei sanftem Regen am Fenster.",
            ),
            "ja" to LocalizedDreamText(
                title = "嵐と 바람",
                subtitle = "感情の整理・雨上がりの静けさ",
                summary = "忙しい日常や多くの出来事を心が整理しているサインです。",
                insight = "嵐の夢の後は、頭の中がすっきりと落ち着きやすくなります。",
                tip = "窓を叩く優しい雨音を聴きながら、穏やかな夜を過ごしましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "폭풍과 바람",
                subtitle = "감정의 정화 및 비 개인 뒤의 평온",
                summary = "바쁜 일상과 복잡한 생각들을 마음이 정리하고 있음을 나타냅니다.",
                insight = "복잡했던 머릿속이 한결 가벼워지는 과정입니다.",
                tip = "창가에 내리는 잔잔한 빗소리와 함께 평온하게 휴식하세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Tempesta",
                subtitle = "Rilascio emotivo e quiete dopo la pioggia",
                summary = "Riflette giornate intense che cercano calma e ordine interiore.",
                insight = "Aiuta la mente a liberarsi dal sovraccarico di pensieri.",
                tip = "Ogni tempesta lascia spazio alla quiete; ascolta la pioggia sulla finestra.",
            ),
            "ru" to LocalizedDreamText(
                title = "Гроза и ветер",
                subtitle = "Очищение мыслей и тишина после дождя",
                summary = "Отражает насыщенный ритм дня и потребность в тишине.",
                insight = "Помогает освободить разум от информационной перегрузки.",
                tip = "Любая буря сменяется покоем под уютный шум дождя за окном.",
            ),
            "id" to LocalizedDreamText(
                title = "Badai & Angin",
                subtitle = "Pelepasan emosi & ketenangan setelah hujan",
                summary = "Mencerminkan jadwal padat yang membutuhkan waktu istirahat.",
                insight = "Membantu pikiran melepaskan beban pikiran yang menumpuk.",
                tip = "Setiap badai pasti berlalu; nikmati suara hujan lembut di jendela.",
            ),
            "tr" to LocalizedDreamText(
                title = "Fırtına",
                subtitle = "Duygusal arınma ve yağmur sonrası dinginlik",
                summary = "Yoğun geçen günlerin ardından zihnin dinlenme ihtiyacını yansıtır.",
                insight = "Zihinsel yorgunluğun atılmasına yardımcı olur.",
                tip = "Her fırtına dinginliğe ulaşır; cama vuran yağmur sesiyle rahatlayın.",
            ),
            "hi" to LocalizedDreamText(
                title = "तूफ़ान और हवा",
                subtitle = "भावनात्मक शुद्धि और बारिश के बाद की शांति",
                summary = "यह व्यस्त दिनचर्या के बाद मन के शांत होने की प्रक्रिया को दर्शाता है।",
                insight = "यह मानसिक थकान को दूर करने में सहायक होता है।",
                tip = "खिड़की पर गिरती हल्की बारिश की आवाज़ के साथ गहरी नींद लें।",
            ),
        ),
        "baby" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Em bé",
                subtitle = "Khởi đầu mới, sự thuần khiết & ý tưởng mới",
                summary = "Đại diện cho một dự định mới, một chương mới đầy hy vọng trong cuộc sống.",
                insight = "Xuất hiện khi bạn đang ấp ủ một mục tiêu hoặc kỹ năng tốt đẹp.",
                tip = "Hãy dịu dàng với bản thân tối nay và tận hưởng giấc ngủ sâu trọn vẹn.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Bebê",
                subtitle = "Novos começos, ternura e ideias novas",
                summary = "Representa um novo projeto, uma nova fase da vida ou cuidado consigo mesmo.",
                insight = "Surge quando você está cultivando novos objetivos e esperanças.",
                tip = "Trate-se com gentileza hoje à noite e descanse profundamente.",
            ),
            "es" to LocalizedDreamText(
                title = "Bebé",
                subtitle = "Nuevos comienzos, ternura y nuevas ideas",
                summary = "Representa un nuevo proyecto o una etapa llena de ilusión en tu vida.",
                insight = "Simboliza crecimiento interior y cuidado personal.",
                tip = "Trátate con amabilidad esta noche y disfruta de un sueño reparador.",
            ),
            "fr" to LocalizedDreamText(
                title = "Nouveau-né",
                subtitle = "Nouveaux départs, douceur et créativité",
                summary = "Représente un nouveau projet ou un chapitre prometteur de votre vie.",
                insight = "Symbolise la bienveillance et l'éclosion de nouvelles idées.",
                tip = "Soyez doux avec vous-même ce soir et dormez paisiblement.",
            ),
            "de" to LocalizedDreamText(
                title = "Neugeborenes",
                subtitle = "Neubeginn, Geborgenheit & frische Ideen",
                summary = "Steht für ein neues Lebenskapitel, ein Projekt oder liebevolle Selbstfürsorge.",
                insight = "Zeigt inneres Wachstum und neue Zuversicht an.",
                tip = "Gönnen Sie sich heute Nacht sanfte Ruhe und tiefen Schlaf.",
            ),
            "ja" to LocalizedDreamText(
                title = "赤ちゃんの夢",
                subtitle = "新しい始まり・純粋さ・新たなアイデア",
                summary = "新しい計画や人生の新章、大切に育てたい目標を象徴しています。",
                insight = "前向きな成長や優しい気持ちを反映しています。",
                tip = "今夜は自分自身をいたわり、穏やかなピアノの音色で眠りましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "아기 꿈",
                subtitle = "새로운 시작, 순수함 및 희망찬 아이디어",
                summary = "새로운 계획이나 삶의 새로운 장, 소중히 돌보아야 할 목표를 상징합니다.",
                insight = "내면의 성장과 긍정적인 기대감을 나타냅니다.",
                tip = "오늘 밤 자신을 따뜻하게 격려하며 깊은 잠에 빠져보세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Neonato",
                subtitle = "Nuovi inizi, dolcezza e nuove idee",
                summary = "Rappresenta un nuovo progetto o un nuovo capitolo ricco di speranza.",
                insight = "Simboleggia crescita personale e cura di sé.",
                tip = "Prenditi cura di te stasera e concediti un riposo profondo.",
            ),
            "ru" to LocalizedDreamText(
                title = "Младенец",
                subtitle = "Новое начало, чистота и свежие идеи",
                summary = "Символизирует новое начинание или светлую главу вашей жизни.",
                insight = "Отражает бережное отношение к себе и своим мечтам.",
                tip = "Подарите себе спокойный и непрерывный ночной отдых.",
            ),
            "id" to LocalizedDreamText(
                title = "Bayi",
                subtitle = "Awal baru, ketulusan & ide segar",
                summary = "Melambangkan rencana baru atau babak baru yang penuh harapan.",
                insight = "Mencerminkan pertumbuhan diri dan perhatian yang lembut.",
                tip = "Berikan istirahat terbaik bagi diri Anda malam ini.",
            ),
            "tr" to LocalizedDreamText(
                title = "Bebek",
                subtitle = "Yeni başlangıçlar, saflık ve taze fikirler",
                summary = "Hayatınızda yeni bir projeyi veya umut dolu yeni bir dönemi temsil eder.",
                insight = "Kişisel gelişim ve şefkat duygusunu simgeler.",
                tip = "Bu gece kendinize nazik davranın ve kesintisiz bir uyku uyuyun.",
            ),
            "hi" to LocalizedDreamText(
                title = "नवजात शिशु",
                subtitle = "नई शुरुआत, मासूमियत और नए विचार",
                summary = "यह जीवन के एक नए अध्याय या नई योजना का प्रतीक है।",
                insight = "यह सकारात्मक विकास और आत्म-देखभाल को दर्शाता है।",
                tip = "आज रात स्वयं को पूर्ण और शांत विश्राम दें।",
            ),
        ),
        "dog" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Chú chó trung thành",
                subtitle = "Sự đồng hành, niềm tin & sự che chở nội tâm",
                summary = "Tượng trưng cho lòng trung thành, tình bạn đáng tin cậy và cảm giác an toàn.",
                insight = "Củng cố cảm giác bình an và kết nối yêu thương.",
                tip = "Bạn đang được an toàn và chở che. Hãy để tiếng dế đêm đưa bạn vào giấc ngủ ngon.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Cão Leal",
                subtitle = "Companheirismo, confiança e proteção",
                summary = "Simboliza lealdade, amizades verdadeiras e segurança emocional.",
                insight = "Fortalece a sensação de acolhimento e tranquilidade.",
                tip = "Você está seguro; deixe os sons suaves da noite embalarem seu sono.",
            ),
            "es" to LocalizedDreamText(
                title = "Perro leal",
                subtitle = "Compañía, confianza y protección interior",
                summary = "Simboliza lealtad, amistad sincera y seguridad emocional.",
                insight = "Refuerza la sensación de calma y apoyo afectivo.",
                tip = "Estás en calma y seguro; deja que los sonidos nocturnos te acompañen.",
            ),
            "fr" to LocalizedDreamText(
                title = "Chien fidèle",
                subtitle = "Fidélité, confiance et protection",
                summary = "Incarne la loyauté, l'amitié sincère et la sécurité émotionnelle.",
                insight = "Renforce le sentiment de confiance et de sérénité.",
                tip = "Vous êtes en sécurité ; laissez le calme de la nuit vous bercer.",
            ),
            "de" to LocalizedDreamText(
                title = "Treuer Hund",
                subtitle = "Vertrauen, Verbundenheit & Geborgenheit",
                summary = "Verkörpert Loyalität, verlässliche Freundschaft und inneren Schutz.",
                insight = "Stärkt das Gefühl von Sicherheit und emotionaler Ruhe.",
                tip = "Sie sind sicher und geborgen – schlafen Sie entspannt ein.",
            ),
            "ja" to LocalizedDreamText(
                title = "忠実な犬",
                subtitle = "信頼・絆・心の安心感",
                summary = "誠実な友情や信頼関係、守られている安心感を象徴しています。",
                insight = "心が温かい繋がりと安らぎを感じているサインです。",
                tip = "静かな夜の虫の声に包まれて、安心して眠りましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "충직한 강아지",
                subtitle = "동행, 신뢰 및 내면의 보호",
                summary = "신뢰할 수 있는 우정과 정서적 안정감, 따뜻한 보호를 상징합니다.",
                insight = "마음속 깊은 곳의 평안함과 유대감을 나타냅니다.",
                tip = "고요한 밤의 소리에 귀 기울이며 편안하게 잠드세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Cane fedele",
                subtitle = "Fedeltà, fiducia e protezione",
                summary = "Simboleggia amicizia sincera, lealtà e sicurezza emotiva.",
                insight = "Rafforza il senso di protezione e serenità interiore.",
                tip = "Sei al sicuro; lasciati cullare dalla quiete della notte.",
            ),
            "ru" to LocalizedDreamText(
                title = "Верная собака",
                subtitle = "Дружба, доверие и внутренняя защита",
                summary = "Воплощает преданность, надёжную дружбу и чувство безопасности.",
                insight = "Укрепляет ощущение душевного тепла и спокойствия.",
                tip = "Вы в безопасности — пусть тихие звуки ночи подарят крепкий сон.",
            ),
            "id" to LocalizedDreamText(
                title = "Anjing Setia",
                subtitle = "Persahabatan, kepercayaan & rasa aman",
                summary = "Melambangkan kesetiaan, persahabatan tulus, dan ketenangan hati.",
                insight = "Memperkuat rasa nyaman dan terlindungi.",
                tip = "Anda aman dan tenang; biarkan suasana malam mengantar tidur Anda.",
            ),
            "tr" to LocalizedDreamText(
                title = "Sadık Köpek",
                subtitle = "Dostluk, güven ve içsel koruma",
                summary = "Sadakati, güvenilir dostlukları ve huzurlu bir güven hissini simgeler.",
                insight = "İçsel huzur ve güven duygusunu pekiştirir.",
                tip = "Güvendesiniz; gecenin sakin ritmiyle rahatça uyuyun.",
            ),
            "hi" to LocalizedDreamText(
                title = "वफ़ादार कुत्ता",
                subtitle = "मित्रता, विश्वास और सुरक्षा",
                summary = "यह सच्ची मित्रता, निष्ठा और भावनात्मक सुरक्षा का प्रतीक है।",
                insight = "यह मन में शांति और भरोसे की भावना को मजबूत करता है।",
                tip = "आप सुरक्षित और शांत हैं; रात की मधुर ध्वनियों के साथ सो जाएं।",
            ),
        ),
        "wedding" to mapOf(
            "vi" to LocalizedDreamText(
                title = "Lễ cưới",
                subtitle = "Sự hòa hợp, gắn kết & cân bằng cuộc sống",
                summary = "Tượng trưng cho sự kết nối hài hòa giữa công việc và nghỉ ngơi.",
                insight = "Phản ánh sự đồng điệu giữa mục tiêu lý trí và nhu cầu nghỉ ngơi.",
                tip = "Trân trọng giấc ngủ đêm nay như nền tảng cho một ngày mai tươi sáng.",
            ),
            "pt-BR" to LocalizedDreamText(
                title = "Casamento",
                subtitle = "Harmonia, compromisso e equilíbrio",
                summary = "Simboliza a integração harmoniosa entre trabalho, descanso e bem-estar.",
                insight = "Indica alinhamento entre suas metas e suas necessidades emocionais.",
                tip = "Valorize seu descanso hoje à noite para um amanhã revigorado.",
            ),
            "es" to LocalizedDreamText(
                title = "Boda",
                subtitle = "Armonía, compromiso y equilibrio interior",
                summary = "Simboliza el equilibrio entre tus metas diarias y tu bienestar personal.",
                insight = "Refleja armonía entre la mente y las emociones.",
                tip = "Dedica esta noche a un descanso profundo y reparador.",
            ),
            "fr" to LocalizedDreamText(
                title = "Mariage",
                subtitle = "Harmonie, engagement et équilibre",
                summary = "Symbolise l'équilibre harmonieux entre vos projets et votre repos.",
                insight = "Indique un alignement serein entre vos objectifs et vos besoins.",
                tip = "Offrez-vous un sommeil réparateur pour préparer un beau lendemain.",
            ),
            "de" to LocalizedDreamText(
                title = "Hochzeit",
                subtitle = "Harmonie, Verbundenheit & innere Balance",
                summary = "Steht für das harmonische Gleichgewicht zwischen Alltag und Erholung.",
                insight = "Zeigt Einklang zwischen Ihren Zielen und Ihrem Wohlbefinden.",
                tip = "Genießen Sie erholsamen Schlaf als Fundament für den neuen Tag.",
            ),
            "ja" to LocalizedDreamText(
                title = "結婚式の夢",
                subtitle = "調和・約束・心身のバランス",
                summary = "仕事と休息の調和や、より良い習慣への前向きな決意を象徴しています。",
                insight = "目標と心のニーズが心地よく一致しているサインです。",
                tip = "明るい明日のために、今夜は質の高い睡眠をとりましょう。",
            ),
            "ko" to LocalizedDreamText(
                title = "결혼식 꿈",
                subtitle = "조화, 다짐 및 삶의 균형",
                summary = "일과 휴식의 조화, 그리고 건강한 습관을 향한 긍정적인 다짐을 상징합니다.",
                insight = "목표와 내면의 정서적 요구가 균형을 이루고 있음을 보여줍니다.",
                tip = "상쾌한 내일을 위해 오늘 밤 깊고 편안한 숙면을 취하세요.",
            ),
            "it" to LocalizedDreamText(
                title = "Matrimonio",
                subtitle = "Armonia, impegno ed equilibrio",
                summary = "Simboleggia l'equilibrio tra lavoro, riposo e benessere personale.",
                insight = "Riflette sintonia tra i tuoi obiettivi e i tuoi bisogni interiori.",
                tip = "Onora il tuo riposo stasera per un risveglio luminoso domani.",
            ),
            "ru" to LocalizedDreamText(
                title = "Свадьба",
                subtitle = "Гармония, единство и жизненный баланс",
                summary = "Символизирует гармонию между делами, отдыхом и заботой о себе.",
                insight = "Отражает внутреннее согласие между целями и чувствами.",
                tip = "Посвятите эту ночь полноценному сну для бодрого утра.",
            ),
            "id" to LocalizedDreamText(
                title = "Pernikahan",
                subtitle = "Harmoni, komitmen & keseimbangan hidup",
                summary = "Melambangkan keseimbangan antara aktivitas harian dan waktu istirahat.",
                insight = "Menunjukkan keselarasan antara tujuan dan ketenangan batin.",
                tip = "Nikmati tidur berkualitas malam ini untuk hari esok yang cerah.",
            ),
            "tr" to LocalizedDreamText(
                title = "Düğün",
                subtitle = "Uyum, bağlılık ve yaşam dengesi",
                summary = "Çalışma hayatı ile dinlenme arasındaki sağlıklı dengeyi simgeler.",
                insight = "Hedefleriniz ile duygusal ihtiyaçlarınız arasındaki uyumu gösterir.",
                tip = "Aydınlık bir yarın için bu gece dinlendirici bir uykuya odaklanın.",
            ),
            "hi" to LocalizedDreamText(
                title = "विवाह",
                subtitle = "सामंजस्य, प्रतिबद्धता और जीवन संतुलन",
                summary = "यह कार्य और विश्राम के बीच सुंदर संतुलन का प्रतीक है।",
                insight = "यह आपके लक्ष्यों और मानसिक शांति के बीच तालमेल को दर्शाता है।",
                tip = "एक उज्ज्वल कल के लिए आज रात गहरी और शांत नींद लें।",
            ),
        ),
    )

    fun localizeSymbol(item: DreamSymbolItem, langCode: String): DreamSymbolItem {
        val norm = UserPreferencesRepository.normalizeLanguage(langCode)
        if (norm == UserPreferencesRepository.LANGUAGE_EN) return item
        val full = SYMBOL_TRANSLATIONS[item.id]?.get(norm)
        if (full != null) {
            return item.copy(
                title = full.title,
                subtitle = full.subtitle,
                summary = full.summary,
                psychologicalInsight = full.insight,
                calmingTip = full.tip,
            )
        }
        return item
    }

    val QUOTES: List<DailyQuoteItem> = listOf(
        DailyQuoteItem(
            id = "q1",
            quote = "I want to be like a caterpillar: eat a lot, sleep for a while, wake up beautiful.",
            author = "Deep Sleep Nightly Reflection",
        ),
        DailyQuoteItem(
            id = "q2",
            quote = "Sleep is the golden chain that ties health and our bodies together.",
            author = "Thomas Dekker",
        ),
        DailyQuoteItem(
            id = "q3",
            quote = "Each night, when I go to sleep, I die. And the next morning, when I wake up, I am reborn.",
            author = "Mahatma Gandhi",
        ),
        DailyQuoteItem(
            id = "q4",
            quote = "A good laugh and a long sleep are the best cures in the doctor's book.",
            author = "Irish Proverb",
        ),
        DailyQuoteItem(
            id = "q5",
            quote = "There is a time for many words, and there is also a time for sleep.",
            author = "Homer",
        ),
        DailyQuoteItem(
            id = "q6",
            quote = "Your future depends on your dreams, so go to sleep.",
            author = "Mesut Barazany",
        ),
    )

    private val QUOTE_TRANSLATIONS: Map<String, Map<String, String>> = mapOf(
        "q1" to mapOf(
            "vi" to "Hãy tận hưởng giấc ngủ êm đềm đêm nay để thức dậy thật rạng rỡ và tràn đầy sức sống vào sáng mai.",
            "pt-BR" to "Quero ser como uma borboleta: descansar em paz à noite e despertar renovado pela manhã.",
            "es" to "Descansa profundamente esta noche para despertar renovado y lleno de vida mañana.",
            "fr" to "Reposez-vous paisiblement cette nuit pour vous réveiller rayonnant demain matin.",
            "de" to "Ruhen Sie heute Nacht tief aus, um morgen früh erfrischt und voller Energie zu erwachen.",
            "ja" to "今夜はゆっくりと羽を休めて、明日の朝を美しく爽やかに迎えましょう。",
            "ko" to "오늘 밤 편안하게 깊은 잠을 자고 내일 아침 더욱 상쾌하고 빛나는 모습으로 깨어나세요.",
            "it" to "Riposa serenamente questa notte per risvegliarti rigenerato domani mattina.",
            "ru" to "Отдохните спокойно этой ночью, чтобы проснуться бодрым и обновлённым утром.",
            "id" to "Beristirahatlah dengan tenang malam ini agar terbangun segar dan berenergi esok pagi.",
            "tr" to "Yarın sabah yenilenmiş ve enerjik uyanmak için bu gece huzurla dinlenin.",
            "hi" to "कल सुबह तरोताज़ा और ऊर्जावान जागने के लिए आज रात गहरी नींद में विश्राम करें।",
        ),
        "q2" to mapOf(
            "vi" to "Giấc ngủ là sợi dây vàng gắn kết sức khỏe và cơ thể của chúng ta.",
            "pt-BR" to "O sono é a corrente dourada que une a saúde e o nosso corpo.",
            "es" to "El sueño es la cadena de oro que une la salud y nuestro cuerpo.",
            "fr" to "Le sommeil est la chaîne d'or qui relie la santé à notre corps.",
            "de" to "Der Schlaf ist die goldene Kette, die Gesundheit und Körper miteinander verbindet.",
            "ja" to "睡眠は健康と私たちの身体を結ぶ黄金の絆です。",
            "ko" to "수면은 건강과 우리의 몸을 하나로 이어주는 황금빛 고리입니다.",
            "it" to "Il sonno è la catena d'oro che unisce la salute e il nostro corpo.",
            "ru" to "Сон — это золотая нить, связывающая здоровье и наше тело воедино.",
            "id" to "Tidur adalah rantai emas yang menyatukan kesehatan dan tubuh kita.",
            "tr" to "Uyku, sağlığımızı ve bedenimizi birbirine bağlayan altın zincirdir.",
            "hi" to "नींद वह सुनहरी कड़ी है जो स्वास्थ्य और हमारे शरीर को एक साथ जोड़ती है।",
        ),
        "q3" to mapOf(
            "vi" to "Mỗi đêm khi chìm vào giấc ngủ, ta khép lại ngày cũ. Và mỗi sáng khi thức dậy, ta được tái sinh.",
            "pt-BR" to "Cada noite, ao dormir, deixo o dia para trás. E na manhã seguinte, ao acordar, renasço.",
            "es" to "Cada noche al dormir cerramos el día, y cada mañana al despertar renacemos.",
            "fr" to "Chaque soir en m'endormant je laisse le jour derrière moi, et chaque matin je renais.",
            "de" to "Jede Nacht lassen wir im Schlaf den Tag zurück, und jeden Morgen werden wir neu geboren.",
            "ja" to "毎晩眠りにつくとき一日が終わり、翌朝目覚めるとき私たちは新しく生まれ変わる。",
            "ko" to "매일 밤 잠들며 하루를 마감하고, 다음 날 아침 눈을 뜨며 우리는 새롭게 태어납니다.",
            "it" to "Ogni notte dormendo chiudiamo la giornata, e ogni mattina svegliandoci rinasciamo.",
            "ru" to "Каждую ночь, засыпая, мы отпускаем день, а каждое утро, просыпаясь, рождаемся заново.",
            "id" to "Setiap malam saat tidur kita menutup hari, dan setiap pagi saat bangun kita terlahir kembali.",
            "tr" to "Her gece uyurken günü geride bırakır, her sabah uyandığımızda yeniden doğarız.",
            "hi" to "हर रात सोते समय हम बीते दिन को विदा करते हैं, और हर सुबह जागते समय हमारा नया जन्म होता है।",
        ),
        "q4" to mapOf(
            "vi" to "Một nụ cười sảng khoái và một giấc ngủ sâu là hai phương thuốc quý giá nhất.",
            "pt-BR" to "Uma boa risada e um longo sono são os melhores remédios do mundo.",
            "es" to "Una buena risa y un sueño profundo son las mejores medicinas.",
            "fr" to "Un bon rire et un long sommeil sont les meilleurs remèdes du monde.",
            "de" to "Ein herzhaftes Lachen und ein tiefer Schlaf sind die beste Medizin.",
            "ja" to "心地よい笑いとたっぷりの睡眠は、どんな薬にも勝る最良の癒やしです。",
            "ko" to "기분 좋은 웃음과 충분한 숙면은 세상에서 가장 훌륭한 명약입니다.",
            "it" to "Una buona risata e un lungo sonno sono le migliori medicine del mondo.",
            "ru" to "Добрый смех и крепкий сон — лучшие лекарства на свете.",
            "id" to "Tawa yang hangat dan tidur yang nyenyak adalah obat terbaik.",
            "tr" to "İyi bir gülümseme ve derin bir uyku dünyadaki en iyi ilaçtır.",
            "hi" to "खुलकर हंसना और गहरी नींद लेना दुनिया की सबसे अच्छी दवा है।",
        ),
        "q5" to mapOf(
            "vi" to "Có thời gian dành cho những câu chuyện, và cũng có thời gian dành trọn cho giấc ngủ bình yên.",
            "pt-BR" to "Há um tempo para muitas palavras e também há um tempo para dormir em paz.",
            "es" to "Hay un tiempo para las palabras, y también hay un tiempo para dormir.",
            "fr" to "Il y a un temps pour les paroles, et il y a aussi un temps pour le sommeil.",
            "de" to "Es gibt eine Zeit für viele Worte, und es gibt eine Zeit für ruhigen Schlaf.",
            "ja" to "語り合うための時間があり、そして静かに眠るための時間がある。",
            "ko" to "대화를 나눌 시간이 있듯이, 온전히 잠들어야 할 평화로운 시간도 있습니다.",
            "it" to "C'è un tempo per le parole e c'è anche un tempo per il sonno.",
            "ru" to "Есть время для долгих бесед, и есть время для спокойного сна.",
            "id" to "Ada waktu untuk berbicara, dan ada pula waktu untuk tidur dengan tenang.",
            "tr" to "Sözler için bir zaman vardır, huzurla uyumak için de bir zaman vardır.",
            "hi" to "बातों का अपना समय होता है, और शांत नींद का भी अपना समय होता है।",
        ),
        "q6" to mapOf(
            "vi" to "Tương lai bắt đầu từ những ước mơ đẹp, vì vậy hãy ngủ thật ngon đêm nay.",
            "pt-BR" to "Seu futuro depende dos seus sonhos, então durma bem esta noite.",
            "es" to "Tu futuro depende de tus sueños, así que duerme bien esta noche.",
            "fr" to "Votre avenir dépend de vos rêves, alors dormez paisiblement cette nuit.",
            "de" to "Ihre Zukunft beginnt mit Ihren Träumen – schlafen Sie heute Nacht gut.",
            "ja" to "未来はあなたの夢から始まります。今夜はぐっすり眠りましょう。",
            "ko" to "당신의 미래는 아름다운 꿈에서 시작됩니다. 오늘 밤 편안히 주무세요.",
            "it" to "Il tuo futuro dipende dai tuoi sogni, quindi dormi bene stanotte.",
            "ru" to "Ваше будущее начинается с ваших снов, поэтому спите крепко этой ночью.",
            "id" to "Masa depan Anda berawal dari mimpi indah, jadi tidurlah dengan nyenyak malam ini.",
            "tr" to "Geleceğiniz hayallerinize bağlıdır, bu yüzden bu gece mışıl mışıl uyuyun.",
            "hi" to "आपका भविष्य आपके सपनों से शुरू होता है, इसलिए आज रात चैन की नींद सोएं।",
        ),
    )

    fun localizeQuote(item: DailyQuoteItem, langCode: String): DailyQuoteItem {
        val norm = UserPreferencesRepository.normalizeLanguage(langCode)
        if (norm == UserPreferencesRepository.LANGUAGE_EN) return item
        val translated = QUOTE_TRANSLATIONS[item.id]?.get(norm) ?: return item
        return item.copy(quote = translated)
    }

    fun getById(id: String): DreamSymbolItem? =
        SYMBOLS.firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun findById(id: String): DreamSymbolItem? = getById(id)

    fun search(query: String, langCode: String = UserPreferencesRepository.LANGUAGE_EN): List<DreamSymbolItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return SYMBOLS
        return SYMBOLS.filter { raw ->
            val loc = localizeSymbol(raw, langCode)
            raw.title.lowercase().contains(q) ||
                raw.subtitle.lowercase().contains(q) ||
                raw.summary.lowercase().contains(q) ||
                loc.title.lowercase().contains(q) ||
                loc.subtitle.lowercase().contains(q) ||
                loc.summary.lowercase().contains(q)
        }
    }

    fun quoteOfTheDay(daySeed: Long = System.currentTimeMillis() / 86_400_000L): DailyQuoteItem {
        val idx = (abs(daySeed) % QUOTES.size).toInt()
        return QUOTES[idx]
    }
}
