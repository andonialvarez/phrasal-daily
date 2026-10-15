package com.andoni.phrasaldaily.data

import android.content.Context
import org.json.JSONArray

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

/** Bundled content, loaded once from assets. */
class ContentRepository(context: Context) {

    val phrasalVerbs: List<PhrasalVerb>
    val falseFriends: List<FalseFriend>

    init {
        val assets = context.applicationContext.assets
        phrasalVerbs = JSONArray(assets.open("phrasal_verbs.json").bufferedReader().use { it.readText() })
            .let { arr ->
                List(arr.length()) { i ->
                    val o = arr.getJSONObject(i)
                    PhrasalVerb(
                        index = i,
                        headword = o.getString("v"),
                        ipa = o.getString("ipa"),
                        sayAs = o.getString("say"),
                        translation = o.getString("es"),
                        definition = o.getString("def"),
                        example = o.getString("ex"),
                        exampleEs = o.getString("exEs"),
                        level = o.optString("lvl", "B1"),
                    )
                }
            }
        falseFriends = JSONArray(assets.open("false_friends.json").bufferedReader().use { it.readText() })
            .let { arr ->
                List(arr.length()) { i ->
                    val o = arr.getJSONObject(i)
                    FalseFriend(
                        index = i,
                        headword = o.getString("w"),
                        ipa = o.getString("ipa"),
                        sayAs = o.getString("say"),
                        looksLike = o.getString("looks"),
                        translation = o.getString("means"),
                        tip = o.getString("tip"),
                        example = o.getString("ex"),
                        exampleEs = o.getString("exEs"),
                    )
                }
            }
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
