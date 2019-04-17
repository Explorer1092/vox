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

package com.voxlearning.utopia.business.net.frameworkapp.messages;

import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.business.net.frameworkapp.types.response.RefreshInfoResponse;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Map;


/**
 * 小游戏完成时发送，用以请求最新水晶等数目的消息
 */
public class RefreshInfoRequest extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;
    private static final Logger logger = LoggerFactory.getLogger(RefreshInfoRequest.class);

    /**
     * 书籍ID
     */
    public String bookId;
    /**
     * 单元ID
     */
    public String unitId;
    /**
     * 课程ID
     */
    public String lessonId;
    /**
     * 练习ID
     */
    public int practiceId;
    /**
     * 作业ID，可以为null
     */
    public String homeworkId;
    /**
     * 此次作业的最终得分
     */
    public int score;


    /**
     * 将请求过来的JSON字符串解析为类对象
     *
     * @param 请求过来的JSON字符串
     * @return 解析好的对象
     */
    @SuppressWarnings("unchecked")
    public static RefreshInfoRequest parseRequest(String input) {
        RefreshInfoRequest result = new RefreshInfoRequest();
        try {
            Map<String, Object> jsonObj = JsonStringDeserializer.getInstance().deserialize(input);
            result.fillFrom(jsonObj);
        } catch (Exception ex) {
            logger.error("error RefreshInfoRequest {}", input);
        }
        return result;
    }

    /**
     * 生成一个对应的返回类型对象
     *
     * @return 该类型消息对应的返回类型对象
     */
    public static RefreshInfoResponse newResponse() {
        RefreshInfoResponse response = new RefreshInfoResponse();
        return response;
    }
}