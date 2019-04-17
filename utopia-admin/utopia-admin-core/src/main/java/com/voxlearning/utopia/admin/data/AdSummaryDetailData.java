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
 * @Author: Yuechen Wang
 * @Date: 2016-06-22
 */
@Data
public class AdSummaryDetailData implements Serializable {
    private static final long serialVersionUID = 1379020615994651437L;
    private String adCode;      // 广告编码
    private Long adId;          // 广告ID
    private String adSlot;      // 广告位ID，String
    private Date date;          // 日期
    private String dateStr;          // 日期
    private Integer provCode;   // 省Code, 按日期查询时为0
    private String provName;    // 省Name, 按日期查询时为空
    private Integer cityCode;   // 市Code, 按日期查询时为0
    private String cityName;    // 市Name, 按日期查询时为空
    private Integer countyCode; // 区Code, 按日期查询时为0
    private String countyName;  // 区Name, 按日期查询时为空
    private Long showPv;        // 曝光量
    private Long showUv;        // 独立用户曝光量
    private Long clickPv;       // 点击量
    private Long clickUv;       // 独立用户点击量
    private String clickRatePv; // 点击率
    private String clickRateUv; // 独立用户点击率
}
