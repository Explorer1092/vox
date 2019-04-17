/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.*;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.*;

import javax.inject.Inject;

/**
 * @author Rui.Bao
 * @since 2014-08-08 11:02 AM
 */
abstract public class AbstractStudentIndexDataLoader extends SpringContainerSupport {

    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject protected HomeworkCommentLoaderClient homeworkCommentLoaderClient;
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected UserTagLoaderClient userTagLoaderClient;
    @Inject protected NewAccomplishmentLoaderClient newAccomplishmentLoaderClient;
    @Inject protected NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject protected NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject protected NewExamServiceClient newExamServiceClient;
    @Inject protected NewExamLoaderClient newExamLoaderClient;
    @Inject protected VacationHomeworkLoaderClient vacationHomeworkLoaderClient;
    @Inject protected GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject protected BusinessCacheSystem businessCacheSystem;
    @Inject protected SelfStudyHomeworkLoaderClient selfStudyHomeworkLoaderClient;
    @Inject protected OutsideReadingLoaderClient outsideReadingLoaderClient;
    @Inject protected AncientPoetryLoaderClient ancientPoetryLoaderClient;

    final public StudentIndexDataContext process(StudentIndexDataContext context) {
        if (context == null) {
            return null;
        }
        FlightRecorder.dot(getClass().getSimpleName());
        try {
            return doProcess(context);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return context;
        }
    }

    abstract protected StudentIndexDataContext doProcess(StudentIndexDataContext context);

}
