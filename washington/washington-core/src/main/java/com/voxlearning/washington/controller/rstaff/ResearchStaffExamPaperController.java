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

package com.voxlearning.washington.controller.rstaff;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
/**
 * Research staff exam paper controller implementation.
 *
 * @author Xiaohai Zhang
 * @since 2013-08-07 13:24
 */
@Controller
@RequestMapping("/rstaff/exampaper")
public class ResearchStaffExamPaperController extends AbstractController {

    /**
     * NEW 教研员
     * 预览试卷
     */
    @RequestMapping(value = "preview.vpage", method = RequestMethod.GET)
    public String preview(Model model, HttpServletRequest request) {
        return "redirect:/rstaff/report/behaviordata.vpage";
    }


    /**
     * 向市/区下的所有老师发送消息
     */
    @RequestMapping(value = "sendmessagetoteachers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendMessageForTeacher(Model model) {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "paper.vpage", method = RequestMethod.GET)
    public String paperDetail(Model model) {
        return "redirect:/rstaff/report/behaviordata.vpage";
    }

    /**
     * 编辑试卷 --》 保存
     */
    @RequestMapping(value = "savepaper.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage savePaperDetail() {
       return MapMessage.errorMessage("功能已下线");
    }


    /**
     * NEW 教研员
     * 组卷统考 -- 试卷及报告 -- 删除试卷
     */
    @RequestMapping(value = "updatepaperenable.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map updatepaperenable(HttpServletRequest request) {
        return MapMessage.errorMessage("功能已下线");
    }
}
