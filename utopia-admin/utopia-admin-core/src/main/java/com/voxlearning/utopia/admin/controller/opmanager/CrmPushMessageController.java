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

package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.FairylandServiceClient;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;
import static com.voxlearning.utopia.service.vendor.api.constant.StudentFairylandMessageType.COMMON_MSG;

/**
 * Created by Summer Yang on 2016/7/15.
 * 发送JPUSH消息
 */
@Controller
@RequestMapping("/opmanager/pushmessage")
public class CrmPushMessageController extends OpManagerAbstractController {
    final private static String RECEIVER_VALUE = "fairyland_email_receiver";

    // inject in alphabetical order
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private FairylandLoaderClient fairylandLoaderClient;
    @Inject private FairylandServiceClient fairylandServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private WorkFlowLoaderClient workFlowLoaderClient;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    /*==========================================================================*/
    /*================                 微信消息                 =================*/
    /*==========================================================================*/

    @RequestMapping(value = "wechatbatchindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String wechatBatchIndex(Model model) {
        int currentPage = Integer.max(1, getRequestInt("currentPage"));
        //获取WorkFlowProcess中待审核的信息
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();

        Set<Long> workFlowRecordIds = workFlowLoaderClient.loadWorkFlowProcessByTargetUser("admin", "admin:" + adminUser.getRealName())
                .stream()
                .filter(record -> record.getWorkFlowType() == null)
                .map(WorkFlowProcess::getWorkflowRecordId)
                .collect(Collectors.toSet());
        List<WorkFlowRecord> workFlowRecordList = new ArrayList<>(
                workFlowLoaderClient.loadWorkFlowRecords(workFlowRecordIds).values()
        );

        int pageSize = 20;
        Pageable page = new PageRequest(currentPage - 1, pageSize);
        Page<WorkFlowRecord> workFlowRecords = PageableUtils.listToPage(workFlowRecordList, page);
        model.addAttribute("workFlowRecordList", workFlowRecords.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPage", workFlowRecords.getTotalPages());
        return "opmanager/pushmessage/wechatbatchindex";
    }

    @RequestMapping(value = "wechatbatchdetail.vpage", method = RequestMethod.GET)
    public String wechatBatchDetail(Model model) {
        return "opmanager/pushmessage/wechatbatchdetail";
    }

    /*==========================================================================*/
    /*================               课外乐园消息                =================*/
    /*==========================================================================*/

    // fairyland 主页
    @RequestMapping(value = "fairyland.vpage", method = RequestMethod.GET)
    public String fairyland(Model model) {

        List<FairylandProduct> onlineFairylandProducts = fairylandLoaderClient.loadFairylandProducts(FairyLandPlatform.STUDENT_APP, FairylandProductType.APPS)
                .stream()
                .filter(p -> StringUtils.isNotBlank(p.fetchRedirectUrl(RuntimeMode.current())))
                .collect(Collectors.toList());

        model.addAttribute("onlineFairylandProducts", onlineFairylandProducts);
        return "opmanager/pushmessage/fairyland";
    }

    // fairyland 主页
    @RequestMapping(value = "fairyland/sendmessage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendMessage() {
        String title = getRequestString("title");
        String popupTitleStr = getRequestString("popupTitle");
        String expiredTimeStr = getRequestString("expiredTime");
        String appKey = getRequestString("appKey");
        String pushType = getRequestString("pushType");
        String linkUrl = getRequestString("linkUrl");
        Integer linkType = getRequestInt("linkType");
        String content = getRequestString("content");
        Long userId = getRequestLong("userId");
        String readType = getRequestString("readType");

        PopupTitle popupTitle = null;
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(appKey) || StringUtils.isBlank(expiredTimeStr)
                || StringUtils.isBlank(readType)) {
            return MapMessage.errorMessage("参数不能为空");
        }
        if (StringUtils.isNotEmpty(popupTitleStr)) {
            popupTitle = PopupTitle.valueOf(popupTitleStr);
        }
        StudentFairylandMessageType studentFairylandMessageType = StudentFairylandMessageType.valueOf(getRequestString("messageType"));
        long expiredTime = DateUtils.stringToDate(expiredTimeStr, DateUtils.FORMAT_SQL_DATETIME).getTime();

        //发消息提醒
        String sendContent = "标题：" + title + "(不包括黑名单用户)\n";
        sendContent += "推送内容：" + content + "\n";
        sendContent += "设置过期时间：" + expiredTimeStr + "\n";
        sendContent += "推送对象：" + ("SpecifyUserId".equals(pushType) ? "个人用户(userId:" + userId + ")" : "全部用户") + "\n";
        sendMail("学生课外乐园发送运营消息", sendContent);

        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("readType", readType);
        if ("SpecifyUserId".equals(pushType)) {
            if (userId == 0L || userLoaderClient.loadUser(userId) == null) {
                return MapMessage.errorMessage("用户不存在");
            }
            if (studentFairylandMessageType == COMMON_MSG) {
                return fairylandServiceClient.saveAppMessage(Collections.singletonList(userId), title,
                        linkUrl, linkType, studentFairylandMessageType, expiredTime, popupTitle, appKey, content, extInfo);
            } else if (studentFairylandMessageType == StudentFairylandMessageType.OPEN_APP_MSG) {
                return fairylandServiceClient.saveOpenAppMessage(Collections.singletonList(userId), title,
                        expiredTime, popupTitle, appKey, content, extInfo);
            }
            return MapMessage.errorMessage();
        } else if ("AllUser".equals(pushType)) {
            AppGlobalMessage message = new AppGlobalMessage();
            message.setExtInfo(extInfo);
            message.setMessageType(studentFairylandMessageType.type);
            message.setTitle(title);
            message.setPopupTitle(popupTitle == null ? null : popupTitle.name());
            message.setMessageSource(AppMessageSource.FAIRYLAND.name());
            message.setExpiredTime(expiredTime);
            message.setNoneBlackTag(JpushUserTag.NON_ANY_BLACK_LIST.tag); //默认只能发送非黑名单用户

            if (studentFairylandMessageType == COMMON_MSG) {
                if (StringUtils.isEmpty(content)) {
                    return MapMessage.errorMessage("content不能为空");
                }
                message.setContent(content);
                message.setLinkUrl(linkUrl);
                message.setLinkType(linkType);
                message.setMessageType(COMMON_MSG.type);
                messageCommandServiceClient.getMessageCommandService().createAppGlobalMessage(message);
                return MapMessage.successMessage();
            } else {
                message.setMessageType(StudentFairylandMessageType.OPEN_APP_MSG.type);
                message.setContent(content);
                return saveOpenAppGlobalMessage(message, expiredTime, appKey);
            }
        } else {
            return MapMessage.errorMessage("推送合适类型");
        }
    }

    private MapMessage saveOpenAppGlobalMessage(AppGlobalMessage globalMessage, Long expiredTime, String appKey) {
        if (globalMessage == null || expiredTime <= System.currentTimeMillis() || StringUtils.isEmpty(appKey)) {
            return MapMessage.errorMessage("参数错误");
        }

        VendorApps vendorApps = vendorLoaderClient.loadVendor(appKey);
        FairylandProduct fairylandProduct = fairylandLoaderClient
                .loadFairylandProducts(FairyLandPlatform.STUDENT_APP, FairylandProductType.APPS)
                .stream().filter(p -> appKey.equals(p.getAppKey())).findFirst().orElse(null);
        if (fairylandProduct == null || vendorApps == null) {
            return MapMessage.errorMessage();
        }
        if (MapUtils.isNotEmpty(globalMessage.getExtInfo())) {
            globalMessage.getExtInfo().put("appKey", appKey);
        } else {
            globalMessage.setExtInfo(new HashMap<>());
            globalMessage.getExtInfo().put("appKey", appKey);
        }
        globalMessage.setLinkType(1);
        String linkUrl = FairylandProductRedirectType.buildMidUrl(fairylandProduct.getAppKey(), FairyLandPlatform.valueOf(fairylandProduct.getPlatform()));
        globalMessage.setLinkUrl(linkUrl);
        messageCommandServiceClient.getMessageCommandService().createAppGlobalMessage(globalMessage);
        return MapMessage.successMessage();
    }

    /*==========================================================================*/
    /*================              OTHER METHODS              =================*/
    /*==========================================================================*/

    @RequestMapping(value = "uploadfile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upload(MultipartFile file) {
        try {
            String fileName = uploadFile(file);
            return MapMessage.successMessage().add("fileName", fileName);

        } catch (Exception e) {
            return MapMessage.errorMessage("上传文件失败");
        }
    }

    private boolean sendMail(String title, String desc) {
        String receivers = crmConfigService.$loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.getType(), RECEIVER_VALUE);
        if (StringUtils.isBlank(receivers)) {
            return false;
        }
        String userName = getCurrentAdminUser().getAdminUserName();
        String date = DateUtils.getNowSqlDatetime();
        String content = "操作用户：" + userName + "\n";
        content += "详细内容：" + desc + "\n";
        content += "操作时间：" + date;
        emailServiceClient.createPlainEmail()
                .to(receivers)
                .subject(title + "变更(来自：" + RuntimeMode.current().name() + "环境)")
                .body(content)
                .send();
        return true;
    }

}