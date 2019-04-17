package com.voxlearning.utopia.entity.teachingdiagnosis;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TeachingDiagnosisTaskCourse implements Serializable {
    private static final long serialVersionUID = 151282530630791997L;
    private String id;
    private String name;
    private String description;
}
