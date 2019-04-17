package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserUnitResultOperationLog;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Named
@CacheBean(type = ChipsEnglishUserUnitResultOperationLog.class)
public class ChipsEnglishUserUnitResultOperationLogDao extends AlpsStaticMongoDao<ChipsEnglishUserUnitResultOperationLog, String> {
    @Override
    protected void calculateCacheDimensions(ChipsEnglishUserUnitResultOperationLog document, Collection<String> dimensions) {
        dimensions.add(ChipsEnglishUserUnitResultOperationLog.ck_user_id(document.getUserId()));
    }

    @CacheMethod
    public List<ChipsEnglishUserUnitResultOperationLog> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }

    /**
     * 插入或更新单元成绩备注
     *
     * @param userId
     * @param unitId
     * @param notes
     * @return
     */
    public void upsert(Long userId, String unitId, String notes) {
        Criteria criteria = Criteria.where("userId").is(userId).and("unitId").is(unitId);

        ChipsEnglishUserUnitResultOperationLog chipsEnglishUserUnitResultOperationLog = Optional.ofNullable(query(Query.query(criteria)))
                .filter(l -> l.size() > 0)
                .map(l -> l.get(0))
                .orElse(null);

        if (chipsEnglishUserUnitResultOperationLog == null) {
            chipsEnglishUserUnitResultOperationLog = new ChipsEnglishUserUnitResultOperationLog();
            chipsEnglishUserUnitResultOperationLog.setUserId(userId);
            chipsEnglishUserUnitResultOperationLog.setUnitId(unitId);
            chipsEnglishUserUnitResultOperationLog.setCreateTime(new Date());
        }
        chipsEnglishUserUnitResultOperationLog.setOperationLog(notes);

        upsert(chipsEnglishUserUnitResultOperationLog);
    }

}
