/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.wechat.impl.service.wechat;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatNoticePersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * wechat notice service
 * Created by Shuai Huan on 2015/1/9.
 */
@Named
public class WechatNoticeService extends SpringContainerSupport {

    private static final int DEFAULT_LIMIT = 50;

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    public List<Map<String, Object>> loadNoticeByMessageType(Integer type) {

        List<WechatNotice> notices = wechatNoticePersistence.findByMessageTypeAndState(type, WechatNoticeState.WAITTING);
        if (notices.size() == 0) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (WechatNotice notice : notices) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notice.getId());
            map.put("openId", notice.getOpenId());
            map.put("message", notice.getMessage());
            map.put("createTime", notice.getCreateDatetime());
            result.add(map);
        }
        return result;
    }

    public int updateNoticeStateTo5(List<Long> noticeIds) {
        // 消息取出后将消息状态从0置成5
        return wechatNoticePersistence.updateStateTo5ByIds(noticeIds);
    }

    public List<Map<String, Object>> loadNoticeTypes() {
        // 给微信发送消息用的load所有消息的配置信息，等发送程序也java化了后这个就可以不用了
        String sql = "SELECT X.*,Y.PARAMS AS TEMPLATE_PARAMS,Y.TEMPLATE_ID AS TEMPLATE_ID,Z.URL FROM VOX_WN_MGR_MESSAGE_TYPE X,VOX_WN_MGR_TEMPLATE Y,VOX_WN_MGR_URL Z WHERE X.TEMPLATE=Y.ID AND X.URL=Z.ID";
        List<Map<String, Object>> dataList = wechatNoticePersistence.getUtopiaSql().withSql(sql).queryAll();
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.emptyList();
        }
        return dataList;
    }

    public List<Map<String, Object>> loadSqlExecutors() {
        // 加载所有需要执行的sql，然后去执行吧
        String sql = "SELECT E.ID,S.RAW_SQL,S.MESSAGE_TYPE,S.PARAMS FROM VOX_WN_MGR_SQL_EXECUTOR E,VOX_WN_MGR_SQL_SELECTOR S WHERE E.STATE=1 AND E.DISABLED=FALSE AND E.EXECUTE_TIME<NOW() AND S.ID=E.SQL AND S.DISABLED=FALSE";
        List<Map<String, Object>> dataList = wechatNoticePersistence.getUtopiaSql().withSql(sql).queryAll();
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.emptyList();
        }
        return dataList;
    }

    public void updateNoticeSqlState(long id, int state, long count) {
        // 加载所有需要执行的sql，然后去执行吧
        String sql = "UPDATE VOX_WN_MGR_SQL_EXECUTOR SET STATE=?,COUNT=?,UPDATE_DATETIME=NOW() WHERE ID = ?";
        wechatNoticePersistence.getUtopiaSql().withSql(sql).useParamsArgs(state, count, id).executeUpdate();
    }

    public List<WechatNotice> loadAfentiEKUnsentMessage(Long id) {
        //查询阿分题做错考点未发送消息
        List<WechatNotice> notices = wechatNoticePersistence.findAfentiEKUnsentMessage(id);
        if (notices.size() == 0) {
            return Collections.emptyList();
        }
        return notices;
    }

    public void updateNoticeState(String openId, String messageId, WechatNoticeState state, String errorCode) {
        FlightRecorder.dot("updateWechatNoticeState begin, openId:" + openId +
                ",messageId:" + messageId + ",state:" + state);
        int rows = wechatNoticePersistence.updateMessageState(openId, messageId, state, errorCode);
        FlightRecorder.dot("updateWechatNoticeState end, total:" + rows);
    }

    public void updateNoticeState(Long id, WechatNoticeState state, String errorCode) {
        wechatNoticePersistence.updateMessageStateById(id, state, errorCode);
    }

    public void updateNoticeMessageId(Long id, String messageId) {
        wechatNoticePersistence.updateMessageId(id, messageId);
    }

    public List<Map<String, Object>> loadNoticeByMessageTypeForCrm(Integer type) {
        Date limit = DateUtils.calculateDateDay(DayRange.current().getStartDate(), -7);
        List<WechatNotice> notices = wechatNoticePersistence.findByMessageTypeForCrm(type, limit);
        if (notices.size() == 0) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (WechatNotice notice : notices) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notice.getId());
            map.put("openId", notice.getOpenId());
            map.put("message", notice.getMessage());
            map.put("createTime", DateUtils.dateToString(notice.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
            result.add(map);
        }
        return result;
    }

    public void updateMessageStateByType(Integer type) {
        wechatNoticePersistence.updateMessageStateByType(type);
    }

    public void deleteMessageStateByType(Integer type) {
        wechatNoticePersistence.deleteMessageStateByType(type);
    }

    public List<WechatNotice> listExpired(Date createDatetime, Integer limit) {
        if (createDatetime == null) {
            return null;
        }
        String date = DateUtils.dateToString(createDatetime);
        String curr = DateUtils.dateToString(new Date());
        int lim = limit == null || limit < 1 ? DEFAULT_LIMIT : limit;
        return wechatNoticePersistence.withSelectFromTable("WHERE STATE IN (?,?,?,?,?) AND CREATE_DATETIME<? OR EXPIRE_TIME<?  LIMIT ?").
                useParamsArgs(WechatNoticeState.ANONYMOUS.getType(), WechatNoticeState.SENDED.getType(), WechatNoticeState.SUCCESS.getType(),
                        WechatNoticeState.FAILED.getType(), WechatNoticeState.EXPIRED.getType(), date, curr, lim).queryAll();
    }

    public List<WechatNotice> loadAllByUserId(Long userId) {
        return userId == null ? null : wechatNoticePersistence.listAllByUserId(userId);
    }

    public int removeByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return wechatNoticePersistence.deleteByIds(ids);
    }

}
