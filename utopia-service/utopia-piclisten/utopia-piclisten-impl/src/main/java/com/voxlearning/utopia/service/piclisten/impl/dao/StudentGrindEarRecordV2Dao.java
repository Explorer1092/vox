package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRecordV2;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * 第二去磨耳朵不哦懂dao
 *
 * @author jiangpeng
 * @since 2017-01-22 下午1:09
 **/
@Named
public class StudentGrindEarRecordV2Dao extends AlpsStaticMongoDao<StudentGrindEarRecordV2, String> {
    @Override
    protected void calculateCacheDimensions(StudentGrindEarRecordV2 document, Collection<String> dimensions) {

    }

    public List<StudentGrindEarRecordV2> loadAll(){
        Criteria criteria = Criteria.where("_id").exists();
        return query(Query.query(criteria));
    }

}
