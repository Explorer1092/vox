package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CrmAudioNewhomework implements Serializable {
    private static final long serialVersionUID = -3881679979737452463L;

    private String startTime;

    private String finishedTime;

    private String homeworkId;

    private int score;

}
