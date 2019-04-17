package com.voxlearning.utopia.agent.bean.resource;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 学校资源卡片
 * Created by Yuechen.Wang on 2016/7/11.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolResourceCard implements Serializable {
    // 基础信息
    private Long schoolId;              // 学校ID
    private String fullName;            // 学校全称
    private String shortName;           // 学校简称
    private SchoolLevel schoolLevel;    // 学校等级
    private Boolean hasThumbnail;       // 是否有缩略图
    private String avatarUrl;           // 学校图片

    private Integer authStatus;         // 认证状态
    private Boolean monthVisited;       // 本月是否拜访过
    private Long latestVisitTime;       // 最后一次拜访的时间
    private Boolean scanMachineFlag; //阅卷机开通权限


    private Integer schoolScale;        // 学校规模
    private Integer regCount;           // 注册人数
    private Integer authCount;          // 认证人数
    private Integer engMauc; // 英语月活
    private Integer lmEngMauc; // 上月英语月活
    private Integer mathMauc; // 数学月活
    private Integer lmMathMauc; // 上月数学月活




    private Integer finEngHwEq1AuStuCount;//认证学生当月完成1套英语作业学生数  =1
    private Integer finEngHwEq2AuStuCount;//认证学生当月完成2套英语作业学生数  =2
    private Integer finMathHwEq1AuStuCount;//认证学生当月完成1套数学作业学生数  =1
    private Integer finMathHwEq2AuStuCount;//认证学生当月完成2套数学作业学生数  =2

    private Integer stuKlxTnCount;//学生快乐学考号数
    private Integer tmFinMathAnshEq1StuCount;//当月答题卡作答1次数学试卷学生使用数  =1


    private String responsibleBd;       // 负责专员



//    @Deprecated private Double authRatio;           // 认证率
//    @Deprecated private String authRatioStr;        // 认证率
//
//    @Deprecated private Boolean locked;             // 是否确认
      private Boolean isDictSchool;       // 是否字典表学校
//    // 业绩相关数据
//    @Deprecated private Integer monthSasc;          // 本月单活数量
//    @Deprecated private Integer monthDasc;          // 本月双活数量
//    @Deprecated private Integer potentialSasc;      // 本月单活可挖数量
//    @Deprecated private Integer potentialDasc;      // 本月双活可挖数量
//    @Deprecated private Integer potentialRsc;       // 未注册学生数量
//    @Deprecated private Integer potentialAsc;       // 未认证学生数量
//    @Deprecated private Integer yesterdaySasc;      // 昨日单活日浮
//    @Deprecated private Integer yesterdayDasc;      // 昨日双活日浮
//    @Deprecated private Date latestVisitTime;       // 上次拜访时间
//    @Deprecated private String latestVisitTimeStr;  // 上次拜访时间

//    // 其他关联信息
//    @Deprecated private Boolean isFavor;                   // 专员是否关注
//    private Map<Long, String> agentUsers;      // 专员列表
    private Map<String, String> ambassadors;   // 校园大使列表

}
