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
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.SchoolType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.api.constant.CrmSchoolClueExternOrBoarder;
import com.voxlearning.utopia.api.constant.CrmSchoolClueSchoolingLength;
import com.voxlearning.utopia.api.constant.CrmSchoolClueStatus;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/11/9
 */
@Getter
@Setter
@Slf4j
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_school_clue")
@UtopiaCacheRevision("20171026")
public class CrmSchoolClue implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = -4136698363199729500L;
    public static final List<Integer> GRADE_LIST = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13);

    @DocumentId
    private String id;
    private Long schoolId;                       // 审核完毕，生成学校后的学校ID
    private String cmainName;                    // 学校主干名
    private String schoolDistrict;               // 校区信息
    private String shortName;                    // 学校简称与学校主干名相同（去掉省市区）
    private Integer schoolPhase;                 // 学校阶段（1小学，2中学,4 高中、5 学前）
    //private Integer schoolingLength;             // 1：小学5年制 、2： 小学6年制 、3： 初中3年制 4：初中4年制、5：学前4年制 、6 高中3年制
    private String eduSystem;                    // 学制
    private Set<Long> branchSchoolIds;           // 分校ID
    private Set<Long> repeatSchool;              // 可能是重复学校
    private String address;                      // 学校地址
    private Long recorderId;                     // 采集者ID
    private String recorderName;                 // 采集者姓名
    private String recorderPhone;                // 采集者电话
    private String reviewer;                     // 审核人
    private String reviewerName;                 // 审核人姓名
    private String reviewNote;                   // 审核意见
    private Date reviewTime;                     // 审核时间
    private Integer status;                      // 状态，-1: 已驳回、0: 暂存、1: 待审核、2: 已通过
    private String photoUrl;                     // 学校照片GFS地址
    private Integer provinceCode;                // 省Code
    private String provinceName;                 // 省名称
    private Integer cityCode;                    // 市Code
    private String cityName;                     // 市Code
    private Integer countyCode;                  // 区Code
    private String countyName;                   // 区Code
    private Integer englishStartGrade;           // 英语起始年级
    private Integer authenticateType;            // 审核类别 1学校鉴定，2 信息完善 3 创建紧急学校 4 照片签到 5 学校位置鉴定
    private Boolean disabled;                    // 禁用
    //相机相关字段
    private String coordinateType;               // GPS类型:wgs84 ,百度类型: bd09ll 高德类型:autonavi
    private String latitude;                     // 地理坐标：纬度
    private String longitude;                    // 地理坐标：经度
    private String dateTime;                     // 照片时间
    private String make;                         // 相机厂商
    private String model;                        // 拍着设备
    private Integer grade1StudentCount;          // 一年级班级人数
    private Integer grade2StudentCount;          // 二年级班级人数
    private Integer grade3StudentCount;          // 三年级班级人数
    private Integer grade4StudentCount;          // 四年级班级人数
    private Integer grade5StudentCount;          // 五年级班级人数
    private Integer grade6StudentCount;          // 六年级班级人数
    private Integer grade7StudentCount;          // 七年级班级人数
    private Integer grade8StudentCount;          // 八年级班级人数
    private Integer grade9StudentCount;          // 九年级班级人数
    //private Integer grade10StudentCount;
    private Integer grade11StudentCount;         // 高一班级人数
    private Integer grade12StudentCount;         // 高二级班级人数
    private Integer grade13StudentCount;         // 高三级班级人数

    private Integer newGrade1ClassCount;          // 一年级班级数
    private Integer newGrade2ClassCount;          // 二年级班级数
    private Integer newGrade3ClassCount;          // 三年级班级数
    private Integer newGrade4ClassCount;          // 四年级班级数
    private Integer newGrade5ClassCount;          // 五年级班级数
    private Integer newGrade6ClassCount;          // 六年级班级数
    private Integer newGrade7ClassCount;          // 七年级班级数
    private Integer newGrade8ClassCount;          // 八年级班级数
    private Integer newGrade9ClassCount;          // 九年级班级数
    //private Integer newGrade10ClassCount;
    private Integer newGrade11ClassCount;         // 高一班级数
    private Integer newGrade12ClassCount;         // 高二班级数
    private Integer newGrade13ClassCount;         // 高三班级数
    //private String gradeDistribution;            // 年级分布
    //private List<Map<String, Integer>> gradeInfo;   // 学校的班级年级信息

    private List<Map<String, Object>> infantGrade;      // 学前年级
    private Integer schoolSize;

    //---------------------------以下为旧版本字段 2016-12-28--------------------------------------
    private String schoolName;                   // 学校名schoolPhase
    private Integer schoolLevel;                 // 学校等级，1表示重点学校，2表示非重点学校
    private Integer schoolType;                  // 学校类型（1国家规定学校，2自定义学校, 3私立学校)
    private Integer infoStatus;                  // 状态，-1: 已驳回、0: 暂存、1: 待审核、2: 已通过
    private Integer externOrBoarder;             // 走读 or 寄宿 1:走读 、2:寄宿、3 走读/寄宿

    private Long masterSchoolId;                 // 非空表示该校为分校，值为总部的学校ID
    private Date createTime;
    private Date updateTime;
    //不清楚用途
    private String source;                       // 来源标示，如UGCSchoolTask的ID

    /*
     * 负责页面显示不存入mongo库中的数据
     */
    @DocumentFieldIgnore
    private SchoolLevel showPhase;                 //schoolPhase 是他的code
    @DocumentFieldIgnore
    private SchoolType showType;                  //公立私立
    @DocumentFieldIgnore
    private CrmSchoolClueStatus showStatus;       //线索状态
    @DocumentFieldIgnore
    private CrmSchoolClueStatus showInfoStatus;   //完善信息状态
    @DocumentFieldIgnore
    private CrmSchoolClueExternOrBoarder showExternOrBoarder;          // 走读 or 寄宿 1:走读 、2:寄宿、3 走读/寄宿
    @DocumentFieldIgnore
    private CrmSchoolClueSchoolingLength showSchoolingLength;          // 1：5年制 、2： 6年制 、3： 3年制 4：4年制
    @DocumentFieldIgnore
    private Set<String> branchSchoolNames;       // 分校名称
    /*
     * 以下代码为旧数据
     */
    private String keyContactName;               // 关键联系人姓名
    private String keyContactPhone;              // 关键联系人电话
    private Integer englishTeacherCount;         // 英语老师数量
    private Integer mathTeacherCount;            // 数学老师数量
    private Integer chineseTeacherCount;         // 语文老师数量
    private Integer grade1AvgStudentCount;       // 一年级班均人数
    private Integer grade1ClassCount;
    private Integer grade2ClassCount;
    private Integer grade2AvgStudentCount;       // 二年级班均人数
    private Integer grade3ClassCount;            // 三年级班级数量
    private Integer grade3AvgStudentCount;
    private Integer grade4ClassCount;            // 四年级班级数量
    private Integer grade4AvgStudentCount;       // 四年级班均人数
    private Integer grade5ClassCount;            // 五年级班级数量
    private Integer grade5AvgStudentCount;       // 五年级班均人数
    private Integer grade6ClassCount;            // 六年级班级数量
    private Integer grade6AvgStudentCount;       // 六年级班均人数
    private Integer grade7ClassCount;            // 七年级班级数量
    private Integer grade7AvgStudentCount;       // 七年级班均人数
    private Integer grade8ClassCount;            // 八年级班级数量
    private Integer grade8AvgStudentCount;       // 八年级班均人数
    private Integer grade9ClassCount;            // 九年级班级数量
    private Integer grade9AvgStudentCount;       // 九年级班级数量
    @DocumentFieldIgnore
    private String niceLevel;

    @JsonIgnore
    public String formatGradeDistribution() {
        return StringUtils.join(fetchGradeInfo().stream().map(p -> p.get("gradeLevel")).collect(Collectors.toList()), ",");
    }

    /**
     * @return 学校学生合计
     */
    @JsonIgnore
    public Integer sumSchoolSizeByGradeInfo() {
        Integer oldGradeStuSum = fetchGradeInfo().stream().mapToInt(p1 -> SafeConverter.toInt(p1.get("studentCount"))).sum();
        Integer infantGradeStuSum = this.infantGrade == null ? 0 : this.infantGrade.stream().mapToInt(p1 -> SafeConverter.toInt(p1.get("studentCount"))).sum();
        return oldGradeStuSum + infantGradeStuSum;
    }

    /**
     * grade1StudentCount 年级的学生数
     * newGrade1ClassCount  年级的学生数
     * keys :{gradeLevel:"年级数",studentCount:"年级学生数",classCount:"班级数"}
     *
     * @return 学校的年级信息
     */
    @JsonIgnore
    public List<Map<String, Object>> fetchGradeInfo(boolean equalZero) {
        List<Map<String, Object>> result = new ArrayList<>();
        GRADE_LIST.forEach(p -> {
            Map<String, Object> gradeInfo = new HashMap<>();
            try {
                PropertyDescriptor studentPd = new PropertyDescriptor(StringUtils.formatMessage("grade{}StudentCount", p), this.getClass());
                Method getMethod = studentPd.getReadMethod();//获得get方法
                Object studentCount = getMethod.invoke(this);//执行get方法返回一个Object
                PropertyDescriptor classPd = new PropertyDescriptor(StringUtils.formatMessage("newGrade{}ClassCount", p), this.getClass());
                getMethod = classPd.getReadMethod();//获得get方法
                Object classCount = getMethod.invoke(this);//执行get方法返回一个Object
                if ((equalZero && SafeConverter.toInt(studentCount) >= 0) || SafeConverter.toInt(studentCount) > 0) {
                    gradeInfo.put("gradeLevel", p);
                    gradeInfo.put("studentCount", studentCount);
                    gradeInfo.put("classCount", classCount);
                    result.add(gradeInfo);
                }
            } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ignored) {
            }
        });
        return result;
    }

    @JsonIgnore
    private List<Map<String, Object>> fetchGradeInfo() {
        return fetchGradeInfo(false);
    }

    // 信息完整返回true
    @JsonIgnore
    public boolean checkSchoolLocationInfo() {
        return !(StringUtils.isBlank(this.latitude) || StringUtils.isBlank(this.longitude)) && !StringUtils.isBlank(this.photoUrl);
    }

    @JsonIgnore
    public boolean isReviewPassed() {
        return status != null && status.equals(CrmSchoolClueStatus.已通过.code);
    }

    @Override
    public void touchCreateTime(long timestamp) {
        if (createTime == null) {
            createTime = new Date(timestamp);
        }
    }

    @JsonIgnore
    public boolean isApproved() {
        if (authenticateType == null) {
            return false;
        }
        if (authenticateType == 1 && toInt(status) == 2) {
            return true;
        }
        if (authenticateType == 2 && toInt(infoStatus) == 2) {
            return true;
        }
        if(authenticateType == 3 && toInt(infoStatus) == 2){
            return true;
        }
        if(authenticateType == 4 && toInt(infoStatus) == 2){
            return true;
        }
        return false;
    }

    @JsonIgnore
    public boolean isAuthstr(){
        if (authenticateType == null) {
            return false;
        }
        if (authenticateType == 1 && toInt(status) == 1) {
            return true;
        }
        if (authenticateType == 2 && toInt(infoStatus) == 1) {
            return true;
        }
        if(authenticateType == 3 && toInt(infoStatus) == 1){
            return true;
        }
        if(authenticateType == 4 && toInt(infoStatus) == 1){
            return true;
        }
        return false;
    }

    @JsonIgnore
    public boolean isRejected() {
        if (authenticateType == null) {
            return false;
        }
        if (authenticateType == 1 && toInt(status) == -1) {
            return true;
        }
        if (authenticateType == 2 && toInt(infoStatus) == -1) {
            return true;
        }
        if(authenticateType == 3 && toInt(infoStatus) == -1){
            return true;
        }
        if(authenticateType == 4 && toInt(infoStatus) == -1){
            return true;
        }
        return false;
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        updateTime = new Date(timestamp);
    }

    private Integer toInt(Integer i) {
        return i==null? 0:i;
    }

    //  标记为位置类型申请
    @JsonIgnore
    public void markLocationClue() {
        this.setDisabled(false);
        this.setAuthenticateType(5);
        this.setStatus(1);
    }
    @JsonIgnore
    public String loadSchoolFullName() {
        if (StringUtils.isBlank(cmainName)) {
            return null;
        }
        return cmainName + (StringUtils.isBlank(schoolDistrict) ? "" : StringUtils.formatMessage("({})", schoolDistrict));
    }

//    @JsonIgnore
//    public static void mergerSchoolExtInfoGradInfo(CrmSchoolClue clue, SchoolExtInfo schoolExtInfo) {
//        if ((clue.getGrade1StudentCount() == null) && schoolExtInfo.getGrade1StudentCount() != null) {
//            clue.setGrade1StudentCount(schoolExtInfo.getGrade1StudentCount());
//        }
//
//        if ((clue.getGrade2StudentCount() == null) && schoolExtInfo.getGrade2StudentCount() != null) {
//            clue.setGrade2StudentCount(schoolExtInfo.getGrade2StudentCount());
//        }
//
//        if ((clue.getGrade3StudentCount() == null) && schoolExtInfo.getGrade3StudentCount() != null) {
//            clue.setGrade3StudentCount(schoolExtInfo.getGrade3StudentCount());
//        }
//
//        if ((clue.getGrade4StudentCount() == null) && schoolExtInfo.getGrade4StudentCount() != null) {
//            clue.setGrade4StudentCount(schoolExtInfo.getGrade4StudentCount());
//        }
//
//        if ((clue.getGrade5StudentCount() == null) && schoolExtInfo.getGrade5StudentCount() != null) {
//            clue.setGrade5StudentCount(schoolExtInfo.getGrade5StudentCount());
//        }
//
//        if ((clue.getGrade6StudentCount() == null) && schoolExtInfo.getGrade6StudentCount() != null) {
//            clue.setGrade6StudentCount(schoolExtInfo.getGrade6StudentCount());
//        }
//
//        if ((clue.getGrade7StudentCount() == null) && schoolExtInfo.getGrade7StudentCount() != null) {
//            clue.setGrade7StudentCount(schoolExtInfo.getGrade7StudentCount());
//        }
//
//        if ((clue.getGrade8StudentCount() == null) && schoolExtInfo.getGrade8StudentCount() != null) {
//            clue.setGrade8StudentCount(schoolExtInfo.getGrade8StudentCount());
//        }
//
//        if ((clue.getGrade9StudentCount() == null) && schoolExtInfo.getGrade9StudentCount() != null) {
//            clue.setGrade9StudentCount(schoolExtInfo.getGrade9StudentCount());
//        }
//
//        // -----------------------------------------------新增字段----------------------------------------------------------------------------
//
//        if ((clue.getGrade11StudentCount() == null) && schoolExtInfo.getGrade11StudentCount() != null) {
//            clue.setGrade11StudentCount(schoolExtInfo.getGrade11StudentCount());
//        }
//        if ((clue.getGrade12StudentCount() == null) && schoolExtInfo.getGrade12StudentCount() != null) {
//            clue.setGrade12StudentCount(schoolExtInfo.getGrade12StudentCount());
//        }
//        if ((clue.getGrade13StudentCount() == null) && schoolExtInfo.getGrade13StudentCount() != null) {
//            clue.setGrade13StudentCount(schoolExtInfo.getGrade13StudentCount());
//        }
//        if ((clue.getNewGrade1ClassCount() == null ) && schoolExtInfo.getNewGrade1ClassCount() != null) {
//            clue.setNewGrade1ClassCount(schoolExtInfo.getNewGrade1ClassCount());
//        }
//
//        if ((clue.getNewGrade2ClassCount() == null ) && schoolExtInfo.getNewGrade2ClassCount() != null) {
//            clue.setNewGrade2ClassCount(schoolExtInfo.getNewGrade2ClassCount());
//        }
//
//        if ((clue.getNewGrade3ClassCount() == null ) && schoolExtInfo.getNewGrade3ClassCount() != null) {
//            clue.setNewGrade3ClassCount(schoolExtInfo.getNewGrade3ClassCount());
//        }
//
//        if ((clue.getNewGrade4ClassCount() == null ) && schoolExtInfo.getNewGrade4ClassCount() != null) {
//            clue.setNewGrade4ClassCount(schoolExtInfo.getNewGrade4ClassCount());
//        }
//
//        if ((clue.getNewGrade5ClassCount() == null ) && schoolExtInfo.getNewGrade5ClassCount() != null) {
//            clue.setNewGrade5ClassCount(schoolExtInfo.getNewGrade5ClassCount());
//        }
//
//        if ((clue.getNewGrade6ClassCount() == null ) && schoolExtInfo.getNewGrade6ClassCount() != null) {
//            clue.setNewGrade6ClassCount(schoolExtInfo.getNewGrade6ClassCount());
//        }
//
//        if ((clue.getNewGrade7ClassCount() == null ) && schoolExtInfo.getNewGrade7ClassCount() != null) {
//            clue.setNewGrade7ClassCount(schoolExtInfo.getNewGrade7ClassCount());
//        }
//
//        if ((clue.getNewGrade8ClassCount() == null ) && schoolExtInfo.getNewGrade8ClassCount() != null) {
//            clue.setNewGrade8ClassCount(schoolExtInfo.getNewGrade8ClassCount());
//        }
//
//        if ((clue.getNewGrade9ClassCount() == null ) && schoolExtInfo.getNewGrade9ClassCount() != null) {
//            clue.setNewGrade9ClassCount(schoolExtInfo.getNewGrade9ClassCount());
//        }
//
//        if ((clue.getNewGrade13ClassCount() == null) && schoolExtInfo.getNewGrade13ClassCount() != null) {
//            clue.setNewGrade13ClassCount(schoolExtInfo.getNewGrade13ClassCount());
//        }
//
//        if ((clue.getNewGrade11ClassCount() == null ) && schoolExtInfo.getNewGrade11ClassCount() != null) {
//            clue.setNewGrade11ClassCount(schoolExtInfo.getNewGrade11ClassCount());
//        }
//
//        if ((clue.getNewGrade12ClassCount() == null ) && schoolExtInfo.getNewGrade12ClassCount() != null) {
//            clue.setNewGrade12ClassCount(schoolExtInfo.getNewGrade12ClassCount());
//        }
//        if (CollectionUtils.isEmpty(clue.getInfantGrade()) && (CollectionUtils.isNotEmpty(schoolExtInfo.getInfantGrade()))) {
//            clue.setInfantGrade(schoolExtInfo.getInfantGrade());
//        }
//        if (CollectionUtils.isNotEmpty(clue.getInfantGrade()) && CollectionUtils.isNotEmpty(schoolExtInfo.getInfantGrade())) {
//            schoolExtInfo.getInfantGrade().forEach(p -> {
//                cleanBlank("studentCount", p);
//                cleanBlank("classCount", p);
//            });
//            clue.getInfantGrade().forEach(p->{
//                cleanBlank("studentCount", p);
//                cleanBlank("classCount", p);
//            });
//            SchoolExtInfo.copyDiff(clue.getInfantGrade(), schoolExtInfo.getInfantGrade());
//        }
//    }

    @JsonIgnore
    private static void cleanBlank(String key, Map<String, Object> infantGrade) {
        String count = SafeConverter.toString(infantGrade.get(key), null);
        if (Objects.equals(count, "")) {
            infantGrade.put(key, null);
        }
    }

//    @JsonIgnore
//    public List<Map<String, Object>> fetchEffectiveGradeInfo() {
//        List<Map<String, Object>> result = new ArrayList<>();
//        EduSystemType eduType = fetchEduSystem();
//        if (eduType != null) {
//            List<Integer> gradeList;
//            if (Objects.equals(EduSystemType.I4, eduType)) {
//                if (CollectionUtils.isEmpty(infantGrade)) {
//                    return infantGrade;
//                } else {
//                    infantGrade.forEach(p -> {
//                        toInt("studentCount", p);
//                        toInt("classCount", p);
//                    });
//                    return infantGrade;
//                }
//            } else {
//                gradeList = Arrays.stream(eduType.getCandidateClazzLevel().split(",")).map(SafeConverter::toInt).collect(Collectors.toList());
//            }
//
//            gradeList.forEach(p -> {
//                Map<String, Object> gradeInfo = new HashMap<>();
//                try {
//                    PropertyDescriptor studentPd = new PropertyDescriptor(StringUtils.formatMessage("grade{}StudentCount", p), this.getClass());
//                    Method getMethod = studentPd.getReadMethod();//获得get方法
//                    Object studentCount = getMethod.invoke(this);//执行get方法返回一个Object
//                    PropertyDescriptor classPd = new PropertyDescriptor(StringUtils.formatMessage("newGrade{}ClassCount", p), this.getClass());
//                    getMethod = classPd.getReadMethod();//获得get方法
//                    Object classCount = getMethod.invoke(this);//执行get方法返回一个Object
//
//                    gradeInfo.put("gradeLevel", p);
//                    gradeInfo.put("studentCount", studentCount);
//                    gradeInfo.put("classCount", classCount);
//                    result.add(gradeInfo);
//                } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
//                }
//            });
//        }
//        return result;
//    }
//
//    @JsonIgnore
//    private static void toInt(String key, Map<String, Object> infantGrade) {
//        Integer count = SafeConverter.toInt(infantGrade.get(key));
//        if (count == 0) {
//            infantGrade.put(key, null);
//        } else {
//            infantGrade.put(key, count);
//        }
//    }

    @JsonIgnore
    public Integer sumStudentCount() {
        return 0;
    }

    public EduSystemType fetchEduSystem() {
        return EduSystemType.of(eduSystem);
    }
}
