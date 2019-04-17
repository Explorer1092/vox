/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 第一步：通过create与Init方法进行基本配置初始化（必须操作）
 * 第二步：通过addXAxisKey与addSerieName进行数据项信息初始化
 * 第三步：通过addData增加数据
 * 第四部：通过toResult进行数据输出
 * Sample:<p>{
 * "baseInfo":{"subTitle":"SubTitle Text","title":"Title Text","yAxisTitle":"yAxisTitle Text"},
 * "names":{"show_pv":"展示pv","click_pv":"点击pv","show_uv":"展示uv","click_uv":"点击uv"},
 * "series":{"show_pv":[0,0],"click_pv":[2,96],"show_uv":[0,0],"click_uv":[2,24]},
 * "keys":["show_pv","click_pv","show_uv","click_uv"],
 * "xAxisList":["13:00","14:00"]}
 * }</p>
 *
 * @author peng
 * @Description 生成higtchars数据
 * @since 16-7-4
 */
@Data
public class HighchartsUtil {

    @Data
    private class SerieDataList implements Serializable {
        private static final long serialVersionUID = 51380261946159771L;
        private Map<String, Integer> nodeData;

        SerieDataList() {
            nodeData = new HashMap<>();
        }

        SerieDataList addData(String key, int num) {
            nodeData.put(key, num);
            return this;
        }

        int getData(String key) {
            Integer integer = nodeData.get(key);
            return SafeConverter.toInt(integer, 0);
        }
    }

    private String title;                       //图表大标题
    private String subTitle;                    //图表顶部小标题
    private String yAxisTitle;                  //y轴标题
    private Set<String> xAxisKeys;              //x轴key List
    private Map<String, String> serieNames;     //每个数据项对应的名称name
    private Set<String> serieKeys;              //每个数据项对应的关键字key
    private Map<String, SerieDataList> series;  //每个数据项的详细数据
    private Map<String, List<Double>> rates;    //数据项的%

    private boolean isAddXAxisKey = true;       //按照添加数据，是否自动保存x坐标key
    private boolean isXAxisSort = true;         //输出数据是否需要通过x坐标进行排序，反之按照添加顺序排序
    private boolean isSortAsc = true;           //数据是否按照x坐标递增序列排序，反之递减

    private DecimalFormat df = new DecimalFormat("##.##%");

    private HighchartsUtil() {
        xAxisKeys = new LinkedHashSet<>();
        serieNames = new HashMap<>();
        serieKeys = new HashSet<>();
        series = new HashMap<>();
        rates = new HashMap<>();
    }

    public HighchartsUtil closeAddXAxisKey() {
        isAddXAxisKey = false;
        return this;
    }

    public HighchartsUtil closeXAxisSort() {
        isAddXAxisKey = false;
        return this;
    }

    public static HighchartsUtil create() {
        HighchartsUtil highchartsUtil = new HighchartsUtil();
        return highchartsUtil;
    }

    public HighchartsUtil init(String title, String subTitle, String yAxisTitle) {
        this.title = title;
        this.subTitle = subTitle;
        this.yAxisTitle = yAxisTitle;
        return this;
    }

    public HighchartsUtil init(String title, String subTitle, String yAxisTitle, List<String> xAxis) {
        this.title = title;
        this.subTitle = subTitle;
        this.yAxisTitle = yAxisTitle;
        if (CollectionUtils.isEmpty(xAxis)) {
            this.xAxisKeys.addAll(xAxis);
        }
        return this;
    }

    private HighchartsUtil addXAxisKey(String key) {
        if (StringUtils.isNotEmpty(key) && !xAxisKeys.contains(key)) {
            xAxisKeys.add(key);
        }
        return this;
    }

    public HighchartsUtil addXAxisKey(List<String> xAxis) {
        if (CollectionUtils.isNotEmpty(xAxis)) {
            this.xAxisKeys.addAll(xAxis);
        }
        return this;
    }

    public HighchartsUtil addSerieName(String key, String name) {
        if (StringUtils.isNotEmpty(key)) {
            serieNames.put(key, name);
            serieKeys.add(key);
            if (!series.containsKey(key)) {
                SerieDataList serieDataList = new SerieDataList();
                series.put(key, serieDataList);
            }
        }
        return this;
    }

    /**
     * add data　可以被覆盖
     *
     * @param seriesKey NotNull 数据项key
     * @param xAxisKey  NotNull x轴横坐标key
     * @param num       　　　NotNull defaule 0
     * @return
     */
    public HighchartsUtil addData(String seriesKey, String xAxisKey, int num) {
        if (StringUtils.isNotEmpty(seriesKey) && StringUtils.isNotEmpty(xAxisKey)) {
            if (!serieKeys.contains(seriesKey)) {
                addSerieName(seriesKey, "");
            }
            if (isAddXAxisKey && !xAxisKeys.contains(xAxisKey)) {
                addXAxisKey(xAxisKey);
            }

            SerieDataList serieDataList = series.get(seriesKey);
            if (serieDataList == null) {
                serieDataList = new SerieDataList();
                series.put(seriesKey, serieDataList);
            }
            serieDataList.addData(xAxisKey, num);
        }
        return this;
    }

    public HighchartsUtil calRate(String seriesKey, String divisor, String dividend) {
        if (StringUtils.isAnyBlank(seriesKey, divisor, dividend)) {
            return this;
        }
        if (serieKeys.contains(dividend) && serieKeys.contains(divisor)) {
            if (!rates.containsKey(seriesKey)) {
                rates.put(seriesKey, new ArrayList<>());
            }

            SerieDataList dividendList = series.get(dividend);
            SerieDataList divisorList = series.get(divisor);

            List<String> xAxisList;
            if (isXAxisSort && isSortAsc) {
                xAxisList = xAxisKeys.stream().sorted(String::compareTo).collect(Collectors.toList());
            } else if (isXAxisSort) {
                xAxisList = xAxisKeys.stream().sorted((s1, s2) -> (s2.compareTo(s1))).collect(Collectors.toList());
            } else {
                xAxisList = new ArrayList<>(xAxisKeys);
            }

            xAxisList.forEach((p) -> {
                int dividendData = dividendList.getData(p);
                int divisorData = divisorList.getData(p);
                double rate =  0.0;
                if (divisorData != 0) {
                  rate = new BigDecimal(dividendData * 100.0).divide(new BigDecimal(divisorData), 2, RoundingMode.HALF_UP).doubleValue();
                }
                rates.get(seriesKey).add(rate);
            });

        }
        return this;
    }

    public Map<String, Object> toResult() {

        Map<String, Object> result = new HashMap<>();
        List<String> xAxisList;
        if (isXAxisSort && isSortAsc) {
            xAxisList = xAxisKeys.stream().sorted(String::compareTo).collect(Collectors.toList());
        } else if (isXAxisSort && !isSortAsc) {
            xAxisList = xAxisKeys.stream().sorted((s1, s2) -> (s2.compareTo(s1))).collect(Collectors.toList());
        } else {
            xAxisList = xAxisKeys.stream().collect(Collectors.toList());
        }

        Map<String, List<Integer>> seriesMap = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        Map<String, String> baseInfo = new HashMap<>();

        serieKeys.forEach((p) -> {
            List<Integer> numList = getRow(p, xAxisList);
            seriesMap.put(p, numList);
            names.put(p, serieNames.get(p));
        });

        baseInfo.put("title", title);
        baseInfo.put("subTitle", subTitle);
        baseInfo.put("yAxisTitle", yAxisTitle);
        result.put("series", seriesMap);
        result.put("names", names);
        result.put("rates", rates);
        result.put("keys", serieKeys);
        result.put("baseInfo", baseInfo);
        result.put("xAxisList", xAxisList);
        return result;
    }

    private List<Integer> getRow(String serieKey, List<String> xAxisList) {

        List<Integer> list = new ArrayList<>();
        xAxisList.forEach((p) -> {
            SerieDataList serieDataList = series.get(serieKey);
            if (serieDataList == null) {
                list.add(0);
            } else {
                list.add(serieDataList.getData(p));
            }
        });
        return list;
    }

    public static void main(String arg[]) {
        System.out.println(MapMessage.errorMessage().set("hh",
                HighchartsUtil.create().addData("serieKey", "xAxisKey", 0).toResult()));
    }
}