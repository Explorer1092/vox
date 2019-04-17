package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsActivityInvitation;
import com.voxlearning.utopia.service.ai.entity.ChipsActivityInvitationVisit;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Named
@CacheBean(type = ChipsActivityInvitationVisit.class)
public class ChipsActivityInvitationVisitPersistence extends StaticMySQLPersistence<ChipsActivityInvitationVisit, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsActivityInvitationVisit document, Collection<String> dimensions) {
        dimensions.add(ChipsActivityInvitationVisit.ck_type_inviter(document.getActivityType(), document.getInviter()));
        dimensions.add(ChipsActivityInvitationVisit.ck_inviter(document.getInviter()));
    }


    @CacheMethod
    public List<ChipsActivityInvitationVisit> loadByActivityTypeAndUserId(@CacheParameter("TYPE") String activityType, @CacheParameter("INVIER") Long userId) {
        Criteria criteria = Criteria.where("DISABLED").is(false).and("ACTIVITY_TYPE").is(activityType).and("INVITER").is(userId);
        return query(Query.query(criteria));
    }


    @CacheMethod
    public List<ChipsActivityInvitationVisit> loadByUserId(@CacheParameter("INVIER") Long userId) {
        Criteria criteria = Criteria.where("INVITER").is(userId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }


    public void insert(String activityType, Long inviter, Long wechatUserId) {
        ChipsActivityInvitationVisit visit = new ChipsActivityInvitationVisit();
        visit.setActivityType(activityType);
        visit.setAuthorizationId(wechatUserId);
        visit.setInviter(inviter);
        visit.setCreateTime(new Date());
        visit.setDisabled(false);
        visit.setUpdateTime(new Date());
        insert(visit);
    }
}
