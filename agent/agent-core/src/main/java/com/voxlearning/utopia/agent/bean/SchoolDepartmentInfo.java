package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 学校所在的部门信息
 * Created by yaguang.wang on 2016/8/30.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolDepartmentInfo {
    private Long groupId;                               // 部门ID
    private String groupName;                           // 部门名
    private Long businessDeveloperId;                   // 专员Id
    private String businessDeveloperName;               // 专员名称
    private String businessDeveloperNumber;             // 专员工号
    private Long cityManagerId;                         // 市经理Id
    private String cityManagerName;                     // 市经理名称


    private String regionGroupName;                     // 所在大区名称
    private String areaGroupName;                       // 所在区域名称
    private String regionName;                          // 学校所在地区名称
}
