package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description: 作业-题目&答案入参
 * @author: Mr_VanGogh
 * @date: 2018/12/13 上午10:03
 */
@Getter
@Setter
public class HomeworkQuestionAnswerRequest implements Serializable {

    private static final long serialVersionUID = -6094629434377955697L;

    private ObjectiveConfigType objectiveConfigType;
    private String homeworkId;
    private Long studentId;
    private Integer categoryId;
    private String lessonId;
    private String videoId;
    private String questionBoxId;
    private String stoneDataId;
    private WordTeachModuleType wordTeachModuleType;
}
