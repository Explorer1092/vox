package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTag;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * SchoolBasicInfo
 *
 * @author song.wang
 * @date 2017/7/17
 */
@Getter
@Setter
public class SchoolBasicInfo {
    private Long schoolId;
    private String schoolName;
    /**
     * 学校简称(去掉省市区的名字)
     */
    private String shortName;
    private String provinceName;
    private String cityName;
    private String countyName;

    private SchoolLevel schoolLevel;    // 学校等级
    private AuthenticationState authState;   // 认证状态
    private Integer schoolScale;        // 学校规模
    private Boolean hasThumbnail;       // 是否有缩略图
    private String thumbnailUrl;           // 缩略图URL
    private Boolean scannerFlag = false; //是否开通扫描仪
    private Boolean isNew = false;   // 专员：最近7日内分配给专员    市经理及以上角色：最近7日加入到字典表
    private Boolean isDictSchool = false;       // 是否字典表学校
    private AgentSchoolPopularityType schoolPopularityType;  // 名校，重点校，普通校
    private Boolean hasBd = false;   // 是否有专员负责
    private Long bdId;        // 专员ID
    private String bdName;    // 专员姓名
    private Integer sort;     // 用于排序的字段，自己的学校是1000，字典表的学校是100，其他类学校是10
    private AgentSchoolPermeabilityType permeabilityType; //学校渗透情况

    private Integer competitiveProductFlag;  // 学校竞品标识：  0：市场没有反馈是否有竞品  1：已反馈，无竞品   2： 已反馈，有竞品

    private String lastVisitTime;       //最近拜访时间
    private Long lastVisitTimeLong;     //最近拜访时间
    private String address;             //地址
    private String genDistance;         //距离当前坐标距离（单位：千米/米）
    private Double genDistanceDouble;   //距离当前坐标距离（单位：米）

    private Integer auditStatus;    //审核状态  1: 待审核

    private Integer externOrBoarder;// 走读 or 寄宿 1:走读 、2:寄宿、3 走读/寄宿（半寄宿）
    private Integer regStuCount;    //注册学生数
    private Integer tmIncRegStuCount;//本月增加注册学生数
    private Integer loginStuCount;  //登录学生数
    private Integer sglSubjMauc;    //单科月活

    private List<AgentTag> tagList;
}
