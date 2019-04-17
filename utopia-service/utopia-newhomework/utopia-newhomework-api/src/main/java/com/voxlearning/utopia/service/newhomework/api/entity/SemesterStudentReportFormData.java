package com.voxlearning.utopia.service.newhomework.api.entity;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 学期报告作业形式信息
 */
@Setter
@Getter
public class SemesterStudentReportFormData implements Serializable {
    private static final long serialVersionUID = 3516132411865114217L;
    private String homework_form_id;           //作业形式id
    private String homework_form_name;         //作业形式名称
    private Integer avg_score;                 //平均分
    private Integer grp_avg_score;             //组平均分
    private Integer grp_max_score;             //组最高分

}
