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

package com.voxlearning.washington.mapper;

import lombok.Data;

import java.io.Serializable;


@Data
public class QuickSelectMapper implements Serializable {

    private static final long serialVersionUID = 4304492157286608281L;

    private Long lessonId = 0L;
    private Integer practiceType = 0;
    private Integer rank = 0;
    private boolean checked = false;
    private Integer time = 0;

}
