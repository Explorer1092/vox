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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkLocation;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.voxlearning.washington.controller.open.OpenApiReturnCode.*;


/**
 * Created by Shuai Huan on 2015/2/4.
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher/reward")
@Slf4j
public class WechatTeacherRewardController extends AbstractOpenController {

    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;

    // 微信老师奖励学生学豆功能
    @RequestMapping(value = "sendcoinreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendCoinReward(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        long userId = SafeConverter.toLong(jsonMap.get("uid"), Long.MIN_VALUE);
        long clazzId = SafeConverter.toLong(jsonMap.get("clazzId"), Long.MIN_VALUE);
        List<Map> details = conversionService.convert(jsonMap.get("details"), List.class);
        String act = SafeConverter.toString(jsonMap.get("act"));
        String homeworkId = SafeConverter.toString(jsonMap.get("homeworkId"));
        if (userId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE || CollectionUtils.isEmpty(details)) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("clazzId", clazzId);
        dataMap.put("details", details);
        try {
            MapMessage message = atomicLockManager.wrapAtomic(newHomeworkServiceClient)
                    .keys("batchSendIntegral", userId)
                    .proxy()
                    .batchRewardStudentIntegral(userId, dataMap);
            if (message.isSuccess()) {
                openAuthContext.setCode(SUCCESS_CODE);
                // 记录批量奖励 一天一次
                if (StringUtils.equals("batch", act)) {
                    asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                            .TeacherBatchRewardStudentDayCacheManager_record(userId, homeworkId)
                            .awaitUninterruptibly();
                }
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
                return openAuthContext;
            }
        } catch (DuplicatedOperationException ex) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("正在发放奖励,请勿重复操作");
        } catch (Exception ex) {
            log.error("send coin reward failed.", ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("发放奖励失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "writecomment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext writeComment(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "commentlibrary.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext commentLibrary(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }


}
