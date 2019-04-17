package com.voxlearning.utopia.agent.bean;


import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TeacherClazzInfo
 *
 * @author song.wang
 * @date 2016/12/8
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClazzInfo {
    private Long teacherId;
    private Long groupId;
    private Long clazzId;
    private Integer clazzLevel;     //年级
    private String clazzName;
    private Subject subject;

    public String formalizeClazzName() {
        ClazzLevel clazzLevel = ClazzLevel.parse(this.getClazzLevel());
        if (clazzLevel == null) {
            clazzLevel = ClazzLevel.getDefaultClazzLevel();
        }
        if (clazzLevel == ClazzLevel.PRIVATE_GRADE) {
            return this.getClazzName() == null? "" : this.getClazzName();
        }
        return clazzLevel.getDescription() + (this.getClazzName() == null ? "" : this.getClazzName());
    }
}
