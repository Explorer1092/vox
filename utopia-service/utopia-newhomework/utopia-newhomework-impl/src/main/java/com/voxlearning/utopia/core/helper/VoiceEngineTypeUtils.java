package com.voxlearning.utopia.core.helper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.VoiceEngineType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 拼语语音地址的
 *
 * @author xuesong.zhang
 * @since 2016/8/5
 */
public class VoiceEngineTypeUtils {

    public static String getAudioUrl(String audio, VoiceEngineType voiceEngineType) {
        if (audio != null && voiceEngineType == VoiceEngineType.ChiVox) {
            return "http://" + audio + ".mp3";
        } else if (audio != null && voiceEngineType == VoiceEngineType.Unisound && audio.contains("edu.hivoice.cn")) {
            String replaceAudioUrl = audio.replace("http://", "https://").replace(":9088", "");
            int locationIndex = audio.lastIndexOf("/");
            if (locationIndex != -1) {
                String location = audio.substring(locationIndex + 1);
                if (StringUtils.isNotEmpty(location)) {
                    replaceAudioUrl = replaceAudioUrl.replace("edu.hivoice.cn", "edu" + location + ".hivoice.cn");
                }
            }
            return replaceAudioUrl;
        } else {
            return audio;
        }

    }

    public static String handleAudioUrl(String month, String audio, VoiceEngineType voiceEngineType, String hid, Long sid) {
        if (audio == null) {
            return null;
        }
        if (voiceEngineType == VoiceEngineType.ChiVox) {
            return null;
        } else {
            String[] strings = StringUtils.split(audio, "/");
            if (strings != null && strings.length >= 3) {
                return month + "|" + sid + "|" + hid + "|" + strings[strings.length - 3] + "_" + strings[strings.length - 2] + "_" + strings[strings.length - 1];
            } else {
                return null;
            }
        }
    }

    public static Map<String, String> getAudioUrl(Collection<String> audios, VoiceEngineType voiceEngineType) {
        if (CollectionUtils.isEmpty(audios) || voiceEngineType == null) {
            return Collections.emptyMap();
        }

        Map<String, String> resultMap = new HashMap<>();
        for (String audio : audios) {
            resultMap.put(audio, getAudioUrl(audio, voiceEngineType));
        }
        return resultMap;
    }

    public static void main(String[] args) {
        System.out.println(getAudioUrl("https://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/70FF2E0F-5F08-45F8-9CBE-AD56D91E5D5F/1537058997162036211/sh1", VoiceEngineType.Unisound));
        System.out.println(getAudioUrl("http://edu.hivoice.cn:9088/WebAudio-1.0-SNAPSHOT/audio/play/591299aa-6626-481e-9521-658d82e08ef3/1536937538928783547/gz", VoiceEngineType.Unisound));
        System.out.println(getAudioUrl("http://edu.hivoice.cn:9088/WebAudio-1.0-SNAPSHOT/audio/play/c95dd03a-3cad-44e9-960d-f0dd76b055b0/1537189967898127536/bj", VoiceEngineType.Unisound));
        System.out.println(getAudioUrl("http://edu.hivoice.cn:9088/WebAudio-1.0-SNAPSHOT/audio/play/27d8a4c6-6794-4f6c-93af-58a2686f0ba3/1536755425969014580/sh", VoiceEngineType.Unisound));
    }
}
