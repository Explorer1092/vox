package com.voxlearning.utopia.business.api.constant.teachingdiagnosis;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class DiagnosisExperimentGroup implements Serializable {

    private static final long serialVersionUID = 903218648134447724L;
    private String id;
    private String name;
    private Boolean reported;
    private List<DiagnosisExperimentContent> experimentList;
}
