package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackCategory;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by yaguang.wang
 * on 2017/2/21.
 */
@Named
@UtopiaCacheSupport(AgentProductFeedback.class)
public class AgentProductFeedbackPersistence extends AlpsStaticJdbcDao<AgentProductFeedback, Long> {
    @Override
    protected void calculateCacheDimensions(AgentProductFeedback document, Collection<String> dimensions) {
        dimensions.add(AgentProductFeedback.ck_wid(document.getWorkflowId()));
        dimensions.add(AgentProductFeedback.ck_platform_uid(document.getUserPlatform(), document.getAccount()));
    }

    public List<AgentProductFeedback> findByTeacherId(Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        return query(Query.query(criteria));
    }

    public Boolean upsertProductFeedbackImage(Long id, AgentProductFeedback image){
        if(id == null){
            return false;
        }
        Update update = new Update();
        if(image != null){
            update.set("PIC1_URL", image);
        }else {
            update.unset("PIC1_URL");
        }
        Criteria criteria = Criteria.where("ID").is(id);
        long count = $update(update,criteria);
        if(count < 1){
            return false;
        }
        evictDocumentCache(query(Query.query(criteria)));
        return true;
    }


    @CacheMethod
    public AgentProductFeedback findByWorkflowId(@CacheParameter("wid") Long workflowId) {
        Criteria criteria = Criteria.where("WORKFLOW_ID").is(workflowId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<AgentProductFeedback> findByUser(@CacheParameter("platform") SystemPlatformType userPlatform, @CacheParameter("uid") String userAccount) {
        Criteria criteria = Criteria.where("USER_PLATFORM").is(userPlatform).and("ACCOUNT").is(userAccount);
        return query(Query.query(criteria));
    }

    public List<AgentProductFeedback> findByStatusAndUpdateTime(AgentProductFeedbackStatus feedbackStatus) {
        Criteria criteria = Criteria.where("FEEDBACK_STATUS").is(feedbackStatus);
        return query(Query.query(criteria));
    }

    public List<AgentProductFeedback> findByRelationCode(Long relationCode){
        Criteria criteria = Criteria.where("RELATION_CODE").is(relationCode);
        return query(Query.query(criteria));
    }

    public Boolean setRelationCode(Long id, Long relationCode){
        if(id == null){
            return false;
        }
        Update update = new Update();
        if(relationCode != null){
            update.set("RELATION_CODE", relationCode);
        }else {
            update.unset("RELATION_CODE");
        }
        Criteria criteria = Criteria.where("ID").is(id);
        long count = $update(update,criteria);
        if(count < 1){
            return false;
        }
        evictDocumentCache(query(Query.query(criteria)));
        return true;
    }

    public List<AgentProductFeedback> findFeedbackByCondition(Date startDate, Date endDate, AgentProductFeedbackSubject subject,
                                                              AgentProductFeedbackType type, AgentProductFeedbackStatus status,
                                                              AgentProductFeedbackCategory firstCategory, AgentProductFeedbackCategory secondCategory,
                                                              AgentProductFeedbackCategory thirdCategory, String pmAccount, Boolean onlineFlag,
                                                              String content, String feedbackPeople, String teacherName, List<Long> teacherIds,
                                                              Integer page, Integer pageSize, String feedbackPeopleId) {
        Criteria criteria = new Criteria();
        smartFilter(criteria, "CREATE_DATETIME", startDate, endDate);
        filterIs(criteria, "TEACHER_SUBJECT", subject);
        filterIs(criteria, "FEEDBACK_TYPE", type);
        filterIs(criteria, "FIRST_CATEGORY", firstCategory);
        filterIs(criteria, "SECOND_CATEGORY", secondCategory);
        filterIs(criteria, "THIRD_CATEGORY", thirdCategory);
        filterIs(criteria, "PM_ACCOUNT", pmAccount);
        if(onlineFlag != null) {
            if(onlineFlag) {
                filterIs(criteria, "ONLINE_FLAG", true);
            }else{
                filterNe(criteria, "ONLINE_FLAG", true);
            }
        }
//        if(callback != null) {
//            if(callback) {
//                filterIs(criteria, "CALLBACK", true);
//            }else{
//                filterNe(criteria, "CALLBACK", true);
//            }
//        }
        filterIs(criteria, "FEEDBACK_STATUS", status);
        filterIs(criteria, "ACCOUNT", feedbackPeopleId);
        filterRegex(criteria, "CONTENT", content);
        filterRegex(criteria, "ACCOUNT_NAME", feedbackPeople);
        filterRegex(criteria, "TEACHER_NAME", teacherName);
        filterIn(criteria, "TEACHER_ID", teacherIds);
        Query query = Query.query(criteria);
        if (page != null && pageSize != null) {
            query.limit(pageSize).skip((page - 1) * (pageSize));
        }
        Sort sort = new Sort(Sort.Direction.DESC, "ID");
        query.with(sort);
        return query(query);
    }

    public Long findFeedbackByConditionCount(Date startDate, Date endDate, AgentProductFeedbackSubject subject,
                                             AgentProductFeedbackType type, AgentProductFeedbackStatus status,
                                             AgentProductFeedbackCategory firstCategory, AgentProductFeedbackCategory secondCategory,
                                             AgentProductFeedbackCategory thirdCategory, String pmAccount, Boolean onlineFlag,
                                             String content, String feedbackPeople, String teacherName, List<Long> teacherIds, String feedbackPeopleId) {
        Criteria criteria = new Criteria();
        smartFilter(criteria, "CREATE_DATETIME", startDate, endDate);
        filterIs(criteria, "TEACHER_SUBJECT", subject);
        filterIs(criteria, "FEEDBACK_TYPE", type);
        filterIs(criteria, "FIRST_CATEGORY", firstCategory);
        filterIs(criteria, "SECOND_CATEGORY", secondCategory);
        filterIs(criteria, "THIRD_CATEGORY", thirdCategory);
        filterIs(criteria, "PM_ACCOUNT", pmAccount);
        if(onlineFlag != null) {
            if(onlineFlag) {
                filterIs(criteria, "ONLINE_FLAG", true);
            }else{
                filterNe(criteria, "ONLINE_FLAG", true);
            }
        }
//        if(callback != null) {
//            if(callback) {
//                filterIs(criteria, "CALLBACK", true);
//            }else{
//                filterNe(criteria, "CALLBACK", true);
//            }
//        }
        filterIs(criteria, "FEEDBACK_STATUS", status);
        filterIs(criteria, "ACCOUNT", feedbackPeopleId);
        filterRegex(criteria, "CONTENT", content);
        filterRegex(criteria, "ACCOUNT_NAME", feedbackPeople);
        filterRegex(criteria, "TEACHER_NAME", teacherName);
        filterIn(criteria, "TEACHER_ID", teacherIds);
        return count(Query.query(criteria));
    }

    private void smartFilter(Criteria criteria, String key, Object foot, Object top) {
        if (foot != null && top != null) {
            criteria.and(key).gte(foot).lt(top);
        } else if (foot != null) {
            criteria.and(key).gte(foot);
        } else if (top != null) {
            criteria.and(key).lt(top);
        }
    }

    private void filterIs(Criteria criteria, String key, Object value) {
        if (value != null) {
            criteria.and(key).is(value);
        }
    }

    private void filterNe(Criteria criteria, String key, Object value) {
        if (value != null) {
            criteria.and(key).ne(value);
        }
    }

    private void filterRegex(Criteria criteria, String key, String value) {
        if (StringUtils.isNoneBlank(value)) {
            criteria.and(key).like("%" + value + "%");
        }
    }

    private void filterIn(Criteria criteria, String key, Collection values) {
        if (CollectionUtils.isNotEmpty(values)) {
            criteria.and(key).in(values);
        }
    }
}
