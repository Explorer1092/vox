package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookPracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.api.entity.PictureBookSeries;
import com.voxlearning.utopia.service.question.api.entity.PictureBookTopic;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 新绘本阅读
 * @author: Mr_VanGogh
 * @date: 2018/6/2 下午3:21
 */
@Named
public class VacationProcessNewHomeworkAnswerDetailLevelReadingsTemplate extends VacationProcessNewHomeworkAnswerDetailTemplate {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.LEVEL_READINGS;
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        int totalPictureBook = 0;
        int finishedCount = 0;
        double sumScore = 0;
        Long sumDuration = 0L;
        List<Map<String, Object>> studentAchievement = new LinkedList<>();
        NewHomeworkPracticeContent vacationHomeworkPracticeContent = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (Objects.nonNull(vacationHomeworkPracticeContent)
                && CollectionUtils.isNotEmpty(vacationHomeworkPracticeContent.getApps())
                && MapUtils.isNotEmpty(reportRateContext.getNewHomeworkResult().getPractices())
                && reportRateContext.getNewHomeworkResult().getPractices().containsKey(type)
                && MapUtils.isNotEmpty(reportRateContext.getNewHomeworkResult().getPractices().get(type).getAppAnswers())) {
            List<String> pictureBookIds = vacationHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .map(NewHomeworkApp::getPictureBookId)
                    .collect(Collectors.toList());
            totalPictureBook = pictureBookIds.size();
            Map<String, PictureBookPlus> pictureBookMap = pictureBookPlusServiceClient.loadByIds(pictureBookIds);
            NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(type);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            finishedCount = appAnswers.size();
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList
                    .stream()
                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
            for (NewHomeworkApp app : vacationHomeworkPracticeContent.getApps()) {
                if (app.getPictureBookId() != null
                        && appAnswers.containsKey(app.getPictureBookId())) {
                    NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(app.getPictureBookId());
                    PictureBookPlus pictureBook = pictureBookMap.get(app.getPictureBookId());
                    Map<String, Object> pictureBookInfo = NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, null, null, null);
                    List<Map<String, Object>> practices = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(app.getQuestions())) {
                        PictureBookPracticeType practiceType = PictureBookPracticeType.EXAM;
                        practices.add(MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName()));
                    }
                    if (CollectionUtils.isNotEmpty(app.getOralQuestions())) {
                        PictureBookPracticeType practiceType = PictureBookPracticeType.ORAL;
                        practices.add(MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName()));
                    }
                    if (app.containsDubbing()) {
                        PictureBookPracticeType practiceType = PictureBookPracticeType.DUBBING;
                        practices.add(MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName()));
                    }
                    pictureBookInfo.put("practices", practices);
                    if (newHomeworkResultAppAnswer.getScore() != null) {
                        int score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                        pictureBookInfo.put("score", score);
                        pictureBookInfo.put("scoreLevel", ScoreLevel.processLevel(score).getLevel());
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
            }
        }

        int avgScore = finishedCount > 0 ?
                new BigDecimal(sumScore)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;
        int avgDuration = finishedCount > 0 ?
                new BigDecimal(sumDuration)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;

        reportRateContext.getResultMap().put(type,
                MapUtils.m(
                        "avgScore", avgScore,
                        "avgScoreLevel", ScoreLevel.processLevel(avgScore).getLevel(),
                        "avgDuration", avgDuration,
                        "totalPictureBook", totalPictureBook,
                        "finishedCount", finishedCount,
                        "finished", finishedCount == totalPictureBook,
                        "studentAchievement", studentAchievement
                ));
    }
}
