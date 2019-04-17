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

package com.voxlearning.utopia.service.newhomework.impl.template;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendData;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.JztReport;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.NewHomeworkSelfStudyClient;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkSyllableDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.VoiceRecommendLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.DoHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.report.ProcessNewHomeworkAnswerDetailReadReciteWithScoreTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.report.ProcessNewHomeworkAnswerDetailWordRecognitionAndReadingTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.*;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.*;
import static java.util.stream.Collectors.toMap;

abstract public class NewHomeworkReportForParentTemple extends SpringContainerSupport {
    @Inject
    protected NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    protected QuestionLoaderClient questionLoaderClient;
    @Inject
    protected NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    protected NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    protected PracticeLoaderClient practiceLoaderClient;
    @Inject
    protected NewContentLoaderClient newContentLoaderClient;
    @Inject
    protected PictureBookLoaderClient pictureBookLoaderClient;
    @Inject
    protected NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject
    protected TeacherLoaderClient teacherLoaderClient;
    @Inject
    protected NewHomeworkSyllableDao newHomeworkSyllableDao;
    @Inject
    protected StudentLoaderClient studentLoaderClient;
    @Inject
    protected GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject
    protected UserBlacklistServiceClient userBlacklistServiceClient;
    @Inject
    protected NewHomeworkSelfStudyClient newHomeworkSelfStudyClient;
    @Inject
    private TestMethodLoaderClient testMethodLoaderClient;
    @Inject
    private FeatureLoaderClient featureLoaderClient;
    @Inject
    private VoiceRecommendLoaderImpl voiceRecommendLoader;
    @Inject
    private DoHomeworkProcessor doHomeworkProcessor;
    @Inject
    protected CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    protected HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;
    @Inject
    protected SelfStudyHomeworkDao selfStudyHomeworkDao;
    @Inject
    private BasicReviewHomeworkPackageDao basicReviewHomeworkPackageDao;
    @Inject
    protected DubbingLoaderClient dubbingLoaderClient;
    @Inject
    private ProcessNewHomeworkAnswerDetailReadReciteWithScoreTemplate processNewHomeworkAnswerDetailreadReciteWithScoreTemplate;
    @Inject
    private ProcessNewHomeworkAnswerDetailWordRecognitionAndReadingTemplate processNewHomeworkAnswerDetailWordRecognitionAndReadingTemplate;
    @Inject
    private SelfStudyHomeworkResultDao selfStudyHomeworkResultDao;


    abstract public Subject getSubject();


    private String changeScore(int score) {
        if (score == 100) {
            return "A+";
        }
        if (score >= 90) {
            return "A";
        }
        if (score >= 80) {
            return "A-";
        }
        if (score >= 70) {
            return "B+";
        }
        if (score >= 60) {
            return "B";
        }
        if (score >= 40) {
            return "C+";
        }
        if (score >= 20) {
            return "C";
        }
        return "D";
    }


    /**
     * 家长通报告新接口
     */
    public JztReport newDoLoadNewHomeworkDetailForParent(NewHomeworkResult newHomeworkResult, User parent, StudentDetail studentDetail, NewHomework newHomework) {
        try {
            Map<Long, User> userMap = new LinkedHashMap<>();
            List<User> users = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId());
            boolean flag = false;//该学生是否在该班级里面
            for (User u : users) {
                if (Objects.equals(u.getId(), studentDetail.getId())) {
                    flag = true;
                }
                userMap.put(u.getId(), u);
            }
            JztReport jztReport = new JztReport();
            //传送学生的参数是否在班级里面
            //不在的时候兼容处理
            if (!flag) {
                List<User> users1 = studentLoaderClient.loadParentStudents(parent.getId());
                List<Long> childrenIds = new LinkedList<>();
                for (User u : users1) {
                    if (userMap.containsKey(u.getId())) {
                        childrenIds.add(u.getId());
                    }
                }
                //多个孩子在班级里面的时候，随机取一个
                if (childrenIds.size() != 0) {
                    flag = true;
                    int v = (int) (Math.random() * childrenIds.size());
                    if (v == childrenIds.size()) {
                        v--;
                    }
                    Long uid = childrenIds.get(v);
                    studentDetail = studentLoaderClient.loadStudentDetail(uid);
                    newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), uid, true);
                }
            }
            //该家长没有一个孩子在该班级
            if (!flag) {
                jztReport.setError(studentDetail.getId() + " is not belong to this class");
                jztReport.setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
                return jztReport;
            }
            NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(newHomework.getId());
            String bookId;
            String unitId = null;
            if (newHomeworkBook == null || MapUtils.isEmpty(newHomeworkBook.getPractices())) {
                bookId = null;
            } else {
                bookId = newHomeworkBook.processBookId();
                Set<String> unitIds = newHomeworkBook.processUnitIds();
                if (CollectionUtils.isNotEmpty(unitIds)) {
                    unitId = unitIds.iterator().next();
                }
            }
            jztReport.setSid(studentDetail.getId());
            jztReport.setBookId(bookId == null ? "" : bookId);
            Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.findByHomeworkForReport(newHomework);
            //是否完成作业
            jztReport.setFinished(newHomeworkResult != null && newHomeworkResult.isFinished());
            //是否是今天的作业
            jztReport.setToday(DateUtils.dateToString(newHomework.getCreateAt(), DateUtils.FORMAT_SQL_DATE).equals(DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE)));
            //是否检查
            jztReport.setChecked(SafeConverter.toBoolean(newHomework.getChecked()));
            //是否补做
            jztReport.setRepair(newHomeworkResult != null && SafeConverter.toBoolean(newHomeworkResult.getRepair()));
            List<String> allHomeworkProcessIds = newHomeworkResult != null ? newHomeworkResult.findAllHomeworkProcessIds(true) : new LinkedList<>();
            Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), allHomeworkProcessIds);
            Long teacherId = newHomework.getTeacherId();
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (teacher != null) {
                jztReport.getTeacherSummaryPart().setTeacherName(teacher.fetchRealname());
                jztReport.getTeacherSummaryPart().setTeacherPicUrl(teacher.fetchImageUrl());
            }
            //作业简介
            newHomeworkDescription(newHomeworkBook, jztReport.getHomeworkDescription(), newHomework, userMap.values(), newHomeworkResultMap);
            //五个灰度subMainName
            List<String> subFunctionNames = Arrays.asList("correctConfig", "knowledgePointConfigV2", "voiceConfig", "phonicsConfig", "olympiadConfig", "classRoomConfig", "blackRegion", "scoreRegion");
            //处理五个灰度
            Map<String, Boolean> grayMap = handGray(studentDetail, subFunctionNames, false);

            boolean scoreRegionFlag = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            jztReport.setScoreRegionFlag(!scoreRegionFlag);
            //是否是期末复习作业
            boolean isTermReview = false;
            if (Objects.equals(newHomework.getType(), NewHomeworkType.TermReview)) {
                isTermReview = true;
            }
            jztReport.setTermReview(isTermReview);
            String selfStudyHomeworkId = null;
            if (jztReport.isFinished()) {
                //总体表现
                handReportDataPart(newHomework, jztReport, newHomeworkResult, newHomeworkResultMap, newHomeworkProcessResultMap);
                //基础必过
                handBasicReviewPart(newHomework, jztReport);

                handReadReciterWithScore(jztReport, newHomeworkResult, newHomeworkProcessResultMap);
                //错题
                selfStudyHomeworkId = newStatisticWrongInfo(jztReport.getTeacherSummaryPart().getErrorModule(), newHomework, newHomeworkResult);
                //自然拼读
                handNaturalSpelling(jztReport, newHomeworkResult, newHomeworkProcessResultMap, scoreRegionFlag);
                //语音达标
                newHandleVoid(bookId, newHomeworkProcessResultMap, jztReport.getTeacherSummaryPart().getTalkingModule(), newHomeworkResult, newHomework);
                //知识点
                if (StringUtils.isNotBlank(unitId)) {
                    newHandleKnowledge(unitId, grayMap, newHomework, jztReport.getTeacherSummaryPart().getKnowledgePointModule(), parent, studentDetail);
                }
                //语文朗读和背诵
                if (newHomeworkResult != null && newHomeworkResult.getPractices() != null && newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.READ_RECITE)) {
                    newHandleReadRecite(jztReport, newHomework, newHomeworkResult, newHomeworkProcessResultMap);
                }
                // 趣味配音
                if (newHomeworkResult != null && newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.DUBBING)) {
                    handleDubbing(jztReport, newHomework, newHomeworkResult);
                }
                jztReport.setBlackRegion(grayMap.containsKey("blackRegion") && SafeConverter.toBoolean(grayMap.get("blackRegion")));
                jztReport.getReportDataPart().setUnStandardWordNum(jztReport.getTeacherSummaryPart().getTalkingModule().getWeakCount());
                jztReport.getReportDataPart().setWrongNum(jztReport.getTeacherSummaryPart().getErrorModule().getWrongQuestionNum());
            } else if (jztReport.isChecked()) {
                //总体表现
                handReportDataPart(newHomework, jztReport, newHomeworkResult, newHomeworkResultMap, newHomeworkProcessResultMap);
            }
            jztReport.setSubject(newHomework.getSubject());

            // 报告错因导流
            // 修改记录:2018/11/22增加英语和语文的导流。junchen.feng@17zuoye.com。英语增加支持的题型有LISTEN_PRACTICE; 语文支持的题型有CHINESE_READINDG, BASIC_KNOWLEDGE
            // 修改记录:2018/11/30去掉语文和英语哦的导流
            List<ObjectiveConfigType> homeworkObjectiveConfigType = Lists.transform(newHomework.getPractices(), NewHomeworkPracticeContent::getType);
            if (newHomework.getSubject() == Subject.MATH && !Collections.disjoint(JZT_REPORT_PRESCRIPTION_CONFIGTYPE, homeworkObjectiveConfigType)) {
                SelfStudyHomeworkResult studyHomeworkResult = null;
                if (selfStudyHomeworkId != null) {
                    studyHomeworkResult = selfStudyHomeworkResultDao.load(selfStudyHomeworkId);
                }
                if (studyHomeworkResult == null || !studyHomeworkResult.isFinished()) {
                    String homeworkPrescription = homeworkPrescription(newHomeworkResult, newHomeworkProcessResultMap, studentDetail);
                    jztReport.setErrorReasonFlowGuide(JSON.parseObject(homeworkPrescription, Map.class));
                    if (jztReport.getErrorReasonFlowGuide() == null) {
                        jztReport.setErrorReasonFlowGuide(Collections.emptyMap());
                    }
                }
                jztReport.setSelfStudyUrl("/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/student-amend/index.vhtml");
                jztReport.setSelfStudyIndexUrl(UrlUtils.buildUrlQuery("/student/selfstudy/homework/index.api", MapUtils.m("homeworkId", selfStudyHomeworkId)));
            }
            jztReport.setSuccess(true);
            return jztReport;
        } catch (Exception e) {
            JztReport jztReport = new JztReport();
            jztReport.setError(e.getMessage());
            return jztReport;
        }
    }

    private String homeworkPrescription(NewHomeworkResult newHomeworkResult, Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap, StudentDetail studentDetail) {
        newHomeworkProcessResultMap = Maps.filterValues(newHomeworkProcessResultMap, o -> o != null && JZT_REPORT_PRESCRIPTION_CONFIGTYPE.contains(o.getObjectiveConfigType()));
        if (newHomeworkResult == null || MapUtils.isEmpty(newHomeworkProcessResultMap)) {
            return null;
        }
        Map<String, Object> httpParams = new HashMap<>();
        httpParams.put("student_id", newHomeworkResult.getUserId());
        httpParams.put("homework_id", newHomeworkResult.getHomeworkId());
        if (studentDetail.getClazzLevel() != null) {
            httpParams.put("class_level", studentDetail.getClazzLevel().getLevel());
        }
        List<Map<String, Object>> behaviors = Lists.newLinkedList();
        httpParams.put("behavior", behaviors);
        for (NewHomeworkProcessResult processResult : newHomeworkProcessResultMap.values()) {
            Map<String, Object> behaviorMap = Maps.newHashMap();
            behaviorMap.put("question_id", processResult.getQuestionId());
            behaviorMap.put("homework_type", processResult.getObjectiveConfigType() != null ? processResult.getObjectiveConfigType().getValue() : null);
            behaviorMap.put("is_right", processResult.getGrasp());
            if (StringUtils.isNotBlank(processResult.getInterventionHintId())) {
                behaviorMap.put("attempt", JSON.parseArray(processResult.getInterventionStringAnswer(), List.class));
                behaviorMap.put("reattempt", processResult.getUserAnswers());
            } else {
                behaviorMap.put("attempt", processResult.getUserAnswers());
                behaviorMap.put("reattempt", null);
            }
            behaviorMap.put("hint_id", processResult.getInterventionHintId());
            behaviors.add(behaviorMap);
        }

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(RuntimeMode.current().le(Mode.TEST) ? JZT_REPORT_ERRORREASON_FLOW_GUIDE_URL_TEST : JZT_REPORT_ERRORREASON_FLOW_GUIDE_URL)
                .json(httpParams)
                .contentType("application/json").socketTimeout(3 * 1000)
                .execute();

        if (response == null || response.getStatusCode() != 200) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", newHomeworkResult.getUserId(),
                    "mod1", newHomeworkResult.getHomeworkId(),
                    "op", "JZT report prescription failure"
            ));
            logger.error("调用:{}失败, httpParams:{}, response: {}", JZT_REPORT_ERRORREASON_FLOW_GUIDE_URL, httpParams, response != null ? response.getResponseString() : "");
            return null;
        } else {
            return response.getResponseString();
        }
    }


    private void handReadReciterWithScore(JztReport jztReport, NewHomeworkResult newHomeworkResult, Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap) {
        if (!newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.READ_RECITE_WITH_SCORE))
            return;
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        if (newHomeworkResultAnswer.getAppAnswers() != null) {
            List<Map<String, Object>> readList = new LinkedList<>();
            List<Map<String, Object>> reciteList = new LinkedList<>();
            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                if (MapUtils.isEmpty(appAnswer.getAnswers()))
                    continue;
                if (appAnswer.getQuestionBoxType() == null)
                    continue;
                Map<String, Object> basicData = new LinkedHashMap<>();
                double value = new BigDecimal(SafeConverter.toInt(appAnswer.getStandardNum()) * 100).divide(new BigDecimal(appAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                basicData.put("standard", value > NewHomeworkConstants.READ_RECITE_STANDARD);
                basicData.put("questionBoxTypeName", appAnswer.getQuestionBoxType().getName());
                List<String> voices = new LinkedList<>();
                basicData.put("voices", voices);
                for (String pid : appAnswer.getAnswers().values()) {
                    if (!newHomeworkProcessResultMap.containsKey(pid))
                        continue;
                    NewHomeworkProcessResult p = newHomeworkProcessResultMap.get(pid);
                    if (CollectionUtils.isEmpty(p.getOralDetails()))
                        continue;
                    voices.addAll(p.getOralDetails()
                            .stream()
                            .flatMap(Collection::stream)
                            .filter(Objects::nonNull)
                            .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), p.getVoiceEngineType()))
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList()));
                }
                if (appAnswer.getQuestionBoxType() == QuestionBoxType.READ) {
                    readList.add(basicData);
                } else {
                    reciteList.add(basicData);
                }
            }
            jztReport.getReadReciteData().addAll(readList);
            jztReport.getReadReciteData().addAll(reciteList);

        }

    }

    private void handBasicReviewPart(NewHomework newHomework, JztReport jztReport) {
        if (jztReport.isTermReview()) {
            Map<Long, List<BasicReviewHomeworkPackage>> map = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(Collections.singleton(newHomework.getClazzGroupId()));
            if (MapUtils.isNotEmpty(map)) {
                List<BasicReviewHomeworkPackage> basicReviewHomeworkPackages = map.get(newHomework.getClazzGroupId());
                if (CollectionUtils.isNotEmpty(basicReviewHomeworkPackages)) {
                    BasicReviewHomeworkPackage homeworkPackage = basicReviewHomeworkPackages.get(0);
                    if (homeworkPackage.getContentTypes() != null) {
                        for (BasicReviewContentType contentType : homeworkPackage.getContentTypes()) {
                            jztReport.getBasicReviewTabList().add(MapUtils.m("tabName", contentType.getJztTabName(), "packageId", homeworkPackage.getId(), "jztContent", contentType.getJztContent()));
                        }
                    }
                }
            }
        }
    }


    //自然拼读特殊处理
    private void handNaturalSpelling(JztReport jztReport, NewHomeworkResult newHomeworkResult, Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap, boolean scoreRegionFlag) {
        if (newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.NATURAL_SPELLING)) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.NATURAL_SPELLING);
            if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                for (NewHomeworkResultAppAnswer newHomeworkResultAppAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                    PracticeType practiceType = practiceLoaderClient.loadPractice(newHomeworkResultAppAnswer.getPracticeId());
                    if (practiceType == null)
                        continue;
                    List<String> voiceUrls = new LinkedList<>();
                    String scoreInfo;
                    if (practiceType.getNeedRecord()) {
                        double score = SafeConverter.toDouble(newHomeworkResultAppAnswer.getScore());
                        scoreInfo = new BigDecimal(score).setScale(0, BigDecimal.ROUND_HALF_UP).intValue() + "分";
                        if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                            voiceUrls = newHomeworkResultAppAnswer.getAnswers()
                                    .values()
                                    .stream()
                                    .filter(newHomeworkProcessResultMap::containsKey)
                                    .map(newHomeworkProcessResultMap::get)
                                    .map(n -> {
                                        List<String> audios = new LinkedList<>();
                                        if (CollectionUtils.isNotEmpty(n.getOralDetails())) {
                                            for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : n.getOralDetails()) {
                                                if (CollectionUtils.isNotEmpty(oralDetails)) {
                                                    for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                                                        String voiceUrl = oralDetail.getAudio();
                                                        if (StringUtils.isNotBlank(voiceUrl)) {
                                                            VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                                                            voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                                            audios.add(voiceUrl);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        return audios;
                                    })
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList());


                        }
                    } else {
                        if (Objects.equals(newHomeworkResultAppAnswer.getCategoryId(), NatureSpellingType.FUNNY_SPELLING.getCategoryId()) || Objects.equals(newHomeworkResultAppAnswer.getCategoryId(), NatureSpellingType.PRONUNCIATION_CLASSIFICATION.getCategoryId())) {
                            scoreInfo = "已完成";
                        } else {
                            double score = SafeConverter.toDouble(newHomeworkResultAppAnswer.getScore());
                            if (scoreRegionFlag) {
                                scoreInfo = changeScore(new BigDecimal(score).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
                            } else {
                                scoreInfo = new BigDecimal(score).setScale(0, BigDecimal.ROUND_HALF_UP).intValue() + "分";
                            }
                        }
                    }
                    jztReport.getNaturalSpelling().add(MapUtils.m(
                            "scoreInfo", scoreInfo,
                            "voiceUrls", voiceUrls,
                            "categoryId", newHomeworkResultAppAnswer.getCategoryId(),
                            "categoryName", practiceType.getCategoryName()
                    ));
                }
            }

        }
    }

    private void newHandleReadRecite(JztReport jztReport, NewHomework newHomework, NewHomeworkResult newHomeworkResult, Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap) {
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.READ_RECITE);
        Set<String> questionIds = newHomeworkResultAnswer.getAnswers().keySet();
        List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkQuestions(ObjectiveConfigType.READ_RECITE);
        Map<String, NewQuestion> newQuestionMap = doHomeworkProcessor.initReadReciteDate(newHomeworkQuestions, ObjectiveConfigType.READ_RECITE, false);

        for (String qid : questionIds) {
            NewHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(newHomeworkResultAnswer.getAnswers().get(qid));
            NewQuestion newQuestion = newQuestionMap.get(qid);
            if (newQuestion == null || newHomeworkProcessResult == null) {
                continue;
            }
            String correction;
            if (newHomeworkProcessResult.getCorrection() != null) {
                correction = newHomeworkProcessResult.getCorrection().getDescription();
            } else if (newHomeworkProcessResult.getReview() != null) {
                correction = "阅";
            } else {
                correction = "未批改";
            }

            Map<String, Object> readReciteData = MapUtils.m(
                    "fileUrl", CollectionUtils.isNotEmpty(newHomeworkProcessResult.getFiles()) ?
                            newHomeworkProcessResult
                                    .getFiles()
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                    .collect(Collectors.toList()) :
                            Collections.emptyList(),
                    "articleName", newQuestion.getArticleName(),
                    "paragraphCName", newQuestion.getParagraph(),
                    "correction", correction
            );
            jztReport.getReadReciteInformation().add(readReciteData);
        }
    }

    private void handleDubbing(JztReport jztReport,
                               NewHomework newHomework,
                               NewHomeworkResult newHomeworkResult) {
        if (newHomeworkResult.isFinishedOfObjectiveConfigType(ObjectiveConfigType.DUBBING)) {
            List<Map<String, Object>> studentAchievement = new LinkedList<>();

            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING);
            if (newHomeworkPracticeContent != null && CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())) {
                Map<String, String> didToHyidMap = newHomeworkPracticeContent
                        .getApps()
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(NewHomeworkApp::getDubbingId, (NewHomeworkApp o) -> new DubbingSyntheticHistory.ID(newHomework.getId(), newHomeworkResult.getUserId(), o.getDubbingId()).toString()));
                Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(didToHyidMap.values());
                Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(didToHyidMap.keySet());
                Set<String> categoryIds = dubbingMap
                        .values()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(Dubbing::getCategoryId)
                        .collect(Collectors.toSet());
                Map<String, DubbingCategory> dubbingCategoryMap = dubbingLoaderClient.loadDubbingCategoriesByIds(categoryIds);

                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult
                        .getPractices()
                        .get(ObjectiveConfigType.DUBBING);

                Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                        .stream()
                        .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
                for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
                    String dubbingId = app.getDubbingId();
                    Dubbing dubbing = dubbingMap.get(dubbingId);
                    String categoryId = dubbing == null ? "" : dubbing.getCategoryId();
                    DubbingCategory dubbingCategory = dubbingCategoryMap.get(categoryId);
                    if (dubbing == null) {
                        continue;
                    }
                    Map<String, Object> dubbingInfoMap = NewHomeworkContentDecorator.decorateDubbing(dubbing, dubbingCategory, null, null, null, ObjectiveConfigType.DUBBING, dubbingThemeMap);

                    if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                        NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer
                                .getAppAnswers()
                                .get(dubbingId);
                        if (newHomeworkResultAppAnswer != null) {
                            if (newHomeworkResultAppAnswer.getDuration() != null) {
                                int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                                        .intValue();
                                dubbingInfoMap.put("duration", duration);
                            } else {
                                dubbingInfoMap.put("duration", null);
                            }
                            if (StringUtils.isNotBlank(newHomeworkResultAppAnswer.getVideoUrl())) {
                                dubbingInfoMap.put("studentVideoUrl", newHomeworkResultAppAnswer.getVideoUrl());
                                boolean syntheticSuccess = true;
                                if (didToHyidMap.containsKey(dubbingId) && dubbingSyntheticHistoryMap.containsKey(didToHyidMap.get(dubbingId))) {
                                    DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(didToHyidMap.get(dubbingId));
                                    syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(newHomework.getCreateAt()));
                                }
                                dubbingInfoMap.put("syntheticSuccess", syntheticSuccess);
                            }
                        }
                    }
                    studentAchievement.add(dubbingInfoMap);
                }
            }

            // 按照配音用时降序
            Comparator<Map<String, Object>> comparator = (e1, e2) -> Integer.compare(SafeConverter.toInt(e2.get("duration")), SafeConverter.toInt(e1.get("duration")));
            studentAchievement = studentAchievement
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.get("duration") != null)
                    .sorted(comparator)
                    .collect(Collectors.toList());
            // 取用时最长的配音
            if (CollectionUtils.isNotEmpty(studentAchievement)) {
                jztReport.setDubbingInfo(
                        MapUtils.m(
                                "type", ObjectiveConfigType.DUBBING,
                                "typeName", ObjectiveConfigType.DUBBING.getValue(),
                                "content", studentAchievement.get(0),
                                "dubbingId", studentAchievement.get(0).get("dubbingId")
                        ));
            }
        }
    }


    /**
     * 知识点
     */
    private void newHandleKnowledge(String unitId, Map<String, Boolean> grayMap, NewHomework newHomework, JztReport.TeacherSummaryPart.KnowledgePointModule knowledgePointContent, User parent, StudentDetail studentDetail) {
        if (newHomework.getSubject() == Subject.CHINESE) {
            return;
        }
        boolean knowledgePointFlag = grayMap.containsKey("knowledgePointConfigV2") && SafeConverter.toBoolean(grayMap.get("knowledgePointConfigV2"));
        knowledgePointFlag = knowledgePointFlag && (!userBlacklistServiceClient.isInBlackListByParent(parent, studentDetail));
        if (knowledgePointFlag) {
            //英语知识点
            if (newHomework.getSubject() == Subject.ENGLISH) {
                List list;
                try {
                    list = newHomeworkSelfStudyClient.getSelfStudy().loadStudentUnitInfos(studentDetail.getId(), unitId);
                } catch (Exception e) {
                    list = new LinkedList();
                }
                if (CollectionUtils.isNotEmpty(list)) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> knowledgePointName = testMethodLoaderClient.getNameById(list);
                    if (MapUtils.isNotEmpty(knowledgePointName)) {
                        knowledgePointContent.setKnowledgePointNames(knowledgePointName.values());
                    }

                }
            } else {
                //数学知识点
                List<Map<String, List<String>>> maps;
                try {
                    maps = newHomeworkSelfStudyClient.getSelfStudy().queryAfentiMathGoal(unitId, studentDetail.getId());
                } catch (Exception e) {
                    maps = new LinkedList<>();
                }
                List<String> kpIds = new LinkedList<>();
                Set<String> kpfIds = new HashSet<>();
                for (Map<String, List<String>> map : maps) {
                    for (String k : map.keySet()) {
                        kpIds.add(k);
                        if (CollectionUtils.isNotEmpty(map.get(k))) {
                            kpfIds.addAll(map.get(k));
                        }
                    }
                }
                //根据于振的接口获取name
                Map<String, String> knowledgePointName = testMethodLoaderClient.getNameById(kpIds);
                Map<String, KnowledgePointFeature> knowledgePointFeatureMap = featureLoaderClient.loadKnowledgePointFeatureIncludeDisabled(kpfIds);
                for (Map<String, List<String>> map : maps) {
                    for (String k : map.keySet()) {
                        if (knowledgePointName.containsKey(k)) {
                            List<String> strings = map.get(k);
                            if (CollectionUtils.isNotEmpty(strings)) {
                                for (String kf : strings) {
                                    KnowledgePointFeature knowledgePointFeature = knowledgePointFeatureMap.get(kf);
                                    if (knowledgePointFeature != null) {
                                        knowledgePointContent.getKnowledgePointNames().add(SafeConverter.toString(knowledgePointName.get(k)) + "(" + knowledgePointFeature.getName() + ")");
                                    } else {
                                        knowledgePointContent.getKnowledgePointNames().add(SafeConverter.toString(knowledgePointName.get(k)));
                                    }
                                }
                            } else {
                                knowledgePointContent.getKnowledgePointNames().add(SafeConverter.toString(knowledgePointName.get(k)));
                            }
                        }
                    }
                }

            }

        }
        knowledgePointFlag = knowledgePointFlag && knowledgePointContent.getKnowledgePointNames().size() > 0;
        knowledgePointContent.setFlag(knowledgePointFlag);
        knowledgePointContent.setKnowledgePointNum(knowledgePointContent.getKnowledgePointNames().size());
    }


    /**
     * 口语音频
     */
    private void newHandleVoid(String bookId, Map<String, NewHomeworkProcessResult> allNewHomeworkProcessResultMap, JztReport.TeacherSummaryPart.TalkingModule talkingModule, NewHomeworkResult newHomeworkResult, NewHomework newHomework) {

        List<Map<String, Object>> oralBriefing = new LinkedList<>();
        if (newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.BASIC_APP)
                || newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.LS_KNOWLEDGE_REVIEW)) {

            List<String> newHomeworkProcessResultIds = new LinkedList<>();
            newHomeworkProcessResultIds.addAll(newHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(ObjectiveConfigType.BASIC_APP));
            newHomeworkProcessResultIds.addAll(newHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(ObjectiveConfigType.LS_KNOWLEDGE_REVIEW));

            Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultIds.stream()
                    .filter(allNewHomeworkProcessResultMap::containsKey)
                    .collect(Collectors.toMap(Function.identity(), allNewHomeworkProcessResultMap::get));

            Set<String> questionIds = newHomeworkProcessResultMap.values()
                    .stream()
                    .map(NewHomeworkProcessResult::getQuestionId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);

            Map<String, Long> qidToSentenceMap = questionMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                    .collect(Collectors.toMap(NewQuestion::getId, o -> o.getSentenceIds().get(0)));
            //基础练习需要推荐
            Map<Long, VoiceRecommendData> voiceRecommendDataBySentenceIds = voiceRecommendLoader.findVoiceRecommendDataBySentenceIds(qidToSentenceMap.values());
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.BASIC_APP);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> apps = newHomeworkResultAnswer != null ? newHomeworkResultAnswer.getAppAnswers() : new LinkedHashMap<>();
            List<String> nIds = new LinkedList<>();
            boolean flag = true;
            boolean hasVoice = false;
            for (NewHomeworkResultAppAnswer app : apps.values()) {
                PracticeType practiceType = practiceLoaderClient.loadPractice(app.getPracticeId());
                if (practiceType.getNeedRecord()) {
                    List<String> voiceUrls = new LinkedList<>();
                    Set<Long> sentenceIds = new HashSet<>();
                    hasVoice = true;
                    if (app.getAnswers() == null) {
                        continue;
                    }
                    for (String processId : app.getAnswers().values()) {
                        NewHomeworkProcessResult n = newHomeworkProcessResultMap.get(processId);
                        if (n == null)
                            continue;
                        String qid = n.getQuestionId();
                        if (StringUtils.isNotBlank(qid) && qidToSentenceMap.containsKey(qid)) {
                            Long sentenceId = qidToSentenceMap.get(qid);
                            if (sentenceId != null) {
                                sentenceIds.add(sentenceId);
                            }
                        }
                        String voiceUrl = CollectionUtils.isEmpty(n.getOralDetails()) ||
                                CollectionUtils.isEmpty(n.getOralDetails().get(0)) ||
                                StringUtils.isBlank(n.getOralDetails().get(0).get(0).getAudio()) ?
                                null :
                                n.getOralDetails().get(0).get(0).getAudio();
                        VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                        if (StringUtils.isNotBlank(voiceUrl)) {
                            voiceUrls.add(voiceUrl);
                        }
                        if (n.getClientType() != null && (!n.getClientType().equals("pc"))) {
                            flag = false;
                        }
                        if (n.getAppOralScoreLevel() == AppOralScoreLevel.D || n.getAppOralScoreLevel() == AppOralScoreLevel.C) {
                            String s = VoiceEngineTypeUtils.handleAudioUrl(DateUtils.dateToString(newHomework.getCreateAt(), "yyyyMMdd"), voiceUrl, voiceEngineType, n.getHomeworkId(), n.getUserId());
                            if (s != null) {
                                nIds.add(s);
                            }
                        }
                    }
                    List<String> recommendVoiceUrlList = new LinkedList<>();
                    if (CollectionUtils.isNotEmpty(sentenceIds)) {
                        recommendVoiceUrlList = sentenceIds.stream()
                                .filter(voiceRecommendDataBySentenceIds::containsKey)
                                .map(voiceRecommendDataBySentenceIds::get)
                                .filter(Objects::nonNull)
                                .map(VoiceRecommendData::getVoiceUrl)
                                .collect(Collectors.toList());
                    }
                    Map<String, Object> basicAppData = new LinkedHashMap<>();
                    oralBriefing.add(basicAppData);
                    basicAppData.put("voiceUrls", voiceUrls);
                    basicAppData.put("maxScoreList", recommendVoiceUrlList);
                    basicAppData.put("categoryName", practiceType.getCategoryName());
                }
            }

            newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.LS_KNOWLEDGE_REVIEW);
            apps = newHomeworkResultAnswer != null ? newHomeworkResultAnswer.getAppAnswers() : new LinkedHashMap<>();

            for (NewHomeworkResultAppAnswer app : apps.values()) {
                PracticeType practiceType = practiceLoaderClient.loadPractice(app.getPracticeId());
                if (practiceType.getNeedRecord()) {
                    hasVoice = true;
                    for (String newHomeworkProcessResultId : app.getAnswers().values()) {
                        NewHomeworkProcessResult n = newHomeworkProcessResultMap.get(newHomeworkProcessResultId);
                        if (n == null)
                            continue;
                        String voiceUrl = CollectionUtils.isEmpty(n.getOralDetails()) ||
                                CollectionUtils.isEmpty(n.getOralDetails().get(0)) ||
                                StringUtils.isBlank(n.getOralDetails().get(0).get(0).getAudio()) ?
                                null :
                                n.getOralDetails().get(0).get(0).getAudio();
                        VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                        if (n.getClientType() != null && (!n.getClientType().equals("pc"))) {
                            flag = false;
                        }
                        if (n.getAppOralScoreLevel() == AppOralScoreLevel.D || n.getAppOralScoreLevel() == AppOralScoreLevel.C) {
                            String s = VoiceEngineTypeUtils.handleAudioUrl(DateUtils.dateToString(newHomework.getCreateAt(), "yyyyMMdd"), voiceUrl, voiceEngineType, n.getHomeworkId(), n.getUserId());
                            if (s != null) {
                                nIds.add(s);
                            }
                        }
                    }
                }
            }

            talkingModule.setFlag(flag);
            talkingModule.setHasVoice(hasVoice);
            Map<String, NewHomeworkSyllable> newHomeworkSyllableMap = newHomeworkSyllableDao.loads(nIds);
            PronunciationRecord pronunciationRecord = new PronunciationRecord(newHomeworkSyllableMap.values());//处理得到数据
            talkingModule.setWords(pronunciationRecord.getWords());
            talkingModule.setWeakCount(pronunciationRecord.getCount());
            talkingModule.setLines(pronunciationRecord.getLines());
            talkingModule.setUnitAndSentenceList(pronunciationRecord.getUnitAndSentenceList());
        }
        talkingModule.setOralList(oralBriefing);
        talkingModule.setBookId(bookId);
        talkingModule.setVoiceFlag(true);
    }


    private String newStatisticWrongInfo(JztReport.TeacherSummaryPart.ErrorModule errorModule, NewHomework newHomework, NewHomeworkResult newHomeworkResult) {
        int wrongQuestionNum = 0;
        boolean showCorrect = false;
        boolean isConfigTrue = true;
        try {
            String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "GET_CORRECT_FUN_IS_SHOW");
            isConfigTrue = ConversionUtils.toBool(config);
        } catch (IllegalArgumentException e) {
            logger.info("CommonConfigLoaderClient GET_CORRECT_FUN_IS_SHOW : e{}", e.getMessage());
        }
        List<ObjectiveConfigType> types = newHomework.getPractices().stream().map(NewHomeworkPracticeContent::getType).collect(Collectors.toList());

        if (CollectionUtils.containsAny(GenerateSelfStudyHomeworkConfigTypes, types)
                && NewHomeworkConstants.showWrongQuestionInfo(newHomework.getCreateAt(), RuntimeMode.getCurrentStage())
                && NeedSelfStudyHomeworkSubjects.contains(newHomework.getSubject())
                && isConfigTrue
                ) {
            showCorrect = true;
        }
        String selfStudyId = null;
        //showCorrect 是否有订正
        if (showCorrect) {
            String s = newHomework.getId() + "_" + newHomeworkResult.getUserId();
            HomeworkSelfStudyRef homeworkSelfStudyRef = homeworkSelfStudyRefDao.load(s);
            if (homeworkSelfStudyRef != null) {
                selfStudyId = homeworkSelfStudyRef.getSelfStudyId();
                SelfStudyHomework selfStudyHomework = selfStudyHomeworkDao.load(selfStudyId);
                if (selfStudyHomework != null) {
                    wrongQuestionNum = selfStudyHomework.findSelfStudyNewHomeworkQuestionIds().size();
                }
            } else {
                showCorrect = false;
            }

        }
        errorModule.setFlag(showCorrect);
        errorModule.setWrongQuestionNum(wrongQuestionNum);
        errorModule.setSemesterWrongNum(0);
        return selfStudyId;
    }


    /**
     * 总体表现
     */
    private void handReportDataPart(NewHomework newHomework, JztReport jztReport,
                                    NewHomeworkResult newHomeworkResult,
                                    Map<String, NewHomeworkResult> newHomeworkResultMap,
                                    Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap
    ) {
        JztReport.ReportDataPart reportDataPart = jztReport.getReportDataPart();
        boolean unScoreRegionFlag = !jztReport.isScoreRegionFlag();

        Set<ObjectiveConfigType> objectiveConfigTypes = newHomework.getPractices().stream()
                .map(NewHomeworkPracticeContent::getType)
                .collect(Collectors.toSet());
        //是否都是不显示分数的type
        boolean allSubject = true;
        for (ObjectiveConfigType type : objectiveConfigTypes) {
            if (NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(type))
                continue;
            if (!type.isSubjective()) {
                allSubject = false;
            }
        }
        if (!jztReport.isFinished()) {
            if (jztReport.isChecked() && !allSubject) {
                int maxScore = 0;
                int sumScore = 0;
                int num = 0;
                for (NewHomeworkResult n : newHomeworkResultMap.values()) {
                    if (n.isFinished()) {
                        int personScore = SafeConverter.toInt(n.processScore());
                        if (maxScore < personScore) {
                            maxScore = personScore;
                        }
                        sumScore += personScore;
                        num++;
                    }
                }
                reportDataPart.setHighestScore(maxScore);
                if (num > 0) {
                    reportDataPart.setAverScore(new BigDecimal(sumScore).divide(new BigDecimal(num), 0, BigDecimal.ROUND_UP).intValue());
                }
                if (unScoreRegionFlag) {
                    reportDataPart.setHighestScoreStr(changeScore(maxScore));
                    reportDataPart.setAverScoreStr(changeScore(reportDataPart.getAverScore()));
                }
            }
            return;
        }
        boolean onlyDubbing = false;
        if (objectiveConfigTypes.size() == 1 && objectiveConfigTypes.contains(ObjectiveConfigType.DUBBING)) {
            onlyDubbing = true;
        }
        reportDataPart.setOnlyDubbing(onlyDubbing);

        if (allSubject) {
            reportDataPart.setScore(-1);
            if (objectiveConfigTypes.size() == 1 && objectiveConfigTypes.contains(ObjectiveConfigType.READ_RECITE_WITH_SCORE)) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
                String scoreStr = processNewHomeworkAnswerDetailreadReciteWithScoreTemplate.processStudentPartTypeScore(newHomework, newHomeworkResultAnswer, ObjectiveConfigType.READ_RECITE_WITH_SCORE);
                reportDataPart.setScoreStr(scoreStr);
            } else {
                reportDataPart.setScoreStr("已完成");
            }
        } else {
            //计算平均分
            //最高分
            int score = SafeConverter.toInt(newHomeworkResult.processScore());
            int maxScore = score;
            int sumScore = 0;
            int num = 0;
            for (NewHomeworkResult n : newHomeworkResultMap.values()) {
                if (n.isFinished()) {
                    int personScore = SafeConverter.toInt(n.processScore());
                    if (maxScore < personScore) {
                        maxScore = personScore;
                    }
                    sumScore += personScore;
                    num++;
                }
            }
            reportDataPart.setScore(score);
            reportDataPart.setHighestScore(maxScore);
            if (num > 0) {
                reportDataPart.setAverScore(new BigDecimal(sumScore).divide(new BigDecimal(num), 0, BigDecimal.ROUND_UP).intValue());
            }
            if (unScoreRegionFlag) {
                reportDataPart.setScoreStr(changeScore(score));
                reportDataPart.setHighestScoreStr(changeScore(maxScore));
                reportDataPart.setAverScoreStr(changeScore(reportDataPart.getAverScore()));
            }
        }
        List<Map<String, Object>> scoreDetail = new LinkedList<>();
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();

        for (ObjectiveConfigType o : objectiveConfigTypes) {
            Map<String, Object> map = new HashMap<>();
            String result;
            if (o == ObjectiveConfigType.READ_RECITE || o == ObjectiveConfigType.DUBBING) {
                result = "已完成";
            } else if (o == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
                result = processNewHomeworkAnswerDetailreadReciteWithScoreTemplate.processStudentPartTypeScore(newHomework, newHomeworkResultAnswer, ObjectiveConfigType.READ_RECITE_WITH_SCORE);
            } else if (o == ObjectiveConfigType.WORD_RECOGNITION_AND_READING) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.WORD_RECOGNITION_AND_READING);
                result = processNewHomeworkAnswerDetailWordRecognitionAndReadingTemplate.processStudentPartTypeScore(newHomework, newHomeworkResultAnswer, ObjectiveConfigType.WORD_RECOGNITION_AND_READING);
            } else if (o == ObjectiveConfigType.NEW_READ_RECITE) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.NEW_READ_RECITE);
                boolean allUnCorrected = true;
                List<String> correctionInfo = new LinkedList<>();
                List<String> voiceUrls = new LinkedList<>();
                for (NewHomeworkResultAppAnswer newHomeworkResultAppAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                    if (newHomeworkResultAppAnswer.getCorrection() != null) {
                        correctionInfo.add(newHomeworkResultAppAnswer.getCorrection().getDescription());
                        allUnCorrected = false;
                    } else if (newHomeworkResultAppAnswer.getReview() != null) {
                        correctionInfo.add("阅");
                        allUnCorrected = false;
                    } else {
                        correctionInfo.add("未批改");
                    }
                    if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                        for (String pid : newHomeworkResultAppAnswer.getAnswers().values()) {
                            if (newHomeworkProcessResultMap.containsKey(pid)) {
                                NewHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(pid);
                                if (newHomeworkProcessResult != null && CollectionUtils.isNotEmpty(newHomeworkProcessResult.getFiles())) {
                                    voiceUrls.addAll(newHomeworkProcessResult
                                            .getFiles()
                                            .stream()
                                            .flatMap(Collection::stream)
                                            .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                            .collect(Collectors.toList()));
                                }
                            }

                        }
                    }
                }
                map.put("voiceUrls", voiceUrls);
                if (allUnCorrected) {
                    result = "未批改";
                } else {
                    result = StringUtils.join(correctionInfo.toArray(), ",");
                }
            } else if (o == ObjectiveConfigType.OCR_MENTAL_ARITHMETIC) {
                result="已完成";
            } else {
                Integer score1 = SafeConverter.toInt(practices.get(o).processScore(o));
                result = unScoreRegionFlag ? changeScore(score1) : score1 + "分";
            }
            map.put("type", o);
            map.put("name", o.getValue());
            map.put("score", result);
            scoreDetail.add(map);
        }
        reportDataPart.setScoreDetail(scoreDetail);
    }


    /**
     * 对应作业简介
     */

    private void newHomeworkDescription(NewHomeworkBook newHomeworkBook, JztReport.HomeworkDescription homeworkDescription, NewHomework newHomework, Collection<User> userList, Map<String, NewHomeworkResult> newHomeworkResultMap) {

        Date endTime = newHomework.getEndTime();
        Long duration = newHomework.getDuration();
        List<Map<String, Object>> bookInfo = handlerBookInfo(newHomeworkBook);
        List<Map<String, Object>> newHomeworkInfo = handlerNewHomework(newHomework);
        long completeNum = newHomeworkResultMap
                .values()
                .stream()
                .filter(NewHomeworkResult::isFinished).count();
        homeworkDescription.setEndTime(DateUtils.dateToString(endTime, "yyyy-MM-dd HH:mm"));
        homeworkDescription.setTotalUser(userList != null ? userList.size() : 0);
        homeworkDescription.setCompleteNum(completeNum);
        int finishTime = duration != null ? new BigDecimal(duration).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue() : 0;
        homeworkDescription.setPlanFinishTime(finishTime + "分钟");
        homeworkDescription.setHomeworkContent(handleHomeworkContent(bookInfo));
        homeworkDescription.setPracticeList(handlePracticeList(newHomeworkInfo, newHomework));
    }


    /**
     * 构建作业简介-作业内容信息
     */
    abstract protected Set<String> handleHomeworkContent(List<Map<String, Object>> bookInfo);

    /**
     * 作业练习要求
     */
    abstract protected List<String> handlePracticeList(List<Map<String, Object>> newHomeworkInfo, NewHomework newHomework);

    /**
     * 作业内容的信息进行处理
     * 数学：知识点。英语：统计类型。语文：各大类题数
     */
    abstract protected List<Map<String, Object>> handlerNewHomework(NewHomework newHomework);

    /**
     * 获得作业的题的课文信息
     */
    abstract protected List<Map<String, Object>> handlerBookInfo(NewHomeworkBook newHomeworkBook);


    //多个灰度绑定一次处理
    private Map<String, Boolean> handGray(StudentDetail student,
                                          List<String> subFunctionNames, boolean withSchoolLevel) {
        if (CollectionUtils.isEmpty(subFunctionNames)) {
            return Collections.emptyMap();
        }
        Map<String, Boolean> result = new HashMap<>();
        Map<String, String> grayExpresseMap = new HashMap<>();
        for (String subFunctionName : subFunctionNames) {
            String grayExpress = StringUtils.join("jzt", "_", subFunctionName, "_", UserType.STUDENT.getType(), "_");
            if (withSchoolLevel) {
                if (student.isPrimaryStudent()) {  // 小学
                    grayExpress = StringUtils.join(grayExpress, "1", "_");
                } else if (student.isJuniorStudent()) {  // 中学
                    grayExpress = StringUtils.join(grayExpress, "2", "_");
                } else if (student.isInfantStudent()) {  // 学前
                    grayExpress = StringUtils.join(grayExpress, "5", "_");
                }
            }
            if (student.getStudentSchoolRegionCode() == null) {
                grayExpress = StringUtils.join(grayExpress, "000000", "_");
            } else {
                grayExpress = StringUtils.join(grayExpress, student.getStudentSchoolRegionCode(), "_");
            }
            if (student.getClazz() == null) {
                grayExpress = StringUtils.join(grayExpress, "000000", "_");
            } else {
                grayExpress = StringUtils.join(grayExpress, student.getClazz().getSchoolId(), "_");
            }
            grayExpress = StringUtils.join(grayExpress, student.getId(), "_", DateUtils.dateToString(new Date(), "yyyyMMdd"));
            result.put(subFunctionName, false);
            grayExpresseMap.put(subFunctionName, grayExpress);
        }
        List<GlobalTag> configs = grayFunctionManagerClient.loadGrayFunctionConfigList();

        for (GlobalTag globalTags : configs) {

            // FIXME 防止灰度里面正则写错把系统搞死
            try {
                for (String subFunctionName : grayExpresseMap.keySet()) {
                    if (!result.get(subFunctionName)) {
                        if (grayExpresseMap.get(subFunctionName).matches(globalTags.getTagValue())) {
                            result.put(subFunctionName, true);
                        }
                    }
                }
            } catch (Exception e) {
                // do nothing here
            }
        }

        return result;
    }

}
