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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.athena.bean.AdPlatform;
import lombok.Data;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: Yuechen Wang
 * @Date: 2016-06-22
 */
@Data
public class AdSummaryData implements Serializable {
    private static final long serialVersionUID = 6483414349389803824L;

    private Long adId;               // 广告id
    private Long totalShowPv;        // 曝光量
    private Long totalShowUv;        // 独立用户曝光量
    private Long totalClickPv;       // 点击量
    private Long totalClickUv;       // 独立用户点击量
    private String totalClickRatePv; // 点击率
    private String totalClickRateUv; // 独立用户点击率
    private Date upToDate;           // 数据截至日期
    private List<AdSummaryDetailData> detailDataList; // 详细数据列表

    public static AdSummaryData generateByAdPlatform(AdPlatform data, int sortType) {
        DecimalFormat df = new DecimalFormat("0.00");
        AdSummaryData summary = new AdSummaryData();
        summary.setAdId(data.getAdId());
        summary.setTotalShowPv(SafeConverter.toLong(data.getTotalShowPv()));
        summary.setTotalShowUv(SafeConverter.toLong(data.getTotalShowUv()));
        summary.setTotalClickPv(SafeConverter.toLong(data.getTotalClickPv()));
        summary.setTotalClickUv(SafeConverter.toLong(data.getTotalClickUv()));
        summary.setTotalClickRatePv(df.format(SafeConverter.toFloat(data.getTotalClickRatePv()) * 100.0));
        summary.setTotalClickRateUv(df.format(SafeConverter.toFloat(data.getTotalClickRateUv()) * 100.0));
        String dateVal = SafeConverter.toString(data.getUptoDate());
        summary.setUpToDate(DateUtils.stringToDate(dateVal, "yyyyMMdd"));
        List<AdSummaryDetailData> detailList = new ArrayList<>();
        for (Map<String, Object> detail : data.getDetailDataList()) {
            AdSummaryDetailData detailData = new AdSummaryDetailData();
            detailData.setAdCode(parseString(detail.get("adCode")));
            detailData.setAdId(SafeConverter.toLong(detail.get("adId")));
            detailData.setAdSlot(parseString(detail.get("adSlot")));
            dateVal = parseString(detail.get("date"));
            detailData.setDate(DateUtils.stringToDate(dateVal, "yyyyMMdd"));
            detailData.setDateStr(detailData.getDate() == null ? "-" : DateUtils.dateToString(detailData.getDate(), "MM月dd日"));
            detailData.setProvCode(SafeConverter.toInt(detail.get("provCode")));
            detailData.setProvName(parseString(detail.get("provName")));
            detailData.setCityCode(SafeConverter.toInt(detail.get("cityCode")));
            detailData.setCityName(parseString(detail.get("cityName")));
            detailData.setCountyCode(SafeConverter.toInt(detail.get("countyCode")));
            detailData.setCountyName(parseString(detail.get("countyName")));
            detailData.setShowPv(SafeConverter.toLong(detail.get("showPv")));
            detailData.setShowUv(SafeConverter.toLong(detail.get("showUv")));
            detailData.setClickPv(SafeConverter.toLong(detail.get("clickPv")));
            detailData.setClickUv(SafeConverter.toLong(detail.get("clickUv")));
            detailData.setClickRatePv(df.format(SafeConverter.toFloat(detail.get("clickRatePv")) * 100.0));
            detailData.setClickRateUv(df.format(SafeConverter.toFloat(detail.get("clickRateUv")) * 100.0));
            detailList.add(detailData);
        }
        // TODO 对这list排个序
        if (sortType == 1) {
            detailList = detailList.stream().sorted((d1, d2) -> (int) DateUtils.dayDiff(d2.getDate(), d1.getDate())).collect(Collectors.toList());
        } else {
            detailList = detailList.stream().sorted((s1, s2) -> Long.compare(s2.getShowPv(), s1.getShowPv())).collect(Collectors.toList());
        }
        summary.setDetailDataList(detailList);
        return summary;
    }

    private static String parseString(Object val) {
        String strVal = SafeConverter.toString(val, "-");
        return "NULL".equalsIgnoreCase(strVal) ? "-" : strVal;
    }
}
