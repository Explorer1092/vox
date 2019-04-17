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

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.business.net.frameworkapp.types.response.UseItemResponse;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;

import java.io.Serializable;
import java.util.Map;


/**
 * 使用消息
 */
public class UseItemRequest extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 要使用的IP的类型ID
     */
    public int ipTypeId;
    /**
     * 要使用的角色ID
     */
    public String roleId;
    /**
     * 要使用的装备ID
     */
    public String equipId;
    /**
     * 要使用的精灵ID
     */
    public String spriteId;
    /**
     * 要使用的技能ID
     */
    public String spellId;


    /**
     * 将请求过来的JSON字符串解析为类对象
     *
     * @param 请求过来的JSON字符串
     * @return 解析好的对象
     */
    @SuppressWarnings("unchecked")
    public static UseItemRequest parseRequest(String input) {
        UseItemRequest result = new UseItemRequest();
        Map<String, Object> jsonObj = JsonStringDeserializer.getInstance().deserialize(input);
        result.fillFrom(jsonObj);
        return result;
    }

    /**
     * 生成一个对应的返回类型对象
     *
     * @return 该类型消息对应的返回类型对象
     */
    public static UseItemResponse newResponse() {
        UseItemResponse response = new UseItemResponse();
        return response;
    }
}