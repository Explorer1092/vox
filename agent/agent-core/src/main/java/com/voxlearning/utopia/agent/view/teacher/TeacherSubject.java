package com.voxlearning.utopia.agent.view.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * TeacherSubject
 *
 * @author song.wang
 * @date 2018/4/18
 */
@Getter
@Setter
public class TeacherSubject {
    private Long teacherId;
    private Subject subject;          // 老师科目列表
    private String subjectName;

    private Map<String, Object> kpiData = new HashMap<>();
}
