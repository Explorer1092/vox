package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_CreateStudentExamResult extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private NewHomeworkQueueServiceImpl newHomeworkQueueService;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        List<JournalNewHomeworkProcessResult> results = new ArrayList<>();
        List<VacationHomeworkProcessResult> processResultList = new ArrayList<>();
        if (MapUtils.isNotEmpty(context.getProcessResult())) {
            processResultList.addAll(context.getProcessResult().values());
        }
        if (MapUtils.isNotEmpty(context.getProcessOralResult())) {
            processResultList.addAll(context.getProcessOralResult().values());
        }
        for (VacationHomeworkProcessResult npr : processResultList) {
            JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
            try {
                BeanUtils.copyProperties(result, npr);
                result.setId(null);
                result.setProcessResultId(npr.getId());
                result.setDuration(NewHomeworkUtils.processDuration(result.getDuration()));
                result.setStudyType(StudyType.vacationHomework);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Journal ProcessResult error.", context.getUserId());
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getVacationHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_COMMON,
                        "op", "student vacation homework result"
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
