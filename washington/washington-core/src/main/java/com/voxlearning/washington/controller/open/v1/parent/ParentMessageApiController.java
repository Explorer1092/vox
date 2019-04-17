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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.message.api.UserReminderService;
import com.voxlearning.utopia.service.message.api.constant.ReminderPosition;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.AppMessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.JxtNewsLoader;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.controller.open.v1.util.JxtNewsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_MESSAGE_IS_TOP;

/**
 * 家长APP消息API
 *
 * @author Jia HuanYin
 * @since 2015/9/15
 */
@Controller
@RequestMapping(value = "/v1/parent/message")
@Slf4j
public class ParentMessageApiController extends AbstractParentApiController {

    @Inject
    private AppMessageLoaderClient appMessageLoaderClient;
    @Inject
    private MessageLoaderClient messageLoaderClient;
    @Inject private GlobalTagServiceClient globalTagServiceClient;

    @Inject
    private JxtNewsLoader jxtNewsLoader;

    @ImportService(interfaceClass = UserReminderService.class)
    private UserReminderService userReminderService;

    @RequestMapping(value = "list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage list() {

        try {
            validateRequest(REQ_MSG_TYPE, REQ_MSG_PAGE, REQ_MSG_SIZE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        Long parentId = getCurrentParentId();
        //屏蔽20001
        if (parentId == 20001L) {
            return successMessage("messages", new ArrayList<>());
        }
        //看1.2的版本还是否要做兼容。要做兼容就把下面的ParentMessageType.REMINDER变成这个type即可
        String type = getRequestString(REQ_MSG_TYPE);
        ParentMessageType messageType = ParentMessageType.nameOf(type);
        if (messageType == ParentMessageType.UNKNOWN) {
            return failMessage(ApiConstants.RES_RESULT_UNSUPPORT_PARENT_MESSAGE_TYPE);
        }
        int page = getRequestInt(REQ_MSG_PAGE);
        int size = getRequestInt(REQ_MSG_SIZE);
        Set<String> tagList = getUserMessageTagList(parentId);

        // FIXME 广告黑名单用户看不到全局消息，简单粗暴
        List<GlobalTag> blackParentAdUsers = globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.ParentAdBlackUsers.name());
        GlobalTag blackUser = blackParentAdUsers.stream()
                .filter(p -> Objects.equals(p.getTagValue(), SafeConverter.toString(parentId)))
                .findAny().orElse(null);

        List<AppGlobalMessage> appGlobalMessageList = new ArrayList<>();
        if (blackUser == null) {
            appGlobalMessageList = appMessageLoaderClient.appGlobalMessages(AppMessageSource.PARENT.name(), tagList)
                    .stream()
                    // 由于有些模板消息设置成了未来时间，需要这里过滤下
                    .filter(p -> new Date(p.getCreateTime()).before(new Date()))
                    .collect(Collectors.toList());
        }

        appGlobalMessageList = appGlobalMessageList.stream().filter(t -> messageType.getType().equals(t.getMessageType())).collect(Collectors.toList());
        List<AppMessage.Location> userMessageLocations = messageLoaderClient.getMessageLoader().loadAppMessageLocations(parentId);
        userMessageLocations = userMessageLocations.stream().filter(p -> messageType.getType().equals(p.getMessageType())).collect(Collectors.toList());
        //先把所有消息转成一个location.包括global的消息.再按照分页计算需要返回的消息。
        List<AppMessage.Location> locationList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userMessageLocations)) {
            locationList.addAll(userMessageLocations);
        }
        if (CollectionUtils.isNotEmpty(appGlobalMessageList)) {
            appGlobalMessageList.forEach(p -> locationList.add(globalMessageToLocation(p)));
        }
        //计算需要返回的消息
        List<AppMessage.Location> returnLocations = getReturnList(locationList, page, size);
        Set<String> needReturnUserMessageIds = returnLocations.stream().filter(p -> p.getUserId() != null).map(AppMessage.Location::getId).collect(Collectors.toSet());
        //需要返回的单个用户消息的map
        Map<String, AppMessage> appMessageMap = messageLoaderClient.getMessageLoader().loadAppMessageByIds(needReturnUserMessageIds);
        //所有global消息的map
        Map<String, AppGlobalMessage> globalMessageMap = appGlobalMessageList.stream().collect(Collectors.toMap(AppGlobalMessage::getId, e -> e));
        List<Map<String, Object>> returnList = new ArrayList<>();
        returnLocations.forEach(location -> {
            if (location.getUserId() == null) {
                returnList.add(appMessageToParentMessage(globalMessageMap.get(location.getId()), messageType));
            } else {
                returnList.add(appMessageToParentMessage(appMessageMap.get(location.getId()), messageType));
            }
        });
        returnList.forEach(p -> p.put("imageUrl", generateCnd2Img(SafeConverter.toString(p.get("imageUrl")))));
        //新增每个消息tag的icon
        returnList.forEach(p -> p.put("tagTypeImageUrl", generateNewIcon(SafeConverter.toString(p.get("tag")))));
        //新增每个消息title的颜色
        returnList.forEach(p -> p.put("tagColor", generateTagColor(SafeConverter.toString(p.get("tag")))));

        //互动消息有家长头像、家长默认头像、小编头像3种情况。。
        String parentDefaultUrl = "/public/skin/parentMobile/images/new_icon/avatar_parent_default.png";
        returnList.forEach(p -> {
            String img = SafeConverter.toString(p.get("img"));
            if (img != null) {
                if ("".equals(img)) {
                    p.put("headImg", getCdnBaseUrlStaticSharedWithSep() + parentDefaultUrl);
                } else if (img.endsWith("jzt.png")) {
                    p.put("headImg", getCdnBaseUrlStaticSharedWithSep() + img);
                } else {
                    p.put("headImg", getUserAvatarImgUrl(img));
                }
            }
        });
        userReminderService.updateReminderViewed(parentId, ReminderPosition.JZT_PARENT_MESSAGE_TAB_XTTZ);
        return successMessage("messages", returnList);
    }

    private String generateCnd2Img(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        return getCdnBaseUrlStaticSharedWithSep() + "gridfs/" + url;
    }

    private Map<String, Object> appMessageToParentMessage(Object message, ParentMessageType messageType) {
        Map<String, Object> messageMap = new HashMap<>();
        Date current = new Date();
        if (message instanceof AppMessage) {
            AppMessage userMessage = (AppMessage) message;
            Map<String, Object> extInfo = userMessage.getExtInfo();
            messageMap.put("messageId", userMessage.getId());
            messageMap.put("userId", userMessage.getUserId());
            messageMap.put("content", userMessage.getContent());
            messageMap.put("imageUrl", userMessage.getImageUrl());
            messageMap.put("linkUrl", generateLinkUrl(userMessage));
            messageMap.put("createTime", userMessage.getCreateTime());
            if (userMessage.getIsTop() != null && userMessage.getIsTop() && userMessage.getTopEndTime() > current.getTime()) {
                messageMap.put(RES_MESSAGE_IS_TOP, Boolean.TRUE);
            } else {
                messageMap.put(RES_MESSAGE_IS_TOP, Boolean.FALSE);
            }
            if (MapUtils.isNotEmpty(extInfo)) {
                extInfo.forEach(messageMap::put);
            }
        } else if (message instanceof AppGlobalMessage) {
            AppGlobalMessage globalMessage = (AppGlobalMessage) message;
            Map<String, Object> extInfo = globalMessage.getExtInfo();
            messageMap.put("messageId", globalMessage.getId());
            messageMap.put("content", globalMessage.getContent());
            messageMap.put("imageUrl", globalMessage.getImageUrl());
            messageMap.put("linkUrl", generateLinkUrl(globalMessage));
            messageMap.put("createTime", globalMessage.getCreateTime());
            if (globalMessage.getIsTop() != null && globalMessage.getIsTop() && globalMessage.getTopEndTime() > current.getTime()) {
                messageMap.put(RES_MESSAGE_IS_TOP, Boolean.TRUE);
            } else {
                messageMap.put(RES_MESSAGE_IS_TOP, Boolean.FALSE);
            }
            if (MapUtils.isNotEmpty(extInfo)) {
                extInfo.forEach(messageMap::put);
            }
        }
        messageMap.put("type", messageType.name());
        return messageMap;
    }

    private String generateLinkUrl(Object message) {
        String link = "";
        if (message == null) {
            return link;
        }
        if (message instanceof AppGlobalMessage) {
            AppGlobalMessage globalMessage = (AppGlobalMessage) message;
            if (StringUtils.isNotBlank(globalMessage.getLinkUrl())) {
                if (globalMessage.getLinkType() == null || globalMessage.getLinkType() == 1) {
                    //站内链接加上域名
                    link = generateHost2Url(globalMessage.getLinkUrl());
                } else {
                    //站外链接不处理
                    link = addHttp2Url(globalMessage.getLinkUrl());
                }
            }
        } else if (message instanceof AppMessage) {
            AppMessage userMessageDynamic = (AppMessage) message;
            if (StringUtils.isNotBlank(userMessageDynamic.getLinkUrl())) {
                if (userMessageDynamic.getLinkType() == null || userMessageDynamic.getLinkType() == 1) {
                    //站内链接加上域名
                    link = generateHost2Url(userMessageDynamic.getLinkUrl());
                } else {
                    //站外链接不处理
                    link = addHttp2Url(userMessageDynamic.getLinkUrl());
                }
            } else {
                String newsId = userMessageDynamic.getExtInfo() != null ? SafeConverter.toString(userMessageDynamic.getExtInfo().get("newsId")) : "";
                if (StringUtils.isNotBlank(newsId)) {
                    JxtNews jxtNews = jxtNewsLoader.getJxtNews(newsId);
                    if (jxtNews != null) {
                        link = JxtNewsUtil.generateJxtNewsDetailView(jxtNews, "sysmsg_commentnotification", getRequestString(REQ_APP_NATIVE_VERSION));
                    }
                }
            }
        }


        return link;
    }

    private String addHttp2Url(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        return url;
    }


    private String generateHost2Url(String link) {
        if (StringUtils.isBlank(link)) {
            return "";
        }
        return ProductConfig.getMainSiteBaseUrl() + link;
    }

    private String generateNewIcon(String tagName) {
        ParentMessageTag tag;
        if (StringUtils.isBlank(tagName)) {
            tag = ParentMessageTag.资讯;
        } else {
            tag = ParentMessageTag.nameOf(tagName);
        }
        if (tag == null || StringUtils.isBlank(tag.getIcon())) {
            tag = ParentMessageTag.资讯;
        }
        return getCdnBaseUrlStaticSharedWithSep() + tag.getIcon();
    }

    private String generateTagColor(String tagName) {
        ParentMessageTag tag = ParentMessageTag.nameOf(tagName);
        if (tag == null || StringUtils.isBlank(tag.getColor())) {
            return ParentMessageTag.资讯.getColor();
        } else {
            return tag.getColor();
        }
    }


    private List<AppMessage.Location> getReturnList(List<AppMessage.Location> messageList, int page, int size) {
        if (CollectionUtils.isEmpty(messageList)) {
            return new ArrayList<>();
        }
        int total = messageList.size();
        int statIndex = page * size;
        if (statIndex > total) {
            return new ArrayList<>();
        }
        int endIndex = total > (statIndex + size) ? statIndex + size : total;
        //先按置顶排序。再按创建时间排序
        Comparator<AppMessage.Location> comparator = (o1, o2) -> ((o2.getIsTop() == Boolean.TRUE ? 1 : 0) - (o1.getIsTop() == Boolean.TRUE ? 1 : 0));
        comparator = comparator.thenComparing((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()));
        messageList = messageList.stream()
                .sorted(comparator).collect(Collectors.toList());
        messageList = messageList.subList(statIndex, endIndex);
        return messageList;
    }


    private AppMessage.Location globalMessageToLocation(AppGlobalMessage message) {
        if (message == null) {
            return null;
        }
        AppMessage.Location location = new AppMessage.Location();
        location.setMessageType(message.getMessageType());
        location.setCreateTime(message.getCreateTime());
        location.setExpiredTime(message.getExpiredTime());
        location.setId(message.getId());
        location.setIsTop(message.getIsTop() != null && message.getIsTop() && message.getTopEndTime() != null && new Date(message.getTopEndTime()).after(new Date()));
        location.setTopEndTime(message.getTopEndTime());
        return location;
    }
}
