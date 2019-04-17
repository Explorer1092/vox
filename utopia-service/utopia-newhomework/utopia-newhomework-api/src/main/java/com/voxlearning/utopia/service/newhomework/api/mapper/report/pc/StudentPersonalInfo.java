package com.voxlearning.utopia.service.newhomework.api.mapper.report.pc;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class StudentPersonalInfo implements Serializable {
    private static final long serialVersionUID = 3923837530519663126L;
    private boolean begin;
    private boolean repair;
    private boolean finished;
    private Long userId;
    private String userName;
    private long avgScore;
    private String avgScoreStr = "--";
    private String finishTimeStr = "未完成";
    private Long finishTime = Long.MAX_VALUE;
    private String durationStr = "--";
    private int duration = Integer.MAX_VALUE;
    private String comment;
    private String audioComment;
    private String correctInfo = "无需";
    private List<String> typeInformation = new LinkedList<>();

}
