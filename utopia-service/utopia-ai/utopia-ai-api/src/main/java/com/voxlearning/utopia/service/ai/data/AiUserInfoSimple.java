package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author xuan.zhu
 * @date 2018/8/24 10:48
 * 用户简要信息（筛选用户用）
 */
@Setter
@Getter
public class AiUserInfoSimple implements Serializable{
    private static final long serialVersionUID = 8493312939915630318L;

    private Long id;                 //用户id
    private String name;             //姓名
    private String className;        //班级名称
    private BigDecimal jztConsume;   //家长通总消费
    private Integer buyTimes;        //购课次数
}
