/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Alex
 * @author qianlong.yang
 * @version 0.1
 * @since 14-12-17
 */
@Controller
@RequestMapping(value = "/v1/user/wechat")
public class UserWechatApiController extends AbstractApiController {

    @Inject private WechatCodeServiceClient wechatCodeServiceClient;

    @RequestMapping(value = "/binding.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserWechatBindingInfo() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        // 判断是否有家长
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(curUser.getId());
        if (studentParents != null && studentParents.size() > 0) {
            for (StudentParent parent : studentParents) {
                if (wechatLoaderClient.isBinding(parent.getParentUser().getId(), WechatType.PARENT.getType())) {
                    resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                    resultMap.add(RES_PARENT_WECHAT_BINDING, true);
                    return resultMap;
                }
            }
        }

        // 没有家长或者家长未绑定微信，那么生成绑定的二维码
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_PARENT_WECHAT_BINDING, false);

        // 生成微信绑定的二维码
        String wechatBindingUrl = wechatCodeServiceClient.getWechatCodeService()
                .generateQRCode(String.valueOf(curUser.getId()), WechatType.PARENT)
                .getUninterruptibly();
        resultMap.add(RES_PARENT_WECHAT_BINDING_URL, wechatBindingUrl);

        return resultMap;
    }

    @RequestMapping(value = "/share.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage wechatShare() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_SHARE_IMG, "分享图片链接");
            validateRequest(REQ_SHARE_TEXT, REQ_SHARE_IMG);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        // 检查次数是否超过限制
        String memKey = CacheKeyGenerator.generateCacheKey(UserWechatApiController.class,
                new String[]{"userId", "shareDate"},
                new Object[]{curUser.getId(), DateUtils.getTodaySqlDate()});

        Integer shareCount = washingtonCacheSystem.CBS.flushable.load(memKey);
        if (shareCount != null && shareCount > 2) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SHARE_LIMIT_MSG);
            return resultMap;
        }

        String clazzName = "";
        if (UserType.STUDENT.getType() == curUser.getUserType()) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(curUser.getId());
            if (clazz != null) {
                clazzName = clazz.getClassName();
            }
        }

        String shareText = getRequestString(REQ_SHARE_TEXT);
        String shareImg = getRequestString(REQ_SHARE_IMG);

        wechatServiceClient.processDrawTogetherWechatNotice(curUser, clazzName, shareText, shareImg);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        shareCount = shareCount == null ? 1 : shareCount + 1;
        washingtonCacheSystem.CBS.flushable.add(memKey, DateUtils.getCurrentToDayEndSecond(), shareCount);

        return resultMap;
    }

    @RequestMapping(value = "/sharetota.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage wechatShareToTravelAmerica() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_SHARE_TEXT_LINK, "分享报告链接");
            validateRequest(REQ_SHARE_TEXT_LINK);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();

        String clazzName = "";
        if (UserType.STUDENT.getType() == curUser.getUserType()) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(curUser.getId());
            if (clazz != null) {
                clazzName = clazz.getClassName();
            }
        }

        String shareTextLink = getRequestString(REQ_SHARE_TEXT_LINK);
        Long reqSendTime = getRequestLong(REQ_SEND_TIME);
        Date sendTime = reqSendTime > 0L ? new Date(reqSendTime) : null;

        // 下线老走美消息，#35882
        if (!shareTextLink.contains("travelusa")) {
            wechatServiceClient.processTravelAmericaWechatNotice(curUser, clazzName, shareTextLink, sendTime);
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    /**
     * 给微信家长发送模版消息接口
     * 模版固定为公告通知提醒
     * 模版关键填充字段first,keyword1,keyword2,remark，如果需要跳转页面则传入url
     */
    @RequestMapping(value = "sendparentwechatoperaionmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage sendParentWechatOperationMessage() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredAny(REQ_WECHAT_TEMPLATE_KEYWORD1, REQ_WECHAT_TEMPLATE_KEYWORD2, "模版消息keyword1", "模版消息keyword2");
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        if (curUser == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "用户不能为空");
            return resultMap;
        }
        // 只能是家长
        Long reqSendTime = getRequestLong(REQ_SEND_TIME);
        String first = getRequestString(REQ_WECHAT_TEMPLATE_FIRST);
        String keyword1 = getRequestString(REQ_WECHAT_TEMPLATE_KEYWORD1);
        String keyword2 = getRequestString(REQ_WECHAT_TEMPLATE_KEYWORD2);
        String remark = getRequestString(REQ_WECHAT_TEMPLATE_REMARK);
        String url = getRequestString(REQ_WECHAT_TEMPLATE_URL);
        Date sendTime = reqSendTime > 0L ? new Date(reqSendTime) : null;
        Map<String, Object> extensionInfo = new HashMap<>();
        extensionInfo.put("first", first);
        extensionInfo.put("keyword1", keyword1);
        extensionInfo.put("keyword2", keyword2);
        extensionInfo.put("remark", remark);
        extensionInfo.put("url", url);
        if (sendTime != null) {
            extensionInfo.put("sendTime", sendTime);
        }
        try {

            wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.ParentOperationalNotice, curUser.getId(), extensionInfo, WechatType.PARENT);
        } catch (Exception ex) {
            logger.error("Failed to process parent operational wechat notice", ex);

        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 给微信老师发送模版消息接口
     * 模版固定为系统通知提醒
     * 模版关键填充字段first,keyword1,keyword2,remark，如果需要跳转页面则传入url
     */
    @RequestMapping(value = "sendteacherwechatoperationmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage sendTeacherWechatOperationMessage() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredAny(REQ_WECHAT_TEMPLATE_KEYWORD1, REQ_WECHAT_TEMPLATE_KEYWORD2, "模版消息keyword1", "模版消息keyword2");
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        if (curUser == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "用户不能为空");
            return resultMap;
        }
        // 只能是家长
        Long reqSendTime = getRequestLong(REQ_SEND_TIME);
        String first = getRequestString(REQ_WECHAT_TEMPLATE_FIRST);
        String keyword1 = getRequestString(REQ_WECHAT_TEMPLATE_KEYWORD1);
        String keyword2 = getRequestString(REQ_WECHAT_TEMPLATE_KEYWORD2);
        String remark = getRequestString(REQ_WECHAT_TEMPLATE_REMARK);
        String url = getRequestString(REQ_WECHAT_TEMPLATE_URL);
        Date sendTime = reqSendTime > 0L ? new Date(reqSendTime) : null;
        Map<String, Object> extensionInfo = new HashMap<>();
        extensionInfo.put("first", first);
        extensionInfo.put("keyword1", keyword1);
        extensionInfo.put("keyword2", keyword2);
        extensionInfo.put("remark", remark);
        extensionInfo.put("url", url);
        if (sendTime != null) {
            extensionInfo.put("sendTime", sendTime);
        }
        try {

            wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.TeacherOperationNotice, curUser.getId(), extensionInfo, WechatType.TEACHER);
        } catch (Exception ex) {
            logger.error("Failed to process parent operational wechat notice", ex);

        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }
}
