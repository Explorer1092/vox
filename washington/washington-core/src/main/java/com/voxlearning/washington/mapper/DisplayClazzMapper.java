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

import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * DisplayClazzMapper.
 *
 * @author Jingwei Dong
 * @author Guohong Tan
 * @author Yaoheng Wu
 * @author Xiaohai Zhang
 * @serial
 * @since 2011-8-4
 */
@Data
public class DisplayClazzMapper implements Serializable {
    private static final long serialVersionUID = 2411283176914328824L;

    private Long id;                                        // 班级学号// 加密的班级学号
    private String className;                               // 班级名称

}
