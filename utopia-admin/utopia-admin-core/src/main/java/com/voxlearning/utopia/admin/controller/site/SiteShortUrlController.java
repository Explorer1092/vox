package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

/**
 * @author xinxin
 * @since 9/10/2016
 */
@Controller
@RequestMapping("/site/surl")
@Slf4j
public class SiteShortUrlController extends SiteAbstractController {

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index() {
        return "/site/surl/index";
    }

    @RequestMapping(value = "/create.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage create() {
        String longUrl = getRequestString("url");
        if (StringUtils.isBlank(longUrl)) return MapMessage.errorMessage("长地址错误");

        try {
            Optional<String> shortUrl = ShortUrlGenerator.generateShortUrl(longUrl, false);
            if (!shortUrl.isPresent()) return MapMessage.errorMessage("生成失败");

            addAdminLog("生成短地址", getCurrentAdminUser().getAdminUserName(), "longUrl:" + longUrl + ",shortUrl:" + shortUrl.get());

            return MapMessage.successMessage().add("surl", ShortUrlGenerator.getShortUrlSiteUrl() + "/" + shortUrl.get());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }
}
