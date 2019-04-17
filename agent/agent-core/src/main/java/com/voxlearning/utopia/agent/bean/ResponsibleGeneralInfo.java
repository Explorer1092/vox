package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 市经理负责概况
 * Created by yaguang.wang on 2017/1/16.
 */
@Getter
@Setter
@NoArgsConstructor
public class ResponsibleGeneralInfo implements Serializable {
    private static final long serialVersionUID = 5849849243518985919L;
    private String principal;           // 负责人
    private Integer juniorSchoolCount;         //小学学校数量
    private Long juniorEngBud;            // 小学英语预算
    private Long juniorMathBud;         //小学数学预算
    private Integer middleSchoolCount;     // 中学
    private Long middleEngBud;
    private Long middleMathBud;
    private Integer highSchoolCount;       // 高中
    private Long highEngBud;
    private Long highMathBud;
    private Integer totalSchoolCount;   // 学校总数
}
