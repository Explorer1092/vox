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

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.feedback.api.entities.ExamFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 用户反馈信息相关API
 * Created by Alex on 14-12-10.
 */
@Controller
@RequestMapping(value = "/v1/user/feedback")
public class UserFeedbackApiController extends AbstractApiController {

    @Inject private FeedbackServiceClient feedbackServiceClient;

    @RequestMapping(value = "/add.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addUserFeedback() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_FEEDBACK_CONTENT, "反馈内容");
            validateRequired(REQ_FEEDBACK_TYPE, "反馈类型");
//            validateEnum(REQ_FEEDBACK_TYPE, "反馈类型", "移动版问题");
            if (StringUtils.isBlank(getRequestString(REQ_FEEDBACK_VERSION))) {
                validateRequest(REQ_FEEDBACK_CONTENT, REQ_CONTACT_MOBILE, REQ_CONTACT_QQ, REQ_FEEDBACK_TYPE);
            } else {
                validateRequest(REQ_FEEDBACK_CONTENT, REQ_CONTACT_MOBILE, REQ_CONTACT_QQ, REQ_FEEDBACK_TYPE, REQ_FEEDBACK_VERSION);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        try {
            User curUser = getApiRequestUser();

            UserFeedback feedback = new UserFeedback();
            feedback.setUserId(curUser.getId());
            feedback.setUserType(curUser.getUserType());
            feedback.setRealName(curUser.getProfile().getRealname() == null ? curUser.getProfile().getNickName() : curUser.getProfile().getRealname());
            feedback.setContactSensitivePhone(sensitiveUserDataServiceClient.encodeMobile(getRequestString(REQ_CONTACT_MOBILE)));
            feedback.setContactSensitiveQq(sensitiveUserDataServiceClient.encodeQq(getRequestString(REQ_CONTACT_QQ)));
            feedback.setContent(getRequestString(REQ_FEEDBACK_CONTENT));
            feedback.setFeedbackType(getRequestString(REQ_FEEDBACK_TYPE));
            feedback.setIp(getWebRequestContext().getRealRemoteAddr());

            feedback.setExtStr1(getRequestString(REQ_FEEDBACK_VERSION));
            feedback.setExtStr2("");

            feedback.setFeedbackSubType1("");
            feedback.setFeedbackSubType2("");
            feedback.setPracticeType(0);
            feedback.setPracticeName("");
            feedback.setRefUrl("");

            feedbackServiceClient.getFeedbackService().saveFeedback(feedback);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }

        return resultMap;
    }

    @RequestMapping(value = "/v2/add.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addUserFeedbackDetails() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_FEEDBACK_CONTENT, "反馈内容");
            validateRequired(REQ_FEEDBACK_TYPE, "反馈类型");
            validateEnum(REQ_FEEDBACK_TYPE, "反馈类型", "移动版问题", "中学移动问题");
            validateRequest(REQ_FEEDBACK_CONTENT, REQ_FEEDBACK_TYPE, REQ_FEEDBACK_SUB_TYPE1, REQ_FEEDBACK_INFO, REQ_FEEDBACK_VERSION);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        try {
            User curUser = getApiRequestUser();

            UserFeedback feedback = new UserFeedback();
            feedback.setUserId(curUser.getId());
            feedback.setUserType(curUser.getUserType());
            feedback.setRealName(curUser.getProfile().getRealname() == null ? curUser.getProfile().getNickName() : curUser.getProfile().getRealname());
            feedback.setContactSensitivePhone("");
            feedback.setContactSensitiveQq("");
            feedback.setContent(getRequestString(REQ_FEEDBACK_CONTENT));
            feedback.setFeedbackType(getRequestString(REQ_FEEDBACK_TYPE));
            feedback.setIp(getWebRequestContext().getRealRemoteAddr());
            feedback.setFeedbackSubType1(getRequestString(REQ_FEEDBACK_SUB_TYPE1));

            Map<String, Object> infoMap = JsonUtils.fromJson(getRequestString(REQ_FEEDBACK_INFO));
            String homeworkInfo = "versionCode:" + getRequestString(REQ_FEEDBACK_VERSION) + "<br>";
            if (infoMap != null) {
                String qid = String.valueOf(infoMap.get("qid"));
                homeworkInfo += "homeworkId:" + infoMap.get("hid") + "<br>" +
                        " qid:" + qid + "<br>";
                feedback.setPracticeType(SafeConverter.toInt(infoMap.get("practice_type")));
                feedback.setPracticeName(String.valueOf(infoMap.get("practice_name")));

                // 这里加错题反馈的
                if ("习题有误".equals(feedback.getFeedbackSubType1()) &&
                        (StringUtils.startsWith(qid, "Q_") || StringUtils.startsWith(qid, "R_"))) {
                    if (curUser.fetchUserType() == UserType.STUDENT) {
                        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(curUser.getId());
                        if (studentDetail != null) {
                            // 区分中小学反馈
                            if (studentDetail.isPrimaryStudent()) {
                                feedbackServiceClient.getFeedbackService().sendExamFeedback(feedback.getUserId(), feedback.getContent(), ExamFeedback.fetchExamFeedbackType(9), qid);
                            } else if (studentDetail.isJuniorStudent()) {
                                feedbackServiceClient.getFeedbackService().sendExamFeedback(feedback.getUserId(), feedback.getContent(), ExamFeedback.fetchExamFeedbackType(10), qid);
                            }
                        }
                    }
                }
            }
            feedback.setExtStr1("");
            feedback.setExtStr2(homeworkInfo);

            feedback.setFeedbackSubType2("");
            feedback.setRefUrl("");

            feedbackServiceClient.getFeedbackService().saveFeedback(feedback);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }

        return resultMap;
    }

    /**
     * 用户反馈的提交子类型
     */
    @RequestMapping(value = "/feedbacktype/get.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getFeedbackType() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_FEEDBACK_SOURCE, "反馈来源");
            validateRequest(REQ_FEEDBACK_SOURCE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String source = getRequestString(REQ_FEEDBACK_SOURCE);
        Long userId = getApiRequestUser().getId();
        List<Map<String, Object>> infoList = getFeedbackType(source);
        if (CollectionUtils.isEmpty(infoList)) {
            resultMap.add(RES_FEEDBACK_SUBTYPE_LIST, Collections.emptyList());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }
        // 1.9.7版本后需要返回更多信息
        // 包括子类型按钮需要跳转的地址，以及某些灰度策略
        if (VersionUtil.compareVersion(ver, "1.9.7.0") >= 0) {
            List<Map<String, Object>> result = new LinkedList<>();
            infoList.stream()
                    .forEach(e -> {

                        //app低版本不显示入口
                        if (e.containsKey("from_ver")) {
                            String fromVer = SafeConverter.toString(e.get("from_ver"));
                            if (VersionUtil.compareVersion(ver, fromVer) < 0) {
                                return;
                            }
                        }

                        Map<String, Object> map = new HashMap<>();
                        map.put(RES_FEEDBACK_SUBTYPE_RESOLVE_TYPE, e.get("type"));
                        map.put(RES_FEEDBACK_SUBTYPE_TITLE, e.get("title"));
                        map.put(RES_FEEDBACK_SUBTYPE_DESC, e.get("desc"));
                        map.put(RES_FEEDBACK_SUBTYPE_ICON, e.get("icon"));
                        map.put(RES_FEEDBACK_SUBTYPE_ERROR_TYPE, e.get("error_type"));
                        map.put(RES_FEEDBACK_DEST_ID, e.get("dest_id"));
                        map.put(RES_FEEDBACK_QUESTION_TYPE, e.get("question_type"));
                        map.put(RES_FEEDBACK_TARGET_URL, e.get("target_url"));
                        String grayFunction = SafeConverter.toString(e.get("gray_function"));
                        String[] functions = grayFunction.split(",");
                        boolean grayFlag = false;
                        for (String function : functions) {
                            if (SafeConverter.toString(userId).endsWith(function)) {
                                grayFlag = true;
                                break;
                            }
                        }
                        map.put(RES_FEEDBACK_SUBTYPE_GRAY_FLAG, grayFlag);
                        result.add(map);
                    });
            resultMap.add(RES_FEEDBACK_SUBTYPE_LIST, result);

            if (getApiRequestUser().fetchUserType() == UserType.STUDENT) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
                if (studentDetail != null) {
                    if (studentDetail.isPrimaryStudent()) {
                        String bigVersion = generateBigVersion(ver);
                        resultMap.add(RES_FEEDBACK_SUBTYPE_H5_LINK, generateVersionUrl("resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/helper/index.vhtml"));
                    } else if (studentDetail.isJuniorStudent()) {
                        resultMap.add(RES_FEEDBACK_SUBTYPE_H5_LINK, generateVersionUrl("resources/apps/hwh5/homework-junior/static/feedback/help_solution.html"));
                        resultMap.add(RES_FEEDBACK_SUBTYPE_JUNIOR_H5_FEEDBACK_LINK, generateVersionUrl("resources/apps/hwh5/homework-junior/static/feedback/help_HWfeedback.html"));
                    }
                }
            }
        } else {
            // 需要兼容1.9.7之前的版本，只返回子类型名称列表
            List<String> subTypeList = infoList.stream()
                    .map(e -> SafeConverter.toString(e.get("title")))
                    .collect(Collectors.toList());
            resultMap.add(RES_FEEDBACK_SUBTYPE_LIST, subTypeList);
        }
        resultMap.add(RES_IMG_DOMAIN, getCdnBaseUrlStaticSharedWithSep());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    private List<Map<String, Object>> getFeedbackType(String source) {
        String config = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "feedback_subtype_new");
        config = config.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> configMap = JsonUtils.fromJson(config);
        if (MapUtils.isEmpty(configMap)) {
            return null;
        }
        return (List<Map<String, Object>>) configMap.get(source);
    }
}
