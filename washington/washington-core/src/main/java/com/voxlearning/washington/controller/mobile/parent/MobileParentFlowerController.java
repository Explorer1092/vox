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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.FlowerSourceType;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 那些关于花的故事
 * Created by Hailong Yang on 2015/09/14.
 */
@Controller
@RequestMapping(value = "/parentMobile/flower")
@Slf4j
public class MobileParentFlowerController extends AbstractMobileParentController {

    @Inject private FlowerServiceClient flowerServiceClient;

    /**
     * 送花
     */
    @RequestMapping(value = "sendflower.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendFlower() {
        Long studentId = getRequestLong("sid");
        String homeworkId = getRequestString("hid");
        String homeworkType = getRequestString("htype");
        Long teacherId = getRequestLong("tid");

        if (studentId == 0 || StringUtils.isBlank(homeworkId) || StringUtils.isBlank(homeworkType) ||
                teacherId == 0 || FlowerSourceType.of(homeworkType) == FlowerSourceType.UNKNOWN) {
            return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }

        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        if (!studentIsParentChildren(currentUserId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("要送花的作业不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        if (newHomework.isHomeworkChecked()) {
            return MapMessage.errorMessage("已检查的作业不能送花").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生信息获取失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        if (studentDetail.getClazzId() == null) {
            return MapMessage.errorMessage("该学生当前已无班级,不能送花").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        FlowerSourceType type = FlowerSourceType.of(homeworkType);
        String flowerKey = homeworkType + "-" + homeworkId;


        //之前作业送花的限制。现在从底层移到业务层了。
        long count = flowerServiceClient.getFlowerService().loadHomeworkFlowers(flowerKey)
                .getUninterruptibly()
                .stream()
                .filter(t -> Objects.equals(studentId, t.getSenderId()))
                .count();
        if (count > 0) {
            return MapMessage.errorMessage("不能重复送花！");
        }

        MapMessage response = flowerServiceClient.getFlowerService()
                .sendFlower(studentId, parent.getId(), teacherId, studentDetail.getClazzId(), type, flowerKey)
                .getUninterruptibly();
        if (!response.isSuccess()) {
            response.setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        return response;
    }

    @RequestMapping(value = "flowerRank.vpage", method = RequestMethod.GET)
    public String flowerRank(Model model) {
        setRouteParameter(model);
        Long studentId = getRequestLong("sid");
        String pageAddr = "parentmobile/sendFlowerList";

        if (studentId <= 0) {
            model.addAttribute(
                    "result",
                    MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE)
            );
            return pageAddr;
        }

        User user = currentUser();
        if (user == null) {
            model.addAttribute(
                    "result",
                    MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE)
            );
            return pageAddr;
        }

        if (!studentIsParentChildren(user.getId(), studentId)) {
            model.addAttribute(
                    "result",
                    MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE)
            );
            return pageAddr;
        }

        List<Map<String, Object>> rankList = flowerRank(studentId);

        if (CollectionUtils.isEmpty(rankList)) {
            model.addAttribute(
                    "result",
                    MapMessage.successMessage().add("rankList", new ArrayList<>())
            );
            return pageAddr;
        }

        Comparator<Map<String, Object>> c = (a, b) ->
                ((Long) (b.get("flowerCount")))
                        .compareTo((Long) (a.get("flowerCount")));
        c = c.thenComparing((a, b) -> ((Long) a.get("lastTime")).compareTo((Long) b.get("lastTime")));
        rankList = rankList.stream().sorted(c)
                .collect(Collectors.toList());

        model.addAttribute(
                "result",
                MapMessage.successMessage().add("rankList", rankList)
        );

        return pageAddr;
    }
}
