package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
//班级学生参与考试信息
public class AttendanceStudentV1 implements Serializable{
    private static final long serialVersionUID = -8811504435140128416L;
    private List<NewExamStudent> unSubmitStudents = new LinkedList<>();//未提交学生
    private List<NewExamStudent> submitStudents = new LinkedList<>();//提交学生
    private List<NewExamStudent> unJoinStudents = new LinkedList<>();//未参与学生
}
