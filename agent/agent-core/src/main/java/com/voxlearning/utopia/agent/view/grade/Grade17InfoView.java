package com.voxlearning.utopia.agent.view.grade;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Grade17InfoView
 *
 * @author song.wang
 * @date 2018/4/3
 */
@Getter
@Setter
public class Grade17InfoView {
    private Integer grade;                                                      // 对应clazzLevel
    private String gradeName;                                                   // 年级名称
    private int stuScale;                                                   // 年级规模
    private int regStuCount;                                                // 注册学生数
    private int auStuCount;                                                 // 认证学生数

    private	int	bindParentStuNum;	                //绑定家长的学生数
    private	int	parentStuActiveSettlementNum;	    //家长学生双活结算

    private List<GradeClass17InfoView> classList = new ArrayList<>();           // 年级下面的班级列表
}
