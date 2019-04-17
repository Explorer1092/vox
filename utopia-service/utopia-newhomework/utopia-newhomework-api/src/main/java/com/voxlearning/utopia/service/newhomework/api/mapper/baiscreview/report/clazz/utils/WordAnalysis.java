package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz.utils;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class WordAnalysis implements Serializable {
    private static final long serialVersionUID = -243281620931069594L;
    private String word;    //单词
    private Set<Long> mishearUserIds = new LinkedHashSet<>(); //听错的学生
    private Set<Long> misLookUserIds = new LinkedHashSet<>(); //认错的学生
    private Set<Long> wrongUserIds = new LinkedHashSet<>();

}
