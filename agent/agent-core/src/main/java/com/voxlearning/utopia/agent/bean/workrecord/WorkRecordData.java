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

package com.voxlearning.utopia.agent.bean.workrecord;

import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.WorkRecordVisitUserInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WorkRecordData implements Serializable {


   private static final long serialVersionUID = -1823342312253738188L;

   public static final String demarcationDate = "2019-03-16 01:00:00";

   private String id;
   private Long userId;
   private String userName;
   private AgentWorkRecordType workType;
   //进校
   private Integer visitSchoolType;               // 1:校级会议  2：拜访老师 3：直播展会推广
   private Long schoolId;
   private String schoolName;
   private String lecturerName;                     // 讲师

   //资源拓维
   private Integer visitIntention;    // 拜访目的  1：初次接洽  2：客情维护 3：促进组会 4：寻求介绍

   //组会
   private String workTitle;
   private CrmMeetingType meetingType;
   private Integer meetingTime;     //宣讲时长 1.小于15分钟，2。15-60分钟，3大于1个小时
   private Integer meetingCount;    //出席人数
   private Integer meetingForm;               //  1.专场，2.插播
   private Boolean isPresent;      // 是否出席

   //陪同
   private AccompanyBusinessType businessType;         // 陪同业务类型
   private String businessRecordId;                    // 关联的业务ID (进校ID, 组会ID 等)
   private Boolean isOldBusinessRecordId;              //是否是旧工作记录ID  true:旧的  false:新的
   private Long accompanyUserId;    //陪访人ID
   private String accompanyUserName;//陪访人姓名
   private String purpose;          //目的
   private Map<EvaluationIndicator,Integer> evaluationMap;//评价

   private List<WorkRecordVisitUserInfo> visitUserInfoList;
   private List<String> photoUrls;     // 现场照片
   private Date workTime;
   private String content;
   private String result;

   //签到信息
   private SignInType signInType;                // 签到类型  GPS, 照片签到
   private String coordinateType;   // 坐标类型
   private String latitude;         // 纬度
   private String longitude;        // 经度
   private String photoUrl;         // 照片地址
   private String address;          // 地址

   private String agencyName;           // 代理人姓名


   private Double workload;          // 工作量T
}
