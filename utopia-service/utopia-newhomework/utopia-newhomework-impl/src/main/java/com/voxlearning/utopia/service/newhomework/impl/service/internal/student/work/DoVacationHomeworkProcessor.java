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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.content.consumer.WordStockLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NaturalSpellingSentence;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.newhomework.impl.service.VacationHomeworkServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation.FinishVacationHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/12/7.
 */
@Named
public class DoVacationHomeworkProcessor extends NewHomeworkSpringBean {

    @Inject private RaikouSDK raikouSDK;

    @Inject
    private VacationHomeworkServiceImpl vacationHomeworkService;
    @Inject
    private FinishVacationHomeworkProcessor finishVacationHomeworkProcessor;
    @Inject
    private DoHomeworkProcessor doHomeworkProcessor;
    @Inject
    private WordStockLoaderClient wordStockLoaderClient;

    /**
     * 学生开始作业首页
     * 此方法待优化
     *
     * @param homeworkId
     * @param studentId
     * @return
     */
    public Map<String, Object> index(String homeworkId, Long studentId) {
        if (StringUtils.isBlank(homeworkId) || null == studentId) {
            return Collections.emptyMap();
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            String[] keys = homeworkId.split("-");
            if (keys.length == 4) {
                vacationHomework = vacationHomeworkService.generateVacationHomework(keys[0], SafeConverter.toInt(keys[1]), SafeConverter.toInt(keys[2]), SafeConverter.toLong(keys[3]));
            }
        }
        if (vacationHomework == null) {
            logger.error("generate vacation homework, homeworkId {}, studentId {}", homeworkId, studentId);
            return Collections.emptyMap();
        }
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(vacationHomework.getPackageId());
        if (vacationHomeworkPackage == null) {
            return Collections.emptyMap();
        }

        // 加个校验，packageId维度的缓存里面没有这个作业id则清除缓存
        String packageId = vacationHomeworkPackage.getId();
        List<VacationHomework.Location> locationList = vacationHomeworkDao.loadVacationHomeworkByPackageIds(Collections.singleton(packageId)).get(packageId);
        if (CollectionUtils.isEmpty(locationList) || locationList.stream().noneMatch(location -> StringUtils.equals(location.getId(), homeworkId))) {
            vacationHomeworkDao.getCache().delete(VacationHomework.ck_packageId(packageId));
        }

        Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceMap = vacationHomework.findPracticeContents();
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> englishPracticeMap = new LinkedHashMap<>();
        for (ObjectiveConfigType objectiveConfigType : ObjectiveConfigType.getSubjectTypes(vacationHomework.getSubject())) {
            if (practiceMap.containsKey(objectiveConfigType)) {
                englishPracticeMap.put(objectiveConfigType, practiceMap.get(objectiveConfigType));
            }
        }
        practiceMap = englishPracticeMap;

        Map<String, Object> result = new HashMap<>();
        result.put("homeworkId", vacationHomework.getId());
        result.put("weekRank", vacationHomework.getWeekRank());
        result.put("dayRank", vacationHomework.getDayRank());
        result.put("homeworkType", vacationHomework.getNewHomeworkType());
        result.put("practiceCount", vacationHomework.findPracticeContents().size());
        result.put("homeworkName", "假期作业");
        result.put("unitName", "");
        Date currentDate = new Date();
        result.put("terminated", currentDate.after(vacationHomeworkPackage.getEndTime()));
        if (!currentDate.after(vacationHomeworkPackage.getEndTime())) {
            result.put("days", DateUtils.dayDiff(vacationHomeworkPackage.getEndTime(), currentDate));
        }
        result.put("remark", vacationHomeworkPackage.getRemark());
        result.put("subject", vacationHomeworkPackage.getSubject());

        boolean isCurrentDayFinished = false;
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> doPractices = new LinkedHashMap<>();
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        if (vacationHomeworkResult != null && vacationHomeworkResult.getPractices() != null) {
            doPractices = vacationHomeworkResult.getPractices();
            if (vacationHomeworkResult.isFinished()) {
                // 为了判断一个很蠢的弹窗加的属性，前端没有弹过窗（前端缓存）&&后端是当天完成的作业，则前端弹窗否则不弹
                isCurrentDayFinished = DateUtils.isSameDay(new Date(), vacationHomeworkResult.getFinishAt());
                result.put("integral", NewHomeworkConstants.FINISH_VACATION_HOMEWORK_INTEGRAL_REWARD);
                result.put("energy", NewHomeworkConstants.FINISH_VACATION_HOMEWORK_ENERGY_REWARD); //能量
                result.put("credit", NewHomeworkConstants.FINISH_VACATION_HOMEWORK_CREDIT_REWARD); //学分
                //每周第一天奖励部件，其他奖励碎片
                if (vacationHomeworkResult.getDayRank() == 1 || vacationHomeworkResult.getDayRank() % 4 == 1) {
                    result.put("assembly", 1); //部件
                } else {
                    result.put("debris", 1); //碎片
                }
                result.put("parentIntegral", 3); //家长分享的学豆
                result.put("finishAt", vacationHomeworkResult.getFinishAt());
                result.put("parentRewardDate", DateUtils.dateToString(DateUtils.calculateDateDay(vacationHomeworkResult.getFinishAt(), 3), "MM月dd日"));
                User user = userLoaderClient.loadUser(vacationHomework.getTeacherId());
                result.put("teacherName", user != null ? user.fetchRealname() + "老师" : "老师");
                Integer score = vacationHomeworkResult.processScore();
                int commentScore;
                if (score == null) {
                    commentScore = 100;//score为null的时候说明只有主观作业
                } else {
                    commentScore = score;
                }
                if (commentScore >= 90 && commentScore <= 100) {
                    result.put("praise", "干得漂亮！");
                    result.put("star", 3);
                } else if (commentScore >= 60 && commentScore <= 89) {
                    result.put("praise", "还不错哦！");
                    result.put("star", 2);
                } else {
                    result.put("praise", "不太理想哦！");
                    result.put("star", 1);
                }
                result.put("showIntegral", true);
                result.put("comment", vacationHomeworkResult.getComment());
                result.put("audioComment", vacationHomeworkResult.getAudioComment());
                result.put("rewardIntegral", vacationHomeworkResult.getRewardIntegral());

                String[] prefixStr = vacationHomework.getId().split("-");
                StringBuilder prefix = new StringBuilder();
                if (prefixStr.length == 4) {
                    for (int i = 0; i < 2; i++) {
                        prefix.append(prefixStr[i]).append("-");
                    }
                    prefix.append(prefixStr[2]);
                }
                String key = "FVH:" + prefix;
                CacheObject<List<Long>> cacheObject = HomeworkCache.getHomeworkCache().get(key);
                List<Long> finishedStudentIds = cacheObject.getValue();

                Long clazzGroupId = vacationHomework.getClazzGroupId();
                List<Long> allStudentIds = studentLoaderClient.loadGroupStudentIds(clazzGroupId);
                List<Long> sids = new ArrayList<>(allStudentIds.size());
                if (CollectionUtils.isNotEmpty(finishedStudentIds)) {
                    for (Long sid : finishedStudentIds) {
                        if (allStudentIds.contains(sid)) {
                            sids.add(sid);
                        }
                    }
                    sids.remove(studentId);
                    Collections.reverse(sids);
                }
                Map<Long, Student> studentsMap = studentLoaderClient.loadStudents(sids);
                List<String> finishedStudents = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(sids)) {
                    for (Long sid : sids) {
                        String studentName = "";
                        Student student = studentsMap.get(sid);
                        if (student != null) {
                            studentName = student.fetchRealname();
                        }
                        finishedStudents.add(studentName);
                    }
                }
                result.put("finishedStudents", finishedStudents);
            }
        }

        int undoPracticesCount = 0;
        boolean needFinish = false;
        /*
         * 总进度=已完成题目总数/作业题目总数
         * 基础训练每个应用算一道题
         * 绘本的话一个绘本算一个进度单位
         * 主观作业，一个题算一个单位
         */
        int totalQuestionCount = 0;
        int doTotalQuestionCount = 0;
        List<Map<String, Object>> practiceInfos = new ArrayList<>();
        for (ObjectiveConfigType objectiveConfigType : practiceMap.keySet()) {
            NewHomeworkResultAnswer vacationHomeworkResultAnswer = doPractices.get(objectiveConfigType);
            NewHomeworkPracticeContent vacationHomeworkPracticeContent = practiceMap.get(objectiveConfigType);
            if (ObjectiveConfigType.BASIC_APP == objectiveConfigType || ObjectiveConfigType.NATURAL_SPELLING == objectiveConfigType) {
                List<NewHomeworkApp> apps = vacationHomeworkPracticeContent.getApps();
                List<String> doHomeworkUrls = new ArrayList<>();
                List<String> finishedUrls = new ArrayList<>();
                List<String> unFinishedUrls = new ArrayList<>();
                int questionCount = 0;
                apps = apps.stream().sorted((c1, c2) -> {
                    Integer r1 = practiceLoaderClient.loadPractice(SafeConverter.toLong(c1.getPracticeId())).getCategoryRank();
                    Integer r2 = practiceLoaderClient.loadPractice(SafeConverter.toLong(c2.getPracticeId())).getCategoryRank();
                    return Integer.compare(r1, r2);
                }).collect(Collectors.toList());
                Map<String, NewHomeworkResultAppAnswer> answerMap = vacationHomeworkResult != null
                        && vacationHomeworkResult.getPractices() != null
                        && vacationHomeworkResult.getPractices().get(objectiveConfigType) != null
                        ? vacationHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers() : Collections.EMPTY_MAP;
                List<Map<String, Object>> appStatus = new ArrayList<>();
                for (NewHomeworkApp newHomeworkApp : apps) {
                    questionCount += newHomeworkApp.getQuestions().size();
                    String key = StringUtils.join(Arrays.asList(newHomeworkApp.getCategoryId(), newHomeworkApp.getLessonId()), "-");
                    if (answerMap.keySet().contains(key)) {
                        finishedUrls.add(UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "lessonId", newHomeworkApp.getLessonId(), "categoryId", newHomeworkApp.getCategoryId(), "practiceId", newHomeworkApp.getPracticeId())));
                    } else {
                        unFinishedUrls.add(UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "lessonId", newHomeworkApp.getLessonId(), "categoryId", newHomeworkApp.getCategoryId(), "practiceId", newHomeworkApp.getPracticeId())));
                    }
                    PracticeType practiceType = practiceLoaderClient.loadPractice(SafeConverter.toLong(newHomeworkApp.getPracticeId()));
                    String categoryName = practiceType != null ? practiceType.getCategoryName() : "";
                    String appKey = newHomeworkApp.getCategoryId() + "-" + newHomeworkApp.getLessonId();
                    NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = answerMap.get(appKey);
                    boolean finished = newHomeworkResultAppAnswer != null && newHomeworkResultAppAnswer.isFinished();
                    appStatus.add(MapUtils.m("appName", categoryName, "questionCount", newHomeworkApp.getQuestions().size(), "finished", finished));
                }
                doHomeworkUrls.addAll(finishedUrls);
                doHomeworkUrls.addAll(unFinishedUrls);
                int appCount = vacationHomeworkPracticeContent.getApps().size();
                int doAppCount = vacationHomeworkResultAnswer != null ? vacationHomeworkResultAnswer.getAppAnswers().size() : 0;
                Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrls", doHomeworkUrls,
                        "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "doCount", doAppCount,
                        "practiceCount", appCount,
                        "questionCount", questionCount,
                        "finished", vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getFinishAt() != null,
                        "appStatus", appStatus);
                practiceInfos.add(practiceInfo);
                totalQuestionCount += appCount;
                doTotalQuestionCount += doAppCount;
                if (vacationHomeworkResultAnswer == null || doAppCount < apps.size()) {
                    undoPracticesCount++;
                }

                if (doAppCount == apps.size() && vacationHomeworkResultAnswer.getFinishAt() == null) {
                    needFinish = true;
                }
            } else if (ObjectiveConfigType.READING == objectiveConfigType) {
                List<NewHomeworkApp> apps = vacationHomeworkPracticeContent.getApps();
                List<String> picBookIds = new ArrayList<>();
                int questionCount = 0;
                for (NewHomeworkApp newHomeworkApp : apps) {
                    questionCount += newHomeworkApp.getQuestions().size();
                    picBookIds.add(newHomeworkApp.getPictureBookId());
                }
                int readingCount = vacationHomeworkPracticeContent.getApps().size();
                int doReadingCount = vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getAppAnswers() != null ? vacationHomeworkResultAnswer.getAppAnswers().size() : 0;

                Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "pictureBookIds", StringUtils.join(picBookIds, ","))),
                        "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "doCount", doReadingCount,
                        "practiceCount", readingCount,
                        "questionCount", questionCount,
                        "finished", vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getFinishAt() != null);

                practiceInfos.add(practiceInfo);
                totalQuestionCount += readingCount;
                doTotalQuestionCount += doReadingCount;
                if (vacationHomeworkResultAnswer == null || doReadingCount < apps.size()) {
                    undoPracticesCount++;
                }
            } else if (ObjectiveConfigType.LEVEL_READINGS == objectiveConfigType) {
                List<NewHomeworkApp> apps = vacationHomeworkPracticeContent.getApps();
                NewHomeworkResultAnswer newHomeworkResultAnswer = doPractices.get(objectiveConfigType);
                int questionCount = 0;
                for (NewHomeworkApp newHomeworkApp : apps) {
                    if (CollectionUtils.isNotEmpty(newHomeworkApp.getQuestions())) {
                        questionCount += newHomeworkApp.getQuestions().size();
                    }
                    if (CollectionUtils.isNotEmpty(newHomeworkApp.getOralQuestions())) {
                        questionCount += newHomeworkApp.getOralQuestions().size();
                    }
                }
                int readingCount = vacationHomeworkPracticeContent.getApps().size();
                int doReadingCount = 0;

                if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                    for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                        if (appAnswer.isFinished()) {
                            doReadingCount++;
                        }
                    }
                }

                Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "doCount", doReadingCount,
                        "practiceCount", readingCount,
                        "questionCount", questionCount,
                        "finished", vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getFinishAt() != null);

                practiceInfos.add(practiceInfo);
                totalQuestionCount += readingCount;
                doTotalQuestionCount += doReadingCount;
                if (vacationHomeworkResultAnswer == null || doReadingCount < apps.size()) {
                    undoPracticesCount++;
                }
                if (doReadingCount == apps.size() && newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() == null) {
                    needFinish = true;
                }
            } else if (ObjectiveConfigType.DUBBING == objectiveConfigType || ObjectiveConfigType.DUBBING_WITH_SCORE == objectiveConfigType) {
                List<NewHomeworkApp> apps = vacationHomeworkPracticeContent.getApps();
                int questionCount = 0;
                List<String> dubbingIds = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(apps)) {
                    for (NewHomeworkApp newHomeworkApp : apps) {
                        questionCount += newHomeworkApp.getQuestions().size();
                        dubbingIds.add(newHomeworkApp.getDubbingId());
                    }
                }
                int appCount = CollectionUtils.isEmpty(apps) ? 0 : apps.size();
                int doAppCount = 0;
                if (vacationHomeworkResultAnswer != null && MapUtils.isNotEmpty(vacationHomeworkResultAnswer.getAppAnswers())) {
                    for (NewHomeworkResultAppAnswer appAnswer : vacationHomeworkResultAnswer.getAppAnswers().values()) {
                        if (appAnswer.isFinished()) {
                            doAppCount++;
                        }
                    }
                }
                totalQuestionCount += questionCount;
                doTotalQuestionCount += doAppCount;
                if (vacationHomeworkResultAnswer == null || doAppCount < appCount) {
                    undoPracticesCount++;
                }

                Map<String, Object> practiceInfo = MiscUtils.m(
                        "objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "dubbingIds", StringUtils.join(dubbingIds, ","))),
                        "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "doCount", doAppCount,
                        "practiceCount", appCount,
                        "questionCount", questionCount,
                        "finished", vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getFinishAt() != null);
                practiceInfos.add(practiceInfo);
            } else if (ObjectiveConfigType.KEY_POINTS == objectiveConfigType || ObjectiveConfigType.NEW_READ_RECITE == objectiveConfigType || ObjectiveConfigType.READ_RECITE_WITH_SCORE == objectiveConfigType) {
                List<NewHomeworkApp> apps = vacationHomeworkPracticeContent.getApps();
                int questionCount = 0;
                for (NewHomeworkApp newHomeworkApp : apps) {
                    questionCount += newHomeworkApp.getQuestions().size();
                    totalQuestionCount += newHomeworkApp.getQuestions().size();
                }
                int videoCount = vacationHomeworkPracticeContent.getApps().size();
                int doQuestionCount = 0;
                int doVideoCount = 0;
                if (vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getAppAnswers() != null) {
                    for (NewHomeworkResultAppAnswer appAnswer : vacationHomeworkResultAnswer.getAppAnswers().values()) {
                        doQuestionCount += appAnswer.getAnswers().size();
                        doTotalQuestionCount += appAnswer.getAnswers().size();
                        if (appAnswer.isFinished()) {
                            doVideoCount++;
                        }
                    }
                }
                Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "doCount", doVideoCount,
                        "practiceCount", videoCount,
                        "questionCount", questionCount,
                        "finished", vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getFinishAt() != null);
                practiceInfos.add(practiceInfo);
                if (vacationHomeworkResultAnswer == null || doQuestionCount < questionCount) {
                    undoPracticesCount++;
                }
            } else {
                int questionCount = vacationHomeworkPracticeContent.getQuestions().size();
                int doQuestionCount = vacationHomeworkResultAnswer != null ? vacationHomeworkResultAnswer.getAnswers().size() : 0;

                Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                        "doCount", doQuestionCount,
                        "questionCount", questionCount,
                        "finished", vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getFinishAt() != null);
                if (ObjectiveConfigType.MENTAL_ARITHMETIC == objectiveConfigType) {
                    practiceInfo.put("timeLimit", vacationHomeworkPracticeContent.getTimeLimit() != null ? vacationHomeworkPracticeContent.getTimeLimit().getTime() : 0);
                    practiceInfo.put("mentalAward", SafeConverter.toBoolean(vacationHomeworkPracticeContent.getMentalAward()));
                }
                practiceInfos.add(practiceInfo);
                totalQuestionCount += questionCount;
                doTotalQuestionCount += doQuestionCount;
                if (vacationHomeworkResultAnswer == null || doQuestionCount < questionCount) {
                    undoPracticesCount++;
                }

                // 所有题都已做完，但缺少finishAt
                if (vacationHomeworkResultAnswer != null && vacationHomeworkResultAnswer.getFinishAt() == null
                        && questionCount == doQuestionCount) {
                    needFinish = true;
                }
            }

        }
        boolean finished = vacationHomeworkResult != null && vacationHomeworkResult.isFinished();
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        result.put("finished", finished);
        result.put("isCurrentDayFinished", isCurrentDayFinished);
        result.put("practices", practiceInfos);
        result.put("undoPracticesCount", undoPracticesCount);
        result.put("finishingRate", new BigDecimal(doTotalQuestionCount * 100).divide(new BigDecimal(totalQuestionCount), 0, BigDecimal.ROUND_HALF_UP).intValue());
        result.put("isInPaymentBlackListRegion", studentDetail.isInPaymentBlackListRegion());
        result.put("fairylandClosed", studentExtAttribute != null && (studentExtAttribute.fairylandClosed() || studentExtAttribute.vapClosed()));
        result.put("showAfentiGuide", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "VacationHW", "AfentiGuide"));
        result.put("openAppUrl", UrlUtils.buildUrlQuery("/app/redirect/jump.vpage",
                MiscUtils.m("appKey", Subject.ENGLISH == vacationHomeworkPackage.getSubject() ? OrderProductServiceType.AfentiExam : OrderProductServiceType.AfentiMath,
                        "platform", "STUDENT_APP",
                        "refer", 330002,
                        "productType", "APPS")));
        result.put("useVenus", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "UseVenus"));
        result.put("newProcess", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "NewIndexUrl"));
//        result.put("openAppUrl", UrlUtils.buildUrlQuery("/resources/apps/hwh5/nianboss/v100/index.html",
//                MiscUtils.m("refer", 330002)));
//        result.put("openAppUrl", UrlUtils.buildUrlQuery("/app/redirect/openurl.vpage",
//                MiscUtils.m("refer", 330002, "fwdUrl", "/resources/apps/hwh5/nianboss/v100/index.html")));

        if ((undoPracticesCount == 0 && !finished) || needFinish) {
            finishVacationHomework(vacationHomework, vacationHomeworkResult, studentId);
        }
        return result;
    }

    /**
     * 根据作业Id和作业类型取题
     * categoryId和lessonId为BasicApp专用属性
     */
    public Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType,
                                                     Integer categoryId, String lessonId,
                                                     String videoId, String questionBoxId) {

        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }
        if (((ObjectiveConfigType.BASIC_APP.equals(objectiveConfigType) || ObjectiveConfigType.NATURAL_SPELLING.equals(objectiveConfigType))
                && ((categoryId == null || categoryId == 0 || StringUtils.isBlank(lessonId))))
                || (ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType) && StringUtils.isBlank(videoId))
                || ((ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType) || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)) && StringUtils.isBlank(questionBoxId))) {
            return Collections.emptyMap();
        }

        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (null == vacationHomework) {
            return Collections.emptyMap();
        }
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(vacationHomework.getPackageId());
        if (vacationHomeworkPackage == null) return Collections.emptyMap();

        List<NewHomeworkQuestion> vacationHomeworkQuestions;
        if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.BASIC_APP.name())
                || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.NATURAL_SPELLING.name())) {
            vacationHomeworkQuestions = vacationHomework.findNewHomeworkQuestions(objectiveConfigType, lessonId, categoryId);
        } else if (ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType)) {
            vacationHomeworkQuestions = vacationHomework.findNewHomeworkKeyPointQuestions(objectiveConfigType, videoId);
        } else if (ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType) || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)) {
            vacationHomeworkQuestions = vacationHomework.findNewHomeworkReadReciteQuestions(objectiveConfigType, questionBoxId);
        } else if (ObjectiveConfigType.DUBBING.equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(objectiveConfigType)) {
            vacationHomeworkQuestions = vacationHomework.findNewHomeworkDubbingQuestions(objectiveConfigType, videoId);
        } else if (ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)) {
            vacationHomeworkQuestions = new ArrayList<>(vacationHomework.findNewHomeworkQuestions(objectiveConfigType, videoId));
            vacationHomeworkQuestions.addAll(vacationHomework.findNewHomeworkOralQuestions(objectiveConfigType, videoId));
        } else {
            vacationHomeworkQuestions = vacationHomework.findNewHomeworkQuestions(objectiveConfigType);
        }

        if (CollectionUtils.isEmpty(vacationHomeworkQuestions)) {
            return Collections.emptyMap();
        }

        Map<String, NewQuestion> mapNewQuestions = doHomeworkProcessor.initReadReciteDate(vacationHomeworkQuestions, objectiveConfigType, objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE || objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        List<String> allQuestionIds = vacationHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
        Map<String, Object> questionMap = new HashMap<>();
        Map<String, Object> extraInfo = new HashMap<>();
        Set<String> eids = new LinkedHashSet<>();
        Map<String, Map<String, Object>> examUnitMap = new LinkedHashMap<>();

        // 重点段落
        Map<String, Boolean> keyPointParagraphMap = new HashMap<>();
        // 重点字词及拼音对应的句子
        Map<String, List<ChineseSentence>> qidKeyPointSentencesMap = new HashMap<>();
        // 重点字词id对应的句子
        Map<Long, ChineseSentence> wordIdSentenceMap = new HashMap<>();

        if (objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
            keyPointParagraphMap = doHomeworkProcessor.initReadReciteKeyPointParagraph(allQuestionIds);
            doHomeworkProcessor.initReadReciteKeyPointWord(
                    vacationHomeworkQuestions,
                    objectiveConfigType,
                    mapNewQuestions,
                    qidKeyPointSentencesMap,
                    wordIdSentenceMap
            );
        }

        int normalTime = 0;
        Map<String, List<List<Integer>>> extra = new HashMap<>();
        for (NewHomeworkQuestion question : vacationHomeworkQuestions) {
            String qid = question.getQuestionId();
            normalTime += question.getSeconds();
            NewQuestion newQuestion = mapNewQuestions.get(qid);
            List<EmbedBook> embedBooks = new ArrayList<>();
            if (newQuestion != null) {
                embedBooks = newQuestion.getBooksNew();
            }
            EmbedBook embedBook;
            if (CollectionUtils.isNotEmpty(embedBooks)) {
                embedBook = embedBooks.get(0);
                if (embedBook != null) {
                    examUnitMap.put(qid, MiscUtils.m(
                            "bookId", vacationHomeworkPackage.getBookId(),
                            "unitId", embedBook.getUnitId(),
                            "lessonId", embedBook.getLessonId(),
                            "unitGroupId", null,
                            "sectionId", embedBook.getSectionId()
                    ));
                }
            }

            if (objectiveConfigType == ObjectiveConfigType.READ_RECITE
                    || objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE) {
                extra.put(question.getQuestionId(), question.getAnswerWay());
                NewQuestion q = mapNewQuestions.get(question.getQuestionId());
                extraInfo.put(question.getQuestionId(), MiscUtils.m(
                        "articleName", q.getArticleName(),
                        "paragraphCName", q.getParagraph()
                ));
            } else if (objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
                extra.put(question.getQuestionId(), question.getAnswerWay());
                if (newQuestion != null) {
                    List<Map<String, Object>> keyPointWords = new ArrayList<>();
                    List<ChineseSentence> keyWordSentences = qidKeyPointSentencesMap.get(question.getQuestionId());
                    keyWordSentences.forEach(e -> keyPointWords.add(
                            MapUtils.m(
                                    "sentence", wordIdSentenceMap.get(e.getId()) == null ? "" : wordIdSentenceMap.get(e.getId()).getContent(),
                                    "word", e.getContent(),
                                    "phonetic", e.getContentPinyinMark())));
                    extraInfo.put(question.getQuestionId(), MiscUtils.m(
                            "articleName", newQuestion.getArticleName(),
                            "paragraph", newQuestion.getParagraph(),
                            "keyPointParagraph", keyPointParagraphMap.getOrDefault(newQuestion.getId(), false),
                            "keyPointWords", keyPointWords
                    ));
                }
            }
            eids.add(qid);
        }
        if (objectiveConfigType == ObjectiveConfigType.READ_RECITE
                || objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE
                || objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
            questionMap.put("extra", extra);
            questionMap.put("extraInfo", extraInfo);
        }

        if (objectiveConfigType == ObjectiveConfigType.DUBBING || objectiveConfigType == ObjectiveConfigType.DUBBING_WITH_SCORE) {
            Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(videoId);
            List<String> questionIds = vacationHomeworkQuestions.stream()
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toList());
            Map<String, NewQuestion> dubbingQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            if (dubbing != null && MapUtils.isNotEmpty(dubbingQuestionMap)) {
                List<Long> wordStockIds = dubbing.getKeyWords()
                        .stream()
                        .filter(dubbingKeyWord -> dubbingKeyWord.getWordStockId() != null)
                        .map(Dubbing.DubbingKeyWord::getWordStockId)
                        .collect(Collectors.toList());
                Map<Long, WordStock> wordStockMap = wordStockLoaderClient.loadWordStocks(wordStockIds);
                Map<String, Dubbing.DubbingKeyWord> dubbingKeyWordMap = dubbing.getKeyWords()
                        .stream()
                        .filter(dubbingKeyWord -> dubbingKeyWord.getEnglishWord() != null)
                        .collect(Collectors.toMap(Dubbing.DubbingKeyWord::getEnglishWord, Function.identity(), (k1, k2) -> k2));
                Set<String> englishWords = new HashSet<>(dubbingKeyWordMap.keySet());
                List<Map<String, Object>> sentenceList = new ArrayList<>();
                dubbingQuestionMap.values()
                        .forEach(question -> {
                            NewQuestionOralDictOptions options = null;
                            NewQuestionsSubContents subContents = question.getContent().getSubContents().get(0);
                            if (subContents != null
                                    && subContents.getOralDict() != null
                                    && CollectionUtils.isNotEmpty(subContents.getOralDict().getOptions())) {
                                options = subContents.getOralDict().getOptions().get(0);
                            }
                            if (options != null) {
                                Map<String, Object> map = new HashMap<>();
                                List<String> englishWordList = new ArrayList<>();

                                Iterator<String> iterator = englishWords.iterator();
                                while (iterator.hasNext()) {
                                    String word = iterator.next();
                                    boolean isInclude = ContainsStr(options.getText(), word);
                                    if (isInclude) {
                                        englishWordList.add(word);
                                        iterator.remove();
                                    }
                                }
                                map.put("sentenceChineseContent", options.getCnText());
                                map.put("sentenceEnglishContent", options.getText());
                                map.put("sentenceVideoStart", options.getVoiceStart());
                                map.put("sentenceVideoEnd", options.getVoiceEnd());
                                map.put("questionId", question.getId());
                                List<Map<String, Object>> keyWordList = new ArrayList<>();
                                if (CollectionUtils.isNotEmpty(englishWordList)) {
                                    englishWordList.forEach(e -> {
                                        Dubbing.DubbingKeyWord keyWord = dubbingKeyWordMap.get(e);
                                        if (keyWord != null) {
                                            Map<String, Object> wordMap = new HashMap<>();
                                            wordMap.put("dubbing_key_word_chinese", keyWord.getChineseWord());
                                            wordMap.put("dubbing_key_word_english", keyWord.getEnglishWord());
                                            String audioUrl = null;
                                            if (MapUtils.isNotEmpty(wordStockMap)) {
                                                WordStock wordStock = wordStockMap.get(keyWord.getWordStockId());
                                                if (wordStock != null) {
                                                    if (SafeConverter.toBoolean(keyWord.getAudioIsUs(), true)) {
                                                        audioUrl = wordStock.getAudioUS();
                                                    } else {
                                                        audioUrl = wordStock.getAudioUK();
                                                    }
                                                }
                                            }
                                            if (audioUrl != null) {
                                                wordMap.put("dubbing_key_word_audio_url", audioUrl);
                                                keyWordList.add(wordMap);
                                            }
                                        }
                                    });
                                }
                                map.put("keyWordList", keyWordList);
                                sentenceList.add(map);
                            }
                        });
                //判断该配音是否是欢快歌曲类型   欢快歌曲类型ID:DC_10300000140166
                DubbingCategory dubbingCategory = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbing.getCategoryId())).get(dubbing.getCategoryId());
                questionMap.put("isSong", dubbingCategory != null && Objects.equals("DC_10300000140166", dubbingCategory.getParentId()));
                questionMap.put("dubbingId", videoId);
                questionMap.put("dubbingName", dubbing.getVideoName());
                questionMap.put("videoUrl", dubbing.getVideoUrl());
                questionMap.put("backgroundMusicUrl", dubbing.getBackgroundMusic());
                questionMap.put("coverImgUrl", dubbing.getCoverUrl());
                questionMap.put("sentenceList", sentenceList);
            }
        }

        if (objectiveConfigType == ObjectiveConfigType.LEVEL_READINGS) {
            List<NewHomeworkQuestion> examQuestions = vacationHomework.findNewHomeworkQuestions(objectiveConfigType, videoId);
            List<NewHomeworkQuestion> oralQuestions = vacationHomework.findNewHomeworkOralQuestions(objectiveConfigType, videoId);
            questionMap.put("examQuestionIds", examQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
            questionMap.put("oralQuestionIds", oralQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
        }
        questionMap.put("examUnitMap", examUnitMap);
        questionMap.put("normalTime", normalTime);
        questionMap.put("eids", eids);
        return questionMap;
    }

    public Map<String, Object> questionAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId,
                                              Integer categoryId, String lessonId,
                                              String videoId, String questionBoxId) {
        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }
        if (((objectiveConfigType == ObjectiveConfigType.BASIC_APP || objectiveConfigType == ObjectiveConfigType.NATURAL_SPELLING)
                && (categoryId == null || categoryId == 0 || StringUtils.isBlank(lessonId)))
                || ((ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType) || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)) && StringUtils.isBlank(questionBoxId))) {
            return Collections.emptyMap();
        }
        Map<String, Object> questionAnswerMap = new HashMap<>();
        if (StringUtils.isNotBlank(homeworkId)) {
            VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
            if (vacationHomework == null || vacationHomework.isDisabledTrue()) {
                return Collections.emptyMap();
            }
            VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(vacationHomework.getId());
            if (vacationHomeworkResult == null) {
                return Collections.emptyMap();
            }

            Collection<String> resultIds;
            if (objectiveConfigType == ObjectiveConfigType.BASIC_APP || objectiveConfigType == ObjectiveConfigType.NATURAL_SPELLING) {
                resultIds = vacationHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(String.valueOf(categoryId), lessonId, objectiveConfigType);
            } else if (objectiveConfigType == ObjectiveConfigType.KEY_POINTS) {
                resultIds = vacationHomeworkResult.findHomeworkProcessIdsForKeyPointsByVideoId(videoId);
            } else if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.READING.name())) {
                // 根据configType类型，复用videoId
                resultIds = vacationHomeworkResult.findHomeworkProcessIdsForReading(videoId, objectiveConfigType);
            } else if (objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE) {
                resultIds = vacationHomeworkResult.findHomeworkProcessIdsForReadReciteByQuestionBoxId(questionBoxId);
            } else if (objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
                resultIds = vacationHomeworkResult.findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(questionBoxId, objectiveConfigType);
            } else if (objectiveConfigType == ObjectiveConfigType.LEVEL_READINGS) {
                resultIds = vacationHomeworkResult.findHomeworkProcessIdsForReading(videoId, objectiveConfigType);
            } else {
                resultIds = vacationHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(objectiveConfigType);
            }

            Collection<VacationHomeworkProcessResult> newHomeworkProcessResults = vacationHomeworkProcessResultDao.loads(resultIds).values();
            for (VacationHomeworkProcessResult vpr : newHomeworkProcessResults) {
                List<List<String>> oralAudios = new ArrayList<>();
                List<List<List<NaturalSpellingSentence>>> sentences = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(vpr.getOralDetails())) {
                    List<List<BaseHomeworkProcessResult.OralDetail>> oralDetailList = vpr.getOralDetails();
                    for (List<BaseHomeworkProcessResult.OralDetail> list1 : oralDetailList) {
                        List<String> audios = new ArrayList<>();
                        List<List<NaturalSpellingSentence>> sentenceList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(list1)) {
                            for (BaseHomeworkProcessResult.OralDetail oralDetail : list1) {
                                audios.add(oralDetail.getAudio());
                                sentenceList.add(oralDetail.getSentences());
                            }
                        }
                        if (CollectionUtils.isNotEmpty(audios)) {
                            oralAudios.add(audios);
                        }
                        if (CollectionUtils.isNotEmpty(sentenceList)) {
                            sentences.add(sentenceList);
                        }
                    }
                }

                Map<String, Object> value = MiscUtils.m(
                        "oralAudios", oralAudios,
                        "files", vpr.getFiles(),
                        "subMaster", vpr.getSubGrasp(),
                        "master", vpr.getGrasp(),
                        "userAnswers", vpr.getUserAnswers(),
                        "fullScore", vpr.getStandardScore(),
                        "score", vpr.getScore(),
                        "oralScoreLevel", vpr.getAppOralScoreLevel()
                );
                if (ObjectiveConfigType.READ_RECITE_WITH_SCORE == objectiveConfigType) {
                    List<List<NaturalSpellingSentence.Word>> wordList = new ArrayList<>();
                    List<NaturalSpellingSentence> spellingSentences = sentences.get(0).get(0);
                    if (CollectionUtils.isNotEmpty(spellingSentences)) {
                        for (NaturalSpellingSentence naturalSpellingSentence : spellingSentences) {
                            List<NaturalSpellingSentence.Word> words = naturalSpellingSentence.getWords();
                            if (CollectionUtils.isNotEmpty(words)) {
                                wordList.add(words);
                            }
                        }
                    }
                    value.putAll(MapUtils.m(
                            "standardScore", vpr.getActualScore(),
                            "words", wordList
                    ));
                }
                Map<String, String> additions = vpr.getAdditions();
                if (additions != null && additions.containsKey("hwTrajectory")) {
                    value.put("hwTrajectory", JSON.parseArray(additions.get("hwTrajectory"), List.class));
                }
                questionAnswerMap.put(vpr.getQuestionId(), value);
            }

            // 趣味配音这个接口不用查process，直接返回配音的相关信息
            if (ObjectiveConfigType.DUBBING == objectiveConfigType || objectiveConfigType == ObjectiveConfigType.DUBBING_WITH_SCORE) {
                Map<ObjectiveConfigType, NewHomeworkResultAnswer> newHomeworkResultAnswerMap = vacationHomeworkResult.getPractices();
                if (MapUtils.isEmpty(newHomeworkResultAnswerMap)
                        || (!newHomeworkResultAnswerMap.containsKey(ObjectiveConfigType.DUBBING)
                        && !newHomeworkResultAnswerMap.containsKey(ObjectiveConfigType.DUBBING_WITH_SCORE))) {
                    return Collections.emptyMap();
                }
                NewHomeworkResultAnswer answer;
                if (ObjectiveConfigType.DUBBING == objectiveConfigType) {
                    answer = newHomeworkResultAnswerMap.get(ObjectiveConfigType.DUBBING);
                } else {
                    answer = newHomeworkResultAnswerMap.get(ObjectiveConfigType.DUBBING_WITH_SCORE);
                }

                if (MapUtils.isNotEmpty(answer.getAppAnswers()) && answer.getAppAnswers().containsKey(videoId)) {
                    NewHomeworkResultAppAnswer appAnswer = answer.getAppAnswers().get(videoId);
                    Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(videoId);
                    if (dubbing != null && MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                        Map<String, Double> questionScoreMap = new HashMap<>();
                        newHomeworkProcessResults.forEach(q -> questionScoreMap.put(q.getQuestionId(), q.getActualScore()));

                        List<NewHomeworkQuestion> newHomeworkQuestions = vacationHomework.findNewHomeworkDubbingQuestions(objectiveConfigType, videoId);
                        List<String> questionIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
                        Map<String, NewQuestion> dubbingQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                        List<Map<String, Object>> sentenceList = new ArrayList<>();
                        if (MapUtils.isNotEmpty(dubbingQuestionMap)) {
                            dubbingQuestionMap.values()
                                    .forEach(question -> {
                                        NewQuestionOralDictOptions options = null;
                                        NewQuestionsSubContents subContents = question.getContent().getSubContents().get(0);
                                        if (subContents != null && subContents.getOralDict() != null && CollectionUtils.isNotEmpty(subContents.getOralDict().getOptions())) {
                                            options = subContents.getOralDict().getOptions().get(0);
                                        }
                                        if (options != null) {
                                            sentenceList.add(MapUtils.m(
                                                    "sentence_chinese_content", options.getCnText(),
                                                    "sentence_english_content", options.getText(),
                                                    "sentence_video_start", options.getVoiceStart(),
                                                    "sentence_video_end", options.getVoiceEnd(),
                                                    "question_id", question.getId(),
                                                    "question_score", questionScoreMap.get(question.getId()) != null ? questionScoreMap.get(question.getId()) : 0
                                            ));
                                        }
                                    });
                        }
                        return MapUtils.m(
                                "dubbingId", videoId,
                                "dubbingName", dubbing.getVideoName(),
                                "coverImgUrl", dubbing.getCoverUrl(),
                                "dubbingVideoUrl", appAnswer.getVideoUrl(),
                                "sentenceCount", appAnswer.getAnswers().size(),
                                "sentenceList", sentenceList,
                                "skipUploadVideo", appAnswer.getSkipUploadVideo()
                        );
                    }
                }
                return Collections.emptyMap();
            }
        }

        return questionAnswerMap;
    }

    private void finishVacationHomework(VacationHomework vacationHomework, VacationHomeworkResult vacationHomeworkResult, Long studentId) {
        if (vacationHomeworkResult != null && !vacationHomeworkResult.getPractices().isEmpty()) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", studentId,
                    "mod1", vacationHomework.getId(),
                    "op", "repair vacation homework result"
            ));
            AlpsThreadPool.getInstance().submit(() -> {
                // 按分组读取班级信息
                Long groupId = vacationHomework.getClazzGroupId();
                Group group = raikouSDK.getClazzClient().getGroupLoaderClient()._loadGroup(groupId).firstOrNull();
                Long clazzId = groupId == null ? null : group.getClazzId();
                ObjectiveConfigType objectiveConfigType = null;
                for (ObjectiveConfigType type : vacationHomeworkResult.getPractices().keySet()) {
                    NewHomeworkResultAnswer ra = vacationHomeworkResult.getPractices().get(type);
                    if (!ra.isFinished()) {
                        objectiveConfigType = type;
                        break;
                    }
                }
                // 所有类型都已完成，用最后一个类型来修复数据
                if (objectiveConfigType == null) {
                    for (ObjectiveConfigType type : vacationHomeworkResult.getPractices().keySet()) {
                        objectiveConfigType = type;
                    }
                }
                if (ObjectiveConfigType.BASIC_APP == objectiveConfigType) {
                    NewHomeworkResultAnswer newHomeworkResultAnswer = vacationHomeworkResult.getPractices().get(objectiveConfigType);
                    if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                        for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                            if (appAnswer.getFinishAt() == null && appAnswer.getAnswers() != null) {
                                Collection<String> processIds = appAnswer.getAnswers().values();
                                Integer categoryId = appAnswer.getCategoryId() != null ? appAnswer.getCategoryId() : 0;
                                String lessonId = appAnswer.getLessonId();
                                String key = StringUtils.join(Arrays.asList(categoryId, lessonId), "-");
                                Double score = 0d;
                                Long duration = 0L;
                                if (CollectionUtils.isNotEmpty(processIds)) {
                                    // 布置的题目和做过的题一致，将剩下的属性补全
                                    Map<String, VacationHomeworkProcessResult> processResultMap = vacationHomeworkProcessResultDao.loads(processIds);
                                    for (VacationHomeworkProcessResult vpr : processResultMap.values()) {
                                        score += vpr.getScore();
                                        duration += vpr.getDuration();
                                    }
                                    Double avgScore = score;
                                    Long practiceId = appAnswer.getPracticeId();
                                    PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
                                    //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
                                    if (practiceType.getNeedRecord()) {
                                        avgScore = new BigDecimal(score).divide(new BigDecimal(processResultMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                    }
                                    vacationHomeworkResultDao.finishHomeworkBasicAppPractice(vacationHomework.toLocation(), objectiveConfigType, key, avgScore, duration);
                                } else {
                                    score = 100D;
                                    duration = NewHomeworkUtils.processDuration(0L);
                                    vacationHomeworkResultDao.finishHomeworkBasicAppPractice(vacationHomework.toLocation(), objectiveConfigType, key, score, duration);
                                }
                            }
                        }
                    }
                } else if (ObjectiveConfigType.READ_RECITE_WITH_SCORE == objectiveConfigType) {
                    NewHomeworkResultAnswer newHomeworkResultAnswer = vacationHomeworkResult.getPractices().get(objectiveConfigType);
                    if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                        for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                            String questionBoxId = appAnswer.getQuestionBoxId();
                            List<String> processIds = vacationHomeworkResult.findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(questionBoxId, objectiveConfigType);
                            Map<String, VacationHomeworkProcessResult> processResultMap = vacationHomeworkProcessResultDao.loads(processIds);
                            Long duration = 0L;
                            Double score = 0D;
                            Integer standardNum = 0;
                            for (VacationHomeworkProcessResult processResult : processResultMap.values()) {
                                duration += processResult.getDuration();
                                score += processResult.getScore();
                                if (processResult.getGrasp() != null && Boolean.TRUE.equals(processResult.getGrasp())) {
                                    ++standardNum;
                                }
                            }
                            Integer appQuestionNum = 0;
                            List<NewHomeworkQuestion> newHomeworkQuestions = vacationHomework.findNewHomeworkReadReciteQuestions(objectiveConfigType, questionBoxId);
                            if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                                appQuestionNum = newHomeworkQuestions.size();
                            }
                            score = new BigDecimal(standardNum).divide(new BigDecimal(appQuestionNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
                            vacationHomeworkResultDao.finishHomeworkReadReciteWithScore(
                                    vacationHomework.toLocation(),
                                    studentId,
                                    objectiveConfigType,
                                    questionBoxId,
                                    score,
                                    duration,
                                    standardNum,
                                    appQuestionNum
                            );
                        }
                    }
                }
                FinishVacationHomeworkContext context = new FinishVacationHomeworkContext();
                context.setUserId(studentId);
                context.setUser(studentLoaderClient.loadStudent(studentId));
                context.setClazz(raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));
                context.setClazzGroupId(groupId);
                context.setVacationHomeworkId(vacationHomework.getId());
                context.setVacationHomework(vacationHomework);
                context.setNewHomeworkType(vacationHomework.getNewHomeworkType());
                context.setObjectiveConfigType(objectiveConfigType);
                context.setClientType("pc");
                context.setClientName("pc");
                context.setSupplementaryData(true);
                finishVacationHomeworkProcessor.process(context);
            });
        }
    }

    /**
     * 当前句子是否包含某个单词
     */
    public static boolean ContainsStr(String s1, String s2) {
        String s1Lower = s1.toLowerCase();
        String s2Lower = s2.toLowerCase();
        if (s1Lower.indexOf(s2Lower) >= 0) {
            return true;
        } else {
            return false;
        }
    }
}
