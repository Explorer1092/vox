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

package com.voxlearning.utopia.service.walker.elf.net.messages;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;
import com.voxlearning.utopia.service.walker.elf.net.types.response.GetLevelPlantResponse;

import java.io.Serializable;
import java.util.Map;


/**
 * 获取所有我所有关卡获得的植物，并按顺序排列
 */
public class GetLevelPlantRequest extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 关卡ID
     */
    public String levelId;


    /**
     * 将请求过来的JSON字符串解析为类对象
     *
     * @param 请求过来的JSON字符串
     * @return 解析好的对象
     */
    @SuppressWarnings("unchecked")
    public static GetLevelPlantRequest parseRequest(String input) {
        GetLevelPlantRequest result = new GetLevelPlantRequest();
        Map<String, Object> jsonObj = JsonStringDeserializer.getInstance().deserialize(input);
        result.fillFrom(jsonObj);
        return result;
    }

    /**
     * 生成一个对应的返回类型对象
     *
     * @return 该类型消息对应的返回类型对象
     */
    public static GetLevelPlantResponse newResponse() {
        GetLevelPlantResponse response = new GetLevelPlantResponse();
        return response;
    }
}