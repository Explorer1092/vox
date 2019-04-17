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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.mapper.SmartClazzRank;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微信智慧教室相关
 * Created by Shuai Huan on 2015/2/28.
 */
@Controller
@RequestMapping(value = "/open/wechat/smartclazz")
@Slf4j
public class WechatTeacherSmartClazzController extends AbstractOpenController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;

    @RequestMapping(value = "clazzdetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext clazzDetail(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        long userId = SafeConverter.toLong(jsonMap.get("uid"), Long.MIN_VALUE);
        long clazzId = SafeConverter.toLong(jsonMap.get("clazzId"), Long.MIN_VALUE);
        if (userId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        try {
            GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
            if (group == null) {
                openAuthContext.setCode("400");
                openAuthContext.setError("invalid data");
                return openAuthContext;
            }

            //班级学豆池初始化
            SmartClazzIntegralPool pool = clazzIntegralServiceClient.getClazzIntegralService()
                    .loadClazzIntegralPool(group.getId())
                    .getUninterruptibly();
            if (pool == null) {
                openAuthContext.setCode("400");
                openAuthContext.setError("invalid data");
                return openAuthContext;
            }

            //奖励给该班级学生的学豆
            List<SmartClazzRank> clazzStudentIntegralList = businessTeacherServiceClient
                    .findSmartClazzIntegralHistory(group.getId(), DayRange.current().getStartDate());

            //剔除无名字的学生
            clazzStudentIntegralList = clazzStudentIntegralList.stream().filter(source ->
                    StringUtils.isNotBlank(source.getStudentName())
            ).collect(Collectors.toList());


            openAuthContext.setCode("200");
            openAuthContext.add("pool", pool);
            openAuthContext.add("studentList", clazzStudentIntegralList);
        } catch (Exception ex) {
            log.error("query smart clazz detail failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询智慧教室详细信息失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "exchangeintegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext exchangeIntegral(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        long userId = SafeConverter.toLong(jsonMap.get("uid"), Long.MIN_VALUE);
        long clazzId = SafeConverter.toLong(jsonMap.get("clazzId"), Long.MIN_VALUE);
        Integer integralCount = conversionService.convert(jsonMap.get("integralCount"), Integer.class);
        if (userId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);

        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            openAuthContext.setCode("400");
            openAuthContext.setError("非认证老师不能兑换学豆！");
            return openAuthContext;
        }

        if (integralCount <= 0 || integralCount % 5 != 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("奖励学豆数必须正整数并且是5的倍数！");
            return openAuthContext;
        }

        try {
            MapMessage mapMessage = atomicLockManager.wrapAtomic(clazzIntegralService)
                    .keyPrefix("WECHAT_SMARTCLAZZ_EXCHANGE:")
                    .keys(teacher.getSubject(), clazzId)
                    .proxy()
                    .saveSmartClazzExchangeIntegral(teacher, clazzId, integralCount);
            if (mapMessage.isSuccess()) {
                openAuthContext.setCode("200");
            } else {
                openAuthContext.setCode("400");
                openAuthContext.setError(mapMessage.getInfo());
            }
        } catch (DuplicatedOperationException de) {
            openAuthContext.setCode("400");
            openAuthContext.setError("操作正在进行，请勿重复操作");
        } catch (Exception ex) {
            log.error("exchange integral error! clazzId:{},teacherId:{},integral:{}", clazzId, teacher.getId(), integralCount, ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("兑换学豆失败！");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "rewardintegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext rewardintegral(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        long userId = SafeConverter.toLong(jsonMap.get("uid"), Long.MIN_VALUE);
        long clazzId = SafeConverter.toLong(jsonMap.get("clazzId"), Long.MIN_VALUE);
        if (userId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);

        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            openAuthContext.setCode("400");
            openAuthContext.setError("非认证老师不能奖励学生！");
            return openAuthContext;
        }

        try {
            MapMessage mapMessage = atomicLockManager.wrapAtomic(businessClazzIntegralServiceClient)
                    .keyPrefix("NEWSMARTCLAZZ_REWARD:")
                    .keys(teacher.getId(), clazzId)
                    .proxy()
                    .rewardSmartClazzStudent(teacher.getId(), raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId), jsonMap);

            if (mapMessage.isSuccess()) {
                openAuthContext.setCode("200");
            } else {
                openAuthContext.setCode("400");
                openAuthContext.setError(mapMessage.getInfo());
            }
        } catch (DuplicatedOperationException de) {
            openAuthContext.setCode("400");
            openAuthContext.setError("操作正在进行，请勿重复操作");
        } catch (Exception ex) {
            log.error("reward integral error! request json:{}", jsonMap, ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("发放学豆失败！");
        }
        return openAuthContext;
    }
}
