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

package com.voxlearning.utopia.service.campaign.impl.lottery;

import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.impl.lottery.wrapper.*;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-11-4.
 */
@Named
public class CampaignWrapperFactory {

    @Inject private TeacherLotteryWrapper teacherLotteryWrapper;
    @Inject private StudentLotteryWrapper studentLotteryWrapper;
    @Inject private TeacherTermBeginLotteryWrapper teacherTermBeginLotteryWrapper;
    @Inject private MiddleTeacherLotteryWrapper middleTeacherLotteryWrapper;
    @Inject private TeacherScholarshipCopperWrapper teacherScholarshipCopperWrapper;
    @Inject private TeacherScholarshipGoldWrapper teacherScholarshipGoldWrapper;
    @Inject private TeacherScholarshipSilverWrapper teacherScholarshipSilverWrapper;
    @Inject private ParentPicListenLotteryWrapper parentPicListenLotteryWrapper;
    @Inject private AfentiPreparationLotteryWrapper afentiPreparationLotteryWrapper;
    @Inject private PicListenBookLotteryWrapper picListenBookLotteryWrapper;
    @Inject private TeacherVocationHomeworkWrapper teacherVocationHomeworkWrapper;
    @Inject private StudentAppLotteryWrapper studentAppLotteryWrapper;

    public AbstractCampaignWrapper get(CampaignType campaignType) {
        switch (campaignType) {
            case TEACHER_LOTTERY:
                return teacherLotteryWrapper;
            case STUDENT_LOTTERY_56:
                return studentLotteryWrapper;
            case TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN:
                return teacherTermBeginLotteryWrapper;
            case MIDDLE_TEACHER_LOTTERY:
                return middleTeacherLotteryWrapper;
            case TEACHER_SCHOLARSHIP_GOLD_LOTTERY:
                return teacherScholarshipGoldWrapper;
            case TEACHER_SCHOLARSHIP_SILVER_LOTTERY:
                return teacherScholarshipSilverWrapper;
            case TEACHER_SCHOLARSHIP_COPPER_LOTTERY:
                return teacherScholarshipCopperWrapper;
            case PARENT_PICLISTEN_LOTTERY_201761:
                return parentPicListenLotteryWrapper;
            case AFENTI_PREPARATION_LOTTERY:
                return afentiPreparationLotteryWrapper;
            case PICLISTENBOOK_ORDER_LOTTERY:
                return picListenBookLotteryWrapper;
            case VOCATION_HOMEWORK_LOTTERY_2017:
                return teacherVocationHomeworkWrapper;
            case STUDENT_APP_LOTTERY:
                return studentAppLotteryWrapper;
            case JUNIOR_ARRANGE_HOMEWORK_LOTTERY:
                return teacherTermBeginLotteryWrapper;
            case SUMMER_VOCATION_LOTTERY_2018:
                return teacherVocationHomeworkWrapper;
            default:
                return null;
        }
    }
}
