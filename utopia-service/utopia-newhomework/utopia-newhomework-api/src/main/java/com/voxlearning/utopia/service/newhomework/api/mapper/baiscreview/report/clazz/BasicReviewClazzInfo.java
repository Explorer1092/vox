package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class BasicReviewClazzInfo implements Serializable {
    private static final long serialVersionUID = 8793085171533202769L;
    private String packageId;
    private Long clazzId;
    private String clazzName;
    private List<Stage> stagesList = new LinkedList<>();

    @Getter
    @Setter
    public static class Stage implements Serializable {
        private static final long serialVersionUID = 6187002684422549116L;
        private String stageName;
        private String homeworkId;
    }
}
