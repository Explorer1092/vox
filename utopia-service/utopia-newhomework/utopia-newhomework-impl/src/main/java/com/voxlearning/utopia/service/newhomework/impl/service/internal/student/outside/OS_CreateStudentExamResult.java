package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingProcessResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;

@Named
public class OS_CreateStudentExamResult extends SpringContainerSupport implements OutsideReadingResultTask {
    @Inject private NewHomeworkQueueServiceImpl newHomeworkQueueService;

    @Override
    public void execute(OutsideReadingContext context) {
        OutsideReadingProcessResult processResult = context.getProcessResult();
        if (processResult == null) {
            return;
        }
        JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
        try {
            BeanUtils.copyProperties(result, processResult);
            result.setId(null);
            result.setProcessResultId(processResult.getId());
            result.setDuration(NewHomeworkUtils.processDuration(result.getDuration()));
            result.setStudyType(StudyType.homework);
            result.setCreateAt(new Date());
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Journal ProcessResult error.", context.getUserId());
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getOutsideReading().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_COMMON,
                    "op", "student homework result"
            ));
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
        }
        newHomeworkQueueService.saveJournalNewHomeworkProcessResults(Collections.singletonList(result));
    }
}
