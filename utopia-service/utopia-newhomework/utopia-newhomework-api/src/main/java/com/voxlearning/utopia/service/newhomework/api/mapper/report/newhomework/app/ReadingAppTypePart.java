package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ReadingAppTypePart implements Serializable {
    private static final long serialVersionUID = -4098335549831600054L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean hasFinishUser;
    private int tapType = 4;
    private int averScore;
    private long averDuration;
    private boolean showUrl;
    private String url;
    private boolean showScore;
    private String subContent;
    private List<Book> tabs = new LinkedList<>();

    @Getter
    @Setter
    public static class Book implements Serializable {
        private static final long serialVersionUID = -187076917657992196L;
        private String readingId;
        private String tabName;
        private long totalDuration;
        private double totalScore;
        private int num;
        private String url;
        private boolean showUrl = true;
        private String tabValue;
        private int averScore;
        private long averDuration;
    }
}
