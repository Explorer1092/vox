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
 * 关卡成绩排名。用于关卡内全市 全省 全国排名
 */
public class StageRankInfo extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * ${var.@comment}
     */
    public String roleId = "";
    /**
     * ${var.@comment}
     */
    public String roleName = "";
    /**
     * ${var.@comment}
     */
    public int level;
    /**
     * 头像
     */
    public String img = "";
    /**
     * 最快通关时间，毫秒数
     */
    public int personalBest;
    /**
     * ${var.@comment}
     */
    public int star;
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