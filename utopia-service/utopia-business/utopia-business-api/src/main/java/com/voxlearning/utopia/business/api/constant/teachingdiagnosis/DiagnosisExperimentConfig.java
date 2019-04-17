package com.voxlearning.utopia.business.api.constant.teachingdiagnosis;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DiagnosisExperimentConfig implements Serializable {

    private static final long serialVersionUID = 903218648134447724L;

    private String answers;
    private String courseIds;
}
