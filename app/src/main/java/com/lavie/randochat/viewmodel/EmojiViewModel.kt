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
            Emoji("popo_after_boom", "https://i.imgur.com/OSBToxi.png", "after boom"),
            Emoji("popo_ah", "https://i.imgur.com/WwOzTnn.png", "ah"),
            Emoji("popo_amazed", "https://i.imgur.com/5k5PtZs.png", "amazed"),
            Emoji("popo_angry", "https://i.imgur.com/fhlFBMe.png", "angry"),
            Emoji("popo_bad_smelly", "https://i.imgur.com/QZYLsIx.png", "bad smelly"),
            Emoji("popo_baffle", "https://i.imgur.com/WPDnrZE.png", "baffle"),
            Emoji("popo_beat_brick", "https://i.imgur.com/82hTVpU.png", "beat brick"),
            Emoji("popo_beat_plaster", "https://i.imgur.com/eOTZi9h.png", "beat plaster"),
            Emoji("popo_beat_shot", "https://i.imgur.com/1t0wClt.png", "beat shot"),
            Emoji("popo_beated", "https://i.imgur.com/gppyG81.png", "beated"),
            Emoji("popo_beauty", "https://i.imgur.com/MW0iDpD.png", "beauty"),
            Emoji("popo_big_smile", "https://i.imgur.com/ufXMdK9.png", "big smile"),
            Emoji("popo_boss", "https://i.imgur.com/Mar84O5.png", "boss"),
            Emoji("popo_burn_joss_stick", "https://i.imgur.com/z9ot2jb.png", "burn joss stick"),
            Emoji("popo_byebye", "https://i.imgur.com/gT5UVDg.png", "bye bye"),
            Emoji("popo_canny", "https://i.imgur.com/UPr0xKx.png", "canny"),
            Emoji("popo_choler", "https://i.imgur.com/sCH0UDC.png", "choler"),
            Emoji("popo_cold", "https://i.imgur.com/qAA41Yo.png", "cold"),
            Emoji("popo_confident", "https://i.imgur.com/DY04hge.png", "confident"),
            Emoji("popo_confuse", "https://i.imgur.com/YPCFWkw.png", "confuse"),
            Emoji("popo_cool", "https://i.imgur.com/0K1wrzZ.png", "cool"),
            Emoji("popo_cry", "https://i.imgur.com/4QZYYIC.png", "cry"),
            Emoji("popo_doubt", "https://i.imgur.com/lzHbzV1.png", "doubt"),
            Emoji("popo_dribble", "https://i.imgur.com/X5uZ93a.png", "dribble"),
            Emoji("popo_embarrassed", "https://i.imgur.com/YyRX2tG.png", "embarrassed"),
            Emoji("popo_extreme_sexy_girl", "https://i.imgur.com/haYEQW2.png", "extreme sexy girl"),
            Emoji("popo_feel_good", "https://i.imgur.com/xStugfC.png", "feel good"),
            Emoji("popo_go", "https://i.imgur.com/V5csBWd.png", "go"),
            Emoji("popo_haha", "https://i.imgur.com/sJzUVQV.png", "haha"),
            Emoji("popo_hell_boy", "https://i.imgur.com/XNaTnxM.png", "hell boy"),
            Emoji("popo_hungry", "https://i.imgur.com/LoyXegV.png", "hungry"),
            Emoji("popo_look_down", "https://i.imgur.com/pOe1P2B.png", "look down"),
            Emoji("popo_matrix", "https://i.imgur.com/FjAn1I2.png", "matrix"),
            Emoji("popo_misdoubt", "https://i.imgur.com/pH6xpRo.png", "misdoubt"),
            Emoji("popo_nosebleed", "https://i.imgur.com/4Z1RVAZ.png", "nosebleed"),
            Emoji("popo_oh", "https://i.imgur.com/k82XpMK.png", "oh"),
            Emoji("popo_ops", "https://i.imgur.com/OLaODha.png", "ops"),
            Emoji("popo_pudency", "https://i.imgur.com/Y6ZBwnf.png", "pudency"),
            Emoji("popo_rap", "https://i.imgur.com/5oPvtkJ.png", "rap"),
            Emoji("popo_sad", "https://i.imgur.com/sJIVd5I.png", "sad"),
            Emoji("popo_sexy_girl", "https://i.imgur.com/VgcppPF.png", "sexy girl"),
            Emoji("popo_shame", "https://i.imgur.com/VQUcio9.png", "shame"),
            Emoji("popo_smile", "https://i.imgur.com/Gyfx5fY.png", "smile"),
            Emoji("popo_spiderman", "https://i.imgur.com/x16WUY6.png", "spiderman"),
            Emoji("popo_still_dreaming", "https://i.imgur.com/qTAN0JP.png", "still dreaming"),
            Emoji("popo_sure", "https://i.imgur.com/aTBArr0.png", "sure"),
            Emoji("popo_surrender", "https://i.imgur.com/3FZkrnP.png", "surrender"),
            Emoji("popo_sweat", "https://i.imgur.com/c6fiw3M.png", "sweat"),
            Emoji("popo_sweet_kiss", "https://i.imgur.com/KzcQ36Q.png", "sweet kiss"),
            Emoji("popo_tire", "https://i.imgur.com/5NeIQas.png", "tire"),
            Emoji("popo_too_sad", "https://i.imgur.com/JJv8JGC.png", "too sad"),
            Emoji("popo_waaaht", "https://i.imgur.com/fNGW21s.png", "waaaht"),
            Emoji("popo_what", "https://i.imgur.com/1ryGK1k.png", "what")
        )
    }
}
