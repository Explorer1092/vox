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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;

/**
 * @author dell
 * @since 下午3:56,13-6-4.
 */
@Controller
@RequestMapping("/crm/region")
@Slf4j
public class CrmRegionController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 跳转到区域主页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String forwardIndex() {
        return "crm/region/index";
    }

    /**
     * 添加区域后，回到区域主页
     */
    @RequestMapping(value = "addRegion.vpage", method = RequestMethod.POST)
    public String addRegion(@RequestParam(value = "addRegion_region", required = false) String cName,
                            @RequestParam(value = "addRegion_city", required = false) String cityCode) {
        if (StringUtils.isBlank(cName)) {
            getAlertMessageManager().addMessageError("区域名称不能为空");
        }

        if (StringUtils.isBlank(cityCode)) {
            getAlertMessageManager().addMessageError("请选择市区");
        }

        Integer cityCodeInt = null;
        try {
            cityCodeInt = conversionService.convert(cityCode, Integer.class);
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("市区编码值非法");
        }

        if (!getAlertMessageManager().hasMessageError()) {
            int regionCode;
            try {

                MapMessage message = raikouSystem.getRegionService().createRegion(cityCodeInt, cName);
                if (!message.isSuccess()) {
                    regionCode = -1;
                } else {
                    regionCode = (Integer) message.get("CODE");
                }
            } catch (CannotAcquireLockException ex) {
                regionCode = -1;
            }
            if (regionCode <= 0) {
                getAlertMessageManager().addMessageError("创建区域失败");
            } else {
                addAdminLog("addRegion", cName, "在编码值为" + cityCode + "的市区下添加" + cName);
                getAlertMessageManager().addMessageSuccess("区域保存成功");
            }
        }

        return redirect("index.vpage");
    }
}
