package com.voxlearning.utopia.service.mizar.impl.dao.settlement;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SchoolSettlementDao
 *
 * @author song.wang
 * @date 2017/6/23
 */
@Named
@UtopiaCacheSupport(SchoolSettlement.class)
public class SchoolSettlementDao extends StaticCacheDimensionDocumentMongoDao<SchoolSettlement, String> {

    @CacheMethod
    public SchoolSettlement loadBySchoolId(@CacheParameter("sid")Long schoolId, @CacheParameter("month")Integer month){
        Criteria criteria = Criteria.where("schoolId").is(schoolId).and("month").is(month).and("disabled").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, SchoolSettlement> loadBySchoolIds(@CacheParameter(value = "sid", multiple = true)Collection<Long> schoolIds, @CacheParameter("month")Integer month){
        Criteria criteria = Criteria.where("schoolId").in(schoolIds).and("month").is(month).and("disabled").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(SchoolSettlement::getSchoolId, Function.identity(), (o1, o2) -> o1));
    }

    @CacheMethod
    public Map<Integer, SchoolSettlement> loadByMonths(@CacheParameter(value = "sid")Long schoolId, @CacheParameter(value = "month", multiple = true)Collection<Integer> months){
        Criteria criteria = Criteria.where("schoolId").is(schoolId).and("month").in(months).and("disabled").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(SchoolSettlement::getMonth, Function.identity(), (o1, o2) -> o1));
    }

    public void disableByMonth(Collection<Long> schoolIds, Integer month){
        Criteria criteria = Criteria.where("schoolId").in(schoolIds).and("month").is(month).and("disabled").is(false);
        List<SchoolSettlement> result = query(Query.query(criteria));
        if(CollectionUtils.isNotEmpty(result)){
            result.forEach(p -> {
                p.setDisabled(true);
                replace(p);
            });
        }
    }
}
