/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.integral.api.constants.CreditType;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.api.support.CreditHistoryBuilderFactory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticCredit;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkStudyMasterDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.queue.ClazzZoneQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 计算(纯主观作业不计算且不含补做)
 * <p>
 * 1、成绩优异[学科]之星：star of excellent
 * 分数最高的前三名，分数相同取用时最短，用时一样取提交时间最早的
 * <p>
 * 2、口算之星 ：star of calculation
 * 口算题分数最高的前三名，分数相同取用时最短
 * <p>
 * 3、专注之星：star of focus
 * 作业中断时间最短，且成绩高于班均
 * 单次作业中断时间=作业时间-做题时间，且作业分数>=班级平均分 且 做题时间 < 班级平均做题时间
 * <p>
 * 4、积极之星：star of positive
 * 最早完成，且成绩高于班均
 * <p>
 * Created by tanguohong on 2016/7/21.
 * updated by tanguohong on 2017/11/09.
 */
@Named
public class PostCheckHomeworkStudyMaster extends SpringContainerSupport implements PostCheckHomework {

    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private NewHomeworkStudyMasterDao newHomeworkStudyMasterDao;
    @Inject
    private ClazzZoneQueueProducer clazzZoneQueueProducer;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private NewHomeworkResultServiceImpl newHomeworkResultService;
    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {

        NewAccomplishment accomplishment = context.getAccomplishment();
        LinkedHashMap<ObjectiveConfigType, NewHomeworkPracticeContent> practices = context.getHomework().findPracticeContents();
        //纯主观作业不计算没有一人完成作业不计算
        if (practices.size() == 1 && context.getHomework().includeSubjective) return;
        //即每次作业需满足至少5个人完成，才进行学
        if (accomplishment == null || accomplishment.getDetails() == null)
            return;

        List<Long> userIds = new ArrayList<>();
        for (Map.Entry<String, NewAccomplishment.Detail> entry : accomplishment.getDetails().entrySet()) {
            NewAccomplishment.Detail detail = entry.getValue();
            if (detail != null) {
                boolean repair = SafeConverter.toBoolean(detail.getRepair());
                if (!repair) {
                    //非补做是目标处理数据
                    long userId = SafeConverter.toLong(entry.getKey());
                    userIds.add(userId);
                }
            }
        }
        if (userIds.size() < 5) {
            return;
        }

        boolean isMental = practices.containsKey(ObjectiveConfigType.MENTAL_ARITHMETIC);
        Map<Long, NewHomeworkResult> studentHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(context.getHomework().toLocation(), userIds, false);
        List<StudentData> datas = new ArrayList<>();
        double totalScore = 0d;
        long totalDuration = 0L;
        int totalCount = 0;

        for (Long studentId : userIds) {
            NewHomeworkResult studentHomeworkResult = studentHomeworkResultMap.get(studentId);
            if (studentHomeworkResult != null && studentHomeworkResult.isFinished()) {
                Integer score = studentHomeworkResult.processScore();
                if (score == null) continue;
                Long duration = studentHomeworkResult.processDuration();
                StudentData data = new StudentData();
                data.setStudentId(studentId);
                data.setScore(score);
                data.setDuration(duration);
                data.setZdDuration((studentHomeworkResult.getFinishAt().getTime() - studentHomeworkResult.getUserStartAt().getTime()) / 1000 - studentHomeworkResult.processDuration());

                if (data.getZdDuration() < 0) {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", studentHomeworkResult.getUserId(),
                            "mod1", studentHomeworkResult.getId(),
                            "op", "ZdDuration"
                    ));
                }

                data.setRepair(false);
                if (isMental) {
                    data.setMentalScore(studentHomeworkResult.practices.get(ObjectiveConfigType.MENTAL_ARITHMETIC).processScore(ObjectiveConfigType.MENTAL_ARITHMETIC));
                    data.setMentalDuration(studentHomeworkResult.practices.get(ObjectiveConfigType.MENTAL_ARITHMETIC).processDuration());
                }
                data.setFinishAt(studentHomeworkResult.getFinishAt());
                datas.add(data);
                totalDuration += duration;
                totalScore += score;
                totalCount++;
            }
        }

        if (totalCount <= 0) {
            return;
        }

        int avgScore = new BigDecimal(totalScore).divide(new BigDecimal(totalCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long avgDuration = new BigDecimal(totalDuration).divide(new BigDecimal(totalCount), 0, BigDecimal.ROUND_HALF_UP).longValue();

        /**
         * 成绩优异[学科]之星：star of excellent
         * 分数最高的前三名，分数相同取用时最短，用时一样取提交时间最早的
         */
        Comparator<StudentData> excellent = (o1, o2) -> {
            if (o2.getScore().equals(o1.getScore())) {
                return Long.compare(o1.getDuration(), o2.getDuration());
            } else {
                return Integer.compare(o2.getScore(), o1.getScore());
            }
        };

        /**
         * 口算之星 ：star of calculation
         * 口算题分数最高的前三名，分数相同取用时最短,最早完成
         */
        Comparator<StudentData> calculation = (o1, o2) -> {
            if (o2.getMentalScore().equals(o1.getMentalScore())) {
                int compare = Long.compare(o1.getMentalDuration(), o2.getMentalDuration());
                if (compare == 0) {
                    compare = Long.compare(o1.getFinishAt().getTime(), o2.getFinishAt().getTime());
                }
                return compare;
            } else {
                return Integer.compare(o2.getMentalScore(), o1.getMentalScore());
            }
        };

        /**
         * 专注之星：star of focus
         * 作业中断时间最短，且成绩高于班均
         * 单次作业中断时间=作业时间-做题时间，且作业分数>=班级平均分 且 做题时间 < 班级平均做题时间
         */
        Comparator<StudentData> focus = (o1, o2) -> Long.compare(o1.getZdDuration(), o2.getZdDuration());

        /**
         * 积极之星：star of positive
         * 最早完成，且成绩高于班均
         */
        Comparator<StudentData> positive = (o1, o2) -> Long.compare(o1.getFinishAt().getTime(), o2.getFinishAt().getTime());

        /**
         * 1、成绩优异[学科]之星：star of excellent
         * 2、口算之星 ：star of calculation
         * 3、专注之星：star of focus
         * 4、积极之星：star of positive
         */
        List<StudentData> excellents = datas.stream().sorted(excellent).collect(Collectors.toList());
        List<StudentData> calculations = isMental ? datas.stream().filter(o -> SafeConverter.toInt(o.getMentalScore()) >= 60).sorted(calculation).collect(Collectors.toList()) : Collections.emptyList();
        List<StudentData> focuss = datas.stream().filter(d -> d.getScore() >= avgScore && d.getDuration() < avgDuration).sorted(focus).collect(Collectors.toList());
        List<StudentData> positives = datas.stream().filter(d -> d.getScore() >= avgScore).sorted(positive).collect(Collectors.toList());

        NewHomeworkStudyMaster master = new NewHomeworkStudyMaster();
        master.setId(context.getHomeworkId());
        master.setSubject(context.getHomework().getSubject());
        if (excellents.size() > 0) {
            List<Long> uids = new ArrayList<>();
            for (StudentData data : excellents) {
                uids.add(data.getStudentId());
                if (uids.size() >= 3) {
                    break;
                }
            }
            master.setExcellentList(uids);
        }
        if (calculations.size() > 0) {
            List<Long> uids = new ArrayList<>();
            for (StudentData data : calculations) {
                uids.add(data.getStudentId());
                if (uids.size() >= 5) {
                    break;
                }
            }
            master.setCalculationList(uids);
            processRankReward(context, master, studentHomeworkResultMap);
        }
        if (focuss.size() > 0) {
            List<Long> uids = new ArrayList<>();
            for (StudentData data : focuss) {
                uids.add(data.getStudentId());
                if (uids.size() >= 3) {
                    break;
                }
            }
            master.setFocusList(uids);
        }
        if (positives.size() > 0) {
            List<Long> uids = new ArrayList<>();
            for (StudentData data : positives) {
                uids.add(data.getStudentId());
                if (uids.size() >= 3) {
                    break;
                }
            }
            master.setPositiveList(uids);
        }
        try {
            newHomeworkStudyMasterDao.insert(master);
        } catch (Exception e) {
            if (e.getMessage().contains("E11000")) {
                logger.error("insert study master error ,duplicate homeworkId: {}", context.getHomeworkId());
                return;
            }
            throw e;
        }
    }

    @Data
    private static class StudentData {
        Long studentId;
        Integer score; //分数
        Long duration; //耗时
        Long zdDuration; //中断时间
        Boolean repair; //是否补做
        Integer mentalScore; //口算分数
        Integer mentalDuration; //口算时间
        Date finishAt; //完成作业时间
    }

    private void processRankReward(CheckHomeworkContext context, NewHomeworkStudyMaster studyMaster, Map<Long, NewHomeworkResult> studentHomeworkResultMap){
        NewHomework newHomework = context.getHomework();
        List<NewHomeworkPracticeContent> practices = new ArrayList<>();
        if (newHomework != null) {
            practices = newHomework.getPractices();
        }
        if (CollectionUtils.isNotEmpty(practices)) {
            for (NewHomeworkPracticeContent practiceContent : practices) {
                if (practiceContent.getType().equals(ObjectiveConfigType.MENTAL_ARITHMETIC)) {

                        // 给上榜的前5名同学发积分奖励
                        if (studyMaster != null && SafeConverter.toBoolean(practiceContent.getMentalAward())) {
                            List<Long> studentIds = studyMaster.getCalculationList();
                            if (CollectionUtils.isNotEmpty(studentIds)) {
                                int i = 1;
                                for (Long studentId : studentIds) {
                                    Integer credit = MentalArithmeticCredit.of(i).getCredit();
                                    CreditHistory hs = CreditHistoryBuilderFactory.newBuilder(studentId, CreditType.homework)
                                            .withAmount(credit)
                                            .withComment("获得结果性奖励积分")
                                            .build();
                                    userIntegralService.changeCredit(hs);
                                    NewHomeworkResult nhr = studentHomeworkResultMap.get(studentId);
                                    if(credit != null && nhr != null && nhr.getCredit() != null){
                                        credit += nhr.getCredit();
                                        newHomeworkResultService.saveFinishHomeworkReward(context.getHomework(), studentId, null, null, credit);
                                    }
                                    i++;
                                }
                            }
                        }


                    // 针对有限时的口算训练发榜单消息
                    if (practiceContent.getTimeLimit() != null && practiceContent.getTimeLimit().getTime() != 0) {
                        // 在消息中心发榜单公布通知
                        String title = "口算训练榜单已公布";
                        String content = "口算训练榜单已公布，请查看。";
                        String link = UrlUtils.buildUrlQuery("/view/student/report/mentalrank", MapUtils.m(
                                "homeworkId", context.getHomeworkId(),
                                "log", "message"));

                        List<Long> studentIds = new ArrayList<>();
                        List<StudentDetail> students = context.getStudents();
                        if (CollectionUtils.isNotEmpty(students)) {
                            students.forEach(e -> studentIds.add(e.getId()));
                        }
                        List<AppMessage> appUserMessages = new ArrayList<>(studentIds.size());
                        if (CollectionUtils.isNotEmpty(studentIds)) {
                            for (Long studentId : studentIds) {
                                AppMessage message = new AppMessage();
                                message.setUserId(studentId);
                                message.setMessageType(StudentAppPushType.METAL_ARITHMETIC_CHART_REMIND.getType());
                                message.setTitle(title);
                                message.setContent(content);
                                message.setLinkUrl(link);
                                message.setLinkType(1); // 站内的相对地址
                                appUserMessages.add(message);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(appUserMessages)) {
                            appUserMessages.forEach(message -> messageCommandServiceClient.getMessageCommandService().createAppMessage(message));
                        }
                    }
                    break;
                }
            }
        }
    }


}
