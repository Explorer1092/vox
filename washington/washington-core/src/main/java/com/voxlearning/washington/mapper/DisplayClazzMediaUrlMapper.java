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

/**
 * @File DisplayClazzMediaUrlMapper.java
 * @Date 2011-9-6
 * @Author dongjw
 * @Description
 */
@Data
public class DisplayClazzMediaUrlMapper implements Serializable {
    private static final long serialVersionUID = -1838729524128965077L;

    private Long clazzId;
    private String clazzName;
    private String clazzLevel;
    private String mediaUrl = "";
    private String avgScore = "";
    private String userName = "";
    private String userImg = "";
    private String schoolName = "";
}
