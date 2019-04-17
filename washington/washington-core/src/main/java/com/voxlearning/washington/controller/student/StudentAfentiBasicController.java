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
 * Student afenti basic controller implementation.
 *
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @author Xiaoguang Wang
 * @author Peng Zou
 * @author Long Qian
 * @author Yizhou Zhang
 * @author Rui Bao
 * @author Longlong Yu
 * @since 2013-03-19
 */
@Controller
@RequestMapping("/student/afenti/basic")
public class StudentAfentiBasicController extends AbstractController {

    /**
     * Sign in ISLAND
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String signInIsland() {
        return "redirect:/student/index.vpage";
    }
}
