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

package com.voxlearning.utopia.service.psr.impl.util;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.psr.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by ChaoLi Lee on 14-7-7.
 * Psr 工具类
 */
@Slf4j
public class PsrToolsEx {

    public static UserExamContent decodeUserExamFromLine(String strLine) {
        return decodeUserExamFromLine(strLine, null);
    }

    public static UserExamContent decodeUserCnExamFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 4)
            return null;

        UserExamContent userExamContent = new UserExamContent();

        //String ver = sArr[0];

        int index = 0;
        userExamContent.setUType(sArr[index++]);
        userExamContent.setUserId(stringToLong(sArr[index++]));
        userExamContent.setIrtTheta(stringToDouble(sArr[index++]));
        userExamContent.setGrade(stringToInt(sArr[index++]));

        userExamContent.setClassId(0);
        userExamContent.setSchoolId(0);
        userExamContent.setRegionCode(0);
        userExamContent.setEkList(new ArrayList<>());

        return userExamContent;
    }

    public static UserExamContent decodeUserExamFromLine(String strLine, String type) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 8)
            return null;

        UserExamContent userExamContent = new UserExamContent();

        String ver = "1";
        if (sArr.length == 8)
            ver = "1";
        else
            ver = sArr[0];

        int index = 0;

        switch (ver) {
            case "1":
                index = 0;
                if (!StringUtils.equals("insert", type))
                    return null; // 不兼容老版本的数据格式
                break;
            case "2":
                index = 1;
                break;
            default:
                return null;
        }

        userExamContent.setUType(sArr[index++]);
        userExamContent.setUserId(stringToLong(sArr[index++]));
        userExamContent.setIrtTheta(stringToDouble(sArr[index++]));
        userExamContent.setEkList(getUserEkContentFromLine(sArr[index++]));
        userExamContent.setClassId(stringToInt(sArr[index++]));
        userExamContent.setSchoolId(stringToInt(sArr[index++]));
        userExamContent.setRegionCode(stringToInt(sArr[index++]));
        userExamContent.setGrade(stringToInt(sArr[index++]));
        //return null;
        return userExamContent;
    }

    public static List<UserEkContent> getUserEkContentFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        List<UserEkContent> retList = new ArrayList<>();
        String[] strArr = strLine.split(";");
        int iItemCount = 4;

        // 没有该Class/Student的知识点信息 或者 离线数据提供的数据格式有错误
        if (strArr.length == 0 || (strArr.length % iItemCount) != 0)
            return null;

        String ek = null;
        double master = 0.0;
        short count = 0;
        short level = 0;
        for (int i = 0; i < strArr.length; i++) {
            int index = i % iItemCount;
            switch (index) {
                case 0:
                    ek = strArr[i];
                    break;
                case 1:
                    master = stringToDouble(strArr[i]);
                    break;
                case 2:
                    count = stringToShort(strArr[i]);
                    break;
                case 3:
                    level = stringToShort(strArr[i]);
                    if (level < 1 || level > 4)
                        level = 1;

                    UserEkContent userEkContent = new UserEkContent();
                    userEkContent.setEk(ek);
                    // 根据count 降低 master 的程度,从而影响 曝光频率(count越高再次推荐的次数越低)
                    if (count > 1)
                        master = master * (2.0/(count+1.0));
                    userEkContent.setMaster(master);
                    userEkContent.setCount(count);
                    userEkContent.setLevel(level);
                    retList.add(userEkContent);
                    ek = null;
                    master = 0.0;
                    count = 0;
                    level = 0;
                    break;
                default:
                    break;
            }
        }

        return retList;
    }

    // en_id_adaptive.dat
    public static UserExamUcContent decodeExamEnAdaptiveUcFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 7)
            return null;

        UserExamUcContent userExamUcContent = new UserExamUcContent();
        userExamUcContent.setType(sArr[0]);
        userExamUcContent.setUserId(stringToLong(sArr[1]));
        userExamUcContent.setUc(stringToDouble(sArr[2]));
        userExamUcContent.setDayAccuracyRate(stringToDouble(sArr[3]));
        userExamUcContent.setAllAccuracyRate(stringToDouble(sArr[4]));
        userExamUcContent.setDayCount(stringToInt(sArr[5]));
        userExamUcContent.setAllCount(stringToInt(sArr[6]));

        return userExamUcContent;
    }

    // en_id_adaptive.dat
    public static ExamKcContent decodeExamEnAdaptiveKcFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 4)
            return null;

        ExamKcContent examKcContent = new ExamKcContent();
        examKcContent.setType(sArr[0]);
        examKcContent.setEk(sArr[1]);
        examKcContent.setAccuracyRate(stringToDouble(sArr[2]));
        examKcContent.setCount(stringToInt(sArr[3]));

        return examKcContent;
    }

    // en_id_adaptive.dat
    public static ExamIcContent decodeExamEnAdaptiveIcFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 5)
            return null;

        ExamIcContent examIcContent = new ExamIcContent();
        examIcContent.setType(sArr[0]);
        examIcContent.setEid(sArr[1]);
        examIcContent.setAccuracyRate(stringToDouble(sArr[2]));
        examIcContent.setCount(stringToInt(sArr[3]));
        examIcContent.setEType(sArr[4]);

        return examIcContent;
    }

    // eid1:similar1;eid2:similar2
    public static Map<String, Double> decodeSimilarEids(String strValue) {
        if (StringUtils.isEmpty(strValue))
            return Collections.emptyMap();

        Map<String, Double> retMap = new HashMap<>();

        String[] items = strValue.split(";");
        if (items.length <= 0)
            return Collections.emptyMap();

        for (String item : items) {
            String[] eidInfo = item.split(":");
            if (eidInfo.length < 2)
                continue;
            retMap.put(eidInfo[0], PsrTools.stringToDouble(eidInfo[1]));
        }

        return retMap;
    }

    public static String encodeSimilarEids(Map<String, Double> eidsInfo) {
        if (eidsInfo == null || eidsInfo.size() <= 0)
            return null;

        String retStr = "";
        for (Map.Entry<String,Double>  entry : eidsInfo.entrySet()) {
            retStr += entry.getKey() + ":" + entry.getValue() + ";";
        }

        return retStr;
    }

    public static String encodeKpProficiency(Map<String/*bookid*/,Map<String/*kpid*/,Double>> data) {
        if (MapUtils.isEmpty(data))
            return null;

        Integer bookNum = 0;

        String value = "";
        for (String bookId : data.keySet()) {
            Map<String, Double> kpProficiency = data.get(bookId);
            if (MapUtils.isEmpty(kpProficiency))
                continue;
            value += bookId + "=";
            for (String kp : kpProficiency.keySet()) {
                Double prof = kpProficiency.get(kp);
                value += kp + ":" + prof.toString() + ";";
            }
            value += "\t";

            if (bookNum++ >= 5)
                break;
        }
        return value;
    }

    public static Map<String/*bookid*/,Map<String/*kpid*/,Double>> decodeKpProficiency(String value) {
        if (StringUtils.isBlank(value))
            return Collections.emptyMap();

        String[] bookArr = value.split("\t");
        if (bookArr.length <= 0)
            return Collections.emptyMap();

        Map<String, Map<String, Double>> retMap = new HashMap<>();
        for (String bookinfo : bookArr) {
            String[] bookItem = bookinfo.split("=");
            if (bookItem.length < 2)
                continue;

            String bookId = bookItem[0];
            String[] kpItem = bookItem[1].split(";");
            if (kpItem.length <= 0)
                continue;

            Map<String/*kpid*/,Double> kpMap = new HashMap<>();
            for (String kpProf : kpItem) {
                String[] item = kpProf.split(":");
                if (item.length < 2)
                    continue;

                String kp = item[0];
                Double prof = SafeConverter.toDouble(item[1], 0.2D);
                if (!kpMap.containsKey(kp))
                    kpMap.put(kp, prof);
            }

            if (!retMap.containsKey(bookId))
                retMap.put(bookId, kpMap);
        }

        return retMap;
    }

    public static short stringToShort(String str) {
        short ret = 0;

        if (StringUtils.isEmpty(str))
            return ret;

        try {
            ret = Short.valueOf(str);
        } catch (NumberFormatException e) {
            ret = 0;
        }
        return ret;
    }

    public static int stringToInt(String str) {
        int ret = 0;

        if (StringUtils.isEmpty(str))
            return ret;

        try {
            ret = Integer.valueOf(str);
        } catch (NumberFormatException e) {
            ret = 0;
        }
        return ret;
    }

    public static long stringToLong(String str) {
        long ret = 0;

        if (StringUtils.isEmpty(str))
            return ret;

        try {
            ret = Long.valueOf(str);
        } catch (NumberFormatException e) {
            ret = 0;
        }
        return ret;
    }

    public static double stringToDouble(String str) {
        double ret = 0.0;

        if (StringUtils.isEmpty(str))
            return ret;

        try {
            ret = Double.valueOf(str);
        } catch (NumberFormatException e) {
            ret = 0.0;
        }
        return ret;
    }
    public static Character stringToChar(String str) {
        if (StringUtils.isEmpty(str))
            return null;

        Character retC = null;

        char[] chars = str.toCharArray();
        if (chars.length > 0)
            retC = chars[0];

        return retC;
    }
}
