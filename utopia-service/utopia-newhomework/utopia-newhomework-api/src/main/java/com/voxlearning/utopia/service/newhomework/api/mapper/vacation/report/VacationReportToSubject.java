package com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class VacationReportToSubject implements Serializable {
    private static final long serialVersionUID = 5074445583870484139L;

    private Subject subject;
    private String subjectName;
    private String packageId;

    private boolean hasJob;

}