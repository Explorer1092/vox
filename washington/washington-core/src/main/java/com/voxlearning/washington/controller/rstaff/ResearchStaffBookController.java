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
 * Research staff book controller implementation.
 *
 * @author Xiaohai Zhang
 * @since 2013-08-07 13:23
 */
@Controller
@RequestMapping("/rstaff/book")
public class ResearchStaffBookController extends AbstractController {

    /**
     * NEW 教研员
     * 根据区域、年级获得书籍信息
     *
     * level 年级
     */
    @RequestMapping(value = "sortbook.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage sortbook() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * NEW 教研员
     * 获取英语教材下的单元
     */
    @RequestMapping(value = "unit.vpage", method = RequestMethod.GET)
    public String findUnitByBookId(Model model) {
        return "redirect:/rstaff/report/behaviordata.vpage";
    }

    /**
     * NEW 教研员
     * 保存教研员选择的课本
     */
    @RequestMapping(value = "saveReseachStaffBook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map saveReseachStaffBook(HttpServletRequest request) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * NEW 教研员
     * 组卷统考 --> 组卷 --> 点击某本书 --> 查看已组试卷
     */
    @RequestMapping(value = "getResearchStaffPaperListByBookId.vpage", method = RequestMethod.GET)
    public String getResearchStaffPaperListByBookId(Model model) {
        return "redirect:/rstaff/report/behaviordata.vpage";
    }


}
