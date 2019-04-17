package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.ErrorQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/2/27
 */
@Named
public class SS_QueueProcessHomeworkResult extends SpringContainerSupport implements SelfStudyHomeworkResultTask {

    @Inject private NewHomeworkQueueServiceImpl newHomeworkQueueService;

    @Override
    public void execute(SelfStudyHomeworkContext context) {
        if (ObjectiveConfigType.ORAL_INTERVENTIONS.equals(context.getObjectiveConfigType())) {
            return;
        }
        SubHomeworkProcessResult processResult = context.getProcessResult();
        JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
        try {
            BeanUtils.copyProperties(result, processResult);
            result.setId(null);
            result.setProcessResultId(processResult.getId());
            result.setDuration(NewHomeworkUtils.processDuration(result.getDuration()));
            result.setStudyType(StudyType.selfstudy);
            result.setCreateAt(new Date());
            //提供给包大爷错题本，需要知道原题IDS
            if(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.equals(processResult.getObjectiveConfigType())){
                Map<String, List<ErrorQuestion>> errorQuestionsMap = context.getSelfStudyHomework().findAppErrorQuestionsMap(processResult.getObjectiveConfigType());
                if(errorQuestionsMap != null){
                    List<ErrorQuestion> errorQuestions = errorQuestionsMap.get(processResult.getCourseId());
                    if(CollectionUtils.isNotEmpty(errorQuestions)){
                        result.getAdditions().put("relatedIds", JsonUtils.toJson(errorQuestions));
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            return;
        }
        newHomeworkQueueService.saveJournalNewHomeworkProcessResults(Collections.singletonList(result));
        // newHomeworkQueueService.saveJournalNewHomeworkProcessResultToKafka(results);
    }
}
