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

import com.voxlearning.utopia.mapper.NeedMasterBookMapper;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @File DisplayEditClazzMapper.java
 * @Date 2011-8-30
 * @Author dongjw
 * @Description
 */
@Data
public class DisplayEditClazzMapper implements Serializable {
    private static final long serialVersionUID = -7875456224025260469L;

    /**
     * 班级编号
     */
    private Long clazzId;
    /**
     * 年级
     */
    private String clazzLevel;
    /**
     * 班级名称
     */
    private String clazzName;
    /**
     * 班级全称
     */
    private String fullClazzName;
    /**
     * 班级人数
     */
    private Integer clazzSize;
    /**
     * 当前人数
     */
    private Integer clazzNum;
    /**
     * 所属学校名称
     */
    private String schoolName;

    private Integer master = 0;

    private String bookJson;

    private Integer needMaster = 0;

    private String eduSystem;       // 学制

    private Boolean freeJoin;      // 是否允许学生自由加入

    private List<Object> books = new ArrayList<>();

    private List<NeedMasterBookMapper> needMasterBook = new ArrayList<NeedMasterBookMapper>();

}
