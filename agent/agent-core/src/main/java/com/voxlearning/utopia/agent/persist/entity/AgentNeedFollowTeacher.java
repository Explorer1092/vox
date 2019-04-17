package com.voxlearning.utopia.agent.persist.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentNeedFollowTeacher
 *
 * @author song.wang
 * @date 2016/7/29
 */
@Getter
@Setter
@NoArgsConstructor
public class AgentNeedFollowTeacher implements Serializable {
    private static final long serialVersionUID = -8890458826795722528L;
    private Long teacherId;
    private String teacherName;
    private String subject;
}
