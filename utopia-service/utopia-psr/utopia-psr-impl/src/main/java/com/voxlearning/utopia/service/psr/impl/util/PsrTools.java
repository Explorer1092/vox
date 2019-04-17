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

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.entity.content.UnitKnowledgePointRef;
import com.voxlearning.utopia.service.psr.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by ChaoLi Lee on 14-7-7.
 * Psr 工具类
 */
@Slf4j
public class PsrTools {

    /*
     * 获取两个List的 交集,即 不超纲数据 ,用于 判断Ek-list
     */
    public static List<String> getSameStringInList(List<String> listA, List<String> listB) {
        if (listA == null || listA.size() <= 0 || listB == null || listB.size() <= 0)
            return null;

        List<String> list = new ArrayList<>();

        for (String str : listA) {
            if (listB.contains(str))
                list.add(str);
        }

        return list;
    }

    /*
     * return: ek 对应 unitId列表
     */
    public static Map<String, Long> getEkUnitIdsFromPointRefs(List<UnitKnowledgePointRef> unitKnowledgePointRefs) {
        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
            return null;

        //Map<String, Long> retMap = new HashMap<>();
        Map<String, Long> retMap = new LinkedHashMap<>();

        String ek = "";
        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefs) {
            ek = getPointKey(unitKnowledgePointRef);
            if (StringUtils.isEmpty(ek))
                continue;

            if (!retMap.containsKey(ek)) {
                retMap.put(ek, unitKnowledgePointRef.getUnitId());
            }
        }

        return retMap;
    }

    /*
     * return: unitId 包含ek列表
     */
    public static Map<Long, List<String>> getUnitIdEksFromPointRefs(List<UnitKnowledgePointRef> unitKnowledgePointRefs) {
        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
            return null;

        //Map<Long, List<String>> retMap = new HashMap<>();
        Map<Long, List<String>> retMap = new LinkedHashMap<>();

        String ek = "";
        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefs) {
            ek = getPointKey(unitKnowledgePointRef);
            if (StringUtils.isEmpty(ek))
                continue;

            if (retMap.containsKey(unitKnowledgePointRef.getUnitId())) {
                List<String> list = retMap.get(unitKnowledgePointRef.getUnitId());
                if (list != null && !list.contains(ek))
                    list.add(ek);
            } else {
                List<String> list = new ArrayList<>();
                list.add(ek);
                retMap.put(unitKnowledgePointRef.getUnitId(), list);
            }
        }

        return retMap;
    }

    public static List<Long> getUnitIdsFromPointRefs(List<UnitKnowledgePointRef> unitKnowledgePointRefs) {
        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
            return null;

        List<Long> list = new ArrayList<>();

        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefs) {
            if (!list.contains(unitKnowledgePointRef.getUnitId()))
                list.add(unitKnowledgePointRef.getUnitId());
        }

        return list;
    }
    public static List<String> getEksFromPointRefs(List<UnitKnowledgePointRef> unitKnowledgePointRefs) {
        return getEksIdFromPointRefs(unitKnowledgePointRefs);
    }

    /*
    public static List<String> getEksFromPointRefs(List<UnitKnowledgePointRef> unitKnowledgePointRefs) {
        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
            return null;

        List<String> list = new ArrayList<>();

        String str = "";
        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefs) {

            str = getPointKey(unitKnowledgePointRef);
            if (StringUtils.isEmpty(str))
                continue;

            if (!list.contains(str))
                list.add(str);
        }

        return list;
    }
    */

    public static List<String> getEksIdFromPointRefs(List<UnitKnowledgePointRef> unitKnowledgePointRefs) {
        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
            return null;

        List<String> list = new ArrayList<>();

        String str = "";
        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefs) {

            str = getPointIdKey(unitKnowledgePointRef);
            if (StringUtils.isEmpty(str))
                continue;

            if (!list.contains(str))
                list.add(str);
        }

        return list;
    }

    public static List<String> getEksFromPointRefsByUnitId(List<UnitKnowledgePointRef> unitKnowledgePointRefs, Long unitId) {
        return getEksIdFromPointRefsByUnitId(unitKnowledgePointRefs, unitId);
    }

    /*
    public static List<String> getEksFromPointRefsByUnitId(List<UnitKnowledgePointRef> unitKnowledgePointRefs, Long unitId) {
        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
            return null;

        List<String> list = new ArrayList<>();

        String str = "";
        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefs) {
            // 如果不在单元内的知识点 过滤掉
            if (!unitKnowledgePointRef.getUnitId().equals(unitId))
                continue;
            str = getPointKey(unitKnowledgePointRef);
            if (StringUtils.isEmpty(str))
                continue;

            if (!list.contains(str))
                list.add(str);
        }

        return list;
    }
    */

    public static List<String> getEksIdFromPointRefsByUnitId(List<UnitKnowledgePointRef> unitKnowledgePointRefs, Long unitId) {
        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
            return null;

        List<String> list = new ArrayList<>();

        String str = "";
        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefs) {
            // 如果不在单元内的知识点 过滤掉
            if (!unitKnowledgePointRef.getUnitId().equals(unitId))
                continue;
            str = getPointIdKey(unitKnowledgePointRef);
            if (StringUtils.isEmpty(str))
                continue;

            if (!list.contains(str))
                list.add(str);
        }

        return list;
    }

    public static Long getFirstUnitIdFromPointRefs(List<UnitKnowledgePointRef> unitKnowledgePointRefs) {
        if (unitKnowledgePointRefs == null || unitKnowledgePointRefs.size() <= 0)
            return null;

        Long unitId = -1L;
        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefs) {
            if (unitId == -1)
                unitId = unitKnowledgePointRef.getUnitId();
            if (unitId > unitKnowledgePointRef.getUnitId())
                unitId = unitKnowledgePointRef.getUnitId();
        }

        return unitId;
    }
    public static String getFirstUnitIdFromPointsMap(Map<String, List<String>> unitsKnowledgePointsMap){
        if (unitsKnowledgePointsMap == null || unitsKnowledgePointsMap.size() <= 0)
            return null;
        String unitId = "-1";
        Set<String> keySet = unitsKnowledgePointsMap.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (unitId == "-1")
                unitId = key;
            if (unitId.compareTo(key) > 0)
                unitId = key;
        }
        return unitId;
    }

    public static String getPointKey(UnitKnowledgePointRef unitKnowledgePointRef) {
        if (unitKnowledgePointRef == null)
            return null;

        String type = unitKnowledgePointRef.getPointType().toLowerCase();

        // mysql 中为 GAMBITS, couchbase中为topic 转换一下
        if (type.equals("gambits"))
            type = "topics";

        // 数据格式化,去掉 末尾的's' 跟couchbase中的数据格式对齐
        type = type.substring(0, type.length() - 1);

        return (type + "#" + unitKnowledgePointRef.getPointName());
    }

    public static String getPointIdKey(UnitKnowledgePointRef unitKnowledgePointRef) {
        if (unitKnowledgePointRef == null)
            return null;

        // pointId == 0, 是老知识点体系,只取新知识点体系的PointId
        if (unitKnowledgePointRef.getPointId() <= 0)
            return null;

        return unitKnowledgePointRef.getPointId().toString();
    }

    public static EkRegionContent decodeEkFromLine(String strLine, String ek) {
        if (StringUtils.isEmpty(strLine) || StringUtils.isEmpty(ek))
            return null;

        String[] sRegionArr = strLine.split(";");

        if (sRegionArr.length <= 0)
            return null;

        EkRegionContent ekRegionContent = new EkRegionContent();
        ekRegionContent.setEk(ek);

        Map<Integer, EkRegionItem> ekRegionItemMap = new LinkedHashMap<>();

        for (String aSRegionArr : sRegionArr) {
            String[] sArr = aSRegionArr.split("\t");
            if (sArr.length < 5)
                continue;

            EkRegionItem ekRegionItem = new EkRegionItem();

            ekRegionItem.setAvgDifferenty(stringToDouble(sArr[1]));
            ekRegionItem.setAvgDifficulty(stringToDouble(sArr[2]));
            ekRegionItem.setEidCount(stringToInt(sArr[3]));
            ekRegionItem.setHotLevel(stringToDouble(sArr[4]));

            ekRegionItemMap.put(stringToInt(sArr[0]), ekRegionItem);
        }
        ekRegionContent.setEkRegionsInfo(ekRegionItemMap);

        return ekRegionContent;
    }

    /*
    public static EkRegionContent decodeEkFromLine(String strLine, String ek) {
        if (StringUtils.isEmpty(strLine) || StringUtils.isEmpty(ek))
            return null;

        String[] sRegionArr = strLine.split(";");

        if (sRegionArr.length <= 0)
            return null;

        EkRegionContent ekRegionContent = new EkRegionContent();
        ekRegionContent.setEk(ek);

        //Map<Integer, EkRegionItem> ekRegionItemMap = new HashMap<>();
        Map<Integer, EkRegionItem> ekRegionItemMap = new LinkedHashMap<>();

        for (String aSRegionArr : sRegionArr) {
            String[] sArr = aSRegionArr.split("\t");
            if (sArr.length < 5)
                continue;

            EkRegionItem ekRegionItem = new EkRegionItem();

            ekRegionItem.setAvgDifferenty(stringToDouble(sArr[1]));
            ekRegionItem.setAvgDifficulty(stringToDouble(sArr[2]));
            ekRegionItem.setEidCount(stringToInt(sArr[3]));
            ekRegionItem.setHotLevel(stringToDouble(sArr[4]));

            ekRegionItemMap.put(stringToInt(sArr[0]), ekRegionItem);
        }
        ekRegionContent.setEkRegionItemMap(ekRegionItemMap);

        return ekRegionContent;
    }
    */

    public static EtRegionContent decodeEtFromLine(String strLine, String et) {
        if (StringUtils.isEmpty(strLine) || StringUtils.isEmpty(et))
            return null;

        String[] sRegionArr = strLine.split(";");

        if (sRegionArr.length <= 0)
            return null;

        EtRegionContent etRegionContent = new EtRegionContent();
        etRegionContent.setEt(et);

        //Map<Integer, EtRegionItem> etRegionItemMap = new HashMap<>();
        Map<Integer, EtRegionItem> etRegionItemMap = new LinkedHashMap<>();

        double hotLevel = 0.0;

        for (String aSRegionArr : sRegionArr) {
            String[] sArr = aSRegionArr.split("\t");
            if (sArr.length < 3)
                continue;
            EtRegionItem etRegionItem = new EtRegionItem();

            etRegionItem.setEidCount(stringToInt(sArr[1]));

            hotLevel = stringToDouble(sArr[2]);
            if (hotLevel < 0 || hotLevel > 1)
                hotLevel = 0.14;
            etRegionItem.setHotLevel(hotLevel);

            etRegionItemMap.put(stringToInt(sArr[0]), etRegionItem);
        }

        etRegionContent.setEtRegionInfo(etRegionItemMap);

        return etRegionContent;
    }

    public static EkToNewEidContent decodeItemFromLine(String strLine, String ek) {
        if (StringUtils.isEmpty(strLine) || StringUtils.isEmpty(ek))
            return null;

        String[] sRegionArr = strLine.split(";");

        if (sRegionArr.length <= 0)
            return null;

        EkToNewEidContent ekToNewEidContent = new EkToNewEidContent();
        ekToNewEidContent.setEk(ek);

        List<String> list = new ArrayList<>();

        for (String sArr : sRegionArr) {
            String[] tmpArr = sArr.split("\t");
            if (tmpArr.length < 1) // error
                continue;
            list.add(tmpArr[0].trim());
        }

        ekToNewEidContent.setEidList(list);

        return ekToNewEidContent;
    }

    public static EkToEidContent decodeCnLessonFromLine(String strLine, String cnLesson) {
        if (StringUtils.isEmpty(strLine) || StringUtils.isEmpty(cnLesson))
            return null;

        String[] sRegionArr = strLine.split(";");

        if (sRegionArr.length <= 0)
            return null;

        EkToEidContent ekToEidContent = new EkToEidContent();
        ekToEidContent.setEk(cnLesson);

        List<EidItem> list = new ArrayList<>();

        for (String aSRegionArr : sRegionArr) {
            String[] sArr = aSRegionArr.split("\t");
            if (sArr.length < 8) // error
                continue;

            EidItem eidItem = new EidItem();
            eidItem.setEid(sArr[0]);
            eidItem.setAccuracyRate(stringToDouble(sArr[1]));
            eidItem.setRightCount(stringToInt(sArr[2]));
            eidItem.setAllCount(stringToInt(sArr[3]));
            eidItem.setIrtA(stringToDouble(sArr[4]));
            eidItem.setIrtB(stringToDouble(sArr[5]));
            eidItem.setIrtC(stringToDouble(sArr[6]));
            eidItem.setEt("");

            Integer grade = stringToInt(sArr[7]);

            list.add(eidItem);
        }

        ekToEidContent.setEidList(list);

        return ekToEidContent;
    }

    public static EkToEidContent decodeItemwFromLine(String strLine, String ek) {
        if (StringUtils.isEmpty(strLine) || StringUtils.isEmpty(ek))
            return null;

        String[] sRegionArr = strLine.split(";");

        if (sRegionArr.length <= 0)
            return null;

        EkToEidContent ekToEidContent = new EkToEidContent();
        ekToEidContent.setEk(ek);

        List<EidItem> list = new ArrayList<>();

        for (String aSRegionArr : sRegionArr) {
            String[] sArr = aSRegionArr.split("\t");
            if (sArr.length < 8) // error
                continue;

            EidItem eidItem = new EidItem();
            eidItem.setEid(sArr[0]);

            eidItem.setAccuracyRate(stringToDouble(sArr[1]));
            eidItem.setRightCount(stringToInt(sArr[2]));
            eidItem.setAllCount(stringToInt(sArr[3]));
            eidItem.setIrtA(stringToDouble(sArr[4]));
            eidItem.setIrtB(stringToDouble(sArr[5]));
            eidItem.setIrtC(stringToDouble(sArr[6]));
            eidItem.setEt(sArr[7]);
            Integer grade = stringToInt(sArr[8]);

            list.add(eidItem);
        }

        ekToEidContent.setEidList(list);

        return ekToEidContent;
    }

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

    public static ExamEnQuizPackageAfenti decodeQuziAfentiPackageFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        ExamEnQuizPackageAfenti examEnQuizPackageAfenti = new ExamEnQuizPackageAfenti();

        // fixme json 解析 eid, sim_eid 暂时不管
        Map<String, Object> retMap = JsonUtils.fromJson(strLine);

        Integer bookId = retMap.containsKey("book_id") ? (Integer)retMap.get("book_id") : 0;
        String quizId = retMap.containsKey("quiz_id") ? (String)retMap.get("quiz_id") : "";
        String quizStatus = retMap.containsKey("open_status") ? (String)retMap.get("open_status") : "opening";

        Long unitId = -1L;
        Integer unitLevel = -1;
        List unitIdList = retMap.containsKey("unit_id_list") ? (List)retMap.get("unit_id_list") : null;
        if (unitIdList != null && unitIdList.size() > 0) {
            Map<String,Object> mMap = (Map<String,Object>)unitIdList.get(0);
            if (unitId.equals(-1L) && mMap.containsKey("id"))
                unitId = stringToLong((String)mMap.get("id"));
            if (unitLevel.equals(-1) && mMap.containsKey("level"))
                unitLevel = (Integer)mMap.get("level");
        }

        Map<Integer/*pos*/, ExamEnAfentiQuizEidItem> quizEids = new HashMap<>();
        List quizEidsList = retMap.containsKey("quiz_eid_list") ? (List)retMap.get("quiz_eid_list") : null;
        for (Object item : quizEidsList) {
            String eid = "";
            String et = "";
            Integer pos = -1;
            Map<String,Object> mMap = (Map<String,Object>)item;
            if (mMap.containsKey("eid"))
                eid = (String)mMap.get("eid");
            if (mMap.containsKey("et"))
                et = (String)mMap.get("et");
            if (mMap.containsKey("pos"))
                pos = (Integer)mMap.get("pos");
            if (StringUtils.isEmpty(eid) || pos.equals(-1))
                continue;
            List<String> eks = mMap.containsKey("ek_list") ? (List)mMap.get("ek_list") : null;
            List<String> ekIds = mMap.containsKey("ekid_list") ? (List)mMap.get("ekid_list") : null;

            ExamEnAfentiQuizEidItem eidItem = new ExamEnAfentiQuizEidItem();

            eidItem.setEid(eid);
            eidItem.setPos(pos);
            eidItem.setEt(et);
            eidItem.setEkIds(ekIds);
            eidItem.setEks(eks);
            quizEids.put(pos, eidItem);
        }

        examEnQuizPackageAfenti.setBookId(bookId.longValue());
        examEnQuizPackageAfenti.setUnitId(unitId);
        examEnQuizPackageAfenti.setUnitLevel(unitLevel);
        examEnQuizPackageAfenti.setQuizId(quizId);
        examEnQuizPackageAfenti.setQuizStatus(quizStatus);
        examEnQuizPackageAfenti.setQuizEidMap(quizEids);

        return examEnQuizPackageAfenti;
    }

    public static UserAppEnContent decodeUserAppEnFromLine(String strLine) {
        return (decodeUserAppEnFromLine(strLine, false));
    }

    public static UserAppEnContent decodeUserAppEnFromLine(String strLine, boolean isAddDataToCouchbase) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 2)
            return null;

        UserAppEnContent userAppEnContent = new UserAppEnContent();

        if (sArr.length >= 3) {
            userAppEnContent.setVer(stringToInt(sArr[0]));
            userAppEnContent.setUserId(stringToLong(sArr[1]));
            userAppEnContent.setEkMap(getUserAppEnEkItemFromLine(sArr[2], userAppEnContent.getVer()));
        } else {
            // 插入couchbase的时候默认 数据格式的版本 ver=2
            // 线上查询的时候 默认 ver=1,兼容couchbase的旧数据
            if (isAddDataToCouchbase)
                userAppEnContent.setVer(2);
            else
                userAppEnContent.setVer(1);

            userAppEnContent.setUserId(stringToLong(sArr[0]));
            userAppEnContent.setEkMap(getUserAppEnEkItemFromLine(sArr[1], userAppEnContent.getVer()));
        }

        return userAppEnContent;
    }

    public static Map<String, UserAppEnEkItem> getUserAppEnEkItemFromLine(String strLine, int ver) {
        if (StringUtils.isEmpty(strLine))
            return null;

        //Map<String, UserAppEnEkItem> retMap = new HashMap<>();
        Map<String, UserAppEnEkItem> retMap = new LinkedHashMap<>();

        String[] strArr = strLine.split(";");
        int iItemCount = 5;
        if (ver >= 2)
            iItemCount = 6;

        // 没有 知识点信息 或者 离线数据提供的数据格式有错误
        if (strArr.length == 0 || (strArr.length % iItemCount) != 0)
            return null;

        String ek = "";
        Character status = 'E';
        Integer days = 0;
        Double accuracyRate = 0.0;
        Integer typeRight = 1;
        Integer etType = 1;

        boolean isInsertMap = false;
        for (int i = 0; i < strArr.length; i++) {
            isInsertMap = false;
            int index = i % iItemCount;
            switch (index) {
                case 0:
                    ek = strArr[i];
                    break;
                case 1:
                    status = stringToChar(strArr[i]);
                    if (status == null)
                        status = 'E';
                    break;
                case 2:
                    days = stringToInt(strArr[i]);
                    break;
                case 3:
                    accuracyRate = stringToDouble(strArr[i]);
                    break;
                case 4:
                    typeRight = stringToInt(strArr[i]);
                    if (ver < 2)
                        isInsertMap = true;
                    break;
                case 5:
                    etType = stringToInt(strArr[i]);
                    isInsertMap = true;
                    break;
                default:
                    break;
            }
            if (isInsertMap) {
                UserAppEnEkItem userAppEnEkItem = new UserAppEnEkItem();
                userAppEnEkItem.setVer(ver);
                userAppEnEkItem.setEk(ek);
                userAppEnEkItem.setStatus(status);
                userAppEnEkItem.setDays(days);
                userAppEnEkItem.setAccuracyRate(accuracyRate);
                userAppEnEkItem.setTypeRight(typeRight);
                userAppEnEkItem.setEtType(etType);
                retMap.put(ek, userAppEnEkItem);
                ek = "";
                status = 'E';
                days = 0;
                accuracyRate = 0.0;
                typeRight = 1;
                etType = 1;
            }
        }

        return retMap;
    }

    // date = 2014-12-10
    public static Date getDateFromYearMonthDay(String date) {
        if (StringUtils.isEmpty(date))
            return new Date();

        String[] strArr = date.split("-");
        if (strArr.length < 3)
            return new Date();

        return new Date(stringToInt(strArr[0]) - 1900, stringToInt(strArr[1]) - 1, stringToInt(strArr[2]));
    }

    //ver'\t'51f78798a3108d6520a3adbd,w2w,2014-10-29,0.614814813297,405,249,word#feeler,pattern#听单词或词组给单词或词组排序;51e7d250a3108d65204e29b7,w2w,2014-09-05,0.30584360128,8197,2507,word#world,pattern#听句子填空
    public static UserExamEnWrongContent decodeUserExamEnWrongFromLine(String userId, String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 2)
            return null;
        Integer ver = stringToInt(sArr[0]);
        if (!ver.equals(1) || StringUtils.isEmpty(sArr[1]))
            return null;

        String[] strArr = sArr[1].split(";");

        if (strArr.length <= 0)
            return null;

        UserExamEnWrongContent userExamEnWrongContent = new UserExamEnWrongContent();
        userExamEnWrongContent.setUserId(stringToLong(userId));
        userExamEnWrongContent.setVer(ver);

        List<UserExamEnWrongItem> userW2WList = new ArrayList<>();
        List<UserExamEnWrongItem> userW2RList = new ArrayList<>();

        Double weightSumW2R = 0.0;
        Double weightSumW2W = 0.0;

        for (String tmpLine : strArr) {
            String[] strTmpArr = tmpLine.split(",");
            if (strTmpArr.length < 8)
                continue;
            // eid type date rate sum right ek et
            UserExamEnWrongItem item = new UserExamEnWrongItem();
            item.setEid(strTmpArr[0]);
            item.setDate(getDateFromYearMonthDay(strTmpArr[2]));
            item.setRate(stringToDouble(strTmpArr[3]));
            item.setSumCount(stringToInt(strTmpArr[4]));
            item.setRightCount(stringToInt(strTmpArr[5]));
            item.setEk(strTmpArr[6]);
            item.setEt(strTmpArr[7]);

            if (strTmpArr[1].toLowerCase().equals("w2r")) {
                weightSumW2R += item.getRate();
                userW2RList.add(item);
            }
            else if (strTmpArr[1].toLowerCase().equals("w2w")) {
                weightSumW2W += item.getRate();
                userW2WList.add(item);
            }
        }

        //weightPer
        for (int i = 0; weightSumW2R > 0 && i < userW2RList.size(); i++) {
            userW2RList.get(i).setWeightPer(userW2RList.get(i).getRate() / weightSumW2R * 100);
        }
        for (int i = 0; weightSumW2W > 0 && i < userW2WList.size(); i++) {
            userW2WList.get(i).setWeightPer(userW2WList.get(i).getRate() / weightSumW2W * 100);
        }

        // 按正确率 降序排列
        final boolean finalDesc = true;  // 默认降序
        Collections.sort(userW2WList, new Comparator<UserExamEnWrongItem>() {
            @Override
            public int compare(UserExamEnWrongItem o1, UserExamEnWrongItem o2) {
                int n = 0;
                if (o2.getRate() - o1.getRate() < 0.0) {
                    n = -1;   // 降序
                    if (!finalDesc)
                        n = 1; // 升序
                } else if (o2.getRate() - o1.getRate() > 0.0) {
                    n = 1;
                    if (!finalDesc)
                        n = -1;
                }
                return n;
            }
        });
        Collections.sort(userW2RList, new Comparator<UserExamEnWrongItem>() {
            @Override
            public int compare(UserExamEnWrongItem o1, UserExamEnWrongItem o2) {
                int n = 0;
                if (o2.getRate() - o1.getRate() < 0.0) {
                    n = -1;   // 降序
                    if (!finalDesc)
                        n = 1; // 升序
                } else if (o2.getRate() - o1.getRate() > 0.0) {
                    n = 1;
                    if (!finalDesc)
                        n = -1;
                }
                return n;
            }
        });

        userExamEnWrongContent.setWrongW2WList(userW2WList);
        userExamEnWrongContent.setWrongW2RList(userW2RList);
        return userExamEnWrongContent;
    }

    /*
     * eid 做错的人数 正确率 总做题数量 总做对数量 ek et
     * ver '\t' 53fd9c00a3103f906b62de41,299,0.640552994654,868,556,word#there's,pattern#按要求写单词的各种词形变化
     */
    public static ExamEnGlobalWrongItem decodeUserExamEnGlobalWrongFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 2)
            return null;
        Integer ver = stringToInt(sArr[0]);
        if (!ver.equals(1) || StringUtils.isEmpty(sArr[1]))
            return null;

        String[] strTmpArr = sArr[1].split(",");

        if (strTmpArr.length < 7)
            return null;

        ExamEnGlobalWrongItem userExamEnGlobalWrongItem = new ExamEnGlobalWrongItem();

        // eid type date rate sum right ek et
        userExamEnGlobalWrongItem.setEid(strTmpArr[0]);
        userExamEnGlobalWrongItem.setWrongCountPerson(stringToInt(strTmpArr[1]));
        userExamEnGlobalWrongItem.setRate(stringToDouble(strTmpArr[2]));
        userExamEnGlobalWrongItem.setSumCount(stringToInt(strTmpArr[3]));
        userExamEnGlobalWrongItem.setRightCount(stringToInt(strTmpArr[4]));
        userExamEnGlobalWrongItem.setEk(strTmpArr[5]);
        userExamEnGlobalWrongItem.setEt(strTmpArr[6]);

        return userExamEnGlobalWrongItem;
    }

    public static UserAppMathContent decodeUserAppMathFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 3)
            return null;

        UserAppMathContent userAppMathContent = new UserAppMathContent();

        userAppMathContent.setUserId(stringToLong(sArr[0]));

        userAppMathContent.setEkMap(getUserAppMathEkItemFromLine(sArr[1]));
        userAppMathContent.setEtMap(getUserAppMathEtItemFromLine(sArr[2]));

        return userAppMathContent;
    }

    /*
     * point#运算定律在小数加减法中的运用;E;60;0.3;3;pattern#连减;0.2;pattern#连加;0.1;pattern#加减混合;0.0;point#小数的大小比较;E;60;0.4;1;pattern#找出几个数中最大的数;0.1;
     */
    public static Map<String, UserAppMathEkItem> getUserAppMathEkItemFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        //Map<String, UserAppMathEkItem> retMap = new HashMap<>();
        Map<String, UserAppMathEkItem> retMap = new LinkedHashMap<>();

        String[] strArr = strLine.split(";");

        // 没有 知识点信息 或者 离线数据提供的数据格式有错误
        if (strArr.length == 0)
            return null;

        int pointCount = 0;        // 总point个数
        int itemPointCount = 5;    // point 格式个数, point#***;E;30;0.5;8
        int itemPatternCount = 2;  // pattern 格式个数,pattern#***;0.5
        int patternCount = 0;      // 总pattern个数
        int curPatternCount = 0;   // 当前point对应的pattern个数
        List<UserAppMathEtItem> tmpEStatusPatternList = new ArrayList<>();

        String et = null;
        Double accuracyRateEt = 0.0;
        String ek = null;
        Character status = 'E';
        Integer days = 0;
        Double accuracyRate = 0.0;
        for (int i = 0; i < strArr.length; i++) {
            int index = 0;

            // status=E 有自己的pattern列表,其中带有pattern的难度系数
            if (curPatternCount > 0) {
                index = (i - pointCount * itemPointCount) % itemPatternCount;
                switch (index) {
                    case 0:
                        et = strArr[i];
                        break;
                    case 1:
                        patternCount++;
                        curPatternCount--;
                        accuracyRateEt = stringToDouble(strArr[i]);
                        UserAppMathEtItem userAppMathEtItem = new UserAppMathEtItem();
                        userAppMathEtItem.setEt(et);
                        userAppMathEtItem.setAccuracyRate(accuracyRateEt);

                        et = null;
                        accuracyRateEt = 0.0;

                        tmpEStatusPatternList.add(userAppMathEtItem);

                        if (curPatternCount == 0) {
                            UserAppMathEkItem userAppMathEkItem = new UserAppMathEkItem();
                            userAppMathEkItem.setEk(ek);
                            userAppMathEkItem.setStatus(status);
                            userAppMathEkItem.setDays(days);
                            userAppMathEkItem.setAccuracyRate(accuracyRate);

                            userAppMathEkItem.getEStatusEtList().addAll(tmpEStatusPatternList);

                            tmpEStatusPatternList.clear();

                            retMap.put(ek, userAppMathEkItem);

                            et = null;
                            accuracyRateEt = 0.0;
                            ek = null;
                            status = 'E';
                            days = 0;
                            accuracyRate = 0.0;
                            curPatternCount = 0;
                        }

                        break;
                    default:
                        break;
                }
            } else {
                index = (i - patternCount * itemPatternCount) % itemPointCount;
                switch (index) {
                    case 0:
                        ek = strArr[i];
                        break;
                    case 1:
                        status = stringToChar(strArr[i]);
                        if (status == null)
                            status = 'E';
                        break;
                    case 2:
                        days = stringToInt(strArr[i]);
                        break;
                    case 3:
                        accuracyRate = stringToDouble(strArr[i]);
                        break;
                    case 4:
                        pointCount++;
                        curPatternCount = stringToInt(strArr[i]);

                        // 后面还有pattern 解析解析
                        if (curPatternCount > 0)
                            break;

                        UserAppMathEkItem userAppMathEkItem = new UserAppMathEkItem();
                        userAppMathEkItem.setEk(ek);
                        userAppMathEkItem.setStatus(status);
                        userAppMathEkItem.setDays(days);
                        userAppMathEkItem.setAccuracyRate(accuracyRate);
                        retMap.put(ek, userAppMathEkItem);
                        ek = null;
                        status = 'E';
                        days = 0;
                        accuracyRate = 0.0;
                        curPatternCount = 0;
                        break;
                    default:
                        break;
                }
            } // end if

        }

        return retMap;
    }

    public static Map<String, UserAppMathEtItem> getUserAppMathEtItemFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        //Map<String, UserAppMathEtItem> retMap = new HashMap<>();
        Map<String, UserAppMathEtItem> retMap = new LinkedHashMap<>();

        String[] strArr = strLine.split(";");

        int iItemCount = 2;

        // 没有 知识点信息 或者 离线数据提供的数据格式有错误
        if (strArr.length == 0 || (strArr.length % iItemCount) != 0)
            return null;

        String et = null;
        Double accuracyRate = 0.0;
        for (int i = 0; i < strArr.length; i++) {
            int index = i % iItemCount;
            switch (index) {
                case 0:
                    et = strArr[i];
                    break;
                case 1:
                    accuracyRate = stringToDouble(strArr[i]);

                    UserAppMathEtItem userAppMathEtItem = new UserAppMathEtItem();
                    userAppMathEtItem.setEt(et);
                    userAppMathEtItem.setAccuracyRate(accuracyRate);
                    retMap.put(et, userAppMathEtItem);
                    accuracyRate = 0.0;
                    break;
                default:
                    break;
            }
        }

        return retMap;
    }

    public static MathEkEtContent decodeMathEkEtFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        int iItemCount = 2;
        String[] strArr = null;

        String[] strVerArr = strLine.split("\t");
        if (strVerArr.length >= 2) {
            iItemCount = 3;
            strArr = strVerArr[1].split(";");
        } else {
            strArr = strLine.split(";");
        }

        // 没有 知识点信息 或者 离线数据提供的数据格式有错误
        if (strArr.length == 0 || (strArr.length % iItemCount) != 0)
            return null;

        MathEkEtContent mathEkEtContent = new MathEkEtContent();

        String et = null;
        Integer level = 0;
        Integer time = 0;
        for (int i = 0; i < strArr.length; i++) {
            int index = i % iItemCount;
            switch (index) {
                case 0:
                    et = strArr[i];
                    break;
                case 1:
                    level = stringToInt(strArr[i]);
                    if (iItemCount == 2)
                        mathEkEtContent.getEtMap().put(level, new MathEtTime(et, time));
                    break;
                case 2:
                    time = stringToInt(strArr[i]);
                    mathEkEtContent.getEtMap().put(level, new MathEtTime(et, time));
                    time = 0;
                    break;
                default:
                    break;
            }
        }

        return mathEkEtContent;
    }

    public static MathEkEkContent decodeMathEkEkFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split(",");
        if (sArr.length == 0)
            return null;

        MathEkEkContent mathEkEkContent = new MathEkEkContent();
        mathEkEkContent.setEkBase(sArr[0]);
        if (sArr.length >= 2)
            mathEkEkContent.setEk(sArr[1]);

        return mathEkEkContent;
    }

    /*
    public static IrtLowHighStruct decodeIrtLowHighFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 4)
            return null;

        IrtLowHighStruct irtLowHighStruct = new IrtLowHighStruct();
        irtLowHighStruct.setTr(sArr[1]);
        if (!sArr[2].equals("None"))
            irtLowHighStruct.setLowv(stringToDouble(sArr[2]));
        if (!sArr[3].equals("None"))
            irtLowHighStruct.setHighv(stringToDouble(sArr[3]));

        return irtLowHighStruct;
    }
    */

    public static IrtLowHighStructEx decodeIrtLowHighFromLineEx(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 2)
            return null;

        IrtLowHighStructEx irtLowHighStructEx = new IrtLowHighStructEx();

        irtLowHighStructEx.setKey(sArr[0]);
        irtLowHighStructEx.setValue(stringToDouble(sArr[1]));

        return irtLowHighStructEx;
    }

    /*
     * 数据格式如下:
     * 1. 最近7天推过的数据,ver=1 or ver=0, eg 51dae4e1a31092fb6698e2f5:1;51cbe43da310006f56e80ad8:0
     * 2. ver\t最近7天推过的数据\texamination,ver=2,中间'\t'分割, eg 2\t51dae4e1a31092fb6698e2f5:1;51cbe43da310006f56e80ad8:0\t51dae4e1a31092fb6698e2f5:1:2:3;51cbe43da310006f56e80ad8:1
     * isPsr, true:psr最近七天的推题数据, false:用户的最近一个月的做题记录
     */
    public static PsrUserHistoryEid decodeUserHistoryEidFromLine(String strLine, Long userId, boolean isPsr) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String ver = "";
        String strSevenData = "";
        String strExamination = "";
        String[] sAllArr = strLine.split("\t");
        if (sAllArr.length == 1) {
            strSevenData = strLine;
        } else if (sAllArr.length == 2) {
            ver = sAllArr[0];
            strSevenData = sAllArr[1];
        } else if (sAllArr.length >= 3) {
            ver = sAllArr[0];
            strSevenData = sAllArr[1];
            strExamination = sAllArr[2];
        }

        PsrUserHistoryEid psrUserHistoryEid = new PsrUserHistoryEid();
        psrUserHistoryEid.setUserId(userId);
        Date now = new Date();

        // 最近7天推题数据/最近30天的做题结果
        String[] sArr = strSevenData.split(";");
        if (sArr.length > 0) {
            Map<String, KeyValuePair<Integer, Long>> tmpExamMap = new LinkedHashMap<>();
            Map<String, Long> tmpPsrMap = new LinkedHashMap<>();

            for (String aSArr : sArr) {
                String[] tmpArr = aSArr.split(":");
                if (tmpArr.length < 2) continue;
                if (!isPsr) {
                    Long time = now.getTime();
                    // 可以按ver判断,ver=1 or "" length=2,ver=2 length=3
                    if (tmpArr.length >= 3)
                        time = stringToLong(tmpArr[2]);
                    // 判断是否超过15天,超过15天的丢掉
                    if (now.getTime() - time >= 15 * 86400 * 1000L)
                        continue;
                    KeyValuePair<Integer, Long> pair = new KeyValuePair<>(stringToInt(tmpArr[1]), time);
                    tmpExamMap.put(tmpArr[0], pair);
                } else {
                    if (tmpArr.length < 2)
                        continue;
                    tmpPsrMap.put(tmpArr[0], stringToLong(tmpArr[1]));
                }
            }
            if (isPsr)
                psrUserHistoryEid.setEidPsrMap(tmpPsrMap);
            else
                psrUserHistoryEid.setEidMasterInfoMap(tmpExamMap);
        }

        // 历史测验数据
        if (isPsr && !StringUtils.isEmpty(strExamination)) {
            String[] sEArr = strExamination.split(";");
            if (sEArr.length > 0) {
                Map<String/*试卷Id*/, List<String/*任务Id*/>> eidExaminationMap = new HashMap<>();
                List<String/*试卷Id_任务Id*/> eidExaminationList = new ArrayList<>();
                for (String examination : sEArr) {
                    String[] sTArr = examination.split(":");
                    if (sTArr.length < 2) continue;
                    String examId = sTArr[0];
                    List<String> tasks = new ArrayList<>();
                    for (int i = 1; i < sTArr.length; i++) {
                        eidExaminationList.add(examId + "_" + sTArr[i]);
                        tasks.add(sTArr[i]);
                    }
                    eidExaminationMap.put(examId, tasks);
                }
                psrUserHistoryEid.setEidExaminationMap(eidExaminationMap);
                psrUserHistoryEid.setEidExaminationList(eidExaminationList);
            }
        }

        return psrUserHistoryEid;
    }

    /*
     * value = lowirttheta:0.0;hightrttheta:1.5;eidcountrateperek:0.4;downsetrange:0.85;basenumberforweight:100;minnotaboveleveleids:5;maxeidcount:50;
     */
    public static String encodePsrConfig(Map<String,String> mapPsrConfig) {
        if (mapPsrConfig == null || mapPsrConfig.size() <= 0)
            return null;

        String strValue = "";
        for (Map.Entry<String,String> entry : mapPsrConfig.entrySet()) {
            if (!StringUtils.isEmpty(strValue))
                strValue += ";";
            strValue += entry.getKey() + ":" + entry.getValue();
        }

        return strValue;
    }

    /*
     * key = psr_config
     * value = lowirttheta:0.0;hightrttheta:1.5;eidcountrateperek:0.4;downsetrange:0.85;basenumberforweight:100;minnotaboveleveleids:5;maxeidcount:50;
     */
    public static Map<String,String> decodePsrConfigFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split(";");
        if (sArr.length == 0)
            return null;
        //Map<String,String> retMap = new HashMap<>();
        Map<String,String> retMap = new LinkedHashMap<>();

        for (String aArr : sArr) {
            String[] tmpArr = aArr.split(":");
            if (tmpArr.length != 2)
                continue;

            retMap.put(tmpArr[0], tmpArr[1]);
        }

        return retMap;
    }

    /*
     * key = "groupusertask_count"
     * value = "ver[\t]userid1:taskid1;userid2:taskid2"
     * count [0...n]
     */
    public static Map<String,String> decodeGroupUserTaskFromLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;

        String[] sArr = strLine.split("\t");
        if (sArr.length < 2)
            return null;
        String ver = sArr[0];
        String[] taskArr = sArr[1].split(";");
        if (taskArr.length == 0)
            return null;

        Map<String,String> retMap = new HashMap<>();
        for (String aArr : taskArr) {
            if (StringUtils.isEmpty(aArr))
                continue;
            String[] tmpArr = aArr.split(":");
            if (tmpArr.length != 2)
                continue;

            retMap.put(tmpArr[0], tmpArr[1]);
        }

        return retMap;
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
