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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductRedirectType;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.common.Mode.STAGING;
import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType.APPS;

/**
 * app跳转通用逻辑
 *
 * @author peng.zhang.a
 * @since 16-7-14
 */

@Controller
@NoArgsConstructor
@RequestMapping("/app/redirect")
public class AppIndexRedirectController extends AbstractController {

    @Inject
    VendorLoaderClient vendorLoaderClient;
    @Inject
    FairylandLoaderClient fairylandLoaderClient;
    @Inject
    private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;
    @Inject
    private AsyncVendorServiceClient asyncVendorServiceClient;

    /**
     * 暂时只针对学生端 通过中间页面打开应用
     */
    @RequestMapping(value = "/openapp.vpage", method = RequestMethod.GET)
    public String openApp(Model model) {
        String downloadRedirectUrl = "redirect:/parentMobile/ucenter/upgrade.vpage";
        String resultPage = "open/redirectapp";
        String errorPage = "parentmobile/error";
        String appKey = getRequestString("appKey");
        String platform = getRequestString("platform");
        String productTypeStr = getRequestString("productType");
        String module = getRequestString("module"); // 跳入到应用的哪个模块，刘瑞体系适用
        String fwdUrl = getRequestString("fwdUrl");
        setUserOrderRefer();
        //传入连接直接打开连接
        if (!StringUtils.isBlank(fwdUrl)) {
            fwdUrl = wrapUpUrlVersion(fwdUrl);
            model.addAttribute("url", fwdUrl);
            model.addAttribute("redirectType", "URL");
            model.addAttribute("orientation", getRequestString("orientation"));
            model.addAttribute("browser", getRequestString("browser"));
            return resultPage;
        }
        FairylandProductType productType = StringUtils.isBlank(productTypeStr) ? APPS : FairylandProductType.parse(productTypeStr);
        StudentDetail studentDetail = getRequestStudentDetail();
        String version = getAppVersion();
        User curUser = currentUser();
        if (curUser != null && curUser.isParent() && VersionUtil.compareVersion(version, "1.6.5.0") < 0) {
            return downloadRedirectUrl;
        }

        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(platform) || studentDetail == null) {
            model.addAttribute("result", MapMessage.errorMessage("请求参数错误").setErrorCode("400"));
            return errorPage;
        }
        VendorApps vendorApps = vendorLoaderClient.loadVendor(appKey);
        FairylandProduct fairylandProduct = fairylandLoaderClient
                .loadFairylandProducts(FairyLandPlatform.of(platform), productType).stream()
                .filter(p -> p.getAppKey().equals(appKey))
                .findFirst()
                .orElse(null);
        if (vendorApps == null || fairylandProduct == null) {
            return errorPage;
        }

        String url = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
        if (StringUtils.isNotBlank(module)) url += ("&module=" + module); // TODO: 2017/2/14 这么不好，再改
        if (StringUtils.isNotBlank(getRequestString("refer"))) url += "&refer=" + getRequestString("refer");
        model.addAttribute("url", url);
        model.addAttribute("orientation", vendorApps.getOrientation());
        model.addAttribute("browser", vendorApps.getBrowser());
        model.addAttribute("appKey", appKey);
        model.addAttribute("productType", productType.name());
        return "open/redirectapp";
    }

    private String wrapUpUrlVersion(String fwdUrl) {
        String url = fwdUrl;
        int positionIndex;
        positionIndex = fwdUrl.indexOf((int) '#');
        String positionStr = null;
        if (positionIndex > 0) {
            positionStr = url.substring(positionIndex);
            url = url.substring(0, positionIndex);
        }

        int paramIndex = fwdUrl.indexOf((int) '?');
        String paramStr = null;
        if (paramIndex > 0) {
            paramStr = url.substring(paramIndex + 1);
            url = url.substring(0, paramIndex);
        }

        try {
            positionIndex = fwdUrl.indexOf((int) '.');
            if (positionIndex <= 0) {
                return fwdUrl;
            }
            url = cdnResourceVersionCollector.getVersionedUrlPath(url);

//            String cdnBaseUrl = getCdnBaseUrlWithSep();
//            if (StringUtils.isNotBlank(url)
//                    && StringUtils.isNotBlank(cdnBaseUrl)
//                    && !url.startsWith("http")
//                    && url.contains("html")) {
//                url = cdnBaseUrl + url;
//            }
        } catch (Exception e) {
            return fwdUrl;
        }
        if (StringUtils.isBlank(url)) {
            return fwdUrl;
        }

        if (StringUtils.isNotBlank(paramStr)) {
            url = url + (url.lastIndexOf((int) '?') > 0 ? "&" : "?");
            url = url + paramStr;
        }
        if (StringUtils.isNotBlank(positionStr)) {
            url = url + positionStr;
        }

        return url;
    }

    @RequestMapping(value = "/getredirectparas.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getRedirectParameter() {
        String appKey = getRequestString("appKey");
        String platform = getRequestString("platform");
        String productType = getRequestString("productType");

        FairylandProductType fairylandProductType = StringUtils.isBlank(productType) ? APPS : FairylandProductType.parse(productType);

        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(platform)) {
            return MapMessage.errorMessage("请求参数错误").setErrorCode("400");
        }

        VendorApps vendorApps = vendorLoaderClient.loadVendor(appKey);
        FairylandProduct fairylandProduct = fairylandLoaderClient
                .loadFairylandProducts(FairyLandPlatform.of(platform), fairylandProductType).stream()
                .filter(p -> p.getAppKey().equals(appKey))
                .findFirst()
                .orElse(null);

        if (vendorApps == null || fairylandProduct == null) {
            return MapMessage.errorMessage("未找到对应的产品");
        }
        Boolean flag = true;
        MapMessage message = new MapMessage();
        message.set("productName", fairylandProductType.name())
                .add("launchUrl", fairylandProduct.fetchRedirectUrl(RuntimeMode.current()))
                .add("orientation", vendorApps.getOrientation())
                .add("browser", vendorApps.getBrowser())
                .add("appKey", appKey)
                .add("success", flag);

        return message;
    }

    @Deprecated
    @RequestMapping(value = "/openurl.vpage", method = RequestMethod.GET)
    public String openurl() {
        String url = getRequestString("fwdUrl");
        url = wrapUpUrlVersion(url);
        setUserOrderRefer();
        return "redirect:" + url;
    }

    @RequestMapping(value = "/jump.vpage", method = RequestMethod.GET)
    public String jump(Model model) {
        long sid = getStudentId();
        OrderProductServiceType appKey = OrderProductServiceType.safeParse(getRequestString("appKey"));
        FairyLandPlatform platform = FairyLandPlatform.of(getRequestString("platform"));
        FairylandProductType productType = FairylandProductType.of(getRequestString("productType"));
        String module = getRequestString("module");
        String position = getRequestString("position");
        if (sid == 0 || appKey == Unknown || platform == null) {
            return errorPage(model, "请求参数错误");
        }
        FairylandProduct fairylandProduct = fairylandLoaderClient.loadFairylandProduct(platform, productType, appKey == null ? "" : appKey.name());
        if (fairylandProduct == null) {
            return errorPage(model, "找不到指定产品");
        }

        Map<String, Object> params = new HashMap<>();

        //阿分新版本上线之后，修改跳转类型，然后可以删除代码　20180201
        FairylandProductRedirectType redirectType = fairylandProduct.getRedirectType();
        if (RuntimeMode.le(Mode.STAGING)) {
            if (appKey == AfentiExam || appKey == AfentiMath || appKey == AfentiChinese) {
                redirectType = FairylandProductRedirectType.THIRD_APP;
            }
        }

        if (redirectType == FairylandProductRedirectType.THIRD_APP) {
            MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                    .registerVendorAppUserRef(appKey.name(), sid)
                    .getUninterruptibly();
            if (!message.isSuccess() || null == message.get("ref")) {
                return errorPage(model, "获取第三方登陆信息错误");
            }
            params.put("session_key", ((VendorAppsUserRef) message.get("ref")).getSessionKey());
        } else if (fairylandProduct.getRedirectType() == FairylandProductRedirectType.SELF_APP) {
            params.put("version", getAppVersion());
            params.put("sid", sid);
        }

        if (StringUtils.isNotBlank(module)) params.put("module", module);

        String url = RuntimeMode.current() == STAGING ? fairylandProduct.getStagingLaunchUrl() : fairylandProduct.getLaunchUrl();
        if (StringUtils.isEmpty(url)) {
            return errorPage(model, "跳转连接错误");
        }
        // 处理新afenti英语跳转路径
//        url = getAfentiExamUrl(url, appKey, sid, params);
        String refer = getRequestString("refer");
        if (StringUtils.isNotBlank(refer)) {
            params.put("refer", refer);
        }

        // 成长世界任务来源
        params.put("from_gw_task", getRequestBool("from_gw_task", false));
        // 实验版本，任务页领取功能测试。
        params.put("from_reward", getRequestBool("from_reward", false));

        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isNotBlank(dubbingId)) params.put("dubbingId", getRequestString("dubbingId"));
        boolean trial = getRequestBool("trial", false);
        if (trial) params.put("trial", true);

        url = UrlUtils.buildUrlQuery(url, params);
        if (!StringUtils.isBlank(position)) url += "#" + position;
        setUserOrderRefer();
        return "redirect:" + url;
    }

//    private String getAfentiExamUrl(String url, OrderProductServiceType appKey, long sid, Map<String, Object> params) {
//        if (appKey != AfentiExam) {
//            return url;
//        }
//        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
//        if (studentDetail == null) {
//            return url;
//        }
//        boolean openNewUrl = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "AfentiEngNew", "isopen");
//        if (openNewUrl) {
//            MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
//                    .registerVendorAppUserRef(appKey.name(), sid)
//                    .getUninterruptibly();
//            if (!message.isSuccess() || null == message.get("ref")) {
//                return url;
//            }
//            params.put("session_key", ((VendorAppsUserRef) message.get("ref")).getSessionKey());
//            if (RuntimeMode.current() == Mode.TEST) {
//                url = "http://afentieng.test.17zuoye.net/entrance/index";
//            } else if (RuntimeMode.current() == STAGING) {
//                url = "http://afentieng.staging.17zuoye.net/entrance/index";
//            } else if (RuntimeMode.current() == PRODUCTION) {
//                url = "http://afentieng.17zuoye.com/entrance/index";
//            }
//        }
//        return url;
//    }

    /**********
     * 不能删除这两个方法，避免有其他活动写死在这边
     *******/
    @Deprecated
    @RequestMapping(value = "/thirdApp.vpage", method = RequestMethod.GET)
    public String thridApp() {
        return "forward:/app/redirect/jump.vpage";
    }

    @Deprecated
    @RequestMapping(value = "/selfApp.vpage", method = RequestMethod.GET)
    public String selfApp() {
        return "forward:/app/redirect/jump.vpage";
    }

    /**
     * 家长登陆需要获取sid
     * 先从链接中获取，获取不到然后在从cookie中获取
     */
    private long getStudentId() {
        User user = currentUser();
        long sid = getRequestLong("sid");
        sid = sid == 0 ? Long.parseLong(getCookieManager().getCookie("sid", "0")) : sid;

        if (user != null && user.isStudent()) {
            return user.getId();
        } else if (user != null && user.isParent() && sid != 0) {
            Set<Long> stuSet = parentLoaderClient.loadParentStudentRefs(user.getId()).stream()
                    .map(StudentParentRef::getStudentId)
                    .collect(Collectors.toSet());
            if (stuSet.contains(sid)) {
                return sid;
            }
        }
        return 0L;
    }


    private String getAppVersion() {
        User user = currentUser();
        if (user != null && user.isParent()) {
            return getRequestString("app_version");
        } else if (user != null && user.isStudent()) {
            return getRequestString("version");
        }
        return "";
    }

    private StudentDetail getRequestStudentDetail() {
        long sid = getStudentId();
        if (sid == 0) {
            return null;
        }
        return studentLoaderClient.loadStudentDetail(sid);
    }


    //保存进入游戏的用户带入入口id，家长端进入游戏需要累计到学生端
    private void setUserOrderRefer() {
        User currentUser = currentUser();
        if (currentUser == null) return;
        Long userId = currentUser.isParent() ? getStudentId() : currentUser.getId();
        String refer = getRequestString("refer");
        asyncOrderCacheServiceClient.getAsyncOrderCacheService()
                .UserOrderReferCacheManager_setRecord(userId, refer)
                .awaitUninterruptibly();
    }

    private String errorPage(Model model, String message) {
        String errorPage = "parentmobile/error";
        model.addAttribute("result", MapMessage.errorMessage(message).setErrorCode("400"));
        return errorPage;
    }
}
