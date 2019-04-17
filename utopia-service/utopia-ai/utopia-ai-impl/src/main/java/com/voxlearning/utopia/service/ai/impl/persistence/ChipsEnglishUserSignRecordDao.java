package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserSignRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


@Named
@CacheBean(type = ChipsEnglishUserSignRecord.class)
public class ChipsEnglishUserSignRecordDao extends AlpsStaticMongoDao<ChipsEnglishUserSignRecord, String> {

    @Override
    protected void calculateCacheDimensions(ChipsEnglishUserSignRecord document, Collection<String> dimensions) {
        dimensions.add(ChipsEnglishUserSignRecord.ck_user_id(document.getUserId()));
    }


    @Override
    public ChipsEnglishUserSignRecord load(String id) {
        return super.$load(id);
    }


    @CacheMethod
    public List<ChipsEnglishUserSignRecord> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }
}
