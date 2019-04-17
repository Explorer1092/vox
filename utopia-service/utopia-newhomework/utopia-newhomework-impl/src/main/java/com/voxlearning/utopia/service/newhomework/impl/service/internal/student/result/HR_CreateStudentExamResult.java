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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Ruib
 * @author xuesong.zhang
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class HR_CreateStudentExamResult extends SpringContainerSupport implements HomeworkResultTask {

    @Inject private NewHomeworkQueueServiceImpl newHomeworkQueueService;

    @Override
    public void execute(HomeworkResultContext context) {

        List<JournalNewHomeworkProcessResult> results = new ArrayList<>();
        List<NewHomeworkProcessResult> processResultList = new ArrayList<>();
        if (MapUtils.isNotEmpty(context.getProcessResult())) {
            processResultList.addAll(context.getProcessResult().values());
        }
        if (MapUtils.isNotEmpty(context.getProcessOralResult())) {
            processResultList.addAll(context.getProcessOralResult().values());
        }
        if (CollectionUtils.isNotEmpty(context.getOcrMentalProcessResults())) {
            processResultList.addAll(context.getOcrMentalProcessResults());
        }
        if (CollectionUtils.isNotEmpty(context.getOcrDictationProcessResults())) {
            processResultList.addAll(context.getOcrDictationProcessResults());
        }
        for (NewHomeworkProcessResult npr : processResultList) {
            JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
            try {
                BeanUtils.copyProperties(result, npr);
                result.setId(null);
                result.setProcessResultId(npr.getId());
                result.setDuration(NewHomeworkUtils.processDuration(result.getDuration()));
                result.setStudyType(StudyType.homework);
                result.setRepair(context.getRepair());
                result.setCreateAt(new Date());
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Journal ProcessResult error.", context.getUserId());
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_COMMON,
                        "op", "student homework result"
                ));
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
                return;
            }
            results.add(result);
        }
        newHomeworkQueueService.saveJournalNewHomeworkProcessResults(results);
        // newHomeworkQueueService.saveJournalNewHomeworkProcessResultToKafka(results);
    }
}
