package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xuan.zhu
 * @date 2018/8/24 15:24
 * 用户运营信息（筛选用户用）
 */
@Setter
@Getter
public class AiUserOperationInfo implements Serializable{
    private static final long serialVersionUID = 8493312939915630318L;

    private Long id;                 //用户id
    private String name;             //姓名
    private String className;        //班级名称
    private String productName;      //产品名称
    private Long registerDate;       //报名日期
    private Boolean inGroup;         //是否进群
    private String orderRef;         //订单来源
    private Boolean questionnaires;  //是否问卷
}
