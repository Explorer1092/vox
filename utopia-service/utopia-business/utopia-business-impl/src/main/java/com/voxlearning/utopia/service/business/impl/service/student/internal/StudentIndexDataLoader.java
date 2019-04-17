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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Student index data loader implementation.
 * Actually, this is chain composer.
 *
 * @author Rui.Bao
 * @author changyuan.liu
 * @author Xiaohai Zhang
 * @since 2014-08-08 11:01 AM
 */
@Named
@StudentIndexDataChain({
        LoadStudentGroup.class,                         // 加载学生的分组信息
        LoadStudentClassmate.class,                     // 加载学生关联的所有同学
        LoadStudentTeacher.class,
        LoadStudentHomeworkLocation.class,              // 加载所有的作业定位
        LoadStudentNewExamCard.class,                   // 生成模拟考试卡

        LoadStudentHomeworkCard_English.class,          // 生成新英语作业卡
        LoadStudentHomeworkCard_Math.class,             // 生成数学作业卡
        LoadStudentHomeworkCard_Chinese.class,          // 生成语文作业卡
//        LoadStudentTermReviewHomeworkCard_English.class,// 生成英语期末复习作业卡片
//        LoadStudentTermReviewHomeworkCard_Math.class,   // 生成数学期末复习作业卡片
//        LoadStudentTermReviewHomeworkCard_Chinese.class,// 生成语文期末复习作业卡片

        LoadStudentMakeUpHomeworkCard_English.class,    // 生成英语补做作业卡
        LoadStudentMakeUpHomeworkCard_Math.class,       // 生成数学补做作业卡
        LoadStudentMakeUpHomeworkCard_Chinese.class,    // 生成语文补做作业卡
//        LoadStudentMakeUpTermReviewHomeworkCard_English.class, // 生成英语期末复习补做作业卡片
//        LoadStudentMakeUpTermReviewHomeworkCard_Math.class, // 生成数学期末复习补做作业卡片
//        LoadStudentMakeUpTermReviewHomeworkCard_Chinese.class, // 生成语文期末复习补做作业卡片

        LoadStudentVacationHomeworkCard.class, // 生成假期作业卡片
//        LoadStudentBasicReviewHomeworkCard.class, // 生成期末基础复习作业卡片
        LoadStudentUnitTestCard.class,  //单元检测
        LoadStudentAncientPoetryActivityCard.class, // 古诗活动卡片
        LoadStudentRewardsCard.class,
        LoadStudentNewUserTaskCard.class,
        LoadStudentPopups.class,
        LoadStudentParentsNoticeCard.class,     // 必须在loadStudentNewUserTaskCard后面
        LoadStudentParentRewardCard.class,
        LoadStudentTinyGroupRelevant.class,
        LoadStudentValidateTeacherInfo.class
})
public class StudentIndexDataLoader extends SpringContainerSupport {

    private final List<AbstractStudentIndexDataLoader> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        StudentIndexDataChain annotation = getClass().getAnnotation(StudentIndexDataChain.class);
        for (Class<? extends AbstractStudentIndexDataLoader> beanClass : annotation.value()) {
            AbstractStudentIndexDataLoader loader = getBean(beanClass);
            if (loader == null) {
                throw new IllegalStateException("Bean " + beanClass.getName() + " not found");
            }
            chains.add(loader);
        }
        logger.debug("Composed {} student index data loader chains", chains.size());
    }

    public Map<String, Object> process(StudentIndexDataContext context) {
        StudentIndexDataContext contextForUse = context;
        for (AbstractStudentIndexDataLoader unit : chains) {
            contextForUse = unit.process(contextForUse);
        }
        return contextForUse.getParam();
    }
}
