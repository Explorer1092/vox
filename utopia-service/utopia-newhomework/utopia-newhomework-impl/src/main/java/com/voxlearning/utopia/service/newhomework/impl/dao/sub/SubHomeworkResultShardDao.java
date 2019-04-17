package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@CacheBean(type = SubHomeworkResult.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class SubHomeworkResultShardDao extends DynamicMongoShardPersistence<SubHomeworkResult, String> {

    @Override
    protected String calculateDatabase(String template, SubHomeworkResult document) {
        SubHomeworkResult.ID id = document.parseID();
        String month = StringUtils.substring(id.getDay(), 0, 6);
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, SubHomeworkResult document) {
        SubHomeworkResult.ID id = document.parseID();
        return StringUtils.formatMessage(template, id.getDay());
    }

    @Override
    protected void calculateCacheDimensions(SubHomeworkResult document, Collection<String> dimensions) {
        dimensions.add(SubHomeworkResult.ck_id(document.getId()));
        dimensions.add(SubHomeworkResult.ck_homework(document.getHomeworkId()));
    }

    public SubHomeworkResult updateNewHomeworkResultUrge(SubHomeworkResult subHomeworkResult) {
        if (subHomeworkResult == null || !subHomeworkResult.isFinished()) {
            return null;
        }
        Criteria criteria = Criteria.where("_id").is(subHomeworkResult.getId());
        Date d = new Date();
        Update update = new Update();
        update.set("urge", true);
        update.set("beanNum", SafeConverter.toInt(subHomeworkResult.getBeanNum()));
        update.set("updateAt", d);
        MongoNamespace namespace = calculateIdMongoNamespace(subHomeworkResult.getId());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, subHomeworkResult.getId()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(subHomeworkResult.getHomeworkId(), subHomeworkResult.getId(), modified, d);
        }
        return modified;
    }

    /**
     * 初始化作业结果中间表
     *
     * @param location 作业的location
     * @param userId   用户ID
     */
    public SubHomeworkResult initSubHomeworkResult(SubHomework.Location location, Long userId) {
        if (location == null || userId == null) {
            return null;
        }
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());
        SubHomeworkResult subHomeworkResult = load(id.toString());
        if (subHomeworkResult == null || subHomeworkResult.getUserStartAt() == null) {
            if (subHomeworkResult == null) {
                subHomeworkResult = new SubHomeworkResult();
                subHomeworkResult.setHomeworkId(location.getId());
                subHomeworkResult.setSubject(location.getSubject());
                subHomeworkResult.setActionId(location.getActionId());
                subHomeworkResult.setClazzGroupId(location.getClazzGroupId());
                subHomeworkResult.setUserId(userId);
                subHomeworkResult.setUserStartAt(new Date());
                return insertIfAbsent(id.toString(), subHomeworkResult);
            } else {
                Criteria criteria = Criteria.where("_id").is(id.toString());
                criteria.and("userStartAt").exists(false);
                Date d = new Date();

                Update update = new Update();
                update.set("userStartAt", d);
                update.set("updateAt", d);

                FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                        .upsert(false)
                        .returnDocument(ReturnDocument.AFTER);
                MongoNamespace namespace = calculateIdMongoNamespace(id.toString());

                SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
                if (modified != null) {
                    changeCache(location.getId(), id.toString(), modified, d);
                }
                return modified;
            }
        } else {
            return null;
        }
    }

    /**
     * 做作业的写入操作，用于绘本
     *
     * @param type                        作业形式
     * @param key                         categoryId+lessonId
     * @param baseHomeworkResultAppAnswer 做题明细
     * @return SubHomeworkResult
     */
    public SubHomeworkResult doHomeworkBasicApp(SubHomework.Location location,
                                                Long userId,
                                                ObjectiveConfigType type,
                                                String key,
                                                BaseHomeworkResultAppAnswer baseHomeworkResultAppAnswer) {

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        Criteria criteria = Criteria.where("_id").is(id.toString());
        criteria.and("finishAt").exists(false);

        Update update = new Update();
        Date d = new Date();
        update.set("practices." + type + ".appAnswers" + "." + key, baseHomeworkResultAppAnswer);
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            SubHomeworkResult subHomeworkResult = load(id.toString());
            LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> practices = subHomeworkResult.getPractices();
            if (practices == null) {
                practices = new LinkedHashMap<>();
            }
            BaseHomeworkResultAnswer answer = practices.getOrDefault(type, new BaseHomeworkResultAnswer());
            LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswerMap = answer.getAppAnswers();
            if (appAnswerMap == null) {
                appAnswerMap = new LinkedHashMap<>();
            }
            appAnswerMap.put(key, baseHomeworkResultAppAnswer);
            answer.setAppAnswers(appAnswerMap);
            practices.put(type, answer);
            subHomeworkResult.setPractices(practices);

            subHomeworkResult.setUpdateAt(d);
            changeCache(location.getId(), id.toString(), subHomeworkResult, d);
        }
        return modified;
    }

    /**
     * 基础训练某一个类型完成
     *
     * @param location 作业
     * @param userId   用户id
     * @param type     作业类型
     * @param key      基础训练（categoryId+lessonId）
     * @param score    分数
     * @param duration 时长
     * @return 是否成功
     */
    public SubHomeworkResult finishHomeworkBasicAppPractice(SubHomework.Location location,
                                                            Long userId,
                                                            ObjectiveConfigType type,
                                                            String key,
                                                            Double score,
                                                            Long duration) {

        if (type == null || StringUtils.isBlank(key) || !StringUtils.equalsIgnoreCase(type.name(), ObjectiveConfigType.BASIC_APP.name())) {
            return null;
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        Criteria criteria = Criteria.where("_id").is(id.toString());
        criteria.and("finishAt").exists(false);

        Update update = new Update();
        Date d = new Date();
        if (Objects.nonNull(score)) {
            update.set("practices." + type + ".appAnswers" + "." + key + ".score", score);
        }
        if (Objects.nonNull(duration)) {
            update.set("practices." + type + ".appAnswers" + "." + key + ".duration", duration);
        }
        update.set("practices." + type + ".appAnswers" + "." + key + ".finishAt", new Date());
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
    }

    /**
     * 重难点专项某一个视频绘本完成
     *
     * @param location 作业
     * @param userId   用户id
     * @param type     作业类型
     * @param videoId  //视频绘本ID
     * @param score    分数
     * @param duration 时长
     * @return 是否成功
     */
    public SubHomeworkResult finishHomeworkKeyPoint(SubHomework.Location location,
                                                    Long userId,
                                                    ObjectiveConfigType type,
                                                    String videoId,
                                                    Double score,
                                                    Long duration) {

        if (type == null || StringUtils.isBlank(videoId) || !(ObjectiveConfigType.KEY_POINTS.equals(type) || ObjectiveConfigType.NEW_READ_RECITE.equals(type) || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(type))) {
            return null;
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        Criteria criteria = Criteria.where("_id").is(id.toString());
        criteria.and("finishAt").exists(false);

        Update update = new Update();
        Date d = new Date();
        if (Objects.nonNull(score)) {
            update.set("practices." + type + ".appAnswers" + "." + videoId + ".score", score);
        }
        if (Objects.nonNull(duration)) {
            update.set("practices." + type + ".appAnswers" + "." + videoId + ".duration", duration);
        }
        update.set("practices." + type + ".appAnswers" + "." + videoId + ".finishAt", new Date());
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
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
    public SubHomeworkResult finishHomeworkNewReadRecite(SubHomework.Location location,
                                                         Long userId,
                                                         ObjectiveConfigType type,
                                                         String questionBoxId,
                                                         Double score,
                                                         Long duration) {

        if (type == null || StringUtils.isBlank(questionBoxId) || !(ObjectiveConfigType.NEW_READ_RECITE.equals(type))) {
            return null;
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        Criteria criteria = Criteria.where("_id").is(id.toString());
        criteria.and("finishAt").exists(false);

        Update update = new Update();
        Date d = new Date();

        if (Objects.nonNull(score)) {
            update.set("practices." + type + ".appAnswers" + "." + questionBoxId + ".score", score);
        }

        if (Objects.nonNull(duration)) {
            update.set("practices." + type + ".appAnswers" + "." + questionBoxId + ".duration", duration);
        }
        update.set("practices." + type + ".appAnswers" + "." + questionBoxId + ".finishAt", new Date());
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
    }

    /**
     * 语文读背(打分)题的一个题包完成
     *
     * @param location      作业
     * @param userId        用户id
     * @param type          作业类型
     * @param questionBoxId 题包ID
     * @param duration      时长
     * @return 是否成功
     */
    public SubHomeworkResult finishHomeworkReadReciteWithScore(SubHomework.Location location,
                                                               Long userId,
                                                               ObjectiveConfigType type,
                                                               String questionBoxId,
                                                               Double score,
                                                               Long duration,
                                                               Integer standardNum,
                                                               Integer appQuestionNum) {
        if (StringUtils.isBlank(questionBoxId)) {
            return null;
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        Criteria criteria = Criteria.where("_id").is(id.toString());
        criteria.and("finishAt").exists(false);

        Update update = new Update();
        Date d = new Date();

        if (Objects.nonNull(score)) {
            update.set("practices." + type + ".appAnswers" + "." + questionBoxId + ".score", score);
        }
        if (Objects.nonNull(duration)) {
            update.set("practices." + type + ".appAnswers" + "." + questionBoxId + ".duration", duration);
        }
        if (Objects.nonNull(standardNum)) {
            update.set("practices." + type + ".appAnswers" + "." + questionBoxId + ".standardNum", standardNum);
        }
        if (Objects.nonNull(appQuestionNum)) {
            update.set("practices." + type + ".appAnswers" + "." + questionBoxId + ".appQuestionNum", appQuestionNum);
        }

        update.set("practices." + type + ".appAnswers" + "." + questionBoxId + ".finishAt", new Date());
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
    }

    /**
     * 字词讲练题的一个题包完成
     */
    public SubHomeworkResult finishHomeworkWordTeachAndPractice(SubHomework.Location location,
                                                                Long userId,
                                                                ObjectiveConfigType type,
                                                                String stoneId,
                                                                Double score,
                                                                Long duration,
                                                                Double wordExerciseScore,
                                                                Double finalImageTextRhymeScore) {
        if (StringUtils.isBlank(stoneId)) {
            return null;
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        Criteria criteria = Criteria.where("_id").is(id.toString());
        criteria.and("finishAt").exists(false);

        Update update = new Update();
        Date d = new Date();

        if (Objects.nonNull(score)) {
            update.set("practices." + type + ".appAnswers" + "." + stoneId + ".score", score);
        }
        if (Objects.nonNull(duration)) {
            update.set("practices." + type + ".appAnswers" + "." + stoneId + ".duration", duration);
        }
        if (Objects.nonNull(wordExerciseScore)) {
            update.set("practices." + type + ".appAnswers" + "." + stoneId + ".wordExerciseScore", wordExerciseScore);
        }
        if (Objects.nonNull(finalImageTextRhymeScore)) {
            update.set("practices." + type + ".appAnswers" + "." + stoneId + ".imageTextRhymeScore", finalImageTextRhymeScore);
        }
        update.set("practices." + type + ".appAnswers" + "." + stoneId + ".finishAt", new Date());
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
    }

    /**
     * 作业完成
     *
     * @param location      作业location
     * @param userId        用户id
     * @param type          作业形式
     * @param score         本次作业形式的总分
     * @param duration      本次作业形式的总用时
     * @param isFinish      本次作业形式是否完成
     * @param isFinishAll   所有作业完成
     * @param finishCorrect 是否批改完
     * @param isRepaired    是否是补做
     * @return SubHomeworkResult
     */
    public SubHomeworkResult finishHomework(SubHomework.Location location,
                                            Long userId,
                                            ObjectiveConfigType type,
                                            Double score,
                                            Long duration,
                                            Boolean isFinish,
                                            Boolean isFinishAll,
                                            Boolean finishCorrect,
                                            Boolean isRepaired,
                                            SchoolLevel schoolLevel,
                                            List<String> ocrMentalAnswers,
                                            Integer ocrMentalQuestionCount,
                                            Integer ocrMentalCorrectQuestionCount,
                                            List<String> ocrDictationAnswers,
                                            Integer ocrDictationQuestionCount,
                                            Integer ocrDictationCorrectQuestionCount
    ) {
        if (Objects.equals(Boolean.FALSE, isFinish) && Objects.equals(Boolean.FALSE, isFinishAll) && Objects.equals(Boolean.FALSE, finishCorrect)) {
            return null;
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        Criteria criteria = Criteria.where("_id").is(id.toString());

        Update update = new Update();
        Date d = new Date();
        update.set("repair", (isRepaired == null) ? Boolean.FALSE : isRepaired);
        // 当前作业形式的计算总分
        if (Objects.nonNull(score)) {
            update.set("practices." + type.name() + ".score", score);
        }
        // 当前作业形式完成的总用时
        if (Objects.nonNull(duration)) {
            update.set("practices." + type.name() + ".duration", duration);
        }
        // 某个作业形式的完成
        if (Objects.equals(isFinish, Boolean.TRUE)) {
            update.set("practices." + type.name() + ".finishAt", new Date());
        }
        if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == type && CollectionUtils.isNotEmpty(ocrMentalAnswers)) {
            update.set("practices." + type.name() + ".ocrMentalAnswers", ocrMentalAnswers);
            update.set("practices." + type.name() + ".ocrMentalQuestionCount", ocrMentalQuestionCount);
            update.set("practices." + type.name() + ".ocrMentalCorrectQuestionCount", ocrMentalCorrectQuestionCount);
        }
        if (ObjectiveConfigType.OCR_DICTATION == type && CollectionUtils.isNotEmpty(ocrDictationAnswers)) {
            update.set("practices." + type.name() + ".ocrDictationAnswers", ocrDictationAnswers);
            update.set("practices." + type.name() + ".ocrDictationQuestionCount", ocrDictationQuestionCount);
            update.set("practices." + type.name() + ".ocrDictationCorrectQuestionCount", ocrDictationCorrectQuestionCount);
        }
        // 全部完成
        if (Objects.equals(isFinishAll, Boolean.TRUE)) {
            update.set("finishAt", d);
            update.set("schoolLevel", schoolLevel);
        }
        // 是否批改完
        if (Objects.equals(finishCorrect, Boolean.TRUE)) {
            update.set("finishCorrect", Boolean.TRUE);
        }
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(SubHomeworkResult.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();

            String ck_h = SubHomeworkResult.ck_homework(location.getId());
            ChangeCacheObject<Set<SubHomeworkResult.Location>> modifier = locations -> {
                locations.stream()
                        .filter(o -> StringUtils.equalsIgnoreCase(o.getId(), id.toString()))
                        .forEach(o -> {
                            o.setUpdateAt(d);
                            if (Objects.equals(isFinishAll, Boolean.TRUE)) {
                                o.setFinishAt(d);
                            }
                        });
                return locations;
            };
            CacheValueModifierExecutor<Set<SubHomeworkResult.Location>> executor = getCache().createCacheValueModifier();
            executor.key(ck_h)
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(modifier)
                    .execute();
        }
        return modified;
    }

    public Set<SubHomeworkResult.Location> findByHomework(String day, Subject subject, String homeworkId, List<Long> groupStudentIds) {
        if (StringUtils.isBlank(day) || subject == null || StringUtils.isBlank(homeworkId)) {
            return Collections.emptySet();
        }

        String key = SubHomeworkResult.ck_homework(homeworkId);
        Set<SubHomeworkResult.Location> set = getCache().load(key);
        if (set != null) {
            return set;
        }

        Set<String> resultIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(groupStudentIds)) {
            for (Long studentId : groupStudentIds) {
                SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, subject, homeworkId, studentId.toString());
                resultIds.add(id.toString());
            }
        }
        set = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(resultIds)) {
            Map<String, SubHomeworkResult> resultMap = loads(resultIds);
            if (MapUtils.isNotEmpty(resultMap)) {
                set = resultMap.values()
                        .stream()
                        .map(SubHomeworkResult::toLocation)
                        .collect(Collectors.toSet());
            }
        }
//        SubHomeworkResult.ID mock = new SubHomeworkResult.ID();
//        mock.setDay(day);
//        mock.setSubject(subject);
//        mock.setHid(homeworkId);
//        mock.setUserId("0");
//        String patternStr = "^{}-{}-{}-";
//        Pattern pattern = Pattern.compile(StringUtils.formatMessage(patternStr, day, subject, homeworkId));
//
//        Criteria criteria = Criteria.where("_id").regex(pattern);
//        Query query = Query.query(criteria);
//        query.field().includes("_id", "homeworkId", "subject", "actionId", "clazzGroupId", "userId", "createAt", "updateAt", "finishAt");
//
//        MongoNamespace namespace = calculateIdMongoNamespace(mock.toString());
//        set = $executeQuery(createMongoConnection(namespace), query)
//                .getUninterruptibly()
//                .stream()
//                .map(SubHomeworkResult::toLocation)
//                .collect(Collectors.toSet());

        getCache().add(key, getDefaultCacheExpirationInSeconds(), set);
        return set;
    }

    /**
     * 奖励学豆
     *
     * @param location       作业location
     * @param userId         用户id
     * @param rewardIntegral 奖励学豆
     * @return boolean
     */
    public SubHomeworkResult saveHomeworkRewardIntegral(SubHomework.Location location, Long userId, Integer rewardIntegral) {
        if (location == null || userId == null || rewardIntegral == null || rewardIntegral < 0) {
            return null;
        }
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());
        SubHomeworkResult subHomeworkResult = this.load(id.toString());
        if (subHomeworkResult == null) {
            return initSubHomeworkResult(location, userId, null, null, rewardIntegral);
        }

        Integer _rewardIntegral = subHomeworkResult.getRewardIntegral();
        if (_rewardIntegral != null)
            rewardIntegral += _rewardIntegral;

        Criteria criteria = Criteria.where("_id").is(id.toString());
        Update update = new Update();
        Date d = new Date();
        update.set("updateAt", d);
        update.set("rewardIntegral", rewardIntegral);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
    }

    /**
     * 完成作业奖励学豆、能量、学分
     *
     * @param location 作业location
     * @param userId   用户id
     * @param integral 奖励学豆
     * @param energy   奖励能量
     * @param credit   奖励学分
     * @return boolean
     */
    public SubHomeworkResult saveFinishHomeworkReward(SubHomework.Location location, Long userId, Integer integral, Integer energy, Integer credit) {
        if (location == null || userId == null || (integral == null && energy == null && credit == null)) {
            return null;
        }
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());
        Criteria criteria = Criteria.where("_id").is(id.toString());
        Update update = new Update();
        Date d = new Date();
        update.set("updateAt", d);
        if (integral != null) {
            update.set("integral", integral);
        }
        if (energy != null) {
            update.set("energy", energy);
        }
        if (credit != null) {
            update.set("credit", credit);
        }

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
    }

    /**
     * 保存老师对此作业的评语
     *
     * @param location 作业location
     * @param userId   用户id
     * @param comment  评语
     * @return boolean
     */
    public SubHomeworkResult saveSubHomeworkComment(SubHomework.Location location, Long userId, String comment, String audioComment) {
        if (location == null || userId == null || (StringUtils.isBlank(comment) && StringUtils.isBlank(audioComment))) {
            return null;
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        SubHomeworkResult subHomeworkResult = this.load(id.toString());
        if (subHomeworkResult == null) {
            return initSubHomeworkResult(location, userId, comment, audioComment, null);
        }

        Criteria criteria = Criteria.where("_id").is(id.toString());
        Update update = new Update();
        Date d = new Date();
        update.set("updateAt", d);
        update.set("comment", comment);
        if (audioComment != null) {
            update.set("audioComment", audioComment);
        }

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
    }

    /**
     * 老师作业评语发学豆初始化作业结果中间表
     *
     * @param location 作业的location
     * @param userId   用户ID
     * @param comment  评语
     * @param integral 学豆
     * @return SubHomeworkResult
     */
    private SubHomeworkResult initSubHomeworkResult(SubHomework.Location location,
                                                    Long userId, String comment, String audioComment, Integer integral) {
        if (location == null || userId == null || (StringUtils.isBlank(comment) && StringUtils.isBlank(audioComment) && integral == null)) {
            return null;
        }
        SubHomeworkResult subHomeworkResult = new SubHomeworkResult();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());
        subHomeworkResult.setHomeworkId(location.getId());
        subHomeworkResult.setSubject(location.getSubject());
        subHomeworkResult.setActionId(location.getActionId());
        subHomeworkResult.setClazzGroupId(location.getClazzGroupId());
        subHomeworkResult.setUserId(userId);
        if (StringUtils.isNotEmpty(comment)) {
            subHomeworkResult.setComment(comment);
        }
        if (StringUtils.isNotEmpty(audioComment)) {
            subHomeworkResult.setAudioComment(audioComment);
        }
        if (integral != null) {
            subHomeworkResult.setRewardIntegral(integral);
        }
        return insertIfAbsent(id.toString(), subHomeworkResult);
    }

    public SubHomeworkResult finishCorrectToApp(String homeworkId,
                                                String id,
                                                ObjectiveConfigType type,
                                                String appId,
                                                Boolean review,
                                                CorrectType correctType,
                                                Correction correction,
                                                String teacherMark,
                                                Boolean isBatch) {

        Criteria criteria = Criteria.where("_id").is(id);
        Date d = new Date();
        Update update = new Update();
        String appWay = "practices." + type.name() + ".appAnswers." + appId + ".";
        if (Objects.equals(Boolean.TRUE, review)) {
            update.set(appWay + "review", review);
        }
        // 批改类型
        if (correctType != null) {
            update = update.set(appWay + "correctType", correctType);
        } else {
            // 默认属性
            update = update.set(appWay + "correctType", CorrectType.CORRECT);
        }
        // 批改信息，不为空且为非批量/一键操作的时候
        if (Objects.equals(Boolean.FALSE, isBatch)) {
            update = update.set(appWay + "correction", correction);
        }
        // 评语
        if (StringUtils.isNotBlank(teacherMark)) {
            update = update.set(appWay + "teacherMark", teacherMark);
        }

        update = update.set(appWay + "correctAt", d);
        update = update.currentDate("updateAt");
        MongoNamespace namespace = calculateIdMongoNamespace(id);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER);
        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(homeworkId, id, modified, d);
        }
        return modified;
    }

    /**
     * 完成某种作业形式的批改或者完成所有作业的批改
     *
     * @param location         作业
     * @param userId           用户id
     * @param type             作业类型
     * @param finishCorrect    某种作业形式的完成
     * @param allFinishCorrect 所有作业完成
     * @return boolean
     */
    public SubHomeworkResult finishCorrect(SubHomework.Location location,
                                           Long userId,
                                           ObjectiveConfigType type,
                                           Boolean finishCorrect,
                                           Boolean allFinishCorrect) {

        if (finishCorrect == null && allFinishCorrect == null) {
            return null;
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());

        Criteria criteria = Criteria.where("_id").is(id.toString());
        Update update = new Update();
        Date d = new Date();
        // 某个作业形式的批改完成
        if (type != null && Objects.equals(finishCorrect, Boolean.TRUE)) {
            update.set("practices." + type.name() + ".correctedAt", d);
        }
        // 是否全部批改完
        if (Objects.equals(allFinishCorrect, Boolean.TRUE)) {
            update.set("finishCorrect", Boolean.TRUE);
        }
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SubHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            changeCache(location.getId(), id.toString(), modified, d);
        }
        return modified;
    }

    private void changeCache(String homeworkId, String homeworkResultId, SubHomeworkResult modified, Date updateAt) {
        getCache().createCacheValueModifier()
                .key(SubHomeworkResult.ck_id(homeworkResultId))
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> modified)
                .execute();

        String ck_h = SubHomeworkResult.ck_homework(homeworkId);
        ChangeCacheObject<Set<SubHomeworkResult.Location>> modifier = locations -> {
            locations.stream()
                    .filter(o -> StringUtils.equalsIgnoreCase(o.getId(), homeworkResultId))
                    .forEach(o -> o.setUpdateAt(updateAt));
            return locations;
        };
        CacheValueModifierExecutor<Set<SubHomeworkResult.Location>> executor = getCache().createCacheValueModifier();
        executor.key(ck_h)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(modifier)
                .execute();
    }
}
