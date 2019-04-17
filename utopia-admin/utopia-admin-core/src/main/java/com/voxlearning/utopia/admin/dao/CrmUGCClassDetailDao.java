package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCClassDetail;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/21.
 */

@Named
public class CrmUGCClassDetailDao extends StaticMongoDao<CrmUGCClassDetail, String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCClassDetail source, Collection<String> dimensions) {
    }
    public List<CrmUGCClassDetail> findUgcClassDetailTop5(Long groupId) {

        Filter filter = filterBuilder.where("groupId").is(groupId);
        Sort sort = new Sort(Sort.Direction.DESC, "percentage");
        return __find_OTF(Find.find(filter).with(sort).limit(5));
    }

    public long getUgcClassDetailCount() {
        return __count_OTF();
    }

    public List<CrmUGCClassDetail> allUgcClassDetails(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

}
