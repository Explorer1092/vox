package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ChipsClazzCompare;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


@Named
@CacheBean(type = ChipsClazzCompare.class)
public class ChipsClazzCompareDao extends AlpsStaticMongoDao<ChipsClazzCompare, String> {

    @Override
    protected void calculateCacheDimensions(ChipsClazzCompare chipsClazzCompare, Collection<String> collection) {

    }

    public List<ChipsClazzCompare> loadByPage(int pageNum, int pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        return query(Query.query(new Criteria()).with(sort).with(pageable));
    }

    public long count() {
        return count(Query.query(new Criteria()));
    }
}
