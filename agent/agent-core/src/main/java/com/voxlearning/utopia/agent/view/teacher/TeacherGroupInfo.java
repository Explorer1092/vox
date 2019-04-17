package com.voxlearning.utopia.agent.view.teacher;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author song.wang
 * @date 2018/4/18
 */
@Getter
@Setter
public class TeacherGroupInfo {
    private Integer grade;
    private Long classId;
    private String className;
    private String classFullName;
    private Long groupId;
    private Date updateTime;
    private Map<String, Object> groupKpiData = new HashMap<>();
}
