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

package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolWorkTitleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentVisitWorkTitleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmMeetingType;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/10/10
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_work_record")
@UtopiaCacheRevision("20180703")
public class CrmWorkRecord implements Serializable {
    private static final long serialVersionUID = -3988479673861623149L;
    private static final Date BEGIN_NEW_WORKRECORD_TIME = stringToDate("2016-07-09");

    @DocumentId
    @Getter @Setter private String id;
    @Getter @Setter private Long workerId;
    @Getter @Setter private String workerName;
    @Getter @Setter private Date workTime;
    @Getter @Setter private String workTitle;    // 进校：AgentSchoolWorkTitleType,   组会：AgentVisitWorkTitleType
    @Getter @Setter private String workContent;
    @Getter @Setter private CrmWorkRecordType workType;
    @Getter @Setter private Long teacherId;
    @Getter @Setter private String teacherName;
    @Getter @Setter private String teacherMobile;
    @Getter @Setter private Long schoolId;
    @Getter @Setter private String schoolName;
    @Getter @Setter private Long partnerId;
    @Getter @Setter private String partnerName;
    @Getter @Setter private Integer talkTime;
    @Getter @Setter private String followingPlan;
    @Getter @Setter private Integer meeteeCount;
    @Getter @Setter private CrmMeetingType meetingType;
    @Getter @Setter private String meetingNote;
    @Getter @Setter private String instructorName;
    @Getter @Setter private Date followingTime;
    @Getter @Setter private String instructorMobile;
    @Getter @Setter private Long interviewerId;
    @Getter @Setter private String interviewerName;
    @Getter @Setter private Integer provinceCode;
    @Getter @Setter private String provinceName;
    @Getter @Setter private Integer cityCode;
    @Getter @Setter private String cityName;
    @Getter @Setter private Integer countyCode;
    @Getter @Setter private String countyName;
    @Getter @Setter private Boolean disabled;
    @Getter @Setter private String taskDetailId;
    private Long researchersId;          // 教研员
    private String researchersName;      // 教研员名称

    @Getter @Setter private String partnerSuggest; //陪访建议

    // 拜访类型为gps正常签到时需要存储经纬度+GPS类型信息
    // 拜访类型为拍摄学校照片签到时除了存储经纬度+GPS类型信息外，还需要存储照片URL和地址
    @Getter @Setter private Integer signType; // 签到方式  1：gps正常签到 2：拍摄学校照片签到
    @Getter @Setter private String latitude;                     // 地理坐标：纬度
    @Getter @Setter private String longitude;                    // 地理坐标：经度
    @Getter @Setter private String coordinateType;               // GPS类型
    @Getter @Setter private String schoolPhotoUrl;                     // 学校照片GFS地址
    @Getter @Setter private String address;                      // 地址

    @Getter @Setter private List<CrmTeacherVisitInfo> visitTeacherList;

    //不存库
    // private transient Set<Long> teacherIds;       //简化复杂的visitTeacherList 的一些操作，与visitTeacherList成对出现，值为此list中的老师ID的列表
    // private Integer recordType;         //新旧 1.新 2.旧 从某个日子开始的版本更迭出现的或者新的数据 只用于前端判断

    // 进校+组会新增字段
    @Getter @Setter private Integer isAgencyClue;   //是否是代理提供线索 1.是 2否

    @Getter @Setter private Boolean isPlanIntoSchool; // 是否是计划内进校
    // 新组会 新增字段
    @Getter @Setter private Integer meetingTime;    //1.小于15分钟，2。15-60分钟，3大于1个小时
    @Getter @Setter private Integer showFrom;       //1.专场，2.插播
    @Getter @Setter private Boolean instructorAttend; //教研员是否出席
    @Getter @Setter private String scenePhotoUrl;      //现场照片

    //陪访新字段
    private String schoolWorkRecordId; //陪访关联进校记录的ID（现在进校、组会、拜访教研员记录ID都关联）
    private Double singleBudgetIncrease; //单科预算增长比
    private Double doubleBudgetIncrease; //双科预算增长比

    private Long agencyId;               // 代理人ID
    private String agencyName;           // 代理人姓名

    private String imgUrl;              //现场照片

    //--------------------------------------拜访教研员---------------------------------------------------------------
    private Integer visitedIntention;    // 拜访目的  VisitedResearchersIntention
    private String visitedPlace;         // 拜访地点
    private String visitedFlow;          // 拜访过程
    private String visitedConclusion;    // 拜访结论
    private String visitedImgUrl;        // 拜访照片
    private List<CrmVisitResearcherInfo> visitedResearcherList;    // 拜访教研员列表（一次拜访多个教研员）
    //-------------------------------------进校新增字段 2016-11-30---------------------------------------------------
    //private Integer registerStudent;     // 预计注册学生数
    //private Integer authStudent;         // 预计认证学生数
    //private Integer singleActStudent;    // 预计单活学生数
    //private Integer doubleActStudent;    // 预计双活学生数

    private Integer forecastStuRegNum;      // 预测注册数据
    private Integer forecastStuAuthNum;     // 预测认证数据
    private Long forecastSascData;          // 预测单活数据
    private Long forecastDascData;          // 预测双活数据

    private Integer schoolLevel;         // 学校阶段 1.小学 2. 中学 4.高中
    // 支持结算
    private Integer studentTotalCount;          //学生总数
    private Integer studentAuthedCount;         //已认证学生数

    ///////////////////////////////////////////////////////////////////////////
    // 2017/5/31 增加记录备忘录记录的字段
    ///////////////////////////////////////////////////////////////////////////
    private String schoolMemorandumInfo;

    ///////////////////////////////////////////////////////////////////////////
    // 学校在进校日30天内被拜访了几次
    ///////////////////////////////////////////////////////////////////////////
    private Integer visitCountLte30;

    //-------------------------------------------陪访反馈评分
    /**
     * 进校准备充分度评分
     */
    private Integer preparationScore;
    /**
     * 产品熟练度评分
     */
    private Integer productProficiencyScore;
    /**
     * 结果符合预期度评分
     */
    private Integer resultMeetExpectedResultScore;


    @Getter @Setter @DocumentCreateTimestamp private Date createTime;
    @Getter @Setter @DocumentUpdateTimestamp private Date updateTime;
    @Getter @Setter private Integer visitSchoolType; //2或空 拜访老师进校  1 校级会议进校

    public static String ck_worker(Long workerId) {
        return CacheKeyGenerator.generateCacheKey(CrmWorkRecord.class, "WID", workerId);
    }

    public static String ck_teacher(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(CrmWorkRecord.class, "TID", teacherId);
    }

    public static String ck_school(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(CrmWorkRecord.class, "SID", schoolId);
    }

    public static String ck_intoSchool(String intoSchoolRecordId){
        return CacheKeyGenerator.generateCacheKey(CrmWorkRecord.class, "ISRID", intoSchoolRecordId);
    }

    public static String ck_workerId_type(Long workerId, CrmWorkRecordType recordType) {
        return CacheKeyGenerator.generateCacheKey(CrmWorkRecord.class,
                new String[]{"workerId", "type"},
                new Object[]{workerId, recordType});
    }

    public static String ck_agencyId_type(Long agencyId) {
        return CacheKeyGenerator.generateCacheKey(CrmWorkRecord.class, "agencyId", agencyId);
    }

    public static String ck_partnerId_type(Long partnerId, CrmWorkRecordType recordType) {
        return CacheKeyGenerator.generateCacheKey(CrmWorkRecord.class,
                new String[]{"partnerId", "type"},
                new Object[]{partnerId, recordType});
    }

    public static String ck_taskDetailId(String taskDetailId) {
        return CacheKeyGenerator.generateCacheKey(CrmWorkRecord.class, "taskDetailId", taskDetailId);
    }

    @JsonIgnore
    public Integer getRecordType() {
        //新旧 1.新 2.旧 从某个日子开始的版本更迭出现的或者新的数据 只用于前端判断
        if (workTime != null && workTime.after(BEGIN_NEW_WORKRECORD_TIME)) {
            return 1;
        } else {
            return 2;
        }
    }

    @JsonIgnore
    public Set<Long> getTeacherIds() {
        if (visitTeacherList != null && visitTeacherList.size() > 0) {
            return visitTeacherList.stream().map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private static Date stringToDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(date);
        } catch (ParseException ex) {
            return null;
        }
    }

    public String fetchWorkTitle(){
        if(StringUtils.isBlank(workTitle)){
            return "";
        }
        String title = workTitle;
        if(workType == CrmWorkRecordType.SCHOOL){
            AgentSchoolWorkTitleType schoolWorkTitleType = AgentSchoolWorkTitleType.of(workTitle);
            if(schoolWorkTitleType != null){
                title = schoolWorkTitleType.getWorkTitle();
            }
        }else if(workType == CrmWorkRecordType.VISIT){
            AgentVisitWorkTitleType visitWorkTitleType = AgentVisitWorkTitleType.of(workTitle);
            if(visitWorkTitleType != null){
                title = visitWorkTitleType.getWorkTitle();
            }
        }
        return title;
    }

}
