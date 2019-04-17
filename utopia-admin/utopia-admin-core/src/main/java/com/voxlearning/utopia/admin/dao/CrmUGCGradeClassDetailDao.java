package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGradeClassDetail;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/21.
 */

@Named
public class CrmUGCGradeClassDetailDao extends StaticMongoDao<CrmUGCGradeClassDetail, String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCGradeClassDetail source, Collection<String> dimensions) {

    }
    public List<CrmUGCGradeClassDetail> findUGCGradeClassDetailTop5(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        Sort sort = new Sort(Sort.Direction.DESC, "percentage");
        return __find_OTF(Find.find(filter).with(sort).limit(5));
    }


    public long getUgcGradeClassDetailCount() {
        return __count_OTF();
    }

    public List<CrmUGCGradeClassDetail> allUgcGradeClassDetailData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

}
