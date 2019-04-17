package com.voxlearning.utopia.admin.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author feng.guo
 * @since 2019-02-02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AchivementCreditData {
    /**
     * 学生ID
     */
    private Long sid;
    /**
     * 学生姓名
     */
    private String userName;
    /**
     * 班级ID
     */
    private Long cid;
    /**
     * 班级名称
     */
    private String clazzName;
    /**
     * 学校ID
     */
    private Long scid;
    /**
     * 学校名称
     */
    private String schoolName;
    /**
     * 本周学分
     */
    private Double proCredit;
    /**
     * 本期学分
     */
    private Double totalCredit;
    /**
     * 本周英语学分
     */
    private Double proEngCredit;
    /**
     * 本周数学学分
     */
    private Double proMathCredit;
    /**
     * 本周语文学分
     */
    private Double proChineseCredit;
}
