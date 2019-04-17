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

package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.spi.common.RandomProvider;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class UserAppEnEkItem implements Serializable {

    private static final long serialVersionUID = -8893888866832049524L;

    private int ver;
    private String ek;
    /**
     * 状态 E D C B A S，S为最高级 掌握状态
     */
    private Character status;
    private int days;
    /**
     * 准确率
     */
    private double accuracyRate;
    /**
     * 根据此数据可以计算出 某个题型 最近是否作对
     */
    private int typeRight;
    /**
     * 该ED对应的题型列表(四会\三会\俩会)，第二个版本中加入的(ver=2)
     */
    private int etType;
    /**
     * 综合权重
     */
    private double weight;
    /**
     * 归一化权重，综合权重/权重总和，用于轮盘选知识点 [ 0 - 100 ]
     */
    private double weightPer;

    public UserAppEnEkItem() {
        ver = 1;
        status = 'E';
        days = 0;
        accuracyRate = 0.0;
        typeRight = 1;
        etType = 1;
        weight = 0.0;
        weightPer = 0.0;
    }

    public double getStatusWeight() {
        double ret = 1.0;
        switch (status) {
            case 'E':
                ret = 1.0;
                break;
            case 'D':
                ret = 0.8;
                break;
            case 'C':
                ret = 0.6;
                break;
            case 'B':
                ret = 0.4;
                break;
            case 'A':
                ret = 0.2;
                break;
            case 'S':
                ret = 0.1;
                break;
            default:
                ret = 1.0;
        }
        // todo 100 可配置
        return ret * 100;
    }

    // todo 40 可配置
    public double getAccuracyRateWeight() {
        return (40 * (1 - accuracyRate));
    }

    public double getOutOfDaysWeight() {
        double ret = 0.0;
        int outDays = 0;

        switch (status) {
            case 'D':
                outDays = 2;
                break;
            case 'C':
                outDays = 3;
                break;
            case 'B':
                outDays = 7;
                break;
            case 'A':
                outDays = 14;
                break;
            case 'S':
            case 'E':
            default:
                outDays = 0;
        }
        // todo 40 可配置
        if (days > outDays) ret = 40;

        return ret;
    }

    public String getRandomPattern() {
        Random random = RandomProvider.getRandom();
        return getRandomPattern(random);
    }

    public String getRandomPattern(Random random) {
        if (random == null) random = RandomProvider.getRandom();

        Map<Integer, String> mapPattern = new HashMap<Integer, String>();

        mapPattern.put(0, "pattern#单词辨识;");
        mapPattern.put(1, "pattern#听音选词;");
        mapPattern.put(2, "pattern#看图识词;");
        mapPattern.put(3, "pattern#单词拼写;");

        return mapPattern.get(random.nextInt(10000) % mapPattern.size());
    }

    public String getPattern() {
        String retStr = "pattern#单词辨识;";
        List<String> list = getPatternList();
        if (list != null && list.size() > 0) retStr = list.get(0);

        return retStr;
    }

    // 当天推过的题型 不在推荐
    public String getPattern(List<String> rmEts) {
        if (rmEts == null || rmEts.size() <= 0) return getPattern();

        String retStr = "pattern#单词辨识;";
        List<String> list = getPatternList();
        if (list == null || list.size() <= 0) return retStr;
        for (String et : list) {
            if (!rmEts.contains(et)) {
                retStr = et;
                break;
            }
        }
        return retStr;
    }

    // 当天推过的题型 不在推荐
    public String getRandomPattern(List<String> rmEts) {
        Random random = RandomProvider.getRandom();
        return getRandomPattern(rmEts, random);
    }

    // 当天推过的题型 不在推荐
    public String getRandomPattern(List<String> rmEts, Random random) {
        if (random == null) random = RandomProvider.getRandom();
        if (rmEts == null || rmEts.size() <= 0) return getRandomPattern(random);

        String retStr = "pattern#单词辨识;";
        Map<Integer, String> mapPattern = new HashMap<>();
        int index = 0;

        if (!rmEts.contains("pattern#单词辨识;"))
            mapPattern.put(index++, "pattern#单词辨识;");
        if (!rmEts.contains("pattern#听音选词;"))
            mapPattern.put(index++, "pattern#听音选词;");
        if (!rmEts.contains("pattern#看图识词;"))
            mapPattern.put(index++, "pattern#看图识词;");
        if (!rmEts.contains("pattern#单词拼写;"))
            mapPattern.put(index++, "pattern#单词拼写;");

        if (mapPattern.size() <= 0) return retStr;

        retStr = mapPattern.get(random.nextInt(10000) % mapPattern.size());

        return retStr;
    }

    // 获取用户不会题型的列表
    public List<String> getPatternList() {
        // 数据出问题了
        if (typeRight < 1 || typeRight > 210) return null;

        Map<Integer, String> patterns = new HashMap<>();
        patterns.put(2, "pattern#单词辨识;");
        patterns.put(3, "pattern#单词拼写;");
        patterns.put(5, "pattern#看图识词;");
        patterns.put(7, "pattern#听音选词;");

        List<String> retList = new ArrayList<>();

        if (ver < 2) {
            // 控制用户不会题型的顺序,
            if (typeRight % 2 != 0) retList.add(patterns.get(2));
            if (typeRight % 7 != 0) retList.add(patterns.get(7));
            if (typeRight % 5 != 0) retList.add(patterns.get(5));
            if (typeRight % 3 != 0) retList.add(patterns.get(3));
            // 数据出问题了
            return (retList.size() <= 0) ? null : retList;
        }

        // 数据出问题了
        if (etType < 1 || etType > 210) return null;

        // 需要掌握的题型列表
        List<Integer> patternNeedMaster = new ArrayList<>();
        if (etType % 2 == 0) patternNeedMaster.add(2);
        if (etType % 3 == 0) patternNeedMaster.add(3);
        if (etType % 5 == 0) patternNeedMaster.add(5);
        if (etType % 7 == 0) patternNeedMaster.add(7);

        // 数据出问题了
        if (patternNeedMaster.size() <= 0) return null;

        // 控制用户不会题型的顺序,
        if (patternNeedMaster.contains(2) && typeRight % 2 != 0) retList.add(patterns.get(2));
        if (patternNeedMaster.contains(7) && typeRight % 7 != 0) retList.add(patterns.get(7));
        if (patternNeedMaster.contains(5) && typeRight % 5 != 0) retList.add(patterns.get(5));
        if (patternNeedMaster.contains(3) && typeRight % 3 != 0) retList.add(patterns.get(3));

        // 数据出问题了
        if (retList.size() <= 0) {
            // 获取第一个et
            retList.add(patterns.get(patternNeedMaster.get(0)));
        }

        return retList;
    }
}
