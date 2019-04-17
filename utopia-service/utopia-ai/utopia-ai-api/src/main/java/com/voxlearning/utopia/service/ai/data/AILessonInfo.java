package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.constant.LessonType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Summer on 2018/3/27
 */
@Getter
@Setter
public class AILessonInfo implements Serializable{

    private static final long serialVersionUID = 5452563230304008944L;
    private String id;
    private String name;
    private Integer rank;
    private LessonType lessonType;
    private Boolean finished;
    private Boolean isLock;
    private Integer star;
    private String unitId;

}
