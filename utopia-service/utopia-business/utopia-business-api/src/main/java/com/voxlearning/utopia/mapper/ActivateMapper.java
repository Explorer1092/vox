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
import java.util.List;

/**
 * Activate mapper.
 *
 * @author Rui Bao
 * @version 0.1
 * @serial
 * @since 13-4-21
 */
@Data
public class ActivateMapper implements Serializable {
    private static final long serialVersionUID = -2291716019773358464L;

    private List<ActivateInfoMapper> userList;
    private Long classId;
}
