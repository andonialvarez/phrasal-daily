package com.andoni.phrasaldaily.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class ItemType { PHRASAL, FALSE_FRIEND }

/** A learnable card: either a phrasal verb or a false friend. */
sealed interface StudyItem {
    val id: String
    val type: ItemType
    val headword: String
    val ipa: String
    val sayAs: String
    val translation: String
    val example: String
    val exampleEs: String
}

data class PhrasalVerb(
    val index: Int,
    override val headword: String,
    override val ipa: String,
    override val sayAs: String,
    override val translation: String,
    val definition: String,
    override val example: String,
    override val exampleEs: String,
    val level: String,
) : StudyItem {
    override val id get() = "pv:$index"
    override val type get() = ItemType.PHRASAL
}

data class FalseFriend(
    val index: Int,
    override val headword: String,
    override val ipa: String,
    override val sayAs: String,
    /** The Spanish word it looks like (and doesn't mean). */
    val looksLike: String,
    /** What it really means in Spanish. */
    override val translation: String,
    val tip: String,
    override val example: String,
    override val exampleEs: String,
) : StudyItem {
    override val id get() = "ff:$index"
    override val type get() = ItemType.FALSE_FRIEND
}

/** Spanish used for translations and examples. */
enum class SpanishVariant { SPAIN, LATAM }

/** Bundled content, loaded once from assets. Latin American wording comes from latam.json overrides. */
class ContentRepository(context: Context) {

    private val spain: Pair<List<PhrasalVerb>, List<FalseFriend>>
    private val latam: Pair<List<PhrasalVerb>, List<FalseFriend>>

    /** Set from the user's settings; screens read the lists below. */
    @Volatile var variant: SpanishVariant = SpanishVariant.SPAIN

    val phrasalVerbs: List<PhrasalVerb> get() = forVariant().first
    val falseFriends: List<FalseFriend> get() = forVariant().second

    private fun forVariant() = if (variant == SpanishVariant.LATAM) latam else spain

    init {
        val assets = context.applicationContext.assets
        fun read(name: String) = assets.open(name).bufferedReader().use { it.readText() }
        val pvJson = JSONArray(read("phrasal_verbs.json"))
        val ffJson = JSONArray(read("false_friends.json"))
        val overrides = JSONObject(read("latam.json"))
        fun field(o: JSONObject, id: String, key: String, latin: Boolean): String =
            overrides.optJSONObject(id)?.takeIf { latin && it.has(key) }?.getString(key) ?: o.getString(key)

        fun build(latin: Boolean): Pair<List<PhrasalVerb>, List<FalseFriend>> {
            val pvs = List(pvJson.length()) { i ->
                val o = pvJson.getJSONObject(i)
                val id = "pv:$i"
                PhrasalVerb(
                    index = i,
                    headword = o.getString("v"),
                    ipa = o.getString("ipa"),
                    sayAs = o.getString("say"),
                    translation = field(o, id, "es", latin),
                    definition = field(o, id, "def", latin),
                    example = o.getString("ex"),
                    exampleEs = field(o, id, "exEs", latin),
                    level = o.optString("lvl", "B1"),
                )
            }
            val ffs = List(ffJson.length()) { i ->
                val o = ffJson.getJSONObject(i)
                val id = "ff:$i"
                FalseFriend(
                    index = i,
                    headword = o.getString("w"),
                    ipa = o.getString("ipa"),
                    sayAs = o.getString("say"),
                    looksLike = field(o, id, "looks", latin),
                    translation = field(o, id, "means", latin),
                    tip = field(o, id, "tip", latin),
                    example = o.getString("ex"),
                    exampleEs = field(o, id, "exEs", latin),
                )
            }
            return pvs to ffs
        }
        spain = build(latin = false)
        latam = build(latin = true)
    }

    fun phrasalForDay(dayIndex: Long): PhrasalVerb =
        phrasalVerbs[Math.floorMod(dayIndex, phrasalVerbs.size.toLong()).toInt()]

    fun falseFriendForDay(dayIndex: Long): FalseFriend =
        falseFriends[Math.floorMod(dayIndex, falseFriends.size.toLong()).toInt()]

    fun byId(id: String): StudyItem? {
        val (kind, num) = id.split(":").let { it.getOrNull(0) to it.getOrNull(1)?.toIntOrNull() }
        num ?: return null
        return when (kind) {
            "pv" -> phrasalVerbs.getOrNull(num)
            "ff" -> falseFriends.getOrNull(num)
            else -> null
        }
    }

    companion object {
        @Volatile private var instance: ContentRepository? = null
        fun get(context: Context): ContentRepository =
            instance ?: synchronized(this) {
                instance ?: ContentRepository(context).also { instance = it }
            }
    }
}
