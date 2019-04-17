package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/7
 */
@Named
@CacheBean(type = DataReportApply.class)
public class DataReportApplyPersistence extends AlpsStaticJdbcDao<DataReportApply,Long> {
    @Override
    protected void calculateCacheDimensions(DataReportApply document, Collection<String> dimensions) {
        dimensions.add(DataReportApply.ck_wid(document.getWorkflowId()));
        dimensions.add(DataReportApply.ck_platform_uid(document.getUserPlatform(), document.getAccount()));
    }

    @CacheMethod
    public DataReportApply findByWorkflowId(@CacheParameter("wid")Long workflowId){
        Criteria criteria = Criteria.where("WORKFLOW_ID").is(workflowId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<DataReportApply> findByUser(@CacheParameter("platform")SystemPlatformType userPlatform, @CacheParameter("uid")String userAccount){
        Criteria criteria = Criteria.where("USER_PLATFORM").is(userPlatform).and("ACCOUNT").is(userAccount);
        return query(Query.query(criteria));
    }

    public List<DataReportApply> findByDate(Date startDate, Date endDate) {
        if(startDate == null || endDate == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("CREATE_DATETIME").gte(startDate).lt(endDate);
        return query(Query.query(criteria));
    }

}
