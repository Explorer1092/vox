/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.dao.jdbc.persistence.AbstractEntityPersistence;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author xin
 * @since 14-5-21 下午1:24
 */
@Named
public class WechatNoticePersistence extends AbstractEntityPersistence<Long, WechatNotice> {

    public List<WechatNotice> findByMessageTypeAndState(Integer type, WechatNoticeState state) {
        Date now = new Date();
        return withSelectFromTable("WHERE STATE=? AND MESSAGE_TYPE=? AND DISABLED=FALSE " +
                "AND SEND_TIME<=? AND EXPIRE_TIME>? limit 3000")
                .useParamsArgs(state.getType(), type, now, now).queryAll();
    }

    @Deprecated
    public List<WechatNotice> findAfentiEKUnsentMessage(Long id) {
        return withSelectFromTable("WHERE STATE IN (1,3,4) AND MESSAGE_TYPE=38 AND DISABLED=FALSE AND ID>? limit 10000")
                .useParamsArgs(id).queryAll();
    }

    public int updateMessageState(String openId, String messageId, WechatNoticeState state, String errorCode) {
        return getUtopiaSql().withSql("UPDATE VOX_WECHAT_NOTICE SET STATE=?,ERROR_CODE=?,UPDATE_DATETIME=NOW() WHERE OPEN_ID=? AND MESSAGE_ID=?")
                .useParamsArgs(state.getType(), errorCode, openId, messageId).executeUpdate();
    }

    public int updateMessageStateById(Long id, WechatNoticeState state, String errorCode) {
        return getUtopiaSql().withSql("UPDATE VOX_WECHAT_NOTICE SET STATE=?,ERROR_CODE=?,UPDATE_DATETIME=NOW() WHERE ID=?")
                .useParamsArgs(state.getType(), errorCode, id).executeUpdate();
    }

    public int updateMessageId(Long id, String messageId) {
        //更新messageId说明消息已发送，会同时更新state
        return getUtopiaSql().withSql("UPDATE VOX_WECHAT_NOTICE SET MESSAGE_ID=?,STATE=4,UPDATE_DATETIME=NOW() WHERE ID=?").useParamsArgs(messageId, id).executeUpdate();
    }

    public int deleteMessage(String openId) {
        return getUtopiaSql().withSql("UPDATE VOX_WECHAT_NOTICE SET DISABLED=TRUE,UPDATE_DATETIME=NOW() WHERE OPEN_ID=?")
                .useParamsArgs(openId).executeUpdate();
    }

    public List<WechatNotice> findByMessageTypeForCrm(Integer type, Date dateLimit) {
        return withSelectFromTable("WHERE STATE=? AND MESSAGE_TYPE=? AND DISABLED=FALSE AND CREATE_DATETIME>? limit 10")
                .useParamsArgs(WechatNoticeState.PENDING.getType(), type, dateLimit).queryAll();
    }

    public int updateMessageStateByType(Integer type) {
        return getUtopiaSql().withSql("UPDATE VOX_WECHAT_NOTICE SET STATE=1,UPDATE_DATETIME=NOW() WHERE MESSAGE_TYPE=? AND STATE=5")
                .useParamsArgs(type).executeUpdate();
    }

    public int deleteMessageStateByType(Integer type) {
        return getUtopiaSql().withSql("UPDATE VOX_WECHAT_NOTICE SET DISABLED=TRUE,UPDATE_DATETIME=NOW() WHERE MESSAGE_TYPE=? AND STATE=5")
                .useParamsArgs(type).executeUpdate();
    }

    public int updateStateAndExpireTimeById(Long id, Integer state, Date expireTime) {
        return getUtopiaSql().withSql("UPDATE VOX_WECHAT_NOTICE SET STATE=?,EXPIRE_TIME=?,UPDATE_DATETIME=NOW(),SEND_TIME=NOW() WHERE ID=?")
                .useParamsArgs(state, expireTime, id).executeUpdate();
    }

    public List<WechatNotice> listAllByUserId(Long userId) {
        return withSelectFromTable("WHERE USER_ID=? AND DISABLED=FALSE ORDER BY CREATE_DATETIME DESC").useParamsArgs(userId).queryAll();
    }

    public int deleteByIds(Collection<Long> ids) {
        StringBuilder builder = new StringBuilder("DELETE FROM VOX_WECHAT_NOTICE WHERE ID IN (");
        for (Long id : ids) {
            builder.append(id).append(",");
        }
        builder.deleteCharAt(builder.length() - 1).append(")");
        return getUtopiaSql().withSql(builder.toString()).executeUpdate();
    }

    public int updateStateTo5ByIds(List<Long> noticeIds){
        StringBuilder builder=new StringBuilder("UPDATE VOX_WECHAT_NOTICE SET STATE = 5 WHERE ID IN (");
        for (Long id:noticeIds){
            builder.append(id).append(",");
        }
        builder.deleteCharAt(builder.length()-1).append(")");
        return getUtopiaSql().withSql(builder.toString()).executeUpdate();
    }

}
