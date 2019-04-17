
package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatTemplateMessageRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

@Named
public class WechatTemplateMessageRecordPersistence extends StaticMySQLPersistence<WechatTemplateMessageRecord, Long> {

    @Override
    protected void calculateCacheDimensions(WechatTemplateMessageRecord document, Collection<String> dimensions) {
    }

    public void insertOrUpdate(WechatTemplateMessageRecord document) {
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATE_DATETIME`=NOW(),`DISABLED`=0 ");
        getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
    }

    public void updateState(String openId, WechatType wechatType, String messageId, WechatNoticeState state, String errorCode) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId)
                .and("DISABLED").is(false)
                .and("WECHAT_TYPE").is(wechatType.getType())
                .and("MESSAGE_ID").is(messageId);
        Update update = new Update();
        update.set("UPDATE_DATETIME", new Date()).set("STATE", state.name()).set("ERROR_CODE", errorCode);
        executeUpdate(update, criteria, getTableName());
    }

    public void updateState(Long id, WechatNoticeState state, String errorCode, String wechatMsgId) {
        Criteria criteria = Criteria.where("ID").is(id)
                .and("DISABLED").is(false);
        Update update = new Update();
        update.set("UPDATE_DATETIME", new Date()).set("STATE", state.name()).set("ERROR_CODE", errorCode).set("MESSAGE_ID", wechatMsgId);
        executeUpdate(update, criteria, getTableName());
    }
}
