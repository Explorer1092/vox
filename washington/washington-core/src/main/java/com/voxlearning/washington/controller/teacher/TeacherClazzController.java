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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Teacher clazz controller implementation.
 *
 * @author Xinqiang Wang
 * @author Xiaohai Zhang
 * @author Rui Bao
 * @since 2013-03-01 19:14
 */
@Controller
@RequestMapping("teacher/clazz")
public class TeacherClazzController extends AbstractTeacherController {

    // 2014暑期改版 -- 班级管理
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    public String clazzlist(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/systemclazz/clazzindex.vpage";
    }

    // 2014暑期改版 -- 教师创建班级入口 -- 选择学制，年级，班级名称
    @RequestMapping(value = "createclazz.vpage", method = RequestMethod.GET)
    public String index() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/systemclazz/clazzindex.vpage";
    }

    // 2014暑期改版 -- 提交建立班级表单
    @RequestMapping(value = "createclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createClazz() {
        logger.info("create clazz referer:{}", getRequest().getHeader("Referer"));// 输出来源page
        return MapMessage.errorMessage("目前暂不允许建立班级！");
    }

    // 2014暑期改版 -- 认证教师点击【学生管理】或者非认证教师点击【学生详情】 -- 获取学生列表
    @RequestMapping(value = "clazzsdetail.vpage", method = RequestMethod.GET)
    public String getDetailByClassId(@RequestParam("clazzId") Long clazzId, Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/clazz/clazzsdetail.vpage";
    }

    /**
     * 班级更换教材
     */
    @RequestMapping(value = "clazzunifybooks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBook(@RequestBody ChangeBookMapper command) {
        Teacher teacher = currentTeacher();
        MapMessage message = contentServiceClient.setClazzBook(teacher, command);
        if (message.isSuccess()) {
            if (teacher != null && Subject.MATH == teacher.getSubject()) {
                Set<Long> clazzIds = new TreeSet<>(StringHelper.toLongList(command.getClazzs()));
                List<Long> bookIds = StringHelper.toLongList(command.getBooks());
                if (CollectionUtils.isNotEmpty(clazzIds) && CollectionUtils.isNotEmpty(bookIds) && bookIds.size() == 1) {
                    Long oldBookId = bookIds.get(0);
                    NewBookProfile newBookProfile = newContentLoaderClient.loadNewBookProfileByOldId(Subject.MATH, oldBookId);
                    if (newBookProfile != null) {
                        ChangeBookMapper newMapper = new ChangeBookMapper();
                        newMapper.setClazzs(command.getClazzs());
                        newMapper.setBooks(newBookProfile.getId());
                        newMapper.setType(command.getType());
                        message = newContentServiceClient.getRemoteReference().setClazzBook(teacher, newMapper);
                        if (message.isSuccess()) {
                            message.setInfo(ConversionUtils.toInt(command.getType()) == 0 ? "教材添加成功" : "教材删除成功");
                        }
                    }
                }
            }
        } else {
            logger.error("Failed to change book: {}", message.getInfo());
        }
        return message;
    }

    /**
     * 下载学生学号
     */
    @RequestMapping(value = "downloadstudentlist.vpage", method = RequestMethod.GET)
    public String downloadstudentlist() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/systemclazz/clazzindex.vpage";
    }
}

