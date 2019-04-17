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

package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Finish homework mapper.
 * Migrate from washington.
 *
 * @author <a href="mailto:xiaohai.zhang@voxlearning.com">Xiaohai Zhang</a>
 * @serial
 * @since 2013-01-31 21:55
 */
@Getter
@Setter
public class FinishHomeworkMapper implements Serializable {
    private static final long serialVersionUID = 8341029785964837093L;

    private Long userId;
    private int practiceCount;
    private int completePractice;
}
