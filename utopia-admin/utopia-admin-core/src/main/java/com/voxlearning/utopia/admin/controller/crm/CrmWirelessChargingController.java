/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.dao.finance.WirelessChargingPersistence;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Wireless Charging
 * Created by Shuai Huan on 2014/11/26.
 */
@Controller
@RequestMapping("/crm/wirelesscharging")
public class CrmWirelessChargingController extends CrmAbstractController {

    @Inject private WirelessChargingPersistence wirelessChargingPersistence;

    @RequestMapping(value = "/wirelesslist.vpage", method = RequestMethod.GET)
    public String index() {
        return "crm/wirelesscharging/wirelesscharginghomepage";
    }

    @RequestMapping(value = "/wirelesslist.vpage", method = RequestMethod.POST)
    public String query(Model model) {

        Long userId = getRequestLong("userId");
        String mobile = getRequestString("mobile");
        String startDateStr = getRequestParameter("startDate", "").trim();
        Integer type = getRequestInt("type", -1);
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");
        try {
            Date startDate;
            if (StringUtils.isNotBlank(startDateStr)) {
                startDate = sdf.parse(startDateStr);
            } else {
                startDate = DateUtils.calculateDateDay(new Date(), -7);
            }
            List<WirelessCharging> wirelessChargingList = getWirelessChargingList(userId, mobile, startDate, type);
            for (WirelessCharging wirelessCharging : wirelessChargingList) {
                wirelessCharging.setTargetSensitiveMobile(sensitiveUserDataServiceClient.showWirelessChargingTargetMobile(wirelessCharging.getId(), "crm:wirelesschargingquery", getCurrentAdminUser().getAdminUserName()));
            }
            model.addAttribute("datas", wirelessChargingList);
            model.addAttribute("userId", userId);
            model.addAttribute("mobile", mobile);
            if (StringUtils.isNotBlank(startDateStr)) {
                model.addAttribute("startDate", sdf.format(startDate));
            }
            model.addAttribute("type", type);
        } catch (Exception ignored) {
        }
        return "crm/wirelesscharging/wirelesscharginghomepage";
    }

    private List<WirelessCharging> getWirelessChargingList(Long userId, String mobile, Date startDate, Integer type) {
        Criteria criteria = new Criteria();
        if (SafeConverter.toLong(userId) > 0) {
            criteria = criteria.and("USER_ID").is(userId);
        } else if (StringUtils.isNotEmpty(mobile)) {
            String sensitiveMobile = sensitiveUserDataServiceClient.encodeMobile(mobile);
            criteria = criteria.and("TARGET_MOBILE").is(sensitiveMobile);
        } else {
            criteria = criteria.and("CREATE_DATETIME").gte(startDate);
        }

        if (type != null && type != -1) {
            criteria = criteria.and("STATUS").is(type);
        }

        Query query = Query.query(criteria)
                .with(new Sort(Sort.Direction.DESC, "CREATE_DATETIME"));
        return wirelessChargingPersistence.query(query);
    }
}
