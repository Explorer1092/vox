/**
 * Author:   xianlong.zhang
 * Date:     2018/9/18 15:24
 * Description: 考试分配人员对象
 * History:
 */
package com.voxlearning.utopia.agent.dao.mongo.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamUserInfo;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Named
@CacheBean(type = AgentExamUserInfo.class)
public class AgentExamUserInfoDao   extends StaticCacheDimensionDocumentMongoDao<AgentExamUserInfo, String> {
    @CacheMethod
    public List<AgentExamUserInfo> loadByExamId(@CacheParameter(value = "examId") String examId){
        Criteria criteria = Criteria.where("examId").is(examId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public AgentExamUserInfo loadByExamIdAndUserId(@CacheParameter(value = "examId") String examId,@CacheParameter(value = "userId") Long userId){
        Criteria criteria = Criteria.where("examId").is(examId).and("createTime").gte(DateUtils.calculateDateDay(new Date(),-60));
        criteria.and("userId").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return query(Query.query(criteria).with(sort)).stream().findFirst().orElse(null);
    }

    public Page<AgentExamUserInfo> findExamListByUserIdsPage(Collection<Long> userIds, Collection<String> exams, Integer grade, Pageable pageable) {
        Criteria criteria = Criteria.where("disabled").is(false).and("createTime").gte(DateUtils.calculateDateDay(new Date(),-60));
        criteria.and("userId").in(userIds);
        if(CollectionUtils.isNotEmpty(exams)){
            criteria.and("examId").in(exams);
        }
        if(grade != null && grade > 0){
            criteria.and("grade").is(grade);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query =  Query.query(criteria).with(pageable).with(sort);
        return new PageImpl<>(query(query.with(pageable)), pageable, count(query));
    }
}
