package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author jiangpeng
 * @since 2017-08-22 下午2:51
 **/
@Data
public class StudentGrindEarRange implements Serializable {
    private static final long serialVersionUID = -7134686748938616421L;

    @JsonProperty("student_id")
    private Long studentId;
    private Integer rank;
    @JsonProperty("day_count")
    private Long dayCount;

    //冗余
    @JsonProperty("clazz_name")
    private String clazzName;
    @JsonProperty("name")
    private String studentName;


}
