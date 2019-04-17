package com.voxlearning.utopia.service.newhomework.api.mapper.vacation;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 假期作业：计划天数
 * @author: Mr_VanGogh
 * @date: 2018/5/28 下午2:41
 */
@Data
public class VacationHomeworkPlannedDaysMapper implements Serializable {

    private static final long serialVersionUID = -5343698060914551164L;

    private Integer days;//计划天数
    private String text;//文案
    private Boolean isChoose = Boolean.FALSE;//是否选中
}
