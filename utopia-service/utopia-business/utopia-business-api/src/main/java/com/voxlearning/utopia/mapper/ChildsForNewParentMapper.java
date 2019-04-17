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
 * Created with IntelliJ IDEA.
 * User: dongjw
 * Date: 12-10-22
 * Time: 下午12:49
 * To change this template use File | Settings | File Templates.
 */
@Data
public class ChildsForNewParentMapper implements Serializable {
    private static final long serialVersionUID = -5268108035150885154L;

    private Long userId;
    private String userName;
    private String userImg;
    private Integer userIntegral;
    private String schoolName;
    private String clazzName;
    private Long clazzId;
}
