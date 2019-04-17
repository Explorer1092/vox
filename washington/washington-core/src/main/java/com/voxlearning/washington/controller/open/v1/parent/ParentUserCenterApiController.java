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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructLesson;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.ParentRemindingType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.business.api.constant.AppUseNumCalculateType;
import com.voxlearning.utopia.core.ObjectIdEntity;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.message.api.UserReminderLoader;
import com.voxlearning.utopia.service.message.api.constant.ReminderPosition;
import com.voxlearning.utopia.service.message.api.constant.ReminderType;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.api.entity.UserReminder;
import com.voxlearning.utopia.service.message.client.AppMessageLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitV2Service;
import com.voxlearning.utopia.service.parent.api.ParentWishLoader;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parentreward.cache.ParentRewardCacheManager;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.reminder.api.mapper.ReminderContext;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.VendorAppsServiceClient;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.mapper.*;
import com.voxlearning.washington.support.ParentUserCenterNativeFunction;
import com.voxlearning.washington.support.SessionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Parent API for App
 * Created by Hailong yang on 2015/09/09.
 */
@Controller
@RequestMapping(value = "/v1/parent/ucenter")
@Slf4j
public class ParentUserCenterApiController extends AbstractParentApiController {

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject
    private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Inject
    private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject
    private UserBlacklistServiceClient userBlacklistServiceClient;

    @ImportService(interfaceClass = MonitorRecruitV2Service.class)
    private MonitorRecruitV2Service monitorRecruitV2Service;
    @Inject
    private AppMessageLoaderClient appMessageLoaderClient;
    @ImportService(interfaceClass = UserReminderLoader.class)
    private UserReminderLoader userReminderLoader;
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @Inject
    private VendorAppsServiceClient vendorAppsServiceClient;

    @ImportService(interfaceClass = ParentWishLoader.class)
    private ParentWishLoader parentWishLoader;

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    private String parentTabListKey = "parentTabList_New";
    private String parentSignKey;

    private static final String TODAY_SPREAD_XIAOU_FLAG = "TODAY_SPREAD_XIAOU_FLAG_";
    private static final String TODAY_SPREAD_STUDY_TOGETHER_FLAG = "TODAY_SPREAD_STUDY_TOGETHER_FLAG_";
    private static final String EVENING_SPREAD_STUDY_TOGETHER_FLAG = "EVENING_SPREAD_STUDY_TOGETHER_FLAG_";
    private static final String PARENT_FUNCTION_CONFIG_ORDER_URL = "view/mobile/parent/17my_shell/order.vpage?useNewCore=wk&";
    private static final String PARENT_FUNCTION_CONFIG_WISH_URL = "/view/mobile/parent/17wish_list/index.vpage";
    private static final Map<String, Integer> xiaoUMap;
    private static final List<SelfStudyType> selfStudyTypeList;

    static {
        selfStudyTypeList = new ArrayList<>();
        selfStudyTypeList.add(SelfStudyType.AFENTI_ENGLISH);
        selfStudyTypeList.add(SelfStudyType.AFENTI_MATH);
        selfStudyTypeList.add(SelfStudyType.AFENTI_CHINESE);
        xiaoUMap = new HashMap<>();
        xiaoUMap.put(OrderProductServiceType.AfentiExam.name(), 1);
        xiaoUMap.put(OrderProductServiceType.AfentiMath.name(), 2);
        xiaoUMap.put(OrderProductServiceType.AfentiChinese.name(), 3);
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    enum RecommendType {
        LOGIN("登录页", "LOGIN", "NATIVE", "https://oss-image.17zuoye.com/diandu/2018/08/31/20180831195410993771.png"),
        PicListen("点读机", "PIC_LISTEN", "NATIVE", "https://oss-image.17zuoye.com/diandu/2018/08/31/20180831195410993771.png"),
        AfentiExam("小U课程", "/zion/nova-report?subject=ENGLISH", "H5", "https://oss-image.17zuoye.com/ue/2018/08/31/20180831195449733679.png"),
        AfentiMath("小U课程", "/zion/nova-report?subject=MATH", "H5", "https://oss-image.17zuoye.com/um/2018/08/31/20180831195507032107.png"),
        AfentiChinese("小U课程", "/zion/nova-report?subject=CHINESE", "H5", "https://oss-image.17zuoye.com/uc/2018/08/31/20180831195524150660.png"),
        XiaoUSummary("小U课程", "/view/mobile/parent/learning_app/detail.vpage?showAppList=true&productType=AfentiExam&order_refer=330260", "H5", "https://oss-image.17zuoye.com/ue/2018/08/31/20180831195449733679.png"),
        StudyTogether("一起学训练营", "/view/mobile/parent/17xue_train/my_train.vpage?source=cj", "H5", "https://oss-image.17zuoye.com/xly/2018/09/04/20180904200106670157.png"),
        PictureBook("英文绘本", "/view/mobile/parent/picture_books/index.vpage?source=cj", "H5", "https://oss-image.17zuoye.com/huiben/2018/08/31/20180831195430782501.png");

        private final String recommendSource;
        private final String recommendUrl;
        private final String recommendType;
        private final String recommendIcon;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Mode current = com.voxlearning.alps.runtime.RuntimeMode.current();
        if (current == Mode.DEVELOPMENT) {
            current = Mode.TEST;
        }
        parentSignKey = "parentSign_" + current.name();
    }

    //将孩子号与家长号关联（需要验证孩子密码）
    @RequestMapping(value = "bindstudentparentwithpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindStudentParentWithPwd() {
        return failMessage(RES_RESULT_UNSUPPORT_ANSWER_EXAM);
    }

    //家长重置学生密码
    @RequestMapping(value = "resetchildpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetStudentPwd() {
        MapMessage resultMap = new MapMessage();
        Long parentId = currentUserId();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String pwd = getRequestString(REQ_PARENT_APP_DIRTY_PASSWD);
        String ver = getRequestString("ver");

        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_PARENT_APP_DIRTY_PASSWD, "密码");
            validateRequest(REQ_STUDENT_ID, REQ_PARENT_APP_DIRTY_PASSWD);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            if (clazz != null && clazz.isTerminalClazz()) {
                return failMessage("暂不支持小学毕业账号").add("isGraduate", Boolean.TRUE);
            }

            if (studentId <= 0) {
                return failMessage(MessageFormat.format(RES_RESULT_STUDENT_ID_ERROR_MSG, studentId));
            }

            if (parentId == null) {
                return failMessage("cookie stale dated");
            }

            //验证家长与孩子关系
            List<User> children = studentLoaderClient.loadParentStudents(parentId);
            User childCandidate = null;
            for (User child : children) {
                if (Objects.equals(child.getId(), studentId)) {
                    childCandidate = child;
                    break;
                }
            }
            if (childCandidate == null) {
                return failMessage("学生与家长无关联关系");
            }

            //验证家长是否已绑手机
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parentId);
            if (userAuthentication == null || !userAuthentication.isMobileAuthenticated()) {
                return failMessage("家长未绑定手机");
            }
            //修改密码
            MapMessage message = userServiceClient.setPassword(childCandidate, pwd);
            if (message.isSuccess()) {
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(studentId);
                userServiceRecord.setOperatorId(parentId.toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("修改密码");
                userServiceRecord.setComments("家长[" + parentId + "]重置孩子[" + studentId + "]密码，操作端[app]");
                userServiceRecord.setAdditions("refer:ParentUserCenterApiController.resetStudentPwd");
                userServiceClient.saveUserServiceRecord(userServiceRecord);

                // 修改学生端sessionkey，让学生重新登录
                VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", studentId);
                if (vendorAppsUserRef != null) {
                    vendorServiceClient.expireSessionKey("17Student", studentId,
                            SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), studentId));
                }
                return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                return failMessage("家长未绑定手机");
            }
        } catch (Exception ex) {
            String message;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else {
                log.error("update student password failed. studentId:{},parentId:{}, ver:{}, pwd:{}",
                        studentId, parentId, ver, pwd, ex);
                message = "重置密码失败";
            }
            return failMessage(message);
        }
    }

    /**
     * 获取学生学豆信息
     */
    @RequestMapping(value = "getUserIntegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserIntegral() {
        MapMessage resultMap = new MapMessage();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String ver = getRequestString("ver");

        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        try {

            if (studentId <= 0) {
                return failMessage(MessageFormat.format(RES_RESULT_STUDENT_ID_ERROR_MSG, studentId));
            }

            StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
            if (student == null || student.getUserIntegral() == null) {
                return failMessage("学豆信息不存在");
            }
            resultMap.add("usable", student.getUserIntegral().getUsable());
            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } catch (Exception ex) {
            String message;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else {
                log.error("update student password failed. studentId:{}, ver:{}", studentId, ver, ex);
                message = "获取学豆信息失败";
            }
            return failMessage(message);
        }
    }

    /**
     * 个人中心功能配置接口
     * 首先 没用ext_tab 那一套, 还要研究清楚 那一套代码简直烦躁。。。。而且这配置几乎很少会改动, 用BlockContent即可。
     * 然后运营消息目前 可以从配置里配置,如果某个运营消息需要动态展示,再另做处理。据说会有一套统一的消息提醒系统,后面再接,先把跟壳的协议定好。
     * 个人中心配置分为 学生相关配置 和 运营相关配置 分别对应 studentFunctionList   operativeFunctionList  是两个配置哦。
     * 功能类型 见
     */
    @RequestMapping(value = "/function/list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage functionList() {
        Boolean isLogin = false;
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            String sessionKey = getRequestString(REQ_SESSION_KEY);
            if (StringUtils.isNoneBlank(sessionKey)) {
                validateRequest(REQ_STUDENT_ID);
                isLogin = true;
            } else {
                validateRequestNoSessionKey(REQ_STUDENT_ID);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId != 0L) {
            Boolean checkRelation = checkStudentParentRef(studentId, getCurrentParentId());
            if (!checkRelation) {
                return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
            }
        }
        String version = getRequestString(REQ_APP_NATIVE_VERSION);

        List<Map<String, Object>> studentFunctionList = new ArrayList<>();
        StudentDetail studentDetail = null;

        //如果未登录或者没有孩子(没有孩子studentId =0 ),则不显示学生相关的配置。
        if (isLogin && studentId != 0L) {
            studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail != null) {
                studentFunctionList = generateFunctionList("studentFunctionList", studentDetail, version, false);
            }
        }

        // 接下来处理运营相关的功能配置
        List<Map<String, Object>> operativeFunctionList = generateFunctionList("operativeFunctionList", studentDetail, version, true);
        //处理广告配置
        List<Map<String, Object>> advertisementFunctionList = new ArrayList<>();
        if (hasSessionKey()) {
            //未登录不显示这个
            advertisementFunctionList = generateFunctionList("advertisementFunctionList", studentDetail, version, false);
        }

        MapMessage result = successMessage();
        //他么还有加上一个个人信息,真是醉啦。
        if (isLogin) {
            User parent = getCurrentParent();
            if (parent != null) {
                MapMessage userInfoMapMessage = parentUserBasicInfo(getCurrentParent(),
                        getRequestString(REQ_APP_NATIVE_VERSION));
                if (userInfoMapMessage != null) {
                    userInfoMapMessage.remove(RES_RESULT);
                    result.add(RES_USER_INFO, userInfoMapMessage);
                }
            }
        }

        return result.add(RES_STUDENT_FUNCTION_LIST, studentFunctionList)
                .add(RES_OPERATIVE_FUNCTION_LIST, operativeFunctionList)
                .add(RES_ADVERTISEMENT_FUNCTION_LIST, advertisementFunctionList);
    }

    @RequestMapping(value = "/skin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getSkinConfig() {
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PALACE_SKIN_CONFIG");
        if (StringUtils.isNotBlank(configValue)) {
            Map<String, Object> configMap = JsonUtils.fromJson(configValue);
            if (MapUtils.isNotEmpty(configMap)) {
                String startDateStr = SafeConverter.toString(configMap.get("startTime"), "");
                Date startDate = DateUtils.stringToDate(startDateStr, "yyyy-MM-dd");
                String endDateStr = SafeConverter.toString(configMap.get("endTime"), "");
                Date endDate = DateUtils.stringToDate(endDateStr, "yyyy-MM-dd");
                String skinSignAndroid = SafeConverter.toString(configMap.get("skinSignAndroid"), "");
                String skinUrlAndroid = SafeConverter.toString(configMap.get("skinUrlAndroid"), "");
                String skinSigniOS = SafeConverter.toString(configMap.get("skinSigniOS"), "");
                String skinUrliOS = SafeConverter.toString(configMap.get("skinUrliOS"), "");
                return successMessage().add("start_time", startDate.getTime())
                        .add("end_time", endDate.getTime())
                        .add("skin_sgin_android", skinSignAndroid)
                        .add("skin_sgin_ios", skinSigniOS)
                        .add("skin_url_android", skinUrlAndroid)
                        .add("skin_url_ios", skinUrliOS);
            }
        }
        return successMessage();
    }

    //家长通底部tab配置
    @RequestMapping(value = "/tab/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage tabList() {
        User parent = getCurrentParent();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        long createTime = getRequestLong(REQ_CREATE_TIME);
        String tabPosition = getRequestString(REQ_REMIND_POSITION);
        if (createTime == 0) {
            createTime = DayRange.current().getStartTime();
        }
        com.voxlearning.utopia.service.reminder.constant.ReminderPosition reminderPosition = com.voxlearning.utopia.service.reminder.constant.ReminderPosition.of(tabPosition);
        if (reminderPosition != null && parent != null) {
            reminderService.clearUserReminder(parent.getId(), reminderPosition);
        }

        List<ParentTabConfig> parentTabConfigList = pageBlockContentServiceClient.loadConfigList("parentTabConfig", parentTabListKey, ParentTabConfig.class);
        List<Map<String, Object>> tabMapList = generateTabList(parentTabConfigList, parent, ver, createTime);
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "TAB_REQUEST_TIME_INTERVAL");

        int defaultTabIndex = getDefaultTabIndex(parent);
        return successMessage()
                .add(RES_PARENT_TAB_LIST, tabMapList)
                .add(RES_TAB_REQUEST_TIME_INTERVAL, SafeConverter.toLong(configValue) * 60 * 1000)
                .add(RES_PARENT_TAB_INDEX, defaultTabIndex);
    }
    
    //未登录用户或没孩子用户默认显示tab 0
    private int getDefaultTabIndex(User parent) {
        int defaultTabIndex = 2;
        if (null == parent) {
            defaultTabIndex = 0;
        } else {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                defaultTabIndex = 0;
            }
        }
        return defaultTabIndex;
    }

//    private int getTabIndex(User parent) {
//        if (parent != null) {
//            String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PARENT_TAB_INDEX_CONFIG");
//            List<ParentTabIndexGrayConfig> tabIndexGrayConfigs = JsonUtils.fromJsonToList(configValue, ParentTabIndexGrayConfig.class);
//            if (CollectionUtils.isNotEmpty(tabIndexGrayConfigs)) {
//                StudentParentRef studentParentRef = parentLoaderClient.loadParentStudentRefs(parent.getId())
//                        .stream()
//                        .findFirst()
//                        .orElse(null);
//                if (studentParentRef != null) {
//                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentParentRef.getStudentId());
//                    if (studentDetail != null) {
//                        ParentTabIndexGrayConfig config = tabIndexGrayConfigs.stream()
//                                .filter(e -> {
//                                    String regionCode = SafeConverter.toString(studentDetail.getStudentSchoolRegionCode());
//                                    String grayExpress = regionCode + "_" + (parent.getId() % 2);
//                                    return grayExpress.matches(e.getGray());
//                                })
//                                .findFirst()
//                                .orElse(null);
//                        if (config != null) {
//                            return config.getIndex();
//                        }
//                    }
//                }
//            }
//        }
//        return 0;
//    }

    @RequestMapping(value = "webview/config.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage webviewConfig() {
        List<WebViewConfig> webViewConfigs = pageBlockContentServiceClient.loadConfigList("jztAppConfig", "webViewConfig", WebViewConfig.class);
        return successMessage().add("open_need_source", webViewConfigs);
    }

    @RequestMapping(value = "/login/config.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loginConfig() {
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "IOS_NOT_LOGIN_VERSION");
        return successMessage().add("is_show", !VersionUtil.checkVersionConfig(configValue, getRequestString(REQ_APP_NATIVE_VERSION)));
    }

    @RequestMapping(value = "/ball/info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ballInfo() {
        try {
            validateRequired(REQ_AD_POSITION, "广告位ID");
            if (hasSessionKey()) {
                validateRequest(REQ_AD_POSITION);
            } else {
                validateRequestNoSessionKey(REQ_AD_POSITION);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String slotId = getRequestString(REQ_AD_POSITION);
        User requestUser = getApiRequestUser();
        Long userId = requestUser == null ? null : requestUser.getId();
        //根据广告位id查询广告信息
        List<Map<String, Object>> mapList = getAdById(userId, slotId);
        if (mapList == null) {
            return successMessage();
        }
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "BALL_AD_CLOSE_TIME");
        int closeTime = SafeConverter.toInt(configValue) * 60;

        return successMessage().add(RES_RESULT_AD_INFO, mapList).add(RES_RESULT_AD_CLOSE_TIME, closeTime);
    }

    private List<Map<String, Object>> getAdById(Long userId, String slotId) {
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(userId, slotId, getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));
        if (CollectionUtils.isEmpty(newAdMappers)) {
            return null;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (int i = 0; i < newAdMappers.size(); i++) {
            NewAdMapper newAdMapper = newAdMappers.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_AD_ID, newAdMapper.getId());
            map.put(RES_RESULT_AD_IMG, combineCdbUrl(newAdMapper.getImg()));
            String link = AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), i, getRequestString(REQ_APP_NATIVE_VERSION), getRequestString(REQ_SYS), "", 0L);
            map.put(RES_RESULT_AD_URL, ProductConfig.getMainSiteBaseUrl() + link);
            mapList.add(map);

            if (Boolean.FALSE.equals(newAdMappers.get(i).getLogCollected())) {
                continue;
            }
            //曝光打点
            LogCollector.info("sys_new_ad_show_logs",
                    MiscUtils.map(
                            "user_id", userId,
                            "env", RuntimeMode.getCurrentStage(),
                            "version", getRequestString("version"),
                            "aid", newAdMapper.getId(),
                            "acode", newAdMapper.getCode(),
                            "index", i,
                            "slotId", slotId,
                            "client_ip", getWebRequestContext().getRealRemoteAddress(),
                            "time", DateUtils.dateToString(new Date()),
                            "agent", getRequest().getHeader("User-Agent"),
                            "uuid", UUID.randomUUID().toString(),
                            "system", getRequestString(REQ_SYS),
                            "system_version", getRequestString("sysVer")
                    ));
        }
        return mapList;
    }

    @RequestMapping(value = "study/plan/recommend.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studyPlanRecommend() {
        User parent = getCurrentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        boolean inGray = false;
        if (studentDetail != null && !studentDetail.isSeniorStudent() && !studentDetail.isJuniorStudent() && !parent.getId().equals(20001L)) {
            inGray = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jzt", "StudyPlanRecommend");
        }
        if (!inGray) {
            return successMessage().add(RES_STUDY_PLAN_RECOMMEND_INFO, Collections.emptyMap());
        }

        int timeQuantum = getTimeQuantum();
        String recommendGreetings = "";
        Map<String, Object> recommendMap = new HashMap<>();
        if (timeQuantum == 1) {
            recommendMap = getMorningRecommend(studentId);
            recommendGreetings = "Hi,早上好";
        }
        if (timeQuantum == 2) {
            recommendMap = getAfternoonRecommend(studentId, timeQuantum);
            recommendGreetings = "Hi，下午好";
        }
        if (timeQuantum == 3) {
            recommendMap = getEveningRecommend(studentId, timeQuantum);
            recommendGreetings = "Hi，晚上好";
        }
        if (MapUtils.isNotEmpty(recommendMap)) {
            recommendMap.put(RES_STUDY_PLAN_RECOMMEND_GREETINGS, recommendGreetings);
            if (StringUtils.isNotBlank(SafeConverter.toString(recommendMap.get(RES_STUDY_PLAN_RECOMMEND_URL)))) {
                recommendMap.put(RES_STUDY_PLAN_RECOMMEND_URL_TEXT, "立即学习");
            }
        }
        return successMessage().add(RES_STUDY_PLAN_RECOMMEND_INFO, recommendMap)
                .add(RES_STUDY_PLAN_RECOMMEND_TIME_QUANTUM, timeQuantum);
    }

    /**
     * 当前所处推荐时间段。-1-不推荐，1-早晨，2-课后，3-睡前
     */
    private int getTimeQuantum() {
        String timeConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STUDY_PLAN_TIME_CONFIG");
        String[] timeArr = timeConfig.split(";");
        if (timeArr.length != 3) {
            return 0;
        }
        for (int index = 0; index < timeArr.length; index++) {
            String time = timeArr[index];
            String startTime = time.split("-")[0];
            Date startDate = DateUtils.stringToDate(startTime + ":00", "HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int startHour = calendar.get(Calendar.HOUR_OF_DAY);
            int startMinute = calendar.get(Calendar.MINUTE);

            String endTime = time.split("-")[1];
            Date endDate = DateUtils.stringToDate(endTime + ":00", "HH:mm:ss");
            calendar.setTime(endDate);
            int endHour = calendar.get(Calendar.HOUR_OF_DAY);
            int endMinute = calendar.get(Calendar.MINUTE);
            boolean startTimeFlag = (LocalTime.now().getHour() == startHour && LocalTime.now().getMinute() >= startMinute) || LocalTime.now().getHour() > startHour;
            boolean endTimeFlag = (LocalTime.now().getHour() == endHour && LocalTime.now().getMinute() <= endMinute) || LocalTime.now().getHour() < endHour;
            if (startTimeFlag && endTimeFlag) {
                return index + 1;
            }
        }
        return 0;
    }

    /**
     * 早晨推荐
     */
    private Map<String, Object> getMorningRecommend(Long studentId) {
        Map<String, Object> map = new HashMap<>();
        String recommendText;
        String recommendSource = RecommendType.PicListen.getRecommendSource();
        String recommendUrl;
        String recommendSociety;
        String recommendIcon = RecommendType.PicListen.getRecommendIcon();
        String recommendFunctionType;
        String unitName = getLatestHomeworkUnitName(studentId);
        List<String> randomTexts = new ArrayList<>();
        if (StringUtils.isNotBlank(unitName)) {
            recommendText = "英语练习显示孩子已经学到《" + unitName + "》啦，快点打开点读机听读同步课文吧";
        } else {
            randomTexts.add("一天之际在于晨，打开点读机播放同步英语课本，在英文环境中练习听力与起床两不误哦");
            randomTexts.add("每日清晨的英文输入让脱口而出成为水到渠成的事，快来打开课本点读练习英文吧");
            randomTexts.add("晨起适合读书哦，打开点读机，给孩子10分钟，背单词，练发音，让流利英语轻松说出来");
            Collections.shuffle(randomTexts);
            recommendText = randomTexts.get(0);
        }
        recommendUrl = RecommendType.PicListen.getRecommendUrl();
        recommendSociety = getPicListenText();
        recommendFunctionType = RecommendType.PicListen.getRecommendType();
        map.put(RES_STUDY_PLAN_RECOMMEND_TEXT, recommendText);
        map.put(RES_STUDY_PLAN_RECOMMEND_SOURCE, recommendSource);
        map.put(RES_STUDY_PLAN_RECOMMEND_URL, recommendUrl);
        map.put(RES_STUDY_PLAN_RECOMMEND_ICON, recommendIcon);
        map.put(RES_STUDY_PLAN_RECOMMEND_FUNCTION_TYPE, recommendFunctionType);
        map.put(RES_STUDY_PLAN_RECOMMEND_SOCIETY, recommendSociety);
        return map;
    }

    /**
     * 放学后推荐
     */
    private Map<String, Object> getAfternoonRecommend(Long studentId, Integer timeQuantum) {
        Map<String, Object> map = new HashMap<>();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        String recommendText = "";
        String recommendSource = "";
        String recommendUrl = "";
        String recommendIcon = "";
        String recommendFunctionType = "";
        String recommendSociety = "";
        //是否是小U付费用户
        boolean isXiaoUPayUser = isXiaoUPayUser(studentId);
        //是否完成全部付费的小U今日课程
        boolean hasFinishAllXiaoU = StringUtils.isBlank(unfinishXiaoU(studentId));
        //今日是否推广过小U
        boolean hasSpreadXiaoUToday = hasSpreadXiaoUToday(studentId);
        if (isXiaoUPayUser) {
            if (!hasFinishAllXiaoU) {
                //小U付费用户 && 没有完成全部付费小U的今日课程     ---小U课程页
                List<String> randomTexts = new ArrayList<>();
                randomTexts.add("宝贝今天的学习还顺利吗？智能的小U课程会帮助孩子的学习更有效哦");
                randomTexts.add("宝贝今天在学校学习了什么内容呢？小U课程正在等你一起来巩固课上所学哦");
                randomTexts.add("完成一天的学习，来巩固下今日知识吧，每天10分钟，轻松查漏补缺");
                Collections.shuffle(randomTexts);
                recommendText = randomTexts.get(0);
                RecommendType recommendType = Arrays.stream(RecommendType.values()).filter(e -> e.name().equals(unfinishXiaoU(studentId))).findFirst().orElse(null);
                if (recommendType != null) {
                    recommendUrl = ProductConfig.getMainSiteBaseUrl() + recommendType.getRecommendUrl();
                    recommendFunctionType = recommendType.getRecommendType();
                    recommendIcon = recommendType.getRecommendIcon();
                }
                recommendSociety = getXiaoUSociety(false, studentDetail);
                //小U推广
                recordTodaySpreadXiaoU(studentId);
            } else if (hasSpreadXiaoUToday) {
                //小U付费用户 && 完成了全部付费小U的今日课程 && 今日推广过小U       ---小U鼓励状态，不跳转
                List<String> randomTexts = new ArrayList<>();
                randomTexts.add("宝贝儿今天的小U课程已经全部完成啦，一定要好好鼓励自觉的好孩子哦");
                randomTexts.add("今天宝贝的小U课程完成的很棒，希望明天能够继续坚持哦，加油！");
                Collections.shuffle(randomTexts);
                recommendText = randomTexts.get(0);
                recommendSociety = getXiaoUSociety(true, studentDetail);
                recommendIcon = RecommendType.AfentiExam.getRecommendIcon();
            }
            recommendSource = RecommendType.AfentiExam.getRecommendSource();
        }

        //小U非付费用户 || （完成了付费小U今日所有课程 && 今日没有推广过小U）
        if (!isXiaoUPayUser || (hasFinishAllXiaoU && !hasSpreadXiaoUToday)) {
            List<StudyGroup> studyGroups = studyTogetherServiceClient.loadStudentActiveLessonGroups(studentId);
            List<Long> lessonIds = studyGroups.stream().map(x -> SafeConverter.toLong(x.getLessonId())).collect(Collectors.toList());
            List<StudyLesson> studyLessons = new ArrayList<>(studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLessons(lessonIds).values());

            boolean activeStudyTogether = activeStudyTogether(studyLessons);
            boolean hasFinishAllLessonToday = hasFinishAllLessonToday(studyLessons, studentId);
            boolean hasSpreadStudyTogetherToday = hasSpreadStudyTogetherToday(studentId);

            if (activeStudyTogether) {
                if (!hasFinishAllLessonToday) {
                    //激活过训练营 && 今日训练营课程没有全部完成       ---我的训练营
                    List<String> randomTexts = new ArrayList<>();
                    randomTexts.add("吃晚饭之前，快来训练营补充一下知识吧，做一个上知天文下知地理的小学霸");
                    randomTexts.add("放学啦！孩子今日的训练营已经准备好啦，每天10分钟，轻松超越同龄人");
                    randomTexts.add("孩子忙了一天啦，要不要换换脑筋，来一起学训练营听一听有趣的知识呢");
                    Collections.shuffle(randomTexts);
                    recommendText = randomTexts.get(0);
                    recommendUrl = ProductConfig.getMainSiteBaseUrl() + RecommendType.StudyTogether.getRecommendUrl() + "&timeQuantum=" + timeQuantum;
                    recommendSociety = getUnfinishStudyTogetherSociety();
                    recommendFunctionType = RecommendType.StudyTogether.getRecommendType();
                    //记录点读机推广
                    recordTodaySpreadStudyTogether(studentId);
                } else if (hasSpreadStudyTogetherToday) {
                    //激活过训练营 && 今日训练营课程全部完成 && 今日推广过训练营     ---训练营鼓励状态，不跳转
                    List<String> randomTexts = new ArrayList<>();
                    randomTexts.add("恭喜宝贝完成了今天的训练营课程，课外知识又扩充咯，快给Ta鼓励吧");
                    randomTexts.add("今日训练营课程宝贝完成的很棒，希望明天能够继续坚持哦，加油！");
                    Collections.shuffle(randomTexts);
                    recommendText = randomTexts.get(0);
                    recommendSociety = getFinishStudyTogetherSociety();
                }
                recommendSource = RecommendType.StudyTogether.getRecommendSource();
                recommendIcon = RecommendType.StudyTogether.getRecommendIcon();
            }

            //没有激活过训练营 || （今日训练营课程全部完成 && 今日没有推广过训练营）
            if (!activeStudyTogether || (hasFinishAllLessonToday && !hasSpreadStudyTogetherToday)) {
                boolean isPrimaryStudent = studentDetail.isPrimaryStudent() && studentDetail.getClazz() != null;
                boolean inBlackList = userBlacklistServiceClient.isInUserBlackList(studentDetail);
                if (!isXiaoUSpreadDate() || isXiaoUPayUser || !isPrimaryStudent || inBlackList) {
                    //不是小U推广日 || （是小U推广日 && 是小U付费用户）    ---英文绘本
                    List<String> randomTexts = new ArrayList<>();
                    randomTexts.add("语言学习重在使用，绘本阅读既能从故事中领悟道理，又能在语义中扩充单词哦！");
                    randomTexts.add("背单词一点都不痛苦哦，好玩儿的绘本让孩子轻松记单词～学有所用！");
                    randomTexts.add("完成了一天的学习，现在放松一下吧，读读好玩儿的英文故事，还能认识新单词呢！");
                    Collections.shuffle(randomTexts);
                    recommendText = randomTexts.get(0);
                    recommendSource = RecommendType.PictureBook.getRecommendSource();
                    recommendUrl = ProductConfig.getMainSiteBaseUrl() + RecommendType.PictureBook.getRecommendUrl();
                    recommendIcon = RecommendType.PictureBook.getRecommendIcon();
                    recommendFunctionType = RecommendType.PictureBook.getRecommendType();
                    recommendSociety = getPictureBookText();
                } else {
                    //小U推广日 && 不是小U付费用户     ---小U固定文案，不跳转
                    recommendText = "天天用小U，门门都是优！快为孩子开通智能的小U同步练习，加强有效学习吧";
                    recommendSource = RecommendType.XiaoUSummary.getRecommendSource();
                    recommendUrl = RecommendType.XiaoUSummary.getRecommendUrl();
                    recommendIcon = RecommendType.XiaoUSummary.getRecommendIcon();
                    recommendFunctionType = RecommendType.XiaoUSummary.getRecommendType();
                    recommendSociety = getSpreadXiaoUSociety();
                }
            }
        }
        map.put(RES_STUDY_PLAN_RECOMMEND_TEXT, recommendText);
        map.put(RES_STUDY_PLAN_RECOMMEND_SOURCE, recommendSource);
        map.put(RES_STUDY_PLAN_RECOMMEND_URL, recommendUrl);
        map.put(RES_STUDY_PLAN_RECOMMEND_SOCIETY, recommendSociety);
        map.put(RES_STUDY_PLAN_RECOMMEND_ICON, recommendIcon);
        map.put(RES_STUDY_PLAN_RECOMMEND_FUNCTION_TYPE, recommendFunctionType);
        return map;
    }

    /**
     * 睡前推荐
     */
    private Map<String, Object> getEveningRecommend(Long studentId, Integer timeQuantum) {
        Map<String, Object> map = new HashMap<>();
        List<StudyGroup> studyGroups = studyTogetherServiceClient.loadStudentActiveLessonGroups(studentId);
        List<Long> lessonIds = studyGroups.stream().map(x -> SafeConverter.toLong(x.getLessonId())).collect(Collectors.toList());
        List<StudyLesson> studyLessons = new ArrayList<>(studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLessons(lessonIds).values());

        List<String> randomTexts = new ArrayList<>();
        String recommendText = "";
        String recommendSource = "";
        String recommendUrl = "";
        String recommendSociety = "";
        String recommendIcon = "";
        String recommendFunctionType = "";
        boolean activeStudyTogether = activeStudyTogether(studyLessons);
        boolean hasSpreadStudyTogetherEvening = hasSpreadStudyTogetherEvening(studentId);
        boolean hasFinishAllLessonToday = hasFinishAllLessonToday(studyLessons, studentId);
        if (activeStudyTogether) {
            if (!hasFinishAllLessonToday) {
                //激活过训练营并且没有完成当日全部课程
                randomTexts = new ArrayList<>();
                randomTexts.add("碎片时间的长期利用会让孩子有不一样的收获，快叫宝贝在睡前GET一些新知识吧");
                randomTexts.add("知识的获取能达到内心的满足，快利用睡前的碎片化时间让宝贝扩展知识吧～");
                randomTexts.add("夜深了适合安静的读书，训练营为宝贝准备了一些有用的小知识哦，快来看看吧");
                Collections.shuffle(randomTexts);
                recommendText = randomTexts.get(0);
                recommendUrl = ProductConfig.getMainSiteBaseUrl() + RecommendType.StudyTogether.getRecommendUrl() + "&timeQuantum=" + timeQuantum;
                recommendSociety = getUnfinishStudyTogetherSociety();
                //记录点训练营推广
                recordEveningSpreadStudyTogether(studentId);
            } else if (hasSpreadStudyTogetherEvening) {
                //激活过训练营，完成了今日全部课程并且推广过训练营
                randomTexts = new ArrayList<>();
                randomTexts.add("宝贝累了一天啦，还认真完成了训练营课程真的太棒啦，快些入梦吧，晚安～");
                randomTexts.add("碎片时间的长期利用能达到轻松且质的飞跃，恭喜今天的又一次飞跃，早点休息哦");
                Collections.shuffle(randomTexts);
                recommendText = randomTexts.get(0);
                recommendUrl = "";
                recommendSociety = getFinishStudyTogetherSociety();
            }
            recommendSource = RecommendType.StudyTogether.getRecommendSource();
            recommendIcon = RecommendType.StudyTogether.getRecommendIcon();
            recommendFunctionType = RecommendType.StudyTogether.getRecommendType();
        }
        if (!activeStudyTogether || (hasFinishAllLessonToday && !hasSpreadStudyTogetherEvening)) {
            String unitName = getLatestHomeworkUnitName(studentId);
            if (StringUtils.isNotBlank(unitName)) {
                recommendText = "英语作业显示孩子学到《" + unitName + "》啦，睡前打开点读机，听本单元可巩固知识，听新单元可预习课文哦";
            } else {
                randomTexts.add("睡前让宝贝来听听英文课文吧，每天10分钟，搞定复习与预习，还能磨出英文小耳朵~");
                randomTexts.add("在睡觉之前，不妨听一听英文课本，让宝贝在睡前再次沉浸在英文的环境中哦");
                randomTexts.add("夜深了，让今天学习的课文伴随宝贝进入梦乡，潜移默化提升英语语感和听力哦~");
                Collections.shuffle(randomTexts);
                recommendText = randomTexts.get(0);
            }
            recommendSource = RecommendType.PicListen.getRecommendSource();
            recommendUrl = RecommendType.PicListen.getRecommendUrl();
            recommendIcon = RecommendType.PicListen.getRecommendIcon();
            recommendFunctionType = RecommendType.PicListen.getRecommendType();
            recommendSociety = getPicListenText();
        }

        map.put(RES_STUDY_PLAN_RECOMMEND_TEXT, recommendText);
        map.put(RES_STUDY_PLAN_RECOMMEND_SOURCE, recommendSource);
        map.put(RES_STUDY_PLAN_RECOMMEND_URL, recommendUrl);
        map.put(RES_STUDY_PLAN_RECOMMEND_SOCIETY, recommendSociety);
        map.put(RES_STUDY_PLAN_RECOMMEND_ICON, recommendIcon);
        map.put(RES_STUDY_PLAN_RECOMMEND_FUNCTION_TYPE, recommendFunctionType);
        return map;
    }

    //******************************************学习计划推荐START***********************************************
    private String getPicListenText() {
        int randomNum = RandomUtils.nextInt(7, 19);
        return "有" + randomNum + "万名小学生正在使用";
    }

    private String getPictureBookText() {
        int randomNum = RandomUtils.nextInt(700, 7000);
        return "有" + randomNum + "名小学生正在听读";
    }

    /**
     * 今天是不是小U推广日
     */
    private boolean isXiaoUSpreadDate() {
        String dates = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "XIAOU_SPREAD_DATE");
        if (StringUtils.isNotBlank(dates)) {
            String[] dateArr = dates.split(",");
            List<String> dateList = Arrays.asList(dateArr);
            return dateList.contains(DateUtils.dateToString(new Date(), "yyyy-MM-dd"));
        }
        return false;
    }

    private List<SelfStudyType> xiaoUPaidList(Long studentId) {
        Map<SelfStudyType, DayRange> map = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentId, false);
        List<SelfStudyType> list = new ArrayList<>();
        map.entrySet().stream()
                .filter(entrySet -> selfStudyTypeList.contains(entrySet.getKey()))
                .filter(entrySet -> entrySet.getValue().getEndDate().after(DayRange.current().getEndDate()))
                .forEach(entrySet -> list.add(entrySet.getKey()));
        return list;
    }

    /**
     * 是否是小U付费用户
     */
    private boolean isXiaoUPayUser(Long studentId) {
        return xiaoUPaidList(studentId).size() > 0;
    }

    /**
     * 未完成小U类型
     */
    private String unfinishXiaoU(Long studentId) {
        Set<String> xiaoUFinishSet = ParentRewardCacheManager.INSTANCE.getXiaoUFinishSet(studentId);
        Set<String> xiaoUPaidSet = xiaoUPaidList(studentId).stream().map(e -> e.getOrderProductServiceType()).collect(Collectors.toSet());
        Map.Entry<String, Integer> entry = xiaoUMap.entrySet().stream()
                .filter(e -> xiaoUPaidSet.contains(e.getKey()))
                .filter(e -> !xiaoUFinishSet.contains(e.getKey()))
                .min(Comparator.comparing(Map.Entry::getValue))
                .orElse(null);
        if (entry != null) {
            return entry.getKey();
        }
        return "";
    }

    /**
     * 记录今天是否推广过小U
     */
    private void recordTodaySpreadXiaoU(Long studentId) {
        CacheSystem.CBS.getCache("persistence").incr(TODAY_SPREAD_XIAOU_FLAG + studentId, 1, 1, DateUtils.getCurrentToDayEndSecond());
    }

    /**
     * 今天是否推广过小U
     */
    private boolean hasSpreadXiaoUToday(Long studentId) {
        CacheObject<Object> cacheObject = CacheSystem.CBS.getCache("persistence").get(TODAY_SPREAD_XIAOU_FLAG + studentId);
        return cacheObject != null && cacheObject.getValue() != null;
    }

    /**
     * 记录今天是否推广一起学训练营
     */
    private void recordTodaySpreadStudyTogether(Long studentId) {
        CacheSystem.CBS.getCache("persistence").incr(TODAY_SPREAD_STUDY_TOGETHER_FLAG + studentId, 1, 1, DateUtils.getCurrentToDayEndSecond());
    }

    /**
     * 今天是否推广过一起学训练营
     */
    private boolean hasSpreadStudyTogetherToday(Long studentId) {
        CacheObject<Object> cacheObject = CacheSystem.CBS.getCache("persistence").get(TODAY_SPREAD_STUDY_TOGETHER_FLAG + studentId);
        return cacheObject != null && cacheObject.getValue() != null;
    }

    /**
     * 记录睡前是否推广一起学训练营
     */
    private void recordEveningSpreadStudyTogether(Long studentId) {
        CacheSystem.CBS.getCache("persistence").incr(EVENING_SPREAD_STUDY_TOGETHER_FLAG + studentId, 1, 1, DateUtils.getCurrentToDayEndSecond());
    }

    /**
     * 睡前是否推广过一起学训练营
     */
    private boolean hasSpreadStudyTogetherEvening(Long studentId) {
        CacheObject<Object> cacheObject = CacheSystem.CBS.getCache("persistence").get(EVENING_SPREAD_STUDY_TOGETHER_FLAG + studentId);
        return cacheObject != null && cacheObject.getValue() != null;
    }

    /**
     * 是否激活过训练营
     */
    private boolean activeStudyTogether(List<StudyLesson> studyLessons) {
        Date now = new Date();
        return studyLessons.stream().anyMatch(studyLesson -> now.after(studyLesson.getOpenDate()) && now.before(studyLesson.getCloseDate()));
    }

    /**
     * 今日课程是否全部完成
     */
    private boolean hasFinishAllLessonToday(List<StudyLesson> studyLessons, Long studentId) {
        boolean finishAllLesson = true;
        for (StudyLesson studyLesson : studyLessons) {
            String lessonId = SafeConverter.toString(studyLesson.getLessonId());
            List<CourseStructLesson> courseLessonList = studyLesson.getCourseLessonList();
            //今天是否有课
            boolean hasLesson = CollectionUtils.isNotEmpty(courseLessonList) && courseLessonList.stream().anyMatch(courseLesson -> DayRange.current().contains(courseLesson.getOpenDate()));
            if (hasLesson) {
                Map<String, Integer> finishInfo = studyTogetherServiceClient.loadStudentTodayFinishInfo(lessonId, studentId).getUninterruptibly();
                if (SafeConverter.toInt(finishInfo.get("star")) == -1) {
                    finishAllLesson = false;
                    break;
                }
            }
        }
        return finishAllLesson;
    }

    /**
     * 学生一个月内最新的作业的一个单元名
     */
    private String getLatestHomeworkUnitName(Long studentId) {
        String unitName = "";
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        Set<Long> groupIds = GroupMapper.filter(groupMappers).idSet();
        Date endDate = new Date();
        Date startDate = DateUtils.calculateDateDay(endDate, -30);
        NewHomework.Location location = newHomeworkPartLoaderClient.loadNewHomeworkByClazzGroupId(groupIds, startDate, endDate)
                .stream()
                .filter(e -> e.getSubject() == Subject.ENGLISH)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .findFirst()
                .orElse(null);
        if (location != null) {
            NewHomeworkBook newHomeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(location.getId());
            if (newHomeworkBook != null) {
                LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practices = newHomeworkBook.getPractices();
                if (MapUtils.isNotEmpty(practices)) {
                    List<NewHomeworkBookInfo> bookInfos = practices.values().stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    Collections.reverse(bookInfos);
                    NewHomeworkBookInfo bookInfo = bookInfos.stream().findFirst().orElse(null);
                    if (bookInfo != null) {
                        unitName = bookInfo.getUnitName();
                    }
                }
            }
        }
        return unitName;
    }

    /**
     * 小U推广社交
     */
    private String getSpreadXiaoUSociety() {
        List<String> serviceTypes = new ArrayList<>();
        serviceTypes.add(OrderProductServiceType.AfentiExam.name());
        serviceTypes.add(OrderProductServiceType.AfentiMath.name());
        serviceTypes.add(OrderProductServiceType.AfentiChinese.name());
        String template = "有{}名小学生正在参与";
        Map<String, Integer> numMap = businessVendorServiceClient.loadNationNum(serviceTypes);
        long num = numMap.values().stream().mapToLong(SafeConverter::toLong).sum();
        String numStr = num < 10000 ? String.valueOf(num) : String.valueOf((num + 5000) / 10000) + "万";
        return StringUtils.formatMessage(template, numStr);
    }

    /**
     * 训练营未完成社交
     */
    private String getUnfinishStudyTogetherSociety() {
        String finishCount = getLessonFinishCount();
        String joinCount = getLessonJoinCount();
        List<String> societyList = new ArrayList<>();
        societyList.add(joinCount + "名小学生正在一起努力");
        societyList.add("今日已有" + finishCount + "名小学生完成");
        societyList.add("有" + joinCount + "名小学生正在奋发向上");
        Collections.shuffle(societyList);
        return societyList.get(0);
    }

    /**
     * 训练营完成社交
     */
    private String getFinishStudyTogetherSociety() {
        String finishCount = getLessonFinishCount();
        String joinCount = getLessonJoinCount();
        List<String> societyList = new ArrayList<>();
        societyList.add("今日已有" + finishCount + "名小学生完成");
        societyList.add("有" + joinCount + "名小学生正在一起努力");
        Collections.shuffle(societyList);
        return societyList.get(0);
    }

    /**
     * 获取配置课程
     */
    private List<String> getConfigLessonIds() {
        String lessonIdStr = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STUDY_PLAN_LESSON_IDS");
        String[] lessonIdArr = lessonIdStr.split(",");
        return Arrays.asList(lessonIdArr);
    }

    /**
     * 训练营课程报名人数之和
     */
    private String getLessonJoinCount() {
        Long totalNum = 0L;
        List<String> lessonIds = getConfigLessonIds();
        for (String lessonId : lessonIds) {
            Long num = studyTogetherServiceClient.loadLessonJoinCount(lessonId).getUninterruptibly();
            totalNum += SafeConverter.toLong(num);
        }
        return totalNum.toString();
    }

    /**
     * 训练营完成今日课程人数之和
     */
    private String getLessonFinishCount() {
        Long totalNum = 0L;
        List<String> lessonIds = getConfigLessonIds();
        for (String lessonId : lessonIds) {
            Map<String, Integer> finishMap = studyTogetherServiceClient.loadStudentTodayFinishInfo(lessonId, 0L).getUninterruptibly();
            totalNum += SafeConverter.toInt(finishMap.get("finishCount"));
        }
        return totalNum.toString();
    }

    /**
     * 小U三科使用人数之和
     */
    private String getXiaoUSociety(boolean finish, StudentDetail studentDetail) {
        List<String> serviceTypes = new ArrayList<>();
        serviceTypes.add(OrderProductServiceType.AfentiExam.name());
        serviceTypes.add(OrderProductServiceType.AfentiMath.name());
        serviceTypes.add(OrderProductServiceType.AfentiChinese.name());

        Map<AppUseNumCalculateType, String> templateMap = new HashMap<>();
        if (finish) {
            templateMap.put(AppUseNumCalculateType.GRADE, "{}名同年级同学正在做练习");
            templateMap.put(AppUseNumCalculateType.SCHOOL, "{}名校友正在做课后练习");
            templateMap.put(AppUseNumCalculateType.NATION, "今日已有{}名小学生做过练习");
        } else {
            templateMap.put(AppUseNumCalculateType.GRADE, "{}名同年级同学正在做练习");
            templateMap.put(AppUseNumCalculateType.SCHOOL, "{}名校友做过练习");
            templateMap.put(AppUseNumCalculateType.NATION, "已有{}名小学生完成练习");
        }
        return generateRandomTexts(templateMap, studentDetail, serviceTypes);
    }

    private String generateRandomTexts(Map<AppUseNumCalculateType, String> templateMap, StudentDetail studentDetail, List<String> serviceTypes) {
        Map<String, Integer> numMap;
        String numStr;
        long num;
        String template;
        numMap = businessVendorServiceClient.loadNationNum(serviceTypes);
        num = numMap.values().stream().mapToLong(SafeConverter::toLong).sum();
        numStr = num < 10000 ? String.valueOf(num) : String.valueOf((num + 5000) / 10000) + "万";
        template = templateMap.get(AppUseNumCalculateType.NATION);
        //如果学生为空，直接返回全国数据
        if (studentDetail == null) {
            return StringUtils.formatMessage(template, numStr);
        }
        List<String> randomTexts = new ArrayList<>();
        randomTexts.add(StringUtils.formatMessage(template, numStr));
        List<AppUseNumCalculateType> calculateTypes = new ArrayList<>();
        calculateTypes.add(AppUseNumCalculateType.GRADE);
        calculateTypes.add(AppUseNumCalculateType.SCHOOL);
        for (AppUseNumCalculateType calculateType : calculateTypes) {
            numMap = businessVendorServiceClient.loadUseNum(calculateType, serviceTypes, studentDetail);
            num = numMap.values().stream().mapToLong(SafeConverter::toLong).sum();
            if (num >= 100) {
                template = templateMap.get(calculateType);
                numStr = num < 10000 ? String.valueOf(num) : String.valueOf((num + 5000) / 10000) + "万";
                randomTexts.add(StringUtils.formatMessage(template, numStr));
            }
        }
        Collections.shuffle(randomTexts);
        return randomTexts.get(0);
    }

    //*******************************************学习计划推荐END***********************************************

    private static HashSet<String> blockTitle20001Set = new HashSet<>();

    static {
        blockTitle20001Set.add("我的优惠券");
        blockTitle20001Set.add("我的订单");
    }

    private boolean showControl(ParentUserCenterFunctionConfig centerFunctionConfig, String version, StudentDetail studentDetail) {
        if (StringUtils.isNotBlank(version)) {
            if (StringUtils.isNotBlank(centerFunctionConfig.getStartVersion())) {
                if (VersionUtil.compareVersion(version, centerFunctionConfig.getStartVersion()) < 0) {
                    return false;
                }
            }
            if (StringUtils.isNotBlank(centerFunctionConfig.getEndVersion())) {
                if (VersionUtil.compareVersion(version, centerFunctionConfig.getEndVersion()) >= 0) {
                    return false;
                }
            }
        }
        if (centerFunctionConfig.safeIosAuthNoShow()) {
            Long currentParentId = getCurrentParentId();
            if (currentParentId == null || currentParentId.equals(20001L)) {
                return false;
            }
        }
        if (StringUtils.isNotBlank(centerFunctionConfig.getGrayMain()) && StringUtils.isNotBlank(centerFunctionConfig.getGraySub())) {
            if (studentDetail == null) {
                return false;
            }
            return grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, centerFunctionConfig.getGrayMain(), centerFunctionConfig.getGraySub());
        }
        return true;
    }

    @RequestMapping(value = "/sign/config.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getSignConfig() {
        MapMessage resultMap = new MapMessage();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = getCurrentParent();
        try {
            if (parent == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_PARENT_ERROR_MSG);
                return resultMap;
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            Map<String, Object> configMap = new HashMap<>();
            String configContent = getPageBlockContentGenerator().getPageBlockContentHtml("parentSignConfig", parentSignKey);
            ParentSignConfig parentSignConfig = JsonUtils.fromJson(configContent.replaceAll("\n|\r|\t", ""), ParentSignConfig.class);
            if (parentSignConfig != null) {
                String mainGray = SafeConverter.toString(parentSignConfig.getMainGray());
                String subGray = SafeConverter.toString(parentSignConfig.getSubGray());
                boolean hitGray = studentDetail == null || StringUtils.isBlank(mainGray) || StringUtils.isBlank(subGray) || grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, mainGray, subGray);
                if (hitGray) {
                    configMap.put("icon", StringUtils.isBlank(parentSignConfig.getIcon()) ? "" : getCdnBaseUrlStaticSharedWithSep() + parentSignConfig.getIcon());
                    configMap.put("url", SafeConverter.toString(parentSignConfig.getUrl(), ""));
                }
            }

            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("configMap", configMap);
        } catch (Exception e) {
            logger.error("get parentSignConfig error. sid:{}", studentId);
            return failMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/authority/setting.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setting() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(getRequestLong(REQ_STUDENT_ID));
        if (studentDetail == null) {
            return failMessage(RES_RESULT_STUDENT_ID_ERROR_MSG);
        }
        MapMessage resultMap = successMessage();
        allSwitch(resultMap, studentDetail);
        return resultMap;
    }


    private List<Map<String, Object>> generateFunctionList(String functionListKey,
                                                           StudentDetail studentDetail, String version, Boolean isTop) {
        List<ParentUserCenterFunctionConfig> functionConfigList =
                pageBlockContentServiceClient.loadConfigList("parentUserCenterConfig", functionListKey, ParentUserCenterFunctionConfig.class);
        if (CollectionUtils.isEmpty(functionConfigList)) {
            return Collections.emptyList();
        }

        if (isTop && getCurrentParentId() != null && studentDetail != null) {
            ParentUserCenterFunctionConfig kolBean = getKolBean(getCurrentParentId(), studentDetail.getId());
            if (null != kolBean) {
                functionConfigList.add(kolBean);
            }
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        functionConfigList.forEach(config -> {
            if (!showControl(config, version, studentDetail)) {
                return;
            }
            //这里要求 配置的type,必须为枚举中定义的哦
            ParentUserCenterNativeFunction function = ParentUserCenterNativeFunction.of(config.getType());
            if (ParentUserCenterNativeFunction.UNKNOWN == function) {
                return;
            }
            //学生个人信息的,把学生姓名拼进去,还有url带上学生id
            if (ParentUserCenterNativeFunction.CHILD_INFO == function && studentDetail != null) {
                config.setTitle(MessageFormat.format(config.getTitle(), studentDetail.fetchRealname()));
                config.setUrl(MessageFormat.format(config.getUrl(), studentDetail.getId().toString()));
            }
            //把所有H5类型的,并且是学生配置的,拼加sid。
            if (ParentUserCenterNativeFunction.H5 == function && studentDetail != null) {
                if (StringUtils.isBlank(config.getUrl())) {
                    return;
                }
                String name;
                try {
                    name = URLEncoder.encode(studentDetail.fetchRealname(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    name = "";
                }
                if (!config.getUrl().contains("?")) {
                    config.setUrl(config.getUrl() + "?sid=" + studentDetail.getId() + "&student_name=" + name);
                } else {
                    config.setUrl(config.getUrl() + "sid=" + studentDetail.getId() + "&student_name=" + name);
                }
            }
            //获取未支付订单数量
            if (ParentUserCenterNativeFunction.H5 == function && StringUtils.isNotBlank(config.getUrl())
                    && config.getUrl().contains(PARENT_FUNCTION_CONFIG_ORDER_URL) && ("我的订单").equals(config.getTitle())) {
                List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(currentUserId());
                //查出没有班级或未毕业的孩子
                List<User> children = getChildrenNotInClazzOrNotGraduated(currentUserId());
                if (CollectionUtils.isNotEmpty(children)) {
                    Map<String, OrderProduct> timeBaseAvailableProductMap = userOrderLoaderClient.loadAllTimeBaseAvailableProduct()
                            .stream()
                            .collect(Collectors.toMap(ObjectIdEntity::getId, s -> s));

                    for (User user : children) {
                        List<UserOrder> childOrders = getChildOrderNeedDisplay(timeBaseAvailableProductMap, user);
                        userOrders.addAll(childOrders);
                    }
                }
                Map<String, VendorApps> vam = vendorAppsServiceClient.getVendorAppsBuffer().loadVendorAppsList().stream()
                        .filter(v -> v.isVisible(RuntimeMode.current().getLevel()))
                        .filter(VendorApps::getWechatBuyFlag)
                        .collect(Collectors.toMap(VendorApps::getAppKey, Function.identity()));
                if (CollectionUtils.isNotEmpty(userOrders)) {
                    int unPaidCount = SafeConverter.toInt(userOrders.stream()
                            .filter(o -> o.getPaymentStatus() == PaymentStatus.Unpaid)
                            .filter(order -> order.getOrderStatus() == OrderStatus.New)
                            .filter(o -> vam.containsKey(o.getOrderProductServiceType()))
                            .count());
                    if (unPaidCount != 0) {
                        config.setRemindingId(currentUserId().toString());
                        config.setRemindingType(ReminderType.NUMBER.name());
                        config.setRemindingNumber(unPaidCount);
                        config.setFunctionRemindingForever(true);
                    }
                }
            }

            //心愿单数量处理
            if (ParentUserCenterNativeFunction.H5 == function
                    && StringUtils.isNotBlank(config.getUrl())
                    && config.getUrl().contains(PARENT_FUNCTION_CONFIG_WISH_URL)
                    && StringUtils.isNotBlank(config.getTitle()) && config.getTitle().contains("学习愿望")) {
                Long wishCount = parentWishLoader.getParentUnreadWishCount(currentUserId());
                if (null != wishCount && wishCount > 0L) {
                    config.setRemindingId(SafeConverter.toString(currentUserId()));
                    config.setRemindingType(ReminderType.NUMBER.name());
                    config.setRemindingNumber(SafeConverter.toInt(wishCount));
                    config.setFunctionRemindingForever(true);
                }
            }

            //student不问空的时候说明是孩子相关的功能,如果url上不为空,则把sid拼上去
            if (StringUtils.isNotBlank(config.getUrl()) && studentDetail != null) {
                config.setUrl(MessageFormat.format(config.getUrl(), studentDetail.getId()));
            }
            //对于枚举定义了顺序大于0的,就用枚举的顺序。
            if (function.getOrder() > 0) {
                config.setOrder(function.getOrder());
            }

            //如果这个枚举有linkFunction ,则设置成link的function
            if (function.getLinkFunction() != null) {
                config.setType(function.getLinkFunction().name());
            }

            //如果功能没有定义是否需要登录,默认不需要。
            if (config.getNeedLogin() == null) {
                config.setNeedLogin(false);
            }
            //如果提醒类型为数字,并且数字大于99,则只返回99
            if (ParentRemindingType.NUMBER.name().equals(config.getRemindingType())) {
                if (config.getRemindingNumber() != null && config.getRemindingNumber() > 99) {
                    config.setRemindingNumber(99);
                }
            }

            resultList.add(toResultMap(config));
        });

        return resultList.stream().sorted(new orderComparator()).collect(Collectors.toList());
    }

    /**
     * 过滤出孩子需要显示的订单
     */
    @NotNull
    private List<UserOrder> getChildOrderNeedDisplay(Map<String, OrderProduct> timeBaseAvailableProductMap, User user) {
        List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderListIncludedCanceled(user.getId());
        userOrders = userOrders.stream()
                // 过滤取消的未支付的单子
                .filter(order -> !(order.getOrderStatus() == OrderStatus.Canceled && order.getPaymentStatus() == PaymentStatus.Unpaid))
                // 过滤一起学翻转课堂的单子
                .filter(order -> order.getOrderType() != null && order.getOrderType() != OrderType.yi_qi_xue_fz)
                .filter(order -> timeBaseAvailableProductMap.containsKey(order.getProductId()))
                // 过滤类型不为空的单子
                .filter(order -> order.getOrderProductServiceType() != null)
                // 未支付的订单价格与产品不相等直接跳过
                .filter(order -> OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == PicListen || order.getPaymentStatus() != PaymentStatus.Unpaid ||
                        (order.getPaymentStatus() == PaymentStatus.Unpaid && matchOrderPrice(order, timeBaseAvailableProductMap)))
                // 洛亚传说 三国订单不显示
                .filter(o -> (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) != A17ZYSPG && OrderProductServiceType.safeParse(o.getOrderProductServiceType()) != SanguoDmz) ||
                        (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == A17ZYSPG && o.getPaymentStatus() == PaymentStatus.Paid) ||
                        (OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == SanguoDmz && o.getPaymentStatus() == PaymentStatus.Paid))
                .collect(Collectors.toList());
        return userOrders;
    }

    /**
     * 查出没有班级的孩子和未毕业的孩子
     */
    private List<User> getChildrenNotInClazzOrNotGraduated(Long parentId) {
        List<User> children = studentLoaderClient.loadParentStudents(parentId);
        if (CollectionUtils.isEmpty(children)) {
            return new ArrayList<>();
        }
        Set<Long> studentIds = children.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds);
        if (MapUtils.isNotEmpty(studentDetails)) {
            children = studentDetails.values().stream()
                    .filter(e -> e.getClazz() == null || !e.getClazz().isTerminalClazz())
                    .collect(Collectors.toList());
        } else {
            children = new ArrayList<>();
        }
        return children;
    }

    private boolean matchOrderPrice(UserOrder order, Map<String, OrderProduct> productInfoMap) {
        List<UserOrderProductRef> uoprList = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
        if (CollectionUtils.isNotEmpty(uoprList)) {
            BigDecimal totalPrice = new BigDecimal(0);
            for (UserOrderProductRef uopr : uoprList) {
                OrderProduct product = productInfoMap.get(uopr.getProductId());
                if (product != null) {
                    totalPrice = totalPrice.add(product.getPrice());
                }
            }
            return Math.abs(totalPrice.doubleValue() - order.getOrderPrice().doubleValue()) <= 1e-6;

        } else {
            OrderProduct product = productInfoMap.get(order.getProductId());
            return product != null && (Math.abs(product.getPrice().doubleValue() - order.getOrderPrice().doubleValue()) <= 1e-6);
        }
    }

    private List<Map<String, Object>> generateTabList(List<ParentTabConfig> tabConfigList, User parent, String version, Long createTime) {
        if (CollectionUtils.isEmpty(tabConfigList))
            return Collections.emptyList();

        //有版本控制的判断,没有灰度或者灰度不完整的认为就是没有灰度,一并加如进来
        //加入时间控制，有条件的就判断当前时间是否在配置时间条件之后-----2017.2.8
        List<ParentTabConfig> returnConfigSet = tabConfigList.stream()
                .filter(config -> VersionUtil.checkVersionConfig(config.getVersion(), version))
                .collect(Collectors.toList());

        //有完整灰度的根据家长判断
        Long parentId = parent != null ? parent.getId() : null;
        if (parentId != null) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
            if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
                List<StudentDetail> studentDetails = new ArrayList<>(studentLoaderClient.loadStudentDetails(studentIds).values());
                //年级过滤
                returnConfigSet = returnConfigSet.stream()
                        .filter(config -> CollectionUtils.isEmpty(config.getClassLevels()) || (CollectionUtils.isNotEmpty(config.getClassLevels()) && studentDetails.stream().anyMatch(s -> config.getClassLevels().stream().anyMatch(cl -> cl.equals(s.getClazzLevelAsInteger())))))
                        .collect(Collectors.toList());
                //灰度过滤
                returnConfigSet = returnConfigSet.stream()
                        .filter(config -> StringUtils.isBlank(config.getGrayMain()) || StringUtils.isBlank(config.getGraySub())
                                || studentDetails.stream().anyMatch(s -> grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(s, config.getGrayMain(), config.getGraySub())))
                        .collect(Collectors.toList());
                return toTabResultMap(returnConfigSet, studentDetails, parentId, createTime, version);
            }
        } else {
            returnConfigSet = returnConfigSet.stream()
                    .filter(config -> CollectionUtils.isEmpty(config.getClassLevels()))
                    .filter(config -> StringUtils.isEmpty(config.getGrayMain()) || StringUtils.isEmpty(config.getGraySub()))
                    .collect(Collectors.toList());
        }
        return toTabResultMap(returnConfigSet, null, parentId, createTime, version);
    }


    private List<Map<String, Object>> toTabResultMap(Collection<ParentTabConfig> configs, List<StudentDetail> studentDetails, Long parentId, Long createTime, String version) {
        if (CollectionUtils.isEmpty(configs)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        configs.forEach(config -> {
            Map<String, Object> map = new LinkedHashMap<>();
            addIntoMap(map, RES_PARENT_TAB_SHOW_ID, config.getShowId());
            if (inIconDisplayDate(config)) {
                String clickIcon = getCdnBaseUrlStaticSharedWithSep() + config.getClickIcon();
                addIntoMap(map, RES_PARENT_TAB_CLICK_ICON, clickIcon);
                String notClickIcon = getCdnBaseUrlStaticSharedWithSep() + config.getNotClickIcon();
                addIntoMap(map, RES_PARENT_TAB_NOT_CLICK_ICON, notClickIcon);
                addIntoMap(map, RES_PARENT_TAB_NAME, config.getTabName());
                addIntoMap(map, RES_PARENT_TAB_IS_DISPLAY_NAVIGATION_BAR, config.getIsDisplayNavigationBar());
                addIntoMap(map, RES_PARENT_TAB_IS_DISPLAY, config.getIsDisplay());
                addIntoMap(map, RES_PARENT_TAB_BRAND_FLAG, config.getBrandFlag());
                addIntoMap(map, RES_PARENT_TAB_URL, config.getTabUrl());
                addIntoMap(map, RES_PARENT_TAB_WEBVIEW_IS_SHOW, Boolean.TRUE);
            }
            //没有登录不显示气泡
            if (parentId != null) {
                if (SafeConverter.toInt(config.getShowId()) != 5) {
                    addIntoMap(map, RES_PARENT_TAB_BUBBLE_CONTENT, generateBubbleContent(config, studentDetails, parentId));
                } else {
                    long wishCount = parentWishLoader.getParentUnreadWishCount(parentId);
                    Long studentId = parentWishLoader.getParentWishLatestStudent(parentId);
                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                    if (wishCount > 0 && studentDetail != null) {
                        addIntoMap(map, RES_PARENT_TAB_BUBBLE_CONTENT_TYPE, BubbleType.STUDY_WISH);
                        addIntoMap(map, RES_PARENT_TAB_BUBBLE_CONTENT, wishCount + "条孩子学习愿望");
                        String studentAvatar = getUserAvatarImgUrl(studentDetail.fetchImageUrl());
                        addIntoMap(map, RES_PARENT_TAB_BUBBLE_CONTENT_IMG, studentAvatar);
                        addIntoMap(map, RES_PARENT_TAB_BUBBLE_CONTENT_URL, ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/17wish_list/index.vpage?useNewCore=wk&rel=1");
                    } else {
                        long messageCount = getNewMessageCount(parentId, createTime, version);
                        addIntoMap(map, RES_PARENT_TAB_BUBBLE_CONTENT, generateMessageBubbleContent(config, parentId, messageCount, createTime));
                    }
                }
            }
            addIntoMap(map, RES_PARENT_TAB_WK_FLAG, SafeConverter.toBoolean(config.getWkFlag()));

            //tab提醒,2.3.5只有一级消息，且默认返回都是0
            int firstRedDotCount = 0;
            int firstNumberCount = 0;
            int secondRedDotCount = 0;
            int secondNumberCount = 0;
            if (VersionUtil.compareVersion(version, "2.3.6") >= 0) {
                //一级消息
                com.voxlearning.utopia.service.reminder.constant.ReminderPosition firstPosition = com.voxlearning.utopia.service.reminder.constant.ReminderPosition.of(config.getReminderPosition());
                if (firstPosition != null) {
                    ReminderContext firstReminderContext = reminderLoader.loadUserReminder(parentId, firstPosition);
                    if (firstReminderContext != null) {
                        firstRedDotCount = SafeConverter.toInt(firstReminderContext.getReminderCount());
                        firstNumberCount = SafeConverter.toInt(firstReminderContext.getReminderNumCount());
                        //一级消息返回给客户端后直接清掉
                        reminderService.clearUserReminder(parentId, firstPosition);
                    }
                }

                //二级消息
                List<com.voxlearning.utopia.service.reminder.constant.ReminderPosition> secondPositions = new ArrayList<>();
                List<String> childPositions = config.getChildPositions();
                if (CollectionUtils.isNotEmpty(childPositions)) {
                    for (String childPosition : childPositions) {
                        com.voxlearning.utopia.service.reminder.constant.ReminderPosition position = com.voxlearning.utopia.service.reminder.constant.ReminderPosition.of(childPosition);
                        if (position != null) {
                            secondPositions.add(position);
                        }
                    }
                }
                Map<com.voxlearning.utopia.service.reminder.constant.ReminderPosition, ReminderContext> secondContextMap = reminderLoader.loadUserReminder(parentId, secondPositions);
                for (ReminderContext reminderContext : secondContextMap.values()) {
                    secondRedDotCount += SafeConverter.toInt(reminderContext.getReminderCount());
                    secondNumberCount += SafeConverter.toInt(reminderContext.getReminderNumCount());
                }
                addIntoMap(map, RES_PARENT_TAB_SECOND_RED_DOT_COUNT, secondRedDotCount);
                addIntoMap(map, RES_PARENT_TAB_SECOND_NUMBER_COUNT, secondNumberCount);
            }

            addIntoMap(map, RES_PARENT_TAB_RED_DOT_COUNT, firstRedDotCount);
            addIntoMap(map, RES_PARENT_TAB_NUMBER_COUNT, firstNumberCount);
            mapList.add(map);
        });
        return mapList;
    }

    private boolean inIconDisplayDate(ParentTabConfig config) {
        return (StringUtils.isBlank(config.getDisplayStartDate()) || DateUtils.stringToDate(config.getDisplayStartDate()).before(new Date())) &&
                (StringUtils.isBlank(config.getDisplayEndDate()) || DateUtils.stringToDate(config.getDisplayEndDate()).after(new Date()));
    }

    private String generateMessageBubbleContent(ParentTabConfig tabConfig, Long parentId, Long messageCount, Long createTime) {
        String bubbleContent = "";
        String cacheKey = "PARENT_TAB_BUBBLE_CONTENT_" + tabConfig.getShowId() + "_" + parentId;
        String cacheContent = CacheSystem.CBS.getCache("persistence").load(cacheKey);
        if (messageCount > 0) {
            String messageContent = createTime + "_" + messageCount;
            if (!messageContent.equals(cacheContent)) {
                bubbleContent = "你有" + messageCount + "个新通知";
                CacheSystem.CBS.getCache("persistence").set(cacheKey, 0, messageContent);
            }
        }
        return bubbleContent;
    }

    private String generateBubbleContent(ParentTabConfig tabConfig, List<StudentDetail> studentDetails, Long parentId) {
        String bubbleContent = "";
        ParentTabBubble bubble = tabConfig.getBubble();
        if (bubble != null && StringUtils.isNotBlank(bubble.getContent())) {
            String bubbleVersion = bubble.getVersion();
            String ver = getRequestString(REQ_APP_NATIVE_VERSION);
            if (VersionUtil.checkVersionConfig(bubbleVersion, ver)) {
                //在灰度范围内
                boolean hitGay = hitGray(bubble, studentDetails);

                //在时间范围内
                Date startDate = null;
                Date endDate = null;
                if (StringUtils.isNotBlank(bubble.getStartDate())) {
                    startDate = DateUtils.stringToDate(bubble.getStartDate());
                }
                if (StringUtils.isNotBlank(bubble.getEndDate())) {
                    endDate = DateUtils.stringToDate(bubble.getEndDate());
                }
                Date now = new Date();
                boolean inPeriod = (startDate == null || now.after(startDate)) && (endDate == null || now.before(endDate));

                //没有显示过此文案
                String content = bubble.getContent();
                if (StringUtils.isNotBlank(bubble.getContentAddDate())) {
                    Date addDate = DateUtils.stringToDate(bubble.getContentAddDate());
                    content += DateUtils.dateToString(addDate, "yyyyMMddHHmmss");
                }
                String cacheKey = "PARENT_TAB_BUBBLE_CONTENT_" + tabConfig.getShowId() + "_" + parentId;
                String cacheContent = CacheSystem.CBS.getCache("persistence").load(cacheKey);
                boolean hasShow = content.equals(cacheContent);
                if (inPeriod && hitGay && !hasShow) {
                    bubbleContent = bubble.getContent();
                    int expireTime = endDate == null ? 0 : (int) ((endDate.getTime() - now.getTime()) / 1000);
                    CacheSystem.CBS.getCache("persistence").set(cacheKey, expireTime, content);
                }
            }
        }
        return bubbleContent;
    }

    private long getNewMessageCount(Long userId, Long createTime, String ver) {
        AppMessageSource source = AppMessageSource.PARENT;
        Set<String> tagSet = getUserMessageTagList(userId);
        //根据tag做过滤
        List<AppGlobalMessage> globalMessages = appMessageLoaderClient.appGlobalMessages(source.name(), tagSet)
                .stream()
                // 由于有些模板消息设置成了未来时间，需要这里过滤下
                .filter(p -> new Date(p.getCreateTime()).before(new Date()))
                .collect(Collectors.toList());
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
        } else { //老师的咯
            UserReminder userReminder = userReminderLoader.loadUserReminder(userId, ReminderPosition.TAPP_TEACHER_INDEX_MSG);
            if (userReminder != null && userReminder.getLastUpdateTime() > createTime && userReminder.getCounter() != null) {
                count += userReminder.getCounter();
            }
        }
        return count;
    }

    //tab气泡在灰度内
    private boolean hitGray(ParentTabBubble bubble, List<StudentDetail> studentDetails) {
        String grayConfig = bubble.getGrayExpress();
        //登录了但没有孩子
        if (CollectionUtils.isEmpty(studentDetails)) {
            String grayExpress = "000000_000000_0_000000";
            return StringUtils.isBlank(grayConfig) || grayExpress.matches(grayConfig);
        }
        return StringUtils.isBlank(grayConfig) || studentDetails.stream().anyMatch(studentDetail -> {
            StringBuilder sb = new StringBuilder();
            if (studentDetail.getStudentSchoolRegionCode() != null) {
                sb.append(studentDetail.getStudentSchoolRegionCode());
            } else {
                sb.append("000000");
            }
            sb.append("_");
            if (studentDetail.getClazz() != null) {
                sb.append(studentDetail.getClazz().getSchoolId());
            } else {
                sb.append("000000");
            }
            sb.append("_");
            if (studentDetail.getClazzLevelAsInteger() != null) {
                sb.append(studentDetail.getClazzLevelAsInteger());
            } else {
                sb.append("0");
            }
            sb.append("_").append(studentDetail.getId());
            String grayExpress = sb.toString();
            return grayExpress.matches(grayConfig);
        });
    }


    private Map<String, Object> toResultMap(ParentUserCenterFunctionConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();
        addIntoMap(map, RES_FUNCTION_TITLE, config.getTitle());
        addIntoMap(map, RES_FUNCTION_ORDER, config.getOrder());
        addIntoMap(map, RES_FUNCTION_TYPE, config.getType());
        addIntoMap(map, RES_FUNCTION_URL, config.getUrl());
        addIntoMap(map, RES_FUNCTION_REMINDING_ID, config.getRemindingId());
        addIntoMap(map, RES_FUNCTION_REMINDING_TYPE, config.getRemindingType());
        addIntoMap(map, RES_FUNCTION_REMINDING_TEXT, config.getRemindingText());
        addIntoMap(map, RES_FUNCTION_REMINDING_NUMBER, config.getRemindingNumber());
        addIntoMap(map, RES_FUNCTION_NEED_LOGIN, config.getNeedLogin());
        addIntoMap(map, RES_FUNCTION_REMINDING_FOREVER, config.getFunctionRemindingForever());
        addIntoMap(map, RES_FUNCTION_ICON_URL, config.getIconUrl());
        return map;
    }

    public static class orderComparator implements Comparator<Map<String, Object>> {

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            Integer order1 = SafeConverter.toInt(o1.get(RES_FUNCTION_ORDER));
            Integer order2 = SafeConverter.toInt(o2.get(RES_FUNCTION_ORDER));

            return Integer.compare(order1, order2);
        }
    }

    private ParentUserCenterFunctionConfig getKolBean(Long parentId, Long studentId) {
        MapMessage mapMessage = monitorRecruitV2Service.appTabShowInApp(parentId, studentId);
        Boolean isRecruit = (Boolean) mapMessage.get("is_recruit");
        Boolean isMonitor = (Boolean) mapMessage.get("is_monitor");
        ParentUserCenterFunctionConfig kolBean = new ParentUserCenterFunctionConfig();
        String iconUrl = getCdnBaseUrlStaticSharedWithSep() + "public/skin/parentMobile/images/monitor.png";
        String url = "";

        if (isMonitor) {
            kolBean.setTitle("一起学班级管理");
            url = fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/17xue_kol/management_index.vpage?";
        }
        if (isRecruit) {
            kolBean.setTitle("一起学班长招募");
            url = fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/17xue_kol/index.vpage?lesson_id=301&";
        }
        kolBean.setUrl(url);
        kolBean.setOrder(1);
        kolBean.setType("H5");
        kolBean.setNeedLogin(false);
        kolBean.setFunctionRemindingForever(false);
        kolBean.setIconUrl(iconUrl);
        if (!isMonitor && !isRecruit) {
            return null;
        }
        return kolBean;
    }

    public enum BubbleType {
        STUDY_WISH;
    }
}
