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
import java.util.List;

/**
 * @File BookMapper.java
 * @Date 2011-8-9
 * @Author dongjw
 * @Description
 */
@Data
public class BookMapper implements Serializable {
    private static final long serialVersionUID = 756690395019070557L;

    private Long bookId;
    private String bookName;
    private Long unitId;
    private String unitName;
    private Integer ptime;
    private List<LessonMapper> lessons;
}
