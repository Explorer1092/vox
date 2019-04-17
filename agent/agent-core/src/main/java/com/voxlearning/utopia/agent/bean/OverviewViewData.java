package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * OverviewViewData
 *
 * @author song.wang
 * @date 2017/4/16
 */

@Getter
@Setter
public class OverviewViewData {

    private Long id;                //数据记录的键
    private String idType;               //数据记录的类型 标记id 是部门ID ，学校ID, 用户ID
    private String name;               //数据记录的名称 部门名称， 学校名称， 用户名称等

    private String viewType;          // 概览类型 JUNIOR, MIDDLE
    private String viewName;         // 概览名称 小学，中学

    private int stuScale;      //学生规模
    private int regStuNum;    // 注册学生数
    private int authStuNum;   // 认证学生数
    private int monthRegStuNum;           //月新增注册学生数
    private int monthAuthStuNum;          //月新增认证学生数
    private int dayRegStuNum;         //昨日新增注册学生数
    private int dayAuthStuNum;        ///昨日新增认证学生数
    private double authRate;         // 认证率


}
