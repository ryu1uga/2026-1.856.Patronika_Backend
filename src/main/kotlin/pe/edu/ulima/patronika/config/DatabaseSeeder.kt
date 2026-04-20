package pe.edu.ulima.patronika.config

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pe.edu.ulima.patronika.model.*
import pe.edu.ulima.patronika.repository.*

@Component
class DatabaseSeeder(
    private val emotionRepository: EmotionRepository,
    private val emotionPhraseRepository: EmotionPhraseRepository,
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository,
    private val optionRepository: OptionRepository,
    private val questionOptionRepository: QuestionOptionRepository
) : CommandLineRunner {
    @Transactional
    override fun run(vararg args: String) {
        if (emotionRepository.count() == 0L) {
            println("Iniciando carga masiva de datos (Seed)...")

            // 1. Emociones
            val savedEmotions = seedEmotions()
            seedPhrases(savedEmotions)

            // 2. Test y Preguntas
            val savedQuestions = seedTestAndQuestions()

            // 3. Opciones
            val savedOptions = seedOptions()

            // 4. Matriz (Pregunta x Opción)
            seedQuestionOptions(savedQuestions, savedOptions)

            println("Carga de datos completada exitosamente.")
        } else {
            println("La base de datos ya contiene datos. Se omitió el seed.")
        }
    }

    // 1. EMOCIONES
    private fun seedEmotions(): Map<String, Emotion> {
        val alegria = Emotion(name = "Alegría", basicEmotion = true, hexCode = "F2B90C", photoUrl = "https://ik.imagekit.io/jdpadillavigo/mindful/Alegria.png")
        val temor = Emotion(name = "Temor", basicEmotion = true, hexCode = "8A5FBF", photoUrl = "https://ik.imagekit.io/jdpadillavigo/mindful/Miedo.png")
        val tristeza = Emotion(name = "Tristeza", basicEmotion = true, hexCode = "1975D1", photoUrl = "https://ik.imagekit.io/jdpadillavigo/mindful/Tristesa.png")
        val enojo = Emotion(name = "Enojo", basicEmotion = true, hexCode = "D94127", photoUrl = "https://ik.imagekit.io/jdpadillavigo/mindful/Enojo.png")
        val ansiedad = Emotion(name = "Ansiedad", basicEmotion = true, hexCode = "2D774C", photoUrl = "https://ik.imagekit.io/jdpadillavigo/mindful/Ansiedad.png")
        val calma = Emotion(name = "Calma", basicEmotion = false, hexCode = "58C5CA", photoUrl = "https://ik.imagekit.io/jdpadillavigo/mindful/Calma.png")
        val frustracion = Emotion(name = "Frustración", basicEmotion = false, hexCode = "E50388", photoUrl = "https://ik.imagekit.io/jdpadillavigo/mindful/Frustracion.png")
        val estres = Emotion(name = "Estrés", basicEmotion = false, hexCode = "F78F1E", photoUrl = "https://ik.imagekit.io/jdpadillavigo/mindful/Estres.png")

        // Guardamos y CAPTURAMOS la lista guardada
        val savedList = emotionRepository.saveAll(
            listOf(
                alegria,
                temor,
                tristeza,
                enojo,
                ansiedad,
                calma,
                frustracion,
                estres
            )
        )
        emotionRepository.flush()

        println("   -> ${savedList.size} Emociones insertadas.")

        // Devolvemos los objetos DE LA LISTA GUARDADA (savedList)
        return mapOf(
            "alegria" to savedList[0],
            "temor" to savedList[1],
            "tristeza" to savedList[2],
            "enojo" to savedList[3],
            "ansiedad" to savedList[4],
            "calma" to savedList[5],
            "frustracion" to savedList[6],
            "estres" to savedList[7]
        )
    }

    // 2. FRASES
    private fun seedPhrases(em: Map<String, Emotion>) {
        val phrases = listOf(
            // Alegría
            EmotionPhrase(emotion = em["alegria"]!!, phrase = "Acepta lo que no puedes cambiar, y hallarás calma."),
            EmotionPhrase(emotion = em["alegria"]!!, phrase = "El objetivo no es tener una vida perfecta, sino vivirla plenamente."),
            EmotionPhrase(emotion = em["alegria"]!!, phrase = "La felicidad se construye, no se espera."),
            EmotionPhrase(emotion = em["alegria"]!!, phrase = "Vivir con sentido trae felicidad."),
            EmotionPhrase(emotion = em["alegria"]!!, phrase = "No busques la felicidad, créala."),

            // Temor
            EmotionPhrase(emotion = em["temor"]!!, phrase = "Cuando haces lo que más temes, entonces puedes hacer cualquier cosa."),
            EmotionPhrase(emotion = em["temor"]!!, phrase = "La curiosidad vencerá al miedo incluso más que la valentía."),
            EmotionPhrase(emotion = em["temor"]!!, phrase = "El miedo puede ser una parte de tu vida, pero no tiene que controlarla."),
            EmotionPhrase(emotion = em["temor"]!!, phrase = "Miedo es lo que estás sintiendo. Valentía es lo que estás haciendo."),
            EmotionPhrase(emotion = em["temor"]!!, phrase = "Todo lo que siempre has querido está al otro lado del miedo."),

            // Tristeza
            EmotionPhrase(emotion = em["tristeza"]!!, phrase = "La tristeza no es debilidad, permítete sentirla."),
            EmotionPhrase(emotion = em["tristeza"]!!, phrase = "Aceptar la tristeza es el primer paso para sanar."),
            EmotionPhrase(emotion = em["tristeza"]!!, phrase = "No hay que huir del dolor, sino acompañarlo con compasión."),
            EmotionPhrase(emotion = em["tristeza"]!!, phrase = "La tristeza enseña a escuchar lo que el cuerpo necesita."),
            EmotionPhrase(emotion = em["tristeza"]!!, phrase = "La tristeza a veces sólo necesita espacio."),

            // Enojo
            EmotionPhrase(emotion = em["enojo"]!!, phrase = "El enojo es una emoción legítima; la violencia no lo es."),
            EmotionPhrase(emotion = em["enojo"]!!, phrase = "El enojo señala límites que han sido cruzados."),
            EmotionPhrase(emotion = em["enojo"]!!, phrase = "Aceptar el enojo permite responder en lugar de reaccionar."),
            EmotionPhrase(emotion = em["enojo"]!!, phrase = "El enojo no destruye cuando se expresa con claridad y respeto."),
            EmotionPhrase(emotion = em["enojo"]!!, phrase = "El enojo no necesita ser eliminado, sino comprendido."),

            // Ansiedad
            EmotionPhrase(emotion = em["ansiedad"]!!, phrase = "Confiar en ti mismo no garantiza el éxito, pero no hacerlo garantiza el fracaso"),
            EmotionPhrase(emotion = em["ansiedad"]!!, phrase = "Cuando me acepto a mí mismo, puedo cambiar"),
            EmotionPhrase(emotion = em["ansiedad"]!!, phrase = "Recuerda que no puedes fallar en ser tú mismo"),
            EmotionPhrase(emotion = em["ansiedad"]!!, phrase = "No eres lo que has hecho, eres lo que eliges hacer después."),
            EmotionPhrase(emotion = em["ansiedad"]!!, phrase = "Quién mira afuera, sueña: quién mira adentro, despierta")
        )
        emotionPhraseRepository.saveAll(phrases)
        println("   -> ${phrases.size} Frases insertadas.")
    }

    // 3. TESTS Y PREGUNTAS
    private fun seedTestAndQuestions(): List<Question> {
        val test = Test(name = "Autoevaluación: Hábitos de Estudio y Procrastinación")

        // 1. GUARDAMOS EL TEST PRIMERO Y USAMOS EL OBJETO GUARDADO (savedTest)
        val savedTest = testRepository.save(test)
        testRepository.flush()

        // 2. Usamos savedTest en las preguntas, NO 'test'
        val questions = listOf(
            Question(test = savedTest, text = "Cuando tengo que hacer una tarea, normalmente la dejo para el último minuto"),
            Question(test = savedTest, text = "Generalmente me preparo por adelantado para los exámenes"),
            Question(test = savedTest, text = "Cuando me asignan lecturas, las leo la noche anterior"),
            Question(test = savedTest, text = "Cuando tengo problemas para entender algo, inmediatamente trato de buscar ayuda"),
            Question(test = savedTest, text = "Asisto regularmente a clases"),
            Question(test = savedTest, text = "Trato de completar el trabajo asignado lo más pronto posible"),
            Question(test = savedTest, text = "Postergo los trabajos de los cursos que no me gustan"),
            Question(test = savedTest, text = "Postergo las lecturas de los cursos que no me gustan"),
            Question(test = savedTest, text = "Constantemente intento mejorar mis hábitos de estudio"),
            Question(test = savedTest, text = "Invierto el tiempo necesario en estudiar aún cuando el tema sea aburrido"),
            Question(test = savedTest, text = "Trato de motivarme para mantener mi ritmo de estudio"),
            Question(test = savedTest, text = "Trato de terminar mis trabajos importantes con tiempo de sobra"),
            Question(test = savedTest, text = "Me tomo el tiempo de revisar mis tareas antes de entregarlas")
        )

        val savedQuestions = questionRepository.saveAll(questions)
        questionRepository.flush()

        println("   -> 1 Test y ${questions.size} Preguntas insertadas.")
        return savedQuestions
    }

    // 4. OPCIONES
    private fun seedOptions(): Map<String, Option> {
        val optSiempre = Option(text = "Siempre")
        val optCasiSiempre = Option(text = "Casi siempre")
        val optAVeces = Option(text = "A veces")
        val optPocasVeces = Option(text = "Pocas veces")
        val optNunca = Option(text = "Nunca")

        // Guardamos y CAPTURAMOS
        val savedOpts = optionRepository.saveAll(
            listOf(
                optSiempre,
                optCasiSiempre,
                optAVeces,
                optPocasVeces,
                optNunca
            )
        )
        optionRepository.flush()

        println("   -> ${savedOpts.size} Opciones insertadas.")

        // Retornamos las guardadas
        return mapOf(
            "siempre" to savedOpts[0],
            "casiSiempre" to savedOpts[1],
            "aVeces" to savedOpts[2],
            "pocasVeces" to savedOpts[3],
            "nunca" to savedOpts[4]
        )
    }

    // 5. QUESTION OPTIONS
    private fun seedQuestionOptions(qs: List<Question>, opts: Map<String, Option>) {
        val qOptions = listOf(
            // --- BLOQUE SIEMPRE ---
            QuestionOption(question = qs[0], option = opts["siempre"]!!, score = 5),
            QuestionOption(question = qs[1], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[2], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[3], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[4], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[5], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[6], option = opts["siempre"]!!, score = 5),
            QuestionOption(question = qs[7], option = opts["siempre"]!!, score = 5),
            QuestionOption(question = qs[8], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[9], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[10], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[11], option = opts["siempre"]!!, score = 1),
            QuestionOption(question = qs[12], option = opts["siempre"]!!, score = 1),

            // --- BLOQUE CASI SIEMPRE ---
            QuestionOption(question = qs[0], option = opts["casiSiempre"]!!, score = 4),
            QuestionOption(question = qs[1], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[2], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[3], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[4], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[5], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[6], option = opts["casiSiempre"]!!, score = 4),
            QuestionOption(question = qs[7], option = opts["casiSiempre"]!!, score = 4),
            QuestionOption(question = qs[8], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[9], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[10], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[11], option = opts["casiSiempre"]!!, score = 2),
            QuestionOption(question = qs[12], option = opts["casiSiempre"]!!, score = 2),

            // --- BLOQUE A VECES ---
            QuestionOption(question = qs[0], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[1], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[2], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[3], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[4], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[5], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[6], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[7], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[8], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[9], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[10], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[11], option = opts["aVeces"]!!, score = 3),
            QuestionOption(question = qs[12], option = opts["aVeces"]!!, score = 3),

            // --- BLOQUE POCAS VECES ---
            QuestionOption(question = qs[0], option = opts["pocasVeces"]!!, score = 2),
            QuestionOption(question = qs[1], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[2], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[3], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[4], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[5], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[6], option = opts["pocasVeces"]!!, score = 2),
            QuestionOption(question = qs[7], option = opts["pocasVeces"]!!, score = 2),
            QuestionOption(question = qs[8], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[9], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[10], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[11], option = opts["pocasVeces"]!!, score = 4),
            QuestionOption(question = qs[12], option = opts["pocasVeces"]!!, score = 4),

            // --- BLOQUE NUNCA ---
            QuestionOption(question = qs[0], option = opts["nunca"]!!, score = 1),
            QuestionOption(question = qs[1], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[2], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[3], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[4], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[5], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[6], option = opts["nunca"]!!, score = 1),
            QuestionOption(question = qs[7], option = opts["nunca"]!!, score = 1),
            QuestionOption(question = qs[8], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[9], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[10], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[11], option = opts["nunca"]!!, score = 5),
            QuestionOption(question = qs[12], option = opts["nunca"]!!, score = 5)
        )

        questionOptionRepository.saveAll(qOptions)
        println("   -> ${qOptions.size} Relaciones Pregunta-Opción insertadas.")
    }
}