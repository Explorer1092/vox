package com.voxlearning.utopia.service.action.api.support;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 25/8/2016
 */
@Getter
@Setter
public class ClazzAttendanceInfo implements Serializable {
    private static final long serialVersionUID = 4423206300449535896L;

    private Long clazzId;
    private Integer totalCount; //班内学生总数
    private Integer count;  //已签到数量
    private Double rate;    //签到率
    private Integer rank;   //排名
}
