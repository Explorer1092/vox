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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.queue.PkQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/21
 */
@Named
public class PostFinishNewHomeworkMakeUpRelevant extends SpringContainerSupport implements PostFinishHomework {
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private NewHomeworkServiceImpl newHomeworkService;

    @Inject private PkQueueProducer pkQueueProducer;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        if (!context.getHomework().isHomeworkTerminated()) return;
        HomeworkType homeworkType;
        switch (context.getHomework().getSubject()){
            case ENGLISH:
                homeworkType = HomeworkType.ENGLISH;
                break;
            case MATH:
                homeworkType = HomeworkType.MATH;
                break;
            case CHINESE:
                homeworkType = HomeworkType.CHINESE;
                break;
            default:
                homeworkType = null;
        }
        // 记录每月完成作业人数，检查作业加一部分，补做作业加一部分
        newHomeworkCacheService.getMonthFinishHomeworkCountManager().increase(Collections.singleton(context.getUserId()), homeworkType);

        // 记录学生在老师的班级中完成作业次数，检查作业加一部分，补做作业加一部分
        if (context.getHomework().isHomeworkChecked() && context.getTeacherId() != null) {
            newHomeworkService.incFinishHomeworkCount(context.getTeacherId(),
                    context.getClazzGroup().getClazzId(), context.getUserId());
        }
    }
}
