package com.voxlearning.utopia.service.campaign.api.constant;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TeacherCoursewareBookInfo implements Serializable {
    private static final long serialVersionUID = 7502062813885298395L;
    private Subject subject;
    private Integer clazzLevel;
    private Integer termType;
    private String bookId;
    private String unitId;
    private String lessonId;
//    private String serieId;
}
