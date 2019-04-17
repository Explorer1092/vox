package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/21
 * \* Time: 上午11:41
 * \* Description:有分数的趣味配音
 * \
 */
@Setter
@Getter
public class DubbingWithScoreAppTypePart implements Serializable {

    private static final long serialVersionUID = -9052916076334895863L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean hasFinishUser;
    private Integer tapType = 4;
    private Long averDuration;
    private Integer avgScore;
    private boolean showUrl;
    private String url;
    private boolean showScore;
    private String subContent;
    private List<DubbingWithScoreAppTypePart.DubbingScorePart> tabs = new LinkedList<>();

    @Setter
    @Getter
    public static class DubbingScorePart implements Serializable {
        private static final long serialVersionUID = 4106273375368028791L;
        private String dubbingId;
        private String tabName;
        private Long totalDuration = 0L;
        private Double totalScore = 0d;
        private Integer num;
        private String url;
        private boolean showUrl = true;
        private String tabValue;
        private Long avgDuration = 0L;
        private Integer avgScore = 0;
    }
}
