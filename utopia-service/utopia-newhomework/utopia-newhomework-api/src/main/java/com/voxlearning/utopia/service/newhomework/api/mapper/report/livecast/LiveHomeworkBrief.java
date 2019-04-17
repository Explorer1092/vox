package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class LiveHomeworkBrief implements Serializable {
    private static final long serialVersionUID = 6581008059421217615L;
    private String homeworkId;
    private Long clazzGroupId;
    private Date endTime;
    private Date startTime;
    private long finishedCount;
    private int userCount;
    private Subject subject;
}
