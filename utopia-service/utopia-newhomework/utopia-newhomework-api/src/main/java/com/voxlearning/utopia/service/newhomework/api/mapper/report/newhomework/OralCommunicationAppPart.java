package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/12/10
 * \* Time: 3:11 PM
 * \* Description:口语交际
 * \
 */
@Setter
@Getter
public class OralCommunicationAppPart implements Serializable {
    private static final long serialVersionUID = 7532050703550196072L;
    private String stoneId;
    private String topicName;
    private List<OralCommunicationAppUser> users = new ArrayList<>();

    @Setter
    @Getter
    public static class OralCommunicationAppUser implements Serializable {
        private static final long serialVersionUID = -4882611012828394258L;
        private Long userId;
        private String userName;
        private Integer durationTime;
        private String duration;
        private Integer score;
        private String scoreStr;
        private Date finishedAt;
    }
}
