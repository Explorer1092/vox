package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz.utils;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class StudentPersonalAnalysis implements Serializable {
    private static final long serialVersionUID = 7010639703452247522L;
    private String userName;
    private Long userId;
    private int wrongNum;//用于排序和去最错误数大的五个人
    private boolean reviewWord;
    private Set<String> wrongWords = new LinkedHashSet<>();
    private boolean reviewSentence;
    private Set<String> wrongSentences = new LinkedHashSet<>();
}
