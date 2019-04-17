package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookOrderStat;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Named
public class PicBookOrderStatDao extends AlpsStaticMongoDao<PicBookOrderStat,String>{

    @Override
    protected void calculateCacheDimensions(PicBookOrderStat document, Collection<String> dimensions) {

    }

    public List<PicBookOrderStat> loadBySeriesId(String seriesId,Date start,Date end){
        Criteria criteria = Criteria.where("series_id").is(seriesId);

        String startStr = DateUtils.dateToString(start,DateUtils.FORMAT_SQL_DATE);
        String endStr = DateUtils.dateToString(end,DateUtils.FORMAT_SQL_DATE);
        // 由于mongo里面的usage_date这个字段是字符串的，不能用date
        if(start != null) {
            criteria.and("usage_date").gte(startStr);

            if(end != null)
                criteria.lte(endStr);
        }else if(end != null){
            criteria.and("usage_date").lte(endStr);
        }

        return query(Query.query(criteria));
    }
}
