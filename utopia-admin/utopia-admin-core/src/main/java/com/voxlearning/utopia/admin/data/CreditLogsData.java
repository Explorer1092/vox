package com.voxlearning.utopia.admin.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author feng.guo
 * @since 2019-02-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditLogsData {
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
     * 获取来源
     */
    private String creditSource;
    /**
     * 获取分值
     */
    private Double credit;
    /**
     * 获取时间
     */
    private Date createTime;
}
