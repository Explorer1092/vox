package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.activity.MathActivityRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganhaitian on 2018/4/1.
 */
@Named
public class MathActivityRecordDao extends AlpsStaticJdbcDao<MathActivityRecord,Long>{

    @Override
    protected void calculateCacheDimensions(MathActivityRecord document, Collection<String> dimensions) {
        dimensions.add(MathActivityRecord.ck_teacherId(document.getTeacherId()));
    }

    @CacheMethod
    public List<MathActivityRecord> loadByPhaseAndClazz(@CacheParameter("phase") Integer phase,
                                                        @CacheParameter("clazz") Integer clazz,
                                                        int limit){
        // 排行榜只看rank非空的记录
        Criteria criteria = Criteria.where("phase").is(phase)
                .and("clazz").is(clazz)
                .and("rank").gt(0);

        Sort sort = new Sort(Sort.Direction.ASC,"rank");
        return query(Query.query(criteria).with(sort).limit(limit));
    }

    @CacheMethod
    public List<MathActivityRecord> loadByTeacherId(@CacheParameter("teacherId") Long teacherId){
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        return query(Query.query(criteria));
    }

}
