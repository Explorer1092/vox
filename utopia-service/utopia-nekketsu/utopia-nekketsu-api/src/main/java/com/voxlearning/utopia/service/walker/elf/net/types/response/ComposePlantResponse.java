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

package com.voxlearning.utopia.service.walker.elf.net.types.response;

import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;
import com.voxlearning.utopia.service.walker.elf.net.types.LevelPlantInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


/**
 * 点击合成植物
 */
public class ComposePlantResponse extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * 各关植物信息
     */
    public Collection<LevelPlantInfo> levelPlantInfoList = new ArrayList<>();
    /**
     * true时，需要把成就按钮+上new
     */
    public boolean showAchvNew;
    /**
     * true时，拯救书按钮+上new
     */
    public boolean showBookNew;
    /**
     * true时，弹出分享
     */
    public boolean showShare;


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}