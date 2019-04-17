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

import lombok.*;

import java.io.Serializable;

/**
 * TTS听力句子
 *
 * @author Junjie Zhang
 * @since 2014-08-15
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class TtsListeningSentence implements Serializable {
    private static final long serialVersionUID = -5432865095853668717L;

    private String role = "";       //角色
    private Integer volume = 3;     //音量
    private Integer speed = 3;      //音速
    private Float pause = 0.5F;     //句尾停顿
    private String content = "";    //内容
    private String voice = "";      //音频文件地址
    private Float duration;         //听力时长
    private Integer LoopTimes = 1;  //循环次数

    public TtsListeningSentence(String voice, Float pause) {
        this.voice = voice;
        this.pause = pause;
    }
}
