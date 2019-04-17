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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.legacy.WellKnownCacheKeyGenerator;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.InactiveStudent;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TeacherRewardController.
 *
 * @author Yaoheng Wu
 * @author Xiaohai Zhang
 * @since 2012-8-27
 */
@Controller
@RequestMapping("/teacher/reward")
public class TeacherRewardController extends AbstractController {

    /**
     * 没满足3个学生绑定了手机的条件点击开始时，跳转到对应的班级
     */
    @RequestMapping(value = "bindstudentjump.vpage", method = RequestMethod.GET)
    public String callAmbassador() {
        List<Long> bindStudentIds = businessTeacherServiceClient.studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(currentUserId());

        Long clazzId = null;
        if (bindStudentIds.isEmpty()) {
            List<Clazz> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(currentUserId());
            Collection<Clazz> nonterminalClazzs = teacherClazzs.stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .collect(Collectors.toList());
            if (!nonterminalClazzs.isEmpty()) {
                clazzId = MiscUtils.firstElement(nonterminalClazzs).getId();
            }
        } else {
            Long studentId = bindStudentIds.get(0);
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            if (null != clazz) {
                clazzId = clazz.getId();
            }
        }
        if (clazzId == null) {
            return "redirect:/teacher/clazz/clazzlist.vpage";
        } else {
            return "redirect:/teacher/clazz/clazzsdetail.vpage?clazzId=" + clazzId;
        }

    }

    //Happy Thanksgiving! 感恩节唤醒学生送话费 大礼包
    @RequestMapping(value = "thanksgiving.vpage", method = RequestMethod.GET)
    public String teacher(Model model) {
        Long teacherId = currentUserId();
        if (teacherId == null) {
            return "redirect:/teacher/index.vpage";
        }
        List<InactiveStudent> students = teacherLoaderClient.loadTeacherInactiveStudents(teacherId);
        //分组
        Map<String, List<InactiveStudent>> data = new HashMap<>();
        int count = 0;
        for (InactiveStudent student : students) {
            if (student.getActiveFlag()) {
                count++;
            }
            if (data.containsKey(student.getClazzName())) {
                data.get(student.getClazzName()).add(student);
            } else {
                List<InactiveStudent> list = new ArrayList<>();
                list.add(student);
                data.put(student.getClazzName(), list);
            }
        }
        model.addAttribute("total", count);
        model.addAttribute("waitCount", students.size() - count);
        model.addAttribute("data", data);
        return "teacherv3/reward/thanksgiving";
    }

    //领取班费奖励
    @RequestMapping(value = "rewardreceivebonus.vpage", method = RequestMethod.GET)
    public String rewardReceiveBonus(Model model) {
        Teacher teacher = currentTeacher();

        // 判断一个老师是否领取过新年奖励
        String key = WellKnownCacheKeyGenerator.generateTeacherNewYearBonusKey(teacher.getId());
        CacheObject<String> cacheObject = washingtonCacheSystem.CBS.persistence.get(key);
        model.addAttribute("newYearBonus", cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue()));
        // 判断是否需要展示二维码
        model.addAttribute("wxbinded", wechatLoaderClient.isBinding(teacher.getId(), WechatType.TEACHER.getType()));

        return "teacherv3/reward/rewardreceivebonus";
    }
}
