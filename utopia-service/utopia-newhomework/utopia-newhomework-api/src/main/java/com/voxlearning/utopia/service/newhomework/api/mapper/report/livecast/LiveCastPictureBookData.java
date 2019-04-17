package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Setter
@Getter
public class LiveCastPictureBookData implements Serializable {
    private static final long serialVersionUID = -5575341764580365745L;
    private int sumScore;
    private long sumDuration;
    private int avgDuration;
    private String time;
    private int avgScore;
    private int finishedCount;
    private int totalUserNum;
    private String pictureBookId;
    private Map<String, Object> pictureInfo = new HashMap<>();
    private List<LiveCastStudentAchievement> studentInfo = new LinkedList<>();
    private boolean flag;

    @Setter
    @Getter
    public static class LiveCastStudentAchievement implements Serializable {
        private static final long serialVersionUID = -3636754035415928361L;
        private long userId;
        private String userName;
        private double score;
        private String time;
        private int duration;
        private Date finishAt;
    }
}
