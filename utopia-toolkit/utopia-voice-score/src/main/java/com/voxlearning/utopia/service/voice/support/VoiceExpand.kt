package com.voxlearning.utopia.service.voice.support

import java.io.Serializable

class VoiceExpand: Serializable {

    companion object {
        internal const val serialVersionUID = 8950345902599730244L
    }

    /**
     * 来源
     */
    var source: String? = null
}