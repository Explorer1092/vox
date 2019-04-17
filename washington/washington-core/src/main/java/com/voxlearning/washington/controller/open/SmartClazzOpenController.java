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

package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.mappers.ClazzStudentCardMapper;
import com.voxlearning.washington.data.OpenAuthContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 智慧课堂与移动端
 *
 * @author Maofeng Lu
 * @since 14-10-27 上午10:27
 */
@Controller
@RequestMapping(value = "/open/smartclazz")
public class SmartClazzOpenController extends AbstractOpenController {

    @Inject private RaikouSDK raikouSDK;

    @RequestMapping(value = "/getstudentscardref.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getclazzstudents(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long userId = conversionService.convert(openAuthContext.getParams().get("uid"), Long.class);
        Long clazzId = conversionService.convert(openAuthContext.getParams().get("clazzId"), Long.class);
        if (!teacherLoaderClient.isTeachingClazz(userId, clazzId)) {
            logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, userId);
            openAuthContext.setCode("500");
            openAuthContext.setError("你没有该班级管理权限");
            return openAuthContext;
        }
        List<Map<String, Object>> students = new LinkedList<>();

        //初始化卡号
        MapMessage mapMessage;
        try {
            //初始化卡号
            mapMessage = atomicLockManager.wrapAtomic(clazzServiceClient)
                    .keyPrefix("SMARTCLAZZ_CARD:")
                    .keys(clazzId)
                    .proxy()
                    .initClazzStudentCard(clazzId, userId);
            if (mapMessage.isSuccess()) {
                List<ClazzStudentCardMapper> studentList = (List<ClazzStudentCardMapper>) mapMessage.get("studentCardList");
                if (studentList == null) {
                    studentList = new LinkedList<>();
                }
                for (ClazzStudentCardMapper user : studentList) {
                    Map<String, Object> studentMap = new LinkedHashMap<>();
                    studentMap.put("studentId", user.getStudentId());
                    studentMap.put("studentName", user.getStudentName());
                    studentMap.put("studentCode", user.getCardNo());
                    studentMap.put("studentAnswer", ""); //为了保持和提交数据的结构统一
                    students.add(studentMap);
                }
                students.sort((o1, o2) -> Integer.compare(SafeConverter.toInt(o1.get("studentCode")), SafeConverter.toInt(o2.get("studentCode"))));
            } else {
                openAuthContext.setError(mapMessage.getInfo());
            }
        } catch (Exception e) {
            //ignore
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("clazzId", clazz.getId());
        dataMap.put("clazzName", clazz.formalizeClazzName());
        dataMap.put("students", students);
        openAuthContext.add("successful", true);
        openAuthContext.add("data", dataMap);
        return openAuthContext;
    }


    @RequestMapping(value = "/postScanAnswer.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext postScanAnswer(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long uid = conversionService.convert(openAuthContext.getParams().get("uid"), Long.class);
        try {
            Map<String, Object> report = (Map<String, Object>) openAuthContext.getParams().get("data");
            if (report != null && report.size() > 0) {
                //iOS应用传输过来的答案选项key有错误，暂时强制转换为统一key,待iOS应用审核通过后,删除处理代码
                if (!report.containsKey("answerCountA") && report.containsKey("AnswerCountA")) {
                    report.put("answerCountA", report.get("AnswerCountA"));
                    report.remove("AnswerCountA");
                }
                if (!report.containsKey("answerCountB") && report.containsKey("AnswerCountB")) {
                    report.put("answerCountB", report.get("AnswerCountB"));
                    report.remove("AnswerCountB");
                }
                if (!report.containsKey("answerCountC") && report.containsKey("AnswerCountC")) {
                    report.put("answerCountC", report.get("AnswerCountC"));
                    report.remove("AnswerCountC");
                }
                if (!report.containsKey("answerCountD") && report.containsKey("AnswerCountD")) {
                    report.put("answerCountD", report.get("AnswerCountD"));
                    report.remove("AnswerCountD");
                }

                Long clazzId = conversionService.convert(report.get("clazzId"), Long.class);
                if (uid != null && clazzId != null) {
                    //缓存5分钟,加分隔符避免缓存键的重复
                    String cacheKey = StringUtils.join(new Object[]{"SMARTCLAZZ_SCAN_ANSWER", uid, clazzId}, ":");
                    Boolean ret = washingtonCacheSystem.CBS.unflushable.set(cacheKey, 300, report);
                    if (!Boolean.TRUE.equals(ret)) {
                        logger.error("Failed to set couchbase (cacheKey={})", cacheKey);
                        throw new RuntimeException("Failed to access couchbase server, the cache key is '" + cacheKey + "'");
                    }
                }
            }
            openAuthContext.add("successful", true);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            openAuthContext.add("successful", false);
            openAuthContext.setCode("500");
            openAuthContext.setError("数据保存失败，原因：" + e.getMessage());
        }

        return openAuthContext;
    }
}
