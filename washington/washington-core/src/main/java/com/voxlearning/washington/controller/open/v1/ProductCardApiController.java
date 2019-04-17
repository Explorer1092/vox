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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.product.ProductCard;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by Alex on 14-9-16.
 */
@Controller
@RequestMapping(value = "/v1/card/studycraft")
@Slf4j
public class ProductCardApiController extends AbstractApiController {

    @Inject private MiscServiceClient miscServiceClient;

    @RequestMapping(value = "/active.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage activeStudyCraftCard() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_CARD_KEY, "卡密码");
            validateRequest(REQ_CARD_KEY);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 验证APP是否允许调用此方法，只有口袋学社才能调用
        VendorApps vendorApps = getApiRequestApp();
        if (!vendorApps.getAppKey().equals("StudyCraft")) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
            return resultMap;
        }

        long cardKey = getRequestLong(REQ_CARD_KEY);
        // generate response message
        User curUser = getApiRequestUser();

        // 激活卡
        MapMessage activeResult = miscServiceClient.activeStudyCraftCard(cardKey, curUser.getId());
        if (activeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            ProductCard card  = (ProductCard) activeResult.get("cardInfo");
            resultMap.add(RES_CARD_SEQ, card.getCardSeq());
            resultMap.add(RES_CARD_EXT_INFO, card.getExtInfo());
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, activeResult.getInfo());
        }

        return resultMap;
    }
}
