package com.voxlearning.utopia.service.newhomework.api.entity.basicreview;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author guoqiang.li
 * @since 2017/11/14
 */
@Getter
@Setter
public class BasicReviewHomeworkAppAnswer implements Serializable {

    private static final long serialVersionUID = -386417313880424769L;

    private Integer categoryId;
    private Long practiceId;
    private String practiceName;
    private String lessonId;
    private LinkedHashMap<String, BasicReviewHomeworkAnswer> answers;
}
