package com.voxlearning.utopia.service.newhomework.impl.dao.vacation;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2016/11/25
 */
@Named
@CacheBean(type = VacationHomeworkResult.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class VacationHomeworkResultDao extends StaticMongoShardPersistence<VacationHomeworkResult, String> {

    @Override
    protected void calculateCacheDimensions(VacationHomeworkResult document, Collection<String> dimensions) {
        dimensions.add(VacationHomeworkResult.ck_id(document.getId()));
    }

    /**
     * 初始化作业中间结果表
     *
     * @param location  假期作业location
     * @param studentId 学生id
     */
    public void initVacationHomeworkResult(VacationHomework.Location location, Long studentId) {
        if (location == null || studentId == null) {
            return;
        }

        VacationHomeworkResult.ID id = new VacationHomeworkResult.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), studentId);
        VacationHomeworkResult homeworkResult = load(id.toString());
        if (homeworkResult != null) {
            return;
        }
        Date d = new Date();
        homeworkResult = new VacationHomeworkResult();
        homeworkResult.setWeekRank(location.getWeekRank());
        homeworkResult.setDayRank(location.getDayRank());
        homeworkResult.setPackageId(location.getPackageId());
        homeworkResult.setHomeworkId(location.getId());
        homeworkResult.setSubject(location.getSubject());
        homeworkResult.setActionId(location.getActionId());
        homeworkResult.setClazzGroupId(location.getClazzGroupId());
        homeworkResult.setUserId(studentId);
        homeworkResult.setUserStartAt(d);
        VacationHomeworkResult modified = insertIfAbsent(id.toString(), homeworkResult);
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(VacationHomeworkResult.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
    }

    /**
     * 做作业的写入操作
     *
     * @param location  假期作业location
     * @param type      作业类型
     * @param qid       题id
     * @param processId 做题明细id
     * @return boolean
     */
    public Boolean doHomework(VacationHomework.Location location,
                              ObjectiveConfigType type,
                              String qid,
                              String processId) {

        VacationHomework.ID id = new VacationHomework.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), location.getStudentId());
        VacationHomeworkResult vacationHomeworkResult = load(id.toString());
        if (vacationHomeworkResult == null) {
            return false;
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = vacationHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer answer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, String> answerMap = answer.getAnswers();
        if (answerMap == null) {
            answerMap = new LinkedHashMap<>();
        }
        answerMap.put(qid, processId);
        answer.setAnswers(answerMap);
        practices.put(type, answer);
        vacationHomeworkResult.setPractices(practices);
        return upsert(vacationHomeworkResult) != null;
    }

    /**
     * 做作业的写入操作，用于绘本
     *
     * @param type                       作业形式
     * @param key                        categoryId+lessonId
     * @param newHomeworkResultAppAnswer 做题明细
     * @return boolean
     */
    public Boolean doHomeworkBasicApp(VacationHomework.Location location,
                                      ObjectiveConfigType type,
                                      String key,
                                      NewHomeworkResultAppAnswer newHomeworkResultAppAnswer) {

        VacationHomework.ID id = new VacationHomework.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), location.getStudentId());
        VacationHomeworkResult vacationHomeworkResult = load(id.toString());
        if (vacationHomeworkResult == null) {
            return false;
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = vacationHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer answer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = answer.getAppAnswers();
        if (appAnswerMap == null) {
            appAnswerMap = new LinkedHashMap<>();
        }
        appAnswerMap.put(key, newHomeworkResultAppAnswer);
        answer.setAppAnswers(appAnswerMap);
        practices.put(type, answer);
        vacationHomeworkResult.setPractices(practices);
        return upsert(vacationHomeworkResult) != null;
    }

    /**
     * 基础训练一题一题
     *
     * @param location                   作业
     * @param type                       作业类型
     * @param key                        categoryId+lessonId
     * @param newHomeworkResultAppAnswer 做题
     * @return boolean
     */
    public VacationHomeworkResult doHomeworkBasicAppPractice(VacationHomework.Location location,
                                                             ObjectiveConfigType type,
                                                             String key,
                                                             NewHomeworkResultAppAnswer newHomeworkResultAppAnswer) {
        if (newHomeworkResultAppAnswer == null) {
            return null;
        }
        VacationHomework.ID id = new VacationHomework.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), location.getStudentId());
        VacationHomeworkResult vacationHomeworkResult = load(id.toString());
        if (vacationHomeworkResult == null) {
            return null;
        }
        if (vacationHomeworkResult.isFinished()) {
            return vacationHomeworkResult;
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = vacationHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer typeAnswer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = typeAnswer.getAppAnswers();
        if (appAnswerMap == null) {
            appAnswerMap = new LinkedHashMap<>();
        }
        NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(key, new NewHomeworkResultAppAnswer());
        LinkedHashMap<String, String> answers = appAnswer.getAnswers();
        if (answers == null) {
            answers = new LinkedHashMap<>();
        }
        for (Map.Entry<String, String> entry : newHomeworkResultAppAnswer.getAnswers().entrySet()) {
            answers.put(entry.getKey(), entry.getValue());
        }

        if (newHomeworkResultAppAnswer.getCategoryId() != null) {
            appAnswer.setCategoryId(newHomeworkResultAppAnswer.getCategoryId());
        }
        if (newHomeworkResultAppAnswer.getPracticeId() != null) {
            appAnswer.setPracticeId(newHomeworkResultAppAnswer.getPracticeId());
        }
        if (StringUtils.isNotBlank(newHomeworkResultAppAnswer.getPracticeName())) {
            appAnswer.setPracticeName(newHomeworkResultAppAnswer.getPracticeName());
        }
        if (StringUtils.isNotBlank(newHomeworkResultAppAnswer.getLessonId())) {
            appAnswer.setLessonId(newHomeworkResultAppAnswer.getLessonId());
        }
        if (StringUtils.isNotBlank(newHomeworkResultAppAnswer.getVideoId())) {
            appAnswer.setVideoId(newHomeworkResultAppAnswer.getVideoId());
        }
        if (StringUtils.isNotBlank(newHomeworkResultAppAnswer.getQuestionBoxId())) {
            appAnswer.setQuestionBoxId(newHomeworkResultAppAnswer.getQuestionBoxId());
        }
        if (newHomeworkResultAppAnswer.getQuestionBoxType() != null) {
            appAnswer.setQuestionBoxType(newHomeworkResultAppAnswer.getQuestionBoxType());
        }
        appAnswer.setAnswers(answers);
        appAnswerMap.put(key, appAnswer);
        typeAnswer.setAppAnswers(appAnswerMap);
        practices.put(type, typeAnswer);
        vacationHomeworkResult.setPractices(practices);
        return upsert(vacationHomeworkResult);
    }

    /**
     * 基础训练某一个类型完成
     *
     * @param location 作业 location
     * @param type     作业类型
     * @param key      基础训练（categoryId+lessonId）
     * @param score    分数
     * @param duration 时长
     * @return boolean
     */
    public Boolean finishHomeworkBasicAppPractice(VacationHomework.Location location,
                                                  ObjectiveConfigType type,
                                                  String key,
                                                  Double score,
                                                  Long duration) {

        if (type == null || StringUtils.isBlank(key) || !StringUtils.equalsIgnoreCase(type.name(), ObjectiveConfigType.BASIC_APP.name())) {
            return false;
        }
        VacationHomework.ID id = new VacationHomework.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), location.getStudentId());
        VacationHomeworkResult vacationHomeworkResult = load(id.toString());
        if (vacationHomeworkResult == null) {
            return false;
        }
        if (vacationHomeworkResult.isFinished()) {
            return false;
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = vacationHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer answer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = answer.getAppAnswers();
        if (appAnswerMap == null) {
            appAnswerMap = new LinkedHashMap<>();
        }
        NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(key, new NewHomeworkResultAppAnswer());
        if (Objects.nonNull(score)) {
            appAnswer.setScore(score);
        }
        if (Objects.nonNull(duration)) {
            appAnswer.setDuration(duration);
        }
        appAnswer.setFinishAt(new Date());
        appAnswerMap.put(key, appAnswer);
        answer.setAppAnswers(appAnswerMap);
        practices.put(type, answer);
        vacationHomeworkResult.setPractices(practices);
        return upsert(vacationHomeworkResult) != null;
    }

    /**
     * 重难点专项某一个视频绘本完成
     *
     * @param location 作业
     * @param type     作业类型
     * @param videoId  //视频绘本ID
     * @param score    分数
     * @param duration 时长
     * @return 是否成功
     */
    public Boolean finishHomeworkKeyPoint(VacationHomework.Location location,
                                          ObjectiveConfigType type,
                                          String videoId,
                                          Double score,
                                          Long duration) {
        if (StringUtils.isBlank(videoId) || !ObjectiveConfigType.KEY_POINTS.equals(type)) {
            return false;
        }
        VacationHomework.ID id = new VacationHomework.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), location.getStudentId());
        VacationHomeworkResult vacationHomeworkResult = load(id.toString());
        if (vacationHomeworkResult == null) {
            return false;
        }
        if (vacationHomeworkResult.isFinished()) {
            return false;
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = vacationHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer answer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = answer.getAppAnswers();
        if (appAnswerMap == null) {
            appAnswerMap = new LinkedHashMap<>();
        }
        NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(videoId, new NewHomeworkResultAppAnswer());
        if (Objects.nonNull(score)) {
            appAnswer.setScore(score);
        }
        if (Objects.nonNull(duration)) {
            appAnswer.setDuration(duration);
        }
        appAnswer.setFinishAt(new Date());
        appAnswerMap.put(videoId, appAnswer);
        answer.setAppAnswers(appAnswerMap);
        practices.put(type, answer);
        vacationHomeworkResult.setPractices(practices);
        return upsert(vacationHomeworkResult) != null;
    }

    /**
     * 语文读背练习题的一个题包完成
     *
     * @param location      作业
     * @param userId        用户id
     * @param type          作业类型
     * @param questionBoxId //语文读背练习题包ID
     * @param duration      时长
     * @return 是否成功
     */
    public VacationHomeworkResult finishHomeworkNewReadRecite(VacationHomework.Location location,
                                                              Long userId,
                                                              ObjectiveConfigType type,
                                                              String questionBoxId,
                                                              Double score,
                                                              Long duration) {
        if (StringUtils.isBlank(questionBoxId) || !(ObjectiveConfigType.NEW_READ_RECITE.equals(type))) {
            return null;
        }
        VacationHomeworkResult.ID id = new VacationHomeworkResult.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), userId);
        VacationHomeworkResult vacationHomeworkResult = load(id.toString());
        if (vacationHomeworkResult == null) {
            return null;
        }
        if (vacationHomeworkResult.isFinished()) {
            return vacationHomeworkResult;
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = vacationHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer answer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = answer.getAppAnswers();
        if (appAnswerMap == null) {
            appAnswerMap = new LinkedHashMap<>();
        }
        NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(questionBoxId, new NewHomeworkResultAppAnswer());
        if (Objects.nonNull(score)) {
            appAnswer.setScore(score);
        }
        if (Objects.nonNull(duration)) {
            appAnswer.setDuration(duration);
        }
        appAnswer.setFinishAt(new Date());
        appAnswerMap.put(questionBoxId, appAnswer);
        answer.setAppAnswers(appAnswerMap);
        practices.put(type, answer);
        vacationHomeworkResult.setPractices(practices);
        return upsert(vacationHomeworkResult);
    }

    /**
     * 语文读背(打分)题的一个题包完成
     *
     * @param location       作业
     * @param userId         用户ID
     * @param type           作业类型
     * @param questionBoxId  题包ID
     * @param score          分数
     * @param duration       时长
     * @param standardNum
     * @param appQuestionNum 题目个数
     * @return 是否成功
     */
    public VacationHomeworkResult finishHomeworkReadReciteWithScore(VacationHomework.Location location,
                                                                    Long userId,
                                                                    ObjectiveConfigType type,
                                                                    String questionBoxId,
                                                                    Double score,
                                                                    Long duration,
                                                                    Integer standardNum,
                                                                    Integer appQuestionNum) {
        if (!(ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(type)) || StringUtils.isBlank(questionBoxId)) {
            return null;
        }
        VacationHomeworkResult.ID id = new VacationHomeworkResult.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), userId);
        VacationHomeworkResult vacationHomeworkResult = load(id.toString());
        if (vacationHomeworkResult == null) {
            return null;
        }
        if (vacationHomeworkResult.isFinished()) {
            return vacationHomeworkResult;
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = vacationHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer answer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = answer.getAppAnswers();
        if (appAnswerMap == null) {
            appAnswerMap = new LinkedHashMap<>();
        }
        NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(questionBoxId, new NewHomeworkResultAppAnswer());
        if (Objects.nonNull(score)) {
            appAnswer.setScore(score);
        }
        if (Objects.nonNull(duration)) {
            appAnswer.setDuration(duration);
        }
        if (Objects.nonNull(standardNum)) {
            appAnswer.setStandardNum(standardNum);
        }
        if (Objects.nonNull(appQuestionNum)) {
            appAnswer.setAppQuestionNum(appQuestionNum);
        }
        appAnswer.setFinishAt(new Date());
        appAnswerMap.put(questionBoxId, appAnswer);
        answer.setAppAnswers(appAnswerMap);
        practices.put(type, answer);
        vacationHomeworkResult.setPractices(practices);
        return upsert(vacationHomeworkResult);
    }

    /**
     * 作业完成
     *
     * @param location    作业location
     * @param type        作业形式
     * @param score       本次作业形式的总分
     * @param duration    本次作业形式的总用时
     * @param isFinish    本次作业形式是否完成
     * @param isFinishAll 所有作业完成
     * @return VacationHomeworkResult
     */
    public VacationHomeworkResult finishHomework(VacationHomework.Location location,
                                                 ObjectiveConfigType type,
                                                 Double score,
                                                 Long duration,
                                                 Boolean isFinish,
                                                 Boolean isFinishAll) {
        if (Objects.equals(Boolean.FALSE, isFinish) && Objects.equals(Boolean.FALSE, isFinishAll)) {
            return null;
        }
        VacationHomework.ID id = new VacationHomework.ID(location.getPackageId(), location.getWeekRank(), location.getDayRank(), location.getStudentId());
        VacationHomeworkResult vacationHomeworkResult = load(id.toString());
        if (vacationHomeworkResult == null) {
            return null;
        }
        if (vacationHomeworkResult.isFinished()) {
            return vacationHomeworkResult;
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = vacationHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer answer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        // 当前作业形式的计算总分
        if (Objects.nonNull(score)) {
            answer.setScore(score);
        }
        // 当前作业形式完成的总用时
        if (Objects.nonNull(duration)) {
            answer.setDuration(duration);
        }
        // 某个作业形式的完成
        if (Objects.equals(isFinish, Boolean.TRUE)) {
            answer.setFinishAt(new Date());
        }
        // 全部完成
        if (Objects.equals(isFinishAll, Boolean.TRUE)) {
            vacationHomeworkResult.setFinishAt(new Date());
        }
        practices.put(type, answer);
        vacationHomeworkResult.setPractices(practices);
        return upsert(vacationHomeworkResult);
    }

    /**
     * 奖励学豆
     *
     * @param homeworkId     作业id
     * @param rewardIntegral 奖励学豆
     * @return boolean
     */
    public Boolean saveVacationHomeworkRewardIntegral(String homeworkId, Integer rewardIntegral) {
        VacationHomeworkResult vacationHomeworkResult = this.load(homeworkId);
        if (vacationHomeworkResult == null) {
            return false;
        }
        Integer _rewardIntegral = vacationHomeworkResult.getRewardIntegral();
        if (_rewardIntegral != null) {
            rewardIntegral += _rewardIntegral;
        }
        vacationHomeworkResult.setRewardIntegral(rewardIntegral);
        return upsert(vacationHomeworkResult) != null;
    }

    /**
     * 保存老师对此作业的评语
     *
     * @param homeworkId 作业id
     * @return boolean
     */
    public Boolean saveVacationHomeworkComment(String homeworkId, String comment, String audioComment) {
        VacationHomeworkResult vacationHomeworkResult = load(homeworkId);
        if (vacationHomeworkResult == null) {
            return false;
        }
        vacationHomeworkResult.setComment(comment);
        if (audioComment != null) {
            vacationHomeworkResult.setAudioComment(audioComment);
        }
        return upsert(vacationHomeworkResult) != null;
    }
}
