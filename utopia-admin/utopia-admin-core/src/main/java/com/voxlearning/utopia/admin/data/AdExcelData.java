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
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: Yuechen Wang
 * @Date: 2016-06-22
 */
@Data
@NoArgsConstructor
public class AdExcelData implements Serializable {

    private int totalShowPv;        // 曝光量
    private int totalShowUv;        // 独立用户曝光量
    private int totalClickPv;       // 点击量
    private int totalClickUv;       // 独立用户点击量
    private String dateStr;         // 日期
    private Date date;              // 日期

}
