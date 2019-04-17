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


import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.*;

import java.io.Serializable;

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
@DocumentCollection(collection = "vox_school_summary_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.D)
@DocumentIndexes({
        @DocumentIndex(def = "{'schoolId':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'countyCode':1,'schoolName':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'countyCode':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'cityCode':1,'schoolName':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'cityCode':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'provinceCode':1,'schoolName':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'provinceCode':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'schoolLevel':1}", background = true)
})
@UtopiaCacheRevision("20170830")
public class CrmSchoolSummary implements Serializable {
    private static final long serialVersionUID = 524234832446248573L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;
    private Boolean disabled;

    private Long schoolId;                             // 学校ID
    private String schoolName;                         // 学校全称
    private SchoolLevel schoolLevel;                   // 学校阶段

    private Integer provinceCode;                      // 省份CODE
    private String provinceName;                       // 省份名称
    private Integer cityCode;                          // 城市CODE
    private String cityName;                           // 城市名称
    private Integer countyCode;                        // 地区CODE
    private String countyName;                         // 地区名称

    private Long latestVisitTime;                      // 最近一次拜访日期
    private Boolean monthVisited;                      // 本月是否拜访过

    //学生数据
    private Integer studentTotalCount;                 // 累计注册学生数
    private Integer studentAuthedCount;                // 累计认证学生数

    private Integer tmIncRegStuCount;                  // 本月新增注册学生数
    private Integer tmIncAuthStuCount;                 // 本月新增认证学生数
    private Integer regEngTeaCount;                    // 累计注册英语老师数(排除假老师、虚拟老师)
    private Integer authEngTeaCount;                   // 累计认证英语老师数(排除假老师、虚拟老师)
    private Integer regMathTeaCount;                   // 累计注册数学老师数(排除假老师、虚拟老师)
    private Integer authMathTeaCount;                  // 累计认证数学老师数(排除假老师、虚拟老师)
    private Integer regChnTeaCount;                    // 累计注册语文老师数(排除假老师、虚拟老师)
    private Integer authChnTeaCount;                   // 累计认证语文老师数(排除假老师、虚拟老师)


    // 17 作业部分
    private Integer finEngHwEq1AuStuCount;             // 认证学生当月完成1套英语作业学生数  =1
    private Integer finEngHwEq2AuStuCount;             // 认证学生当月完成2套英语作业学生数  =2
    private Integer finEngHwGte3AuStuCount;            // 认证学生当月完成3套及以上英语作业学生数 >=3 认证学生英语月活数

    private Integer finMathHwEq1AuStuCount;            // 认证学生当月完成1套数学作业学生数  =1
    private Integer finMathHwEq2AuStuCount;            // 认证学生当月完成2套数学作业学生数  =2
    private Integer finMathHwGte3AuStuCount;           // 认证学生当月完成3套及以上数学作业学生数  >=3 认证学生数学月活数

    private Integer finChnHwEq1AuStuCount;             // 认证学生当月完成1套语文作业学生数  =1
    private Integer finChnHwEq2AuStuCount;             // 认证学生当月完成2套语文作业学生数  =2
    private Integer finChnHwGte3AuStuCount;            // 认证学生当月完成3套及以上语文作业学生数 >=3 认证学生语文月活数

    private Integer finSglSubjHwEq1AuStuCount;         // 认证学生当月完成1套任一科目作业学生数  =1
    private Integer finSglSubjHwEq2AuStuCount;         // 认证学生当月完成2套任一科目作业学生数  =2
    private Integer finSglSubjHwGte3AuStuCount;        // 认证学生当月完成3套及以上任一科目作业学生数 >=3 认证学生单科月活数

    private Integer finSglSubjHwGte3IncAuStuCount;     // 认证学生当月完成3套及以上任一科目作业学生数（新增）
//    @Deprecated
//    private Integer finSglSubjHwGte3BfAuStuCount;      // 认证学生当月完成3套及以上任一科目作业学生数（回流）
    private Integer finSglSubjHwGte3LtBfAuStuCount;    // 认证学生当月完成3套及以上任一科目作业学生数（长回）
    private Integer finSglSubjHwGte3StBfAuStuCount;    // 认证学生当月完成3套及以上任一科目作业学生数（短回）


    // 快乐学部分
    private Integer stuKlxTnCount;                     // 学生快乐学考号数

    // 数学
    private Integer finMathAnshEq1StuCount;            // 当月作答1次数学试卷学生数  =1
    private Integer finMathAnshEq1IncStuCount;         // 当月作答1次数学试卷学生数（新增）
    private Integer finMathAnshEq1BfStuCount;          // 当月作答1次数学试卷学生数（回流）
    private Integer finMathAnshGte2StuCount;           // 当月作答2次及以上数学试卷学生数  >=2
    private Integer finMathAnshGte2IncStuCount;        // 当月作答2次及以上数学试卷学生数（新增）
    private Integer finMathAnshGte2BfStuCount;         // 当月作答2次及以上数学试卷学生数（回流）

    // 英语
    private Integer finEngAnshEq1StuCount;             // 当月作答1次英语试卷学生数
    private Integer finEngAnshGte2StuCount;            // 当月作答2次及以上英语试卷学生数
    private Integer finEngAnshGte2IncStuCount;         // 当月作答2次及以上英语试卷学生数（新增）
    private Integer finEngAnshGte2BfStuCount;          // 当月作答2次及以上英语试卷学生数（回流）

    // 物理
    private Integer finPhyAnshEq1StuCount;             // 当月作答1次物理试卷学生数
    private Integer finPhyAnshGte2StuCount;            // 当月作答2次及以上物理试卷学生数
    private Integer finPhyAnshGte2IncStuCount;         // 当月作答2次及以上物理试卷学生数（新增）
    private Integer finPhyAnshGte2BfStuCount;          // 当月作答2次及以上物理试卷学生数（回流）

    // 化学
    private Integer finCheAnshEq1StuCount;             // 当月作答1次化学试卷学生数 
    private Integer finCheAnshGte2StuCount;            // 当月作答2次及以上化学试卷学生数
    private Integer finCheAnshGte2IncStuCount;         // 当月作答2次及以上化学试卷学生数（新增）
    private Integer finCheAnshGte2BfStuCount;          // 当月作答2次及以上化学试卷学生数（回流）

    // 生物
    private Integer finBiolAnshEq1StuCount;            // 当月作答1次生物试卷学生数 
    private Integer finBiolAnshGte2StuCount;           // 当月作答2次及以上生物试卷学生数
    private Integer finBiolAnshGte2IncStuCount;        // 当月作答2次及以上生物试卷学生数（新增）
    private Integer finBiolAnshGte2BfStuCount;         // 当月作答2次及以上生物试卷学生数（回流）

    // 语文
    private Integer finChnAnshEq1StuCount;             // 当月作答1次语文试卷学生数
    private Integer finChnAnshGte2StuCount;            // 当月作答2次及以上语文试卷学生数
    private Integer finChnAnshGte2IncStuCount;         // 当月作答2次及以上语文试卷学生数（新增）
    private Integer finChnAnshGte2BfStuCount;          // 当月作答2次及以上语文试卷学生数（回流）

    // 历史
    private Integer finHistAnshEq1StuCount;            // 当月作答1次历史试卷学生数 
    private Integer finHistAnshGte2StuCount;           // 当月作答2次及以上历史试卷学生数
    private Integer finHistAnshGte2IncStuCount;        // 当月作答2次及以上历史试卷学生数（新增）
    private Integer finHistAnshGte2BfStuCount;         // 当月作答2次及以上历史试卷学生数（回流）

    // 地理
    private Integer finGeogAnshEq1StuCount;            // 当月作答1次地理试卷学生数
    private Integer finGeogAnshGte2StuCount;           // 当月作答2次及以上地理试卷学生数
    private Integer finGeogAnshGte2IncStuCount;        // 当月作答2次及以上地理试卷学生数（新增）
    private Integer finGeogAnshGte2BfStuCount;         // 当月作答2次及以上地理试卷学生数（回流）

    // 政治
    private Integer finPolAnshEq1StuCount;             // 当月作答1次政治试卷学生数
    private Integer finPolAnshGte2StuCount;            // 当月作答2次及以上政治试卷学生数
    private Integer finPolAnshGte2IncStuCount;         // 当月作答2次及以上政治试卷学生数（新增）
    private Integer finPolAnshGte2BfStuCount;          // 当月作答2次及以上政治试卷学生数（回流）


    //// ============================================================================================================================================================

//    @Deprecated private Integer authStatus;                        // 认证状态  该字段即将废除，在201708月份大数据将不再提供该字段，可直接从School对象中获取认证状态
//    @DocumentField("cName") private String cname;           //school short name delete by wangsong 20170710
//    //校园大使
//    private List<Long> ambassadorList;       //校园大使Id列表  delete by wangsong 20170710

    //老师数据
//    private List<Long> teacherTotalList;          //所有老师列表  delete by wangsong 20170710
    // 2017/02/06删除  wangsong
//    private List<Long> teacherNoUseList;          //未使用老师列表
//    private List<Long> teacherNoAuthList;         //使用但未认证老师列表
//    private List<Long> teacherAuthedList;         //已认证老师列表  delete by wangsong 20170710

    // 业绩相关数据
//    private Integer monthSasc;          // 本月单活数量
//    private Integer monthDasc;          // 本月双活数量
//    private Integer potentialSasc;      // 本月单活可挖数量
//    private Integer potentialDasc;      // 本月双活可挖数量  delete by wangsong 20170710
//    private Integer potentialRsc;       // 未注册学生数量
//    private Integer yesterdaySasc;      // 昨日单活日浮
//    private Integer yesterdayDasc;      // 昨日双活日浮


    // ------------------------------------------------------------------------------------------------
    // Alex 20160711
    // 以下是老字段，为了兼容历史数据所以保留下来，新的数据生产逻辑可以忽略以下字段
    // ------------------------------------------------------------------------------------------------
//    @Deprecated private Integer studentNoUseCount;          //未使用学生数
//    @Deprecated private Integer studentNoAuthCount;         //使用单未认证老师数
//    @Deprecated private Integer studentDoubleAuthedCount;  //双科认证学生数
//    @Deprecated private Integer studentHcaActiveCount;         //高覆盖地区结算数
//    @Deprecated private Integer studentDoubleSubjectAuthedCount;         //双科认证数
//    @Deprecated private Boolean isNewSchool;     //是否是学校=根据学期来划分,学期的节点由产品提出,每个学期得修改日期
    //班级数据
//    @Deprecated private List<Long> clazzIdList;     //班级id列表
//
//    public AuthenticationState niceAuthStatus() {
//        return AuthenticationState.safeParse(this.authStatus);
//    }

    public static String ck_sid(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(CrmSchoolSummary.class, "SID", schoolId);
    }


// -------------------------------       新加字段  by wangsong  20140111 -----------------------------------

//    @Deprecated private Integer auStuEngMauc;//认证学生英语月活数   Active quantity   delete by wangsong 20170801
//    @Deprecated private Integer lmAuStuEngMauc;//上月认证学生英语月活数   delete by wangsong 20170801
//    @Deprecated private Integer auStuMathMauc;//认证学生数学月活数   delete by wangsong 20170801
//    @Deprecated private Integer lmAuStuMathMauc;//上月认证学生数学月活数   delete by wangsong 20170801
//    private Integer auStuEngPtMauc;//认证学生英语月活可挖掘量  potential Active quantity 潜在活跃数  delete by wangsong 20170710
//    private Integer auStuMathPtMauc;//认证学生数学月活可挖掘量  delete by wangsong 20170710
//    private Integer engMaucDf;//昨日英活日浮  delete by wangsong 20170710
//    private Integer mathMaucDf;//昨日数活日浮  delete by wangsong 20170710


//    private Integer tmUsedAuEngTeaCount;//认证英语老师当月布置英语作业老师使用数  delete by wangsong 20170710
//    private Integer lmUsedAuEngTeaCount;//认证英语老师上月布置英语作业老师使用数  delete by wangsong 20170710


//    private Integer tmUsedAuMathTeaCount;//认证数学老师当月布置数学作业老师使用数   delete by wangsong 20170710
//    private Integer lmUsedAuMathTeaCount;//认证数学老师上月布置数学作业老师使用数   delete by wangsong 20170710



//    @Deprecated private Integer finTgtHwEq1UaStuCount;//未认证学生累计完成1套作业学生数	指定作业   delete by wangsong 20170801
//    @Deprecated private Integer finTgtHwEq2UaStuCount;//未认证学生累计完成2套作业学生数	指定作业   delete by wangsong 20170801
//    @Deprecated private Integer finHwGte3UaStuCount;//未认证学生累计完成3套及以上作业学生数   delete by wangsong 20170801


    //快乐学O2O
//    private List<Long> qbManagers;//校本题库管理员列表    delete by wangsong 20170710
//    private List<Long> subjectLeaders;//学科组长列表    delete by wangsong 20170710

//    private Integer tmEstTpMathTeaCount;//创建试卷当月数学老师使用数   delete by wangsong 20170710
//    private Integer lmEstTpMathTeaCount;//创建试卷上月数学老师使用数   delete by wangsong 20170710
//    private Integer tmScanTpMathTeaCount;//扫描完成当月数学老师使用数   delete by wangsong 20170710
//    private Integer lmScanTpMathTeaCount;//扫描完成上月数学老师使用数   delete by wangsong 20170710

//    private Integer lmFinMathAnshEq1StuCount;//上月答题卡作答1次数学试卷学生使用数  =1  delete by wangsong 20170710

//    @Deprecated private Integer lmFinMathAnshGte2StuCount;//上月答题卡作答2次及以上数学试卷学生使用数	大于等于2次   delete by wangsong 20170801
//    private Integer tmFinMathAnshLt2StuCount;//当月答题卡作答少于2次数学试卷学生考号数（可挖掘量）  delete by wangsong 20170710
//    private Integer finMathAnshGte2StuDf;//昨日答题卡作答2次及以上数学试卷学生使用数日浮  大于等于2  delete by wangsong 20170710
//    private Integer finMathAnshStuDf;//昨日答题卡作答数学试卷学生使用数日浮  大于等于1  delete by wangsong 20170710




}
