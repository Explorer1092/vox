package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/11/3
 */

@Setter
@Getter
public class DubbingAppTypePart implements Serializable {

    private static final long serialVersionUID = -7365834874389484804L;

    private ObjectiveConfigType type;
    private String typeName;
    private boolean hasFinishUser;
    private Integer tapType = 4;
    private Long averDuration;
    private boolean showUrl;
    private String url;
    private boolean showScore;
    private String subContent;
    private List<DubbingPart> tabs = new LinkedList<>();

    @Setter
    @Getter
    public static class DubbingPart implements Serializable {

        private static final long serialVersionUID = -6073840487685659478L;

        private String dubbingId;
        private String tabName;
        private Long totalDuration;
        private Integer num;
        private String url;
        private boolean showUrl = true;
        private String tabValue;
        private Long avgDuration;
    }
}
