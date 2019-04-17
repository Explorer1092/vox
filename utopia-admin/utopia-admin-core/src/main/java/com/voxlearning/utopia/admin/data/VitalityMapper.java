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

package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Vitality log data structure.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-03-20
 */
@Data
public class VitalityMapper implements Serializable {
    private static final long serialVersionUID = -3488194658324742600L;

    private String id;
    private Date createTime;
    private Date updateTime;
    private Long studentId;
    private int vitalityQty;
    private String type;
}
