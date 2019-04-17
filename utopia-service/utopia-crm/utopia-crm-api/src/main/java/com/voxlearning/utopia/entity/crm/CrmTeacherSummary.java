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
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 老师信息汇总
 *
 * @author Alex
 * @version 0.1
 * @since 2015-07-01
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_teacher_summary_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.D)
@DocumentIndexes({
        @DocumentIndex(def = "{'teacherId':1,'registerTime':-1}", background = true, unique = true),
        @DocumentIndex(def = "{'mobile':1}", background = true),
        @DocumentIndex(def = "{'countyCode':1,'authTime':1}", background = true),
        @DocumentIndex(def = "{'countyCode':1,'unusualStatus':1}", background = true),
        @DocumentIndex(def = "{'countyCode':1,'lifeCycleStatus':1}", background = true),
        @DocumentIndex(def = "{'countyCode':1,'registerTime':1}", background = true),
        @DocumentIndex(def = "{'countyCode':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'cityCode':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'provinceCode':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'schoolId':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'schoolLevel':1}", background = true)
})
@UtopiaCacheRevision("20180509")
public class CrmTeacherSummary implements Serializable {
    private static final long serialVersionUID = -8614835715523321312L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;
    private Boolean disabled;

    private Long teacherId;                             // 老师ID
    // 个人信息
    private String realName;                            // 老师姓名


    @DocumentField("mobile")
    private String sensitiveMobile;                     // 电话
    private Long registerTime;                          // 注册时间
    private String webSourceCategory;                   // 注册方式


    private String authStatus;                          // 认证状态
    private Long authTime;                              // 认证时间
    private Boolean autoAuthPostPoned;                  // 自动认证未通过

    // 地区
    private Integer provinceCode;                       // 省份CODE
    private String provinceName;                        // 省份名称
    private Integer cityCode;                           // 城市CODE
    private String cityName;                            // 城市名称
    private Integer countyCode;                         // 地区CODE
    private String countyName;                          // 地区名称

    private Long schoolId;                              // 学校ID
    private String schoolName;                          // 学校名称
    private String schoolLevel;                         // 学校阶段
    private String subject;                             // 科目

    private Boolean authCond1Reached;                   // 老师认证条件1, 8人3次作业是否达成
    private Boolean authCond2Reached;                   // 老师认证条件2, 绑定手机是否达成
    private Boolean authCond3Reached;                   // 老师认证条件3, 3名同学绑定手机是否达成
    private Long authCond4Reached;                      // 老师认证条件4, 老师在某一个班组内最大布置作业个数
    private Long authCond5Reached;                      // 老师认证条件5, 老师在某一个班组内最大检查作业个数

    private Integer totalHomeworkCount;                 // 布置作业总次数
    private Boolean fakeTeacher;                        // 是否是假老师
    private String validationType;                      // 排假类型
    private String fakeDesc;                            // 排假说明

//    private List<String> unusualStatus;                 // 标签 - 异常状态
    private Long mainAccount;                           // 主账号ID，如果账号本身就是主账号，那么 mainAccount = null
    private Long latestVisitTime;                       // 最近一次拜访日期
    private Boolean monthVisited;                       // 本月是否拜访过
    private Integer groupCount;                         // 带班数量(班组数量)
    private Integer allStudentCount;                    // 学生总数
    private Integer authStudentCount;                   // 认证学生总数


    // 17作业
    private Integer tmHwSc;                             // 当月布置所有作业套数 this month homework set count
    private Integer tmTgtHwSc;                          // 当月布置指定作业套数
    private Integer last30DaysHwSc;                     // 近30天布置所有作业套数
    private Long latestAssignHomeworkTime;              // 最近一次布置作业时间
    private Integer finCsHwEq1AuStuCount;               // 认证学生当月完成1套当前科目作业学生数  =1
    private Integer finCsHwEq2AuStuCount;               // 认证学生当月完成2套当前科目作业学生数  =2
    private Integer finCsHwGte3AuStuCount;              // 认证学生当月完成3套及以上当前科目作业学生数  >=3 认证学生当前科目月活数
    private Integer finSglSubjHwGte3AuStuCount;         // 认证学生当月完成3套及以上任一科目作业学生数  >=3 认证学生单科月活数
    private Integer finHwEq1UaStuCount;                 // 未认证学生累计完成1套当前group当前科目作业学生数
    private Integer finHwEq2UaStuCount;                 // 未认证学生累计完成2套当前group当前科目作业学生数
    private Integer finHwGte3UaStuCount;                // 未认证学生累计完成3套及以上当前group当前科目作业学生数



    // 快乐学
    private Integer stuKlxTnCount;                      // 考号数
    private Integer tmEstTpCount;                       // 当月创建试卷数 this month establish test paper count;
    private Integer tmScanTpCount;                      // 当月扫描试卷数
    private Long latestEstTpTime;                       // 最近一次创建试卷日期
    private Long latestScanTpTime;                      // 最近一次扫描试卷时间
    private Integer tmCsAnshEq1StuCount;                // 当月答题卡作答1次当前科目试卷学生数  =1
    private Integer tmCsAnshGte2StuCount;               // 当月答题卡作答2次及以上当前科目试卷学生数  >=2

    private Integer vacationHwGroupCount;               // 老师布置假期作业的班组数
    private Integer maxSameSubjAuTeaCountInClass;       // 老师所在各个班级中同科目的认证老师最大值


    //// ============================================================================================================================================================

    //private Gender gender;                          // 老师性别
//    private String gender;                            // 老师性别   delete by wangsong 20170710
//    private String birthday;                          // 生日   delete by wangsong 20170710
//    private Boolean isBind;                           //手机号是否绑定   delete by wangsong 20170710

//    @DocumentField("email")
//    private String sensitiveEmail;                    // 邮箱   delete by wangsong 20170710

//    @DocumentField("qq")
//    private String sensitiveQq;                       // qq号码   delete by wangsong 20170710

//    private Integer level;                            // 等级  delete by wangsong 20170710
//    private Integer levelScore;                       // 等级分数   delete by wangsong 20170710

//    // 注册
//    private String webSource;                         // 注册方式   delete by wangsong 20170710

    //private CrmTeacherWebSourceCategoryType webSourceCategory; // 注册方式


    // 认证
    // private AuthenticationState authStatus;           // 认证状态

//    private String authType;                          // 认证方式   delete by wangsong 20170710

//    // 微信
//    private Boolean wechatBinded;                     // 是否绑定微信   delete by wangsong 20170710
//    private Long wechatBindTime;                      // 微信绑定时间   delete by wangsong 20170710

//    private String schoolShortName;                   // 老师所在学校名简称   delete by wangsong 20170710
    // private SchoolType schoolType;                    // 老师所在学校类型
//    private String schoolType;                        // 老师所在学校类型  delete by wangsong 20170710
    // private SchoolLevel schoolLevel;                  // 老师所在学校级别

    // private Subject subject;                          // 老师学科

//    private String shippingAddress;                   // 通讯地址  delete by wangsong 20170710

    // 校园大使
//    private Boolean ambassador;                       // 是否为校园大使
//    private Long ambassadorTime;                      // 成为校园大使时间    delete by wangsong 20170710

    // 相关老师
//    private Long invitorId;                          // 原始邀请老师ID    delete by wangsong 20170710
//    private String invitorName;                      // 原始邀请老师姓名    delete by wangsong 20170710
//    private Long inviteTime;                         // 邀请时间    delete by wangsong 20170710
//    private String inviteType;                       // 邀请方式    delete by wangsong 20170710

    // 相关班级和学生
//    @Deprecated private Long firstCreateClazzTime;                // 首次建班时间   delete by wangsong 20170801
//    private List<Long> clazzList;                     // 老师的班级列表    delete by wangsong 20170710
//    @DocumentField("groupList") private List<Long> groupIdList;                   // 老师的组的id列表  delete by wangsong 20170710


    // 作业信息

//    @Deprecated private Long firstAssignHomeworkTime;             // 第一次布置作业时间   delete by wangsong 20170801
//    @Deprecated private Long firstCheckHomeworkTime;              // 第一次检查作业时间   delete by wangsong 20170801
//
//    @Deprecated private Long latestCheckHomeworkTime;             // 最近一次检查作业时间   delete by wangsong 20170801
//    @Deprecated private Integer day7HomeworkCount;                // 最近7天布置作业次数  delete by wangsong 20170710
//    @Deprecated private Integer day30HomeworkCount;               // 最近30天布置作业次数  delete by wangsong 20170710
//    private Integer monthHwCount;                     // 本月布置所有作业数量  delete by wangsong 20170710
//    @Deprecated private Integer monthValidHwCount;                // 本月布置指定作业数量   delete by wangsong 20170801

    // 奖励
//    private Integer rewardIntegralSum;                 // 历史获得金币量  delete by wangsong 20170710
//    private Integer rewardIntegralCount;               // 历史获得金币次数  delete by wangsong 20170710
//    private Integer consumeIntegralSum;                // 历史消耗金币量  delete by wangsong 20170710
//    private Integer consumeIntegralCount;              // 历史消耗金币次数  delete by wangsong 20170710
//    private Integer rewardChargingSum;                 // 历史获得话费总额 delete by wangsong 20170710
//    private Integer rewardChargingCount;               // 历史获得话费总次数 delete by wangsong 20170710

    // 任务
//    private Integer taskCount;                     // 关联任务数  delete by wangsong 20170710
//    private Integer taskRecordCount;               // 关联记录数  delete by wangsong 20170710
//    private Long latestWorkTime;                   // 最近维护时间   delete by wangsong 20170710
//    private String latestWorkExecutor;             // 最近维护人员姓名(id)形式   delete by wangsong 20170710





    // 标签 - 使用状态
//    private String usageStatus;  // 使用状态 注册未建班 建班未使用 使用未检查 使用检查-未认证 已认证     delete by wangsong 20170710


//    private Map<String, Object> extensionAttributes; // delete by wangsong 20170710

    // 主副账号

//    private List<Long> subAccountList;  // 副账号列表    delete by wangsong 20170710

    // 业绩相关
//    private Integer englishSasc;         // 认证学生英语月活数    delete by wangsong 20170710
//    private Integer mathSasc;            // 认证学生数学月活数    delete by wangsong 20170710



    // ------------------------------------------------------------------------------------------------
    // Alex 20160711
    // 以下是老字段，为了兼容历史数据所以保留下来，新的数据生产逻辑可以忽略以下字段
    // 开发上也不要使用以下字段
    // ------------------------------------------------------------------------------------------------

//    @Deprecated private Integer basicHomeworkCount;               // 布置一般作业次数     delete by wangsong 20170710
//    @Deprecated private Long firstAssignBasicHomeworkTime;        // 第一次布置一般作业时间     delete by wangsong 20170710
//    @Deprecated private Long firstCheckBasicHomeworkTime;         // 第一次检查一般作业时间     delete by wangsong 20170710
//    @Deprecated private Long latestAssignBasicHomeworkTime;       // 最近一次布置一般作业时间    delete by wangsong 20170710
//    @Deprecated private Integer quizHomeworkCount;               // 布置测验作业次数    delete by wangsong 20170710
//    @Deprecated private Long firstAssignQuizHomeworkTime;        // 第一次布置测验作业时间   delete by wangsong 20170710
//    @Deprecated private Long firstCheckQuizHomeworkTime;         // 第一次布置测验作业时间   delete by wangsong 20170710
//    @Deprecated private Long latestAssignQuizHomeworkTime;       // 最近一次布置测验作业时间  delete by wangsong 20170710
//    @Deprecated private Integer clazzCount;                       // 班级总数
//    @Deprecated private Integer useStudentCount;                  // 使用学生总数
//    @Deprecated private Integer hasParentStudentCount;            // 绑定家长的学生数量
//    @Deprecated private Integer sameSchoolInviteCount;           // 同校邀请人数   delete by wangsong 20170801
//    @Deprecated private Integer diffSchoolInviteCount;           // 已校邀请人数   delete by wangsong 20170801
//    @Deprecated private Integer activeTeacherCount;              // 老师唤醒老师的唤醒人数   delete by wangsong 20170801

    public CrmTeacherSummary initializeIfNecessary() {
        if (getDisabled() == null) setDisabled(Boolean.FALSE);
//        if (getSameSchoolInviteCount() == null) setSameSchoolInviteCount(0);
//        if (getDiffSchoolInviteCount() == null) setDiffSchoolInviteCount(0);
//        if (getActiveTeacherCount() == null) setActiveTeacherCount(0);
//        if (getClazzCount() == null) setClazzCount(0);
        if (getAllStudentCount() == null) setAllStudentCount(0);
//        if (getUseStudentCount() == null) setUseStudentCount(0);
        if (getAuthStudentCount() == null) setAuthStudentCount(0);
//        if (getHasParentStudentCount() == null) setHasParentStudentCount(0);
        if (getTotalHomeworkCount() == null) setTotalHomeworkCount(0);
//        if (getBasicHomeworkCount() == null) setBasicHomeworkCount(0);
//        if (getQuizHomeworkCount() == null) setQuizHomeworkCount(0);
//        if (getDay7HomeworkCount() == null) setDay7HomeworkCount(0);
//        if (getDay30HomeworkCount() == null) setDay30HomeworkCount(0);
//        if (getRewardIntegralSum() == null) setRewardIntegralSum(0);
//        if (getRewardIntegralCount() == null) setRewardIntegralCount(0);
//        if (getConsumeIntegralSum() == null) setConsumeIntegralSum(0);
//        if (getConsumeIntegralCount() == null) setConsumeIntegralCount(0);
//        if (getRewardChargingSum() == null) setRewardChargingSum(0);
//        if (getRewardChargingCount() == null) setRewardChargingCount(0);
//        if (getTaskCount() == null) setTaskCount(0);
//        if (getTaskRecordCount() == null) setTaskRecordCount(0);
        if (getAuthCond1Reached() == null) setAuthCond1Reached(Boolean.FALSE);
        if (getAuthCond2Reached() == null) setAuthCond2Reached(Boolean.FALSE);
        if (getAuthCond3Reached() == null) setAuthCond3Reached(Boolean.FALSE);
        if (getFakeTeacher() == null) setFakeTeacher(Boolean.FALSE);
//        if (getExtensionAttributes() == null) setExtensionAttributes(new LinkedHashMap<>());
        return this;
    }

    public String getSubjectValue() {
        return Subject.ofWithUnknown(subject).getValue();
    }

    public int getAuthState() {
        if (authStatus == null) {
            return 0;
        }
        AuthenticationState state = AuthenticationState.valueOf(authStatus);
        return state == null ? 0 : state.getState();
    }

    public AuthenticationState getAuthStateEnum() {
        if (authStatus == null) {
            return null;
        }
        AuthenticationState state = AuthenticationState.valueOf(authStatus);
        return state;
    }

    // 判断是否是真老师， 真老师的话返回true
    public boolean getManualFakeTeacher() {
        return Boolean.FALSE.equals(fakeTeacher) || !CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(validationType);
    }

    public static String ck_tid(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(CrmTeacherSummary.class, "TID", teacherId);
    }

    public static String ck_mob(String mobile) {
        return CacheKeyGenerator.generateCacheKey(CrmTeacherSummary.class, "MOB", mobile);
    }

    public static String ck_sid(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(CrmTeacherSummary.class, "SID", schoolId);
    }

    @JsonIgnore
    public boolean isNotManualFakeTeacher() {
        return !Boolean.TRUE.equals(getFakeTeacher())
                || !CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(getValidationType());
    }

    @JsonIgnore
    public boolean isKlxTeacher() {
        return "HIGH".equals(schoolLevel) || ("MIDDLE".equals(schoolLevel) && !"ENGLISH".equals(subject) && !"MATH".equals(subject));
    }



    // -----------------------------------  20170112  by wangsong -----------------------//


//    private Integer lmHwCount;//上月布置所有作业个数 last month homework times    delete by wangsong 20170710
//    private Integer lmHwSc;//上月布置所有作业套数 last month homework set count   delete by wangsong 20170801
//    private Integer auStuCsPtMauc;//认证学生当前科目月活可挖掘量	当前科目，以班组科目为准，待确认   auth student  current subject Potential month active user count;    delete by wangsong 20170710
//    private Integer csMaucDf;//昨日当前科目日浮  current subject Day float    delete by wangsong 20170710



//    private Integer lmAuStuCsMauc;//上月认证学生当前科目月活数


//    private Boolean isQbManager;//是否为校本题库管理员    delete by wangsong 20170710
//    private Boolean isSubjectLeader; //是否为学科组长    delete by wangsong 20170710


//    private Integer lmEstTpCount;//老师上月创建试卷数     delete by wangsong 20170710

//    private Integer lmScanTpCount;//老师上月扫描试卷数     delete by wangsong 20170710

//    private Integer lmCsAnshEq1StuCount;//上月答题卡作答1次该科目试卷学生使用数    delete by wangsong 20170710

//    private Integer lmCsAnshGte2StuCount;//上月答题卡作答2次及以上该科目试卷学生使用数   delete by wangsong 20170801
//    private Integer tmCsAnshLt2StuCount;//当月答题卡未作答2次该科目试卷学生考号数（可挖掘量）   delete by wangsong 20170710
//    private Integer csAnshEq2StuDf;//昨日答题卡作答2次该科目试卷学生使用数日浮   delete by wangsong 20170710
//    private Integer csAnshStuDf;//昨日答题卡作答该科目试卷学生使用数日浮   delete by wangsong 20170710
//    private Boolean isAuth;//是否认证（快乐学）	认证？实名？其他？   delete by wangsong 20170710
//    private Long authTime;//认证时间        上面定义的有了
//    private Integer authType;//认证方式

//    private Integer mode; // 模式 1: 17模式 2:快乐学模式    delete by wangsong 20170801

//    private Integer finCsHwEq3AuthCount; //完成3套当前科目作业认证学生数 当前科目月活   delete by wangsong 20170801

    /*public boolean isKlxTeacher() {
        return Objects.equals(mode, 2);
    }*/

    /*public boolean is17modeTeacher() {
        return Objects.equals(mode, 1);
    }*/

    //--------------------------- 2017/06/19 新增是否布置暑假作业-----------------------//
    @Deprecated
    public Boolean summerVacation;

    /**
     * 由于summary中registerTime格式改变为yyyyMMddHHmmss，所以需要转换成真正的时间戳
     * @return
     */
    public Long fetchRegisterTimeStamp(){
        if (null != this.getRegisterTime()){
            Date date = DateUtils.stringToDate(String.valueOf(this.getRegisterTime()), "yyyyMMddHHmmss");
            if (null != date){
                return date.getTime();
            }
        }
        return null;
    }
    /**
     * 由于summary中authTime格式改变为yyyyMMddHHmmss，所以需要转换成真正的时间戳
     * @return
     */
    public Long fetchAuthTimeStamp(){
        if (null != this.getRegisterTime()){
            Date date = DateUtils.stringToDate(String.valueOf(this.getAuthTime()), "yyyyMMddHHmmss");
            if (null != date){
                return date.getTime();
            }
        }
        return null;
    }
}
