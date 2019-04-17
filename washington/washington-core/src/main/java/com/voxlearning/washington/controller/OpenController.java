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

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.google.code.kaptcha.util.Configurable;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.core.cdn.url2.rules.CdnRule;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;


@Controller
@RequestMapping(value = "")
@Slf4j
@NoArgsConstructor
public class OpenController extends AbstractController {
    private static final String MENTAL_RESOURCE = "mental_resource";

    @Inject private RaikouSystem raikouSystem;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    /**
     * 根据父节点id获取地理信息列表
     */
    @RequestMapping(value = "getregion-{regionCode}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map getRegion(@PathVariable Integer regionCode) {
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadChildRegions(regionCode);
        List<Map> regions = new ArrayList<>(regionList.size());
        for (ExRegion region : regionList) {
            regions.add(MiscUtils.map()
                    .add("key", region.getCode())
                    .add("value", region.getName())
            );
        }
        return MapMessage.successMessage().add("rows", regions).add("total", regions.size());
    }

    @RequestMapping(value = "captcha", method = RequestMethod.GET)
    public void captcha(HttpServletResponse resp) throws IOException {
        String token = getRequestParameter("token", "");
        Properties properties = new Properties();
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, "0123456789");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "100");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "40");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "30");
        properties.setProperty(Constants.KAPTCHA_BORDER, "no");
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.FishEyeGimpy");

        Producer kaptchaProducer = new DefaultKaptcha();
        ((Configurable) kaptchaProducer).setConfig(new Config(properties));

        // Set to expire far in the past.
        resp.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        resp.setHeader("Pragma", "no-cache");

        // return a jpeg
        resp.setContentType("image/jpeg");

        // create the text for the image
        String capText = kaptchaProducer.createText();

        // create the image with the text
        BufferedImage bi = kaptchaProducer.createImage(capText);

        // write the data out
        ImageIO.write(bi, "jpg", resp.getOutputStream());

        //set the attributes after we write the image in case the image writing fails.
        saveCaptchaCode(token, capText);
    }

    /**
     * 根据当前无效cdn地址切换新cdn地址。
     */
    @RequestMapping(value = "changeimgdomain.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeImgDomain() {
        String cdnUrl = getRequestParameter("imgDomain", "");
        //domainCdnKeyMap和cdnDomainMap中的url不包含http://,所以需要截一下

        // modified by shiyang
        if (StringUtils.isNotBlank(cdnUrl) && cdnUrl.matches("^http(s)?://.+$")) {
            cdnUrl = cdnUrl.replaceAll("^http(s)?://", "").trim();
        }
        //cdn类型列表
        List<String> cdnDomainMapKeys = ProductConfig.getCdnDomainMapKeys();

        if (cdnDomainMapKeys.size() == 0) {
            return MapMessage.errorMessage("CDN配置错误，请联系客服或技术");
        }
        //key是domain， value是cdn类型
        Map<String, String> domainCdnKeyMap = ProductConfig.getDomainCdnKeyMap();
        //key是cdn类型，value是domain
        Map<String, String> cdnDomainMap = ProductConfig.getCdnDomainMap();

        //根据imgDomain获取cdn类型
        String cdnType = domainCdnKeyMap.get(cdnUrl);

        int idx = (cdnDomainMapKeys.indexOf(cdnType) + 1) % cdnDomainMapKeys.size();
        cdnType = cdnDomainMapKeys.get(idx);
        cdnUrl = cdnDomainMap.get(cdnType);
        //如果cdnType=skip则表示没有可用的cdn服务直接返回主站地址
        //参考prepareflashloadercdntypes.ftl中设置的失效时间
        int expire = 86400 * 14;
        if ("skip".equals(cdnType)) {
            cdnUrl = ProductConfig.getMainSiteBaseUrl();
            expire = 7200;
        } else {
            cdnUrl = CdnRule.protocol + cdnUrl;
        }

        // modified by shiyang
        if ("https".equals(getRequest().getHeader("X-Forwarded-Proto"))) {
            cdnUrl = cdnUrl.replaceAll("^http://", "https://");
        }

        getWebRequestContext().getCookieManager().setCookie("cdntype", cdnType, expire);
        return MapMessage.successMessage().add("imgDomain", cdnUrl);
    }

    @RequestMapping(value = "getcdndomains.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCdndomains() {
        return MapMessage.successMessage().add("cdndomains", CdnConfig.getCdnDomainMap());
    }

    @RequestMapping(value = "resourceurl.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map getResourceUrl() {
        String names = getRequestString("names");

        List<String> nameArr = JsonUtils.fromJsonToList(names, String.class);

        Map<String, Object> map = new HashMap<>();
        if (nameArr == null) {
            return map;
        }

        for (String name : nameArr) {
            map.put(name, cdnResourceVersionCollector.getVersionedUrlPath(name));
        }

        return map;
    }

    /**
     * 获取ip信息
     *
     * @return
     */
    @RequestMapping(value = "getipinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getIpInfo() {
        String ip = getWebRequestContext().getRealRemoteAddress();
        String info = raikouSystem.parseIpLocation(ip).getFullAddress();
        return MapMessage.successMessage().add("ip", ip).add("address", info);
    }

    // 活动类的微信分享用
    @RequestMapping(value = "getpwxjsapiconfig.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getParentJsapiConfig() {
        setCorsHeadersForParent();
        String sourceUrl = getRequestString("url");
        if (StringUtils.isBlank(sourceUrl)) {
            return MapMessage.errorMessage("parameter url is required!");
        }

        String type = getRequestString("t");
        if (StringUtils.isBlank(type)) {
            type = "0";
        }

        String domain;
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            domain = "http://wechat.test.17zuoye.net";
        } else if (RuntimeMode.isStaging()) {
            domain = "http://wechat.staging.17zuoye.net";
        } else {
            domain = "http://wechat.17zuoye.com";
        }

        String url = domain + "/others/getjsapiconfig.vpage";
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url)
                .contentCharset(Charset.forName("UTF-8"))
                .addParameter("url", sourceUrl)
                .addParameter("t", type)
                .execute();

        MapMessage result = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
        if (result == null) {
            return MapMessage.errorMessage("parameter url is required!");
        }

        return result;
    }

    // 口算训练通用配置
    @RequestMapping(value = "mentalarithmeticconfig.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getMentalArithmeticConfig() {
        MapMessage resultMap = new MapMessage();
        try {
            String mentalConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), MENTAL_RESOURCE);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add("data", JsonUtils.fromJson(mentalConfig));
            return resultMap;
        } catch (Exception ex) {
            logger.error("Failed to load MENTAL_RESOURCE", ex);
            resultMap.add(RES_RESULT, RES_RESULT_ERROR);
            resultMap.add(RES_MESSAGE, "获取口算训练配置信息异常");
            return resultMap;
        }
    }
}
