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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccountsTools;
import com.voxlearning.utopia.service.mizar.api.entity.oa.UserOfficialAccountsRef;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by Summer Yang on 2016/7/6.
 * <p>
 * 公众号 接口API
 */
@Controller
@RequestMapping(value = "/v1/parent/officialaccount")
@Slf4j
public class ParentOfficialAccountsApiController extends AbstractParentApiController {

    // 获取公众号文章数据 -- 根据时间戳获取 每次返回时间戳之后的全部新的数据
    @RequestMapping(value = "/loadarticle.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadArticles() {
        MapMessage mapMessage = new MapMessage();
        Long accountId = getRequestLong(REQ_OFFICIAL_ACCOUNT_ID);
        Long startDateTime = getRequestLong(REQ_OFFICIAL_ACCOUNT_ARTICLE_START_DATE);
        try {
            if (startDateTime != 0) {
                validateRequest(REQ_OFFICIAL_ACCOUNT_ID, REQ_OFFICIAL_ACCOUNT_ARTICLE_START_DATE);
            } else {
                validateRequest(REQ_OFFICIAL_ACCOUNT_ID);
            }
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountById(accountId);
        if (accounts == null) {
            return failMessage("公众号不存在");
        }
        Long parentId = getCurrentParentId();
        try {
            // 最多取7天以内的文章
            Date defaultStartDate = DateUtils.calculateDateDay(new Date(), -7);
            // 获得最近一次关注时间
            Date latestFollowTime = officialAccountsServiceClient.getFollowAccountDate(parentId,accountId);
            // 如果起始时间是空，查询最近一次关注的修改时间，从那个时间点开始load，超过七天按七天取
            Date startDate;
            if(startDateTime == 0)
                startDate = latestFollowTime;
            else {
                startDate = new Date(startDateTime);
                // 和外面的小红点儿逻辑改成一致的，舍掉关注前面的数据
                if(latestFollowTime != null && startDate.before(latestFollowTime)){
                    startDate = latestFollowTime;
                }
            }

            // 如果获取到的时间大于7天， 则还按照7天的时间去过滤
            if (startDate == null || defaultStartDate.after(startDate)) {
                startDate = defaultStartDate;
            }

            List<Map<String, Object>> articleList = officialAccountsServiceClient
                    .loadArticlesByAccountsIdAndCreateDate(accountId, startDate, parentId);
            // 处理版本信息显示
            String ver = getRequestString(REQ_APP_NATIVE_VERSION);
            if (VersionUtil.compareVersion(ver, "1.7.2") < 0 && CollectionUtils.isNotEmpty(articleList)) {
                // 老版本只显示文章信息
                articleList = articleList.stream().filter(m ->
                        StringUtils.equals(SafeConverter.toString(m.get("type")), "article")).collect(Collectors.toList());
            }
            mapMessage.add("articleList", articleList);

            // 1.8.0以前的版本才显示工具栏信息
            if(VersionUtil.compareVersion(ver,"1.8.0") < 0){
                // 获取公众号工具栏
                List<OfficialAccountsTools> toolList = officialAccountsServiceClient.loadAccountToolsByAccountId(accountId);
                // 拼装数据
                List<Map<String, Object>> toolDataList = new ArrayList<>();
                for (OfficialAccountsTools tool : toolList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("toolName", tool.getToolName());
                    map.put("toolUrl", tool.getToolUrl());
                    map.put("bindSid", tool.getBindSid());
                    toolDataList.add(map);
                }
                mapMessage.add("toolList", toolDataList);
            }

            return mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        } catch (Exception ex) {
            return failMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/toollist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadToolList() {
        MapMessage mapMessage = new MapMessage();
        Long accountId = getRequestLong(REQ_OFFICIAL_ACCOUNT_ID);
        try {
            validateRequest(REQ_OFFICIAL_ACCOUNT_ID);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountById(accountId);
        if (accounts == null) {
            return failMessage("公众号不存在");
        }

        try {
            // 获取公众号工具栏
            // 拼装数据
            List<Map<String, Object>> toolDataList = officialAccountsServiceClient
                    .loadAccountToolsByAccountId(accountId)
                    .stream()
                    .map(tool -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("toolName", tool.getToolName());
                        map.put("toolUrl", tool.getToolUrl());
                        map.put("bindSid", tool.getBindSid());
                        return map;
                    })
                    .collect(Collectors.toList());

            mapMessage.add("toolList", toolDataList);
            mapMessage.add(REQ_OFFICIAL_ACCOUNT_ID,accountId);
            return mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);

        } catch (Throwable t) {
            return failMessage(t.getMessage());
        }
    }

    // 获取公众号是否被用户关注
    @RequestMapping(value = "/loadfollow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadFollow() {

        MapMessage mapMessage = new MapMessage();
        Long accountId = getRequestLong(REQ_OFFICIAL_ACCOUNT_ID);
        try {
            validateRequest(REQ_OFFICIAL_ACCOUNT_ID);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountById(accountId);
        if (accounts == null) {
            return failMessage("公众号不存在");
        }

        Long parentId = getCurrentParentId();
        try {
            // 如果在公众号区域内并且没有取消关注 or 有关注关系 为关注状态
            boolean isFollow = officialAccountsServiceClient.isFollow(accountId, parentId);
            mapMessage.add("isFollow", isFollow);

            // 默认都可以取消关注
            mapMessage.add("cantUnfollow", false);
            // 如果是自学乐园公众号的话，如果有未到期的阿分题订单，是不允许取消关注的
            if (Objects.equals(accountId, OfficialAccounts.SpecialAccount.FAIRY_LAND.getId())) {
                // 遍历家长下面的每个学生，但凡有一个订单未到期，就不能取关
                studentLoaderClient.loadParentStudents(parentId)
                        .forEach(stu -> userOrderLoaderClient.getUserAppPaidStatus(
                                OrderProductServiceType.getAllValidTypes()
                                        .stream()
                                        .map(Enum::name)
                                        .collect(Collectors.toList()),
                                stu.getId(), false)
                                .forEach((k, v) -> {
                                    if (v != null && v.isActive())
                                        mapMessage.set("cantUnfollow", true);
                                }));
            }
            // 点读机公众号是不允许取消关注
            else if(Objects.equals(accounts.getAccountsKey(),
                    OfficialAccounts.SpecialAccount.GRIND_EAR_SERVICE.getKey())){
                mapMessage.set("cantUnfollow", true);
            }

            return mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        } catch (Exception ex) {
            return failMessage(ex.getMessage());
        }
    }

    // 关注/ 取消关注公众号
    @RequestMapping(value = "/follow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage follow() {
        MapMessage mapMessage;
        Long accountId = getRequestLong(REQ_OFFICIAL_ACCOUNT_ID);
        String status = getRequestString(REQ_OFFICIAL_ACCOUNT_STATUS);
        UserOfficialAccountsRef.Status refStatus;
        try {
            refStatus = UserOfficialAccountsRef.Status.valueOf(status);
            validateRequest(REQ_OFFICIAL_ACCOUNT_ID, REQ_OFFICIAL_ACCOUNT_STATUS);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        if (refStatus == null) {
            return failMessage("关注状态错误");
        }
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountById(accountId);
        if (accounts == null) {
            return failMessage("公众号不存在");
        }
        Long parentId = getCurrentParentId();
        try {
            mapMessage = officialAccountsServiceClient.updateFollowStatus(parentId, accountId, refStatus);
            if (mapMessage.isSuccess()) {
                return mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                return failMessage(mapMessage.getInfo());
            }
        } catch (Exception ex) {
            return failMessage(ex.getMessage());
        }
    }

    // 给公众号发送消息
    @RequestMapping(value = "/sendmessage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendMessage() {
        try {
            validateRequestNoSessionKey(REQ_APP_MESSAGE_UID, REQ_APP_MESSAGE_TITLE, REQ_APP_MESSAGE_CONTENT,
                    REQ_APP_MESSAGE_LINKURL, REQ_APP_MESSAGE_EXTINFO);
            validateRequired(REQ_APP_MESSAGE_UID, "用户ID");
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        try {
            String userIdStr = getRequestString(REQ_APP_MESSAGE_UID);
            String title = getRequestString(REQ_APP_MESSAGE_TITLE);
            String content = getRequestString(REQ_APP_MESSAGE_CONTENT);
            String linkUrl = getRequestString(REQ_APP_MESSAGE_LINKURL);
            String extInfoStr = getRequestString(REQ_APP_MESSAGE_EXTINFO);
            MapMessage message = officialAccountsServiceClient.sendMessage(Collections.singletonList(SafeConverter.toLong(userIdStr)), title,
                    content, linkUrl, extInfoStr, false);
            if (message.isSuccess()) {
                return successMessage("发送成功");
            } else {
                return failMessage(message.getInfo());
            }
        } catch (Exception ex) {
            return failMessage(ex.getMessage());
        }
    }

}
