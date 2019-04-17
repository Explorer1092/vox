package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/12/4
 * \* Time: 4:51 PM
 * \* Description:
 * \
 */
@Setter
@Getter
public class OralCommunicationAppTypePart  implements Serializable {
    private static final long serialVersionUID = -8448807837312029451L;
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
    private List<OralCommunicationScorePart> tabs = new LinkedList<>();

    @Setter
    @Getter
    public static class OralCommunicationScorePart implements Serializable{
        private static final long serialVersionUID = -8177289575570030439L;
        private String stoneDataId;
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
