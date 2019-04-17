package com.voxlearning.utopia.service.parent.homework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * 作业结果dao
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-09
 */
@Named
@CacheBean(type = HomeworkResult.class, useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class HomeworkResultDao extends AlpsDynamicMongoDao<HomeworkResult, String> {

    @Override
    protected void calculateCacheDimensions(HomeworkResult homeworkResult, Collection<String> dimensions) {
        dimensions.add(homeworkResult.getId());
    }

    @Override
    protected String calculateDatabase(String template, HomeworkResult document) {
        return StringUtils.formatMessage(template, HomeworkUtil.yyyyMM(document.getId()));
    }

    @Override
    protected String calculateCollection(String template, HomeworkResult document) {
        return StringUtils.formatMessage(template, HomeworkUtil.yyyyMMdd(document.getId()));
    }


    /**
     * 根据作业id、用户id查询作业结果
     *
     * @param homeworkId 作业id
     * @param userId 用户id
     * @return 作业结果列表
     */
    public List<HomeworkResult> loadHomeworkResults(String homeworkId, Long userId){
        //获取DB连接
        MongoConnection mongoConnection = this.createMongoConnection(calculateIdMongoNamespace(homeworkId));
        //查询
        Criteria criteria = Criteria.where("homeworkId").is(homeworkId)
                .and("userId").is(userId);
        List<HomeworkResult> result = executeQuery(mongoConnection, Query.query(criteria));
        return result;
    }

}
