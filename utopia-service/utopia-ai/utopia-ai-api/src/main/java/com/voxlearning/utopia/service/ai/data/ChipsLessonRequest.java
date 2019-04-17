package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.constant.LessonType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Data
public class ChipsLessonRequest implements Serializable {

    private static final long serialVersionUID = -2530754344589299463L;
    private String lessonId;
    private LessonType lessonType;
    private String unitId;
    private String bookId;
}
