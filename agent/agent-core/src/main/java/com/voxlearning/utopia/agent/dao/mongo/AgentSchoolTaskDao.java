package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentSchoolTask;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/11/26
 */
@Named
public class AgentSchoolTaskDao extends StaticMongoDao<AgentSchoolTask, String> {

    @Override
    protected void calculateCacheDimensions(AgentSchoolTask source, Collection<String> dimensions) {
    }

    public List<AgentSchoolTask> findTaskIdIs(String taskId) {
        Filter filter = filterBuilder.where("taskId").is(taskId);
        return find(filter);
    }

    public List<AgentSchoolTask> findExecutorIdIs(Long executorId) {
        Filter filter = filterBuilder.where("executorId").is(executorId);
        Sort sort = new Sort(Sort.Direction.DESC, "pushed").and(new Sort(Sort.Direction.ASC, "endTime"));
        return find(filter, sort);
    }

    public long countUnfinished(Long executorId, Date endTime) {
        Filter filter = filterBuilder.where("executorId").is(executorId).and("finished").ne(true).and("endTime").gte(endTime);
        return count(filter);
    }

    public long countFinished(String taskId) {
        Filter filter = filterBuilder.where("taskId").is(taskId).and("finished").is(true);
        return count(filter);
    }

    private List<AgentSchoolTask> find(Filter filter) {
        return __find_OTF(filter.toBsonDocument());
    }

    private List<AgentSchoolTask> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }

    private long count(Filter filter) {
        return __count_OTF(filter.toBsonDocument());
    }
}
