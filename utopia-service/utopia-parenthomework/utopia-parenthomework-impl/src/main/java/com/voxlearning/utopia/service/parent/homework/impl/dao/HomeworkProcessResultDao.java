package com.voxlearning.utopia.service.parent.homework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * 作业结果详情dao
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-09
 */
@Named
@CacheBean(type = HomeworkProcessResult.class, useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.OTHER_FIELDS)
public class HomeworkProcessResultDao extends AlpsDynamicMongoDao<HomeworkProcessResult, String> {

    /**
     * 缓存维度
     *
     * @param document
     * @param dimensions
     */
    @Override
    protected void calculateCacheDimensions(HomeworkProcessResult document, Collection<String> dimensions) {
        dimensions.add(document.ckHomeworkResultId());
    }

    /**
     * 分库
     *
     * @param template
     * @param document
     * @return
     */
    @Override
    protected String calculateDatabase(String template, HomeworkProcessResult document) {
        return StringUtils.formatMessage(template, HomeworkUtil.yyyyMM(document.getId()));
    }

    /**
     * 分表
     *
     * @param template
     * @param document
     * @return
     */
    @Override
    protected String calculateCollection(String template, HomeworkProcessResult document) {
        return StringUtils.formatMessage(template, HomeworkUtil.yyyyMMdd(document.getId()));
    }

    /**
     * 查询作业结果详情
     *
     * @param homeworkResultId 作业结果id
     * @return 作业结果详情列表
     */
    @CacheMethod
    public List<HomeworkProcessResult> loadHomeworkProcessResult(@CacheParameter("homeworkResultId") String homeworkResultId){
        //获取DB连接
        MongoConnection mongoConnection = this.createMongoConnection(calculateIdMongoNamespace(homeworkResultId));
        //查询
        Criteria criteria = Criteria.where("homeworkResultId").is(homeworkResultId);
        List<HomeworkProcessResult> homeworkProcessResults = executeQuery(mongoConnection, Query.query(criteria));
        return homeworkProcessResults;
    }

}
