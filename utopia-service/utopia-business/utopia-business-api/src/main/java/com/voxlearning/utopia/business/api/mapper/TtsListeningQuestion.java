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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * TTS听力大题
 *
 * @author Junjie Zhang
 * @since 2014-08-12
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TtsListeningQuestion implements Serializable {
    private static final long serialVersionUID = -1575397997095557855L;

    private String tip;                                 //提示语
    private TtsListeningSentence tipSentence;           //制作提示语数据
    private Integer playTimes = 1;                      //每道小题播放次数
    private Float interval = 1f;                        //每道小题间隔时间
    private List<TtsListeningSubQuestion> subQuestions; //听力小题
}
