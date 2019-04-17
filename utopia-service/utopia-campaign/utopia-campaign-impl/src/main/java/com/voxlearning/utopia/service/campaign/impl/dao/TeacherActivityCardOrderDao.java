package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCardOrder;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class TeacherActivityCardOrderDao extends AlpsStaticMongoDao<TeacherActivityCardOrder, String> {
    @Override
    protected void calculateCacheDimensions(TeacherActivityCardOrder document, Collection<String> dimensions) {

    }

    public List<TeacherActivityCardOrder> loadAll() {
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return query(Query.query(new Criteria()).with(sort));
    }
}
