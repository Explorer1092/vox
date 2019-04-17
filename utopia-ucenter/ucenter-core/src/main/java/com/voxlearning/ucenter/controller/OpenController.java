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

package com.voxlearning.ucenter.controller;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.google.code.kaptcha.util.Configurable;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author xinxin
 * @since 14/12/2015.
 */
@Controller
@NoArgsConstructor
@RequestMapping(value = "/")
public class OpenController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 根据父节点id获取地理信息列表
     */
    @RequestMapping(value = "getregion.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map getRegion() {
        int regionCode = getRequestInt("regionCode");

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
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.WaterRipple");

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
}
