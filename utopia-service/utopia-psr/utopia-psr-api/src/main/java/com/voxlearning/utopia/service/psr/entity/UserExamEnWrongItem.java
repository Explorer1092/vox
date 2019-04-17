package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Deprecated
public class UserExamEnWrongItem implements Serializable {

    private static final long serialVersionUID = 4168083741153501360L;

    private String eid;
    private String ek;
    private String et;
    /** 最后状态的时间，如最后做正确或者错误的时间 */
    private Date date;
    /** 准确率 */
    private Double rate;
    /** 做题总数 */
    private Integer sumCount;
    /** 作对总数 */
    private Integer rightCount;
    /** 百分比轮盘算法归一化权重使用 */
    private Double weightPer;


}
