package com.voxlearning.utopia.service.mizar.impl.dao.settlement;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolRefundAmortizeData;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SchoolRefundAmortizeDataDao
 *
 * @author song.wang
 * @date 2017/6/23
 */
@Named
@UtopiaCacheSupport(SchoolRefundAmortizeData.class)
public class SchoolRefundAmortizeDataDao extends StaticCacheDimensionDocumentMongoDao<SchoolRefundAmortizeData, String> {

    @CacheMethod
    public List<SchoolRefundAmortizeData> loadBySchoolId(@CacheParameter("sid")Long schoolId, @CacheParameter("month")Integer month){
        Criteria criteria = Criteria.where("schoolId").is(schoolId).and("month").is(month).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<SchoolRefundAmortizeData>> loadBySchoolIds(@CacheParameter(value = "sid", multiple = true)Collection<Long> schoolIds, @CacheParameter("month")Integer month){
        Criteria criteria = Criteria.where("schoolId").in(schoolIds).and("month").is(month).and("disabled").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(SchoolRefundAmortizeData::getSchoolId));
    }

    public void disableByMonth(Collection<Long> schoolIds, Integer month){
        Criteria criteria = Criteria.where("schoolId").in(schoolIds).and("month").is(month).and("disabled").is(false);
        List<SchoolRefundAmortizeData> result = query(Query.query(criteria));
        if(CollectionUtils.isNotEmpty(result)){
            result.forEach(p -> {
                p.setDisabled(true);
                replace(p);
            });
        }
    }
}
