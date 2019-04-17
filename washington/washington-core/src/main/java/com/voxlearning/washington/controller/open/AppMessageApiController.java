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

package com.voxlearning.washington.controller.open;

import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.ExLinkedHashMap;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.runtime.system.RuntimeMode;
import com.voxlearning.alps.util.MapUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.entity.task.TeacherRookieTask;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.business.api.TeacherRookieTaskService;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;
import com.voxlearning.utopia.service.business.consumer.StudentAdvertisementInfoLoaderClient;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementPriority;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementSlot;
import com.voxlearning.utopia.service.config.client.AdvertisementSlotServiceClient;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.message.api.UserReminderLoader;
import com.voxlearning.utopia.service.message.api.UserReminderService;
import com.voxlearning.utopia.service.message.api.constant.ReminderPosition;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.api.entity.UserReminder;
import com.voxlearning.utopia.service.message.client.AppMessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.api.entity.AppUserMessageDynamic;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.UsaAdventure;
import static com.voxlearning.utopia.service.push.api.constant.AppMessageSource.*;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwe.liao
 * @since 2015/12/24
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/appmessage")
public class AppMessageApiController extends AbstractApiController {

    private static final int SIZE = 10;

    @Inject private RaikouSystem raikouSystem;

    @Inject private AdvertisementSlotServiceClient advertisementSlotServiceClient;
    @Inject private UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private AppMessageLoaderClient appMessageLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private MessageLoaderClient messageLoaderClient;
    @Inject private StudentAdvertisementInfoLoaderClient studentAdvertisementInfoLoaderClient;
    @Inject private ActivityConfigServiceClient activityConfigServiceClient;
    @ImportService(interfaceClass = TeacherRookieTaskService.class)
    private TeacherRookieTaskService teacherRookieTaskService;

    @ImportService(interfaceClass = UserReminderLoader.class)
    private UserReminderLoader userReminderLoader;

    @ImportService(interfaceClass = UserReminderService.class)
    private UserReminderService userReminderService;

    /**
     * 这个用来记录POPUP消息广告位是否已经对用户展示过
     * 在内存中做一个不可靠的映射，能解决日志过多的情况
     * Key为 UserId_AdId
     * Value 为 展示次数
     */
    private static Map<String, Integer> userAdCounter = new HashMap<>();

    //特殊广告
    private static final Set<String> SPECIAL_ADS = Sets.newHashSet("320201", "120202");
    // 控制老师报名趣味活动首页弹屏是否展示的 cache key
    private static final String TEACHER_SING_UP_ACTIVITY_POP = "TEACHER_SING_UP_ACTIVITY_POP:";
    // 控制注册15天内的老师是否弹新手任务弹窗
    private static final String TEACHER_ROOKIE_TASK_POP = "TEACHER_ROOKIE_TAKS_POP:";

    /**
     * 获取APP下的对应UID的UserMessage
     */
    @RequestMapping(value = "loaduserMessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadAppUserMessage() {
        try {
            validateRequest(REQ_APP_MESSAGE_PAGE);
            validateRequired(REQ_APP_MESSAGE_PAGE, "请求页数");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            } else {
                return failMessage("服务器错误，请重试");
            }
        }
        //上面try只验证了参数存在。所以这里还要判断有效性。
        int page = getRequestInt(REQ_APP_MESSAGE_PAGE);
        if (page < 0) {
            return failMessage("参数错误");
        }
        User user = getApiRequestUser();
        AppMessageSource source = AppMessageSource.UNKNOWN;
        VendorApps apiRequestApp = getApiRequestApp();
        ReminderPosition position = null;
        if (UserType.TEACHER.getType() == user.getUserType()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (teacherDetail == null)
                return failMessage("请登录老师号");
            //消息来源。根据当前用户和AppKey转换
            source = AppMessageUtils.getMessageSource(apiRequestApp.getAppKey(), teacherDetail);
            position = ReminderPosition.TAPP_TEACHER_INDEX_MSG;
        } else if (UserType.STUDENT.getType() == user.getUserType()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            if (studentDetail == null) {
                return failMessage("请登录学生账号");
            }
            //消息来源。根据当前用户和AppKey转换
            source = AppMessageUtils.getMessageSource(apiRequestApp.getAppKey(), studentDetail);
            position = ReminderPosition.SAPP_STUDENT_INDEX_BELL;
        }
        //用户信息
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        Set<String> tagSet = getUserMessageTagList(user.getId());

        //根据tag做过滤
        List<AppGlobalMessage> globalMessages = appMessageLoaderClient.appGlobalMessages(source.name(), tagSet)
                .stream()
                // 由于有些模板消息设置成了未来时间，需要这里过滤下
                .filter(p -> new Date(p.getCreateTime()).before(new Date()))
                .collect(Collectors.toList());

        Map<String, AppGlobalMessage> globalMessageMap = globalMessages.stream().collect(Collectors.toMap(AppGlobalMessage::getId, e -> e));
        List<AppMessage.Location> locationList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(globalMessages)) {
            globalMessages.forEach(p -> locationList.add(globalMessageToLocation(p)));
        }
        //单个用户的消息
        messageLoaderClient.getMessageLoader().loadAppMessageLocations(user.getId())
                .stream()
                .filter(p -> !StudentAppPushType.IGNORE_TYPE_SET.contains(StudentAppPushType.of(p.getMessageType())))
                .forEach(locationList::add);
        //计算需要返回那些消息
        List<AppMessage.Location> returnList = getReturnList(locationList, page, SIZE, version);
        //把需要返回的单个用户的消息查出来
        Set<String> userMessageIds = returnList.stream().filter(p -> p.getUserId() != null).map(AppMessage.Location::getId).collect(Collectors.toSet());
        Map<String, AppMessage> appMessageMap = messageLoaderClient.getMessageLoader().loadAppMessageByIds(userMessageIds);
        List<Map<String, Object>> messageList = new ArrayList<>();
        Set<String> globViewedMsgIds = new HashSet<>();
        for (Map.Entry<String, AppMessage> entry : appMessageMap.entrySet()) {
            AppMessage msg = entry.getValue();
            if(StringUtils.isNotEmpty(msg.getAppTagMsgId())){
                globViewedMsgIds.add(msg.getAppTagMsgId());
            }
        }
        returnList.forEach(p -> {
            if (p.getUserId() == null) {
                messageList.add(messageToMap(globalMessageMap.get(p.getId()),globViewedMsgIds));
            } else {
                messageList.add(messageToMap(appMessageMap.get(p.getId()),globViewedMsgIds));
            }
        });
        userReminderService.updateReminderViewed(user.getId(), position);
        return successMessage("messageList", messageList);
    }


    @RequestMapping(value = "haveViewMessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage haveViewMessage(){
        try {
            validateRequest();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            } else {
                return failMessage("服务器错误，请重试");
            }
        }

        User user = getApiRequestUser();
        AppMessageSource source = AppMessageSource.UNKNOWN;
        VendorApps apiRequestApp = getApiRequestApp();
        if (UserType.TEACHER.getType() == user.getUserType()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (teacherDetail == null)
                return failMessage("请登录老师号");
            //消息来源。根据当前用户和AppKey转换
            source = AppMessageUtils.getMessageSource(apiRequestApp.getAppKey(), teacherDetail);
        } else if (UserType.STUDENT.getType() == user.getUserType()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            if (studentDetail == null) {
                return failMessage("请登录学生账号");
            }
            //消息来源。根据当前用户和AppKey转换
            source = AppMessageUtils.getMessageSource(apiRequestApp.getAppKey(), studentDetail);
        }

        Set<String> tagSet = getUserMessageTagList(user.getId());
        //根据tag做过滤
        List<AppGlobalMessage> globalMessages = appMessageLoaderClient.appGlobalMessages(source.name(), tagSet)
                .stream()
                // 由于有些模板消息设置成了未来时间，需要这里过滤下
                .filter(p -> new Date(p.getCreateTime()).before(new Date()))
                .collect(Collectors.toList());
        Map<String, AppGlobalMessage> globalMessageMap = globalMessages.stream().collect(Collectors.toMap(AppGlobalMessage::getId, e -> e));
        List<AppMessage.Location> locationList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(globalMessages)) {
            globalMessages.forEach(p -> locationList.add(globalMessageToLocation(p)));
        }
        //单个用户的消息
        messageLoaderClient.getMessageLoader().loadAppMessageLocations(user.getId())
                .stream()
                .filter(p -> !StudentAppPushType.IGNORE_TYPE_SET.contains(StudentAppPushType.of(p.getMessageType())))
                .forEach(locationList::add);

        Set<String> userMessageIds = locationList.stream().filter(p -> p.getUserId() != null).map(AppMessage.Location::getId).collect(Collectors.toSet());
        Map<String, AppMessage> appMessageMap = messageLoaderClient.getMessageLoader().loadAppMessageByIds(userMessageIds);
        //globalMessageMap全局消息  appMessageMap 个人消息
        boolean viewed = true;
        for (Map.Entry<String, AppMessage> entry : appMessageMap.entrySet()) {
            String key = entry.getKey();
            AppMessage msg = entry.getValue();
            if(msg != null){
                if(msg.getViewed() == null){
                    viewed = false;
                    break;
                }
            }
        }
        Set<String> globViewedMsgIds = new HashSet<>();
        for (Map.Entry<String, AppMessage> entry : appMessageMap.entrySet()) {
            AppMessage msg = entry.getValue();
            if(msg != null){
                if(StringUtils.isNotEmpty(msg.getAppTagMsgId())){
                    globViewedMsgIds.add(msg.getAppTagMsgId());
                }
            }
        }
        //全局消息中去掉已经在个人信息中的记录，如果还有，则是未读的。
        for(String globMsgId : globViewedMsgIds){
            globalMessageMap.remove(globMsgId);
        }

        if(MapUtils.isNotEmpty(globalMessageMap)){
            viewed = false;
        }
        return successMessage("viewed", viewed);
    }

    /**
     * app 首页弹窗广告接口--调用新的广告系统  711上线
     */
    @RequestMapping(value = "loaduserpopupmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadUserPopupMessage() {
        try {
            validateRequest();
        } catch (Exception ex) {
            if (ex instanceof IllegalArgumentException) {
                return failMessage(ex);
            } else {
                return failMessage("服务器错误，请重试");
            }
        }
        String version = getRequestString(REQ_APP_NATIVE_VERSION);

        User user = getApiRequestUser();
        if (user == null) {
            return failMessage(ApiConstants.RES_RESULT_LOAD_USER_ERROR);
        }
        // 走新的广告位系统 120201-老师APP端-Popup消息  220401-家长APP端-Popup消息 320201-学生APP端-Popup消息
        String slotId = "";
        String appKey = getRequestString(REQ_APP_KEY);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        if (appKey.equals("17Parent")) {
            if (VersionUtil.compareVersion(ver, "2.3") >= 0) {
                slotId = "221302";
            } else {
                slotId = "220401";
            }
        } else if(appKey.equals("17Teacher")){
            if (VersionUtil.compareVersion(ver, "1.8.4") >= 0) {
                slotId = "120202";
            } else {
                slotId = "120201";
            }
        } else if(appKey.equals("17Student")){
            slotId = "320201";
        } else if(appKey.equals("17JuniorStu")){
            slotId = "350103";
        } else if(appKey.equals("17JuniorTea")){
            slotId = "150104";
        } else if(appKey.equals("17JuniorPar")){

        }

        if (StringUtils.isBlank(slotId)) {
            return failMessage(ApiConstants.RES_RESULT_LOAD_USER_ERROR);
        }
        String system = "";
        if (isIOSRequest(getRequest())) {
            system = "ios";
        } else if (isAndroidRequest(getRequest())) {
            system = "android";
        }
        AdvertisementSlot slot = advertisementSlotServiceClient.getAdvertisementSlotBuffer().load(slotId);
        if (slot == null) {
            return successMessage("messageList", new ArrayList<>());
        }
        // 双科融合提示START 2018年9月30日前
        VendorApps apps;
        if (juniorAppLimitTime() && (apps = getApiRequestApp()) != null) {
            if (washingtonCacheSystem.CBS.flushable.load("CREATE_AD:" + user.getId()) == null) {
                if (juniorStudentAppOnlineTime() && Objects.equals(apps.getAppKey(),"17Student")) {
                    StudentDetail studentDetail = getApiRequestStudentDetail();
                    if (studentDetail != null && studentDetail.getClazz() != null && (studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent())) {
                        return createAD(system, version, studentDetail.getId(), RES_RESULT_JUNIOR_LINK, RES_RESULT_JUNIOR_STUDENT_TIP, "https://cdn.17zuoye.com/static/project/app/17studentapp/images/17student_pop.png");
                    }
                } else if (juniorTeacherAppOnlineTime() && Objects.equals(apps.getAppKey(),"17Teacher")) {
                    TeacherDetail teacherDetail = getApiRequestTeacherDetail();
                    if (teacherDetail != null && teacherDetail.getKtwelve() != null && (teacherDetail.isSeniorTeacher() || teacherDetail.isJuniorTeacher())) {
                        return createAD(system, version, teacherDetail.getId(), RES_RESULT_JUNIOR_TEACHER_LINK, RES_RESULT_JUNIOR_TEACHER_TIP, "https://cdn.17zuoye.com/static/project/app/17teacherapp/images/teacher_banner_v1.png");
                    }
                }
            }
        }
        // 双科融合提示 END

        // 老师端趣味活动报名 START
        apps = getApiRequestApp();
        if (Objects.equals(apps.getAppKey(), "17Teacher")) {
            long dayDiff = DateUtils.dayDiff(new Date(), user.getCreateTime());
            if (dayDiff <= 15) {
                if (!washingtonCacheSystem.CBS.flushable.get(TEACHER_ROOKIE_TASK_POP + user.getId()).containsValue()) {
                    TeacherRookieTask teacherRookieTask = teacherRookieTaskService.loadRookieTask(user.getId());
                    if (teacherRookieTask == null) {
                        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
                        if (teacherDetail != null) {
                            ExLinkedHashMap<String, String> map = MapUtils.map(
                                    "user_id", user.getId(),
                                    "env", RuntimeMode.getCurrentStage(),
                                    "version", ver,
                                    "aid", "20190225123456",
                                    "acode", "20190225123456",
                                    "module", "m_4fgdETB5Uu",
                                    "op", "o_eo7OTYrTlO",
                                    "s0", teacherDetail.getCityName(),
                                    "s1", Optional.ofNullable(teacherDetail.getSubject()).map(Enum::name).orElse(null),
                                    "index", 1,
                                    "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                    "time", DateUtils.dateToString(new Date()),
                                    "agent", getRequest().getHeader("User-Agent"),
                                    "system", getRequestString(REQ_SYS)
                            );
                            if (RuntimeMode.lt(Mode.PRODUCTION)) {
                                log.info("teacherRookieTask pop:{}", JsonUtils.toJson(map));
                            }

                            LogCollector.instance().info("web_teacher_logs", map);
                            return createRookieTaskAD(system, version, user.getId());
                        }
                    }
                }
            }
        }
        if (Objects.equals(apps.getAppKey(), "17Teacher")) {
            if (!washingtonCacheSystem.CBS.flushable.get(TEACHER_SING_UP_ACTIVITY_POP + user.getId()).containsValue()) {
                List<ActivityConfig> activityConfigs = activityConfigServiceClient.loadNoSignUpActivity(user.getId());
                if (CollectionUtils.isNotEmpty(activityConfigs)) {
                    return createSignActivityAD(system, version, user.getId(), activityConfigs);
                }
            }
        }
        // 老师端趣味活动报名 END

        try {
            List<NewAdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(user.getId(), slotId, system, version);
            // 获取特殊广告位
            List<StudentAdvertisementInfo> studentAdvertisementInfoList = new ArrayList<>();
            if (SPECIAL_ADS.contains(slotId)) {
                final String studentSlotId = slotId;
                studentAdvertisementInfoList = studentAdvertisementInfoLoaderClient
                        .loadByUserId(user.getId())
                        .stream()
                        .filter(p -> studentSlotId.equals(p.getSlotId()))
                        .filter(p -> !ObjectUtils.anyBlank(p.getShowEndTime(),p.getShowStartTime(),p.getImgUrl()))
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(data) && CollectionUtils.isEmpty(studentAdvertisementInfoList)) {
                return successMessage("messageList", new ArrayList<>());
            }

            List<Map<String, Object>> messageList = new ArrayList<>();
            for (NewAdMapper adMapper : data) {
                messageList.add(adToMap(adMapper, system, version, user.getId()));
            }
            for (StudentAdvertisementInfo info : studentAdvertisementInfoList) {
                messageList.add(advertisementInfoToMap(info, user.getId()));
            }

            // 每次一起展示的广告加入一个统一的UUID
            String uuid = UUID.randomUUID().toString();
            for (int i = 0; i < data.size(); i++) {
                // 如果在映射中存在，不进行打点，直接下一条
                String key = currentUserId() + "_" + data.get(i).getId();
                if (userAdCounter.containsKey(key)) {
                    continue;
                }
                // 将用户与该广告的映射暂存
                userAdCounter.put(key, 1);
                if (Boolean.TRUE.equals(data.get(i).getLogCollected())) {
                    // log
                    LogCollector.instance().info("sys_new_ad_show_logs",
                            MiscUtils.map(
                                    "user_id", currentUserId(),
                                    "env", RuntimeMode.getCurrentStage(),
                                    "version", version,
                                    "aid", data.get(i).getId(),
                                    "acode", data.get(i).getCode(),
                                    "index", i,
                                    "slotId", slotId,
                                    "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                    "time", DateUtils.dateToString(new Date()),
                                    "agent", getRequest().getHeader("User-Agent"),
                                    "uuid", uuid,
                                    "system", system,
                                    "system_version", ""
                            ));
                }
            }
            return successMessage("messageList", messageList);
        } catch (Exception ex) {
            return failMessage(ApiConstants.RES_RESULT_INTERNAL_ERROR_MSG);
        }
    }

    @RequestMapping(value = "loadusernewmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadUserNewMessage() {
        try {
            validateRequired(REQ_CREATE_TIME, "起始时间");
            validateRequest(REQ_CREATE_TIME);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            } else {
                return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
            }
        }
        long createTime = getRequestLong(REQ_CREATE_TIME);
        if (createTime == 0) {
            createTime = DateUtils.calculateDateDay(new Date(), -1).getTime();
        }
        User user = getApiRequestUser();
        if (user == null) {
            return failMessage(RES_RESULT_LOAD_USER_ERROR);
        }
        AppMessageSource source = AppMessageSource.UNKNOWN;
        VendorApps requestApp = getApiRequestApp();
        if (requestApp == null) {
            return failMessage(RES_RESULT_APP_ERROR_MSG);
        }
        if (UserType.TEACHER.getType() == user.getUserType()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            source = AppMessageUtils.getMessageSource(requestApp.getAppKey(), teacherDetail);
        } else if (UserType.STUDENT.getType() == user.getUserType()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            source = AppMessageUtils.getMessageSource(requestApp.getAppKey(), studentDetail);
        } else if (UserType.PARENT.getType() == user.getUserType()) {
            source = AppMessageUtils.getMessageSource(requestApp.getAppKey(), null);
        }

        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        Set<String> tagSet = getUserMessageTagList(user.getId());
        long newCount = getNewMessageCount(source, user.getId(), createTime, tagSet, ver);
        boolean hadNew = false;
        if (newCount > 0) {
            hadNew = true;
        }
        return successMessage(RES_RESULT_HAD_NEW_MESSAGE, hadNew).add(RES_RESULT_NEW_MESSAGE_COUNT, newCount);
    }

    /**
     * 给用户发站内信
     */
    @RequestMapping(value = "sendusermessage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendAppUserMessage() {
        try {
            String sessionKey = getRequestString(REQ_SESSION_KEY);
            if(StringUtils.isNotBlank(sessionKey)){
                validateRequest(REQ_APP_MESSAGE_TYPE, REQ_APP_MESSAGE_UID, REQ_APP_MESSAGE_TITLE, REQ_APP_MESSAGE_CONTENT, REQ_APP_MESSAGE_IMGURL,
                        REQ_APP_MESSAGE_LINKURL, REQ_APP_MESSAGE_LINKTYPE, REQ_APP_MESSAGE_EXTINFO);
            }else {
                validateRequestNoSessionKey(REQ_APP_MESSAGE_TYPE, REQ_APP_MESSAGE_UID, REQ_APP_MESSAGE_TITLE, REQ_APP_MESSAGE_CONTENT, REQ_APP_MESSAGE_IMGURL,
                        REQ_APP_MESSAGE_LINKURL, REQ_APP_MESSAGE_LINKTYPE, REQ_APP_MESSAGE_EXTINFO);
            }
            validateRequired(REQ_APP_MESSAGE_UID, "用户ID");
        } catch (Exception ex) {
            if (ex instanceof IllegalArgumentException) {
                return failMessage(ex);
            } else {
                return failMessage("服务器错误，请重试");
            }
        }
        try {
            Integer messageType = getRequestInt(REQ_APP_MESSAGE_TYPE);
            String userIdStr = getRequestString(REQ_APP_MESSAGE_UID);
            String title = getRequestString(REQ_APP_MESSAGE_TITLE);
            String content = getRequestString(REQ_APP_MESSAGE_CONTENT);
            String imgUrl = getRequestString(REQ_APP_MESSAGE_IMGURL);
            String linkUrl = getRequestString(REQ_APP_MESSAGE_LINKURL);
            Integer linkType = getRequestInt(REQ_APP_MESSAGE_LINKTYPE);
            String extInfoStr = getRequestString(REQ_APP_MESSAGE_EXTINFO);

            List<Long> userIdList = JsonUtils.fromJsonToList(userIdStr, Long.class);
            List<AppMessage> userMessageList = AppMessageUtils.generateAppUserMessage(userIdList, messageType, title, content, imgUrl, linkUrl, linkType, extInfoStr);
            userMessageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
            return successMessage("发送成功");
        } catch (Exception e) {
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }
    }

    /**
     * 给单个学生的所有家长发送Jpush消息
     */
    @RequestMapping(value = "sendjpushmessagetosps.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendJpushMessageToStudentParents() {
        try {
            validateRequired(REQ_APP_MESSAGE_CONTENT, "通知文本内容");
            validateRequired(REQ_APP_MESSAGE_EXTINFO, "扩展信息");
            validateRequired(REQ_APP_MESSAGE_UID, "用户ID");
            validateRequestNoSessionKey(REQ_APP_MESSAGE_CONTENT, REQ_APP_MESSAGE_EXTINFO, REQ_APP_MESSAGE_UID);
        } catch (IllegalVendorUserException ex) {
            return failMessage(ex);
        } catch (IllegalArgumentException ex) {
            return failMessage(ex);
        }

        try {
            String appKey = getApiRequestApp().getAppKey();
            String content = getRequestString(REQ_APP_MESSAGE_CONTENT);
            String extInfoStr = getRequestString(REQ_APP_MESSAGE_EXTINFO);

            User user = raikouSystem.loadUser(SafeConverter.toLong(getRequestString(REQ_APP_MESSAGE_UID)));
            if (user.fetchUserType() != UserType.STUDENT) return failMessage("用户类型错误");

            VendorAppsUserRef appUserRef = vendorLoaderClient.loadVendorAppUserRef(appKey, user.getId());
            if (appUserRef == null) return failMessage("无效的用户ID");

            AppMessageSource source = AppMessageSource.of(appKey.toUpperCase());
            if (source == AppMessageSource.UNKNOWN) return failMessage("消息来源错误");

            //诺亚 三国　走美即将下线，停止报告推送
            if (source == A17ZYSPG || source == TRAVELAMERICA || source == SANGUODMZ) {
                return successMessage("操作成功");
            }

            // FIXME: 2016/11/23 如果是凌晨0点到7点发送的jpush，发送时间随机到8点30到11点30
            // FIXME: 2016/11/23 临时处理方式，等底层做匀速发送
            List<StudentParent> parents = parentLoaderClient.loadStudentParents(user.getId());
            List<Long> userIdList = parents.stream().map(p -> p.getParentUser().getId()).collect(Collectors.toList());
            Map<String, Object> extInfo = JsonUtils.fromJson(extInfoStr);
            extInfo.put("tag", ParentMessageTag.报告.name());
            extInfo.put("s", ParentAppPushType.REPORT.name());

            Date current = new Date();
            String date = DateUtils.dateToString(current, DateUtils.FORMAT_SQL_DATE);
            if (Arrays.asList("0", "1", "2", "3", "4", "5", "6").contains(DateUtils.dateToString(current, "H"))) {
                long start = DateUtils.stringToDate(date + " 08:30:00", DateUtils.FORMAT_SQL_DATETIME).getTime();
                long delta = RandomUtils.nextInt(10800) * 1000L;
                appMessageServiceClient.sendAppJpushMessageByIds(content, source, userIdList, extInfo, start + delta);
            } else {
                appMessageServiceClient.sendAppJpushMessageByIds(content, source, userIdList, extInfo);
            }

            String link = SafeConverter.toString(extInfo.get("url"));
            List<AppUserMessageDynamic> mesgs = new ArrayList<>();
            for (Long parentId : userIdList) {
                AppUserMessageDynamic message = new AppUserMessageDynamic();
                message.setUserId(parentId);
                message.setContent(content);
                message.setLinkUrl(link);
                message.setLinkType(0);
                message.setMessageType(ParentMessageType.REMINDER.getType());
                Map<String, Object> ext = new HashMap<>();
                ext.put("studentId", user.getId());
                ext.put("tag", ParentMessageTag.报告.name());
                ext.put("type", ParentMessageType.REMINDER.name());
                ext.put("appKey", appKey);
                message.setExtInfo(ext);
                mesgs.add(message);
            }
            appMessageServiceClient.saveAppUserDynamicMessage(mesgs);

            //发送公众号消息 Feature #35841
            OrderProductServiceType orderProductServiceType = OrderProductServiceType.valueOf(appKey);
            if (orderProductServiceType == UsaAdventure ||
                    orderProductServiceType == OrderProductServiceType.GreatAdventure) {
                sendOfficeAccountsMessage(orderProductServiceType, userIdList, user, link);
            }
            return successMessage("操作成功");
        } catch (Exception e) {
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }
    }

    /**
     * 给单个学生的所有家长发送Jpush消息
     */
    @RequestMapping(value = "sendjpushmessagetostudentparents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendJpushMessage() {
        try {
            validateRequired(REQ_APP_MESSAGE_CONTENT, "通知文本内容");
            validateRequired(REQ_APP_MESSAGE_EXTINFO, "扩展信息");
            validateRequest(REQ_APP_MESSAGE_CONTENT, REQ_APP_MESSAGE_EXTINFO);
        } catch (IllegalVendorUserException ex) {
            return failMessage(ex);
        } catch (IllegalArgumentException ex) {
            return failMessage(ex);
        }

        try {
            User user = getApiRequestUser();
            if (user.fetchUserType() != UserType.STUDENT) return failMessage("用户类型错误");

            String appKey = getApiRequestApp().getAppKey();
            String content = getRequestString(REQ_APP_MESSAGE_CONTENT);
            String extInfoStr = getRequestString(REQ_APP_MESSAGE_EXTINFO);

            AppMessageSource source = AppMessageSource.of(appKey.toUpperCase());
            if (source == AppMessageSource.UNKNOWN) return failMessage("消息来源错误");

            //诺亚 三国　走美即将下线，停止报告推送
            if (source == A17ZYSPG
                    || source == AppMessageSource.TRAVELAMERICA
                    || source == AppMessageSource.SANGUODMZ) {
                return successMessage("操作成功");
            }

            List<StudentParent> parents = parentLoaderClient.loadStudentParents(user.getId());
            List<Long> userIdList = parents.stream().map(p -> p.getParentUser().getId()).collect(Collectors.toList());
            Map<String, Object> extInfo = JsonUtils.fromJson(extInfoStr);
            extInfo.put("tag", ParentMessageTag.报告.name());
            extInfo.put("s", ParentAppPushType.REPORT.name());
            appMessageServiceClient.sendAppJpushMessageByIds(content, source, userIdList, extInfo);

            String link = SafeConverter.toString(extInfo.get("url"));
            List<AppMessage> mesgs = new ArrayList<>();
            for (Long parentId : userIdList) {
                AppMessage message = new AppMessage();
                message.setUserId(parentId);
                message.setContent(content);
                message.setLinkUrl(link);
                message.setLinkType(0);
                message.setMessageType(ParentMessageType.REMINDER.getType());
                Map<String, Object> ext = new HashMap<>();
                ext.put("studentId", user.getId());
                ext.put("tag", ParentMessageTag.报告.name());
                ext.put("type", ParentMessageType.REMINDER.name());
                ext.put("appKey", appKey);
                message.setExtInfo(ext);
                mesgs.add(message);
            }
            mesgs.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);

            //发送公众号消息 Feature #35841
            OrderProductServiceType orderProductServiceType = OrderProductServiceType.valueOf(appKey);
            if (orderProductServiceType == UsaAdventure ||
                    orderProductServiceType == OrderProductServiceType.GreatAdventure) {
                sendOfficeAccountsMessage(orderProductServiceType, userIdList, user, link);
            }
            return successMessage("操作成功");
        } catch (Exception e) {
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }
    }

    /**
     * 给用户发Popup消息
     */
    @RequestMapping(value = "sendpopupmessage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendPopupMessage() {
        try {
            validateRequestNoSessionKey(REQ_APP_MESSAGE_SOURCE, REQ_APP_MESSAGE_TYPE, REQ_APP_MESSAGE_UID, REQ_APP_MESSAGE_CONTENT, REQ_APP_MESSAGE_IMGURL,
                    REQ_APP_MESSAGE_LINKURL, REQ_APP_MESSAGE_LINKTYPE, REQ_APP_MESSAGE_BTN_CONTENT, REQ_APP_MESSAGE_START, REQ_APP_MESSAGE_END, REQ_APP_MESSAGE_RANK, REQ_APP_MESSAGE_EXTINFO);
        } catch (Exception ex) {
            if (ex instanceof IllegalArgumentException) {
                return failMessage(ex);
            } else {
                return failMessage("服务器错误，请重试");
            }
        }
        // FIXME:现在其实没有没有发的逻辑，AppPopupMessage这玩意都不用了
        return successMessage("发送成功");
    }

    private MapMessage createAD(String system, String version, Long uid, String url, String content, String image) {
        NewAdMapper mapper = new NewAdMapper();
        Date now = new Date();
        String dateId = DateUtils.dateToString(now, "yyyyMMdd");
        mapper.setId(Long.valueOf(dateId));
        mapper.setBtnContent("去下载");
        mapper.setUrl(url);
        mapper.setContent(content);
        mapper.setImg(image);
        Date startDate = DateUtils.stringToDate(dateId, "yyyyMMdd");
        mapper.setShowStartTime(startDate.getTime());
        // 第二天的0点
        Long endTime = DateUtils.nextDay(startDate, 1).getTime();
        mapper.setShowEndTime(endTime);
        mapper.setHasUrl(true);
        mapper.setPriority(AdvertisementPriority.TOP);
        // 每天只弹一次
        washingtonCacheSystem.CBS.flushable.set("CREATE_AD:"+uid, DateUtils.getCurrentToDayEndSecond(), 1);
        return successMessage("messageList", Collections.singletonList(adToMap(mapper, system, version, uid)));
    }

    private MapMessage createRookieTaskAD(String system, String version, Long uid) {
        String imageUrl = "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/tasksystem_index_banner.png";

        // String url, String content, String image;
        NewAdMapper mapper = new NewAdMapper();
        Date now = new Date();
        String dateId = DateUtils.dateToString(now, "yyyyMMdd");
        mapper.setId(Long.valueOf(dateId));
        mapper.setBtnContent("领取任务");
        mapper.setUrl("/view/mobile/teacher/activity2018/primary/task_system/novicetask?source=home_page_task_pop");
        mapper.setContent("");
        mapper.setImg(imageUrl);
        Date startDate = DateUtils.stringToDate(dateId, "yyyyMMdd");
        mapper.setShowStartTime(startDate.getTime());
        // 第二天的0点
        Long endTime = DateUtils.nextDay(startDate, 1).getTime();
        mapper.setShowEndTime(endTime);
        mapper.setHasUrl(true);
        mapper.setPriority(AdvertisementPriority.TOP);

        // 注册15天之内只弹一次
        Date endDate = DateUtils.addDays(new Date(), 17); // 考虑到非0点进行计算的偏差，多存两天
        long currentToDateEndSecond = DateUtils.getCurrentToDateEndSecond(endDate);
        washingtonCacheSystem.CBS.flushable.set(TEACHER_ROOKIE_TASK_POP + uid, SafeConverter.toInt(currentToDateEndSecond, 0), 1);
        return successMessage("messageList", Collections.singletonList(adToMap(mapper, system, version, uid)));
    }

    private MapMessage createSignActivityAD(String system, String version, Long uid, List<ActivityConfig> configs) {
        configs.sort(Comparator.comparing(ActivityConfig::getStartTime));
        ActivityConfig config = configs.get(0);

        String imageUrl = "";
        if (config.getType().equals(ActivityTypeEnum.TANGRAM)) {
            imageUrl = "https://cdn-cnc.17zuoye.cn/resources/mobile/student/images/jigsawpuzzle_640320.png";
        } else if (config.getType().equals(ActivityTypeEnum.TWENTY_FOUR)) {
            imageUrl = "https://cdn-cnc.17zuoye.cn/resources/mobile/student/images/twentyfourpoints_640320.png";
        } else {
            imageUrl = "https://cdn-cnc.17zuoye.cn/resources/mobile/student/images/sudoku_640320.png";
        }

        // String url, String content, String image;
        NewAdMapper mapper = new NewAdMapper();
        Date now = new Date();
        String dateId = DateUtils.dateToString(now, "yyyyMMdd");
        mapper.setId(Long.valueOf(dateId));
        mapper.setBtnContent("立即查看");
        mapper.setUrl("/view/mobile/teacher/activity2018/funny_activity/detail?activityId=" + config.getId());
        mapper.setContent(config.getTitle() + "正在进行，点击“立即查看”并报名即可参与");
        mapper.setImg(imageUrl);
        Date startDate = DateUtils.stringToDate(dateId, "yyyyMMdd");
        mapper.setShowStartTime(startDate.getTime());
        // 第二天的0点
        Long endTime = DateUtils.nextDay(startDate, 1).getTime();
        mapper.setShowEndTime(endTime);
        mapper.setHasUrl(true);
        mapper.setPriority(AdvertisementPriority.TOP);
        // 每天只弹一次
        washingtonCacheSystem.CBS.flushable.set(TEACHER_SING_UP_ACTIVITY_POP + uid, DateUtils.getCurrentToDayEndSecond(), 1);
        return successMessage("messageList", Collections.singletonList(adToMap(mapper, system, version, uid)));
    }

    // 广告内容转成app需要的map
    private Map<String, Object> adToMap(NewAdMapper adMapper, String system, String version, Long uid) {
        Map<String, Object> map = new HashMap<>();
        if (adMapper == null) {
            return map;
        }
        map.put("id", adMapper.getId().toString()); // 必填 string类型， 旧版APP IOS版本有类型校验
        map.put("uid", uid);
        map.put("type", 0);  // 这里是Int 壳那边的代码定死了类型
        map.put("title", "");// 暂时不给了
        map.put("content", adMapper.getContent()); // 必填
        map.put("imgUrl", generateCnd2Img(adMapper.getImg())); // 必填
        if (adMapper.getHasUrl()) {
            String url;
            if (adMapper.getUrl() != null && (adMapper.getUrl().contains("/view/mobile/common/download")
                    || adMapper.getUrl().contains("/view/mobile/teacher/activity2018/funny_activity/detail")
                    || adMapper.getUrl().contains("/view/mobile/teacher/activity2018/primary/task_system/novicetask")
            )) {
                url = adMapper.getUrl();
            } else {
                url = AdvertiseRedirectUtils.redirectUrl(adMapper.getId(), 0, version, system, "", 0L);
            }
            map.put("linkUrl", generateLinkUrl(url));
        } else {
            map.put("linkUrl", "");
        }
        map.put("extInfo", Collections.emptyMap()); // 这里要给一个空MAP 壳儿那边有验证
        map.put("ct", "");
        if (adMapper.getPriority().getLevel() == 0) {
            map.put("isTop", Boolean.TRUE);
        } else {
            map.put("isTop", Boolean.FALSE);
        }
        map.put("btnContent", adMapper.getBtnContent()); // 必填
        map.put("start", adMapper.getShowStartTime()); // 必填
        map.put("end", adMapper.getShowEndTime()); // 必填
        map.put("rank", adMapper.getPriority().getLevel());
        return map;
    }

    private Map<String, Object> advertisementInfoToMap(StudentAdvertisementInfo info, Long userId) {
        Map<String, Object> map = new HashMap<>();
        if (info == null) {
            return map;
        }
        map.put("id", info.getId()); // 必填 string类型， 旧版APP IOS版本有类型校验
        map.put("uid", userId);
        map.put("type", 0);  // 这里是Int 壳那边的代码定死了类型
        map.put("title", "");// 暂时不给了
        map.put("content", info.getMessageText()); // 必填
        map.put("imgUrl", info.getImgUrl()); // 必填
        map.put("linkUrl", info.getClickUrl());
        map.put("extInfo", Collections.emptyMap()); // 这里要给一个空MAP 壳儿那边有验证
        map.put("ct", "");
        map.put("isTop", Boolean.TRUE);
        map.put("btnContent", info.getBtnContent()); // 必填
        map.put("start", info.getShowStartTime()); // 必填
        map.put("end", info.getShowEndTime()); // 必填
        map.put("rank", 0);
        return map;
    }

    //消息转成map
    private Map<String, Object> messageToMap(Object object,Set<String> globMsgIds) {
        Date current = new Date();
        Map<String, Object> map = new HashMap<>();
        if (object == null) {
            return map;
        }
        if (object instanceof AppMessage) {
            AppMessage message = (AppMessage) object;
            map.put("id", message.getId());
            map.put("type", message.getMessageType());
            map.put("title", message.getTitle());
            map.put("content", message.getContent());
            map.put("imgUrl", generateCnd2Img(message.getImageUrl()));
            map.put("viewed",message.getViewed());
            if (StringUtils.isNotBlank(message.getLinkUrl())) {
                if (message.getLinkType() == null || message.getLinkType() == 1) {
                    map.put("linkUrl", generateLinkUrl(message.getLinkUrl()));
                } else {
                    map.put("linkUrl", message.getLinkUrl());
                }
            }
            map.put("extInfo", message.getExtInfo());
            map.put("ct", message.getCreateTime());
            if (message.getIsTop() != null && message.getIsTop() && message.getTopEndTime() > current.getTime()) {
                map.put("isTop", Boolean.TRUE);
            } else {
                map.put("isTop", Boolean.FALSE);
            }
            return map;
        } else if (object instanceof AppGlobalMessage) {
            AppGlobalMessage message = (AppGlobalMessage) object;
            map.put("id", message.getId());
            map.put("type", message.getMessageType());
            map.put("title", message.getTitle());
            map.put("content", message.getContent());
            map.put("imgUrl", generateCnd2Img(message.getImageUrl()));
            if(CollectionUtils.isNotEmpty(globMsgIds) && globMsgIds.contains(message.getId())){
                map.put("viewed",Boolean.TRUE);
            }else{
                map.put("viewed",Boolean.FALSE);
            }
            if (StringUtils.isNotBlank(message.getLinkUrl())) {
                if (message.getLinkType() == null || message.getLinkType() == 1) {
                    //站内链接加上cdn域名
                    map.put("linkUrl", generateLinkUrl(message.getLinkUrl()));
                } else {
                    //站外链接不处理
                    map.put("linkUrl", message.getLinkUrl());
                }
            }
            map.put("extInfo", message.getExtInfo());
            map.put("ct", message.getCreateTime());
            if (message.getIsTop() != null && message.getIsTop() && message.getTopEndTime() != null && message.getTopEndTime() > current.getTime()) {
                map.put("isTop", Boolean.TRUE);
            } else {
                map.put("isTop", Boolean.FALSE);
            }
            return map;
        }
        return map;
    }

    private String generateCnd2Img(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return getCdnBaseUrlStaticSharedWithSep() + "gridfs/" + url;
    }

    private List<AppMessage.Location> getReturnList(List<AppMessage.Location> mapList, int page, int size, String version) {
        if (CollectionUtils.isEmpty(mapList)) {
            return new ArrayList<>();
        }
        //过滤掉版本控制的消息
        Iterator<AppMessage.Location> iterator = mapList.iterator();
        while (iterator.hasNext()) {
            StudentAppPushType type = StudentAppPushType.of(iterator.next().getMessageType());
            if (type == null) {
                continue;
            }
            //低于起始版本。高于截止版本.移除
            if (StudentAppPushType.IGNORE_TYPE_SET.contains(type) || VersionUtil.compareVersion(version, type.getStartVersion()) < 0 || VersionUtil.compareVersion(version, type.getEndVersion()) >= 0) {
                iterator.remove();
            }
        }
        //先按置顶排序。再按创建时间排序
        Comparator<AppMessage.Location> comparator = (o1, o2) -> (o2.getIsTop() == Boolean.TRUE ? 1 : 0) - (o1.getIsTop() == Boolean.TRUE ? 1 : 0);
        comparator = comparator.thenComparing((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()));
        mapList = mapList
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
        int total = mapList.size();
        int statIndex = page * size;
        if (statIndex > total) {
            return new ArrayList<>();
        }
        int endIndex = total > (statIndex + size) ? statIndex + size : total;
        mapList = mapList.subList(statIndex, endIndex);
        return mapList;
    }

    private String generateLinkUrl(String link) {
        if (StringUtils.isBlank(link)) {
            return "";
        }

        // FIXME 兼容题库反馈的数据
        if (link.startsWith("http://") || link.startsWith("https://")) {
            return link;
        }

        return fetchMainsiteUrlByCurrentSchema() + link;
    }

    private long getNewMessageCount(AppMessageSource source, Long userId, Long createTime, Collection<String> tagSet, String ver) {
        //根据tag做过滤
        List<AppGlobalMessage> globalMessages = appMessageLoaderClient.appGlobalMessages(source.name(), tagSet)
                .stream()
                // 由于有些模板消息设置成了未来时间，需要这里过滤下
                .filter(p -> new Date(p.getCreateTime()).before(new Date()))
                .collect(Collectors.toList());
        List<Map<String, Object>> messageList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(globalMessages)) {
            globalMessages.forEach(p -> messageList.add(messageToMap(p,null)));
        }

        long count = 0;
        if (CollectionUtils.isNotEmpty(globalMessages)) {
            count += globalMessages.stream()
                    .filter(p -> p.getCreateTime() > createTime)
                    //这个是学生端的成就消息不进消息中心
                    .filter(p -> source != AppMessageSource.STUDENT || !StudentAppPushType.IGNORE_TYPE_SET.contains(StudentAppPushType.of(p.getMessageType())))
                    //这个是学生端(这里面已经即有中学又有小学的了。)的消息有版本控制的需要处理
                    .filter(p -> StudentAppPushType.of(p.getMessageType()) == null || (VersionUtil.compareVersion(ver, StudentAppPushType.of(p.getMessageType()).getStartVersion()) >= 0 && VersionUtil.compareVersion(ver, StudentAppPushType.of(p.getMessageType()).getEndVersion()) < 0))
                    //这个是家长端只要Reminder的消息
                    .filter(p -> source != AppMessageSource.PARENT || Objects.equals(ParentMessageType.REMINDER.getType(), p.getMessageType()))
                    .count();
        }
        if (source == AppMessageSource.PARENT) {
            UserReminder userReminder = userReminderLoader.loadUserReminder(userId, ReminderPosition.JZT_PARENT_MESSAGE_TAB_XTTZ);
            if (userReminder != null && userReminder.getLastUpdateTime() > createTime && userReminder.getCounter() != null) {
                count += userReminder.getCounter();
            }

        } else if (source == AppMessageSource.STUDENT) {
            UserReminder userReminder = userReminderLoader.loadUserReminder(userId, ReminderPosition.SAPP_STUDENT_INDEX_BELL);
            if (userReminder != null && userReminder.getLastUpdateTime() - 5000 > createTime) {  //这里减5秒,时间有误差
                count += 1; //红点类型的直接加1就行了
            }
        } else if (source == AppMessageSource.JUNIOR_STUDENT){
            UserReminder userReminder = userReminderLoader.loadUserReminder(userId, ReminderPosition.SAPP_STUDENT_INDEX_BELL);
            if (userReminder != null && userReminder.getLastUpdateTime() - 5000 > createTime) {
                count += 1; //红点类型的直接加1就行了
            }
        }else { //老师的咯
            UserReminder userReminder = userReminderLoader.loadUserReminder(userId, ReminderPosition.TAPP_TEACHER_INDEX_MSG);
            if (userReminder != null && userReminder.getLastUpdateTime() > createTime && userReminder.getCounter() != null) {
                count += userReminder.getCounter();
            }
        }
        return count;
    }

    private void sendOfficeAccountsMessage(OrderProductServiceType orderProductServiceType, List<Long> userIdList,
                                           User user, String link) {
        String pattern = "{0}家长您好， {0}的{1}{2}学习报告已出，可点击详情查看。";
        String product = orderProductServiceType == UsaAdventure ? "走遍美国学英语" : "酷跑学单词";
        String title = product + "学习报告";
        String reportType = "每周";

        //走美有周报与月报区分
        if (orderProductServiceType == UsaAdventure && "MONTH".equals(getRequestString("report_type"))) {
            reportType = "每月";
        }
        String cont = MessageFormat.format(pattern, user.fetchRealname(), product, reportType);
        Map<String, Object> ext = new HashMap<>();
        ext.put("accountsKey", "fairyland");
        officialAccountsServiceClient.sendMessage(userIdList, title, cont, link, JsonUtils.toJson(ext), false);
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
