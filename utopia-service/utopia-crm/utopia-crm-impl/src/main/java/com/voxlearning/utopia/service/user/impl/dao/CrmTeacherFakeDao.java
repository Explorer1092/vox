package com.voxlearning.utopia.service.user.impl.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.api.constant.ReviewStatus;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/12/2
 */
@Named("user.CrmTeacherFakeDao")
public class CrmTeacherFakeDao extends StaticMongoDao<CrmTeacherFake, String> {

    @Override
    protected void calculateCacheDimensions(CrmTeacherFake source, Collection<String> dimensions) {
    }

    public List<CrmTeacherFake> findTeacherIdIs(Long teacherId) {
        Filter filter = filterBuilder.where("teacherId").is(teacherId);
        return find(filter);
    }

    public List<CrmTeacherFake> findFakedTeacher(Long teacherId) {
        Filter filter = filterBuilder.where("reviewStatus").ne(ReviewStatus.REJECT);
        filterIs(filter, "teacherId", teacherId);
        return find(filter);
    }

    public List<CrmTeacherFake> findFakerIdIs(Long fakerId) {
        Filter filter = filterBuilder.where("fakerId").is(fakerId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return find(filter, sort);
    }

    public Page<CrmTeacherFake> smartFind(ReviewStatus reviewStatus, Long teacherId, Pageable pageable) {
        Filter filter = filterBuilder.build();
        filterIs(filter, "reviewStatus", reviewStatus);
        filterIs(filter, "teacherId", teacherId);
        return find(filter, pageable);
    }

    private void filterIs(Filter filter, String key, Object value) {
        if (value != null) {
            filter.and(key).is(value);
        }
    }

    private List<CrmTeacherFake> find(Filter filter) {
        return __find_OTF(Find.find(filter));
    }

    private List<CrmTeacherFake> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }

    private Page<CrmTeacherFake> find(Filter filter, Pageable pageable) {
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }
}
