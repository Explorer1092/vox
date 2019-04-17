package com.voxlearning.utopia.service.psr.entity.termreport;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mingming.zhao on 2016/10/20.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class student_month_infos implements Serializable {
    private static final long serialVersionUID = -5605685638246663501L;
    private String student_id;
    private Integer completeNums;
    private Double avgscores;
    private Integer has_score_count;
}




