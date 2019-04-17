package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by song.wang on 2016/4/19.
 */

@Getter
@Setter
@NoArgsConstructor
public class GroupTeacherData {
    private Long groupId;
    private Long teacherId;
    private String realName;
    private Subject subject;
    private String authStatus;
    private String mobile;
}
