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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceServiceClient;
import com.voxlearning.utopia.service.business.impl.service.TeachingResourceServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.teacher.DeprecatedAmbassadorService;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.constants.TeacherLevelValueType;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author RuiBao
 * @version 0.1
 * @since 5/14/2015
 */
@Named
public class ProcessInvitationReward extends AbstractTcpProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private DeprecatedAmbassadorService deprecatedAmbassadorService;
    @Inject private TeachingResourceServiceImpl teachingResourceService;

    @Override
    protected TeacherCertificationContext doProcess(TeacherCertificationContext context) {
        if (context.isRewardSkipped()) return context;

        Teacher teacher = context.getUser();

        if (Boolean.TRUE.equals(teacher.getIsInvite())) {
            InviteHistory history = asyncInvitationServiceClient.loadByInvitee(teacher.getId())
                    .enabled()
                    .findFirst();
            User inviter = history == null ? null : userLoaderClient.loadUser(history.getUserId());
            if (inviter != null) {
                // 结束邀请关系
                asyncInvitationServiceClient.getAsyncInvitationService()
                        .finishInviteHistories(teacher.getId())
                        .awaitUninterruptibly();
                // 教研员邀请人
                if (inviter.fetchUserType() == UserType.RESEARCH_STAFF) {
                    IntegralHistory ih = new IntegralHistory(inviter.getId(), IntegralType.成功邀请其他老师, 3000);
                    ih.setComment("您成功邀请" + teacher.getProfile().getRealname() + "参与课题，获得课题奖励300园丁豆");
                    ih.setRelationUserIdUniqueKey(teacher.getId());
                    userIntegralService.changeIntegral(ih);
                }
                // 教师邀请人
                if (inviter.fetchUserType() == UserType.TEACHER) {

                    // 预备大使加 努力值
                    ambassadorServiceClient.getAmbassadorService().addCompetitionScore(inviter.getId(), teacher.getId(), AmbassadorCompetitionScoreType.INVITE_TEACHER);

                    // 正式大使 添加积分
                    ambassadorServiceClient.getAmbassadorService().addAmbassadorScore(inviter.getId(), teacher.getId(), AmbassadorCompetitionScoreType.INVITE_TEACHER);
                    // 记录大使邀请人数 每个月 同科老师
                    Subject subject = userLoaderClient.loadUserSubject(inviter.getId());
                    if (subject != null && subject == teacher.getSubject()) {
                        deprecatedAmbassadorService.addInviteCountMonth(inviter.getId());
                    }

                    // 完成拉新任务
                    teachingResourceService.finishUserTask(inviter.getId(), TeachingResourceTask.RECRUIT_NEW.name());
                }
            }
        }
        return context;
    }
}
