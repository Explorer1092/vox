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
 * AI。可能是关卡AI，也可能是根据真实用户的成绩做成的AI
 */
public class ParkourAi extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * 角色ID.为空时代表关卡AI
     */
    public String roleId = "";
    /**
     * 角色名
     */
    public String roleName = "";
    /**
     * ${var.@comment}
     */
    public String img = "";
    /**
     * 等级。需要根据等级来算出对应的平跑速度
     */
    public int level;
    /**
     * 每道题平均耗时毫秒数。根据这个数据，让ai定时进行一次假答题
     */
    public int timePerQuestion;
    /**
     * 正确率,每次假答题的答对概率
     */
    public double correctRate;
    /**
     * ${var.@comment}
     */
    public String schoolName = "";
    /**
     * 学校全名
     */
    public String schoolFullName = "";


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}