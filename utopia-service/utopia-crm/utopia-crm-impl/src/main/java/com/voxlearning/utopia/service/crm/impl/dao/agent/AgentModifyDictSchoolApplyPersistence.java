package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * AgentModifyDictSchoolApplyPersistence
 *
 * @author song.wang
 * @date 2016/12/28
 */
@Named
@UtopiaCacheSupport(AgentModifyDictSchoolApply.class)
public class AgentModifyDictSchoolApplyPersistence extends AlpsStaticJdbcDao<AgentModifyDictSchoolApply, Long> {
    @Override
    protected void calculateCacheDimensions(AgentModifyDictSchoolApply source, Collection<String> dimensions) {
        dimensions.add(AgentModifyDictSchoolApply.ck_id(source.getSchoolId()));
        dimensions.add(AgentModifyDictSchoolApply.ck_wid(source.getWorkflowId()));
        dimensions.add(AgentModifyDictSchoolApply.ck_uid_status(source.getAccount(), source.getStatus()));
        dimensions.add(AgentModifyDictSchoolApply.ck_platform_uid(source.getUserPlatform(), source.getAccount()));
        //dimensions.add(AgentModifyDictSchoolApply.ck_s_r(source.getStatus(),source.getResolved()));
    }

    @CacheMethod
    public List<AgentModifyDictSchoolApply> findBySchoolId(@CacheParameter("sid") Long schoolId) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public AgentModifyDictSchoolApply findByWorkflowId(@CacheParameter("wid") Long workflowId) {
        Criteria criteria = Criteria.where("WORKFLOW_ID").is(workflowId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public List<AgentModifyDictSchoolApply> findByStatusAndResolved(ApplyStatus status, Boolean resolved) {
        Criteria criteria = Criteria.where("STATUS").is(status).and("RESOLVED").is(resolved);
        return query(Query.query(criteria));
    }

    public List<AgentModifyDictSchoolApply> findByDate(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("CREATE_DATETIME").gte(startDate).lt(endDate);
        return query(Query.query(criteria));
    }

    public List<AgentModifyDictSchoolApply> findByUpdateDate(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("UPDATE_DATETIME").gte(startDate).lt(endDate);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentModifyDictSchoolApply> findByUser(@CacheParameter("platform") SystemPlatformType userPlatform, @CacheParameter("uid") String userAccount) {
        Criteria criteria = Criteria.where("USER_PLATFORM").is(userPlatform).and("ACCOUNT").is(userAccount);
        return query(Query.query(criteria));
    }

}
