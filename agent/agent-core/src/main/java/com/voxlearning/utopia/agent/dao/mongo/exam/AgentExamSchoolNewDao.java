/**
 * Author:   xianlong.zhang
 * Date:     2018/9/14 15:49
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.dao.mongo.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamSchoolNew;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

@Named
@CacheBean(type = AgentExamSchoolNew.class)
public class AgentExamSchoolNewDao  extends StaticCacheDimensionDocumentMongoDao<AgentExamSchoolNew, String> {

    public Page<AgentExamSchoolNew> findAllExamListPage(Collection<Integer> countys,Integer type, String name, Integer grade, Pageable pageable) {

        Criteria criteria = Criteria.where("createDateTime").gte(DateUtils.calculateDateDay(new Date(),-60));
        if(StringUtils.isNotBlank(name)){
            criteria.and("NAME").regex(Pattern.compile(".*"+name+".*"));
        }
        if(type == 2){
            criteria.and("distributionState").is(false);
        }
        if(grade != null && grade > 0){
            criteria.and("GRADE").is(grade);
        }
//        if(CollectionUtils.isNotEmpty(countys)){
//            criteria.and("regionCode").in(countys);
//        }
        Sort sort = new Sort(Sort.Direction.DESC, "createDateTime");
        Query query =  Query.query(criteria).with(pageable).with(sort);
        return new PageImpl<>(query(query.with(pageable)), pageable, count(query));
    }

    //数据在考试端插入
    public AgentExamSchoolNew findExamByExamId(String examId){
        Criteria criteria = Criteria.where("examId").is(examId);
        Query query =  Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }
}
