package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class MentalTypePart implements Serializable {
    private static final long serialVersionUID = -761312848186918886L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean hasFinishUser;
    private int tapType = 3;
    private int averScore;
    private long averDuration;
    private boolean showUrl;
    private String url;
    private boolean showScore;
    private String subContent;
    private List<Point> tabs = new LinkedList<>();
    private List<CalculationStudent> calculationStudents = new LinkedList<>();

    @Setter
    @Getter
    public static class Point implements Serializable {
        private static final long serialVersionUID = 8125497758361312780L;
        private String kid;
        private String tabName;
        private int rightNum;
        private int rightRate;
        private String tabValue;
        private boolean showUrl;
        private String url;
        private int num;
    }

    @Getter
    @Setter
    public static class CalculationStudent implements Serializable {
        private static final long serialVersionUID = -2853155693014708604L;
        private String imageUrl;
        private Long userId;
        private String userName;
        private int score;
        private int duration;
        private String scoreStr;
        private String durationStr;
    }

}
