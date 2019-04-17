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

package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_APP_NATIVE_VERSION;
import static com.voxlearning.washington.controller.open.ApiConstants.REQ_SYS;

/**
 *
 * Created by jiangpeng on 16/4/18.
 */

@Controller
@RequestMapping(value = "/teacherMobile/invite")
@Slf4j
public class MobileTeacherInviteController extends AbstractMobileTeacherController {

    @Inject private UserAdvertisementServiceClient userAdvertisementServiceClient;

    /**
     * 福利列表
     * 用的是广告的功能哦
     *
     * @return
     */
    @RequestMapping(value = "/welfare/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage welfareList() {

        Teacher teacher = currentTeacher();
        if (teacher == null)
            return noLoginResult;

        final String slotId = "130101";
//        List<AdMapper> adMapperList = userAdvertisementLoaderClient.loadAdvertisementData(currentUserId(), AdvertisementPositionType.TEACHER_APP_WELFARE.getType());
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        List<Map<String, Object>> adMapList = new ArrayList<>();
        int index = 0;
        String ua = getRequest().getHeader("User-Agent");
        String sys = getRequestString(REQ_SYS);
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(currentUserId(), slotId, getRequestString(REQ_SYS), ver);
        for (NewAdMapper p : newAdMappers) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("img", combineCdbUrl(p.getImg()));
            map.put("resourceUrl", AdvertiseRedirectUtils.redirectUrl(p.getId(), index, ver, sys, "", 0L));
            adMapList.add(map);
            //曝光打点
            if (Boolean.TRUE.equals(p.getLogCollected())) {
                LogCollector.info("sys_new_ad_show_logs",
                        MiscUtils.map(
                                "user_id", teacher.getId(),
                                "env", RuntimeMode.getCurrentStage(),
                                "version", ver,
                                "aid", p.getId(),
                                "acode", SafeConverter.toString(p.getCode()),
                                "index", index,
                                "slotId", slotId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", ua,
                                "system", sys
                        ));
            }
            index++;
        }
        return MapMessage.successMessage().add("welfareList", adMapList);
    }
}
