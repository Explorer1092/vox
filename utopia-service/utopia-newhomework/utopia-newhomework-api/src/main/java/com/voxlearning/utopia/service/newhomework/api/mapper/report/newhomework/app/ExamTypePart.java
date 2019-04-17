package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ExamTypePart implements Serializable {
    private static final long serialVersionUID = 8839166562059132015L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean showUrl;
    private String url;
    private int tapType = 2;
    private boolean hasFinishUser;
    private int averScore;
    private boolean showScore;
    private String subContent;
    private long averDuration;
    private List<Question> question = new LinkedList<>();

    @Getter
    @Setter
    public static class Question implements Serializable {
        private static final long serialVersionUID = -1242949275871462103L;
        private String qid;
        private int num;
        private boolean showUrl = true;
        private String url;
        private int rightNum;
        private int index;
        private int rate;
        private boolean scoreType = false;

    }
}
