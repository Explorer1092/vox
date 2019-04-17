package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentTask;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Jia HuanYin
 * @since 2015/11/25
 */
@Named
public class AgentTaskDao extends StaticMongoDao<AgentTask, String> {

    @Override
    protected void calculateCacheDimensions(AgentTask source, Collection<String> dimensions) {
    }

    public List<AgentTask> findCreaterIdIs(Long createrId, Date createStart, Date createEnd) {
        Filter filter = filterBuilder.where("createrId").is(createrId).and("disabled").is(false);
        smartFilter(filter, "createTime", createStart, createEnd);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return find(filter, sort);
    }

    private void smartFilter(Filter filter, String key, Object foot, Object top) {
        if (foot != null && top != null) {
            filter.and(key).gte(foot).lt(top);
        } else if (foot != null) {
            filter.and(key).gte(foot);
        } else if (top != null) {
            filter.and(key).lt(top);
        }
    }

    private List<AgentTask> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }
}
