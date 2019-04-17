package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGradeDetail;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/18.
 */

@Named
public class CrmUGCGradeDetailDao extends StaticMongoDao<CrmUGCGradeDetail, String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCGradeDetail source, Collection<String> dimensions) {

    }
    public List<CrmUGCGradeDetail> findUGCGradeDetailTop5(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        Sort sort = new Sort(Sort.Direction.DESC, "percentage");
        return __find_OTF(Find.find(filter).with(sort).limit(5));
    }


    public long getUgcGradeDetailCount() {
        return __count_OTF();
    }

    public List<CrmUGCGradeDetail> allUgcGradeDetailData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }


}
