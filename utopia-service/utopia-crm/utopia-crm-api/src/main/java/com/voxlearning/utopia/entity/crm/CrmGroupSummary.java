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

package com.voxlearning.utopia.entity.crm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2015/10/9.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_group_summary_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.D)
@DocumentIndexes({
        @DocumentIndex(def = "{'groupId':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'teacherId':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'schoolLevel':1}", background = true)
})
@UtopiaCacheRevision("201708041")
public class CrmGroupSummary implements Serializable {
    private static final long serialVersionUID = 7604428528829474415L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;
    private Boolean disabled;

    private Long groupId;                          // 班组ID
    private Long clazzId;                          // 班级ID
    private Integer clazzLevel;                    // 年级
    private String clazzName;                      // 组所属班级的名称(clazz.formalizeClazzName()方法取出来的名字)
    private Long teacherId;                        // 老师ID
    private Long schoolId;                         // 学校ID
    private String schoolName;                     // 学校名称
    private String schoolLevel;                    // 学校阶段
    private Integer regStuCount;                   // 累计注册学生数
    private Integer authStuCount;                  // 累计认证学生数


    // 17作业
    private Integer tmHwSc;                        // 当月布置所有作业套数(当前group)
    private Integer tmTgtHwSc;                     // 当月布置指定作业套数(当前group)
    private Long latestHwTime;                     // 最近一次布置作业日期(当前group)
    private Integer finCsHwEq1AuStuCount;          // 认证学生当月完成1套当前科目作业学生数  =1
    private Integer finCsHwEq2AuStuCount;          // 认证学生当月完成2套当前科目作业学生数  =2
    private Integer finCsHwGte3AuStuCount;         // 认证学生当月完成3套及以上当前科目作业学生数  >=3 认证学生当前科目月活数
    private Integer finSglSubjHwGte3AuStuCount;    // 认证学生当月完成3套及以上任一科目作业学生数  >=3 认证学生单科月活数
    private Integer finHwEq1UaStuCount;            // 未认证学生累计完成1套当前group当前科目作业学生数
    private Integer finHwEq2UaStuCount;            // 未认证学生累计完成2套当前group当前科目作业学生数
    private Integer finHwGte3UaStuCount;           // 未认证学生累计完成3套及以上当前group当前科目作业学生数


    // 快乐学
    private Integer stuKlxTnCount;                 // 考号数
    private Integer tmScanTpCount;                 // 当月扫描试卷数(当前group)
    private Long latestScanTpTime;                 // 最近一次扫描试卷日期
    private Integer tmCsAnshEq1StuCount;           // 当月答题卡作答1次当前科目试卷学生数  =1
    private Integer tmCsAnshGte2StuCount;          // 当月答题卡作答2次及以上当前科目试卷学生数  >=2

    private Boolean vacationHwFlag;                // 是否布置了假期作业包（仅针对小学数据）


//// ============================================================================================================================================================

//    private Integer mode;   // 模式 1: 17模式  2:快乐学模式   delete by wangsong 20170801

    //学生数据
//    private Map<Long, CrmGroupStudentInfo> studentList;//学生列表  17作业学生信息   delete by wangsong 20170801
//    // 快乐学学生数据
//    private Map<String, CrmKlxGroupStudentInfo> klxStudentList; // 快乐学学生列表   delete by wangsong 20170801

    // 作业数据
//    private Integer monthHwCount;      // 本月布置所有作业数量    delete by wangsong 20170710
//    private Integer monthValidHwCount; // 本月布置指定作业数量   delete by wangsong 20170801

    //--------------------------- 2017/06/19 新增是否布置暑假作业-----------------------//
    @Deprecated
    public Boolean summerVacation;

    // ------------------------------------------------------------------------------------------------
    // Alex 20160711
    // 以下是老字段，为了兼容历史数据所以保留下来，新的数据生产逻辑可以忽略以下字段
    // ------------------------------------------------------------------------------------------------
//    @Deprecated private Integer studentTotalCount;          //学生总数
//    @Deprecated private Integer studentNoUseCount;          //未使用学生数
//    @Deprecated private Integer studentNoAuthCount;         //使用单未认证老师数
//    @Deprecated private Integer studentAuthedCount;         //已认证学生数
//    @Deprecated private Map<String, String> studentNoUseList;        // 未使用学生列表 Map key=StudentId value=StudentName
//    @Deprecated private Map<String, String> studentNoAuthList;       // 未使用学生列表 Map key=StudentId value=StudentName
//    @Deprecated private Map<String, String> studentAuthedList;       // 已认证学生列表 Map key=StudentId value=StudentName
//    @Deprecated private Map<String, String> studentHcaActiveList;       // 本月高覆盖学生列表 Map key=StudentId value=StudentName
//    @Deprecated private Map<String, String> studentDoubleSubjectAuthedList;       // 双科认证学生列表 Map key=StudentId value=StudentName
//    @Deprecated private Integer validAuthHwCount;//学生认证有效布置作业数
//    @Deprecated private Integer validHcaHwCount;//本月高覆盖作业数

    public static String ck_tid(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(CrmGroupSummary.class, "TID", teacherId);
    }

    public static String ck_gid(Long groupId) {
        return CacheKeyGenerator.generateCacheKey(CrmGroupSummary.class, "GID", groupId);
    }

    public static String ck_cid(Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(CrmGroupSummary.class, "CID", clazzId);
    }

    public static String ck_sid(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(CrmGroupSummary.class, "SID", schoolId);
    }

    public String formalizeClazzName() {
        ClazzLevel clazzLevel = ClazzLevel.parse(this.getClazzLevel());
        if (clazzLevel == null) {
            clazzLevel = ClazzLevel.getDefaultClazzLevel();
        }
        if (clazzLevel == ClazzLevel.PRIVATE_GRADE) {
            return defaultString(this.getClazzName());
        }
        return clazzLevel.getDescription() + defaultString(this.getClazzName());

    }
    @JsonIgnore
    public String formatClazzLevelName() {
        ClazzLevel clazzLevel = ClazzLevel.parse(this.getClazzLevel());
        if (clazzLevel == null) {
            clazzLevel = ClazzLevel.getDefaultClazzLevel();
        }
        return clazzLevel.getDescription();
    }

//    @JsonIgnore
//    public boolean isStudentAuthed(Long studentId) {
////        if (studentList == null || !studentList.containsKey(studentId)) {
////            return false;
////        }
////
////        CrmGroupStudentInfo studentInfo = studentList.get(studentId);
////        return studentInfo != null && (studentInfo.getEnglishauthed() || studentInfo.getMathauthed());
//        return false;
//    }


    private String defaultString(String data) {
        return data == null ? "" : data;
    }

//    public Map<String, String> fetchStudentAuthedList() {
////        if (studentList == null || studentList.size() == 0) {
////            return Collections.emptyMap();
////        }
////        return studentList.entrySet().stream()
////                .filter(e -> e.getValue() != null && e.getValue().hasAuthed())
////                .collect(Collectors.toMap(k -> String.valueOf(k.getKey()), v -> v.getValue().getStudentname(), (s1, s2) -> s1));
//        return Collections.emptyMap();
//
//    }


    // -----------------------  20170112  wangsong ------------------------//

//    private Integer lmHwCount;//上月布置所有作业个数（同组同老师）   delete by wangsong 20170801
//    private Integer lmHwSc;// 上月布置所有作业套数（同组同老师）  delete by wangsong 20170710
//    private Integer auStuCsMauc;//认证学生当前科目月活数   delete by wangsong 20170801
//    private Integer auStuCsPtMauc;// 认证学生当前科目月活可挖掘量  delete by wangsong 20170710
//    private Integer auStuCsMancDf;//认证学生当前科目月活日浮   delete by wangsong 20170710

    // 快乐学部分的字段
//    private Integer lmScanCsTpCount;//老师上月扫描此group试卷数   delete by wangsong 20170710
//    private Integer tmCsAnshStuCount;//学生当月答题卡作答此科目试卷学生使用数   delete by wangsong 20170801

//    private Integer lmCsAnshStuCount;//学生上月答题卡作答此科目试卷学生使用数   delete by wangsong 20170710
//    private Integer lmCsAnshEq2StuCount;//学生上月答题卡作答2次此科目试卷学生使用数   delete by wangsong 20170710
//    private Integer tmCsAnshLt2StuCount;//当月答题卡作答少于2次此科目试卷学生考号数（可挖掘量）   delete by wangsong 20170710
//    private Integer csAnshEq2StuDf;//学生当月答题卡作答2次此科目试卷学生使用数日浮   delete by wangsong 20170710
//    private Integer csAnshStuDf;//学生当月答题卡作答此科目试卷学生使用数日浮  delete by wangsong 20170710


}
