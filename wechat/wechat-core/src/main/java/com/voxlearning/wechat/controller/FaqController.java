package com.voxlearning.wechat.controller;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaq;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaqCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xin Xin
 * @since 11/8/15
 */
@Controller
@RequestMapping(value = "/faq")
public class FaqController extends AbstractController {

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String type = getRequestString("t");
        if (StringUtils.isEmpty(type)) {
            return redirectWithMsg("参数错误", model);
        }

        try {
            List<WechatFaqCatalog> catalogs = wechatLoaderClient.loadWechatFaqCatalogs(WechatType.of(Integer.valueOf(type)));
            if (!CollectionUtils.isEmpty(catalogs)) {
                List<Map<String, String>> infos = new ArrayList<>();
                catalogs.stream().forEach(c -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("title", c.getName());
                    map.put("description", c.getDescription());
                    map.put("picUrl", getRequestContext().getWebAppBaseUrl() + c.getPicUrl());
                    map.put("url", "/faq/catalog.vpage?id=" + c.getId());
                    infos.add(map);
                });

                model.addAttribute("catalogs", infos);
            }
        } catch (Exception ex) {
            logger.error("Get catalog list failed, type:{}", type, ex);
        }
        return "/parent/faq/index";
    }

    @RequestMapping(value = "/catalog.vpage", method = RequestMethod.GET)
    public String catalog(Model model) {
        Long id = getRequestLong("id");
        String type = getRequestString("t");
        if (0 == id || StringUtils.isBlank(type)) {
            return redirectWithMsg("参数错误", model);
        }

        try {
            WechatFaqCatalog catalog = wechatLoaderClient.loadWechatFaqCatalog(id);
            if (null != catalog) {
                model.addAttribute("catalog", catalog);

                WechatType wechatType = WechatType.of(Integer.valueOf(type));
                if (wechatType == null) {
                    wechatType = WechatType.PARENT;
                }
                List<WechatFaq> faqs = wechatLoaderClient.loadCatalogWechatFaqs(catalog.getId(), wechatType);

                if (!CollectionUtils.isEmpty(faqs)) {
                    model.addAttribute("questions", faqs);
                }
            }
        } catch (Exception ex) {
            logger.error("Get catalog {} failed", id, ex);
        }
        return "/parent/faq/catalog";
    }

    @RequestMapping(value = "/question.vpage", method = RequestMethod.GET)
    public String question(Model model) {
        Long id = getRequestLong("id");
        if (0 == id) {
            return redirectWithMsg("参数错误", model);
        }

        try {
            WechatFaq wechatFaq = wechatLoaderClient.loadWechatFaq(id);
            if (null != wechatFaq) {
                model.addAttribute("id", id);
                model.addAttribute("title", wechatFaq.getTitle());
                model.addAttribute("content", wechatFaq.getContent());
            }
        } catch (Exception ex) {
            logger.error("Get question failed, id:{}", id, ex);
        }
        return "/parent/faq/question";
    }
}
