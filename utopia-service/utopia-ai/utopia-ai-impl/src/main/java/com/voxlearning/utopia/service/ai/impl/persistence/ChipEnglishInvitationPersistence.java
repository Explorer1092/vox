package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipEnglishInvitation;

import javax.inject.Named;
import java.util.*;

/**
 * @author songtao
 * @since 2018/5/3
 */
@Named
public class ChipEnglishInvitationPersistence extends StaticMySQLPersistence<ChipEnglishInvitation, Long> {

    @Override
    protected void calculateCacheDimensions(ChipEnglishInvitation document, Collection<String> dimensions) {
        dimensions.add(ChipEnglishInvitation.ck_inviter(document.getInviter()));
    }

    @CacheMethod
    public List<ChipEnglishInvitation> loadByInviterId(@CacheParameter("INVITER")  Long inviter) {
        Criteria criteria = Criteria.where("INVITER").is(inviter).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public void insertOrUpdate(ChipEnglishInvitation document) {
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0,`PRODUCT_ID`='"+ document.getProductId() + "',`INVITER`=" + document.getInviter());
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(document);
        }
    }

    public void deleteByInviter(Long inviter, String productItemId) {
        Update update = new Update();
        update.set("UPDATETIME", new Date()).set("DISABLED", true);
        Criteria criteria = Criteria.where("INVITER").is(inviter).and("PRODUCT_ITEM_ID").is(productItemId).and("DISABLED").is(false);
        long res = executeUpdate(update, criteria, getTableName());
        if (res > 0L) {
            getCache().delete(ChipEnglishInvitation.ck_inviter(inviter));
        }
    }

    public void deleteByInvitee(Long invitee, String productItemId) {
        Criteria criteria = Criteria.where("INVITEE").is(invitee).and("PRODUCT_ITEM_ID").is(productItemId).and("DISABLED").is(false);
        ChipEnglishInvitation chipEnglishInvitation = query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
        if (chipEnglishInvitation != null) {
            Update update = new Update();
            update.set("UPDATETIME", new Date()).set("DISABLED", true);
            executeUpdate(update, criteria, getTableName());
            cleanCache(chipEnglishInvitation);
        }
    }

    public long updateToSendByItem(Long inviter, String productItemId) {
        Criteria criteria = Criteria.where("INVITER").is(inviter)
                .and("DISABLED").is(false)
                .and("PRODUCT_ITEM_ID").is(productItemId)
                .and("SEND").is(false);
        Update update = new Update();
        update.set("UPDATETIME", new Date()).set("SEND", true);
        long res = executeUpdate(update, criteria, getTableName());
        if (res > 0L) {
            getCache().delete(ChipEnglishInvitation.ck_inviter(inviter));
        }
        return res;
    }
    public long updateToSendByProduct(Long inviter, String productId) {
        Criteria criteria = Criteria.where("INVITER").is(inviter)
                .and("DISABLED").is(false)
                .and("PRODUCT_ID").is(productId)
                .and("SEND").is(false);
        Update update = new Update();
        update.set("UPDATETIME", new Date()).set("SEND", true);
        long res = executeUpdate(update, criteria, getTableName());
        if (res > 0L) {
            getCache().delete(ChipEnglishInvitation.ck_inviter(inviter));
        }
        return res;
    }

    private void cleanCache(ChipEnglishInvitation document) {
        List<String> keys = new ArrayList<>();
        calculateCacheDimensions(document, keys);
        getCache().delete(keys);
    }

}
