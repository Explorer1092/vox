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

package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;

import javax.inject.Named;

/**
 * 成就：口语
 * 成长：无
 * 是否要评分？文档前后矛盾
 * 前面写：口语作业成就为A的次数
 * 后面写：完成口语5即可获得第一个成就
 * 对于文案来说，无歧义是最基本的需求。
 *
 * @author Xiaohai Zhang
 * @since Aug 4, 2016
 */
@Named("actionEventHandler.finishOral")
public class FinishOral extends AbstractActionEventHandler {
    @Override
    public ActionEventType getEventType() {
        return ActionEventType.FinishOral;
    }

    @Override
    public void handle(ActionEvent event) {
        int count = SafeConverter.toInt(event.getAttributes().get("count"));
        addAndGet(event.getUserId(),event.getType(),count);
    }
}
