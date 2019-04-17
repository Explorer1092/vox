package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastPictureBookData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastPictureBookInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastReportRateContext;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessLiveCastHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.PictureBook;
import com.voxlearning.utopia.service.question.api.entity.PictureBookSeries;
import com.voxlearning.utopia.service.question.api.entity.PictureBookTopic;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessLiveCastHomeworkAnswerDetailReadingTemplate extends ProcessLiveCastHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READING;
    }

    //1、初始化数据
    //2、APP进行循环
    //3、在对2中 liveCastHomeworkResultMapToObjectiveConfigType循环
    //建议改动，1、先初始化数据结构,2、循环liveCastHomeworkResultMapToObjectiveConfigType 将数据挂上去，3、然后数据回归处理
    // 备用优化接口
    @Override
    public void processNewHomeworkAnswerDetail(LiveCastReportRateContext liveCastReportRateContext) {
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(liveCastReportRateContext.getLiveCastHomeworkResultMap(), ObjectiveConfigType.READING);
        if (MapUtils.isEmpty(liveCastHomeworkResultMapToObjectiveConfigType)) {
            return;
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = liveCastReportRateContext.getLiveCastHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.READING);
        Map<String, LiveCastPictureBookData> liveCastPictureBookDataMap = new LinkedHashMap<>();
        if (Objects.nonNull(newHomeworkPracticeContent)
                && CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())) {
            List<String> pictureBookIds = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .map(NewHomeworkApp::getPictureBookId)
                    .collect(Collectors.toList());
            Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList
                    .stream()
                    .collect(Collectors
                            .toMap(PictureBookSeries::getId, Function.identity()));
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList
                    .stream()
                    .collect(Collectors.
                            toMap(PictureBookTopic::getId, Function.identity()));
            for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
                PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
                if (pictureBook == null) {
                    continue;
                }
                LiveCastPictureBookData liveCastPictureBookData = new LiveCastPictureBookData();
                liveCastPictureBookData.setPictureBookId(app.getPictureBookId());
                liveCastPictureBookData.setTotalUserNum(MapUtils.isNotEmpty(liveCastReportRateContext.getUserMap()) ? liveCastReportRateContext.getUserMap().size() : 0);
                liveCastPictureBookData.setPictureInfo(NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, null));
                liveCastPictureBookDataMap.put(app.getPictureBookId(), liveCastPictureBookData);
            }

            for (LiveCastHomeworkResult liveCastHomeworkResult : liveCastHomeworkResultMapToObjectiveConfigType.values()) {
                if (liveCastHomeworkResult.getPractices().containsKey(ObjectiveConfigType.READING)) {
                    NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(ObjectiveConfigType.READING);
                    if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                        for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                            if (liveCastPictureBookDataMap.containsKey(entry.getKey())) {
                                LiveCastPictureBookData liveCastPictureBookData = liveCastPictureBookDataMap.get(entry.getKey());
                                liveCastPictureBookData.setFlag(true);
                                liveCastPictureBookData.setFinishedCount(liveCastPictureBookData.getFinishedCount() + 1);
                                int score = new BigDecimal(entry.getValue().getScore())
                                        .setScale(0, BigDecimal.ROUND_HALF_UP)
                                        .intValue();
                                liveCastPictureBookData.setSumScore(liveCastPictureBookData.getSumScore() + score);
                                int duration = new BigDecimal(entry.getValue().processDuration())
                                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                                        .intValue();


                                int minutes = duration / 60;
                                int second = duration % 60;
                                String time;
                                if (minutes == 0) {
                                    time = second + "秒";
                                } else {
                                    time = minutes + "分" + second + "秒";
                                }
                                liveCastPictureBookData.setSumDuration(liveCastPictureBookData.getSumDuration() + duration);
                                LiveCastPictureBookData.LiveCastStudentAchievement liveCastStudentAchievement = new LiveCastPictureBookData.LiveCastStudentAchievement();
                                liveCastStudentAchievement.setUserId(SafeConverter.toLong(liveCastHomeworkResult.getUserId()));
                                User user = liveCastReportRateContext.getUserMap().get(liveCastHomeworkResult.getUserId());
                                liveCastStudentAchievement.setUserName(user != null ? user.fetchRealname() : "");
                                liveCastStudentAchievement.setDuration(duration);
                                liveCastStudentAchievement.setScore(score);
                                liveCastStudentAchievement.setTime(time);
                                liveCastStudentAchievement.setFinishAt(entry.getValue().getFinishAt());
                                liveCastPictureBookData.getStudentInfo().add(liveCastStudentAchievement);
                            }
                        }
                    }
                }
            }
        }
        List<LiveCastPictureBookData> liveCastPictureBookDatas = liveCastPictureBookDataMap.values()
                .stream()
                .filter(LiveCastPictureBookData::isFlag)
                .collect(Collectors.toList());
        liveCastPictureBookDatas.forEach(o -> {
            if (o.getFinishedCount() > 0) {
                BigDecimal _avgScore = new BigDecimal(o.getSumScore() / o.getFinishedCount());
                int avgScore = _avgScore.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                BigDecimal _avgDuration = new BigDecimal(o.getSumDuration() / o.getFinishedCount());
                int avgDuration = _avgDuration.setScale(0, BigDecimal.ROUND_UP).intValue();
                int minutes = avgDuration / 60;
                int second = avgDuration % 60;
                String time;
                if (minutes == 0) {
                    time = second + "秒";
                } else {
                    time = minutes + "分" + second + "秒";
                }
                o.setTime(time);
                o.setAvgScore(avgScore);
                o.setAvgDuration(avgDuration);
            }
        });
        Map<String, List<LiveCastPictureBookData.LiveCastStudentAchievement>> studentsInfo = liveCastPictureBookDatas.stream()
                .collect(Collectors.toMap(LiveCastPictureBookData::getPictureBookId, LiveCastPictureBookData::getStudentInfo));
        liveCastPictureBookDatas.forEach(o -> o.setStudentInfo(null));
        liveCastReportRateContext.getResult()
                .put(ObjectiveConfigType.READING.name(),
                        MapUtils.m(
                                "pictureBookInfo", liveCastPictureBookDatas,
                                "studentsInfo", studentsInfo
                        ));


    }

    //TODO 数据结构发生变化，需要跟新wiki
    @Override
    public void processNewHomeworkAnswerDetailPersonal(LiveCastReportRateContext liveCastReportRateContext) {
        int totalPictureBook = 0;
        int finishedCount = 0;
        double sumScore = 0;
        Long sumDuration = 0L;
        Map<String, LiveCastPictureBookInfo> liveCastPictureBookInfoMap = new LinkedHashMap<>();
        NewHomeworkPracticeContent newHomeworkPracticeContent = liveCastReportRateContext.getLiveCastHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.READING);
        if (CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())
                && MapUtils.isNotEmpty(liveCastReportRateContext.getLiveCastHomeworkResult().getPractices().get(ObjectiveConfigType.READING).getAppAnswers())) {
            List<String> pictureBookIds = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .map(NewHomeworkApp::getPictureBookId)
                    .collect(Collectors.toList());
            totalPictureBook = pictureBookIds.size();
            Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
            NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastReportRateContext.getLiveCastHomeworkResult().getPractices().get(ObjectiveConfigType.READING);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            finishedCount = appAnswers.size();
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList
                    .stream()
                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
            for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
                PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
                if (pictureBook == null)
                    continue;
                LiveCastPictureBookInfo liveCastPictureBookInfo = new LiveCastPictureBookInfo();
                Map<String, Object> pictureBookInfo = NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, null);
                liveCastPictureBookInfo.setPictureBookInfo(pictureBookInfo);
                liveCastPictureBookInfo.setPictureBookId(app.getPictureBookId());
                liveCastPictureBookInfoMap.put(app.getPictureBookId(), liveCastPictureBookInfo);
            }
            for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : appAnswers.entrySet()) {
                if (liveCastPictureBookInfoMap.containsKey(entry.getKey())) {
                    LiveCastPictureBookInfo liveCastPictureBookInfo = liveCastPictureBookInfoMap.get(entry.getKey());
                    NewHomeworkResultAppAnswer value = entry.getValue();
                    int score = new BigDecimal(value.getScore())
                            .setScale(0, BigDecimal.ROUND_HALF_UP)
                            .intValue();
                    sumScore += score;
                    int duration = new BigDecimal(SafeConverter.toLong(value.processDuration()))
                            .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                            .intValue();
                    sumDuration += duration;
                    int minutes = duration / 60;
                    int second = duration % 60;
                    String time;
                    if (minutes == 0) {
                        time = second + "秒";
                    } else {
                        time = minutes + "分" + second + "秒";
                    }
                    liveCastPictureBookInfo.setTime(time);
                    liveCastPictureBookInfo.setScore(score);
                    liveCastPictureBookInfo.setDuration(duration);
                    liveCastPictureBookInfo.setFlag(true);
                }
            }
        }
        double avgScore = finishedCount > 0 ?
                new BigDecimal(sumScore)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;
        int avgDuration = finishedCount > 0 ?
                new BigDecimal(sumDuration)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;

        int minutes = avgDuration / 60;
        int second = avgDuration % 60;
        String time;
        if (minutes == 0) {
            time = second + "秒";
        } else {
            time = minutes + "分" + second + "秒";
        }

        liveCastReportRateContext.getResultMap().put(ObjectiveConfigType.READING,
                MapUtils.m(
                        "avgScore", avgScore,
                        "avgDuration", avgDuration,
                        "time", time,
                        "totalPictureBook", totalPictureBook,
                        "finishedCount", finishedCount,
                        "finished", finishedCount == totalPictureBook,
                        "studentAchievement", liveCastPictureBookInfoMap.values().stream().filter(LiveCastPictureBookInfo::isFlag).collect(Collectors.toList())
                ));
    }

}
