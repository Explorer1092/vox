/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.parkour.net.types;

import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;

import java.io.Serializable;


/**
 * 关卡词汇
 */
public class StageWord extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * ${var.@comment}
     */
    public int stageId;
    /**
     * ${var.@comment}
     */
    public String wordId = "";
    /**
     * 中文内容
     */
    public String cnText = "";
    /**
     * 英文内容
     */
    public String enText = "";
    /**
     * 多译编号 对应到sentence表中的multiMeaning字段
     */
    public int multiMeaning;
    /**
     * 图片名称
     */
    public String picName = "";
    /**
     * 单词类型
     */
    public String speechPart = "";
    /**
     * 释义1
     */
    public String paraphrase1 = "";
    /**
     * 释义2
     */
    public String paraphrase2 = "";
    /**
     * 释义3
     */
    public String paraphrase3 = "";
    /**
     * 释义4
     */
    public String paraphrase4 = "";
    /**
     * 多音编号
     */
    public int polyphony;
    /**
     * 音节声音
     */
    public String syllableAudio = "";
    /**
     * 音节切分
     */
    public String syllableSegmentation = "";
    /**
     * 单词声音
     */
    public String wordAudio = "";
    /**
     * 此类型标签
     */
    public String tag = "";
    /**
     * 单词视频
     */
    public String wordVideo = "";
    /**
     * 英文内容
     */
    public String pronounceUK = "";
    /**
     * 美式发音
     */
    public String pronounceUS = "";
    /**
     * 英式发音音标
     */
    public String phoneticMapUK = "";
    /**
     * 美式发音音标
     */
    public String phoneticMapUS = "";
    /**
     * 英音音频
     */
    public String audioUK = "";
    /**
     * 美音音频
     */
    public String audioUS = "";
    /**
     * 英音音频播放时间
     */
    public String audioUKPlaytime = "";
    /**
     * 美音音频播放时间
     */
    public String audioUSPlaytime = "";


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}