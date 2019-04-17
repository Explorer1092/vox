package com.voxlearning.utopia.agent.view.school;

import lombok.Getter;
import lombok.Setter;

/**
 *
 *
 * @author song.wang
 * @date 2018/6/28
 */
@Setter
@Getter
public class SchoolGradeBasicData {
    private Integer grade;                 // 对应ClazzLevel
    private String gradeDesc;
    private Integer clazzNum;
    private Integer studentNum;
}
