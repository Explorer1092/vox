package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolDetail;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2015/12/31.
 */
@Named
public class CrmUGCSchoolDetailDao extends StaticMongoDao<CrmUGCSchoolDetail, String> {


    @Override
    protected void calculateCacheDimensions(CrmUGCSchoolDetail source, Collection<String> dimensions) {
    }

    public List<CrmUGCSchoolDetail> ugcSchoolDetail(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        Sort sort = new Sort(Sort.Direction.DESC, "percentage");
        return find(filter, sort);
    }

    private List<CrmUGCSchoolDetail> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort).limit(5));
    }

    public long getUgcSchoolDetailCount() {
        return __count_OTF();
    }

    public List<CrmUGCSchoolDetail> allUgcSchoolDetailData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

}
