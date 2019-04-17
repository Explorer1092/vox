package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Named
public class DH_ProcessHomeworkRepairData extends AbstractHomeworkIndexDataProcessor {
    @Override
    protected void doProcess(HomeworkIndexDataContext context) {
        //当作业没有未做的作业类型且newHomeworkResult的finishAt为空的时候说明学生完成作业的时候数据，因此需要修复数据
        if ((context.getUndoPracticesCount() == 0 && !context.getFinished())
                || context.getNeedFinish()) {
            doHomeworkProcessor.finishHomework(context.getNewHomework(), context.getNewHomeworkResult(), context.getStudentId());
        }
        //针对生字认读的数据修复
        if (wordIsNeedRepair(context.getDoPractices())) {
            doHomeworkProcessor.fixWordHomeworkResultData(context.getNewHomework(), context.getNewHomeworkResult(), context.getStudentId());
        }
    }

    // 修复生字认读数据
    private boolean wordIsNeedRepair(LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> doPractices) {
        boolean need = false;
        for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : doPractices.entrySet()) {
            if (ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(entry.getKey())) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = entry.getValue();
                if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                    for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                        if (appAnswer != null && (appAnswer.getAppQuestionNum() == null || appAnswer.getStandardNum() == null)) {
                            need = true;
                            break;
                        }
                    }
                }
            }
        }
        return need;
    }
}
