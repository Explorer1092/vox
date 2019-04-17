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

package com.voxlearning.washington.controller.client;


import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.AlpsConversionService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.upgrade.ApkInfo;
import com.voxlearning.utopia.core.upgrade.ApkUpgradeProvider;
import com.voxlearning.utopia.core.upgrade.UpgradeCond;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.config.api.entity.*;
import com.voxlearning.utopia.service.config.client.AdvertisementSlotServiceClient;
import com.voxlearning.utopia.service.config.client.ClientApplicationConfigServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.vendor.api.constant.ClientAppConfigType;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
public class ClientController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AdvertisementSlotServiceClient advertisementSlotServiceClient;
    @Inject private ClientApplicationConfigServiceClient clientApplicationConfigServiceClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private UserAdvertisementServiceClient userAdvertisementServiceClient;

    private static final String appProxySecretKey = "17zuoye";
    private static final Set<String> ANDROID_PRODUCT_ID = new HashSet<>();
    private static final Set<String> U3D_PRODUCT_ID = new HashSet<>();
    private static final Set<String> auditUserAccountList;

    static {
        ANDROID_PRODUCT_ID.add("100");
        ANDROID_PRODUCT_ID.add("1000");
        ANDROID_PRODUCT_ID.add("200");
        ANDROID_PRODUCT_ID.add("2000");
        ANDROID_PRODUCT_ID.add("300");
        ANDROID_PRODUCT_ID.add("3000");
        ANDROID_PRODUCT_ID.add("900");
        ANDROID_PRODUCT_ID.add("400");
        U3D_PRODUCT_ID.add("500");
        U3D_PRODUCT_ID.add("501");
        U3D_PRODUCT_ID.add("101501");
        U3D_PRODUCT_ID.add("100501");
        U3D_PRODUCT_ID.add("101502");
        U3D_PRODUCT_ID.add("100502");
        U3D_PRODUCT_ID.add("101503");
        U3D_PRODUCT_ID.add("100503");
        U3D_PRODUCT_ID.add("101504");
        U3D_PRODUCT_ID.add("100504");
        U3D_PRODUCT_ID.add("101505");
        U3D_PRODUCT_ID.add("100505");
        U3D_PRODUCT_ID.add("400");
        U3D_PRODUCT_ID.add("701");
        U3D_PRODUCT_ID.add("110");
        U3D_PRODUCT_ID.add("310");

        // init audit account
        auditUserAccountList = Collections.unmodifiableSet(
                new LinkedHashSet<>(
                        Arrays.asList(
                                "3921029",
                                "12422307",
                                "20001")
                )
        );
    }

    @RequestMapping(value = "servertime.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Long serverTime() {
        return System.currentTimeMillis();
    }

    @RequestMapping(value = "client-update.vpage", method = RequestMethod.GET)
    public String clientPc() {
        getResponse().setHeader("Content-Type", "application/json");
        return "client/client-update";
    }

    @RequestMapping(value = "client-update-student-homework-app.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> clientMobStudent() {
        getResponse().setHeader("Content-Type", "application/json");
        String data = getPageBlockContentGenerator().getPageBlockContentHtml("student_homework_app_config", "student_mobile_app");
        data = data.replace("\r", "").replace("\n", "").replace("\t", "");
        return JsonUtils.fromJson(data);
    }

    @RequestMapping(value = "client-app-version.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clientAppVersion() {
        String os = getRequestString("os").toLowerCase();
        String osVer = getRequestString("osver");
        String hw = getRequestString("hw");
        String appVer = getRequestString("appver");
        String device = getRequestString("device");

        if (!"android".equals(os) && !"ios".equals(os)) {
            return MapMessage.errorMessage("未知的设备OS:" + os);
        }

        try {
            String upgradeConfig = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "client_app_version_info");
            upgradeConfig = upgradeConfig.replace("\r", "").replace("\n", "").replace("\t", "");
            Map<String, Object> config = JsonUtils.fromJson(upgradeConfig);

            // 如果有 pre-release, 需要进行灰度发布的控制
            if (config.containsKey("pre_release")) {
                Map preReleaseConfig = (Map) config.get("pre_release");
                if (preReleaseConfig.containsKey(os)) {
                    Map preReleaseOsConfig = (Map) preReleaseConfig.get(os);
                    String preReleaseVersion = String.valueOf(preReleaseOsConfig.get("version"));
                    String preReleaseBaseVersion = String.valueOf(preReleaseOsConfig.get("base_version"));
                    String preReleaseNotice = String.valueOf(preReleaseOsConfig.get("notice"));
                    String preReleaseUrl = String.valueOf(preReleaseOsConfig.get("apk_url"));
                    boolean preReleaseUpgradeCheck = (boolean) preReleaseOsConfig.get("must_upgrade");
                    List preReleaseTargetDevices = (List) preReleaseOsConfig.get("target_user_devices");

                    if (preReleaseTargetDevices.contains(device)) {
                        if (StringUtils.isBlank(preReleaseBaseVersion) || compareAppVersion(preReleaseVersion, preReleaseBaseVersion) == 0) {
                            if (compareAppVersion(preReleaseVersion, appVer) > 0) {
                                return MapMessage.successMessage()
                                        .add("is_latest_version", false)
                                        .add("must_upgrade", false)
                                        .add("notice", preReleaseNotice)
                                        .add("apk_url", preReleaseUrl);
                            } else {
                                return MapMessage.successMessage()
                                        .add("is_latest_version", true)
                                        .add("must_upgrade", false)
                                        .add("notice", "")
                                        .add("apk_url", "");
                            }
                        } else {
                            if (compareAppVersion(preReleaseBaseVersion, appVer) > 0) {
                                return MapMessage.successMessage()
                                        .add("is_latest_version", false)
                                        .add("must_upgrade", false)
                                        .add("notice", preReleaseNotice)
                                        .add("apk_url", preReleaseUrl);
                            } else if (compareAppVersion(preReleaseBaseVersion, appVer) <= 0 && compareAppVersion(preReleaseVersion, appVer) > 0) {
                                return MapMessage.successMessage()
                                        .add("is_latest_version", false)
                                        .add("must_upgrade", preReleaseUpgradeCheck)
                                        .add("notice", preReleaseNotice)
                                        .add("apk_url", preReleaseUrl);
                            } else {
                                return MapMessage.successMessage()
                                        .add("is_latest_version", true)
                                        .add("must_upgrade", false)
                                        .add("notice", "")
                                        .add("apk_url", "");
                            }
                        }
                    }
                }
            }

            List upgradeRules = (List) config.get(os);
            if (upgradeRules == null || upgradeRules.size() == 0) {
                return MapMessage.successMessage()
                        .add("is_latest_version", true)
                        .add("must_upgrade", false)
                        .add("notice", "")
                        .add("apk_url", "");
            }

            String upgradeCheckKey = appVer + "_" + hw + "_" + osVer;

            for (Object upgradeRule1 : upgradeRules) {
                Map upgradeRule = (Map) upgradeRule1;
                Set<String> keys = upgradeRule.keySet();
                for (String upgradeKey : keys) {
                    if (upgradeCheckKey.matches(upgradeKey)) {
                        MapMessage result = MapMessage.successMessage();
                        result.putAll((Map) upgradeRule.get(upgradeKey));
                        return result;
                    }
                }
            }

            return MapMessage.successMessage()
                    .add("is_latest_version", true)
                    .add("must_upgrade", false)
                    .add("notice", "")
                    .add("apk_url", "");

        } catch (Exception e) {
            logger.error("client app upgrade check error.", e);
            return MapMessage.errorMessage("系统错误!");
        }
    }

    @RequestMapping(value = "client-load-image.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clientLoadImage() {
        String sys = getRequestString("sys");
        String productId = getRequestString("productId");
        String apkVer = getRequestString("apkVer");
        String region = getRequestString("region");
        String school = getRequestString("school");
        String date = DateUtils.nowToString("yyyyMMdd");
        StringBuilder builder = new StringBuilder();
        builder.append(sys).append("_").append(productId).append("_").append(apkVer).append("_").append(region).append("_").append(school).append("_").append(date);
        String source = builder.toString();
        MapMessage resultMap = new MapMessage();
        try {
            String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "client_load_image");
            regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
            Map<String, Object> regMap = JsonUtils.fromJson(regStr);
            if (regMap != null && !regMap.isEmpty()) {
                Set<String> keys = regMap.keySet();
                for (String key : keys) {
                    if (source.matches(key)) {
                        Map<String, Object> val = (Map) regMap.get(key);
                        if (val != null) {
                            resultMap.putAll(val);
                            break;
                        }
                    }
                }
            }
            regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "client_welcome_image");
            regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
            regMap = JsonUtils.fromJson(regStr);
            if (regMap != null && !regMap.isEmpty()) {
                Set<String> keys = regMap.keySet();
                for (String key : keys) {
                    if (source.matches(key)) {
                        Map<String, Object> val = (Map) regMap.get(key);
                        if (val != null) {
                            resultMap.putAll(val);
                            break;
                        }
                    }
                }
            }
            resultMap.add("result", "success");
            return resultMap;
        } catch (Exception e) {
            logger.error("clientLoadImage - Excp : {}", e);
            resultMap.add("result", "failure");
            return resultMap;
        }
    }

    @RequestMapping(value = "client-upgrade-image.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clientUpgradeImage() {
        String sys = getRequestString("sys");                 // 系统
        String productId = getRequestString("productId");     // 产品ID
        String apkVer = getRequestString("apkVer");           // 版本
        String ktwelve = getRequestString("ktwelve");         // 学段
        String subject = getRequestString("subject");         // 学科
        String region = getRequestString("region");           // 地区
        String school = getRequestString("school");           // 学校
        String userId = getRequestString("userId");           // 用户ID

        // 拼接参数
        List<String> params = Arrays.asList(
                sys, productId, apkVer, ktwelve, subject, region, school, userId
        );
        String source = StringUtils.join(params, "_");
        MapMessage resultMap = new MapMessage();
        try {
            String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "client_upgrade_image");
            regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
            Map<String, Object> regMap = JsonUtils.fromJson(regStr);
            if (regMap != null && !regMap.isEmpty()) {
                Set<String> keys = regMap.keySet();
                for (String key : keys) {
                    if (source.matches(key)) {
                        Map<String, Object> val = (Map) regMap.get(key);
                        if (val != null) {
                            resultMap.putAll(val);
                            break;
                        }
                    }
                }
            }

            resultMap.add("result", "success");
            return resultMap;
        } catch (Exception e) {
            logger.error("Failed load clientUpgradeImage - Excp : {}", e);
            resultMap.add("result", "failure");
            return resultMap;
        }
    }


    //=====================================控制app内部是否使用proxy模块=============================================//
    //==============按照一定的灰度策略控制是否可用,如果可用,则下发dns配置,注意:下发的配置信息需要签名,否则被劫持都不知道======//
    @RequestMapping(value = "appnetworkproxypolicy.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage appNetworkProxyPolicy() {
        String productId = getRequestString("productId");
        String region = getRequestString("region");
        String userId = getRequestString("user");
        MapMessage resultMap = new MapMessage();
        try {
            if (StringUtils.isBlank(productId)) {
                resultMap.add("use_proxy", false);
                resultMap.add("result", "success");
                return resultMap;
            }
            // 审核账户不开启代理
            if (auditUserAccountList.contains(userId)) {
                resultMap.add("use_proxy", false);
                resultMap.add("result", "success");
                return resultMap;
            }
            String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "app_network_proxy_policy");
            regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
            Map<String, Object> regMap = JsonUtils.fromJson(regStr);
            if (MapUtils.isEmpty(regMap)) {
                resultMap.add("use_proxy", false);
                resultMap.add("result", "success");
                return resultMap;
            }
            Object map = regMap.get(productId);
            if (map == null || !(map instanceof Map)) {
                resultMap.add("use_proxy", false);
                resultMap.add("result", "success");
                return resultMap;
            }
            Object settings = regMap.get("settings");
            if (settings == null || !(settings instanceof Map)) {
                resultMap.add("use_proxy", false);
                resultMap.add("result", "success");
                return resultMap;
            }

            // 所有检查通过,返回dns配置列表
            Map<String, String> sigMap = new HashMap<>();

            Map<String, Object> settingMap = (Map) settings;
            for (String key : settingMap.keySet()) {
                Object value = settingMap.get(key);
                if (value instanceof List || value instanceof Map) {
                    sigMap.put(key, JsonUtils.toJson(settingMap.get(key)));
                } else if (value instanceof String) {
                    sigMap.put(key, SafeConverter.toString(settingMap.get(key)));
                }
            }
            String sig = DigestSignUtils.signMd5(sigMap, appProxySecretKey);
            resultMap.add("sig", sig);
            resultMap.add("settings", settingMap);
            // extensions 不做sig验证
            resultMap.add("extensions", regMap.get("extensions"));
            resultMap.add("result", "success");

            Map<String, Object> configMap = (Map) map;
            // 检查总开关
            if (!SafeConverter.toBoolean(configMap.get("use_proxy"))) {
                resultMap.add("use_proxy", false);
                return resultMap;
            }

            // 按区域检查
            if (!checkRegion(SafeConverter.toString(configMap.get("check_region")), region)) {
                resultMap.add("use_proxy", false);
                return resultMap;
            }

            resultMap.add("use_proxy", true);
            return resultMap;
        } catch (Exception e) {
            logger.error("appNetworkProxyPolicy - Excp : {}", e);
            resultMap.add("result", "500");
            return resultMap;
        }
    }


    // 新版APP闪屏广告图获取接口
    @RequestMapping(value = "loadwelcomeimg.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadWelcomeImg() {
        String sys = getRequestString("sys");             // 操作系统
        String sysVer = getRequestString("sysVer");       // 操作系统版本
        String productId = getRequestString("productId"); // 客户端产品ID
//        String apkVer = getRequestString("apkVer");       // 客户端app版本
        String ver = getRequestString("ver");       // 客户端app版本
        String userId = getRequestString("userId");       // 登陆用户ID
        MapMessage resultMap = new MapMessage();
        if (StringUtils.isBlank(productId)) {
            resultMap.add("result", "400");
            resultMap.add("message", "productId must be exist!");
            return resultMap;
        }
        if (Objects.equals(20001L, SafeConverter.toLong(userId))) {
            resultMap.add("result", "success");
            return resultMap;
        }
        try {
            // 通过productId 获取广告位
            String slotId = "";
            if (productId.startsWith("10")) {
                slotId = "320301";  // 小学学生app
            } else if (productId.startsWith("11")) {
                slotId = "350102";  // 中学学生app
            } else if (productId.startsWith("31")) {
                slotId = "150103";  // 中学老师app
            } else if (productId.startsWith("21")) {
                slotId = "250102";  // 中学家长app
            } else if (productId.startsWith("2")) {
                slotId = "220501";  // 家长app
            } else if (productId.startsWith("3")) {
                slotId = "120301";  // 老师app
            }
            AdvertisementSlot slot = advertisementSlotServiceClient.getAdvertisementSlotBuffer().load(slotId);
            if (slot == null) {
                resultMap.add("result", "400");
                resultMap.add("message", "广告位不存在");
                return resultMap;
            }

            // 添加一个参数 是否展示联盟广告
            boolean showAllianceAd = false;
            if (StringUtils.isNotBlank(userId)) {
                List<User> students = studentLoaderClient.loadParentStudents(SafeConverter.toLong(userId));
                if (CollectionUtils.isNotEmpty(students)) {
                    for (User user : students) {
                        StudentDetail detail = studentLoaderClient.loadStudentDetail(user.getId());
                        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(detail, "AD", "ShowAlliance")) {
                            // 只要有一个中灰度了 就过。
                            showAllianceAd = true;
                            break;
                        }
                    }
                }
            }
            resultMap.add("showAllianceAd", showAllianceAd); // 是否展示联盟广告

            List<NewAdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(SafeConverter.toLong(userId), slotId, sys, ver);
            // 这里只有一张图片
            if (CollectionUtils.isEmpty(data)) {
                resultMap.add("result", "success");
                resultMap.add("imgInfo", new HashMap<>());
                return resultMap;
            }
            // 打点
            for (int i = 0; i < data.size(); i++) {
                if (Boolean.FALSE.equals(data.get(i).getLogCollected())) {
                    continue;
                }
                // log
                LogCollector.instance().info("sys_new_ad_show_logs",
                        MiscUtils.map(
                                "user_id", userId,
                                "env", RuntimeMode.getCurrentStage(),
                                "version", ver,
                                "aid", data.get(i).getId(),
                                "acode", data.get(i).getCode(),
                                "index", i,
                                "slotId", slotId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", getRequest().getHeader("User-Agent"),
                                "uuid", UUID.randomUUID().toString(),
                                "system", sys,
                                "system_version", sysVer
                        ));
            }
            Map<String, Object> dataMap = adToMap(MiscUtils.firstElement(data), ver, sys, slotId, SafeConverter.toLong(userId));
            resultMap.add("result", "success");
            resultMap.add("imgInfo", dataMap);
            return resultMap;
        } catch (Exception ex) {
            logger.error("clientLoadWelcomeImg - Excp : {}", ex);
            resultMap.add("result", "500");
            return resultMap;
        }
    }

    private Map<String, Object> adToMap(NewAdMapper adMapper, String version, String system, String slotId, Long userId) {
        Map<String, Object> data = new HashMap<>();
        // 设置返回的参数
        data.put("id", adMapper.getId());
        data.put("imgUrl", combineMessageUrl(adMapper.getImg()));
        data.put("gifUrl", combineMessageUrl(adMapper.getGif()));
        data.put("extUrl", combineMessageUrl(adMapper.getExt()));
        data.put("showSeconds", adMapper.getShowSeconds());
        if (adMapper.getHasUrl()) {
            // 首先过滤http的URL和非HTTP的URL
            if (adMapper.getUrl().startsWith("http")) {
                String url = AdvertiseRedirectUtils.redirectUrl(adMapper.getId(), 0, version, system, "", 0L);
                data.put("linkUrl", generateLinkUrl(url));
            } else {
                // 这里要处理对应的环境 ios android
                // url格式 ios:xxxxxx|||android:xxxxx
                data.put("linkUrl", "");
                String url = adMapper.getUrl();
                if (StringUtils.isNotBlank(url)) {
                    String[] urlArray = StringUtils.split(url, "|||");
                    if (urlArray != null && urlArray.length == 2) {
                        data.put("linkUrl", filterUrl(urlArray, system));
                    }
                }
            }
        } else {
            data.put("linkUrl", "");
        }
        data.put("showLogo", adMapper.getShowLogo());
        data.put("startTime", adMapper.getShowStartTime());
        data.put("endTime", adMapper.getShowEndTime());
        data.put("needLogin", adMapper.getNeedLogin());
        data.put("slotId", slotId);
        data.put("acode", adMapper.getCode());
        return data;
    }

    private String filterUrl(String[] urlArray, String system) {
        if (StringUtils.isBlank(system)) {
            return "";
        }
        // 获取配置信息
        for (String url : urlArray) {
            if (StringUtils.isNotBlank(url) && url.startsWith(system)) {
                // 找到了 返回
                String[] array = StringUtils.split(url, "$");
                if (array.length != 2) {
                    return "";
                } else {
                    return array[1];
                }
            }
        }
        return "";
    }

    private String combineMessageUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        return getCdnBaseUrlStaticSharedWithSep() + "gridfs/" + url;
    }

    private String generateLinkUrl(String link) {
        if (StringUtils.isBlank(link)) {
            return "";
        }
        return fetchMainsiteUrlByCurrentSchema() + link;
    }

    //=====================================线上app快速审核通过api相关=============================================//
    //==============根据不同的app productId，返回相关信息。用于制造崩溃假象，欺骗AppStore审核，从而快速审核通过========//
    //==============切记，此后门不能常用，万一被苹果搞了，得不偿失。常在河边走，哪有不湿鞋。===========================//
    @RequestMapping(value = "rapidauditinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage rapidAuditInfo() {
        String productId = getRequestString("productId");
        MapMessage resultMap = new MapMessage();
        String str = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "app_rapid_audit_info");
        str = str.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> map = JsonUtils.fromJson(str);
        if (MapUtils.isEmpty(map)) {
            resultMap.set("result", "500");
            resultMap.set("message", "empty rapid audit info");
            return resultMap;
        }
        resultMap.add("info", map.get(productId));
        resultMap.add("result", "success");
        return resultMap;
    }


    //=====================================战略数据收集api相关=============================================//
    //========================收集用户经纬度，安装列表，活跃进程等信息的控制策略===============================//

    /**
     * 获取抓取数据的频率，单位秒
     */
    @RequestMapping(value = "heartbeatfrequency.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchHeartbeatFrequency() {
        String productId = getRequestString("productId");
        String ktwelve = StringUtils.defaultIfBlank(getRequestString("ktwelve"), Ktwelve.PRIMARY_SCHOOL.name());
        int defaultFrequency = 300;                         // 默认心跳检测的频率，单位秒
        int defaultCollectFrequency = 86400;                // 默认收集数据的限制，单位秒。（多长时间之内不再收集）
        MapMessage resultMap = new MapMessage();
        String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "app_collect_data_frequency_v1");
        regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> regMap = JsonUtils.fromJson(regStr);
        if (MapUtils.isEmpty(regMap)) {
            resultMap.set("result", "500");
            resultMap.set("message", "empty regMap");
            return resultMap;
        }
        List<Map<String, Object>> configList = (List) regMap.get(productId);
        if (CollectionUtils.isEmpty(configList)) {
            resultMap.set("result", "500");
            resultMap.set("message", "empty config");
            return resultMap;
        }

        Map<String, Object> config = configList.stream()
                .filter(e -> Objects.equals(e.get("ktwelve"), ktwelve))
                .findFirst()
                .orElse(null);
        if (config == null) {
            resultMap.set("result", "500");
            resultMap.set("message", "empty config");
            return resultMap;
        }
        resultMap.add("result", "success");
        resultMap.add("frequency", SafeConverter.toInt(regMap.get("frequency"), defaultFrequency));
        resultMap.add("lbs", SafeConverter.toInt(config.get("lbs"), defaultCollectFrequency));
        resultMap.add("installed_app_list", SafeConverter.toInt(config.get("installed_app_list"), defaultCollectFrequency));
        resultMap.add("active_app_list", SafeConverter.toInt(config.get("active_app_list"), defaultCollectFrequency));
        return resultMap;
    }

    /**
     * 获取抓取策略的配置
     */
    @RequestMapping(value = "collectdatasetting.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage collectData() {

        String checkNetwork = getRequestString("network");
        String checkRegion = getRequestString("region");
        String productId = getRequestString("productId");
        String ktwelve = StringUtils.defaultIfBlank(getRequestString("ktwelve"), Ktwelve.PRIMARY_SCHOOL.name());
        MapMessage resultMap = new MapMessage();
        String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "app_collect_data_setting_v1");
        regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> regMap = JsonUtils.fromJson(regStr);
        if (MapUtils.isEmpty(regMap)) {
            resultMap.add("result", "success");
            resultMap.add("collect_flag", false);
            resultMap.add("message", "empty regMap");
            return resultMap;
        }
        List<Map<String, Object>> configList = (List) regMap.get(productId);
        if (CollectionUtils.isEmpty(configList)) {
            resultMap.add("result", "success");
            resultMap.add("collect_flag", false);
            resultMap.add("message", "empty config");
            return resultMap;
        }

        Map<String, Object> config = configList.stream()
                .filter(e -> Objects.equals(e.get("ktwelve"), ktwelve))
                .findFirst()
                .orElse(null);

        if (config == null) {
            resultMap.add("result", "success");
            resultMap.add("collect_flag", false);
            resultMap.add("message", "empty config");
            return resultMap;
        }

        boolean collectFlag = SafeConverter.toBoolean(config.get("collect_flag"), false);
        if (!collectFlag) {
            resultMap.add("result", "success");
            resultMap.add("collect_flag", false);
            return resultMap;
        }
        Map<String, Object> setting = (Map) config.get("setting");
        if (MapUtils.isEmpty(setting)) {
            resultMap.add("result", "success");
            resultMap.add("collect_flag", false);
            return resultMap;
        }
        String configTime = SafeConverter.toString(setting.get("check_time"));
        String configNetwork = SafeConverter.toString(setting.get("check_network"));
        String configRegion = SafeConverter.toString(setting.get("check_region"));

        //兼容家长端，传来的region参数是所有孩子的region用逗号分隔的字符串，只要有一个孩子匹配即为成功
        String[] checkRegionSplit = checkRegion.split(",");
        List<String> checkRegionList = new ArrayList<>();
        for (String region : checkRegionSplit) {
            checkRegionList.add(region);
        }

        if (checkTime(configTime, System.currentTimeMillis())
                && checkNetwork(configNetwork, checkNetwork)
                && checkRegionList.stream().anyMatch(e -> checkRegion(configRegion, e))) {
            resultMap.add("collect_flag", true);
            resultMap.add("collect_begin_time", setting.get("response_collect_begin_time"));
            resultMap.add("collect_end_time", setting.get("response_collect_end_time"));
            resultMap.add("collect_frequency", setting.get("response_collect_frequency"));
            resultMap.add("network_switch", setting.get("response_network_switch"));
            resultMap.add("rate", setting.get("response_percent"));
            resultMap.add("force_collect_rule", setting.get("response_force_collect_rule"));
        } else {
            resultMap.add("collect_flag", false);
        }
        resultMap.add("result", "success");
        return resultMap;
    }

    private boolean checkNetwork(String configNetwork, String reqNetwork) {
        if (StringUtils.isBlank(configNetwork)) {
            return true;
        }

        // 首先对network按照 , 分割
        String[] configNetworkList = configNetwork.trim().split(",");
        for (String network : configNetworkList) {
            if (checkString(network, reqNetwork)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPercent(String configPercent) {
        if (StringUtils.isBlank(configPercent)) {
            return true;
        }
        int percent = SafeConverter.toInt(configPercent, 0);
        return RandomUtils.nextInt(1, 100) < percent;
    }

    // app version 默认都是x.y.z格式,如果出现了其他类型,直接作为错误返回
    private static int compareAppVersion(String ver1, String ver2) {
        String[] ver1List = ver1.split("\\.");
        String[] ver2List = ver2.split("\\.");

        if (ver1List.length != 3 || ver2List.length != 3) {
            throw new RuntimeException("app version is not correct!, ver1:" + ver1 + ", ver2:" + ver2);
        }

        int ver1First = Integer.parseInt(ver1List[0]);
        int ver1Second = Integer.parseInt(ver1List[1]);
        int ver1Third = Integer.parseInt(ver1List[2]);
        int ver2First = Integer.parseInt(ver2List[0]);
        int ver2Second = Integer.parseInt(ver2List[1]);
        int ver2Third = Integer.parseInt(ver2List[2]);

        if (ver1First == ver2First && ver1Second == ver2Second && ver1Third == ver2Third) {
            return 0;
        }

        if (ver1First > ver2First) {
            return 1;
        } else if (ver1Second > ver2Second) {
            return 1;
        } else if (ver1Third > ver2Third) {
            return 1;
        }

        return -1;
    }

    // ===============================================================================================================
    // APP VER3升级检查策略用到的常量定义
    static final String REQ_PRODUCT_ID = "productId";
    static final String REQ_PRODUCT_NAME = "productName";
    static final String REQ_APK_NAME = "apkName";
    static final String REQ_APK_VERSION = "apkVer";
    static final String REQ_ANDROID_VERCODE = "androidVerCode";
    static final String REQ_SDK_VERSION = "sdkVer";
    static final String REQ_SYS_VERSION = "sysVer";
    static final String REQ_CHANNEL = "channel";
    static final String REQ_REGION_CODE = "region";
    static final String REQ_KTWELVE = "ktwelve";
    static final String REQ_SCHOOL = "school";
    static final String REQ_CLAZZ = "clazz";
    static final String REQ_SUBJECT = "subject";
    static final String REQ_CLAZZ_LEVEL = "clazzLevel";
    static final String REQ_USER = "user";
    static final String REQ_USER_TYPE = "userType";
    static final String REQ_IMEI = "imei";
    static final String REQ_BRAND = "brand";
    static final String REQ_MODEL = "model";
    static final String REQ_MOBILE = "mobile";
    static final String REQ_TEST = "test";
    static final String REQ_IS_AUTO = "isAuto";
    static final String REQ_MD5 = "apkMD5";

    // Ver3的APP升级检查【下期再用，留作备份】
    @RequestMapping(value = "app3/upgrade.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage appUpgradeCheck() {
        // APP请求参数
        String reqProductId = getRequestString(REQ_PRODUCT_ID);
        String reqApkVersion = getRequestString(REQ_APK_VERSION);
        String reqAndroidVerCode = getRequestString(REQ_ANDROID_VERCODE);
        String reqSdkVersion = getRequestString(REQ_SDK_VERSION);
        String reqChannel = getRequestString(REQ_CHANNEL);
        String reqUserType = getRequestString(REQ_USER_TYPE);
        String reqUser = getRequestString(REQ_USER);
        String reqImei = getRequestString(REQ_IMEI);
        final String reqTest = getRequestString(REQ_TEST);
        //差分包升级本地包的MD5
        String reqMD5 = getRequestString(REQ_MD5);

        //没有此参数视为自动升级
        Boolean isAuto = getRequestBool(REQ_IS_AUTO, true);

        MapMessage resultMap = new MapMessage();

        // FIXME 有一些非法请求，imei巨长无比，忽略
        if (reqImei.length() > 150) {
            resultMap.set("result", "400");
            resultMap.set("message", "非法的请求参数!");
            return resultMap;
        }

        // 记录版本号
        incUserApkVerCount(reqImei, reqProductId, reqApkVersion);

        try {
            List<ClientAppUpgradeCtl> ctls = clientApplicationConfigServiceClient.getClientAppUpgradeCtlBuffer()
                    .dump().getClientAppUpgradeCtls();
            ctls = ctls.stream().filter(source -> {
                        boolean isSameProduct = reqProductId.equals(source.getProductId());
                        if (isSameProduct && StringUtils.isBlank(reqTest)) {
                            return "published".equals(source.getStatus());
                        } else {
                            return isSameProduct;
                        }
                    }
            ).collect(Collectors.toList());
            //自动升级请求：过滤手动升级的配置
            //手动升级：不做处理。直接拿全部配置
            if (isAuto) {
                ctls = ctls.stream().filter(p -> p.getIsManual() == null || Boolean.FALSE == p.getIsManual()).collect(Collectors.toList());
            }
            //处理一下没有选择普通升级和强制升级的数据
            //前端没有选的都认为是普通升级
            ctls.stream().filter(p -> StringUtils.isBlank(p.getResponse().getUpgradeType())).forEach(p -> p.getResponse().setUpgradeType("1"));
            //强制升级优先
            Comparator<ClientAppUpgradeCtl> c = Comparator.comparingInt(o -> SafeConverter.toInt(o.getResponse().getUpgradeType()));
            //再按rank排
            c = c.thenComparing((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.getRank()), SafeConverter.toInt(o1.getRank())));
            ctls = ctls.stream().sorted(c).collect(Collectors.toList());

            ClientAppUpgradeCtl ctl;
            if ("200".equals(reqProductId) || "201".equals(reqProductId)) {
                ctl = getUpdateConfigForParentApp(ctls);
            } else {
                ctl = getUpdateConfigForTeacherOrStudentApp(ctls);
            }
            if (ctl != null) {
                ClientAppUpgradeCtlResponse response = ctl.getResponse();
                //这些是iOS和android升级都需要的参数
                resultMap.add("result", "success");
                resultMap.add("upgrade", true);
                resultMap.add("productId", response.getProductId());
                resultMap.add("apkVer", response.getApkVer());
                resultMap.add("upgradeType", response.getUpgradeType());
                resultMap.add("description", response.getDescription());
                resultMap.add("updateTime", response.getUpdateTime());
                resultMap.add("packageName", response.getPackageName());
                resultMap.add("scheme", response.getScheme());
                resultMap.add("image", response.getImage());
                resultMap.add("closeHardwareAccelerationList", closeHardwareAccelerationList(reqProductId));

                resultMap.add("closeCrosswalkList", getCloseCrossWalkList(reqProductId));
                resultMap.add("wonderCloseCrosswalkList", internalGetCloseCrossWalkList("new_wonder_crosswalk_close"));

                //如果admin配置了response则不取config里面的
                if (StringUtils.isNotBlank(response.getApkUrl())) {
                    //整包参数
                    resultMap.add("apkSize", response.getApkSize());
                    resultMap.add("apkUrl", response.getApkUrl());
                    resultMap.add("apkMD5", response.getApkMD5().toUpperCase());
                } else if (ANDROID_PRODUCT_ID.contains(reqProductId) || U3D_PRODUCT_ID.contains(reqProductId)) {
                    //android才调差分升级接口
                    UpgradeCond cond = new UpgradeCond();
                    cond.setProductId(reqProductId);
                    cond.setChannel(reqChannel);
                    cond.setVersionTo(response.getApkVer());
                    cond.setClientApkMD5(reqMD5);
                    ApkInfo apkInfo = ApkUpgradeProvider.getApkUpgradeInfo(cond);
                    //这下面的是android升级需要的下载地址
                    //差分包参数
                    if (apkInfo != null) {
                        if (apkInfo.isPatch()) {
                            resultMap.add("patchSize", apkInfo.getSize());
                            resultMap.add("patchUrl", apkInfo.getUrl());
                            resultMap.add("patchMD5", apkInfo.getMd5().toUpperCase());
                        }
                        //整包参数
                        resultMap.add("apkSize", response.getApkSize());
                        resultMap.add("apkUrl", apkInfo.getCompleteApkInfo().getUrl());
                        resultMap.add("apkMD5", apkInfo.getCompleteApkInfo().getMd5().toUpperCase());
                    } else {
                        //android命中了升级。但是没有升级包
                        com.voxlearning.alps.spi.bootstrap.LogCollector.info("client_app_upgrade_logs",
                                MapUtils.map(
                                        REQ_PRODUCT_ID, reqProductId,
                                        REQ_APK_VERSION, reqApkVersion,
                                        "target_version", response.getApkVer(),
                                        "client_md5", reqMD5,
                                        REQ_CHANNEL, reqChannel,
                                        REQ_USER, reqUser,
                                        "op", "no_patch",
                                        "env", RuntimeMode.current().name(),
                                        "upgrade", "false",
                                        "upgradeType", response.getUpgradeType(),
                                        "appClientIp", getWebRequestContext().getRealRemoteAddr()
                                ));
                        return resultMap;
                    }
                }

                return resultMap;
            }
            resultMap.set("result", "success");
            resultMap.set("upgrade", false);
            resultMap.add("closeHardwareAccelerationList", closeHardwareAccelerationList(reqProductId));

            resultMap.add("closeCrosswalkList", getCloseCrossWalkList(reqProductId));
            resultMap.add("wonderCloseCrosswalkList", internalGetCloseCrossWalkList("new_wonder_crosswalk_close"));

            return resultMap;
        } catch (Exception e) {
            logger.error("client app upgrade check ver3 error.", e);
            resultMap.set("result", "500");
            resultMap.set("message", "系统错误!");
            return resultMap;
        }
    }

    @RequestMapping(value = "closehalist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getCloseHardwardAcceleration() {
        return MapMessage.successMessage().add("halist", closeHardwareAccelerationList(""));
    }

    //CMR-JSPatch
    @RequestMapping(value = "/getpatch.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getPatch() {
        String productId = getRequestString("productId");
        String apkVer = getRequestString("apkVer");
        String sysVer = getRequestString("sysVer");
        String userId = getRequestString("user");
        String model = getRequestString("model");
        String ktwelve = getRequestString("ktwelve");

        MapMessage resultMap = new MapMessage();
        try {
            List<ClientAppConfigCtl> ctls = clientApplicationConfigServiceClient.getClientAppConfigCtlBuffer()
                    .loadPublished()
                    .stream()
                    .filter(e -> (e.getProductId().equals(productId) && ClientAppConfigType.JSPATCH.name().equals(e.getType())))
                    .sorted((o1, o2) -> {
                        long c1 = SafeConverter.toLong(o1.getCreateDateTime());
                        long c2 = SafeConverter.toLong(o2.getCreateDateTime());
                        return Long.compare(c2, c1);
                    })
                    .collect(Collectors.toList());

            boolean isParentAppRequest = false;
            Set<StudentDetail> students = new HashSet<>();
            if (StringUtils.isNotBlank(userId)) {
                User user = raikouSystem.loadUser(SafeConverter.toLong(userId));
                if (user != null && user.getUserType() == UserType.PARENT.getType() && ("200".equals(productId) || "201".equals(productId))) {
                    students = parentLoaderClient.loadParentStudentRefs(SafeConverter.toLong(userId))
                            .stream()
                            .map(StudentParentRef::getStudentId)
                            .collect(Collectors.toSet())
                            .stream()
                            .map(studentLoaderClient::loadStudentDetail)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    isParentAppRequest = true;
                }
            }

            boolean checkResult = false;
            for (ClientAppConfigCtl ctl : ctls) {
                if (isParentAppRequest) {
                    if (students.stream().anyMatch(s -> checkJsPatchConfig(ctl,
                            apkVer,
                            sysVer,
                            userId,
                            model,
                            s.getClazz() != null && s.getClazz().getEduSystem() != null && s.getClazz().getEduSystem().getKtwelve() != null ? s.getClazz().getEduSystem().getKtwelve().name() : null))) {
                        checkResult = true;
                    }
                } else {
                    if (checkJsPatchConfig(ctl, apkVer, sysVer, userId, model, ktwelve)) {
                        checkResult = true;
                    }
                }

                if (checkResult) {
                    ClientAppConfigCtlResponse response = ctl.getResponse();
                    resultMap.add("result", "success");
                    resultMap.add("patchUrl", response.getPatchUrl().matches("^http(s)?://.+$") ? response.getPatchUrl() : getCdnBaseUrlStaticSharedWithSep() + response.getPatchUrl());
                    resultMap.add("patchMD5", response.getPatchMD5());
                    resultMap.add("isOpen", SafeConverter.toBoolean(ctl.getIsOpen()));
                    return resultMap;
                }
            }
            resultMap.add("result", "success");
            return resultMap;
        } catch (Exception e) {
            logger.error("client app jspatch error.", e);
            resultMap.add("result", 500);
            resultMap.add("message", "系统错误!");
            return resultMap;
        }
    }

    //App record
    @RequestMapping(value = "/getapprecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getAppRecord() {
        String productId = getRequestString("productId");
        String region = getRequestString("region");
        String userId = getRequestString("user");

        MapMessage resultMap = new MapMessage();

        try {
            List<ClientAppConfigCtl> ctls = clientApplicationConfigServiceClient.getClientAppConfigCtlBuffer()
                    .loadPublished()
                    .stream()
                    .filter(e -> e.getProductId().equals(productId))
                    .filter(e -> ClientAppConfigType.RECORD.name().equals(e.getType()))
                    .sorted((o1, o2) -> {
                        long c1 = SafeConverter.toLong(o1.getCreateDateTime());
                        long c2 = SafeConverter.toLong(o2.getCreateDateTime());
                        return Long.compare(c2, c1);
                    })
                    .collect(Collectors.toList());

            boolean isParentAppRequest = false;
            Set<StudentDetail> students = new HashSet<>();
            if (StringUtils.isNotBlank(userId)) {
                User user = raikouSystem.loadUser(SafeConverter.toLong(userId));
                if (user != null && user.getUserType() == UserType.PARENT.getType() && ("200".equals(productId) || "201".equals(productId))) {
                    students = parentLoaderClient.loadParentStudentRefs(SafeConverter.toLong(userId))
                            .stream()
                            .map(StudentParentRef::getStudentId)
                            .collect(Collectors.toSet())
                            .stream()
                            .map(studentLoaderClient::loadStudentDetail)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    isParentAppRequest = true;
                }
            }

            String yzs = "close";

            boolean checkResult = false;
            for (ClientAppConfigCtl ctl : ctls) {
                if (isParentAppRequest) {
                    if (students.stream().anyMatch(s -> checkAppRecordConfig(ctl, s.getStudentSchoolRegionCode() != null ? s.getStudentSchoolRegionCode().toString() : null, userId))) {
                        checkResult = true;
                    }
                } else {
                    if (checkAppRecordConfig(ctl, region, userId)) {
                        checkResult = true;
                    }
                }

                if (checkResult) {
                    ClientAppConfigCtlResponse response = ctl.getResponse();
                    resultMap.add("result", "success");
                    resultMap.add("record_mode", response.getRecordMode());

                    // yzs默认为关
                    if (StringUtils.isNotEmpty(response.getYzs())) {
                        yzs = response.getYzs();
                    }

                    resultMap.add("yzs", yzs);

                    return resultMap;
                }
            }

            resultMap.add("result", "success");
            resultMap.add("record_mode", "normal");
            resultMap.add("yzs", yzs);
            return resultMap;
        } catch (Exception e) {
            logger.error("client app getrecord error.", e);
            resultMap.add("result", 500);
            resultMap.add("message", "系统错误!");
            return resultMap;
        }

    }

    @RequestMapping(value = "getappdownload.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getAppDownload() {
        String productId = getRequestString("productId");
        String apkVer = getRequestString("apkVer");
        String region = getRequestString("region");

        MapMessage resultMap = new MapMessage();

        try {
            List<ClientAppConfigCtl> ctls = clientApplicationConfigServiceClient.getClientAppConfigCtlBuffer()
                    .loadPublished()
                    .stream()
                    .filter(e -> (e.getProductId().equals(productId) && ClientAppConfigType.APP_DOWNLOAD.name().equals(e.getType())))
                    .sorted((o1, o2) -> {
                        long c1 = SafeConverter.toLong(o1.getCreateDateTime());
                        long c2 = SafeConverter.toLong(o2.getCreateDateTime());
                        return Long.compare(c2, c1);
                    })
                    .collect(Collectors.toList());

            for (ClientAppConfigCtl ctl : ctls) {
                //家长app的region是所有孩子的region用","分隔的字符串
                String[] regionSplit = region.split(",");
                List<String> regionList = new ArrayList<>();
                Collections.addAll(regionList, regionSplit);
                if (regionList.stream().anyMatch(e -> checkAppAutoDownLoadConfig(ctl, apkVer, e))) {
                    ClientAppConfigCtlResponse response = ctl.getResponse();
                    resultMap.add("result", "success");
                    resultMap.put("apkVer", response.getApkVer());
                    resultMap.put("apkSize", response.getApkSize());
                    resultMap.put("apkUrl", response.getPatchUrl());
                    resultMap.put("apkMD5", response.getPatchMD5());
                    return resultMap;
                }
            }
            resultMap.add("result", "success");
            return resultMap;
        } catch (Exception e) {
            logger.error("client app auto download error.", e);
            resultMap.add("result", 500);
            resultMap.add("message", "系统错误!");
            return resultMap;
        }
    }

    // Resource, 学生APP资源更新
    @RequestMapping(value = "/getappresource.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getAppResource() {
        String productId = getRequestString("productId");
        String apkVer = getRequestString("apkVer");
        String sysVer = getRequestString("sysVer");
        String userId = getRequestString("user"); // 学生ID
        String model = getRequestString("model");
        MapMessage resultMap = new MapMessage();
        try {
            List<ClientAppConfigCtl> ctls = clientApplicationConfigServiceClient.getClientAppConfigCtlBuffer()
                    .loadPublished()
                    .stream()
                    .filter(t -> ClientAppConfigType.APP_RESOURCE.name().equals(t.getType()))
                    .filter(e -> productId.equals(e.getProductId()))
                    .sorted(Comparator.comparing(ClientAppConfigCtl::getCreateDateTime).reversed())
                    .collect(Collectors.toList());

            StudentDetail student = studentLoaderClient.loadStudentDetail(SafeConverter.toLong(userId));
            // 传来无效的学生Id，直接Pass
            if (student == null) {
                resultMap.add("result", "success");
                return resultMap;
            }
            String ktwelve = (student.getClazz() == null || student.getClazz().getEduSystem() == null && student.getClazz().getEduSystem().getKtwelve() == null) ?
                    null : student.getClazz().getEduSystem().getKtwelve().name();
            String region = student.getStudentSchoolRegionCode() == null ? null : student.getStudentSchoolRegionCode().toString();

            for (ClientAppConfigCtl ctl : ctls) {
                if (checkAppResourceConfig(ctl, apkVer, sysVer, model, ktwelve, userId, region)) {
                    ClientAppConfigCtlResponse response = ctl.getResponse();
                    resultMap.add("result", "success");
                    resultMap.put("apkVer", response.getApkVer());
                    resultMap.put("apkSize", response.getApkSize());
                    resultMap.put("apkUrl", response.getPatchUrl());
                    resultMap.put("apkMD5", response.getPatchMD5());
                    resultMap.add("isOpen", SafeConverter.toBoolean(ctl.getIsOpen()));
                    return resultMap;
                }
            }
            resultMap.add("result", "success");
            return resultMap;
        } catch (Exception e) {
            logger.error("client app update resource error.", e);
            resultMap.add("result", 500);
            resultMap.add("message", "系统错误!");
            return resultMap;
        }
    }

    // Resource 资源动态配置
    @RequestMapping(value = "/getdynamicresource.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getDynamicResource() {
        String productId = getRequestString("productId");
        String apkVer = getRequestString("apkVer");
        String sysVer = getRequestString("sysVer");
        String userId = getRequestString("user");
        String model = getRequestString("model");
        MapMessage resultMap = new MapMessage();
        try {
            List<ClientAppConfigCtl> dynamicCtls = clientApplicationConfigServiceClient.getClientAppConfigCtlBuffer()
                    .loadPublished()
                    .stream()
                    .filter(t -> ClientAppConfigType.DYNAMIC_APP_RESOURCE.name().equals(t.getType()))
                    .filter(e -> productId.equals(e.getProductId()))
                    .sorted(Comparator.comparing(ClientAppConfigCtl::getCreateDateTime).reversed())
                    .collect(Collectors.toList());
            List<Map<String, Object>> dynamicResources = new ArrayList<>();
            for (ClientAppConfigCtl ctl : dynamicCtls) {
                if (checkDynamicResourceConfig(ctl, apkVer, sysVer, model, userId)) {
                    ClientAppConfigCtlResponse response = ctl.getResponse();
                    Map<String, Object> resourceMapper = new HashMap<>();
                    resourceMapper.put("resourceId", ctl.getId());
                    resourceMapper.put("resourceType", response.getApkVer());
                    resourceMapper.put("resourceSize", response.getApkSize());
                    resourceMapper.put("resourceUrl", response.getPatchUrl());
                    resourceMapper.put("resourceMD5", response.getPatchMD5());
                    resourceMapper.put("isOpen", SafeConverter.toBoolean(ctl.getIsOpen()));
                    dynamicResources.add(resourceMapper);
                }
            }
            resultMap.add("result", "success");
            resultMap.add("resources", dynamicResources);
            return resultMap;
        } catch (Exception e) {
            logger.error("client app get dynamic resource error.", e);
            resultMap.add("result", 500);
            resultMap.add("message", "系统错误");
            return resultMap;
        }
    }

    //静默下载region校验
    private boolean checkAppAutoDownLoadConfig(ClientAppConfigCtl config, String apkVer, String region) {
        //比较客户端版本
        String configApkVer = config.getApkVer();
        if (!checkVersion(configApkVer, apkVer)) {
            return false;
        }

        String configRegionCode = config.getRegion();
        return checkUpgradeRegion(configRegionCode, region);
    }

    private boolean checkAppRecordConfig(ClientAppConfigCtl config, String region, String userId) {
        // 区编码
        String configRegionCode = config.getRegion();
        if (!checkUpgradeRegion(configRegionCode, region)) {
            return false;
        }

        //比较用户id
        String configUserId = config.getUser();
        return checkNumber(configUserId, userId);

    }

    private boolean checkJsPatchConfig(ClientAppConfigCtl config,
                                       String apkVer,
                                       String sysVer,
                                       String userId,
                                       String model,
                                       String ktwelve) {
        //比较客户端版本
        String configApkVer = config.getApkVer();
        if (!checkVersion(configApkVer, apkVer)) {
            return false;
        }

        //比较系统版本号
        String configSysVer = config.getSysVer();
        if (!checkVersion(configSysVer, sysVer)) {
            return false;
        }

        //比较用户id
        String configUserId = config.getUser();
        if (!checkNumber(configUserId, userId)) {
            return false;
        }

        //比较手机型号
        String configModel = config.getModel();
        if (!checkString(configModel, model)) {
            return false;
        }

        //比较中小学
        String configKtwelve = config.getKtwelve();
        if (!checkKtwelve(configKtwelve, ktwelve)) {
            return false;
        }

        return true;
    }

    // 校验APP资源更新
    private boolean checkAppResourceConfig(ClientAppConfigCtl config,
                                           String apkVer,
                                           String sysVer,
                                           String model,
                                           String ktwelve,
                                           String userId,
                                           String regionCode) {

        //比较客户端版本
        String configApkVer = config.getApkVer();
        if (!checkVersion(configApkVer, apkVer)) {
            return false;
        }

        //比较系统版本号
        String configSysVer = config.getSysVer();
        if (!checkVersion(configSysVer, sysVer)) {
            return false;
        }

        //比较手机型号
        String configModel = config.getModel();
        if (!checkString(configModel, model)) {
            return false;
        }

        //比较中小学
        String configKtwelve = config.getKtwelve();
        if (!checkKtwelve(configKtwelve, ktwelve)) {
            return false;
        }

        //比较用户id
        String configUserId = config.getUser();
        if (!checkNumber(configUserId, userId)) {
            return false;
        }

        // 区编码
        String configRegionCode = config.getRegion();
        if (!checkUpgradeRegion(configRegionCode, regionCode)) {
            return false;
        }

        return true;
    }

    // 校验动态资源资源配置
    private boolean checkDynamicResourceConfig(ClientAppConfigCtl config,
                                               String apkVer,
                                               String sysVer,
                                               String model,
                                               String userId) {

        //比较客户端版本
        String configApkVer = config.getApkVer();
        if (!checkVersion(configApkVer, apkVer)) {
            return false;
        }

        //比较系统版本号
        String configSysVer = config.getSysVer();
        if (!checkVersion(configSysVer, sysVer)) {
            return false;
        }

        //比较手机型号
        String configModel = config.getModel();
        if (!checkString(configModel, model)) {
            return false;
        }

        //比较用户id
        String configUserId = config.getUser();
        if (!checkNumber(configUserId, userId)) {
            return false;
        }

        return true;
    }

    // 比较请求参数里面内容和配置信息里面的条件是否一致，如果一致返回TRUE，否则返回FALSE
    private boolean checkUpgradeConfig(ClientAppUpgradeCtl config,
                                       String reqProductId,
                                       String reqApkVersion,
                                       String reqAndroidVerCode,
                                       String reqSdkVersion,
                                       String reqSysVersion,
                                       String reqChannel,
                                       String reqRegionCode,
                                       String reqSchool,
                                       String reqClazz,
                                       String reqClazzLevel,
                                       String reqUserType,
                                       String reqUser,
                                       String reqImei,
                                       String reqBrand,
                                       String reqModel,
                                       String reqMobile,
                                       String reqKtwelve,
                                       String reqAccountStatus) {

        // 时间段
        String configTime = config.getTime();
        if (!checkUpgradeTime(configTime, System.currentTimeMillis())) {
            return false;
        }

        // 比较客户端版本号
        String configApkVersion = config.getApkVer();
        if (!checkVersion(configApkVersion, reqApkVersion)) {
            return false;
        }

        // 比较安卓版本标志
        String configAndroidVerCode = config.getAndroidVerCode();
        if (!checkNumber(configAndroidVerCode, reqAndroidVerCode)) {
            return false;
        }

        // 渠道
        String configChannel = config.getChannel();
        if (!checkString(configChannel, reqChannel)) {
            return false;
        }

        // 安卓SDK版本
        // FIXME 这里恶心了，安卓用的是LONG型的，IOS用的是a.b.c格式
        // FIXME 按照PRODUCT ID写死吧，Android 100， IOS 101
        String configSdkVersion = config.getSdkVer();
        if (reqProductId.equals("101") || reqProductId.equals("201")) {
            if (!checkVersion(configSdkVersion, reqSdkVersion)) {
                return false;
            }
        } else {
            if (!checkNumber(configSdkVersion, reqSdkVersion)) {
                return false;
            }
        }

        // 系统版本
        String configSysVer = config.getSysVer();
        if (!checkVersion(configSysVer, reqSysVersion)) {
            return false;
        }

        // 手机厂商
        String configBrand = config.getBrand();
        if (!checkString(configBrand, reqBrand)) {
            return false;
        }

        // 手机型号
        String configModel = config.getModel();
        if (!checkString(configModel, reqModel)) {
            return false;
        }

        // 区编码
        String configRegionCode = config.getRegion();
        if (!checkUpgradeRegion(configRegionCode, reqRegionCode)) {
            return false;
        }

        // 学科
        //   String configSubject = (String) config.getSubject();
        //   if (!checkString(configSubject, reqSubject)) {
        //      return false;
        //  }

        // 学校
        String configSchool = config.getSchool();
        if (!checkNumber(configSchool, reqSchool)) {
            return false;
        }

        // 年级
        String configClazzLevel = config.getClazzLevel();
        if (!checkNumber(configClazzLevel, reqClazzLevel)) {
            return false;
        }

        // 班级
        String configClazz = config.getClazz();
        if (!checkNumber(configClazz, reqClazz)) {
            return false;
        }

        // 类型
        String configUserType = config.getUserType();
        if (!checkString(configUserType, reqUserType)) {
            return false;
        }

        // 学号
        String configUser = config.getUser();
        if (!checkNumber(configUser, reqUser)) {
            return false;
        }

        // 手机
        String configMobile = config.getMobile();
        if (!checkString(configMobile, reqMobile)) {
            return false;
        }

        // 手机串号
        String configImei = config.getImei();
        if (!checkString(configImei, reqImei)) {
            return false;
        }

        // 升级百分比
        String configRate = config.getRate();
        ClientAppUpgradeCtlResponse response = config.getResponse();
        if (!response.getProductId().equals("") && !response.getApkVer().equals("")) {
            String targetProductId = String.valueOf(response.getProductId());
            String targetApkVer = String.valueOf(response.getApkVer());
            if (!checkRate(configRate, targetProductId, targetApkVer)) {
                return false;
            }
        }

        // 数量
        String configCount = config.getCount();
        if (!response.getProductId().equals("") && !response.getApkVer().equals("")) {
            String targetProductId = String.valueOf(response.getProductId());
            String targetApkVer = String.valueOf(response.getApkVer());
            if (!checkCount(configCount, targetProductId, targetApkVer)) {
                return false;
            }
        }
        //中小学
        if (!checkKtwelve(config.getKtwelve(), reqKtwelve)) {
            return false;
        }

        //账号异常
        if (!checkAccountStatus(config.getAccountStatus(), reqAccountStatus)) {
            return false;
        }

        return true;
    }

    // 数值型配置比较，支持 =, != > < >= <= :
    private boolean checkNumber(String configValue, String reqValue) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        // 如果请求参数没有，直接False
// FIXME: 这里是原来的逻辑，reqValue.trim()不是long的话，返回false
//        if (StringUtils.isBlank(reqValue) || !NumberUtil.isLong(reqValue.trim())) {
//            return false;
//        }
        if (StringUtils.isBlank(reqValue)) {
            return false;
        }

        long reqIntValue;
        try {
            reqIntValue = Long.parseLong(reqValue.trim());
        } catch (Exception ex) {
            // 不是long，返回false
            return false;
        }

        String[] configVersionList = configValue.trim().split("&");
        boolean checkResult = false;
        for (String configVer : configVersionList) {
            if (StringUtils.isBlank(configVer)) {
                continue;
            }
            if (configVer.startsWith("!=")) {
                long configIntValue = Long.parseLong(configVer.substring(2).trim());
                if (reqIntValue == configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("<=")) {
                long configIntValue = Long.parseLong(configVer.substring(2).trim());
                if (reqIntValue > configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith(">=")) {
                long configIntValue = Long.parseLong(configVer.substring(2).trim());
                if (reqIntValue < configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("=")) {
                long configIntValue = Long.parseLong(configVer.substring(1).trim());
                if (reqIntValue == configIntValue) {
                    checkResult = true;
                    break;
                }
            } else if (configVer.startsWith(">")) {
                long configIntValue = Long.parseLong(configVer.substring(1).trim());
                if (reqIntValue <= configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("<")) {
                long configIntValue = Long.parseLong(configVer.substring(1).trim());
                if (reqIntValue >= configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.indexOf(":") > 0) {
                int index = configVer.indexOf(":");
                long startValue = Long.parseLong(configVer.substring(0, index).trim());
                long endValue = Long.parseLong(configVer.substring(index + 1).trim());
                if (reqIntValue < startValue || reqIntValue > endValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else {
                throw new RuntimeException("Unsupported number config:" + configVer);
            }
        }

        return checkResult;
    }

    // 字符串配置比较，支持 =, !=
    private boolean checkString(String configValue, String reqValue) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        // 如果请求参数没有，直接False
        if (StringUtils.isBlank(reqValue)) {
            return false;
        }

        String[] configValueList = configValue.trim().split("&");
        boolean checkResult = false;
        for (String configVal : configValueList) {
            if (StringUtils.isBlank(configVal)) {
                continue;
            }
            if (configVal.startsWith("!=")) {
                String compareValue = configVal.substring(2).trim();
                if (compareValue.equals(reqValue.trim())) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVal.startsWith("=")) {
                String compareValue = configVal.substring(1).trim();
                if (compareValue.equals(reqValue.trim())) {
                    checkResult = true;
                    break;
                }
            }

        }
        return checkResult;
    }

    // 时间段比较，支持 >=A, <=B, A#B, A#B,C 等几种格式
    private boolean checkTime(String configValue, Long reqTime) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        // 首先对时间段按照 , 分割
        String[] configTimeList = configValue.trim().split(",");
        for (String configTime : configTimeList) {
            if (configTime.startsWith(">=")) {
                String startTime = configValue.substring(2).trim();
                Long compareTime = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (reqTime >= compareTime) {
                    return true;
                }
            } else if (configTime.startsWith("<=")) {
                String endTime = configValue.substring(2).trim();
                Long compareTime = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (reqTime <= compareTime) {
                    return true;
                }
            } else if (configTime.indexOf("#") > 0) {
                String startTime = configValue.substring(0, configTime.indexOf("#")).trim();
                Long compareStartTime = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                String endTime = configTime.substring(configTime.indexOf("#") + 1).trim();
                Long compareEndTime = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (compareStartTime <= reqTime && compareEndTime >= reqTime) {
                    return true;
                }
            } else {
                throw new RuntimeException("Unsupported time cconfig value:" + configValue);
            }
        }

        return false;
    }

    // 时间段比较，支持 >=A, <=B, A#B, A#B,C 等几种格式
    private boolean checkUpgradeTime(String configValue, Long reqTime) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        // 首先对时间段按照 , 分割
        String[] configTimeList = configValue.trim().split("&");
        if (configTimeList.length <= 1) {
            configTimeList = configValue.trim().split(",");
        }
        for (String configTime : configTimeList) {
            if (configTime.startsWith(">=")) {
                String startTime = configTime.substring(2).trim();
                Long compareTime = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (reqTime >= compareTime) {
                    return true;
                }
            } else if (configTime.startsWith("<=")) {
                String endTime = configTime.substring(2).trim();
                Long compareTime = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (reqTime <= compareTime) {
                    return true;
                }
            } else if (configTime.indexOf("#") > 0) {
                String startTime = configTime.substring(0, configTime.indexOf("#")).trim();
                Long compareStartTime = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                String endTime = configTime.substring(configTime.indexOf("#") + 1).trim();
                Long compareEndTime = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (compareStartTime <= reqTime && compareEndTime >= reqTime) {
                    return true;
                }
            } else {
                throw new RuntimeException("Unsupported time cconfig value:" + configTime);
            }
        }

        return false;
    }

    private boolean checkRegion(String configValue, String reqValue) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }
        if (StringUtils.isBlank(reqValue)) {
            return false;
        }
        int rcode;
        try {
            rcode = Integer.parseInt(reqValue);
        } catch (Exception ex) {
            // 不是int，返回false
            return false;
        }

        // 请求参数的Region
        ExRegion reqRegion = raikouSystem.loadRegion(rcode);
        if (reqRegion == null || reqRegion.fetchRegionType() != RegionType.COUNTY) {
            return false;
        }

        // 首先对区域按照 , 分割
        String[] configRegionList = configValue.trim().split(",");
        boolean checkResult = false;
        for (String configRegion : configRegionList) {
            if (StringUtils.isBlank(configRegion)) {
                continue;
            }
            String regionCode = configRegion.substring(configRegion.indexOf("=") + 1).trim();
            ExRegion compareRegion = raikouSystem.loadRegion(Integer.parseInt(regionCode));
            if (compareRegion == null) {
                continue;
            }
            if (configRegion.startsWith("=")) {
                if (compareRegion.fetchRegionType() == RegionType.COUNTY) {
                    if (reqRegion.getCountyCode() == compareRegion.getCountyCode()) {
                        checkResult = true;
                        break;
                    }
                } else if (compareRegion.fetchRegionType() == RegionType.CITY) {
                    if (reqRegion.getCityCode() == compareRegion.getCityCode()) {
                        checkResult = true;
                        break;
                    }
                } else {
                    if (reqRegion.getProvinceCode() == compareRegion.getProvinceCode()) {
                        checkResult = true;
                        break;
                    }
                }
            } else if (configRegion.startsWith("!=")) {
                if (compareRegion.fetchRegionType() == RegionType.COUNTY) {
                    if (reqRegion.getCountyCode() == compareRegion.getCountyCode()) {
                        checkResult = false;
                        break;
                    }
                } else if (compareRegion.fetchRegionType() == RegionType.CITY) {
                    if (reqRegion.getCityCode() == compareRegion.getCityCode()) {
                        checkResult = false;
                        break;
                    }
                } else if (reqRegion.getProvinceCode() == compareRegion.getProvinceCode()) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else {
                throw new RuntimeException("Unsupported region cconfig value:" + configValue);
            }
        }
        return checkResult;
    }

    private boolean checkUpgradeRegion(String configValue, String reqValue) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }
        if (StringUtils.isBlank(reqValue)) {
            return false;
        }
        int rcode;
        try {
            rcode = Integer.parseInt(reqValue);
        } catch (Exception ex) {
            // 不是int，返回false
            return false;
        }

        // 请求参数的Region
        ExRegion reqRegion = raikouSystem.loadRegion(rcode);
        if (reqRegion == null || reqRegion.fetchRegionType() != RegionType.COUNTY) {
            return false;
        }

        // 首先对区域按照 , 分割
        String[] configRegionList = configValue.trim().split("&");
        if (configRegionList.length <= 1) {
            configRegionList = configValue.trim().split(",");
        }
        boolean checkResult = false;
        for (String configRegion : configRegionList) {
            if (StringUtils.isBlank(configRegion)) {
                continue;
            }
            String regionCode;
            if (configRegion.startsWith("=")) {
                regionCode = configRegion.substring(configRegion.indexOf("=") + 1).trim();
            } else if (configRegion.startsWith("!=")) {
                regionCode = configRegion.substring(configRegion.indexOf("!=") + 2).trim();
            } else {
                throw new RuntimeException("Unsupported region cconfig value:" + configRegion);
            }
            ExRegion compareRegion = raikouSystem.loadRegion(Integer.parseInt(regionCode));
            if (compareRegion == null) {
                continue;
            }
            if (configRegion.startsWith("=")) {
                if (compareRegion.fetchRegionType() == RegionType.COUNTY) {
                    if (reqRegion.getCountyCode() == compareRegion.getCountyCode()) {
                        checkResult = true;
                        break;
                    }
                } else if (compareRegion.fetchRegionType() == RegionType.CITY) {
                    if (reqRegion.getCityCode() == compareRegion.getCityCode()) {
                        checkResult = true;
                        break;
                    }
                } else {
                    if (reqRegion.getProvinceCode() == compareRegion.getProvinceCode()) {
                        checkResult = true;
                        break;
                    }
                }
            } else if (configRegion.startsWith("!=")) {
                if (compareRegion.fetchRegionType() == RegionType.COUNTY) {
                    if (reqRegion.getCountyCode() == compareRegion.getCountyCode()) {
                        checkResult = false;
                        break;
                    }
                } else if (compareRegion.fetchRegionType() == RegionType.CITY) {
                    if (reqRegion.getCityCode() == compareRegion.getCityCode()) {
                        checkResult = false;
                        break;
                    }
                } else if (reqRegion.getProvinceCode() == compareRegion.getProvinceCode()) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else {
                throw new RuntimeException("Unsupported region cconfig value:" + configRegion);
            }
        }
        return checkResult;
    }

    // 升级百分比
    private boolean checkRate(String configValue, String productId, String apkVersion) {
        // 如果配置里面不需要检查，直接True
// FIXME: 这里是原来的逻辑，如果configValue不是int，返回true
//        if (StringUtils.isBlank(configValue) || !NumberUtil.isInteger(configValue)) {
//            return true;
//        }
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        int configIntValue = 0;
        try {
            configIntValue = Integer.parseInt(configValue);
        } catch (Exception ex) {
            // 不是int，返回true
            return true;
        }

        String totalCountCacheKey = CacheKeyGenerator.generateCacheKey(ClientController.class,
                new String[]{"pid", "av", "type"},
                new Object[]{productId, apkVersion, "rt"});
        String upgradeCountCacheKey = CacheKeyGenerator.generateCacheKey(ClientController.class,
                new String[]{"pid", "av", "type"},
                new Object[]{productId, apkVersion, "ru"});
        CacheObject<Long> totalCountObject = washingtonCacheSystem.CBS.unflushable.get(totalCountCacheKey);
        Long totalCount = 0L;
        if (totalCountObject != null && totalCountObject.getValue() != null) {
            totalCount = AlpsConversionService.getInstance().convert(totalCountObject.getValue(), Long.class);
        }
        washingtonCacheSystem.CBS.unflushable.incr(totalCountCacheKey, 1, 0, 21 * 24 * 3600);

        CacheObject<Long> upgradeCountObject = washingtonCacheSystem.CBS.unflushable.get(upgradeCountCacheKey);
        Long upgradeCount = 0L;
        if (upgradeCountObject != null && upgradeCountObject.getValue() != null) {
            upgradeCount = AlpsConversionService.getInstance().convert(upgradeCountObject.getValue(), Long.class);
        }

        long shouldUpgradeConfig = totalCount * configIntValue / 1000;
        if (upgradeCount < shouldUpgradeConfig) {
            washingtonCacheSystem.CBS.unflushable.incr(upgradeCountCacheKey, 1, 0, 21 * 24 * 3600);
            return true;
        }

        return false;
    }

    // 升级数量
    private boolean checkCount(String configValue, String productId, String apkVersion) {
        // 如果配置里面不需要检查，直接True
// FIXME: 这是原来的逻辑，configValue不是整数的话，返回了true
//        if (StringUtils.isBlank(configValue) || !NumberUtil.isInteger(configValue)) {
//            return true;
//        }

        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        int configIntValue;
        try {
            configIntValue = Integer.parseInt(configValue);
        } catch (Exception ex) {
            // 不是整数，返回了true
            return true;
        }

        String upgradeCountCacheKey = getApkVersionCountCacheKey(productId, apkVersion);

        CacheObject<Integer> upgradeCountObject = washingtonCacheSystem.CBS.unflushable.get(upgradeCountCacheKey);

        if (upgradeCountObject == null || upgradeCountObject.getValue() == null) {   // 一个升级了的都没有
            return true;
        }

        int upgradeCount = upgradeCountObject.getValue();
        return upgradeCount < configIntValue;
    }

    private boolean checkKtwelve(String configKtwelve, String reqKtwelve) {
        //没配置，直接通过
        if (StringUtils.isBlank(configKtwelve)) {
            return true;
        }
        String[] configKtwelveList = configKtwelve.trim().split("&");
        boolean checkResult = false;

        //配置了。请求没传，把请求视为小学端的。此时只要配置的是小学端就算通过
        if (StringUtils.isBlank(reqKtwelve)) {
            for (String configVal : configKtwelveList) {
                if (configVal.equals(Ktwelve.PRIMARY_SCHOOL.name())) {
                    checkResult = true;
                    break;
                }
            }
        } else {
            //配置了。请求也传了。完全匹配才通过
            for (String configVal : configKtwelveList) {
                if (configVal.equalsIgnoreCase(reqKtwelve)) {
                    checkResult = true;
                    break;
                }
            }
        }

        return checkResult;
    }

    private boolean checkAccountStatus(String configAccountStatus, String reqAccountStatus) {
        if (StringUtils.isBlank(configAccountStatus)) {
            return true;
        }

        if (StringUtils.isBlank(reqAccountStatus)) {
            return false;
        }

        boolean checkResult = false;
        //账户异常配置的是全部或者请求的与配置的相同就返回true
        if ("ALL".equals(configAccountStatus) || configAccountStatus.equalsIgnoreCase(reqAccountStatus)) {
            checkResult = true;
        }

        return checkResult;
    }

    private boolean checkVersion(String configVersion, String reqVersion) {
        // 如果配置里面不需要检查Version，直接True
        if (StringUtils.isBlank(configVersion)) {
            return true;
        }
        // 如果请求参数没有Version，直接False
        if (StringUtils.isBlank(reqVersion)) {
            return false;
        }

        String[] configVersionList = configVersion.trim().split("&");
        boolean checkResult = false;
        for (String configVer : configVersionList) {
            if (StringUtils.isBlank(configVer)) {
                continue;
            }
            if (configVer.startsWith("!=")) {
                String compareVersion = configVer.substring(2);
                if (compareVersion(reqVersion, compareVersion) == 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith(">=")) {
                String compareVersion = configVer.substring(2);
                if (compareVersion(reqVersion, compareVersion) < 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("<=")) {
                String compareVersion = configVer.substring(2);
                if (compareVersion(reqVersion, compareVersion) > 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("=")) {
                String compareVersion = configVer.substring(1);
                if (compareVersion(reqVersion, compareVersion) == 0) {
                    checkResult = true;
                    break;
                }
            } else if (configVer.startsWith(">")) {
                String compareVersion = configVer.substring(1);
                if (compareVersion(reqVersion, compareVersion) <= 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("<")) {
                String compareVersion = configVer.substring(1);
                if (compareVersion(reqVersion, compareVersion) >= 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.indexOf(":") > 0) {
                int index = configVersion.indexOf(":");
                String startVersion = configVer.substring(0, index).trim();
                String endVersion = configVer.substring(index + 1).trim();
                if (compareVersion(reqVersion, startVersion) < 0 || compareVersion(reqVersion, endVersion) > 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else {
                throw new RuntimeException("Unknown app version config:" + configVer);
            }

        }
        return checkResult;
    }

    // 版本号比较，支持2，3，4版本号比较
    private static int compareVersion(String ver1, String ver2) {
        String[] ver1List = ver1.trim().split("\\.");
        String[] ver2List = ver2.trim().split("\\.");

        if (ver1List.length < 2 || ver2List.length < 2) {
            throw new RuntimeException("app version is not correct!, ver1:" + ver1 + ", ver2:" + ver2);
        }

        int ver1First = Integer.parseInt(ver1List[0]);
        int ver1Second = Integer.parseInt(ver1List[1]);
        int ver1Third = ver1List.length >= 3 ? Integer.parseInt(ver1List[2]) : -1;
        int ver1Fourth = ver1List.length >= 4 ? Integer.parseInt(ver1List[3]) : -1;
        int ver2First = Integer.parseInt(ver2List[0]);
        int ver2Second = Integer.parseInt(ver2List[1]);
        int ver2Third = ver2List.length >= 3 ? Integer.parseInt(ver2List[2]) : -1;
        int ver2Fourth = ver2List.length >= 4 ? Integer.parseInt(ver2List[3]) : -1;

        if (ver1First == ver2First && ver1Second == ver2Second && ver1Third == ver2Third && ver1Fourth == ver2Fourth) {
            return 0;
        }

        if (ver1First > ver2First) {
            return 1;
        } else if (ver1First < ver2First) {
            return -1;
        } else if (ver1Second > ver2Second) {
            return 1;
        } else if (ver1Second < ver2Second) {
            return -1;
        } else if (ver1Third > ver2Third) {
            return 1;
        } else if (ver1Third < ver2Third) {
            return -1;
        } else if (ver1Fourth > ver2Fourth) {
            return 1;
        }
        return -1;
    }

    // 关闭硬件加速的机器配置
    private String getCloseCrossWalkList(String productId) {
        String blockName = "new_crosswalk_close";

        if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "110")) {
            blockName = "crosswalk_close_middle_student";
        } else if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "310")) {
            blockName = "crosswalk_close_middle_teacher";
        }

        return internalGetCloseCrossWalkList(blockName);
    }

    // 关闭硬件加速的机器配置
    private String closeHardwareAccelerationList(String productId) {
        String blockName = "hardware_acceleration_close";
        if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "200")) {
            blockName = "hardware_acceleration_close_parent";
        } else if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "300")) {
            blockName = "hardware_acceleration_close_teacher";
        } else if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "110")) {
            blockName = "hardware_acceleration_close_middle_student";
        } else if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "310")) {
            blockName = "hardware_acceleration_close_middle_teacher";
        }

        String hardwareAccelerationConfig = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", blockName);
        if (StringUtils.isBlank(hardwareAccelerationConfig)) {
            return "";
        }
        hardwareAccelerationConfig = hardwareAccelerationConfig.replace("\r", "").replace("\n", "").replace("\t", "");
        List<String> configList = JsonUtils.fromJsonToList(hardwareAccelerationConfig, String.class);
        return StringUtils.join(configList, ",");
    }

    // 关闭crosswalk引擎的配置
    private String internalGetCloseCrossWalkList(String key) {
        String crosswalkConfig = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", key);
        if (StringUtils.isBlank(crosswalkConfig)) {
            return "";
        }
        crosswalkConfig = crosswalkConfig.replace("\r", "").replace("\n", "").replace("\t", "");
        List<String> configList = JsonUtils.fromJsonToList(crosswalkConfig, String.class);
        return StringUtils.join(configList, ",");
    }

    private String getApkVersionCountCacheKey(String productId, String apkVer) {
        return CacheKeyGenerator.generateCacheKey(ClientController.class,
                new String[]{"pid", "av", "type"},
                new Object[]{productId, apkVer, "uc"});
    }

    private void incUserApkVerCount(String imei, String productId, String apkVer) {
        String userApkVerCacheKey = CacheKeyGenerator.generateCacheKey(ClientController.class,
                new String[]{"uimei", "pid", "av"},
                new Object[]{imei, productId, apkVer});

        CacheObject<Integer> userApkVerCacheObject = washingtonCacheSystem.CBS.unflushable.get(userApkVerCacheKey);
        if (userApkVerCacheObject != null && userApkVerCacheObject.getValue() != null) {
            return;
        }

        washingtonCacheSystem.CBS.unflushable.set(userApkVerCacheKey, 21 * 24 * 3600, "1");

        String apkVerUpgradeCountCacheKey = getApkVersionCountCacheKey(productId, apkVer);

        washingtonCacheSystem.CBS.unflushable.incr(apkVerUpgradeCountCacheKey, 1, 1, 21 * 24 * 3600);
    }

    private ClientAppUpgradeCtl getUpdateConfigForParentApp(List<ClientAppUpgradeCtl> ctls) {
        String reqProductId = getRequestString(REQ_PRODUCT_ID);
        String reqApkVersion = getRequestString(REQ_APK_VERSION);
        String reqAndroidVerCode = getRequestString(REQ_ANDROID_VERCODE);
        String reqSdkVersion = getRequestString(REQ_SDK_VERSION);
        String reqSysVersion = getRequestString(REQ_SYS_VERSION);
        String reqChannel = getRequestString(REQ_CHANNEL);
        String reqRegionCode = getRequestString(REQ_REGION_CODE);
        String reqKtwelve = getRequestString(REQ_KTWELVE);
        String reqSchool = getRequestString(REQ_SCHOOL);
        String reqClazz = getRequestString(REQ_CLAZZ);
        String reqClazzLevel = getRequestString(REQ_CLAZZ_LEVEL);
        //这里客户端坑了。没有登录的时候这里就不传这个参数。所以家长的请求就默认给个2了。
        String reqUserType = getRequestParameter(REQ_USER_TYPE, "2");
        String reqUser = getRequestString(REQ_USER);
        String reqImei = getRequestString(REQ_IMEI);
        String reqBrand = getRequestString(REQ_BRAND);
        String reqModel = getRequestString(REQ_MODEL);
        String reqMobile = getRequestString(REQ_MOBILE);
        Set<StudentDetail> studentDetails = new HashSet<>();
        if (StringUtils.isNotBlank(reqUser) && NumberUtils.isDigits(reqUser)) {
            studentDetails = parentLoaderClient.loadParentStudentRefs(SafeConverter.toLong(reqUser))
                    .stream()
                    .map(StudentParentRef::getStudentId)
                    .collect(Collectors.toSet())
                    .stream()
                    .map(studentLoaderClient::loadStudentDetail)
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());
        }
        final Set<StudentDetail> students = studentDetails;
        //家长这里的判断逻辑会复杂一点
        //1.是否登录||没有孩子 。直接把reqUser扔进去判断，取优先级最高的返回升级
        //2.有孩子的。逐个去判断孩子是否命中升级策略
        ClientAppUpgradeCtl config;
        if (StringUtils.isBlank(reqUser) || CollectionUtils.isEmpty(students)) {
            config = ctls.stream().filter(ctl -> checkUpgradeConfig(ctl,
                    reqProductId,
                    reqApkVersion,
                    reqAndroidVerCode,
                    reqSdkVersion,
                    reqSysVersion,
                    reqChannel,
                    reqRegionCode,
                    reqSchool,
                    reqClazz,
                    reqClazzLevel,
                    reqUserType,
                    reqUser,
                    reqImei,
                    reqBrand,
                    reqModel,
                    reqMobile,
                    reqKtwelve,
                    "")).findFirst().orElse(null);
        } else {
            config = ctls.stream().filter(ctl -> students.stream().anyMatch(s -> checkUpgradeConfig(ctl,
                    reqProductId,
                    reqApkVersion,
                    reqAndroidVerCode,
                    reqSdkVersion,
                    reqSysVersion,
                    reqChannel,
                    s.getStudentSchoolRegionCode() != null ? s.getStudentSchoolRegionCode().toString() : null,
                    s.getClazz() != null && s.getClazz().getSchoolId() != null ? s.getClazz().getSchoolId().toString() : null,
                    s.getClazzId() != null ? s.getClazzId().toString() : null,
                    s.getClazz() != null ? s.getClazz().getClassLevel() : null,
                    reqUserType,
                    reqUser,
                    reqImei,
                    reqBrand,
                    reqModel,
                    reqMobile,
                    s.getClazz() != null && s.getClazz().getEduSystem() != null && s.getClazz().getEduSystem().getKtwelve() != null ? s.getClazz().getEduSystem().getKtwelve().name() : null,
                    ctl.getAccountStatus()))).findFirst().orElse(null);
        }
        return config;
    }

    private ClientAppUpgradeCtl getUpdateConfigForTeacherOrStudentApp(List<ClientAppUpgradeCtl> ctls) {
        String reqProductId = getRequestString(REQ_PRODUCT_ID);
        String reqApkVersion = getRequestString(REQ_APK_VERSION);
        String reqAndroidVerCode = getRequestString(REQ_ANDROID_VERCODE);
        String reqSdkVersion = getRequestString(REQ_SDK_VERSION);
        String reqSysVersion = getRequestString(REQ_SYS_VERSION);
        String reqChannel = getRequestString(REQ_CHANNEL);
        String reqRegionCode = getRequestString(REQ_REGION_CODE);
        String reqKtwelve = getRequestString(REQ_KTWELVE);
        String reqSchool = getRequestString(REQ_SCHOOL);
        String reqClazz = getRequestString(REQ_CLAZZ);
        String reqClazzLevel = getRequestString(REQ_CLAZZ_LEVEL);
        String reqUserType = getRequestString(REQ_USER_TYPE);
        String reqUser = getRequestString(REQ_USER);
        String reqImei = getRequestString(REQ_IMEI);
        String reqBrand = getRequestString(REQ_BRAND);
        String reqModel = getRequestString(REQ_MODEL);
        String reqMobile = getRequestString(REQ_MOBILE);
        //学生账号异常字段
        String reqAccountStatus = "";
        if (StringUtils.isNoneBlank(reqUser) && reqUserType.equals(String.valueOf(UserType.STUDENT.getType()))) {
            StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(SafeConverter.toLong(reqUser));
            if (studentExtAttribute != null && studentExtAttribute.getAccountStatus() != null) {
                reqAccountStatus = studentExtAttribute.getAccountStatus().name();
            }
        }
        final String reqStatus = reqAccountStatus;
        return ctls.stream().filter(ctl -> checkUpgradeConfig(ctl,
                reqProductId,
                reqApkVersion,
                reqAndroidVerCode,
                reqSdkVersion,
                reqSysVersion,
                reqChannel,
                reqRegionCode,
                reqSchool,
                reqClazz,
                reqClazzLevel,
                reqUserType,
                reqUser,
                reqImei,
                reqBrand,
                reqModel,
                reqMobile,
                reqKtwelve,
                reqStatus)).findFirst().orElse(null);
    }
}
