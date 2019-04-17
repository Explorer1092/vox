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

package com.voxlearning.washington.controller.thirdparty.qiyukf;


import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.*;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrgLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.washington.controller.thirdparty.base.cors.OptionsMethod;
import com.voxlearning.washington.controller.thirdparty.qiyukf.model.*;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;
import static java.util.Objects.requireNonNull;

/**
 * 七鱼客服接口
 *
 * @author Wenlong Meng
 * @since Jan 30, 2019
 */
@Slf4j
@Controller
@RequestMapping("/thirdparty/api/qiyukf")
public class QiyukfApiController extends AbstractController {

    private static final String TOKEN_CACHE_KEY = "17zy_kf_token";
    private static final String TOKEN_SUFFIX = "17zykf_token_";
    private static final int TOKEN_EXPIRE_SECONDS = 7200;
    private static final String TOKEN_LOCK_KEY = "17ZYKF_TOKEN_LOCK";
    private static final String QIYU_VERIFY_TARGET = "/qiyu/get_custom_info";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private UserLoginServiceClient userLoginServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private VendorLoaderClient vendorLoaderClient;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @Inject private AgentOrgLoaderClient agentOrgLoaderClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    //Logic

    /**
     * 测试跨域的接口
     *
     * @return
     */
    @RequestMapping(value = {"get_user_info.vpage", "get_call_user_info.vpage"}, method = {RequestMethod.OPTIONS})
    @OptionsMethod
    public void testCrossDomain(HttpServletResponse resp) {
        setHeader(resp);
        resp.setContentType("application/json;charset=UTF-8");
        LoggerUtils.info("thirdparty.qiyukf.testCrossDomain", getRequest().getRequestURI());
    }

    /**
     * 获取token
     */
    @RequestMapping(value = "get_token.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getToken(HttpServletRequest req) {
        String appId = req.getParameter("appid");
        String appSecret = req.getParameter("appsecret");

        MapMessage failedMsg = MapMessage.errorMessage().add("rlt", QiYuRLT.FAILED.getCode());
        boolean lock = false;

        try {
            requireNonNull(appId, "appid不能为空!");
            requireNonNull(appSecret, "appsecret不能为空!");

            String actualAppId = loadCommonCoinfig("kefu_qiyu_appid");
            requireNonNull(actualAppId, "appid的配置丢失!");

            String actualAppSecret = loadCommonCoinfig("kefu_qifu_app_secret");
            requireNonNull(actualAppSecret, "appsecret的配置丢失!");

            Validate.isTrue(actualAppId.equals(appId), "参数错误!");
            Validate.isTrue(actualAppSecret.equals(appSecret), "参数错误!");

            Long expiredMillSeconds;
            Long now = System.currentTimeMillis();

            // 有效期内token不变
            KeyValuePair<String, Long> token = null;
            CacheObject<KeyValuePair<String, Long>> cacheObject;

            Cache cache = CacheSystem.CBS.getCache("persistence");
            cacheObject = cache.get(TOKEN_CACHE_KEY);
            boolean expired = false;

            if (cacheObject != null) {
                token = cacheObject.getValue();
                if (token != null) {
                    Long expiredTime = token.getValue();
                    if (expiredTime < now) {
                        // 上锁
                        AtomicLockManager.getInstance().acquireLock(TOKEN_LOCK_KEY);
                        lock = true;
                        expired = true;
                    }
                }
            }

            if (expired || token == null) {
                // 生成新的token
                String newToken = TOKEN_SUFFIX + RandomStringUtils.randomAlphanumeric(10) + "_" + now;
                // 失效的毫秒数
                expiredMillSeconds = TOKEN_EXPIRE_SECONDS * TimeUnit.SECONDS.toMillis(1);
                Long expiredTime = now + expiredMillSeconds;

                token = new KeyValuePair<>(newToken, expiredTime);
                cache.add(TOKEN_CACHE_KEY, TOKEN_EXPIRE_SECONDS, token);

            } else {
                expiredMillSeconds = token.getValue() - now;
            }

            MapMessage mapMessage = MapMessage.successMessage()
                    .add("rlt", QiYuRLT.SUCCESS.getCode())
                    .add("token", token.getKey())
                    .add("expires", expiredMillSeconds);
            LoggerUtils.info("thirdparty.qiyukf.get_token", appId, appSecret, mapMessage);
            mapMessage.remove("success");// 去掉success字段
            getResponse().setContentType("application/json;charset=UTF-8");
            return mapMessage;
        } catch (Exception e) {
            MapMessage mapMessage = failedMsg.setInfo(e.getMessage());
            LoggerUtils.info("thirdparty.qiyukf.get_token.error", appId, appSecret, e.getMessage());
            return mapMessage;
        } finally {
            if (lock)
                AtomicLockManager.getInstance().releaseLock(TOKEN_LOCK_KEY);
        }
    }

    /**
     * 获得用户信息
     *
     * @param req
     * @param resp
     */
    @RequestMapping(value = "get_user_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserInfo(HttpServletRequest req, HttpServletResponse resp) {
        String appId = "", token = "", userIdStr = "", requestBody = "";
        try {
            requestBody = IOUtils.toString(req.getInputStream(), "UTF-8");
            Map<String, Object> parameters = JsonUtils.fromJson(requestBody);
            appId = SafeConverter.toString(parameters.get("appid"));
            token = SafeConverter.toString(parameters.get("token"));
            userIdStr = SafeConverter.toString(parameters.get("userid"));
            QiYuUserType qiYuUserType = QiYuUserType.of(toInt(parameters.get("usertype")));
            String actualAppId = loadCommonCoinfig("kefu_qiyu_appid");

            if (RuntimeMode.isProduction()) {
                requireNonNull(actualAppId, "appid的配置丢失!");
                requireNonNull(appId, "appid不能为空!");
                requireNonNull(token, "token不能为空!");
                requireNonNull(userIdStr, "userid不能为空!");
                if (!appId.equals(actualAppId)) {
                    MapMessage mapMessage = MapMessage.errorMessage("appId错误!").add("rlt", QiYuRLT.FAILED.getCode());
                    LoggerUtils.info("thirdparty.qiyukf.get_user_info.error", requestBody, mapMessage);
                    return mapMessage;
                }

                String currToken = getCurrentToken();
                if (currToken == null || !token.equals(currToken)) {
                    MapMessage mapMessage = MapMessage.errorMessage().add("rlt", QiYuRLT.EXPIRED.getCode());
                    LoggerUtils.info("thirdparty.qiyukf.get_user_info.error", currToken, requestBody, mapMessage);
                    return mapMessage;
                }
            }

            Long userId = SafeConverter.toLong(userIdStr);

            Map<QiYuDataAttr, Object> userData;
            switch (qiYuUserType) {
                case PLATFORM:
                    //查询平台用户
                    User user = raikouSystem.loadUser(userId);
                    requireNonNull(user, "用户不存在!");
                    userData = generateUserData(user);
                    break;
                case AGENT:
                    //查询市场用户
                    AgentUser agentUser = agentUserLoaderClient.load(userId);
                    requireNonNull(agentUser, "用户不存在!");
                    userData = generateMarketerData(agentUser);
                    break;
                case YIQIXUE:
                    //查询一起学直播用户信息并转化为统一格式
                    userData = generateYiqixueUserData(userId);
                    break;
                default:
                    MapMessage mapMessage = MapMessage.errorMessage("非法的用户类型!");
                    LoggerUtils.info("thirdparty.qiyukf.get_user_info.error", requestBody, mapMessage);
                    return mapMessage;
            }


            // 转换成七鱼的格式
            List<Map<String, Object>> resultData = transToQiyuFormat(userData);

            MapMessage mapMessage = MapMessage.successMessage()
                    .add("rlt", QiYuRLT.SUCCESS.getCode())
                    .add("data", resultData);
            mapMessage.remove("success");
            getResponse().setContentType("application/json;charset=UTF-8");
            LoggerUtils.info("thirdparty.qiyukf.get_user_info", requestBody, mapMessage);
            return mapMessage;

        } catch (Exception e) {
            MapMessage mapMessage = MapMessage.errorMessage(e.getMessage()).add("rlt", QiYuRLT.FAILED.getCode());
            LoggerUtils.info("thirdparty.qiyukf.get_user_info.error", requestBody, mapMessage);
            return mapMessage;
        } finally {
            setHeader(resp);
        }
    }

    /**
     * 获得呼叫中心的用户信息
     *
     * @param req
     * @param resp
     * @return
     */
    @RequestMapping(value = "get_call_user_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCallUserInfo(HttpServletRequest req, HttpServletResponse resp) {
        String appId = "", token = "", userIdStr = "";
        try {
            String requestBody = IOUtils.toString(req.getInputStream(), "UTF-8");
            Map<String, Object> parameters = JsonUtils.fromJson(requestBody);

            appId = SafeConverter.toString(parameters.get("appid"));
            token = SafeConverter.toString(parameters.get("token"));
            userIdStr = SafeConverter.toString(parameters.get("userid"));

            // 电话号码不能少于11位
            Validate.isTrue(userIdStr.length() >= 11, "非法的手机号");

            // 电话号码，可能包含0或者是+86，需要再截取一下
            userIdStr = userIdStr.substring(userIdStr.length() - 11, userIdStr.length());
            String actualAppId = loadCommonCoinfig("kefu_qiyu_appid");
            String currToken = getCurrentToken();
            Map<QiYuDataAttr, Object> userData = new HashMap<>();
            userData.put(QiYuDataAttr.MOBILE, userIdStr);

            if (RuntimeMode.isProduction()) {
                requireNonNull(appId, "appid不能为空!");
                requireNonNull(token, "token不能为空!");
                requireNonNull(userIdStr, "userid不能为空!");

                requireNonNull(actualAppId, "appid的配置丢失!");
                Validate.isTrue(appId.equals(actualAppId), "appId错误!");

                requireNonNull(currToken, "参数错误!");
                Validate.isTrue(token.equals(currToken), "参数错误!");
            }

            // 先查看是否是市场人员
            // 为了方便ivr后台导出统计数据，要在实际姓名前面加上客服组前缀
            AgentUser marketer = agentUserLoaderClient.findByMobile(userIdStr);
            if (marketer == null) {
                List<UserAuthentication> userAuthentications = userLoaderClient.loadMobileAuthentications(userIdStr);
                if (CollectionUtils.isNotEmpty(userAuthentications)) {
                    Function<UserAuthentication, Integer> usrTypeSortFunc = ua -> {
                        if (ua.getUserType() == null)
                            return 10;
                        else
                            return QiYuUserTypeSort.valueOf(ua.getUserType().name()).getSortValue();
                    };

                    // 按照排序取优先级最大的那个身份
                    userAuthentications.sort(Comparator.comparingInt(usrTypeSortFunc::apply));
                    UserAuthentication rank1Ua = userAuthentications.get(0);

                    // 获得对应用户
                    User rank1User = raikouSystem.loadUser(rank1Ua.getId());
                    requireNonNull(rank1User, "用户不存在!");

                    //构建用户title
                    String title = this.buildTitle(rank1User);

                    if (userAuthentications.size() > 1) {
                        userData.put(QiYuDataAttr.NAME, title + rank1User.fetchRealname());
                        userData.put(QiYuDataAttr.IDENTITY, "多身份");
                    } else {
                        // 非多身份用户就正常显示
                        userData.putAll(generateUserData(rank1User));
                        userData.compute(QiYuDataAttr.NAME, (key, oldVal) -> title + oldVal);
                    }
                } else {
                    Map<String, Object> _17xueUsr = load17XueUser(userIdStr);
                    Validate.notNull(_17xueUsr, "找不到手机号码绑定的信息!");

                    userData.put(QiYuDataAttr.NAME, "一起学-" + _17xueUsr.getOrDefault("realName", ""));
                    userData.put(QiYuDataAttr.IDENTITY, "一起学");
                }
            } else {
                userData.put(QiYuDataAttr.NAME, marketer.getRealName());
                userData.put(QiYuDataAttr.IDENTITY, "市场人员");
            }

            List<Map<String, Object>> resultData = transToQiyuFormat(userData);
            MapMessage mapMessage = MapMessage.successMessage()
                    .add("rlt", QiYuRLT.SUCCESS.getCode())
                    .add("data", resultData);
            mapMessage.remove("success");
            getResponse().setContentType("application/json;charset=UTF-8");
            LoggerUtils.info("thirdparty.qiyukf.get_call_user_info", appId, token, userIdStr, mapMessage);
            return mapMessage;

        } catch (Exception e) {
            MapMessage mapMessage = MapMessage.errorMessage(e.getMessage()).add("rlt", QiYuRLT.FAILED.getCode());
            LoggerUtils.info("thirdparty.qiyukf.get_call_user_info.error", appId, token, userIdStr, mapMessage);
            return mapMessage;
        } finally {
            setHeader(resp);
        }
    }

    /**
     * 获得呼叫中心，多身份用户iframe自定义信息页面
     *
     * @param model
     * @param req
     */
    @RequestMapping(value = "get_custom_user_info.vpage", method = RequestMethod.GET)
    public String getCustomUserInfo(Model model, HttpServletRequest req) {
        String uid = "", authTime = "", md5 = "";
        try {
            uid = req.getParameter("uid");
            authTime = req.getParameter("authTime");
            md5 = req.getParameter("md5");

            requireNonNull(uid, "uid不能为空!");
            requireNonNull(authTime, "非法的请求，authTime不能为空!");
            requireNonNull(md5, "非法的请示,md5不能为空!");

            // 校验md5值
            String expectMd5 = DigestUtils.md5Hex(loadCommonCoinfig("kefu_qifu_app_secret") + authTime.trim() + QIYU_VERIFY_TARGET);
            if (RuntimeMode.isProduction()) {
                if (!md5.equals(expectMd5)) {
                    throw new IllegalArgumentException("非法的请求，md5不匹配!");
                }
            }

            if (uid.length() < 11) {
                throw new IllegalArgumentException("非多身份号码!");
            }

            // 电话号码，可能包含0或者是+86，需要再截取一下
            String mobile = uid.substring(uid.length() - 11, uid.length());
            if (!MobileRule.isMobile(mobile)) {
                throw new IllegalArgumentException("非法的手机号码!");
            }

            List<UserAuthentication> userAuthentications = userLoaderClient.loadMobileAuthentications(mobile);
            if (CollectionUtils.isEmpty(userAuthentications)) {
                throw new IllegalArgumentException("手机号不存在!");
            }

            // 只显示多身份的
            if (userAuthentications.size() <= 1) {
                throw new IllegalArgumentException("非多身份号码!");
            }

            List<Map<String, String>> result = new ArrayList<>();
            userAuthentications.forEach(ua -> {
                User user = raikouSystem.loadUser(ua.getId());
                if (user != null) {

                    Map<String, String> ud = new LinkedHashMap<>();
                    // 将用户数据转换成以code为键值的形式，方便freemarker操作
                    generateUserData(user).forEach((k, v) -> ud.put(k.getLabel(), v.toString()));

                    result.add(ud);
                }
            });

            model.addAttribute("userInfo", result);
            LoggerUtils.info("thirdparty.qiyukf.get_custom_user_info", uid, authTime, md5, result);
        } catch (Exception e) {
            model.addAttribute("errorInfo", e.getMessage());
            LoggerUtils.info("thirdparty.qiyukf.get_custom_user_info.error", uid, authTime, md5, e.getMessage());
        }

        return "other/qifkefu_call_userinfo";
    }

    /**
     * 根据电话为用户分配客服组，若无则七鱼默认执行ivr流程
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "ivr_crm_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ivrCrmInfo(HttpServletRequest req) {
        String checkSum = "", time = "", md5 = "", phone = "";
        QiYuEventType eventType = null;
        try {
            checkSum = req.getParameter("checksum");
            time = req.getParameter("time");
            Validate.notBlank(checkSum, "参数checkSum为空!");
            Validate.notBlank(time, "参数time为空!");

            String requestBody = IOUtils.toString(req.getInputStream(), "UTF-8");
            String appSecret = loadCommonCoinfig("kefu_qifu_app_dep_secret");

            md5 = DigestUtils.md5Hex(requestBody).toLowerCase();
            String expectedCheckSum = DigestUtils.sha1Hex(appSecret + md5 + time);
            if (RuntimeMode.isProduction()) {
                Validate.isTrue(checkSum.equals(expectedCheckSum), "checkSum无效!");
            }

            MapMessage resultMsg = MapMessage.successMessage();
            Map<String, Object> parameters = JsonUtils.fromJson(requestBody);
            phone = SafeConverter.toString(parameters.get("phone"));
            eventType = QiYuEventType.of(toInt(parameters.get("eventtype")));

            //根据事件类型处理
            switch (eventType) {
                case USER_INFO:
                    //分配客服组
                    QiYuCustomerServiceGroup groupId = getIVRDistributeGroup(phone);

                    Map<String, Object> resultMap = new HashMap<>();
                    if (groupId != QiYuCustomerServiceGroup.IVR) {//已分配客服组, 默认IVR不需要传groupId
                        resultMap.put("groupId", groupId.getCode());
                    }
                    resultMsg.add("code", 200);
                    resultMsg.add("result", resultMap);
                    break;

                default://异常操作
                    resultMsg.add("code", 500);
                    resultMsg.add("result", "This operation is not supported");
                    break;
            }

            LoggerUtils.info("thirdparty.qiyukf.ivr_crm_info", checkSum, time, md5, phone, eventType, resultMsg);
            resultMsg.remove("success");
            getResponse().setContentType("application/json;charset=UTF-8");
            return resultMsg;
        } catch (Exception e) {
            MapMessage mapMessage = MapMessage.errorMessage()
                    .add("code", 500)
                    .add("message", e.getMessage());
            LoggerUtils.info("thirdparty.qiyukf.ivr_crm_info", checkSum, time, md5, phone, eventType, mapMessage);
            return mapMessage;
        }
    }

    /**
     * 获得一个号码被分配到的呼叫客服组
     *
     * @param phone 用户手机号
     * @return
     */
    private QiYuCustomerServiceGroup getIVRDistributeGroup(String phone) {
        List<UserAuthentication> uaList = userLoaderClient.loadMobileAuthentications(phone);
        if (ObjectUtils.get(() -> uaList.size(), 0) > 1) {
            return QiYuCustomerServiceGroup.IVR;
        }

        //一起学直播用户
        Map<String, Object> _17xueUsr = load17XueUser(phone);
        if (_17xueUsr != null) {
            //一起学同时属于其他身份的用户直接IVR
            if (CollectionUtils.isNotEmpty(uaList)) {
                return QiYuCustomerServiceGroup.IVR;
            }
            return QiYuCustomerServiceGroup.YIQIXUE;
        }
        //无身份：ivr
        if (CollectionUtils.isEmpty(uaList)) {
            return QiYuCustomerServiceGroup.IVR;
        }

        //已认证中、小学老师
        UserAuthentication cua = uaList.stream()
                .filter(ua -> ua.getUserType() == UserType.TEACHER)
                .findFirst()
                .orElse(null);
        if (cua != null) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(cua.getId());
            //已认证
            if (teacherDetail != null && teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS) {
                //中、小学
                return teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher() ? QiYuCustomerServiceGroup.TEACHER_M : QiYuCustomerServiceGroup.TEACHER_P;
            }
        }

        //已绑定手机中/小学生
        cua = uaList.stream()
                .filter(ua -> ua.getUserType() == UserType.STUDENT)
                .findFirst()
                .orElse(null);
        if (cua != null) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(cua.getId());
            if (studentDetail != null) {
                //中、小学
                return studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent() ? QiYuCustomerServiceGroup.STUDENT_M : QiYuCustomerServiceGroup.STUDENT_P;
            }
        }

        //已绑定手机家长: 转接到小学生客服组
        cua = uaList.stream()
                .filter(ua -> ua.getUserType() == UserType.PARENT)
                .findFirst()
                .orElse(null);
        if (cua != null) {
            return QiYuCustomerServiceGroup.STUDENT_P;
        }

        return QiYuCustomerServiceGroup.IVR;
    }

    /**
     * 判断手机号是否关联一起学账号
     *
     * @param phone
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> load17XueUser(String phone) {
        String domain = get17XueDomain();

        // 生成sig
        VendorApps vendorApps = vendorLoaderClient.loadVendor("YiQiXue");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("mobile", phone);
        String sigPromised = DigestSignUtils.signMd5(paramMap, vendorApps.getSecretKey());

        Map<String, Object> params = MapUtils.m("mobile", phone, "sig", sigPromised);
        AlpsHttpResponse resp = HttpRequestExecutor.defaultInstance()
                .get(UrlUtils.buildUrlQuery(domain + "/auth/info.vpage", params))
                .execute();

        return Optional.ofNullable(JsonUtils.fromJson(resp.getResponseString()))
                .map(m -> (Map<String, Object>) m.get("data"))
                .map(m -> m.get("17xStudents"))
                .filter(o -> o instanceof List)
                .map(o -> (ArrayList<Map<String, Object>>) o)
                .orElse(new ArrayList<>())
                .stream()
                //isRealPayUser(true——已付费)字段替换原state，兼容旧版
//                .filter(u -> (u.containsKey("isRealPayUser") ? MapUtils.getBoolean(u,"isRealPayUser") : MapUtils.getIntValue(u,"state") >= 5))
                .findFirst()
                .orElse(null);
    }

    /**
     * 生成一起学用户信息
     *
     * @param userId 一起学直播用户id
     * @return
     */
    private Map<QiYuDataAttr, Object> generateYiqixueUserData(Long userId) {
        //查询一起学直播用户信息
        Map<String, Object> yiqixueUser = this.load17XueUserById(userId);
        //封装为统一格式
        Map<QiYuDataAttr, Object> userData = new LinkedHashMap<>();
        userData.put(QiYuDataAttr.ID, userId);
        userData.put(QiYuDataAttr.NAME, yiqixueUser.get("name"));
        userData.put(QiYuDataAttr.IDENTITY, "一起学直播");
        userData.put(QiYuDataAttr.MOBILE, yiqixueUser.get("mobile"));
        userData.put(QiYuDataAttr.CLAZZ, yiqixueUser.get("grade"));
        userData.put(QiYuDataAttr.PARENT_ID, yiqixueUser.get("platformParentId"));
        userData.put(QiYuDataAttr.PAY_STATE, YiQiXuePayState.of((Integer) yiqixueUser.get("state")).getDesc());

        return userData;
    }

    /**
     * 把数据转换成七鱼的接收格式
     *
     * @param orgData 原数据
     * @return
     */
    private List<Map<String, Object>> transToQiyuFormat(Map<QiYuDataAttr, Object> orgData) {
        List<Map<String, Object>> resultData = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);

        String identity = SafeConverter.toString(orgData.get(QiYuDataAttr.IDENTITY));

        orgData.forEach((k, v) -> {
            Map<String, Object> data = new HashMap<>();

            data.put("index", index.getAndIncrement());
            data.put("key", k.getKey());
            data.put("label", k.getLabel());
            data.put("value", SafeConverter.toString(v));// 七鱼技术说，这个值必须是字串。wtf..
            data.put("zone", true);

            // 针对系统内置的参数，才设置save是true。七鱼的技术支持是这么说的...
            if (!StringUtils.isEmpty(k.getMapName())) {
                data.put("map", k.getMapName());
                data.put("save", k.isSave());
            }

            // 如果是id,并且身份是老师、学生、家长。则添加href跳转链接的参数
            if ((k == QiYuDataAttr.ID || k == QiYuDataAttr.PARENT_ID) && ("学生".equals(identity) || "老师".equals(identity) || "家长".equals(identity))) {
                String urlSuffix;
                if (RuntimeMode.isProduction()) {
                    urlSuffix = "http://admin.17zuoye.net/";
                } else
                    urlSuffix = "http://admin.test.17zuoye.net/";

                if ("学生".equals(identity)) {
                    if (k == QiYuDataAttr.ID) {
                        urlSuffix += "crm/student/studenthomepage.vpage?studentId=" + v;
                    } else if (k == QiYuDataAttr.PARENT_ID) {
                        urlSuffix += "crm/parent/parenthomepage.vpage?parentId=" + v;
                    }
                } else if ("老师".equals(identity))
                    urlSuffix += "crm/teachernew/teacherdetail.vpage?teacherId=" + v;
                else if ("家长".equals(identity))
                    urlSuffix += "crm/parent/parenthomepage.vpage?parentId=" + v;

                data.put("href", urlSuffix);
            }

            resultData.add(data);
        });

        return resultData;
    }

    /**
     * 生成市场人员的信息
     */
    private Map<QiYuDataAttr, Object> generateMarketerData(AgentUser user) {

        // 用户信息
        Map<QiYuDataAttr, Object> userData = new LinkedHashMap<>();
        Long userId = user.getId();

        userData.put(QiYuDataAttr.ID, userId);
        userData.put(QiYuDataAttr.NAME, user.getRealName());
        userData.put(QiYuDataAttr.IDENTITY, "市场人员");
        userData.put(QiYuDataAttr.MOBILE, user.getTel());

        AgentGroup group = agentOrgLoaderClient.loadAgentGroupByUserId(userId);
        if (group != null)
            userData.put(QiYuDataAttr.DEPARTMENT, group.getGroupName());

        return userData;
    }

    /**
     * 生成普通用户的信息
     */
    private Map<QiYuDataAttr, Object> generateUserData(User user) {

        // 用户信息
        Map<QiYuDataAttr, Object> userData = new LinkedHashMap<>();
        Long userId = user.getId();

        userData.put(QiYuDataAttr.ID, userId);
        userData.put(QiYuDataAttr.NAME, user.fetchRealname());

        String mobile = sensitiveUserDataServiceClient.loadUserMobile(userId, "onlinecs");
        if (!StringUtils.isEmpty(mobile))
            userData.put(QiYuDataAttr.MOBILE, mobile);

        if (UserType.STUDENT.getType() == user.getUserType()) {
            userData.put(QiYuDataAttr.IDENTITY, "学生");

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(userId);
            String clazzName = clazz == null ? "" : clazz.getClassName();

            userData.put(QiYuDataAttr.CLAZZ, clazzName);

            School school = clazz == null ? null : raikouSystem.loadSchoolIncludeDisabled(clazz.getSchoolId());
            String schoolName = school == null ? "" : school.getCmainName();

            userData.put(QiYuDataAttr.SCHOOL, schoolName);

            //家长id
            Long parentId = null;
            List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(userId);
            if (CollectionUtils.isNotEmpty(studentParents)) {
                for (StudentParent studentParent : studentParents) {
                    if (studentParent.isKeyParent()) {
                        parentId = studentParent.getParentUser().getId();
                        break;
                    }
                    if (parentId == null) {
                        parentId = studentParent.getParentUser().getId();
                    }
                }
                if (parentId != null) {
                    userData.put(QiYuDataAttr.PARENT_ID, parentId);
                    //最后登录时间
                    Date lastLoginTime = userLoginServiceClient.getUserLoginService().findUserLastLoginTime(parentId);
                    if (lastLoginTime != null) {
                        userData.put(QiYuDataAttr.LAST_LOGIN, DateUtils.dateToString(lastLoginTime));
                    }
                }
            }
        } else if (UserType.TEACHER.getType() == user.getUserType()) {
            userData.put(QiYuDataAttr.IDENTITY, "老师");

            Teacher teacher = teacherLoaderClient.loadTeacher(userId);
            if (teacher == null) {
                userData.put(QiYuDataAttr.AUTH_STATE, "");
                userData.put(QiYuDataAttr.SUBJECT, "");
                userData.put(QiYuDataAttr.SCHOOL, "");
            } else {

                AuthenticationState authState = teacher.fetchCertificationState();
                userData.put(QiYuDataAttr.AUTH_STATE, authState.getDescription());

                Subject subject = teacher.getSubject();
                if (subject != null) {
                    userData.put(QiYuDataAttr.SUBJECT, subject.getValue());
                }

                String schoolName = Optional.ofNullable(asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchool(userId).getUninterruptibly())
                        .map(School::getCmainName)
                        .orElse("");
                userData.put(QiYuDataAttr.SCHOOL, schoolName);
            }

        } else if (UserType.PARENT.getType() == user.getUserType()) {
            userData.put(QiYuDataAttr.IDENTITY, "家长");

            // 如果家长姓名为空，选第一个孩子的姓名加上称谓
            if (StringUtils.isEmpty(user.fetchRealname())) {
                List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
                if (!CollectionUtils.isEmpty(studentParentRefs)) {

                    StudentParentRef studentParentRef = studentParentRefs.get(0);
                    User firstStu = raikouSystem.loadUser(studentParentRef.getStudentId());

                    if (firstStu != null) {
                        userData.put(QiYuDataAttr.NAME, firstStu.fetchRealname() + studentParentRef.getCallName());
                    }
                }
            }

            //最后登录时间
            Date lastLoginTime = userLoginServiceClient.getUserLoginService().findUserLastLoginTime(userId);
            if (lastLoginTime != null) {
                userData.put(QiYuDataAttr.LAST_LOGIN, DateUtils.dateToString(lastLoginTime));
            }

        }

        return userData;
    }

    // @TODO 如果放缓存里面被flush掉，七鱼的请求过来就全部过期了。要不要放mongo或者aerospike?
    private String getCurrentToken() {
        Cache cache = CacheSystem.CBS.getCache("persistence");
        CacheObject<KeyValuePair<String, Long>> cacheObject = cache.get(TOKEN_CACHE_KEY);
        if (cacheObject != null) {

            KeyValuePair<String, Long> token = cacheObject.getValue();
            if (token != null) {
                Long now = System.currentTimeMillis();
                if (now < token.getValue())
                    return token.getKey();
            }
        }

        return null;
    }

    private String loadCommonCoinfig(String configKey) {
        return crmConfigService.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), configKey);
    }

    /**
     * 根据一起学用户id查询用户基本信息
     *
     * @param userId 一起学直播用户id
     * @return 一起学用户基本信息
     */
    private Map<String, Object> load17XueUserById(Long userId) {
        String domain = get17XueDomain();

        // 生成sig
        VendorApps vendorApps = vendorLoaderClient.loadVendor("YiQiXue");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userId", userId.toString());
        String sigPromised = DigestSignUtils.signMd5(paramMap, vendorApps.getSecretKey());

        Map<String, Object> params = MapUtils.m("userId", userId, "sig", sigPromised);
        String url = UrlUtils.buildUrlQuery(domain + "/auth/user/info.vpage", params);
        AlpsHttpResponse resp = HttpRequestExecutor.defaultInstance()
                .post(url)
                .execute();
        Map<String, Object> result = (Map<String, Object>) JsonUtils.fromJson(resp.getResponseString()).get("data");
        return result;
    }

    /**
     * 获取17学domain
     *
     * @return
     */
    private String get17XueDomain() {
        String domain;
        if (RuntimeMode.isUsingTestData())
            domain = "https://17xue-student.test.17zuoye.net";
        else if (RuntimeMode.isStaging())
            domain = "https://17xue-student.staging.17zuoye.net";
        else
            domain = "https://xue.17xueba.com";
        return domain;
    }

    /**
     * 构建用户title，如"小学老师"、"中学老师"、"小学学生"、"中学学生"
     *
     * @param user
     * @return
     */
    private String buildTitle(User user) {
        String result = "";
        if (user != null && user.fetchUserType() == UserType.TEACHER) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            result = (teacherDetail.isSeniorTeacher() || teacherDetail.isJuniorTeacher() ? "中学" : "小学") + user.fetchUserType().getDescription() + "-";
        } else if (user != null && user.fetchUserType() == UserType.STUDENT) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            result = (studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent() ? "中学" : "小学") + user.fetchUserType().getDescription() + "-";
        }
        return result;
    }

    /**
     * 设置header
     */
    private void setHeader(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Headers", "origin, x-csrftoken, content-type, accept, x-auth-code, X-App-Id, X-Token");
        resp.setHeader("Access-Control-Allow-Method", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Origin", "*");
    }


}
