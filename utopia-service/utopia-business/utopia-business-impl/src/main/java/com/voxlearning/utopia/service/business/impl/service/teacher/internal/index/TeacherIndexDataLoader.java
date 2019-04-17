/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-8-5
 */
@Named
@Slf4j
@NoArgsConstructor
public class TeacherIndexDataLoader extends SpringContainerSupport {
    @Inject private LoadUserGuideInformation loadUserGuideInformation;
    @Inject private LoadHomeworkCard loadHomeworkCard;
    @Inject private LoadVacationHomeworkCard loadVacationHomeworkCard;
    @Inject private LoadMessConditions loadMessConditions;
    @Inject private LoadPopup loadPopup;
    @Inject private LoadAuthCard loadAuthCard;
    @Inject private LoadAfterAuthCard loadAfterAuthCard;
    @Inject private LoadInviteCard loadInviteCard;
    @Inject private LoadMentorInfo loadMentorInfo;
    @Inject private LoadActivityCard loadActivityCard;

    private final List<AbstractTeacherIndexDataLoader> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        // 引导流程模块，必须放在第一个执行
        chains.add(loadUserGuideInformation);
        // 作业，必须放在第二个执行
        chains.add(loadHomeworkCard);
        // 假期作业
        chains.add(loadVacationHomeworkCard);
        // 认证相关，只有未认证教师有
        chains.add(loadAuthCard);
        // 认证后一个月以内显示的东东
        chains.add(loadAfterAuthCard);
        // 获取各种条件(必须放在弹窗之前)
        chains.add(loadMessConditions);
        // 首页弹窗(一次性的和非一次性的)
        chains.add(loadPopup);
        // 邀请卡片
        chains.add(loadInviteCard);
        // 获取mentor 信息
        chains.add(loadMentorInfo);
        // 获取临时活动类卡片
        chains.add(loadActivityCard);

    }

    public Map<String, Object> process(final TeacherIndexDataContext context) {
        TeacherIndexDataContext contextForUse = context;
        for (AbstractTeacherIndexDataLoader unit : chains) {
            contextForUse = unit.process(contextForUse);
        }
        return contextForUse.getParam();
    }
}
