package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class GradeReportConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String book;
    private List<String> units;
    private Integer g2Score;
    private Integer g3Score;
    private Integer g3radioPer100;
}
