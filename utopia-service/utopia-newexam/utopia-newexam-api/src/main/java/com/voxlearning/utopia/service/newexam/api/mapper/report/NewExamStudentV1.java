package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class NewExamStudentV1 implements Serializable {
    private static final long serialVersionUID = 214851579734090940L;
    private List<NewExamDetailToStudent> newExamDetailToStudents = new LinkedList<>();
    private Map<Long, NewExamDetailToStudent> newExamDetailToStudentMap = new LinkedHashMap<>();
    private int joinNum;
}
