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

package com.voxlearning.utopia.service.business.impl.service.student.internal.app;

import com.voxlearning.utopia.service.business.impl.service.student.internal.*;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.card.*;

import javax.inject.Named;

/**
 * 学生端app首页作业卡data loader
 * Created by Shuai Huan on 2016/1/14.
 */
@Named
@StudentIndexDataChain({
        LoadStudentGroup.class,
        LoadStudentHomeworkLocation.class,

        LoadStudentAppNewExamCard.class,

        LoadStudentAppHomeworkCard_English.class,
        LoadStudentAppHomeworkCard_Math.class,
        LoadStudentAppHomeworkCard_Chinese.class,

        LoadStudentAppOcrHomeworkCard_English.class,
        LoadStudentAppOcrHomeworkCard_Math.class,

        LoadStudentAppVacationHomeworkCard.class,

//        LoadStudentAppTermReviewHomeworkCard_English.class,
//        LoadStudentAppTermReviewHomeworkCard_Math.class,
//        LoadStudentAppTermREviewHomeworkCard_Chinese.class,

        LoadStudentAppMakeUpHomeworkCard_English.class,
        LoadStudentAppMakeUpHomeworkCard_Math.class,
        LoadStudentAppMakeUpHomeworkCard_Chinese.class,

        LoadStudentAppMakeUpOcrHomeworkCard_English.class,
        LoadStudentAppMakeUpOcrHomeworkCard_Math.class,

//        LoadStudentAppMakeUpTermReviewHomeworkCard_English.class,
//        LoadStudentAppMakeUpTermReviewHomeworkCard_Math.class,
//        LoadStudentAppMakeUpTermReviewHomeworkCard_Chinese.class,

        LoadStudentAppUnitTestCard.class,   //单元检测

//        LoadStudentAppBasicReviewHomeworkCard.class,
//         LoadStudentAppNationalDayHomeworkCard.class,

        // 如果LoadStudentAppNewExamCard中有数据了（__enterableNewExamCards）不再设定素质测试卡片
        // LoadAbilityExamCard.class,

//        LoadStudentAppMothersDayHomeworkCard.class,
//        LoadStudentAppKidsDayHomeworkCard.class

        LoadStudentAppCorrectionHomeworkCard.class,     //生成作业巩固卡
        LoadStudentAppActivityCard.class,
        LoadStudentAppAncientPoetryActivityCard.class, // 诗词大会卡片
        LoadStudentAppOutsideReadingCard.class     // 课外阅读任务卡片

})
public class StudentAppIndexDataLoader extends StudentIndexDataLoader {
}