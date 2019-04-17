package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class NewExamThemeForSub implements Serializable {
    private static final long serialVersionUID = 6708023270912148781L;
    private String desc;
    private List<SubQuestion> subQuestions = new LinkedList<>();

    @Getter
    @Setter
    public static class SubQuestion implements Serializable {
        private static final long serialVersionUID = 2473073723953427111L;
        private String qid;
        private int index;
        private int subIndex;
        private double standardScore;
    }
}
