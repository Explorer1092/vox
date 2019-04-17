package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.activity.SudokuDayQuestion;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = SudokuDayQuestion.class)
public class SudokuDayQuestionDao extends AlpsStaticMongoDao<SudokuDayQuestion, String> {

    @Override
    protected void calculateCacheDimensions(SudokuDayQuestion document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public List<SudokuDayQuestion> loadByActivityId(@CacheParameter("AID") String activityId) {
        Criteria criteria = Criteria.where("activityId").is(activityId);
        return query(Query.query(criteria));
    }

}
