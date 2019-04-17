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

package com.voxlearning.washington.controller.connect;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;

/**
 * Created by Summer Yang on 2015/8/20.
 */
@Controller
@RequestMapping("/bbs")
@Slf4j
@NoArgsConstructor
public class BBSController extends AbstractController {

    @Inject private AmbassadorServiceClient ambassadorServiceClient;

    @RequestMapping(value = "lighticon.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage lightIcon(@RequestBody String body) {
        String secretKey = "0501511e269e47d48626c7a69f85a8ba";
        Map<String, Object> map = JsonUtils.fromJson(body);
        Long teacherId = ConversionUtils.toLong(map.get("teacherId"), 0L);
        String token = ConversionUtils.toString(map.get("token"));
        if (teacherId == 0L || StringUtils.isBlank(token)) {
            return MapMessage.errorMessage();
        }

        String secretStr = DigestUtils.md5Hex(teacherId + secretKey);
        if (StringUtils.equals(token, secretStr)) {

            ambassadorServiceClient.getAmbassadorService().recordAmbassadorMentor(teacherId, MiscUtils.map(UserTagType.AMBASSADOR_MENTOR_BBS, UserTagEventType.AMBASSADOR_MENTOR_BBS));
            // 每周论坛发贴 给预备大使加努力值 每周加两次 一次一分
            String key = "TEACHER_BBS_ADD_COMPETITION_WEEK:" + teacherId;
            CacheObject<String> cacheObject = CacheSystem.CBS.getCache("persistence").get(key);
            if (cacheObject != null) {
                if (StringUtils.isBlank(cacheObject.getValue()) || SafeConverter.toInt(StringUtils.trim(cacheObject.getValue())) == 1) {
                    // 预备大使添加努力值
                    ambassadorServiceClient.getAmbassadorService().addCompetitionScore(teacherId, 0L, AmbassadorCompetitionScoreType.BBS);
                    // 正式大使添加积分
                    ambassadorServiceClient.getAmbassadorService().addAmbassadorScore(teacherId, 0L, AmbassadorCompetitionScoreType.BBS);
                }
                CacheSystem.CBS.getCache("persistence").incr(key, 1, 1, DateUtils.getCurrentToWeekEndSecond());
            }
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }
}
