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
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.response.GetAllStagePuzzleListResponse;

import java.io.Serializable;


/**
 * 全部打过的关卡的单词以及已获得的拼图
 */
public class GetAllStagePuzzleListRequest extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;


    /**
     * 将请求过来的JSON字符串解析为类对象
     *
     * @param 请求过来的JSON字符串
     * @return 解析好的对象
     */
    @SuppressWarnings("unchecked")
    public static GetAllStagePuzzleListRequest parseRequest(String input) {
        GetAllStagePuzzleListRequest req = JsonStringDeserializer.getInstance().deserialize(input, GetAllStagePuzzleListRequest.class);
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
    public static GetAllStagePuzzleListResponse newResponse() {
        GetAllStagePuzzleListResponse response = new GetAllStagePuzzleListResponse();
        return response;
    }
}