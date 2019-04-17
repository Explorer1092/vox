package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public class LiveHomeworkReportContext implements Serializable {
    private static final long serialVersionUID = -4250537697050311643L;

    private LiveCastHomework liveCastHomework;
    private Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap;
    private Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap;
    private Map<Long, User> userMap;
    private LiveHomeworkReport liveHomeworkReport;
    private Map<String, NewQuestion> newQuestionMap;

    private List<ObjectiveConfigType> objectiveConfigTypes = new LinkedList<>();
    private Map<Long, String> liveCastHomeworkResultIdMap;

    public LiveHomeworkReportContext(LiveCastHomework liveCastHomework,
                                     Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap,
                                     Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap,
                                     Map<Long, User> userMap,
                                     LiveHomeworkReport liveHomeworkReport,
                                     Map<Long, String> liveCastHomeworkResultIdMap) {
        this.liveCastHomework = liveCastHomework;
        this.liveCastHomeworkResultMap = liveCastHomeworkResultMap;
        this.liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultMap;
        this.userMap = userMap;
        this.liveHomeworkReport = liveHomeworkReport;
        this.liveCastHomeworkResultIdMap = liveCastHomeworkResultIdMap;
    }


    /**
     * 处理学生表格的数据
     */
    //1、初始化多少类型的名字，设置一共的人数
    //2、对学生进行循环
    //2->1学生没有答题
    //2->2学生已经答题的情况
    public void processStudentReportBriefs() {
        liveHomeworkReport.setTotalStudentNum(userMap.size());
        objectiveConfigTypes = liveCastHomework.getPractices()
                .stream()
                .map(NewHomeworkPracticeContent::getType)
                .collect(Collectors.toList());
        liveHomeworkReport.setObjectiveConfigTypeNames(
                objectiveConfigTypes.stream()
                        .map(ObjectiveConfigType::getValue)
                        .collect(Collectors.toList()));

        int totalScore = 0;
        for (Map.Entry<Long, String> entry : liveCastHomeworkResultIdMap.entrySet()) {
            User user = userMap.get(entry.getKey());
            LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultMap.get(entry.getValue());
            LiveHomeworkReport.StudentReportBrief studentReportBrief = new LiveHomeworkReport.StudentReportBrief();
            liveHomeworkReport.getStudentReportBriefs().add(studentReportBrief);
            studentReportBrief.setSid(user.getId());
            studentReportBrief.setSname(user.fetchRealname());
            List<String> scoreInfo = new LinkedList<>();
            studentReportBrief.setScoreInfo(scoreInfo);
            if (liveCastHomeworkResult == null || liveCastHomeworkResult.getPractices() == null) {
                scoreInfo.addAll(objectiveConfigTypes.stream().map(ignored -> "未完成").collect(Collectors.toList()));
            } else {
                if (liveCastHomeworkResult.isFinished()) {
                    studentReportBrief.setFinishAt(DateUtils.dateToString(liveCastHomeworkResult.getFinishAt(), "MM月dd日 HH:mm"));
                    Long duration = liveCastHomeworkResult.processDuration();
                    int minute = new BigDecimal(SafeConverter.toLong(duration)).divide(new BigDecimal(60), BigDecimal.ROUND_UP).intValue();
                    studentReportBrief.setDuration(minute + "分");
                    int score = SafeConverter.toInt(liveCastHomeworkResult.liveCastProcessScore());
                    //-1的时候表示个人成绩有未批改
                    //-2表示都是没有分数的类型
                    if (score != -1 && score != -2) {
                        totalScore += score;
                        studentReportBrief.setPersonAverScore(SafeConverter.toString(score));
                        liveHomeworkReport.setCalculationScoreNum(1 + liveHomeworkReport.getCalculationScoreNum());
                    }

                    studentReportBrief.setCastTime(duration);
                    studentReportBrief.setScore(score);
                    studentReportBrief.setFinishTime(liveCastHomeworkResult.getFinishAt().getTime());
                    liveHomeworkReport.setHomeworkFinishedNum(liveHomeworkReport.getHomeworkFinishedNum() + 1);
                    studentReportBrief.setFinished(true);
                    Date finishAt = liveCastHomeworkResult.getFinishAt();
                    studentReportBrief.setRepair((finishAt.getTime() > liveCastHomework.getEndTime().getTime()
                            || (liveCastHomework.getCheckedAt() != null && finishAt.getTime() > liveCastHomework.getCheckedAt().getTime())));
                }
                studentReportBrief.setComment(liveCastHomeworkResult.getComment());
                for (ObjectiveConfigType objectiveConfigType : objectiveConfigTypes) {
                    if (liveCastHomeworkResult.getPractices() != null && liveCastHomeworkResult.getPractices().containsKey(objectiveConfigType)) {
                        NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(objectiveConfigType);
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.isFinished()) {
                            if (objectiveConfigType == ObjectiveConfigType.MENTAL) {
                                Integer mentalDuration = newHomeworkResultAnswer.processDuration();
                                if (mentalDuration != null) {
                                    int minutes = mentalDuration / 60;
                                    int second = mentalDuration % 60;
                                    if (minutes == 0) {
                                        scoreInfo.add(SafeConverter.toString(newHomeworkResultAnswer.processScore(objectiveConfigType), "0") + "分" + " (" + second + "\"" + ")");
                                    } else {
                                        scoreInfo.add(SafeConverter.toString(newHomeworkResultAnswer.processScore(objectiveConfigType), "0") + "分" + " (" + minutes + "'" + second + "\"" + ")");
                                    }
                                } else {
                                    scoreInfo.add(SafeConverter.toString(newHomeworkResultAnswer.processScore(objectiveConfigType), "0") + "分");
                                }
                            } else {
                                if (NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(objectiveConfigType)) {
                                    scoreInfo.add("已完成");
                                } else {
                                    if (objectiveConfigType.isSubjective()) {
                                        Integer score = newHomeworkResultAnswer.processScore(objectiveConfigType);
                                        if (score == null) {
                                            scoreInfo.add("未打分");
                                        } else {
                                            scoreInfo.add(SafeConverter.toString(newHomeworkResultAnswer.processScore(objectiveConfigType), "0") + "分");
                                        }
                                    } else {
                                        scoreInfo.add(SafeConverter.toString(newHomeworkResultAnswer.processScore(objectiveConfigType), "0") + "分");
                                    }
                                }
                            }

                        } else {
                            scoreInfo.add("未完成");
                        }
                    } else {
                        scoreInfo.add("未完成");
                    }
                }
            }
        }
        liveHomeworkReport.getStudentReportBriefs()
                .sort(Comparator.comparingLong(LiveHomeworkReport.StudentReportBrief::getFinishTime));

        if (liveHomeworkReport.getCalculationScoreNum() != 0) {
            int averScore = new BigDecimal(totalScore).divide(new BigDecimal(liveHomeworkReport.getCalculationScoreNum()), BigDecimal.ROUND_HALF_UP).intValue();
            liveHomeworkReport.setClazzAverScore(averScore);
        }
    }
}
