package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/21
 * \* Time: 下午5:34
 * \* Description: 有分数的配音
 * \
 */
@Setter
@Getter
public class DubbingWithScoreAppPart implements Serializable {

    private static final long serialVersionUID = 859478585071627732L;
    private String dubbingId;
    private String dubbingName;
    private String videoUrl;
    private List<DubbingWithScoreAppPart.DubbingScoreAppUser> users = new LinkedList<>();

    @Setter
    @Getter
    public static class DubbingScoreAppUser implements Serializable {

        private static final long serialVersionUID = 5824193345376635966L;
        private Long userId;
        private String userName;
        private Integer durationTime;
        private String duration;
        private Integer score;
        private String scoreStr;
        private Date finishedAt;
        private String studentVideoUrl;
        private boolean syntheticSuccess = true;
    }
}

