package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.personal;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SubjectToBasicReviewHomework implements Serializable {
    private static final long serialVersionUID = 2563665466552868765L;
    private Subject subject;
    private String packageId;
    private String subjectName;
}
