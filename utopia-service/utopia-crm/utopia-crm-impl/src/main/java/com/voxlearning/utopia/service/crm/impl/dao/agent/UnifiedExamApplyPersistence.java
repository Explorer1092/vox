package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by dell on 2017/4/17.
 */
@Named
@CacheBean(type = UnifiedExamApply.class)
public class UnifiedExamApplyPersistence extends AlpsStaticJdbcDao<UnifiedExamApply,Long> {

    @Override
    protected void calculateCacheDimensions(UnifiedExamApply document, Collection<String> dimensions) {
        dimensions.add(UnifiedExamApply.ck_wid(document.getWorkflowId()));
        dimensions.add(UnifiedExamApply.ck_platform_uid(document.getUserPlatform(), document.getAccount()));
    }


    //标注方法 使用缓存 表明缓存使用的key 组成 元素
    @CacheMethod
    public UnifiedExamApply loadByWorkflowId(@UtopiaCacheKey(name = "wid")Long workflowId){
        Criteria criteria = Criteria.where("WORKFLOW_ID").is(workflowId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
    @CacheMethod
    public List<UnifiedExamApply> findByUser(@CacheParameter("platform") SystemPlatformType userPlatform,@CacheParameter("uid") String account){
        Criteria criteria = Criteria.where("USER_PLATFORM").is(userPlatform).and("ACCOUNT").is(account);
        return query(Query.query(criteria));
    }
    public List<UnifiedExamApply> findByCreateTime(Date startDate, Date endDate){
        if(startDate == null || endDate == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("CREATE_DATETIME").gte(startDate).lt(endDate);
        return query(Query.query(criteria));
    }
}
