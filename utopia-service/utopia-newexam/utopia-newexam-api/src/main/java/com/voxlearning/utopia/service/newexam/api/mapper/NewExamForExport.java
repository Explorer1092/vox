package com.voxlearning.utopia.service.newexam.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public  class NewExamForExport implements Serializable {
    private static final long serialVersionUID = 561954679468433037L;
    private Long studentId;
    private String studentName;
    private String schoolName;
    private String clazzName;
    private String beginTime;
    private String submitTime;
    private double score;
}
