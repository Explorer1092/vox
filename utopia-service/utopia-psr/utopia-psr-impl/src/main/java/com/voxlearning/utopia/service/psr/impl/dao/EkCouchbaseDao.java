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

package com.voxlearning.utopia.service.psr.impl.dao;

import com.voxlearning.alps.annotation.common.ObjectCacheKeyGenerator;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.support.PsrCacheSystem;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import com.voxlearning.utopia.service.psr.impl.util.PsrToolsEx;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/*
 * ChaoLi Lee 2014-07-01
 * couchbase 中 key 需要注意的地方：
 * 1> couchbase 中 point 类型为 word \ grammer \ topic, msyql中对应的类型为 words \ grammers \ gambits
 *    所以在 过滤 知识点时, 要将 mysql 中的gambits 替换为 topic,将words替换为word, 将grammers替换为grammer
 * 2> 因topic 中带有空格,所以入库的时候把空格转换成了下划线. " " => "_", 所以在查库的时候 要判断一下key中是否带有空格,有则替换为下划线
 */
@Slf4j
@Named
public class EkCouchbaseDao extends CouchbaseFormat {

    @Inject private PsrCacheSystem psrCacheSystem;

    public boolean addCouchbaseData(String strKey, String strValue) {
        return addCouchbaseData(strKey, strValue, 0);
    }

    public boolean addCouchbaseData(String strKey, String strValue, int expirationInSeconds) {
        if (StringUtils.isEmpty(strKey) || StringUtils.isEmpty(strValue))
            return false;

        String tmpStr = strKey.replace(" ", "_");
        return Boolean.TRUE.equals(psrCacheSystem.CBS.psr.add(ObjectCacheKeyGenerator.generate(tmpStr), expirationInSeconds, strValue));
    }

    public boolean setCouchbaseData(String strKey, String strValue) {
        return setCouchbaseData(strKey, strValue, 0);
    }

    public boolean setCouchbaseData(String strKey, String strValue, int expirationInSeconds) {
        if (StringUtils.isEmpty(strKey) || StringUtils.isEmpty(strValue))
            return false;

        String tmpStr = strKey.replace(" ", "_");
        return Boolean.TRUE.equals(psrCacheSystem.CBS.psr.set(ObjectCacheKeyGenerator.generate(tmpStr), expirationInSeconds, strValue));
    }

    public String getCouchbaseDataByKey(String strKey) {
        if (StringUtils.isEmpty(strKey))
            return null;

        String tmpStr = strKey.replace(" ", "_");
        return psrCacheSystem.CBS.psr.load(ObjectCacheKeyGenerator.generate(tmpStr));
    }


    public String formatCouchbaseKey(String strKey) {
        if (StringUtils.isEmpty(strKey))
            return null;

        if (strKey.contains(" "))
            strKey = strKey.replace(" ", "_");

        return strKey;
    }

    /********************************************************************************************/
    // 查询数据库func

    // 查询用户profile
    public UserExamContent getUserExamContentFromCouchbase(Long uid, Integer grade, Subject subject) {
        if (uid == null || subject == null)
            return null;

        String strKey = "";
        if (subject.equals(Subject.ENGLISH))
            strKey = formatCouchbaseKey("_student_" + uid + "_" + grade);
        else {
            // key : _student_math_30001_3
            String strSubject = subject.equals(Subject.MATH) ? subject.name().toLowerCase() : subject.name();
            strKey = formatCouchbaseKey("_student_" + strSubject + "_" + uid + "_" + grade);
        }

        if (subject.equals(Subject.CHINESE))
            return PsrToolsEx.decodeUserCnExamFromLine(getCouchbaseDataByKey(strKey));

        return PsrToolsEx.decodeUserExamFromLine(getCouchbaseDataByKey(strKey));
    }
    // ek_profile
    public EkRegionContent getEkRegionContentFromCouchbase(String ek, Integer grade) {
        if (StringUtils.isEmpty(ek) || grade == null)
            return null;

        String strKey = formatCouchbaseKey("_ek_" + ek + "_" + grade);
        return PsrTools.decodeEkFromLine(getCouchbaseDataByKey(strKey), ek);
    }

    // et_profile
    public EtRegionContent getEtRegionContentFromCouchbase(String et, Integer grade) {
        if (StringUtils.isEmpty(et) || grade == null)
            return null;

        String strKey = formatCouchbaseKey("_et_" + et + "_" + grade);
        return PsrTools.decodeEtFromLine(getCouchbaseDataByKey(strKey), et);
    }

    // cn_lesson_recom
    public EkToEidContent getCnLessonToEidContentFromCouchbase(String cnLesson) {
        if (StringUtils.isEmpty(cnLesson))
            return null;

        // _cnlesson_BKC_10100000253802
        String strKey = formatCouchbaseKey("_cnlesson_" + cnLesson);
        return PsrTools.decodeCnLessonFromLine(getCouchbaseDataByKey(strKey), cnLesson);
    }

    // ek_itemw_recom
    public EkToEidContent getEkToEidContentFromCouchbase(String ek, Integer grade) {
        if (StringUtils.isEmpty(ek) || grade == null)
            return null;

        // _itemw_1035002457_3
        String strKey = formatCouchbaseKey("_itemw_" + ek + "_" + grade);
        return PsrTools.decodeItemwFromLine(getCouchbaseDataByKey(strKey), ek);
    }

    // ek_item_recom, 补题逻辑暂不考虑 grade
    public EkToNewEidContent getEkToNewEidContentFromCouchbase(String ek, Integer grade) {
        if (StringUtils.isEmpty(ek))
            return null;

        // _item_1035002457
        String strKey = formatCouchbaseKey("_item_" + ek);
        return PsrTools.decodeItemFromLine(getCouchbaseDataByKey(strKey), ek);
    }

    // 适应性补题数据
    public UserExamUcContent getUserExamUcContentFromCouchbase(Long uid) {
        if (uid == null)
            return null;

        // key : _uc_30001
        String strKey = formatCouchbaseKey("_uc_" + uid);

        return PsrTools.decodeExamEnAdaptiveUcFromLine(getCouchbaseDataByKey(strKey));
    }
    // 适应性补题数据
    public UserExamUcContent getUserExamUcContentFromCouchbase(Long uid, Subject subject) {
        if (uid == null || subject == null)
            return null;

        // key : _uc_30001_math
        String strKey = formatCouchbaseKey("_uc_" + uid + "_"
                + (subject.equals(Subject.MATH) ? subject.name().toLowerCase() : subject.name())
        );

        return PsrTools.decodeExamEnAdaptiveUcFromLine(getCouchbaseDataByKey(strKey));
    }
    // 适应性补题数据
    public ExamKcContent getExamKcContentFromCouchbase(String ek) {
        if (StringUtils.isEmpty(ek))
            return null;

        // key : kc_2222
        String strKey = formatCouchbaseKey("_kc_" + ek);

        return PsrTools.decodeExamEnAdaptiveKcFromLine(getCouchbaseDataByKey(strKey));
    }

    // 适应性补题数据
    public ExamIcContent getExamIcContentFromCouchbase(String eid) {
        if (StringUtils.isEmpty(eid))
            return null;

        // key : ic_Q_2222
        String strKey = formatCouchbaseKey("_ic_" + eid);

        return PsrTools.decodeExamEnAdaptiveIcFromLine(getCouchbaseDataByKey(strKey));
    }

    // userId 的做题记录
    public PsrUserHistoryEid getPsrUserHistoryEid(Long uid, Subject subject) {
        return getPsrUserHistory(uid, false, subject);
    }
    // psr 推题记录
    public PsrUserHistoryEid getPsrUserHistoryPsrEid(Long uid, Subject subject) {
        return getPsrUserHistory(uid, true, subject);
    }
    public PsrUserHistoryEid getPsrUserHistory(Long uid, boolean isPsr, Subject subject) {
        if (uid == null || subject == null)
            return null;

        if (subject.equals(Subject.ENGLISH)) {
            String strKey = formatCouchbaseKey("history_" + uid);  // userId 的做题记录 //fixme del 线上已经取不到用户的做题记录了。
            if (isPsr)
                strKey = formatCouchbaseKey("historypsr_" + uid);  // psr 推题记录

            return PsrTools.decodeUserHistoryEidFromLine(getCouchbaseDataByKey(strKey), uid, isPsr);
        } else {
            String strSubject = subject.equals(Subject.MATH) ? subject.name().toLowerCase() : subject.name();
            String strKey = formatCouchbaseKey("history_" + uid + "_" + strSubject);  // userId 的做题记录
            if (isPsr)
                strKey = formatCouchbaseKey("historypsr_" + uid + "_" + strSubject);  // psr 推题记录

            return PsrTools.decodeUserHistoryEidFromLine(getCouchbaseDataByKey(strKey), uid, isPsr);
        }
    }

    // irt 模型参数数据
    public List<IrtLowHighStructEx> getIrtLowHighStructExFromCouchbase() {
        List<IrtLowHighStructEx> retList = new ArrayList<>();

        int lineCount = getIrtLowHighStructExNumFromCouchbase();
        for (Integer i = 0; i < lineCount; i++) {
            IrtLowHighStructEx item = getIrtLowHighStructExItemFromCouchbase(i);
            if (item == null)
                continue;
            retList.add(item);
        }
        return retList;
    }

    public Integer getIrtLowHighStructExNumFromCouchbase() {
        String key = formatCouchbaseKey("irtevaex_count");
        return PsrTools.stringToInt(getCouchbaseDataByKey(key));
    }

    public IrtLowHighStructEx getIrtLowHighStructExItemFromCouchbase(Integer count) {
        if (count == null || count < 0)
            return null;
        String key = formatCouchbaseKey("irtevaex_" + count);
        return PsrTools.decodeIrtLowHighFromLineEx(getCouchbaseDataByKey(key));
    }


    /********************************************************************************************/
    // App Math
    public UserAppMathContent getUserAppMathContentFromCouchbase(Long uid) {
        if (uid == null)
            return null;

        String strKey = formatCouchbaseKey("appmath_" + uid);
        return PsrTools.decodeUserAppMathFromLine(getCouchbaseDataByKey(strKey));
    }


    public List<MathEkEkContent> getMathEkEkContentFromCouchbase() {
        List<MathEkEkContent> retList = new ArrayList<>();

        int lineCount = getMathEkEkContentNumFromCouchbase();
        for (Integer i = 0; i < lineCount; i++) {
            MathEkEkContent item = getMathEkEkContentItemFromCouchbase(i);
            if (item == null)
                continue;
            retList.add(item);
        }
        return retList;
    }

    public Integer getMathEkEkContentNumFromCouchbase() {
        String strKey = formatCouchbaseKey("appmathekek_count");
        return PsrTools.stringToInt(getCouchbaseDataByKey(strKey));
    }

    public MathEkEkContent getMathEkEkContentItemFromCouchbase(Integer count) {
        if (count == null || count < 0)
            return null;

        String strKey = formatCouchbaseKey("appmathekek_" + count);
        return PsrTools.decodeMathEkEkFromLine(getCouchbaseDataByKey(strKey));
    }

    public MathEkEtContent getMathEkEtContentFromCouchbase(String ek) {
        if (StringUtils.isEmpty(ek))
            return null;

        String strKey = formatCouchbaseKey("appmatheket_" + ek);

        return PsrTools.decodeMathEkEtFromLine(getCouchbaseDataByKey(strKey));
    }



    /********************************************************************************************/
    // App English
    public UserAppEnContent getUserAppEnContentFromCouchbase(Long uid) {
        if (uid == null)
            return null;
        String strKey = formatCouchbaseKey("appen_" + uid);
        return PsrTools.decodeUserAppEnFromLine(getCouchbaseDataByKey(strKey));
    }


    /********************************************************************************************/
    /*
     * 根据 Ek-lsit 获取 Eid-list
     */
    public EkEidListContent getEkEidListContentFromCouchbaseByEk(String ek, Integer grade) {
        List<String> ekList = new ArrayList<>();
        ekList.add(ek);
        return (getEkEidListContentFromCouchbaseByEkList(ekList, grade));
    }

    /*
     * 根据 Ek-lsit 获取 Eid-list
     */
    public EkEidListContent getEkEidListContentFromCouchbaseByEkList(List<String> ekList, Integer grade) {
        if (ekList == null || ekList.size() <= 0 || grade == null)
            return null;

        EkEidListContent ekEidListContent = new EkEidListContent();
        List<EkToEidContent> list = new ArrayList<>();

        for (String anEkList : ekList) {
            EkToEidContent item = getEkToEidContentFromCouchbase(anEkList, grade);
            if (item == null)
                continue;
            list.add(item);
        }
        ekEidListContent.setEkList(list);
        return ekEidListContent;
    }

    /*
     * 根据 Ek-lsit 获取 Eid-list
     */
    public EkEidListContent getEkEidListContentFromCouchbaseByCnLessons(List<String> cnLessons) {
        if (CollectionUtils.isEmpty(cnLessons))
            return null;

        EkEidListContent ekEidListContent = new EkEidListContent();
        List<EkToEidContent> list = new ArrayList<>();

        for (String lesson : cnLessons) {
            EkToEidContent item = getCnLessonToEidContentFromCouchbase(lesson);
            if (item == null)
                continue;
            list.add(item);
        }
        ekEidListContent.setEkList(list);
        return ekEidListContent;
    }

}
