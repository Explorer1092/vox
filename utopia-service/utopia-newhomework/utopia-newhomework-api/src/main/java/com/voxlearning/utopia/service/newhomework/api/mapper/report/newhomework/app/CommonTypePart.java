package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CommonTypePart implements Serializable {
    private static final long serialVersionUID = 9130483906866007022L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean showUrl;
    private String url;
    private int tapType = 0;
    private boolean hasFinishUser;
    private int averScore;
    private long averDuration;
    private boolean showScore;
    private String subContent;
}