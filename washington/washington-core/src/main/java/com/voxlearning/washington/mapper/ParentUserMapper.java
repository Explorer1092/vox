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

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ParentUserMapper extends UserMapper {
    private static final long serialVersionUID = -2991922796965528147L;

    @Getter @Setter private String currentDate;
    @Getter @Setter private String currentWeek;
    @Getter @Setter private String currentTimePart; //当前是上午还是下午
    @Getter @Setter private String imgUrl;
    @Getter @Setter private List<DisplayMySonsMapper> displayMySonsMappers = new ArrayList<DisplayMySonsMapper>(0);
    @Getter @Setter private String downLoad = "";
}
