/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.ClazzLevel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shuai Huan on 2014/5/19.
 */
public class ClazzLevelUtil {

    public static List<KeyValuePair<Integer, String>> getClazzLevelList() {
        List<KeyValuePair<Integer, String>> clazzLevelList = ClazzLevel.toKeyValuePairs();
        List<KeyValuePair<Integer, String>> pairs = new ArrayList<>();

        Map<String, String> m = new LinkedHashMap<>();
        m.put("7", "七年级");
        m.put("8", "八年级");
        m.put("9", "九年级");
        m.put("10", "高一");
        m.put("11", "高二");
        m.put("12", "高三");

        for (KeyValuePair<Integer, String> levelItem : clazzLevelList) {
            KeyValuePair<Integer, String> pair = new KeyValuePair<>();
            pair.setKey(levelItem.getKey());
            pair.setValue(m.get(levelItem.getKey()));
            if (pair.getValue() == null)
                pair.setValue(levelItem.getValue());

            pairs.add(pair);
        }

        return pairs;
    }

    public static List<KeyValuePair<Integer, String>> getBookClazzLevelList() {
        List<KeyValuePair<Integer, String>> pairs = new ArrayList<>();

        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(1, "一年级");
        m.put(2, "二年级");
        m.put(3, "三年级");
        m.put(4, "四年级");
        m.put(5, "五年级");
        m.put(6, "六年级");
        m.put(7, "初一");
        m.put(8, "初二");
        m.put(9, "初三");
        m.put(10, "高一");
        m.put(11, "高二");
        m.put(12, "高三");

        for (Integer key : m.keySet()) {
            KeyValuePair<Integer, String> pair = new KeyValuePair<>();
            pair.setKey(key);
            pair.setValue(m.get(key));
            pairs.add(pair);
        }
        return pairs;
    }

    public static void main(String[] args) {
        System.out.println(getBookClazzLevelList());
    }
}
