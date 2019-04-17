package com.voxlearning.utopia.service.business.impl.dao;

import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;

import javax.inject.Named;
import java.util.*;

/**
 * Created by ganhaitian on 2017/8/6.
 */
@Named
@CacheBean(type = TeacherResourceTask.class)
public class TeacherResourceTaskDao extends AlpsStaticMongoDao<TeacherResourceTask, String> {

    @Override
    protected void calculateCacheDimensions(TeacherResourceTask document, Collection<String> dimensions) {
        dimensions.add(TeacherResourceTask.ck_uid(document.getUserId()));
    }

    /**
     * For back door
     * 获的所有在有效期内的任务数据
     *
     * @return
     */
    public List<TeacherResourceTask> loadAllValidTaskForBackDoor() {
        Criteria criteria = Criteria.where("status").is("ONGOING")
                .and("expiryDate").gte(new Date());
        return query(Query.query(criteria));
    }

    /**
     * 获得老师下面处于某状态的任务列表
     *
     * @param userId
     * @param status
     * @return
     */
    @CacheMethod
    public List<TeacherResourceTask> loadTeacherTaskByStatus(Long userId, String status) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").is(status);
        return query(Query.query(criteria));
    }

    /**
     * 调用时请走 @see com.voxlearning.utopia.service.business.impl.service.NewTeacherResourceServiceImpl#loadTeacherTaskUpdateExpired(java.lang.Long)
     */
    @CacheMethod
    public List<TeacherResourceTask> loadTeacherTasks(@CacheParameter("USER_ID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "createAt");
        return query(Query.query(criteria).with(sort));
    }

    public boolean updateTaskStatus(String id, String status) {
        TeacherResourceTask org = load(id);
        if (org == null)
            return false;

        Criteria criteria = Criteria.where("_id").is(id);
        Update update = Update.update("status", status).set("updateAt", new Date());

        UpdateResult result = updateOne(createMongoConnection(), criteria, update);
        boolean ret = result.getModifiedCount() > 0;
        if (ret) {
            Set<String> keys = new HashSet<>();
            calculateCacheDimensions(org, keys);
            getCache().delete(keys);
        }

        return ret;
    }

    public List<TeacherResourceTask> loadExpiryData() {
        try {
            Date fixDate = DateUtils.parseDate("2018-12-29", "yyyy-MM-dd");

            Criteria criteria = Criteria.where("resourceId").is("5c1116b9ecf1bd0ce60f10bb")
                    .and("status").in(Arrays.asList(TeacherResourceTask.Status.EXPIRED, TeacherResourceTask.Status.ONGOING))
                    .and("createAt").lte(fixDate);
            return query(Query.query(criteria));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

}
