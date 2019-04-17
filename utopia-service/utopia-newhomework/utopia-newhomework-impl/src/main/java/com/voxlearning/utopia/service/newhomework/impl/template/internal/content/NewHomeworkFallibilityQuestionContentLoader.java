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

package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.recom.entity.paks.WeekWrongQuestionPackage;
import com.voxlearning.athena.api.recom.entity.paks.WrongQuestionInfo;
import com.voxlearning.athena.api.recom.entity.wrapper.ClazzWeekWrongPackageWrapper;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.mapper.assign.FallibilityQuestionBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.assign.WeekWrongQuestionBO;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.AthenaHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 类NewHomeworkFallibilityQuestionContentLoader的实现，小学数学、小学英语 高频错题推题接口
 *
 * @author zhangbin
 * @since 2017/1/5 18:13
 */
@Named
public class NewHomeworkFallibilityQuestionContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject
    private AthenaHomeworkLoaderClient athenaHomeworkLoaderClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.FALLIBILITY_QUESTION;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        TeacherDetail teacher = mapper.getTeacher();
        List<ClazzWeekWrongPackageWrapper> clazzWeekWrongPackageWrapperList = new ArrayList<>();
        try {
            clazzWeekWrongPackageWrapperList = athenaHomeworkLoaderClient.getAthenaHomeworkLoader().loadClazzWeekWrongQuestionPackages(mapper.getGroupIds());
        } catch (Exception ex) {
            logger.error("newHomeworkFallibilityQuestionContent call athena error:", ex);
        }

        // 如果测试环境没数据每个班组mock两条
        if (RuntimeMode.isUsingTestData() && CollectionUtils.isEmpty(clazzWeekWrongPackageWrapperList)) {
            clazzWeekWrongPackageWrapperList = new ArrayList<>();
            for (Long groupId : mapper.getGroupIds()) {
                ClazzWeekWrongPackageWrapper mock = new ClazzWeekWrongPackageWrapper();
                mock.setClazzGroupId(groupId);
                List<WeekWrongQuestionPackage> weekWrongQuestionPackages = new ArrayList<>();

                WeekWrongQuestionPackage package1 = new WeekWrongQuestionPackage();
                package1.setId(mapper.getUnitId() + "_" + groupId + "_" + "package1");
                package1.setLowLossRate(0.5);
                package1.setHighLossRate(0.5);
                package1.setTimeSpan("2018-06-04_2018-06-10");
                package1.setQuestionCnt(1);
                package1.setQuestionSeconds(10L);
                List<WrongQuestionInfo> wrongQuestionInfoList = new ArrayList<>();
                WrongQuestionInfo wrongQuestionInfo = new WrongQuestionInfo();
                wrongQuestionInfo.setLossRate(0.5);
                wrongQuestionInfo.setDocId("Q_10309435678820");
                if (mapper.getTeacher().getSubject() == Subject.MATH) {
                    wrongQuestionInfo.setDocId("Q_10209358966778");
                }
                wrongQuestionInfoList.add(wrongQuestionInfo);
                package1.setWrongQuestionInfoList(wrongQuestionInfoList);
                weekWrongQuestionPackages.add(package1);

                WeekWrongQuestionPackage package2 = new WeekWrongQuestionPackage();
                package2.setId(mapper.getUnitId() + "_" + groupId + "_" + "package2");
                package2.setLowLossRate(0.5);
                package2.setHighLossRate(0.5);
                package2.setTimeSpan("2018-06-11_2018-06-17");
                package2.setQuestionCnt(1);
                package2.setQuestionSeconds(10L);
                wrongQuestionInfoList = new ArrayList<>();
                wrongQuestionInfo = new WrongQuestionInfo();
                wrongQuestionInfo.setLossRate(0.5);
                wrongQuestionInfo.setDocId("Q_10310974928694");
                if (mapper.getTeacher().getSubject() == Subject.MATH) {
                    wrongQuestionInfo.setDocId("Q_10209358579595");
                }
                wrongQuestionInfoList.add(wrongQuestionInfo);
                package2.setWrongQuestionInfoList(wrongQuestionInfoList);
                weekWrongQuestionPackages.add(package2);

                mock.setWeekWrongQuestionPackages(weekWrongQuestionPackages);
                clazzWeekWrongPackageWrapperList.add(mock);
            }
        }

        //新题库-题型
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        //试题类型白名单
        List<Integer> contentTypeList = teacher.getSubject() == Subject.ENGLISH ? QuestionConstants.englishExamIncludeContentTypeIds :
                teacher.getSubject() == Subject.MATH ? QuestionConstants.homeworkMathIncludeContentTypeIds : QuestionConstants
                        .examChineseIncludeContentTypeIds;
        //老师使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(),
                teacher.getId(), mapper.getBookId());

        //处理题包
        if (CollectionUtils.isNotEmpty(clazzWeekWrongPackageWrapperList)) {
            processPackageContent(content, clazzWeekWrongPackageWrapperList, mapper.getBookId(), mapper.getUnitId(), contentTypeMap, teacherAssignmentRecord,
                    contentTypeList, mapper.getCurrentPageNum());
        }
        return content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> contentList = loadContent(mapper);
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<WeekWrongQuestionBO> packageList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(contentList)) {
            for (Map<String, Object> contentMapper : contentList) {
                if (StringUtils.equals("package", SafeConverter.toString(contentMapper.get("type")))) {
                    List<FallibilityQuestionBO> packages = (List<FallibilityQuestionBO>) contentMapper.get("packages");
                    if (CollectionUtils.isNotEmpty(packages)) {
                        for (FallibilityQuestionBO fallibilityQuestionBO : packages) {
                            List<WeekWrongQuestionBO> weekWrongQuestionBOList = fallibilityQuestionBO.getWeekWrongQuestionBOList();
                            String timeSpan = fallibilityQuestionBO.getTimeSpan();
                            if (CollectionUtils.isNotEmpty(weekWrongQuestionBOList)) {
                                for (WeekWrongQuestionBO weekWrongQuestionBO : weekWrongQuestionBOList) {
                                    if (packageList.size() < 4) {
                                        weekWrongQuestionBO.setName(timeSpan + "一周错题");
                                        packageList.add(weekWrongQuestionBO);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(packageList)) {
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "packages", packageList
            );
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> content = previewSpecialExam(contentIdList);
        return MiscUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "content", content
        );
    }

    private void processPackageContent(List<Map<String, Object>> content, List<ClazzWeekWrongPackageWrapper> clazzWeekWrongPackageWrapperList,
                                       String bookId, String unitId, Map<Integer, NewContentType> contentTypeMap,
                                       TeacherAssignmentRecord teacherAssignmentRecord, List<Integer> contentTypeList, Integer currentPageNum) {
        if (CollectionUtils.isNotEmpty(clazzWeekWrongPackageWrapperList)) {
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);

            List<FallibilityQuestionBO> fallibilityQuestionBOList = new ArrayList<>();

            //获取所有班级的周次
            Set<String> timeSpanSet = new HashSet<>();
            for (ClazzWeekWrongPackageWrapper wrapper : clazzWeekWrongPackageWrapperList) {
                if (CollectionUtils.isNotEmpty(wrapper.getWeekWrongQuestionPackages())) {
                    for (WeekWrongQuestionPackage weekWrongQuestionPackage : wrapper.getWeekWrongQuestionPackages()) {
                        timeSpanSet.add(weekWrongQuestionPackage.getTimeSpan());
                    }
                }
            }
            //周次降序
            List<String> sortedTimeSpanList = new ArrayList<>(timeSpanSet);
            sortedTimeSpanList.sort(Comparator.reverseOrder());

            if (CollectionUtils.isNotEmpty(sortedTimeSpanList)) {
                //按周分组
                for (int week = (currentPageNum - 1) * 2; week < currentPageNum * 2 && week < sortedTimeSpanList.size(); week++) {
                    FallibilityQuestionBO fallibilityQuestionBO = new FallibilityQuestionBO();
                    if (sortedTimeSpanList.get(week) != null) {
                        String timeSpan = sortedTimeSpanList.get(week).replace("_", "~");
                        Long dayInterval;
                        try {
                            dayInterval = getTimespan(timeSpan.split("~")[1]);
                        } catch (Exception ignored) {
                            return;
                        }
                        if (week == 0) {
                            if (dayInterval != null && dayInterval <= 7) {
                                fallibilityQuestionBO.setTimeSpan(timeSpan + " (本周)");
                            } else {
                                fallibilityQuestionBO.setTimeSpan(timeSpan);
                            }
                        } else if (week == 1) {
                            if (dayInterval != null && dayInterval <= 14) {
                                fallibilityQuestionBO.setTimeSpan(timeSpan + " (上周)");
                            } else {
                                fallibilityQuestionBO.setTimeSpan(timeSpan);
                            }
                        } else {
                            fallibilityQuestionBO.setTimeSpan(timeSpan);
                        }
                    }
                    List<WeekWrongQuestionBO> weekWrongQuestionBOList = new ArrayList<>();
                    //按班级分组并排序
                    Comparator<ClazzWeekWrongPackageWrapper> comparator = Comparator.comparing(ClazzWeekWrongPackageWrapper::getClazzGroupId);
                    List<ClazzWeekWrongPackageWrapper> sortedClazzGroupWrapperList = clazzWeekWrongPackageWrapperList.stream()
                            .sorted(comparator)
                            .collect(Collectors.toList());
                    for (ClazzWeekWrongPackageWrapper clazzWeekWrongPackageWrapper : sortedClazzGroupWrapperList) {
                        WeekWrongQuestionBO weekWrongQuestionBO = new WeekWrongQuestionBO();
                        Long groupId = SafeConverter.toLong(clazzWeekWrongPackageWrapper.getClazzGroupId());
                        weekWrongQuestionBO.setGroupId(groupId);
                        //获取班级名称
                        Map<Long, GroupMapper> groupMap = groupLoaderClient.loadGroups(Collections.singletonList(groupId), false);
                        String clazzName = "";
                        if (groupMap.get(groupId) != null) {
                            Long clazzId = groupMap.get(groupId).getClazzId();
                            Map<Long, Clazz> classMap = raikouSDK.getClazzClient()
                                    .getClazzLoaderClient()
                                    .loadClazzs(Collections.singleton(clazzId))
                                    .stream()
                                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
                            Clazz clazz = classMap.get(clazzId);
                            if (clazz != null) {
                                clazzName = clazz.formalizeClazzName();
                                weekWrongQuestionBO.setGroupName(clazzName);
                            }
                        }

                        String currentTimeSpan = sortedTimeSpanList.get(week);
                        List<WeekWrongQuestionPackage> weekWrongQuestionPackageList = clazzWeekWrongPackageWrapper
                                .getWeekWrongQuestionPackages();
                        if (CollectionUtils.isNotEmpty(weekWrongQuestionPackageList)) {
                            int index = 0;
                            for (WeekWrongQuestionPackage weekPackage : weekWrongQuestionPackageList) {
                                index++;
                                //匹配出该班级下当前周次
                                if (weekPackage.getTimeSpan().equals(currentTimeSpan)) {
                                    WeekWrongQuestionPackage weekWrongQuestionPackage = weekWrongQuestionPackageList.get(--index);
                                    if (weekWrongQuestionPackage != null) {
                                        //获取当前请求页的DocIds
                                        Set<String> allQuestionDocIdSet = new HashSet<>();
                                        if (CollectionUtils.isNotEmpty(weekWrongQuestionPackage.getWrongQuestionInfoList())) {
                                            //当前周
                                            weekWrongQuestionPackage.getWrongQuestionInfoList()
                                                    .stream()
                                                    .filter(e -> e.getDocId() != null)
                                                    .forEach(e -> allQuestionDocIdSet.add(e.getDocId()));
                                        }
                                        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIdSet)
                                                .stream()
                                                .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
                                        Map<String, NewQuestion> docIdQuestionMap = allQuestionMap.values()
                                                .stream()
                                                .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
                                        //总的使用次数
                                        Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader
                                                .loadTotalAssignmentRecordByContentType
                                                        (Subject.MATH, allQuestionMap.keySet(), HomeworkContentType.QUESTION);

                                        weekWrongQuestionBO.setId(weekWrongQuestionPackage.getId() == null ? RandomUtils.randomNumeric(5) :
                                                weekWrongQuestionPackage.getId());
                                        weekWrongQuestionBO.setName(weekWrongQuestionPackage.getName() == null ? clazzName + " 高频错题" :
                                                weekWrongQuestionPackage.getName());
                                        Double lowLossRate = weekWrongQuestionPackage.getLowLossRate();
                                        Double highLossRate = weekWrongQuestionPackage.getHighLossRate();
                                        if (lowLossRate != null && highLossRate != null) {
                                            Integer lowLossRateToInt = (int) Math.round(lowLossRate * 100);
                                            Integer highLossRateToInt = (int) Math.round(highLossRate * 100);
                                            weekWrongQuestionBO.setLossRate(lowLossRateToInt + "%-" + highLossRateToInt + "%");
                                        }

                                        //题包高频错题列表
                                        List<Map<String, Object>> questionMapList = new ArrayList<>();
                                        List<WrongQuestionInfo> wrongQuestionInfoList = weekWrongQuestionPackage.getWrongQuestionInfoList();
                                        if (CollectionUtils.isNotEmpty(wrongQuestionInfoList)) {
                                            for (WrongQuestionInfo wrongQuestionInfo : wrongQuestionInfoList) {
                                                String docId = wrongQuestionInfo.getDocId();
                                                NewQuestion newQuestion = docIdQuestionMap.get(docId);
                                                Double lossRate = wrongQuestionInfo.getLossRate();
                                                Map<String, Integer> lossRateMap = new HashMap<>();
                                                if (lossRate != null) {
                                                    Integer LossRateToInt = (int) Math.round(lossRate * 100);
                                                    lossRateMap.put("lossRate", LossRateToInt);
                                                }
                                                if (newQuestion != null
                                                        && contentTypeList.contains(newQuestion.getContentTypeId())
                                                        && newQuestion.supportOnlineAnswer()
                                                        && !Objects.equals(newQuestion.getNotFitMobile(), 1)) {
                                                    Map<String, Object> question = NewHomeworkContentDecorator.decorateNewQuestion(
                                                            newQuestion, contentTypeMap,
                                                            totalAssignmentRecordMap,
                                                            teacherAssignmentRecord, book);
                                                    question.putAll(lossRateMap);
                                                    questionMapList.add(question);
                                                }
                                            }
                                        }
                                        weekWrongQuestionBO.setQuestionNum(questionMapList.size());
                                        Long seconds = questionMapList
                                                .stream()
                                                .mapToInt(e -> SafeConverter.toInt(e.get("seconds")))
                                                .summaryStatistics()
                                                .getSum();
                                        weekWrongQuestionBO.setSeconds(seconds);
                                        weekWrongQuestionBO.setQuestions(questionMapList);
                                        weekWrongQuestionBOList.add(weekWrongQuestionBO);
                                    }
                                    break;
                                }
                            }
                        }
                        fallibilityQuestionBO.setWeekWrongQuestionBOList(weekWrongQuestionBOList);
                    }
                    fallibilityQuestionBOList.add(fallibilityQuestionBO);
                }
            }
            content.add(MiscUtils.m("type", "package", "totalPages", Math.ceil((double) sortedTimeSpanList.size() / 2), "currentPageNum",
                    currentPageNum, "packages", fallibilityQuestionBOList));
        }
    }

    private static Long getTimespan(String day) {
        try {
            if (StringUtils.isBlank(day)) {
                return null;
            }
            Date day1 = new SimpleDateFormat("yyyy-MM-dd").parse(day);
            Date day2 = new Date();
            return (day2.getTime() - day1.getTime()) / (24 * 60 * 60 * 1000);
        } catch (ParseException ex) {
            return null;
        }
    }
}
