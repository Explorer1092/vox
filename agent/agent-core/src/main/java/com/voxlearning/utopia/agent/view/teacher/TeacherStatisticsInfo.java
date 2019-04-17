package com.voxlearning.utopia.agent.view.teacher;

import lombok.Getter;
import lombok.Setter;

/**
 * TeacherStatisticsInfo
 *
 * @author deliang.che
 * @since  2019/2/15
 */
@Getter
@Setter
public class TeacherStatisticsInfo {
    private Long schoolId;
    private Integer regTeaNum;      //注册老师数
    private Integer regEngTeaNum;   //注册英语老师数
    private Integer regMathTeaNum;  //注册数学老师数
    private Integer regChiTeaNum;   //注册语文老师数

    private Integer authTeaNum;     //认证老师数
    private Integer authEngTeaNum;  //认证英语老师数
    private Integer authMathTeaNum; //认证数学老师数
    private Integer authChiTeaNum;  //认证语文老师数

    private Integer tmHwTeaNum;     //本月布置老师数
    private Integer tmHwEngTeaNum;  //本月布置英语老师数
    private Integer tmHwMathTeaNum; //本月布置数学老师数
    private Integer tmHwChiTeaNum;  //本月布置语文老师数
}
