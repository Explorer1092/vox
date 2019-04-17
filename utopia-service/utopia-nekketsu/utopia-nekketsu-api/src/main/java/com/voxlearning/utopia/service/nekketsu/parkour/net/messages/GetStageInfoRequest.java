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
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.response.GetStageInfoResponse;

import java.io.Serializable;


/**
 * 关卡信息。包括关卡单词，我当前获得的拼图，我的本关最好成绩，本关星星数，本关排行榜以及随机挑战候选人和同班挑战候选人
 */
public class GetStageInfoRequest extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**  */
    public int stageId;


    /**
     * 将请求过来的JSON字符串解析为类对象
     *
     * @param 请求过来的JSON字符串
     * @return 解析好的对象
     */
    @SuppressWarnings("unchecked")
    public static GetStageInfoRequest parseRequest(String input) {
        GetStageInfoRequest req = JsonStringDeserializer.getInstance().deserialize(input, GetStageInfoRequest.class);
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
    public static GetStageInfoResponse newResponse() {
        GetStageInfoResponse response = new GetStageInfoResponse();
        return response;
    }
}