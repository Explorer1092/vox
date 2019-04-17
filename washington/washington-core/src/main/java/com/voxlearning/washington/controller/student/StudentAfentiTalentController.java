/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.washington.controller.student;

import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Student TALENT controller implementation.
 *
 * @author Guohong Tan
 * @author Maofeng Lu
 * @author Yizhou Zhang
 * @author Xiaoguang Wang
 * @author Xiaohai Zhang
 * @author Long Qian
 * @author Rui Bao
 * @since 2013-06-06
 */
@Controller
@RequestMapping("/student/afenti/talent")
public class StudentAfentiTalentController extends AbstractController {

    /**
     * 阿分题单词达人
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String signIn() {
        return "redirect:/student/index.vpage";
    }
}
