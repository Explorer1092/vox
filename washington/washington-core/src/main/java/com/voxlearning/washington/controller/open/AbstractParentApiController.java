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

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.galaxy.service.coin.api.entity.Coin;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.api.constant.NewUserTask;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.StudentHomeworkStatus;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.client.AsyncPiclistenCacheServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.reminder.api.ReminderLoader;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.DPUserService;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.userlevel.api.mapper.UserActivationLevel;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.constant.DotSourceType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentHomeworkDynamicToNativeType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.StudyAppData;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.MySelfStudyGlobalMsgServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.washington.athena.ParentReportLoaderClient;
import com.voxlearning.washington.helpers.ParentRegisterHelper;
import com.voxlearning.washington.helpers.ParentStudentCallNameHelper;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;
import com.voxlearning.washington.support.SessionUtils;
import lombok.Data;
import lombok.Getter;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Hailong Yang
 * @version 0.1
 * @since 2015/09/16
 */
public class AbstractParentApiController extends AbstractApiController {

    @Inject private RaikouSystem raikouSystem;

    //家长登录成功发个广播
    @AlpsPubsubPublisher(topic = "galaxy.parent.login.queue", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @Inject
    private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @StorageClientLocation(storage = "homework")
    protected StorageClient storageClient;

    protected final String parentMobileProvisionPrefix = "parent_login_provision_";

    @Inject
    protected PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject
    protected UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject
    protected CommonConfigServiceClient commonConfigServiceClient;


    @ImportService(interfaceClass = ReminderLoader.class)
    protected ReminderLoader reminderLoader;
    @ImportService(interfaceClass = ReminderService.class)
    protected ReminderService reminderService;


    @ImportService(interfaceClass = MySelfStudyService.class)
    protected MySelfStudyService mySelfStudyService;
    @Inject
    protected ParentReportLoaderClient parentReportLoaderClient;
    @Inject
    protected UserBlacklistServiceClient userBlacklistServiceClient;
    @Inject
    protected MySelfStudyGlobalMsgServiceClient mySelfStudyGlobalMsgServiceClient;
    @Inject
    protected AsyncFootprintServiceClient asyncFootprintServiceClient;

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    protected ParentSelfStudyService parentSelfStudyService;

    @Inject
    private AsyncVendorServiceClient asyncVendorServiceClient;

    @Inject
    protected AsyncPiclistenCacheServiceClient asyncPiclistenCacheServiceClient;

    @Inject
    @Getter
    protected GrayFunctionManagerClient grayFunctionManagerClient;

    @Inject
    protected ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;
    @Inject
    protected ParentStudentCallNameHelper parentStudentCallNameHelper;
    @Inject
    protected ParentRegisterHelper parentRegisterHelper;
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @ImportService(interfaceClass = DPUserService.class)
    protected DPUserService dpUserService;

    private static final String ICO_SELF_STUDY = "/public/skin/parentMobile/images/dynamic/self_study.png";
    protected static final String ICO_DYNAMIC_ENGLISH = "/public/skin/parentMobile/images/new_icon/english.png";
    protected static final String ICO_DYNAMIC_MATH = "/public/skin/parentMobile/images/new_icon/math.png";
    protected static final String ICO_DYNAMIC_CHINESE = "/public/skin/parentMobile/images/new_icon/chinese.png";
    protected static final String ICO_DYNAMIC_DOT = "/public/skin/parentMobile/images/new_icon/dot.png";
    protected static final String ICO_DYNAMIC_VOTE = "/public/skin/parentMobile/images/new_icon/vote.png";
    protected static final String ICO_SYNC_APP_DIAN_DU = "/public/skin/parentMobile/images/app_icon/sync_app_diandu.png";
    protected static final String ICO_SYNC_APP_DIAN_DU_BIG = "/public/skin/parentMobile/images/app_icon/sync_app_diandu_big.png";

    private static final String URL_SELF_STUDY = "/parentMobile/home/index2.vpage?sid={0}&source_type={1}";
    private static final String URL_LEARNING = "/parentMobile/learning/index.vpage?sid={0}&source_type={1}";
    private static final String URL_VOICERECOMMEND_DETAIL = "/view/mobile/common/voice_recommend?recommend_id=";

    //微信支付0.01元白名单
    public static Map<String, String> payStudentParentIdTestMap = new HashMap<>();

    //错题本学科-AppKey
    public static Map<Subject, String> subjectAppKey = new HashMap<>();

    static {
        payStudentParentIdTestMap.put("362868306", "24900420");
        payStudentParentIdTestMap.put("361240469", "26151913");
        payStudentParentIdTestMap.put("361267379", "25258271");
        payStudentParentIdTestMap.put("338401515", "22767288");
        payStudentParentIdTestMap.put("361267378", "26855642");
        payStudentParentIdTestMap.put("361267378", "26855642");
        payStudentParentIdTestMap.put("361267361", "28106421");
        payStudentParentIdTestMap.put("361267380", "28519265");
        payStudentParentIdTestMap.put("361267377", "28517259");
        //下面这个是测试环境的。因为与微信互调的原因。暂时用不了路伟的测试环境直接支付成功的js
        //只好在这加了3个测试环境的账号
        payStudentParentIdTestMap.put("333875333", "256219");
        payStudentParentIdTestMap.put("333875312", "256219");
        payStudentParentIdTestMap.put("333875405", "256846");
        //托管班的1分钱账号
        payStudentParentIdTestMap.put("30009", "256537");
        // 付费推广 1分钱账号
        payStudentParentIdTestMap.put("333898792", "257267");
        payStudentParentIdTestMap.put("371670285", "210134640");
        payStudentParentIdTestMap.put("371676653", "210139674");
        payStudentParentIdTestMap.put("371676359", "210139579");
        payStudentParentIdTestMap.put("371676584", "210139646");
        payStudentParentIdTestMap.put("371676572", "210139637");
        payStudentParentIdTestMap.put("333794803", "257397");
        subjectAppKey.put(Subject.CHINESE, "AfentiChinese");
        subjectAppKey.put(Subject.MATH, "AfentiMath");
        subjectAppKey.put(Subject.ENGLISH, "AfentiExam");
    }

//    @Override
//    public boolean onBeforeControllerMethod() {
//        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
//        if (StringUtils.isNotBlank(ver) && VersionUtil.compareVersion(ver, "2.1.8") <= 0) {
//            try {
//                getResponse().getWriter().write(JsonUtils.toJson(failMessage(RES_RESULT_VERSION_OLD_ERROR_MSG)));
//            } catch (IOException e) {
//                logger.error("Parent App Redirect Error:", e);
//            }
//            return false;
//        }
//        return super.onBeforeControllerMethod();
//    }

    public void validateRequest(String... paramKeys) {
        super.validateRequest(paramKeys);
        User user = getApiRequestUser();
        // 验证用户身份
        if (user.fetchUserType() != UserType.PARENT) {
            throw new IllegalArgumentException(RES_RESULT_USER_TYPE_ERROR_MSG);
        }
    }

    public Long getCurrentParentId() {
        User user = getCurrentParent();
        return user != null ? user.getId() : null;
    }

    public User getCurrentParent() {
        User user = getApiRequestUser();
        return user != null && user.fetchUserType() == UserType.PARENT ? user : null;
    }


    protected boolean isSuccess(MapMessage mapMessage) {
        return mapMessage != null && RES_RESULT_SUCCESS.equals(mapMessage.get(RES_RESULT));
    }

    protected Boolean isShenhe(Long parentId) {
        return parentId != null && parentId.equals(20001L);
    }

    //新作业的extInfo
    protected List<Map<String, Object>> buildExtInfo_New(NewAccomplishment newAccomplishment) {
        int totalFinishCount = newAccomplishment == null ? 0 : newAccomplishment.size();
        List<Map<String, Object>> mapList = new ArrayList<>();
        //完成数
        Map<String, Object> finishDetail = new HashMap<>();
        String title;
        if (totalFinishCount == 0) {
            finishDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
            title = "去第一个完成作业";
            finishDetail.put("title", title);
            mapList.add(finishDetail);
        }
        return mapList;
    }

//    protected String getGalaxySiteUrl() {
//        String domain = ProductConfig.get("galaxy.domain");
//        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
//            domain = "https://" + domain;
//        }
//        return domain;
//    }

    //已完成，未检查的extInfo
    //作业动态的卡片
    protected List<Map<String, Object>> buildExtInfo_Finish(NewHomeworkFinishRewardInParentApp rewardInParentApp, NewHomework.Location location, List<Flower> homeworkFlowerList, Long studentId) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        //学豆奖励
        if (rewardInParentApp != null) {
            Map<String, Object> integralDetail = generateRewardInParentApp(rewardInParentApp, location);
            if (MapUtils.isNotEmpty(integralDetail)) {
                mapList.add(integralDetail);
            }

        }
        //送花列表
        Map<String, Object> flowerDetail = generateFlowerInfo(homeworkFlowerList, studentId);
        mapList.add(flowerDetail);
        return mapList;
    }

    //作业已完成，已检查
    protected List<Map<String, Object>> buildExtInfo_Finish_Check(NewHomeworkFinishRewardInParentApp rewardInParentApp, NewHomework.Location location, NewHomeworkResult newHomeworkResult, NewAccomplishment newAccomplishment, List<VoiceRecommend> voiceRecommendList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (rewardInParentApp != null) {
            Map<String, Object> integralDetail = generateRewardInParentApp(rewardInParentApp, location);
            if (MapUtils.isNotEmpty(integralDetail)) {
                mapList.add(integralDetail);
            }

        }
        //完成情况
        Boolean repair = newHomeworkResult.getRepair();
        //判断学生是否补做，老数据newHomeworkResult没有repair字段，如果repair==null则需要从NewAccomplishment取。
        if (repair == null) {
            if (newAccomplishment != null && newAccomplishment.getDetails().keySet().contains(newHomeworkResult.getUserId().toString())) {
                repair = newAccomplishment.getDetails().get(newHomeworkResult.getUserId().toString()).getRepair();
            } else {
                repair = true;
            }
        }
        Map<String, Object> finishInfo = new HashMap<>();
        finishInfo.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
        if (repair) {
            finishInfo.put("title", "补做完成");
            mapList.add(finishInfo);
        }

        //语音推荐
        if (voiceRecommendList != null) {
            VoiceRecommend voiceRecommend = voiceRecommendList.stream().filter(p -> p.getHomeworkId().equals(location.getId())).findFirst().orElse(null);
            if (voiceRecommend != null) {
                Map<String, Object> voiceRecommendDetail = new HashMap<>();
                voiceRecommendDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_VOTE);
                voiceRecommendDetail.put("redirectTitle", "老师推荐了本次优秀语音>");
                voiceRecommendDetail.put("redirectUrl", URL_VOICERECOMMEND_DETAIL + voiceRecommend.getId() + "&sid=" + newHomeworkResult.getUserId());
                mapList.add(voiceRecommendDetail);
            }
        }
        return mapList;
    }

    protected List<Map<String, Object>> buildExtInfo_SelfStudy(Long studentId, NewHomework.Location location, StudentHomeworkStatus status, String ver) {
        String title;
        DotSourceType type;
        ParentHomeworkDynamicToNativeType nativeType;
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (location.getSubject() == Subject.ENGLISH) {
            title = "听磁带复习课文>";
            nativeType = ParentHomeworkDynamicToNativeType.POINT_READ;
        } else {
            title = "复习今天的课文>";
            nativeType = ParentHomeworkDynamicToNativeType.TEXT_READ;
        }
        //自学工具
        if (status == StudentHomeworkStatus.NEW) {
            //新作业：包括新作业和过期未完成未检查的作业
            type = DotSourceType.HOMEWORK_DYNAMIC_NEW;
        } else if (status == StudentHomeworkStatus.FINISH) {
            //已完成的作业
            type = DotSourceType.HOMEWORK_DYNAMIC_FINISH;
        } else {
            //已检查的作业：包括未完成和已完成但是已检查的作业
            type = DotSourceType.HOMEWORK_DYNAMIC_CHECK;
        }
        Map<String, Object> selfStudyDetail = new HashMap<>();
        selfStudyDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_SELF_STUDY);
        selfStudyDetail.put("redirectTitle", title);
        if (VersionUtil.compareVersion(ver, "1.6.5") >= 0) {
            selfStudyDetail.put("redirectType", nativeType.getType());
        } else if (VersionUtil.compareVersion(ver, "1.6.0") >= 0) {
            selfStudyDetail.put("redirectUrl", MessageFormat.format(URL_LEARNING, studentId, type.getType()));
        } else {
            selfStudyDetail.put("redirectUrl", MessageFormat.format(URL_SELF_STUDY, studentId, type.getType()));
        }

        mapList.add(selfStudyDetail);
        return mapList;
    }

    protected Map<String, Object> buildExtInfo_MistakenNoteBook(Long studentId, Subject subject) {
        Map<String, Object> afentiInfoMap = new HashMap<>();
        afentiInfoMap.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_SELF_STUDY);
        afentiInfoMap.put("redirectTitle", "打开错题本，消灭错题>");
//        afentiInfoMap.put("redirectUrl", "/view/mobile/parent/mistakennotebook/detail?sid=" + studentId + "&subject=" + subject.name() + "&subjectName=" + subject.getValue() + "&refer=240007");
        afentiInfoMap.put("redirectUrl", "/view/mobile/student/wonderland/openapp?appKey=" + subjectAppKey.get(subject) + "&platform=STUDENT_APP&productType=APPS&module=Wrongbook&sid=" + studentId);

        return afentiInfoMap;
    }


    protected MapMessage getWantToBindParentIdentityMobileBySid(Long studentId, Integer callNameId, String requestSource) {

        try {
            CallName callName = CallName.of(callNameId);
            if (callName == null) {
                return failMessage("家长身份错误，请重新选择身份");
            }

            boolean needCreateNewParent = true;
            String starMobile = "";
            String userMobile = "";
            List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
            boolean bindAndLogin = true;
            Long parentId = null;
            boolean hadSameCallNameParent = false;

            if (CollectionUtils.isNotEmpty(studentParents)) {  //该学生有家长
                //家长中是否包括请求角色的家长
                List<StudentParent> hadCallNameParents = new ArrayList<>();
                hadCallNameParents.addAll(studentParents);
                StudentParent sameIdentityParent = hadCallNameParents.stream()
                        .filter(p -> p != null)
                        .filter(p -> callName.name().equals(p.getCallName()))
                        .findFirst().orElse(null);
                if (sameIdentityParent != null) { //学生已有的家长中包括该请求绑定的角色，则返回该角色家长的手机号
                    needCreateNewParent = false;
                    hadSameCallNameParent = true;
                    parentId = sameIdentityParent.getParentUser().getId();
                    starMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(parentId);
                    // FIXME requestSource is always ""
//                    if ("internal".equals(requestSource)) {
//                        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parentId, REQ_GET_USER_MOBILE_MSG, SafeConverter.toString(parentId));
//
//                        userMobile = authenticatedMobile == null ? "" : authenticatedMobile;
//                    }
                    if (StringUtils.isNotEmpty(starMobile)) {
                        bindAndLogin = false;
                    }
                } else {
                    List<StudentParent> keyParents = new ArrayList<>();
                    keyParents.addAll(studentParents);
                    keyParents = keyParents.stream()
                            .filter(p -> p != null)
                            .filter(StudentParent::isKeyParent)
                            .filter(p -> CallName.of(p.getCallName()) == null)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(keyParents) && keyParents.size() == 1) {
                        StudentParent keyStudentParent = keyParents.get(0);
                        if (keyStudentParent != null) {
                            parentId = keyStudentParent.getParentUser().getId();
                            //这里还要先验证一下这个关键家长的与这个孩子的所有身份信息
                            User parent = keyStudentParent.getParentUser();
                            MapMessage validateMessage = parentStudentCallNameHelper.validateStudentParentRef(studentId, parent, callName);
                            ;
                            if (!validateMessage.isSuccess()) {
                                return failMessage(validateMessage.getInfo());
                            }
                            starMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(parentId);
                            // FIXME requestSource is always ""
//                            if ("internal".equals(requestSource)) {
//                                String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parentId, REQ_GET_USER_MOBILE_MSG, SafeConverter.toString(parentId));
//
//                                userMobile = authenticatedMobile == null ? "" : authenticatedMobile;
//                            }
                            needCreateNewParent = false;
                        }
                    }
                }
            } else { //该学生没有家长
                UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(studentId);
                if (userAuthentication != null) {    //学生有绑定手机,返回学生绑定的手机号
                    starMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(studentId);
                    // FIXME requestSource is always ""
//                    if ("internal".equals(requestSource)) {
//                        AuthenticatedMobile authenticatedMobile = sensitiveUserDataServiceClient.loadUserAuthenticationMobile(studentId, REQ_GET_USER_MOBILE_MSG);
//                        userMobile = authenticatedMobile == null ? "" : authenticatedMobile.getMobile();
//                    }
                }
            }
            MapMessage message = successMessage(RES_BINDING_MOBILE, StringUtils.trimToEmpty(starMobile))
                    .add(RES_NEED_CREATE_NEW_PARENT, needCreateNewParent).add(RES_BIND_AND_LOGIN, bindAndLogin).add(RES_PARENT_ID, parentId == null ? "" : parentId)
                    .add("hadSameCallNameParent", hadSameCallNameParent);
            if ("internal".equals(requestSource)) {
                message.add(RES_USER_SENSITIVE_MOBILE, userMobile);
            }
            return message;
        } catch (Exception e) {
            return failMessage("验证选择身份失败");
        }
    }

    protected boolean isJuniorLogin(User loginUser) {
        if (loginUser == null) {
            return false;
        }
        if (loginUser.fetchUserType() == UserType.STUDENT) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(loginUser.getId());
            if (studentDetail != null && studentDetail.isJuniorStudent()) {
                return true;
            }
        }
        return false;
    }

    protected boolean studentIsParentChildren(Long parentId, Long studentId) {
        if (studentId == null || studentId == 0 || parentId == null || parentId == 0)
            return false;
        List<StudentParent> parents = parentLoaderClient.loadStudentParents(studentId);
        return !CollectionUtils.isEmpty(parents) && parents.stream().anyMatch(p -> p.getParentUser().getId().equals(parentId));

    }

    /**
     * 获取用户名（统一为：XX爸爸；XX妈妈等）(取第一个产生绑定关系的孩子)
     */
    protected String generateUserNameForJxtNewsComment(User user) {
        String userName = "";
        if (user == null) {
            return userName;
        }
        if (UserType.PARENT == user.fetchUserType()) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                //1.5.3以上支持c端家长评论。是可能没有孩子的.所以名字显示为带星号的手机号
                userName = sensitiveUserDataServiceClient.loadUserMobileObscured(user.getId());
            } else {
                StudentParentRef studentParentRef = studentParentRefs.stream()
                        .sorted((e1, e2) -> Long.compare(e2.getCreateTime().getTime(), e1.getCreateTime().getTime()))
                        .findFirst()
                        .orElse(null);
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentParentRef.getStudentId());
                userName = studentDetail.fetchRealname() + (CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName());
            }
        } else if (UserType.TEACHER == user.fetchUserType()) {
            Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
            String subjectName = teacher != null && teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
            userName = subjectName + user.fetchRealname() + "老师";
        }
        return userName;
    }

    //一定是个家长。不需要管身份的时候直接登录
    protected MapMessage doParentLogin(User parent) {
        if (parent != null && UserType.of(parent.getUserType()) == UserType.PARENT) {
            return generateParentLoginInfo(parent);
        } else {
            return failMessage(RES_RESULT_MOBILE_PARENT_LOGIN_USERTYPE_ERROR_MSG);
        }
    }


    //返回请求的身份对应的家长信息
    protected MapMessage getParentWithCallName(User student, CallName callName, String parentMobile) {
        if (student == null) {
            return failMessage("用户不存在");
        }
        if (callName == null) {
            return failMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }

        if (StringUtils.isBlank(parentMobile)) {
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(student.getId());
            if (userAuthentication == null || !userAuthentication.isMobileAuthenticated()) {
                MapMessage resultMap = new MapMessage();
                resultMap.add(RES_RESULT, RES_RESULT_STUDENT_NOT_BIND_MOBILE_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_PARENT_MOBLIE_STUDENT_NOT_BIND_MOBLIE_MSG);
                return resultMap;
            }
            parentMobile = userAuthentication.getSensitiveMobile();
        }
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(student.getId());
        UserAuthentication existParent = userLoaderClient.loadMobileAuthentication(parentMobile, UserType.PARENT);
        //绑定关系需要的参数
        boolean isKeyParent = CollectionUtils.isEmpty(studentParents) || !studentParents.stream().anyMatch(StudentParent::isKeyParent);
        StudentParent existCallNameParent = studentParents.stream().filter(p -> callName.getValue().equals(p.getCallName())).findFirst().orElse(null);
        User parent;
        if (existParent != null) {
            if (CollectionUtils.isNotEmpty(studentParents)) {
                if (studentParents.stream().anyMatch(p -> p.getParentUser().getId().equals(existParent.getId()))) {
                    StudentParent studentParent = studentParents.stream().filter(p -> p.getParentUser().getId().equals(existParent.getId())).findFirst().get();
                    if (callName.name().equals(studentParent.getCallName())) {
                        //userId和身份都相同。直接返回
                        parent = studentParent.getParentUser();
                        return successMessage().add("loginParent", parent);
                    } else if (StringUtils.isBlank(studentParent.getCallName())) {
                        //userId相同。身份为空。把请求的身份绑给这个家长
                        parent = studentParent.getParentUser();
                        MapMessage mapMessage = parentServiceClient.setParentCallName(parent.getId(), student.getId(), callName);
                        if (!mapMessage.isSuccess()) {
                            return failMessage(mapMessage.getInfo());
                        }
                        return successMessage().add("loginParent", parent);
                    } else {
                        //userId相同。身份不匹配。直接报错
                        return failMessage(RES_RESULT_PARENT_HAD_BEEN_OTHER_IDENTITY);
                    }
                } else if (studentParents.stream().anyMatch(p -> callName.name().equals(p.getCallName()) && !p.getParentUser().getId().equals(existParent.getId()))) {
                    //身份相同。userId不同。直接报错
                    return failMessage(RES_RESULT_IDENTITY_EXIST_ERROR_MSG);
                }

            }
            //1、没有家长
            //2、没有找到身份+Id都相同的家长
            //3、没有身份为空的关键家长
            //4、没有身份相同、Id不相同的家长
            MapMessage mapMessage = parentStudentCallNameHelper.validateParentWithCallName(existParent.getId(), callName);
            if (!mapMessage.isSuccess()) {
                return failMessage(mapMessage.getInfo());
            }
            parent = raikouSystem.loadUser(existParent.getId());
        } else if (existCallNameParent != null) {
            Long parentId = existCallNameParent.getParentUser().getId();
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parentId);
            //存在的相同身份的家长已经手机绑定过。直接报错
            if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
                return failMessage(RES_RESULT_IDENTITY_EXIST_ERROR_MSG);
            }
            //将填写的手机绑给这个家长
            userServiceClient.activateUserMobile(parentId, parentMobile);

            parent = raikouSystem.loadUser(parentId);
            //这里不需要身份了。绑定完手机。直接返回了。
            return successMessage().add("loginParent", parent);
        } else {
            //创建一个家长
            MapMessage mapMessage = createParent(student, parentMobile);
            if (!isSuccess(mapMessage)) {
                return mapMessage;
            }
            Object newUser = mapMessage.get("newParent");
            if (newUser == null) {
                return failMessage(RES_RESULT_FAILED_CREATE_PARENT);
            }
            parent = (User) newUser;

        }
        //新创建的、需要更新身份的。都再绑一次
        MapMessage mapMessage = parentServiceClient.bindExistingParent(student.getId(), parent.getId(), isKeyParent, callName.name());
        if (!mapMessage.isSuccess()) {
            return failMessage(mapMessage.getInfo());
        }
        return successMessage().add("loginParent", parent);
    }

    //处理登录成功需要返回给客户端的信息
    protected MapMessage generateParentLoginInfo(User loginUser) {
        return parentUserInfoForLogin(loginUser);
    }

    //这个是登录完成后返回给app的。需要注册jpush tag。所以内容会多点。
    protected MapMessage parentUserInfoForLogin(User user) {
        //家长个人基本信息
        MapMessage mapMessage = generateParentInfo(user);
        //处理学生个人信息
        List<User> students = studentLoaderClient.loadParentStudents(user.getId());
        Map<Long, StudentDetail> studentUserMaps;
        if (CollectionUtils.isNotEmpty(students)) {
            Set<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toSet());
            studentUserMaps = studentLoaderClient.loadStudentDetails(studentIds);
            List<Map<String, Object>> studentInfoList = generateParentStudentInfo(user, studentUserMaps, true);
            mapMessage.add(RES_CLAZZ_STUDENTS, studentInfoList);
        }
        doExtThingForLogin(user);

        mapMessage.add(RES_SHOW_JXT_NEWS, true);
        mapMessage.add(RES_PARENT_STYLE_TEST, 1);
        mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        return mapMessage;
    }

    //家长基本信息
    protected MapMessage generateParentInfo(User user) {
        MapMessage mapMessage = new MapMessage();
        // 获取用户的登录信息
        mapMessage.add(RES_USER_TYPE, RoleType.ROLE_PARENT.getType());
        mapMessage.add(RES_USER_ID, user.getId());
        mapMessage.add(RES_SESSION_KEY, attachUser2RequestApp(user.getId()));
        // 获取用户的其他个人信息
        mapMessage.add(RES_REAL_NAME, user.fetchRealname());
        mapMessage.add(RES_USER_IMG_URL, getParentAppImgUrlWithDefault(user));

        String markedMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(user.getId());
        mapMessage.add(RES_USER_MOBILE, markedMobile);

        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(user.getId());
        if (StringUtils.isNotBlank(authenticatedMobile)) {
            UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(authenticatedMobile, UserType.STUDENT);
            mapMessage.add(RES_MOBILE_BIND_STUDENT, userAuthentication != null);
        } else {
            mapMessage.add(RES_MOBILE_BIND_STUDENT, false);
        }
        VendorAppsUserRef vendorAppUserRef = vendorLoaderClient.loadVendorAppUserRef("YiQiXue", user.getId());
        if (vendorAppUserRef == null) {
            MapMessage message = asyncVendorServiceClient.getAsyncVendorService().registerVendorAppUserRef("YiQiXue", user.getId()).getUninterruptibly();
            if (message.isSuccess() && message.get("ref") instanceof VendorAppsUserRef) {
                vendorAppUserRef = (VendorAppsUserRef) message.get("ref");
            }
        }
        String yiQiXueStudentHost = generateYiQiXueStudentHost();
        String uri = "/m/auth/jzt/jpk.vpage";
        mapMessage.add(RES_RESULT_YI_QI_XUE_URL, yiQiXueStudentHost + uri);
        mapMessage.add(RES_RESULT_YI_QI_XUE_SESSION_KEY, vendorAppUserRef == null ? "" : vendorAppUserRef.getSessionKey());

        mapMessage.add(RES_USER_LEVEL_ENABLE, true);
        //用户等级信息
        UserActivationLevel parentActivationLevel = userLevelLoader.getParentLevel(user.getId());
        if (null != parentActivationLevel) {
            mapMessage.add(RES_RESULT_USER_LEVEL, parentActivationLevel.getLevel());
            mapMessage.add(RES_RESULT_USER_LEVEL_NAME, parentActivationLevel.getName());
            mapMessage.add(RES_RESULT_ACTIVATION_VALUE, parentActivationLevel.getValue());
            mapMessage.add(RES_RESULT_MAX_ACTIVATION_VALUE, parentActivationLevel.getLevelEndValue() + 1);
            mapMessage.add(RES_RESULT_MIN_ACTIVATION_VALUE, parentActivationLevel.getLevelStartValue());
            mapMessage.add(RES_RESULT_USER_LEVEL_URL, "/view/mobile/parent/grade/detail.vpage");
        }
        return mapMessage;
    }

    //登录成功时做额外处理
    protected void doExtThingForLogin(User parent) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
        String userPass = ua.getPassword();
        // 登录成功时记录信息
        Long userId = parent.getId();
//        userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddress(), OperationSourceType.app);
//        userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.app);
        getWebRequestContext().saveAuthenticationStates(-1, userId, userPass, RoleType.ROLE_PARENT);
        //如果是短信邀请进来的，默认绑定手机号
        miscServiceClient.bindInvitedTeacherMobile(userId);

        String deviceId = getRequestString(REQ_UUID);
        if (StringUtils.isEmpty(deviceId))
            deviceId = getRequestString(REQ_IMEI);

        String sys = getRequestString(REQ_SYS);
        String model = getRequestString(REQ_MODEL);

        // 登录成功，记录设备号
        asyncFootprintServiceClient.getAsyncFootprintService().recordUserDeviceInfo(parent.getId(), deviceId, sys, model);

        parentServiceClient.agreeParentBrandFlag(parent.getId());

        //发送家长通登录成功广播
        Map<String, Object> info = new HashMap<>(1);
        info.put("uid", parent.getId());

        messagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(info)));
    }

    //孩子基本信息列表
    protected List<Map<String, Object>> generateParentStudentInfo(User parent, Map<Long, StudentDetail> studentDetailMap, boolean withTag) {
        List<Map<String, Object>> studentInfoList = new ArrayList<>();
        //家长和学生的称呼
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
        Map<Long, String> studentCallNameMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(studentParentRefs)) {
            Collections.sort(studentParentRefs, (o1, o2) -> {
                if (StringUtils.isNotBlank(o2.getCallName()) && StringUtils.isBlank(o1.getCallName())) {
                    return 1;
                } else {
                    return 0;
                }
            });
            studentParentRefs.stream()
                    .filter(p -> p.getStudentId() != null)
                    .forEach(p -> studentCallNameMap.put(p.getStudentId(), StringUtils.replace(p.getCallName(), "它", "他")));
        }
        boolean over230 = StringUtils.isNotBlank(getClientVersion()) && VersionUtil.compareVersion(getClientVersion(), "2.3.0") >= 0;
        Map<Long, Integer> studentMedalMap = new HashMap<>();
        if (over230) {
            studentMedalMap = studyTogetherServiceClient.getStudyTogetherHulkService().loadStudentMedal(studentDetailMap.keySet());
        }
        Map<Long, List<GroupMapper>> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentDetailMap.keySet(), false);
        String studentDefaultName = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STUDENT_DEFAULT_NAME");
        for (Long id : studentDetailMap.keySet()) {
            StudentDetail studentDetail = studentDetailMap.get(id);
            List<GroupMapper> groupMapperList = studentGroups.get(id);
            Map<String, Object> map = new HashMap<>();
            map.put(RES_STUDENT_ID, studentDetail.getId());
            map.put(RES_REAL_NAME, StringUtils.isBlank(studentDetail.getProfile().getRealname()) ? studentDefaultName : studentDetail.getProfile().getRealname());
            map.put(RES_USER_IMG_URL, getUserAvatarImgUrl(studentDetail.getProfile().getImgUrl()));
            map.put(RES_CALL_NAME, studentCallNameMap.getOrDefault(studentDetail.getId(), ""));
            map.put(RES_REGION_CODE, studentDetail.getStudentSchoolRegionCode());
            map.put(RES_STUDENT_KTWELVE, null == studentDetail.getClazz() ? Ktwelve.UNKNOWN : studentDetail.getClazz().getEduSystem().getKtwelve());
            //这里开始是一系列的开关
            allSwitch(map, studentDetail);
            String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(id);
            if (StringUtils.isNotBlank(authenticatedMobile)) {
                map.put(RES_STUDENT_MOBILE, authenticatedMobile);
            }

            if (over230) {
                Coin coin = dpCoinLoader.loadCoin(studentDetail.getId());
                map.put(RES_USER_COIN_URL, "/view/mobile/parent/17kol_mall/mall.vpage?useNewCore=wk");
                map.put(RES_USER_COIN, coin == null ? 0 : SafeConverter.toInt(coin.getTotalCount()));

                int medalCount = SafeConverter.toInt(studentMedalMap.get(id));
                map.put(RES_USER_MEDAL, medalCount);
                if (medalCount == 0) {
                    map.put(RES_USER_MEDAL_URL, "");
                } else {
                    //勋章数量
                    String domain = ProductConfig.get("learntogether.domain");
                    if (StringUtils.isNotBlank(domain)) {
                        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
                            domain = "https://" + domain;
                        }
                        map.put(RES_USER_MEDAL_URL, domain + "/redirect/share?action=medal-display&hash=%3FkeepHash%3Dtrue");
                    } else
                        map.put(RES_USER_MEDAL_URL, "");
                }
            }

            map.put(RES_INTEGRAL, studentDetail.getUserIntegral() == null ? 0 : studentDetail.getUserIntegral().getUsable());
            map.put(RES_INTEGRAL_URL, "/parentMobile/home/integralchip.vpage?student_name=" + studentDetail.fetchRealname());

            if (studentDetail.getClazz() != null) {
                if (studentDetail.getClazz().isTerminalClazz()) {
                    map.put(RES_JIE, studentDetail.getClazz().getJie() + "届");
                    map.put("isGraduate", Boolean.TRUE);
                } else {
                    map.put(RES_RESULT_STUDENT_HAD_CLAZZ, Boolean.TRUE);

                    map.put(RES_CLAZZ_NAME, studentDetail.getClazz().formalizeClazzName());
                    map.put(RES_SCHOOL_NAME, studentDetail.getStudentSchoolName());


                    //学生的科目信息
                    if (CollectionUtils.isNotEmpty(groupMapperList)) {
                        List<Integer> subjects = groupMapperList.stream()
                                .map(GroupMapper::getSubject)
                                .sorted((o1, o2) -> o1.getKey() - o2.getKey())
                                .map(Subject::getId)
                                .collect(Collectors.toList());
                        map.put(RES_RESULT_SUBJECT_LIST, subjects);
                    }
                    //登录完成的时候返回学生信息需要返回这些字段用户注册tag。个人信息tab刷新的时候就不需要返回这些了。
                    if (withTag) {
                        map.put(RES_CLAZZ_ID, studentDetail.getClazz().getId());
                        map.put(RES_CLAZZ_LEVEL, studentDetail.getClazz().getClassLevel());
                        map.put(RES_SCHOOL_ID, studentDetail.getClazz().getSchoolId());
                        ExRegion region = raikouSystem.loadRegion(studentDetail.getStudentSchoolRegionCode());
                        if (region != null) {
                            map.put(RES_PROVINCE_CODE, region.getProvinceCode());
                            map.put(RES_PROVINCE_NAME, region.getProvinceName());
                            map.put(RES_CITY_CODE, region.getCityCode());
                            map.put(RES_CITY_NAME, region.getCityName());
                            map.put(RES_COUNTRY_CODE, region.getCountyCode());
                            map.put(RES_COUNTRY_NAME, region.getCountyName());
                        }
                    }
                }
            } else {
                //没有班级。客户端需要一个字段判断。去加入班级
                map.put(RES_RESULT_STUDENT_HAD_CLAZZ, Boolean.FALSE);
            }
            studentInfoList.add(map);
        }
        return studentInfoList;
    }

    private Integer getMedalCount(Long id) throws UnsupportedEncodingException {
        String domain = ProductConfig.get("learntogether.domain");
        if (StringUtils.isBlank(domain)) {
            return null;
        }
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "https://" + domain;
        }
        String param = "{\"student_id\":" + id + "}";

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .get(domain + "/rpc?service=PublicService&method=medalCount&parameters=" + URLEncoder.encode(param, "UTF-8"))
                .execute();
        if (null == response || response.hasHttpClientException()) {
            return null;
        }
        Map<String, Object> result = JsonUtils.fromJson(response.getResponseString());
        if (result.containsKey("success") && result.containsKey("data") && "true".equals(result.get("success").toString())) {
            return SafeConverter.toInt(result.get("data"));
        }

        return null;
    }


    /**
     * 有 支付开关pay  成长世界开关fairyland，自学产品开关vap, app使用时长appUseTime
     *
     * @param map
     */
    protected void allSwitch(Map<String, Object> map, StudentDetail studentDetail) {
        //支付开关pay
        boolean showFreePaySwitch = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jztapp", "freePaySwitch", true);
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (showFreePaySwitch) {
            Boolean approve;
            if (studentExtAttribute == null)
                approve = true;
            else
                approve = studentExtAttribute.fetchPayFreeStatus();
            map.put(RES_PAY_LIMIT_APPROVE, approve);
        } else
            map.put(RES_PAY_LIMIT_APPROVE, true);
        map.put(RES_PAY_LIMIT_SHOW, showFreePaySwitch);

        //成长世界开关fairyland
        Boolean fairyLandApprove = studentExtAttribute == null || !studentExtAttribute.fairylandClosed();
        map.put(RES_FAIRYLAND_APPROVE, fairyLandApprove);
        map.put(RES_FAIRYLAND_SHOW, true);

        //自学产品开关vap
        Boolean vapApprove = studentExtAttribute == null || !studentExtAttribute.vapClosed();
        map.put(RES_VPA_APPROVE, vapApprove);
        map.put(RES_VAP_SHOW, true);

        //app使用时长限制
        Integer useTime;
        if (studentExtAttribute == null) {
            useTime = 0;
        } else {
            useTime = studentExtAttribute.fetchAppUseTimeLimit();
        }
        map.put(RES_USE_APP_TIME, useTime);
        map.put(RES_USE_TIME_SHOW, true);

        Integer singleUseTime;
        if (studentExtAttribute == null) {
            singleUseTime = 0;
        } else {
            singleUseTime = studentExtAttribute.fetchAppUseOnceTimeLimit();
        }
        map.put(RES_SINGLE_USE_APP_TIME, singleUseTime);
        map.put(RES_SINGLE_USE_TIME_SHOW, true);

        //198自学产品入口判断
        if (VersionUtil.compareVersion("2.0.0.0", getRequestString(REQ_APP_NATIVE_VERSION)) <= 0)
            map.put(RES_SHOW_AUTHORITY_ENTRY, true);
        else
            map.put(RES_SHOW_AUTHORITY_ENTRY, true);

        //2.0.0 一起作业学生端自学设置入口控制
        map.put(RES_SHOW_STUDENT_APP_SELF_STUDY_SETTING, false);

        // 学豆乐园开关状态
        map.put("integral_fairyland_approve", studentExtAttribute == null || !studentExtAttribute.fetchCloseIntegralFairyland());
    }

    //创建家长号、设置身份
    protected MapMessage createParent(User student, String parentMobile) {
        if (student == null) {
            return failMessage("用户不存在");
        }
        //如果是老师账号就直接return错误
        if (UserType.of(student.getUserType()) == (UserType.TEACHER)) {
            return failMessage(RES_RESULT_WRONG_ROLE_PARENT_AND_STUDENT);
        }
        String lock = "create_parent_" + student.getId() + "_" + parentMobile;
        try {
            AtomicLockManager.instance().acquireLock(lock);
        } catch (CannotAcquireLockException e) {
            return failMessage(RES_RESULT_DUPLICATE_OPERATION);
        }
        try {
            //登录的是学生账号才去创建家长
            User newParent;
            MapMessage resultMap = new MapMessage();
            //进到这里连的时候分两种情况：
            //1、1.3.6以前的版本登录接口，在外层就已经判断了学生没有绑定手机直接返回错误了
            //2、1.3.6之后的版本进到这里面的时候学生没有绑定也允许创建家长号

            boolean isNewParent = true;
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(parentMobile, UserType.PARENT);
            if (ua == null) {
                NeonatalUser neonatalUser = new NeonatalUser();
                neonatalUser.setRoleType(RoleType.ROLE_PARENT);
                neonatalUser.setUserType(UserType.PARENT);
                neonatalUser.setMobile(parentMobile);
                neonatalUser.setWebSource(UserWebSource.parent_app.getSource());
                neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
                MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
                if (!message.isSuccess()) {
                    resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                    resultMap.add(RES_MESSAGE, message.getInfo());
                    return resultMap;
                }
                newParent = (User) message.get("user");
            } else {
                newParent = raikouSystem.loadUser(ua.getId());
                isNewParent = false;
//                userServiceClient.activateUserMobile(newParent.getId(), parentMobile);
            }
            //绑手机
            userServiceClient.activateUserMobile(newParent.getId(), parentMobile);
            //1.5.3的需求=同时看学生有没有绑手机。没有的话，也把这个手机绑给学生
            UserAuthentication studentAuthentication = userLoaderClient.loadUserAuthentication(student.getId());
            if (studentAuthentication == null || !studentAuthentication.isMobileAuthenticated()) {
                userServiceClient.activateUserMobile(student.getId(), parentMobile);
            }

            //创建账号的时候需要额外处理
            if (isNewParent) {
                //1、清除卡片缓存
                try {
                    asyncUserCacheServiceClient.getAsyncUserCacheService()
                            .NewUserTaskCacheManager_completeStudentNewUserTask(student, NewUserTask.parentWechatBinded)
                            .awaitUninterruptibly();
                } catch (CannotAcquireLockException ex) {
                    logger.error("清除卡片缓存");
                }
                //2、创建家长打点
                LogCollector.instance().info("backend-general",
                        MiscUtils.map(
                                "student_id", student.getId(),
                                "parent_id", newParent.getId(),
                                "env", RuntimeMode.getCurrentStage(),
                                "version", getRequestString(REQ_APP_NATIVE_VERSION),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", getRequest().getHeader("User-Agent"),
                                "op", "create_parent",
                                "mod1", getRequestString(REQ_SYS)
                        ));
            }
            return successMessage().add("newParent", newParent);
        } finally {
            AtomicLockManager.instance().releaseLock(lock);
        }
    }


    protected Map<UserType, User> internalUserLogin(String userAccount, String userPassword, boolean isNotPassword) {
        try {
            Map<UserType, User> userTypeMap = new HashMap<>();
            List<User> loginUserList = userLoaderClient.loadUsers(userAccount, null);
            if (CollectionUtils.isEmpty(loginUserList)) {
                return userTypeMap;
            }

            for (User loginUser : loginUserList) {
                // 临时密码校验 xuesong.zhang 2015-12-15
                boolean tempMatch = false;
                if (StringUtils.isNotBlank(userPassword) && StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(loginUser.getId()), userPassword)) {
                    tempMatch = true;
                }
                if (!isNotPassword) {
                    UserAuthentication ua = userLoaderClient.loadUserAuthentication(loginUser.getId());
                    if (!tempMatch && !ua.fetchUserPassword().match(userPassword)) {
                        continue;
                    }
                }
                if (UserType.TEACHER.getType() == loginUser.getUserType()) {
                    continue;
                }

                userTypeMap.put(UserType.of(loginUser.getUserType()), loginUser);
            }

            if (userTypeMap.isEmpty()) {
                if (CollectionUtils.isNotEmpty(loginUserList)) {
                    for (User user : loginUserList) {
//                        userServiceClient.recordUserLoginFailure2(user.getId());
                        asyncFootprintServiceClient.getAsyncFootprintService().recordUserLoginFailure(user.getId());
                    }
                }
            }
            return userTypeMap;
        } catch (Exception ex) {
            logger.error("获取用户信息失败,[userAccount:{}]", userAccount, ex);
            throw new UtopiaRuntimeException(ex.getMessage());
        }
    }


    private String getParentAppImgUrlWithDefault(User user) {
        String parentDefaultUrl = "/public/skin/parentMobile/images/new_icon/avatar_parent_default.png";
        String studentDefaultUrl = "/public/skin/parentMobile/images/new_icon/avatar_child_default.png";
        String img;
        if (StringUtils.isBlank(user.fetchImageUrl())) {
            if (user.fetchUserType() == UserType.PARENT) {
                img = parentDefaultUrl;
            } else {
                img = studentDefaultUrl;
            }
            return getCdnBaseUrlStaticSharedWithSep() + img;
        } else {
            return getUserAvatarImgUrl(user.fetchImageUrl());
        }
    }

    //这个是个人信息页面的内容
    protected MapMessage parentUserBasicInfo(User user, String version) {
        //家长个人基本信息
        MapMessage mapMessage = generateParentInfo(user);
        //处理学生信息
        List<User> students = studentLoaderClient.loadParentStudents(user.getId());
        //是否显示资讯
        if (CollectionUtils.isNotEmpty(students)) {
            Set<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toSet());
            Map<Long, StudentDetail> studentUserMaps = studentLoaderClient.loadStudentDetails(studentIds);
            //学生个人基本信息
            List<Map<String, Object>> studentInfoList = generateParentStudentInfo(user, studentUserMaps, false);
            mapMessage.add(RES_ORDER_COUNT, 0);
            mapMessage.add(RES_CLAZZ_STUDENTS, studentInfoList);
        }
        //服务器端返回JPushTag信息
        if (VersionUtil.compareVersion(version, "1.3.8") >= 0) {
            Set<String> tagSet = getUserMessageTagList(user.getId());
            mapMessage.add(RES_JPUSH_TAGS, tagSet);
        }

        /*
         * 品牌隔离是否显示条款
         * C端用户都不显示。//后来改了需求,C端用户从底层取出来就是同意的,除非他改过不同意。。。
         * 其他用户点过同意才不显示。isParentBrandFlag返回true的意思是点过同意。
         * 3.0版本去掉这个了。强盛了之后去掉这个字段吧
         */
        if (VersionUtil.compareVersion(version, "1.6.0") >= 0) {
            mapMessage.add(RES_SHOW_PROVISIONS, !parentLoaderClient.isParentBrandFlag(user.getId()));
            mapMessage.add(RES_PROVISIONS_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/close_off");
            //先固定一个,后面做配置
            mapMessage.add(RES_UESR_CENTER_RED_POINT_ID, "ucenter_1");
        }
        //是否显示资讯
        mapMessage.add(RES_SHOW_JXT_NEWS, true);
        mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        //支付密码相关
        boolean parentPasswordExist = false;
        boolean grayContains = false;
        Long parentId = user.getId();
        if (!Objects.equals(parentId, 20001L)) {
            //检验家长端支付密码是否设置
            if (dpUserService.isSetPaymentPassword(user.getId())) {
                parentPasswordExist = true;
            }
            //校验家长孩子是否在支付密码灰度范围内
            List<User> users = studentLoaderClient.loadParentStudents(user.getId());
            if (CollectionUtils.isNotEmpty(users)) {
                List<StudentDetail> studentDetails = new ArrayList<>(studentLoaderClient.loadStudentDetails(users.stream().map(User::getId).collect(Collectors.toList())).values());
                for (StudentDetail student : studentDetails) {
                    if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "Order", "FinancePay")) {
                        grayContains = true;
                        break;
                    }
                }
            }
        }
        mapMessage.add(RES_PARENT_PASSWORD_EXIST, parentPasswordExist);
        mapMessage.add(RES_PARENT_CONTAIN_GRAY, grayContains);

        mapMessage.add(RES_PARENT_STYLE_TEST, 1);

        return mapMessage;
    }

    private Map<String, Object> generateRewardInParentApp(NewHomeworkFinishRewardInParentApp rewardInParentApp, NewHomework.Location location) {
        Map<String, Object> integralDetail = new HashMap<>();
        if (MapUtils.isNotEmpty(rewardInParentApp.getNotReceivedRewardMap()) && rewardInParentApp.getNotReceivedRewardMap().containsKey(location.getId())) {
            integralDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
            integralDetail.put("title", "可领取额外学豆奖励");
        } else if (MapUtils.isNotEmpty(rewardInParentApp.getHadReceivedRewardMap()) && rewardInParentApp.getHadReceivedRewardMap().containsKey(location.getId())) {
            integralDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
            integralDetail.put("title", "已领取额外学豆奖励");
        }

        return integralDetail;
    }

    private Map<String, Object> generateFlowerInfo(List<Flower> homeworkFlowerList, Long studentId) {
        boolean send = CollectionUtils.isNotEmpty(homeworkFlowerList) && homeworkFlowerList.stream().anyMatch(p -> p.getSenderId().equals(studentId));
        int totalFlowerCount = CollectionUtils.isEmpty(homeworkFlowerList) ? 0 : homeworkFlowerList.size();
        //送花列表
        Map<String, Object> flowerDetail = new HashMap<>();
        flowerDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
        String title;
        if (send) {
            title = "已给老师送花";
        } else if (totalFlowerCount == 0) {
            title = "去第一个送花";
        } else {
            title = totalFlowerCount + "位家长已给老师送花";
        }
        flowerDetail.put("title", title);

        return flowerDetail;
    }

    protected MapMessage validateMobileVerifyCode(SmsType type, String mobile, String code, boolean needDeleteCode) {
        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, type.name(), needDeleteCode);
        if (!validateResult.isSuccess()) {
            return failMessage(RES_RESULT_VERIFY_CODE_ERROR_MSG);
        }

        return successMessage();
    }

    protected String getProgressFromData(StudyAppData studyAppData) {
        if (studyAppData == null)
            return "";
        String progress = studyAppData.getProgress();
        return progress == null ? "" : progress;
    }

    protected Boolean studentHasZuoyecuotiEntry(StudentDetail studentDetail) {
        return studentDetail != null && studentDetail.getClazz() != null && !studentDetail.getClazz().isTerminalClazz() && studentDetail.isPrimaryStudent();
    }

    protected String getAfentiEnglishReportUrl() {
        if (RuntimeMode.le(Mode.TEST))
            return "https://paradise.test.17zuoye.net/plan/home/entrance";
        else if (RuntimeMode.isStaging())
            return "https://paradise.staging.17zuoye.net/plan/home/entrance";
        else if (RuntimeMode.isProduction())
            return "https://paradise.17zuoye.com/plan/home/entrance";
        return "http://paradise.test.17zuoye.net/static/";
    }


    protected Map<String, Object> generateFairylandExtra(FairylandProduct fairylandProduct, VendorApps vendorApps) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fairylandProduct == null || null == vendorApps) {
            return map;
        }
        String url = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
        map.put("url", url);
        map.put("name", fairylandProduct.getProductName());
        map.put("orientation", vendorApps.getOrientation());
        map.put("browser", vendorApps.getBrowser());
        map.put("fullScreen", true);
        map.put("hideTitle", true);
        //安卓特殊要求必传
        if (fairylandProduct.getAppKey().equals(SelfStudyType.LISTEN_WORLD.getOrderProductServiceType())) {
            map.put("useNewCore", "crosswalk");
        }
        return map;
    }

    protected String generateJumpUrl(String h5Url, Long sid, FairylandProduct product, String mainSiteSchema, String channel) {
        if (product == null) {
            return mainSiteSchema + h5Url + "?sid=" + sid + "&rel=xxx";
        }
        return mainSiteSchema + h5Url + "?sid=" + sid + "&productType=" + product.getAppKey() + "&order_refer=" + channel;
    }

    @Data
    protected class ConfigContext {
        @Deprecated
        List<PageBlockContent> configPageBlockContentList;
        String version;
        Boolean isLogin;
        Integer clazzLevel;
        Long studentId;
        Boolean hasChild;
        List<FairylandProduct> fairylandProducts;
        Boolean isGraduated;
        StudentDetail studentDetail;
    }

    private String generateYiQiXueStudentHost() {
        String requestSchema = "https://";
        if (!getWebRequestContext().isHttpsRequest()) {
            requestSchema = "http://";
        }

        String yiQiXueHost = "17xue-student.test.17zuoye.net";
        if (RuntimeMode.isProduction()) {
            yiQiXueHost = "xue.17xueba.com";
        } else if (RuntimeMode.isStaging()) {
            yiQiXueHost = "17xue-student.staging.17zuoye.net";
        }
        return requestSchema + yiQiXueHost;
    }

    //过期老的sessionKey .生成一个新的Key
    protected void expireAndCreateNewSessionKey(Long parentId) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Parent", parentId);
        if (vendorAppsUserRef != null) {
            vendorServiceClient.expireSessionKey(
                    "17Parent",
                    parentId,
                    SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), parentId));
        }
    }

    //绑定家长的unionId
    protected void bindWechatUnionIdAndUserId(Long parentId, String unionId, String openId) {
        if (parentId == null || StringUtils.isBlank(unionId) || StringUtils.isBlank(openId)) {
            return;
        }
        //如果这个unionId已经绑给其他人了。不做操作
        User user = wechatLoaderClient.getWechatLoader().loadWechatUserByUnionId(unionId);
        if (user != null) {
            return;
        }
        String lock = "bindUserAndWechat_" + parentId;
        try {
            AtomicLockManager.getInstance().acquireLock(lock);
            wechatServiceClient.getWechatService().bindUserAndWechat(parentId, openId, unionId, "", WechatType.PARENT_APP.getType());
        } finally {
            AtomicLockManager.getInstance().releaseLock(lock);
        }
    }

    // 线上/staging银座九号的就id+pwd登录
    // 其他环境都可以登录 
    protected boolean isMatchIdPwdLoginCondition(User parent) {
        if (parent == null) {
            return false;
        }
        if (RuntimeMode.current().lt(Mode.STAGING)) {
            return true;
        }
        List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
        Set<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toSet());
        return studentLoaderClient.loadStudentDetails(studentIds)
                .values().stream().anyMatch(e -> (e.getClazz() != null && AppAuditAccounts.isAppParentLoginEnableSchool(e.getClazz().getSchoolId())));
    }

    protected String getGalaxySiteUrl() {
        String domain = ProductConfig.get("galaxy.domain");
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "https://" + domain;
        }
        return domain;
    }
}
