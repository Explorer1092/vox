package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/12/22
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSnapshot implements Serializable {
    private static final long serialVersionUID = 5499510468895707827L;

    private Long teacherId;
    private String realName;
    private Subject subject;
    private AuthenticationState authStatus;
    private String mobile;
    public TeacherSnapshot(Long teacherId, String realName, String mobile){
        this.teacherId = teacherId;
        this.realName = realName;
        this.mobile = mobile;
    }
}
