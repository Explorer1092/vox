/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by XiaoPeng.Yang on 14-11-26.
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/lottery")
public class SiteLotteryController extends SiteAbstractController {

    @Inject private CampaignLoaderClient campaignLoaderClient;
    @Inject private CampaignServiceClient campaignServiceClient;

    private static final int DEFAULT_CAMPAIGN_ID = CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN.getId();

    @RequestMapping(value = "ratelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String rateList(Model model) {
        Integer campaignId = getRequestInt("campaignId");
        List<CampaignLottery> lotteries = campaignLoaderClient.getCampaignLoader().findCampaignLotteries(campaignId);
        model.addAttribute("lotteries", lotteries);
        model.addAttribute("campaignId", campaignId);
        return "site/lottery/ratelist";
    }

    @RequestMapping(value = "editrate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editRate() {
        String data = getRequestString("rates");
        if (StringUtils.isBlank(data)) {
            return MapMessage.errorMessage("数据错误");
        }
        String[] rates = StringUtils.split(data, ",");
        for (String rate : rates) {
            String[] id_rate = StringUtils.split(rate, "_");
            campaignServiceClient.getCampaignService().$updateCampaignLotteryRate(ConversionUtils.toLong(id_rate[0]), ConversionUtils.toInt(id_rate[1]));
        }
        addAdminLog("修改中奖率");
        return MapMessage.successMessage().setInfo("操作成功");
    }

    @RequestMapping(value = "new/ratelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String newRatelist(Model model) {
        Integer id = Integer.valueOf(getRequestParameter("id", DEFAULT_CAMPAIGN_ID + ""));

        List<CampaignLottery> lotteries = campaignLoaderClient.getCampaignLoader().findCampaignLotteries(id);
        CampaignType campaignType = CampaignType.of(id);
        model.addAttribute("lotteries", lotteries);
        model.addAttribute("campaignId", campaignType.getId());
        model.addAttribute("campaignTypeName", campaignType.getName());
        model.addAttribute("lotteriesJSON", JSON.toJSONString(lotteries));
        return "site/lottery/new/ratelist";
    }

    @ResponseBody
    @RequestMapping(value = "new/save.vpage", method = {RequestMethod.POST})
    public MapMessage newSave(@RequestBody CampaignLottery campaignLottery) {
        Integer campaignId = campaignLottery.getCampaignId();
        CampaignType campaignType = CampaignType.of(campaignId);

        MapMessage mapMessage = campaignServiceClient.getCampaignService().validateCampaignLottery(campaignType, campaignLottery);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        if (campaignLottery.getAwardContent() == null) {
            campaignLottery.setAwardContent("");
        }

        try {
            if (campaignLottery.getId() == null) {
                campaignServiceClient.getCampaignService().$insertCampaignLottery(campaignLottery);
                addAdminLog("添加抽奖活动奖品");
            } else {
                campaignServiceClient.getCampaignService().$updateCampaignLottery(campaignLottery);
                addAdminLog("修改抽奖活动中奖率和奖品信息");
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("保存失败, 请检查奖品序号是否已存在");
        }
        return MapMessage.successMessage();
    }
}
