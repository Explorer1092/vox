/**
 * Author:   xianlong.zhang
 * Date:     2018/9/14 18:18
 * Description: 分配给专员记录表
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
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamDistribution;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@CacheBean(type = AgentExamDistribution.class)
public class AgentExamDistributionDao extends StaticCacheDimensionDocumentMongoDao<AgentExamDistribution, String> {

    public Page<AgentExamDistribution> findUserExamListPage(Collection<Integer> countys,Integer type , Collection<Long> schoolIds, Integer grade, Pageable pageable) {
        Criteria criteria = Criteria.where("disabled").is(false).and("distributionState").is(true);
        if(type != null && type == 3){
            criteria.and("evaluateState").is(false);
        }
        if(CollectionUtils.isNotEmpty(schoolIds)){
            criteria.and("schoolId").in(schoolIds);
        }
        if(grade != null && grade > 0){
            criteria.and("grade").is(grade);
        }
        if(countys.size() > 0 ){
            criteria.and("regionCode").in(countys);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "examCreateDate");
        Query query =  Query.query(criteria).with(pageable).with(sort);
        return new PageImpl<>(query(query.with(pageable)), pageable, count(query));
    }
    @CacheMethod
    public List<AgentExamDistribution> findByExamId(@CacheParameter(value = "examId") String examId){
        Criteria criteria = Criteria.where("examId").is(examId).and("disabled").is(false);
        Query query =  Query.query(criteria);
        return query(query);
    }
    @CacheMethod
    public Map<String,List<AgentExamDistribution>> findByExamIds(@CacheParameter(value = "examId",multiple = true) Collection<String> examIds){
        Criteria criteria = Criteria.where("examId").in(examIds).and("disabled").is(false);
        Query query =  Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(AgentExamDistribution::getExamId));
    }

    public List<AgentExamDistribution> findUserExamList(Collection<Integer> countys,Integer type , Collection<Long> schoolIds, Integer grade) {
        Criteria criteria = Criteria.where("disabled").is(false).and("distributionState").is(true).and("examCreateDate").gte(DateUtils.calculateDateDay(new Date(),-60));
        if(type != null && type == 3){
            criteria.and("evaluateState").is(false);
        }
        if(CollectionUtils.isNotEmpty(schoolIds)){
            criteria.and("schoolId").in(schoolIds);
        }
        if(grade != null && grade > 0){
            criteria.and("grade").is(grade);
        }
        if(countys.size() > 0 ){
            criteria.and("regionCode").in(countys);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "examCreateDate");
        Query query =  Query.query(criteria).with(sort);
        return query(query);
    }
}
