package com.voxlearning.utopia.service.mizar.impl.dao.change;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by xiang.lv on 2016/9/21.
 *
 * @author xiang.lv
 * @date 2016/9/21   11:52
 */
@Named
@CacheBean(type = MizarEntityChangeRecord.class)
public class MizarEntityChangeRecordDao extends AlpsStaticMongoDao<MizarEntityChangeRecord, String> {

    @Override
    protected void calculateCacheDimensions(MizarEntityChangeRecord document, Collection<String> dimensions) {
        dimensions.add(MizarEntityChangeRecord.ck_applicant(document.getApplicantId()));
        dimensions.add(MizarEntityChangeRecord.ck_entity(document.getEntityType()));
        dimensions.add(MizarEntityChangeRecord.ck_id(document.getId()));
    }

    @CacheMethod
    public List<MizarEntityChangeRecord> loadByApplicant(@CacheParameter(value = "APP") String applicantId) {
        if (StringUtils.isBlank(applicantId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("applicantId").is(applicantId);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<MizarEntityChangeRecord> loadByStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("auditStatus").is(status);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<MizarEntityChangeRecord> loadByEntityType(@CacheParameter(value = "T") String entityType) {
        if (StringUtils.isBlank(entityType)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("entityType").is(entityType);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<MizarEntityChangeRecord> loadByTarget(String targetId, String entityType) {
        if (StringUtils.isBlank(targetId) || StringUtils.isBlank(entityType)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("entityType").is(entityType).and("targetId").is(targetId);
        return query(Query.query(criteria));
    }


}
