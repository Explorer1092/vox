package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.CrmReviewResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * CrmReviewResultDao
 *
 * @author song.wang
 * @date 2016/7/6
 */
@Named
public class CrmReviewResultDao extends StaticMongoDao<CrmReviewResult, String> {
    @Override
    protected void calculateCacheDimensions(CrmReviewResult source, Collection<String> dimensions) {

    }

    public List<CrmReviewResult> findAll(){
        Filter filter = filterBuilder.build();
        return find(filter);
    }

    public List<CrmReviewResult> findBySchoolId(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return find(filter, sort);

    }

    public CrmReviewResult findLastestBySchoolId(Long schoolId) {
        List<CrmReviewResult> list = findBySchoolId(schoolId);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public List<CrmReviewResult> findLastestBySchoolIds(Collection<Long> schoolIds) {
        Filter filter = filterBuilder.where("schoolId").in(schoolIds);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return find(filter, sort);
    }

    private void filterIs(Filter filter, String key, Object value) {
        if (value != null) {
            filter.and(key).is(value);
        }
    }

    private void filterIn(Filter filter, String key, Collection<?> values) {
        if (values != null) {
            filter.and(key).in(values);
        }
    }

    private List<CrmReviewResult> find(Filter filter) {
        return __find_OTF(Find.find(filter));
    }

    private List<CrmReviewResult> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }

}
