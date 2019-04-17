package com.voxlearning.utopia.service.newhomework.impl.template.internal;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.VacationInternalProcessHomeworkAnswerTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class VacationInternalProcessHomeworkAnswerForReadingTemple extends VacationInternalProcessHomeworkAnswerTemple {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READING;
    }

    @Inject
    private PictureBookLoaderClient pictureBookLoaderClient;

    @Override
    public void internalProcessHomeworkAnswer(Map<ObjectiveConfigType, Object> resultMap, Map<String, VacationHomeworkProcessResult> allProcessResultMap, Map<Integer, NewContentType> contentTypeMap, Map<String, NewQuestion> allQuestionMap, VacationHomework vacationHomework, VacationHomeworkResult vacationHomeworkResult, ObjectiveConfigType type) {
        NewHomeworkPracticeContent newHomeworkPracticeContent = vacationHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return;
        if (CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps()))
            return;
        if (vacationHomeworkResult.getPractices() == null)
            return;
        if (!vacationHomeworkResult.getPractices().containsKey(type))
            return;
        NewHomeworkResultAnswer newHomeworkResultAnswer = vacationHomeworkResult.getPractices().get(type);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
        if (MapUtils.isEmpty(appAnswers))
            return;

        int finishedCount = appAnswers.size();
        double sumScore = 0;
        Long sumDuration = 0L;
        List<Map<String, Object>> studentAchievement = new LinkedList<>();

        List<String> pictureBookIds = newHomeworkPracticeContent
                .getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkApp::getPictureBookId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList
                .stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
        for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
            if (app.getPictureBookId() == null)
                continue;
            if (!pictureBookMap.containsKey(app.getPictureBookId()))
                continue;
            if (!appAnswers.containsKey(app.getPictureBookId()))
                continue;
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(app.getPictureBookId());
            PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
            Map<String, Object> pictureBookInfo = NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, null);
            if (newHomeworkResultAppAnswer.getScore() != null) {
                int score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                pictureBookInfo.put("score", score);
                sumScore += score;
            } else {
                pictureBookInfo.put("score", null);
            }
            if (newHomeworkResultAppAnswer.getDuration() != null) {
                int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
                pictureBookInfo.put("duration", duration);
                sumDuration += duration;
            } else {
                pictureBookInfo.put("duration", null);
            }
            studentAchievement.add(pictureBookInfo);
        }
        double avgScore = finishedCount > 0 ?
                new BigDecimal(sumScore)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;
        Map<String, Object> value = MapUtils.m(
                "avgScore", avgScore,
                "avgDuration", sumDuration,
                "totalPictureBook", pictureBookIds.size(),
                "finishedCount", finishedCount,
                "finished", finishedCount == pictureBookIds.size(),
                "studentAchievement", studentAchievement
        );
        if (MapUtils.isNotEmpty(value)) {
            resultMap.put(type, value);
        }
    }
}
