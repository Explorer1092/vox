package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz.utils;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class SentenceAnalysis implements Serializable {
    private static final long serialVersionUID = -5124129277746302334L;
    private String sentence;
    private Set<Long> wrongUserIds = new LinkedHashSet<>();
}