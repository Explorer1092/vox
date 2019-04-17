package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class LiveCastHomeworkDetail implements Serializable {
    private static final long serialVersionUID = 8127458718552715415L;
    private String homeworkId;
    private Long groupId;
    private int userCount;
    private int finishedCount;
    private int checkedCount;
    private boolean needCheck;
    private boolean hasSubjective;
    private long correctCount;
}
