package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolTask;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2016/1/7
 */
@Named
public class CrmUGCSchoolTaskDao extends StaticMongoDao<CrmUGCSchoolTask, String> {

    @Override
    protected void calculateCacheDimensions(CrmUGCSchoolTask source, Collection<String> dimensions) {
    }

    public List<CrmUGCSchoolTask> findExecutorIdIs(Long executorId) {
        Filter filter = filterBuilder.where("executorId").is(executorId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return find(filter, sort);
    }

    public long countUnfinished(Long executorId) {
        Filter filter = filterBuilder.where("executorId").is(executorId).and("finished").ne(true);
        return count(filter);
    }

    private List<CrmUGCSchoolTask> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }

    private long count(Filter filter) {
        return __count_OTF(filter.toBsonDocument());
    }
}
