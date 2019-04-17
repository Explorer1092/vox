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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 体验券
 * Created by Shuai Huan on 2014/12/1.
 */
@Controller
@RequestMapping("/crm/coupon")
public class CrmCouponController extends CrmAbstractController {

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @RequestMapping(value = "/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String query(Model model) {

        Long userId = getRequestLong("userId");
        try {
            List<Map<String, Object>> datas = new LinkedList<>();
            List<RewardCouponDetail> detailList = rewardLoaderClient.getRewardCouponDetailLoader().loadUserRewardCouponDetails(userId);
            for (RewardCouponDetail detail : detailList) {
                Map<String, Object> data = new HashMap<>();
                data.put("userId", userId);
                data.put("createTime", detail.getCreateDatetime());
                RewardProduct product = crmRewardService.$loadRewardProduct(detail.getProductId());
                data.put("couponName", product.getProductName());
                data.put("couponNO", detail.getCouponNo());
                data.put("used", detail.getUsed());
                data.put("id", detail.getId());
                datas.add(data);
            }
            model.addAttribute("userId", userId);
            model.addAttribute("datas", datas);
        } catch (Exception ignored) {
        }
        return "crm/coupon/list";
    }

    @RequestMapping(value = "/changeused.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeUsed() {
        Long couponId = getRequestLong("couponId");
        RewardCouponDetail detail = rewardManagementClient.loadRewardCouponDetail(couponId);
        MapMessage message;
        try {
            message = rewardManagementClient.couponUsed(detail);
        } catch (Exception ex) {
            logger.error("Failed to use coupon", ex);
            message = MapMessage.errorMessage();
        }
        return message;
    }
}
