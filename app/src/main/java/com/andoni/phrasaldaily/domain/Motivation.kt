package com.andoni.phrasaldaily.domain

import kotlin.random.Random

/**
 * Reminder texts. Placeholders: {name}, {streak}, {word}.
 * Phrases with {streak} are only used when the streak is at least 2.
 */
object Motivation {

    private val titles = listOf(
        "¡Tu phrasal verb te espera!",
        "Un minuto de inglés 🧠",
        "Hora de tu dosis diaria",
        "¿Listo para el de hoy?",
        "Tu racha te necesita 🔥",
        "Ding, ding: inglés a domicilio",
        "Nuevo día, nueva palabra",
        "Pssst… {name}",
        "Tu cerebro quiere gimnasio",
        "Inglés exprés ☕",
    )

    private val bodies = listOf(
        "{name}, hoy toca '{word}'. ¿Te atreves a adivinar qué significa?",
        "Un phrasal verb al día y el inglés deja de dar miedo. Hoy: '{word}'.",
        "Llevas {streak} días seguidos, {name}. No dejes que la racha se apague 🔥",
        "{streak} días de racha. Tu yo del futuro te lo agradecerá.",
        "Dos minutos ahora valen más que dos horas el mes que viene.",
        "Los grandes cambios empiezan con palabras pequeñas. Hoy, '{word}'.",
        "Don't give up! Tú lo has empezado, tú lo terminas 💪",
        "{name}, el inglés no se aprende solo… pero casi: solo tienes que abrir la app.",
        "Hoy aprenderás algo que mañana usarás. Palabra de app.",
        "¿Sabías que '{word}' sale en series a todas horas? Descubre por qué.",
        "Cada día cuenta. Literalmente: aquí llevamos la cuenta 😉",
        "Keep it up, {name}! Tu racha de {streak} días está en juego.",
        "Netflix sin subtítulos está más cerca de lo que crees.",
        "Tu racha es como una planta 🌱: un poquito de agua cada día.",
        "Un phrasal verb hoy, una conversación mañana.",
        "Hoy no es día de 'give up', es día de 'show up'.",
        "Prometido: '{word}' es más fácil de lo que parece.",
        "{name}, tus neuronas han pedido algo nuevo para merendar 🍪",
        "La constancia vence al talento. Y tú tienes las dos.",
        "Racha de {streak} días. ¿A por el siguiente nivel?",
        "Un minuto. Una palabra. Un paso más cerca del inglés que quieres.",
        "Spoiler: hoy vas a aprender '{word}'.",
        "Los que hablan inglés con soltura también empezaron con un verbo al día.",
        "¡No me dejes en visto, {name}! Tu palabra de hoy te espera 👀",
        "Aprender inglés es como ir al gimnasio: lo difícil es entrar. Entra.",
        "Hoy es un gran día para 'pick up' algo nuevo.",
        "{streak} días seguidos… ¡esto ya es un hábito de verdad!",
        "¿Un café y un phrasal verb? La combinación perfecta ☕",
        "Tu viaje, tu entrevista, tu serie favorita: todo empieza aquí.",
        "Lo bueno de aprender poco cada día es que nunca es mucho.",
        "Hey {name}, let's 'catch up'! Te he guardado '{word}'.",
        "Tu cerebro ya está calentando. Solo falta que abras la app 🔥",
        "Pequeños pasos, grandes resultados. Paso de hoy: '{word}'.",
        "Ni excusas ni pereza: hoy toca inglés, {name}.",
        "Si no lo haces hoy, ¿quién va a cuidar de tu racha de {streak} días?",
        "Un día sin inglés es un día sin… bueno, sin inglés. ¡Evítalo!",
        "Abre, aprende, presume. Así de fácil.",
        "Hoy aprenderás una palabra que muchos nativos usan sin pensar.",
        "Cinco segundos para abrir la app, toda una vida para usar lo aprendido.",
        "'{word}' quiere conocerte. ¿Le das una oportunidad?",
        "Tu racha no se va a alargar sola, {name} 😏",
        "Yesterday you said tomorrow. Hoy es el día.",
        "Practice makes progress. Tu práctica de hoy está lista.",
        "Learning never 'wears out'. Ven a por tu palabra.",
        "Hoy es perfecto para 'brush up' tu inglés.",
    )

    private val streakDangerBodies = listOf(
        "⚠️ {name}, tu racha de {streak} días termina hoy si no entras.",
        "¡Alerta de racha! {streak} días en juego. Solo necesitas un minuto.",
        "No dejes que {streak} días de esfuerzo se esfumen. Hoy: '{word}'.",
    )

    private val greetingsMorning = listOf("¡Buenos días, {name}!", "Good morning, {name}!", "¡Arriba, {name}!")
    private val greetingsAfternoon = listOf("¡Buenas tardes, {name}!", "¡Hola, {name}!", "Hey, {name}!")
    private val greetingsNight = listOf("¡Buenas noches, {name}!", "¡Hola, {name}!", "Good evening, {name}!")

    /** Short quotes shown on the home screen. */
    val dailyQuotes = listOf(
        "Poco a poco se va lejos.",
        "La práctica hace al maestro (y al bilingüe).",
        "Lo que haces cada día importa más que lo que haces de vez en cuando.",
        "Un idioma nuevo es una ventana nueva al mundo.",
        "No tienes que ser perfecto, solo constante.",
        "Hoy un verbo, mañana una conversación.",
        "Every expert was once a beginner.",
        "Little by little, a little becomes a lot.",
        "El mejor momento para empezar fue ayer. El segundo mejor, hoy.",
        "Equivocarse también es aprender.",
        "Small steps every day.",
        "Tu esfuerzo de hoy es tu fluidez de mañana.",
    )

    data class Reminder(val title: String, val body: String, val index: Int)

    fun reminder(name: String, streak: Int, word: String, lastIndex: Int, random: Random = Random.Default): Reminder {
        val displayName = name.ifBlank { "crack" }
        val candidates = if (streak >= 3 && random.nextInt(4) == 0) {
            streakDangerBodies.indices.map { it + 1000 }
        } else {
            bodies.indices.filter { streak >= 2 || "{streak}" !in bodies[it] }
        }.filter { it != lastIndex }
        val idx = candidates.random(random)
        val raw = if (idx >= 1000) streakDangerBodies[idx - 1000] else bodies[idx]
        fun fill(s: String) = s.replace("{name}", displayName)
            .replace("{streak}", streak.toString())
            .replace("{word}", word)
        return Reminder(title = fill(titles.random(random)), body = fill(raw), index = idx)
    }

    fun greeting(name: String, hour: Int, dayIndex: Long): String {
        val list = when (hour) {
            in 5..13 -> greetingsMorning
            in 14..20 -> greetingsAfternoon
            else -> greetingsNight
        }
        return list[Math.floorMod(dayIndex, list.size.toLong()).toInt()].replace("{name}", name.ifBlank { "crack" })
    }

    fun quoteFor(dayIndex: Long): String = dailyQuotes[Math.floorMod(dayIndex, dailyQuotes.size.toLong()).toInt()]
}
