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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaq;
import com.voxlearning.utopia.service.wechat.client.WechatFaqServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 微信faq管理
 * Created by Shuai Huan on 2015/4/23.
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/wechatfaq")
public class SiteWechatFaqController extends SiteAbstractController {

    @Inject private WechatFaqServiceClient wechatFaqServiceClient;

    @RequestMapping(value = "list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String list(Model model) {
        long catalogId = getRequestLong("cid");
        int status = getRequestInt("status");
        String keyWords = getRequestString("keywords");
        long id = getRequestLong("id");
        int type = getRequestInt("type");
        model.addAttribute("faqs", queryFaqs(id, keyWords, catalogId, status, type));
        model.addAttribute("status", status);
        model.addAttribute("cid", catalogId);
        model.addAttribute("type", type);
        model.addAttribute("wechatTypes", WechatType.values());
        model.addAttribute("catalogs", wechatLoaderClient.loadWechatFaqCatalogs(WechatType.of(type)));

        return "site/wechatfaq/list";
    }

    @RequestMapping(value = "toeditpage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String edit(Model model) {
        long id = getRequestLong("id");
        int type = getRequestInt("type");
        if (id > 0) {
            model.addAttribute("faq", wechatLoaderClient.loadWechatFaq(id));
        }
        model.addAttribute("catalogs", wechatLoaderClient.loadWechatFaqCatalogs(WechatType.of(type)));
        return "site/wechatfaq/edit";
    }

    @RequestMapping(value = "addfaq.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addFaq(@RequestBody WechatFaq entity) {
        if (entity.getId() != null) {
            WechatFaq wechatFaq = wechatLoaderClient.loadWechatFaq(entity.getId());
            wechatFaq.setType(entity.getType());
            wechatFaq.setStatus(entity.getStatus());
            wechatFaq.setCatalogId(entity.getCatalogId());
            wechatFaq.setContent(entity.getContent());
            wechatFaq.setDescription(entity.getDescription());
            wechatFaq.setKeyWord(entity.getKeyWord());
            wechatFaq.setPicUrl(entity.getPicUrl());
            wechatFaq.setTitle(entity.getTitle());
            entity = wechatFaq;
        }
        entity.setDisabled(false);
        MapMessage message = wechatServiceClient.addOrUpdateWechatFaq(entity);
        return message.setInfo(message.isSuccess() ? "操作成功" : "操作失败");
    }

    @RequestMapping(value = "approvefaq.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveFaq(@RequestParam String faqIds) {
        if (StringUtils.isBlank(faqIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            wechatServiceClient.changeFaqsStatus(StringUtils.toLongList(faqIds), "published");
        } catch (Exception ignore) {
            return MapMessage.errorMessage("后台异常，请联系管理员");
        }
        return MapMessage.successMessage("操作成功");
    }

    @RequestMapping(value = "rejectfaq.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectFaq(@RequestParam String faqIds) {
        if (StringUtils.isBlank(faqIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            wechatServiceClient.changeFaqsStatus(StringUtils.toLongList(faqIds), "draft");
        } catch (Exception ignore) {
            return MapMessage.errorMessage("后台异常，请联系管理员");
        }
        return MapMessage.successMessage("操作成功");
    }

    @RequestMapping(value = "removefaq.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeFaq(@RequestParam String faqIds) {
        if (StringUtils.isBlank(faqIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            wechatServiceClient.removeFaqs(StringUtils.toLongList(faqIds));
        } catch (Exception ignore) {
            return MapMessage.errorMessage("后台异常，请联系管理员");
        }
        return MapMessage.successMessage("操作成功");
    }

    private List<WechatFaq> queryFaqs(Long id, String keyWords, Long catalogId, Integer status, int type) {
        List<WechatFaq> wechatFaqs = wechatFaqServiceClient.getWechatFaqService()
                .loadAllWechatFaqs()
                .getUninterruptibly();

        Stream<WechatFaq> stream = wechatFaqs.stream()
                .filter(e -> !e.isDisabledTrue())
                .filter(e -> Objects.equals(e.getType(), type));
        if (id > 0) {
            return stream
                    .filter(e -> Objects.equals(id, e.getId()))
                    .collect(Collectors.toList());
        } else {
            return stream
                    .filter(e -> StringUtils.isEmpty(keyWords) || e.getTitle().contains(keyWords))
                    .filter(e -> catalogId == null || Objects.equals(e.getCatalogId(), catalogId))
                    .filter(e -> status != 0 || Objects.equals(e.getStatus(), "draft"))
                    .filter(e -> status != 1 || Objects.equals(e.getStatus(), "published"))
                    .collect(Collectors.toList());
        }

        // 需要的话重新写。。。这都叫啥实现
//        String queryFaqs = "";
//        Map<String, Object> queryParams = new HashMap<>();
//
//        if (id > 0) {
//            queryParams.put("id", id);
//            queryFaqs += " AND wf.ID = :id ";
//        } else {
//            if (StringUtils.isNotEmpty(keyWords)) {
//                queryParams.put("keyWords", "%" + keyWords + "%");
//                queryFaqs += " AND wf.TITLE LIKE :keyWords ";
//            }
//            if (catalogId != null) {
//                queryParams.put("catalogId", catalogId);
//                queryFaqs += " AND wf.CATALOG_ID = :catalogId ";
//            }
//            if (status == 0) {
//                queryParams.put("status", "draft");
//                queryFaqs += " AND wf.STATUS = :status ";
//            }
//            if (status == 1) {
//                queryParams.put("status", "published");
//                queryFaqs += " AND wf.STATUS = :status ";
//            }
//        }
//
//        queryFaqs = "SELECT wf.* FROM VOX_WECHAT_FAQ wf " +
//                " WHERE wf.DISABLED = 0 AND type = " + type + " " + queryFaqs;
//
//        queryFaqs += " ORDER BY wf.CREATE_DATETIME DESC";
//        return wechatLoaderClient.loadWechatFaqForCrm(queryFaqs, queryParams);
    }
}
