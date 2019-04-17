package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ExamEnQuizPackageAfenti implements Serializable {

    private static final long serialVersionUID = 1392253493362382223L;

    private Long bookId;
    private Long unitId;
    private Integer unitLevel;

    private String quizId;
    private String quizStatus;
    private Map<Integer/*eid_pos*/, ExamEnAfentiQuizEidItem> quizEidMap;

    // fixme 暂时不推荐类卷试题 升级的时候解析类卷吧, 有的类卷有22个package, "package_size": 22,
}

