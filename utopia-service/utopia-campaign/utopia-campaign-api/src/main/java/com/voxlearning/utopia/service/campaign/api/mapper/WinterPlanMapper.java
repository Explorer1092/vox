package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = {"clazzId"})
public class WinterPlanMapper implements java.io.Serializable {

    private Integer clazzLevel;
    private Long clazzId;
    private String clazzName;
    private Integer size;
    private Integer assignSize;
    private List<StudentInfo> students;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(of = "studentId")
    public static class StudentInfo implements java.io.Serializable {
        private Long studentId;
        private String studentName;
    }
}
