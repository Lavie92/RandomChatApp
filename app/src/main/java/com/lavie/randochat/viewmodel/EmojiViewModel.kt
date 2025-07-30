package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import com.lavie.randochat.model.Emoji
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmojiViewModel : ViewModel() {

    private val _emojis = MutableStateFlow<List<Emoji>>(emptyList())
    val emojis: StateFlow<List<Emoji>> = _emojis.asStateFlow()

    init {
        loadEmojis()
    }

    private fun loadEmojis() {
        _emojis.value = listOf(
            Emoji("after_boom", "https://i.imgur.com/OSBToxi.png", "after boom"),
            Emoji("ah", "https://i.imgur.com/WwOzTnn.png", "ah"),
            Emoji("amazed", "https://i.imgur.com/5k5PtZs.png", "amazed"),
            Emoji("angry", "https://i.imgur.com/fhlFBMe.png", "angry"),
            Emoji("bad_smelly", "https://i.imgur.com/QZYLsIx.png", "bad smelly"),
            Emoji("baffle", "https://i.imgur.com/WPDnrZE.png", "baffle"),
            Emoji("beat_brick", "https://i.imgur.com/82hTVpU.png", "beat brick"),
            Emoji("beat_plaster", "https://i.imgur.com/eOTZi9h.png", "beat plaster"),
            Emoji("beat_shot", "https://i.imgur.com/1t0wClt.png", "beat shot"),
            Emoji("beated", "https://i.imgur.com/gppyG81.png", "beated"),
            Emoji("beauty", "https://i.imgur.com/MW0iDpD.png", "beauty"),
            Emoji("big_smile", "https://i.imgur.com/ufXMdK9.png", "big smile"),
            Emoji("boss", "https://i.imgur.com/Mar84O5.png", "boss"),
            Emoji("burn_joss_stick", "https://i.imgur.com/z9ot2jb.png", "burn joss stick"),
            Emoji("byebye", "https://i.imgur.com/gT5UVDg.png", "bye bye"),
            Emoji("canny", "https://i.imgur.com/UPr0xKx.png", "canny"),
            Emoji("choler", "https://i.imgur.com/sCH0UDC.png", "choler"),
            Emoji("cold", "https://i.imgur.com/qAA41Yo.png", "cold"),
            Emoji("confident", "https://i.imgur.com/DY04hge.png", "confident"),
            Emoji("confuse", "https://i.imgur.com/YPCFWkw.png", "confuse"),
            Emoji("cool", "https://i.imgur.com/0K1wrzZ.png", "cool"),
            Emoji("cry", "https://i.imgur.com/4QZYYIC.png", "cry"),
            Emoji("doubt", "https://i.imgur.com/lzHbzV1.png", "doubt"),
            Emoji("dribble", "https://i.imgur.com/X5uZ93a.png", "dribble"),
            Emoji("embarrassed", "https://i.imgur.com/YyRX2tG.png", "embarrassed"),
            Emoji("extreme_sexy_girl", "https://i.imgur.com/haYEQW2.png", "extreme sexy girl"),
            Emoji("feel_good", "https://i.imgur.com/xStugfC.png", "feel good"),
            Emoji("go", "https://i.imgur.com/V5csBWd.png", "go"),
            Emoji("haha", "https://i.imgur.com/sJzUVQV.png", "haha"),
            Emoji("hell_boy", "https://i.imgur.com/XNaTnxM.png", "hell boy"),
            Emoji("hungry", "https://i.imgur.com/LoyXegV.png", "hungry"),
            Emoji("look_down", "https://i.imgur.com/pOe1P2B.png", "look down"),
            Emoji("matrix", "https://i.imgur.com/FjAn1I2.png", "matrix"),
            Emoji("misdoubt", "https://i.imgur.com/pH6xpRo.png", "misdoubt"),
            Emoji("nosebleed", "https://i.imgur.com/4Z1RVAZ.png", "nosebleed"),
            Emoji("oh", "https://i.imgur.com/k82XpMK.png", "oh"),
            Emoji("ops", "https://i.imgur.com/OLaODha.png", "ops"),
            Emoji("pudency", "https://i.imgur.com/Y6ZBwnf.png", "pudency"),
            Emoji("rap", "https://i.imgur.com/5oPvtkJ.png", "rap"),
            Emoji("sad", "https://i.imgur.com/sJIVd5I.png", "sad"),
            Emoji("sexy_girl", "https://i.imgur.com/VgcppPF.png", "sexy girl"),
            Emoji("shame", "https://i.imgur.com/VQUcio9.png", "shame"),
            Emoji("smile", "https://i.imgur.com/Gyfx5fY.png", "smile"),
            Emoji("spiderman", "https://i.imgur.com/x16WUY6.png", "spiderman"),
            Emoji("still_dreaming", "https://i.imgur.com/qTAN0JP.png", "still dreaming"),
            Emoji("sure", "https://i.imgur.com/aTBArr0.png", "sure"),
            Emoji("surrender", "https://i.imgur.com/3FZkrnP.png", "surrender"),
            Emoji("sweat", "https://i.imgur.com/c6fiw3M.png", "sweat"),
            Emoji("sweet_kiss", "https://i.imgur.com/KzcQ36Q.png", "sweet kiss"),
            Emoji("tire", "https://i.imgur.com/5NeIQas.png", "tire"),
            Emoji("too_sad", "https://i.imgur.com/JJv8JGC.png", "too sad"),
            Emoji("waaaht", "https://i.imgur.com/fNGW21s.png", "waaaht"),
            Emoji("what", "https://i.imgur.com/1ryGK1k.png", "what")
        )
    }
}
