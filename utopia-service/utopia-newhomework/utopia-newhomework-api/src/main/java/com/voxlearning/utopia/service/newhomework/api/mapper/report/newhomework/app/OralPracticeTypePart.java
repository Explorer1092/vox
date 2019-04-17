
package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class OralPracticeTypePart implements Serializable {
    private static final long serialVersionUID = -3899475673419011214L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean showUrl;
    private String url;
    private int tapType = 2;
    private boolean hasFinishUser;
    private int averScore;
    private long averDuration;
    private boolean showScore;
    private String subContent;
    private List<OralPracticeTypePart.Question> question = new LinkedList<>();

    @Getter
    @Setter
    public static class Question implements Serializable {
        private static final long serialVersionUID = 5109966486235721501L;
        private String qid;
        private int index;
        private boolean showUrl = true;
        private String url;
        private int num;
        private double totalScore;
        private int averScore;
        private int rate;
        private boolean scoreType = true;

    }
}
