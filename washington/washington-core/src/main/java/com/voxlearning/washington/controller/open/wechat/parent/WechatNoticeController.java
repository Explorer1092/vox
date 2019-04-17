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

package com.voxlearning.washington.controller.open.wechat.parent;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.business.cache.BusinessCache;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeSnapshot;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xin
 * @since 14-5-21 下午6:40
 */
@Controller
@RequestMapping(value = "/open/wechat/notice")
@Slf4j
public class WechatNoticeController extends AbstractOpenController {

    private final String prefix = "WX_LAST_OP_TIME_";

    @RequestMapping(value = "loadnotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadNotice(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        int type = SafeConverter.toInt(openAuthContext.getParams().get("type"), Integer.MIN_VALUE);
        if (Integer.MIN_VALUE == type) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的消息类型");
            return openAuthContext;
        }
        try {
            List<Map<String, Object>> notices = wechatServiceClient.loadNoticeByMessageType(type);
            if (notices.size() > 0) {
                // update state to 5, means loaded out
                List<Long> noticeIds = notices.stream().map(x -> SafeConverter.toLong(x.get("id"))).collect(Collectors.toList());
                wechatServiceClient.updateNoticeStateTo5(noticeIds);
                openAuthContext.add("notices", notices);
            }
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("load math homeowrk create notice failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询微信通知失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "loadusernotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadUserNotice(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        if (userId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("openId为空");
            return openAuthContext;
        }
        try {
            List<WechatNoticeSnapshot> noticeSnaps = new ArrayList<>();
            List<WechatNoticeSnapshot> latestSnaps = wechatLoaderClient.loadWechatNoticeSnapshotByUserId(userId, false);
            if (latestSnaps != null && !latestSnaps.isEmpty()) {
                noticeSnaps.addAll(latestSnaps);
            }
            List<WechatNoticeSnapshot> historySnaps = wechatLoaderClient.loadWechatNoticeSnapshotByUserId(userId, true);
            if (historySnaps != null && !historySnaps.isEmpty()) {
                noticeSnaps.addAll(historySnaps);
            }
            if (!noticeSnaps.isEmpty()) {
                openAuthContext.add("notices", noticeSnaps);
            }
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("load math homeowrk create notice failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询用户微信通知失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "loadafentiekunsentmessage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadAfentiEKUnsentMessage(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long id = SafeConverter.toLong(openAuthContext.getParams().get("id"), Long.MIN_VALUE);
        if (id == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("openId为空");
            return openAuthContext;
        }
        try {
            List<WechatNotice> notices = wechatServiceClient.loadAfentiEKUnsentMessage(id);
            if (notices.size() > 0) {
                openAuthContext.add("notices", notices);
            }
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("load math homeowrk create notice failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询阿分题微信通知失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "setnoticestate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext setNoticeState(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String openid = SafeConverter.toString(openAuthContext.getParams().get("openid"));
        int state = SafeConverter.toInt(openAuthContext.getParams().get("state"), Integer.MIN_VALUE);
        String messageId = SafeConverter.toString(openAuthContext.getParams().get("msgid"));
        // FIXME: errorCode是否需要进行存在性检查？是远程调用的参数一部分
        String errorCode = SafeConverter.toString(openAuthContext.getParams().get("errorcode"));
        if (null == openid || Integer.MIN_VALUE == state || null == messageId) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的参数");
            return openAuthContext;
        }

        try {
            wechatServiceClient.updateNoticeState2(openid, messageId, WechatNoticeState.of(state), errorCode);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("set notice state failed.openId:{},messageId:{},state:{}", openid, messageId, state, ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("更新通知状态失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "setnoticestatebyid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext setNoticeStateById(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long nid = SafeConverter.toLong(openAuthContext.getParams().get("nid"), Long.MIN_VALUE);
        int state = SafeConverter.toInt(openAuthContext.getParams().get("state"), Integer.MIN_VALUE);
        // FIXME: errorCode是否需要存在性检查？是远程调用的参数一部分
        String errorCode = SafeConverter.toString(openAuthContext.getParams().get("errorcode"));
        if (Long.MIN_VALUE == nid || Integer.MIN_VALUE == state) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的参数");
            return openAuthContext;
        }

        try {
            wechatServiceClient.updateNoticeState2(nid, WechatNoticeState.of(state), errorCode);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("set notice state failed.nid:{},state:{}", nid, state, ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("更新通知状态失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "setmessageid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext setMessageId(HttpServletRequest request) {
        // 获取所有的配置的模版消息
        OpenAuthContext context = getOpenAuthContext(request);
        long noticeId = SafeConverter.toLong(context.getParams().get("nid"), Long.MIN_VALUE);
        String messageId = SafeConverter.toString(context.getParams().get("msgid"));
        if (Long.MIN_VALUE == noticeId || null == messageId) {
            context.setCode("400");
            context.setError("无效的参数");
            return context;
        }
        try {
            wechatServiceClient.updateNoticeMessageId2(noticeId, messageId);
            context.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("set notice state failed.id:{},messageId:{}", noticeId, messageId, ex);
            context.setCode("400");
            context.setError("更新通知状态失败");
        }
        return context;
    }

    @RequestMapping(value = "loadnoticetypes.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadNoticeTypes(HttpServletRequest request) {
        // 获取所有表中配置的消息类型
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        try {
            List<Map<String, Object>> noticeTypes = BusinessCache.getBusinessCache()
                    .wrapCache(wechatServiceClient)
                    .expiration(600)
                    .keyPrefix("WECHAT_NOTICE_TYPES_DEFINITION_LOADS")
                    .proxy()
                    .loadNoticeTypes();
            openAuthContext.add("noticeTypes", noticeTypes);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("load notice types failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询微信消息配置信息失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "loadnoticesqls.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadNoticeSqls(HttpServletRequest request) {
        // 获取所有需要执行的sql列表送去执行，执行者是单线程，防止大量插入拖慢数据库
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        try {
//            List<Map<String, Object>> sqlExecutors = businessCacheClient.getBusinessCacheSystem().CBS.flushable
//                    .wrapCache(wechatServiceClient)
//                    .expiration(20)
//                    .keyPrefix("WECHAT_NOTICE_SQL_EXECUTORS_LOADS")
//                    .proxy()
//                    .loadSqlExecutors();
            List<Map<String, Object>> sqlExecutors = wechatServiceClient.loadSqlExecutors();
            openAuthContext.add("sqlExecutors", sqlExecutors);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("load sql executors failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询微信sql执行者失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "updatenoticesqlstate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext updateNoticeSqlState(HttpServletRequest request) {
        // 更新所有sql的状态to,2(正在执行),3(执行失败),4(执行成功)，count(该sql共产生多少条微信消息)
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long id = SafeConverter.toLong(openAuthContext.getParams().get("id"), Long.MIN_VALUE);
        Integer state = SafeConverter.toInt(openAuthContext.getParams().get("state"), Integer.MIN_VALUE);
        long count = SafeConverter.toLong(openAuthContext.getParams().get("count"), 0L);
        if (Integer.MIN_VALUE == state || Long.MIN_VALUE == id) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的参数");
            return openAuthContext;
        }
        try {
            wechatServiceClient.updateNoticeSqlState(id, state, count);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            WechatNoticeController.log.error("update notice sql state failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("更新sql执行状态失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "setlastoptime.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext setLastOpTime(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        String openId = SafeConverter.toString(context.getParams().get("openId"));
        String opTime = SafeConverter.toString(context.getParams().get("opTime"));
        if (StringUtils.isEmpty(openId) || StringUtils.isEmpty(opTime)) {
            context.setCode("400");
            context.setError("无效的参数");
            return context;
        }
        Boolean ret = washingtonCacheSystem.CBS.persistence.set(prefix + openId, 48 * 60 * 60, opTime);
        if (Boolean.TRUE.equals(ret)) {
            context.setCode("200");
        } else {
            context.setCode("400");
            context.setError("刷新最后操作时间失败");
        }
        return context;
    }

    @RequestMapping(value = "getlastoptime.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getLastOpTime(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        String openId = SafeConverter.toString(context.getParams().get("openId"));
        if (StringUtils.isEmpty(openId)) {
            context.setCode("400");
            context.setError("无效的参数");
            return context;
        }
        CacheObject<String> cacheObject = washingtonCacheSystem.CBS.persistence.get(prefix + openId);
        if (cacheObject != null) {
            String opTime = cacheObject.getValue();
            context.setCode("200");
            context.add("opTime", opTime);
        } else {
            context.setCode("400");
            context.setError("获取最后操作时间失败");
        }
        return context;
    }

}
