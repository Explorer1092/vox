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

package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * BookJsonMapper.java
 *
 * @author Jingwei Dong
 * @author Xiaohai Zhang
 * @serial
 * @since 2011-8-31
 */
@Data
public class BookJsonMapper implements Serializable {
    private static final long serialVersionUID = 1899004035726346697L;

    private String id;
    private String goodsId;
    private String cname;
    private String imgUrl;
    private String press;
}