package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.constant.AiUserVideoLevel;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = AIUserVideo.class)
public class AIUserVideoDao extends AlpsStaticMongoDao<AIUserVideo, String> {
    @Override
    protected void calculateCacheDimensions(AIUserVideo document, Collection<String> dimensions) {
        dimensions.add(AIUserVideo.ck_id(document.getId()));
        dimensions.add(AIUserVideo.ck_uid(document.getUserId()));
        dimensions.add(AIUserVideo.ck_unit_id(document.getUnitId()));
        dimensions.add(AIUserVideo.ck_unit_status(document.getUnitId(), document.getStatus()));
    }

    public long updateExamineStatus(String id, AIUserVideo.ExamineStatus from, AIUserVideo.ExamineStatus to, String updater, AIUserVideo.Category category, String description) {
        Criteria criteria = Criteria.where("_id").is(id)
                .and("disabled").is(false)
                .and("status").is(from);

        Update update = new Update()
                .set("status", to)
                .set("examiner", updater)
                .set("updateTime", new Date());
        if (category != null) {
            update.set("category", category.name());
        }

        if (StringUtils.isNotBlank(description)) {
            update.set("description", description);
        }

        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanCacheById(id, from);
        }
        return count;
    }

    public void updateContent(String id, String comment,String commentAudio, List<AIUserVideo.Label> labels, String updater, AIUserVideo.Category category) {
        Criteria criteria = Criteria.where("_id").is(id)
                                    .and("disabled").is(false);
        Update update = new Update();
        update.set("updateTime", new Date()).set("examiner", updater);

        if (StringUtils.isNotBlank(comment)) {
            update.set("comment", comment);
        } else {
            update.set("comment", "");
        }
        if (StringUtils.isNotBlank(commentAudio)) {
            update.set("commentAudio", commentAudio);
        }
        if (CollectionUtils.isNotEmpty(labels)) {
            update.set("labels", labels);
        }
        if (category != null) {
            update.set("category", category.name());
        }

        AIUserVideo res = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (res != null) {
            cleanCacheById(id);
        }
    }

    public void updateForRemark(String id, AiUserVideoLevel level, boolean forRemark, Integer remarkLessonScore, String remarkQRCid) {
        Criteria criteria = Criteria.where("_id").is(id);

        Update update = new Update()
                .set("remarkLevel", level)
                .set("forRemark", forRemark)
                .set("remarkLessonScore", remarkLessonScore)
                .set("remarkQRCid", remarkQRCid)
                .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanCacheById(id);
        }
    }


    public long updateForShare(String id) {
        Criteria criteria = Criteria.where("_id").is(id)
                .and("disabled").is(false);

        Update update = new Update()
                .set("forShare", true);
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanCacheById(id);
        }
        return count;
    }

    @CacheMethod
    public List<AIUserVideo> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public List<AIUserVideo> loadByUserIdAndUnitId(Long userId, String unitId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("unitId").is(unitId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public List<AIUserVideo> loadByUnitId(String unitId, AIUserVideo.ExamineStatus status) {
        Criteria criteria = Criteria.where("unitId").is(unitId).and("status").is(status.name()).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public List<AIUserVideo> loadByDateRange(Date startDate, Date endDate) {
        Criteria criteria = Criteria.where("createTime").gt(startDate).and("createTime").lt(endDate).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    private void cleanCacheById(String id) {
        AIUserVideo config = $load(id);
        if (config != null) {
            Set<String> cacheIds = new HashSet<>();
            cacheIds.add(AIUserVideo.ck_id(config.getId()));
            cacheIds.add(AIUserVideo.ck_uid(config.getUserId()));
            cacheIds.add(AIUserVideo.ck_unit_id(config.getUnitId()));
            cacheIds.add(AIUserVideo.ck_unit_status(config.getUnitId(), config.getStatus()));
            getCache().deletes(cacheIds);
        }
    }

    private void cleanCacheById(String id, AIUserVideo.ExamineStatus status) {
        AIUserVideo config = $load(id);
        if (config != null) {
            Set<String> cacheIds = new HashSet<>();
            cacheIds.add(AIUserVideo.ck_id(config.getId()));
            cacheIds.add(AIUserVideo.ck_uid(config.getUserId()));
            cacheIds.add(AIUserVideo.ck_unit_id(config.getUnitId()));
            cacheIds.add(AIUserVideo.ck_unit_status(config.getUnitId(), config.getStatus()));
            cacheIds.add(AIUserVideo.ck_unit_status(config.getUnitId(), status));
            getCache().deletes(cacheIds);
        }
    }
}
