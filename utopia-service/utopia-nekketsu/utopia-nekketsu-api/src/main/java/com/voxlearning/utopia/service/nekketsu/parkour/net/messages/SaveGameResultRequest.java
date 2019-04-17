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

package com.voxlearning.utopia.service.nekketsu.parkour.net.messages;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.AnswerResult;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.response.SaveGameResultResponse;

import java.io.Serializable;
import java.util.Collection;


/**
 * 游戏结束，存储结果，返回奖励
 */
public class SaveGameResultRequest extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 将指定字段与之前server端返回的key经md5后生成
     */
    public String token;
    /**  */
    public int stageId;
    /**
     * AI(与本关AI),RANDOM(与随机选手),CLASSMATE(与本班同学)
     */
    public String fightType;
    /**
     * 对手id，如果fightType为AI则字段为空
     */
    public String opponentId;
    /**
     * 答题结果
     */
    public Collection<AnswerResult> answerList;
    /**
     * 是否获胜
     */
    public boolean win;
    /**
     * 通关时间，毫秒数
     */
    public int timeCost;
    /**
     * 捡到金币数
     */
    public int coinPickCount;
    /**
     * 每道题平均耗时毫秒数
     */
    public int timePerQuestion;
    /**
     * 正确率
     */
    public double correctRate;


    /**
     * 将请求过来的JSON字符串解析为类对象
     *
     * @param 请求过来的JSON字符串
     * @return 解析好的对象
     */
    @SuppressWarnings("unchecked")
    public static SaveGameResultRequest parseRequest(String input) {
        SaveGameResultRequest req = JsonStringDeserializer.getInstance().deserialize(input, SaveGameResultRequest.class);
        if (null == req) {
            throw new NullPointerException();
        }
        return req;
    }

    /**
     * 生成一个对应的返回类型对象
     *
     * @return 该类型消息对应的返回类型对象
     */
    public static SaveGameResultResponse newResponse() {
        SaveGameResultResponse response = new SaveGameResultResponse();
        return response;
    }
}