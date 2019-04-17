package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.api.entity.JournalNewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.impl.service.AvengerQueueServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午6:46
 */
@Named
public class ER_CreateStudentExamResult extends SpringContainerSupport implements NewExamResultTask{

    @Inject
    private AvengerQueueServiceImpl avengerQueueService;

    @Override
    public void execute(NewExamResultContext context) {
        List<JournalNewExamProcessResult> results = new ArrayList<>();
        List<NewExamProcessResult> processResultList = new ArrayList<>();
        if (context.getCurrentProcessResult() != null) {
            processResultList.add(context.getCurrentProcessResult());
        }

        for (NewExamProcessResult npr : processResultList) {
            JournalNewExamProcessResult result = new JournalNewExamProcessResult();
            try {
                BeanUtils.copyProperties(result, npr);
                result.setId(null);
                result.setOldId(npr.getId());
                //result.setDuration(NewHomeworkUtils.processDuration(result.getDuration()));
                //result.setStudyType(StudyType.homework);
                //result.setRepair(context.getRepair());
                result.setCreateAt(new Date());
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Journal NewExam ProcessResult error.", context.getUserId());
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getNewExamId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_COMMON,
                        "op", "student homework result"
                ));
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
                return;
            }
            results.add(result);
        }
        avengerQueueService.sendJournalNewExamProcessResultProducer(results);
    }
}
