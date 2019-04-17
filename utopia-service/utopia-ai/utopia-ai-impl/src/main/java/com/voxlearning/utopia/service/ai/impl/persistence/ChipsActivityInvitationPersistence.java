package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsActivityInvitation;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsActivityInvitation.class)
public class ChipsActivityInvitationPersistence extends StaticMySQLPersistence<ChipsActivityInvitation, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsActivityInvitation document, Collection<String> dimensions) {
        dimensions.add(ChipsActivityInvitation.ck_inviter(document.getInviter()));
        dimensions.add(ChipsActivityInvitation.ck_type_inviter(document.getActivityType(), document.getInviter()));
    }

    @Override
    public ChipsActivityInvitation load(Long id) {
        return $load(id);
    }

    @CacheMethod
    public List<ChipsActivityInvitation> loadByActivityTypeAndInviter(@CacheParameter("TYPE") String activityType, @CacheParameter("INVIER") Long userId) {
        Criteria criteria = Criteria.where("DISABLED").is(false).and("ACTIVITY_TYPE").is(activityType).and("INVITER").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<ChipsActivityInvitation> loadByInviter(@CacheParameter("INVIER") Long userId) {
        Criteria criteria = Criteria.where("DISABLED").is(false).and("INVITER").is(userId);
        return query(Query.query(criteria));
    }

    public void inserOrUpdate(Long inviter, Long invitee, String type, Integer status) {
        ChipsActivityInvitation invitation = new ChipsActivityInvitation();
        invitation.setActivityType(type);
        invitation.setInvitee(invitee);
        invitation.setInviter(inviter);
        invitation.setStatus(status);
        invitation.setUpdateTime(new Date());
        invitation.setCreateTime(new Date());
        invitation.setDisabled(false);
        doInserOrUpdate(invitation);
    }
    private void doInserOrUpdate(ChipsActivityInvitation activityInvitation) {
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), activityInvitation, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0").append(",`STATUS`=").append(activityInvitation.getStatus());
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(activityInvitation);
        }
    }

    private void cleanCache(ChipsActivityInvitation userEntity) {
        if (userEntity == null) {
            return;
        }
        Set<String> cacheKeys = new HashSet<>();
        calculateCacheDimensions(userEntity, cacheKeys);
        getCache().delete(cacheKeys);
    }
}
