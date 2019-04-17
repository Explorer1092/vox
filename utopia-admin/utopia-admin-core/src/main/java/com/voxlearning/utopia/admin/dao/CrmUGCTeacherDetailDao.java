package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCTeacherDetail;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/28.
 */

@Named
public class CrmUGCTeacherDetailDao extends StaticMongoDao<CrmUGCTeacherDetail,String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCTeacherDetail source, Collection<String> dimensions) {

    }
    public List<CrmUGCTeacherDetail> findUgcTeacherDetailTop5(Long groupId) {

        Filter filter = filterBuilder.where("clazzId").is(groupId);
        Sort sort = new Sort(Sort.Direction.DESC, "percentage");
        return __find_OTF(Find.find(filter).with(sort).limit(5));
    }

    public long getUgcTeacherDetailCount() {
        return __count_OTF();
    }

    public List<CrmUGCTeacherDetail> allUgcTeacherDetails(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

}
