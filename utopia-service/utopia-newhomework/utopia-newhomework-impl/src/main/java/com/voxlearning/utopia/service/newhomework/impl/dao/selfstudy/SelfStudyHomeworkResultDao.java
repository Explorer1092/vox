package com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.COURSE_APP_CONFIGTYPE;
/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Named
@CacheBean(type = SelfStudyHomeworkResult.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SelfStudyHomeworkResultDao extends AsyncDynamicMongoPersistence<SelfStudyHomeworkResult, String> {

    @Override
    protected String calculateDatabase(String template, SelfStudyHomeworkResult document) {
        SelfStudyHomeworkResult.ID id = document.parseID();
        String month = id.getMonth();
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, SelfStudyHomeworkResult document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(SelfStudyHomeworkResult document, Collection<String> dimensions) {
        dimensions.add(SelfStudyHomeworkResult.ck_id(document.getId()));
    }

    /**
     * 初始化作业结果中间表
     *
     * @param location 作业的location
     * @return CorrectQuestionResult
     */
    public SelfStudyHomeworkResult initSelfStudyHomeworkResult(SelfStudyHomework.Location location) {
        SelfStudyHomeworkResult homeworkResult = new SelfStudyHomeworkResult();
        homeworkResult.setId(location.getId());
        homeworkResult.setHomeworkId(location.getId());
        homeworkResult.setSubject(location.getSubject());
        homeworkResult.setClazzGroupId(location.getClazzGroupId());
        homeworkResult.setUserId(location.getStudentId());
        homeworkResult.setUserStartAt(new Date());
        return insertIfAbsent(location.getId(), homeworkResult);
    }

    /**
     * 基础训练某一个类型完成
     *
     * @param location 作业
     * @param type     作业类型
     * @param key      基础训练（categoryId+lessonId）、巩固学习（课程id）
     * @param score    分数
     * @param duration 时长
     * @return 是否成功
     */
    public SelfStudyHomeworkResult finishAppPractice(SelfStudyHomework.Location location,
                                                     ObjectiveConfigType type,
                                                     String key,
                                                     Double score,
                                                     Long duration) {

        if (type == null || StringUtils.isBlank(key)) {
            return load(location.getId());
        }

        String id = location.getId();
        Criteria criteria = Criteria.where("_id").is(id);
        criteria.and("finishAt").exists(false);

        Update update = new Update();
        Date d = new Date();
        if (Objects.nonNull(score)) {
            update.set(StringUtils.join("practices.", type, ".appAnswers.", key, ".score"), score);
        }
        if (Objects.nonNull(duration)) {
            update.set(StringUtils.join("practices.", type, ".appAnswers.", key, ".duration"), duration);
        }
        update.set(StringUtils.join("practices.", type, ".appAnswers.", key, ".finishAt"), new Date());
        update.set("updateAt", d);
        if (COURSE_APP_CONFIGTYPE.contains(type)) {
            update.set(StringUtils.join("practices.", type, ".appAnswers.", key, ".courseId"), key);
        }

        MongoNamespace namespace = calculateIdMongoNamespace(id);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SelfStudyHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(SelfStudyHomeworkResult.ck_id(id))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified;
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
     * @return SelfStudyHomeworkResult
     */
    public SelfStudyHomeworkResult finishHomework(SelfStudyHomework.Location location,
                                                  ObjectiveConfigType type,
                                                  Double score,
                                                  Long duration,
                                                  Boolean isFinish,
                                                  Boolean isFinishAll) {
        if (Objects.equals(Boolean.FALSE, isFinish) && Objects.equals(Boolean.FALSE, isFinishAll)) {
            return null;
        }

        String id = location.getId();
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        Date date = new Date();
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
            update.set("practices." + type.name() + ".finishAt", date);
        }
        // 全部完成
        if (Objects.equals(isFinishAll, Boolean.TRUE)) {
            update.set("finishAt", date);
        }
        update.set("updateAt", date);

        MongoNamespace namespace = calculateIdMongoNamespace(id);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        SelfStudyHomeworkResult modified = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(SelfStudyHomeworkResult.ck_id(id))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified;
    }
}
