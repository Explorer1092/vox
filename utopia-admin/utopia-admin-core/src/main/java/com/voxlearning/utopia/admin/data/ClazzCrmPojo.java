package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.util.Date;

/**
 * crm 基础信息tab展示对象
 * @author guangqing
 * @since 2018/8/22
 */
@Data
public class ClazzCrmPojo {

    //班级id
    private Long clazzId;
    //班级名称
    private String clazzName;
    //班主任
    private String clazzTeacherName;
    //用户上线
    private Integer userLimitation;
    //产品名称
    private String productName;
    //产品id
    private String productId;
    //课程名
    private String bookName;
    private String createTime;
    //用户数
    private Integer userCount;

    //类型
    private String type;
    private String typeDesc;
}
