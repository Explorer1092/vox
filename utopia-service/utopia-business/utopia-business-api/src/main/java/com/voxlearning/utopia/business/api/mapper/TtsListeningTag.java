/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.business.api.mapper;

import com.voxlearning.utopia.business.api.constant.TtsListeningTagType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * TTS听力标签
 *
 * @author Junjie Zhang
 * @since 2014-08-15
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class TtsListeningTag implements Serializable {
    private static final long serialVersionUID = -1811041125192376852L;

    private TtsListeningSentence sentence;
    private Integer loopTimes;
    private TtsListeningTagType tagType = TtsListeningTagType.SENTENCE;

    public static TtsListeningTag getSentenceTag(String role, Integer volume, Integer speed, String content) {
        TtsListeningTag tag = new TtsListeningTag();
        tag.setTagType(TtsListeningTagType.SENTENCE);
        TtsListeningSentence sentence1 = new TtsListeningSentence();
        sentence1.setRole(role);
        sentence1.setVolume(volume);
        sentence1.setSpeed(speed);
        sentence1.setContent(content);
        tag.setSentence(sentence1);
        return tag;
    }

    public static TtsListeningTag getSentenceTag(String voice, Float duration) {
        TtsListeningTag tag = new TtsListeningTag();
        tag.setTagType(TtsListeningTagType.SENTENCE);
        TtsListeningSentence sentence1 = new TtsListeningSentence();
        sentence1.setVoice(voice);
        sentence1.setDuration(duration);
        tag.setSentence(sentence1);
        return tag;
    }

    public static TtsListeningTag getPauseTag(float pause) {
        TtsListeningTag tag = new TtsListeningTag();
        tag.setTagType(TtsListeningTagType.PAUSE);
        TtsListeningSentence sentence1 = new TtsListeningSentence();
        sentence1.setPause(pause);
        tag.setSentence(sentence1);
        return tag;
    }

    public static TtsListeningTag getLoopTag(int loop) {
        TtsListeningTag tag = new TtsListeningTag();
        tag.setTagType(TtsListeningTagType.LOOP);
        tag.setLoopTimes(loop);
        return tag;
    }

    public static TtsListeningTag getMuteTag() {
        TtsListeningTag tag = new TtsListeningTag();
        tag.setTagType(TtsListeningTagType.MUTE);
        return tag;
    }

    @Override
    public String toString() {
        switch (tagType) {
            case LOOP:
                return String.format("TtsListeningTag{loopTimes=%d}", loopTimes);
            case SENTENCE:
                return String.format("TtsListeningTag{sentence=%s}", sentence.toString());
            case PAUSE:
                return String.format("TtsListeningTag{pause=%f}", sentence.getPause());
            case MUTE:
                //noinspection RedundantStringFormatCall
                return String.format("TtsListeningTag{mute}");
        }
        return "";
    }
}
