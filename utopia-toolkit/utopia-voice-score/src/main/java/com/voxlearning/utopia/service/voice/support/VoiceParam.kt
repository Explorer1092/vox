package com.voxlearning.utopia.service.voice.support

import org.springframework.web.multipart.MultipartFile
import java.io.Serializable

class VoiceParam(val multipartFile: MultipartFile,
                 val text: String,
                 var coefficient: String,
                 var mode: String,
                 val userId: Long?,
                 var resultMap: Map<String, Any>,
                 val ua: String,
                 val ip: String,
                 val language: String,
                 val appkey: String,
                 val sys: String,
                 val fileInfoMap: Map<String, Any?>,
                 val reqAppKey: String,
                 val deviceId: String,
                 val voiceEngine: String) : Serializable {

    companion object {
        internal const val serialVersionUID = -1419404317470871373L
    }
}