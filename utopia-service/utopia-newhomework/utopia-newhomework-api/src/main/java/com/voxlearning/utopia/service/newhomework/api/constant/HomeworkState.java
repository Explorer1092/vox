/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 作业状态
 *
 * @author Rui Bao
 * @since 2013-08-08
 */
@RequiredArgsConstructor
public enum HomeworkState {
    ASSIGN_HOMEWORK(0, "班级没有作业，可以布置作业"),
    CHECK_HOMEWORK(1, "检查作业"),
    ADJUST_DELETE_HOMEWORK(2, " 调整作业/检查作业"),
    NOT_APPLICABLE_ASSIGN_HOMEWORK(3, "不适合布置作业");

    @Getter private final int type;
    @Getter private final String description;
}
