package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2016/12/8
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAndClazzListData {

    private Long teacherId;
    private String teacherName;
    private Integer authStudentCount;                 // 认证学生总数
    private List<TeacherClazzInfo> teacherClazzInfoList;

    public Boolean isEnglish(){
        if(CollectionUtils.isEmpty(teacherClazzInfoList)){
            return false;
        }
        return teacherClazzInfoList.stream().anyMatch(p -> p.getSubject() == Subject.ENGLISH);
    }

    public Boolean isMath(){
        if(CollectionUtils.isEmpty(teacherClazzInfoList)){
            return false;
        }
        return teacherClazzInfoList.stream().anyMatch(p -> p.getSubject() == Subject.MATH);
    }

    public Boolean isChinese(){
        if(CollectionUtils.isEmpty(teacherClazzInfoList)){
            return false;
        }
        return teacherClazzInfoList.stream().anyMatch(p -> p.getSubject() == Subject.CHINESE);
    }
}
