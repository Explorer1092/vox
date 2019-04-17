package com.voxlearning.utopia.service.newhomework.api.mapper.termreport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbin
 * @since 2016/11/1 12:27
 */

@Getter
@Setter
@ToString
public class UnitReportBO implements Serializable {
    private static final long serialVersionUID = -3480534243537155639L;
    /**
     * 布置作业次数
     */
    private Integer layoutHomeworkTimes;
    /**
     * 学生作业的完成情况
     */
    private List<StudentUnitReportBO> studentUnitReportBOList;
}
