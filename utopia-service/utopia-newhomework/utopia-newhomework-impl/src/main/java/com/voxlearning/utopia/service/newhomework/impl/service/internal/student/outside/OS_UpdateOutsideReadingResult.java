package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.google.common.collect.Maps;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingMissionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.OutsideReadingProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.OutsideReadingResultDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author majianxin
 */
@Named
public class OS_UpdateOutsideReadingResult extends SpringContainerSupport implements OutsideReadingResultTask {

    @Inject private OutsideReadingResultDao outsideReadingResultDao;
    @Inject private OutsideReadingProcessResultDao outsideReadingProcessResultDao;

    @Override
    public void execute(OutsideReadingContext context) {

        String questionId = context.getStudentHomeworkAnswer().getQuestionId();
        OutsideReadingProcessResult processResult = context.getProcessResult();
        OutsideReadingResult outsideReadingResult = outsideReadingResultDao.load(context.getReadingId(), context.getUserId());
        LinkedHashMap<String, String> missionAnswers = outsideReadingResult.getMissionAnswers(context.getMissionId());

        Map<String, OutsideReadingMissionResult> othersMissionResult = Maps.filterKeys(outsideReadingResult.getNotNullMissionResults(), o -> !Objects.equals(o, context.getMissionId()));
        boolean otherMissionFinished = othersMissionResult.values().stream().allMatch(OutsideReadingMissionResult::isFinished);

        //关卡完成, 算星星
        int star = 0;
        int questionCount = context.getSubjectiveQuestionIds().size() + context.getObjectiveQuestionIds().size();
        boolean updateProcessResultId = !missionAnswers.containsKey(questionId) || processResult.getGrasp();
        if (updateProcessResultId) {
            missionAnswers.put(questionId, processResult.getId());
        }
        if (questionCount == missionAnswers.size()) {
            context.setMissionFinished(Boolean.TRUE);
            if (otherMissionFinished) {
                context.setReadingFinished(Boolean.TRUE);
            }
            Map<String, String> objectiveProcessIdMap = Maps.filterKeys(missionAnswers, context.getObjectiveQuestionIds()::contains);
            Map<String, OutsideReadingProcessResult> processResultMap = outsideReadingProcessResultDao.loads(objectiveProcessIdMap.values());
            List<Boolean> objectiveQuestionGrasp = processResultMap.values().stream().map(OutsideReadingProcessResult::getGrasp).collect(Collectors.toList());
            star = calculateStar(objectiveQuestionGrasp);
        }

        OutsideReadingMissionResult missionResult = outsideReadingResult.getMissionResult(context.getMissionId());
        if (star == 3 && (missionResult == null || SafeConverter.toInt(missionResult.getStar()) < 3)) {
            context.setAddReadingCount(true);
        }

        OutsideReadingResult readingResult = outsideReadingResultDao.finishOutsideReading(outsideReadingResult,
                context.getMissionId(),
                missionAnswers,
                star,
                context.isReadingFinished(),
                context.isMissionFinished());
        context.setReadingResult(readingResult);
    }

    //算星星
    private int calculateStar(List<Boolean> objectiveQuestionGrasp) {
        int star;
        star = 1;
        if (objectiveQuestionGrasp.stream().noneMatch(o -> Objects.equals(o, Boolean.FALSE))) {
            star = 3;
        } else if (objectiveQuestionGrasp.stream().anyMatch(o -> Objects.equals(o, Boolean.TRUE))) {
            star = 2;
        }
        return star;
    }
}
