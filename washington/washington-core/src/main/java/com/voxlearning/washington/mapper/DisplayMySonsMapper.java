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

package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.core.ArgMapper;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @File DisplayMySonsMapper.java
 * @Date 2011-8-28
 * @Author dongjw
 * @Description
 */
@Data
public class DisplayMySonsMapper implements Serializable {
    private static final long serialVersionUID = -3798219986424623775L;

    private Long userId;
    private String userName;
    private String userImg;
    private Long clazzId = 0L;
    private String clazzName = "";
    private String clazzLevel = "";
    private String schoolName = "";
    private List<ArgMapper> homeWorks = new ArrayList<>();
    private List<ArgMapper> messages = new ArrayList<>();
    private List<DisplayClazzMediaUrlMapper> displayClazzMediaUrlMappers = new ArrayList<>();
    private Integer integral = 0;
}
