package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author xinxin
 * @since 12/8/2016
 */
@Getter
@Setter
public class StudentHomeworkResultMapper implements Serializable {
    private static final long serialVersionUID = -6790312065960476052L;

    private String title;
    private String endDate;
    private List<StudentHomeworkResultItemMapper> items;
}
