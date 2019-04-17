package com.voxlearning.utopia.service.crm.impl.dao.crm;

/**
 * @author fugui.chang
 * @since 2016/11/22
 */

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

@Named
@CacheBean(type = WechatWfMessage.class)
public class WechatWfMessagePersistence extends AlpsStaticJdbcDao<WechatWfMessage, Long> {
    @Override
    protected void calculateCacheDimensions(WechatWfMessage document, Collection<String> dimensions) {
    }

    public int updateWechatWfMessageStatusByRecordId(Long recordId, String status) {
        if (recordId == null || StringUtils.isBlank(status)) {
            return 0;
        }

        Criteria criteria = Criteria.where("RECORD_ID").is(recordId);
        Update update = Update.update("STATUS", status).set("UPDATE_DATETIME", new Date());
        return (int) $update(update, criteria);
    }

    public WechatWfMessage loadByRecordId(Long recordId) {
        if(recordId == null){
            return null;
        }
        Criteria criteria = Criteria.where("RECORD_ID").is(recordId).and("DISABLED").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
