package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherVocationLottery;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

@Named
@CacheBean(type = TeacherVocationLottery.class)
public class TeacherVocationLotteryDao extends AlpsStaticJdbcDao<TeacherVocationLottery,Long> {

    @Override
    protected void calculateCacheDimensions(TeacherVocationLottery document, Collection<String> dimensions) {
        dimensions.add(TeacherVocationLottery.ck_teacher_id(document.getTeacherId()));
    }

    @CacheMethod
    public TeacherVocationLottery loadByTeacherId(@CacheParameter("TEACHER_ID") Long teacherId){
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public boolean incMultiFields(Long teacherId,Map<String,Object> fieldDeltaMap){
        TeacherVocationLottery org = loadByTeacherId(teacherId);
        if(org == null)
            return false;

        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);

        Update update = new Update();
        fieldDeltaMap.forEach((field,delta) -> update.inc(field, SafeConverter.toInt(delta)));

        long effectRows = $update(update,criteria);
        if(effectRows <= 0)
            return false;

        evictDocumentCache(org);
        return true;
    }
}
