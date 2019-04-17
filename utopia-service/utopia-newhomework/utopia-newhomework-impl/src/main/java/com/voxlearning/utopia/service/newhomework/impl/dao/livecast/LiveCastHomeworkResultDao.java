package com.voxlearning.utopia.service.newhomework.impl.dao.livecast;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Named
@UtopiaCacheSupport(LiveCastHomeworkResult.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class LiveCastHomeworkResultDao extends AsyncDynamicMongoPersistence<LiveCastHomeworkResult, String> {

    @Override
    protected String calculateDatabase(String template, LiveCastHomeworkResult document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, LiveCastHomeworkResult document) {
        LiveCastHomeworkResult.ID id = document.parseID();
        return StringUtils.formatMessage(template, id.getMonth());
    }

    @Override
    protected void calculateCacheDimensions(LiveCastHomeworkResult document, Collection<String> dimensions) {
        dimensions.add(LiveCastHomeworkResult.ck_id(document.getId()));
    }

    public void initLiveCastHomeworkResult(LiveCastHomework.Location location, Long studentId) {
        if (location == null || studentId == null) {
            return;
        }

        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
        LiveCastHomeworkResult liveCastHomeworkResult = load(id.toString());
        if (liveCastHomeworkResult != null) {
            return;
        }

        Date d = new Date();
        liveCastHomeworkResult = new LiveCastHomeworkResult();
        liveCastHomeworkResult.setHomeworkId(location.getId());
        liveCastHomeworkResult.setSubject(location.getSubject());
        liveCastHomeworkResult.setActionId(location.getActionId());
        liveCastHomeworkResult.setClazzGroupId(location.getClazzGroupId());
        liveCastHomeworkResult.setUserId(studentId);
        liveCastHomeworkResult.setUserStartAt(d);
        liveCastHomeworkResult.setCreateAt(d);
        liveCastHomeworkResult.setUpdateAt(d);

        LiveCastHomeworkResult modified = insertIfAbsent(id.toString(), liveCastHomeworkResult);
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(LiveCastHomeworkResult.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
    }


    public Boolean noteCommentToUnbeginStudent(LiveCastHomework.Location location, Long studentId, String comment) {
        if (location == null || studentId == null || comment == null) {
            return false;
        }

        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
        LiveCastHomeworkResult liveCastHomeworkResult = load(id.toString());
        if (liveCastHomeworkResult != null) {
            return false;
        }

        Date d = new Date();
        liveCastHomeworkResult = new LiveCastHomeworkResult();
        liveCastHomeworkResult.setHomeworkId(location.getId());
        liveCastHomeworkResult.setSubject(location.getSubject());
        liveCastHomeworkResult.setActionId(location.getActionId());
        liveCastHomeworkResult.setClazzGroupId(location.getClazzGroupId());
        liveCastHomeworkResult.setUserId(studentId);
        liveCastHomeworkResult.setCreateAt(d);
        liveCastHomeworkResult.setUpdateAt(d);
        liveCastHomeworkResult.setComment(comment);

        LiveCastHomeworkResult modified = insertIfAbsent(id.toString(), liveCastHomeworkResult);
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(LiveCastHomeworkResult.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified != null;
    }


    public Boolean noteCommentToBeginStudent(LiveCastHomeworkResult liveCastHomeworkResult, String comment) {
        if (liveCastHomeworkResult == null || comment == null) {
            return false;
        }
        // 评语不变的时候不再改动数据
        if (comment.equals(SafeConverter.toString(liveCastHomeworkResult.getComment(), ""))) {
            return true;
        }

        String id = liveCastHomeworkResult.getId();
        Criteria criteria = Criteria.where("_id").is(id);
        Date d = new Date();
        Update update = new Update();
        update.set("updateAt", d);
        update.set("comment", comment);

        MongoNamespace namespace = calculateIdMongoNamespace(id);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);
        LiveCastHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();


        if (modified != null) {
            getCache().getCacheObjectModifier().modify(LiveCastHomeworkResult.ck_id(id),
                    getDefaultCacheExpirationInSeconds(), currentValue -> modified);
        }
        return modified != null;

    }


    public Boolean finishCorrect(String id,
                                 ObjectiveConfigType type,
                                 Boolean finishCorrect,
                                 Boolean allFinishCorrect) {

        if (finishCorrect == null && allFinishCorrect == null) {
            return false;
        }
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        Date d = new Date();
        // 某个作业形式的批改完成
        if (type != null && Objects.equals(finishCorrect, Boolean.TRUE)) {
            update.set("practices." + type.name() + ".correctedAt", d);
        }
        // 是否全部批改完
        if (Objects.equals(allFinishCorrect, Boolean.TRUE)) {
            update.set("finishCorrect", Boolean.TRUE);
            update.set("correctAt", d);
        }
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);
        LiveCastHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(LiveCastHomeworkResult.ck_id(id),
                    getDefaultCacheExpirationInSeconds(), currentValue -> modified);
        }
        return modified != null;
    }




    public Boolean changeScore(LiveCastHomeworkResult liveCastHomeworkResult, ObjectiveConfigType type, double score) {
        if (liveCastHomeworkResult == null || type == null) {
            return false;
        }
        if (liveCastHomeworkResult.getPractices() == null || !(liveCastHomeworkResult.getPractices().containsKey(type))) {
            return false;
        }
        String id = liveCastHomeworkResult.getId();

        Criteria criteria = Criteria.where("_id").is(id);
        Date d = new Date();
        Update update = new Update();
        update.set("updateAt", d);
        update.set("practices." + type + ".score", score);


        MongoNamespace namespace = calculateIdMongoNamespace(id);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);
        LiveCastHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();


        if (modified != null) {
            getCache().getCacheObjectModifier().modify(LiveCastHomeworkResult.ck_id(id),
                    getDefaultCacheExpirationInSeconds(), currentValue -> modified);
        }
        return modified != null;
    }


    public Boolean doHomework(LiveCastHomework.Location location,
                              Long studentId,
                              ObjectiveConfigType type,
                              String qid,
                              String processId) {

        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
        Date d = new Date();

        Criteria criteria = Criteria.where("_id").is(id.toString());
        criteria.and("practices." + type + ".answers" + "." + qid).exists(false);

        Update update = new Update();
        update.set("practices." + type + ".answers" + "." + qid, processId);
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER);
        LiveCastHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();

        if (modified != null) {
            // 这步一定是命中缓存的，并且可以拿到更新前的数据
            LiveCastHomeworkResult liveCastHomeworkResult = load(id.toString());
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = liveCastHomeworkResult.getPractices();
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
            liveCastHomeworkResult.setPractices(practices);
            liveCastHomeworkResult.setUpdateAt(d);

            getCache().createCacheValueModifier()
                    .key(LiveCastHomeworkResult.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> liveCastHomeworkResult)
                    .execute();
        }

        return modified != null;
    }


    /**
     * 做作业的写入操作，用于绘本
     *
     * @param type                       作业形式
     * @param key                        categoryId+lessonId
     * @param newHomeworkResultAppAnswer 做题明细
     * @return boolean
     */
    public LiveCastHomeworkResult doHomeworkBasicApp(LiveCastHomework.Location location,
                                                     Long userId,
                                                     ObjectiveConfigType type,
                                                     String key,
                                                     NewHomeworkResultAppAnswer newHomeworkResultAppAnswer) {

        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), userId);

        Criteria criteria = Criteria.where("_id").is(id.toString());
        criteria.and("finishAt").exists(false);

        Update update = new Update();
        Date d = new Date();
        update.set("practices." + type + ".appAnswers" + "." + key, newHomeworkResultAppAnswer);
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER);
        LiveCastHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();

        if (modified != null) {
            LiveCastHomeworkResult liveCastHomeworkResult = load(id.toString());
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = liveCastHomeworkResult.getPractices();
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
            liveCastHomeworkResult.setPractices(practices);
            liveCastHomeworkResult.setUpdateAt(d);

            getCache().createCacheValueModifier()
                    .key(LiveCastHomeworkResult.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> liveCastHomeworkResult)
                    .execute();
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
    public Boolean finishHomeworkBasicAppPractice(LiveCastHomework.Location location,
                                                  Long userId,
                                                  ObjectiveConfigType type,
                                                  String key,
                                                  Double score,
                                                  Long duration) {

        if (type == null || StringUtils.isBlank(key) || !StringUtils.equalsIgnoreCase(type.name(), ObjectiveConfigType.BASIC_APP.name())) {
            return false;
        }

        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), userId);

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
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER);
        LiveCastHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(LiveCastHomeworkResult.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified != null;
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
     * @return NewHomeworkResult
     */
    public LiveCastHomeworkResult finishHomework(LiveCastHomework.Location location,
                                                 Long userId,
                                                 ObjectiveConfigType type,
                                                 Double score,
                                                 Long duration,
                                                 Boolean isFinish,
                                                 Boolean isFinishAll,
                                                 Boolean finishCorrect) {
        if (Objects.equals(Boolean.FALSE, isFinish) && Objects.equals(Boolean.FALSE, isFinishAll)) {
            return null;
        }

        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), userId);

        Criteria criteria = Criteria.where("_id").is(id.toString());

        Update update = new Update();
        Date d = new Date();
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
        // 全部完成
        if (Objects.equals(isFinishAll, Boolean.TRUE)) {
            update.set("finishAt", d);
        }
        // 是否批改完
        if (Objects.equals(finishCorrect, Boolean.TRUE)) {
            update.set("finishCorrect", Boolean.TRUE);
        }
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER);
        LiveCastHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(LiveCastHomeworkResult.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified;
    }
}
