package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/11/6
 */

@Getter
@Setter
public class DubbingAppPart implements Serializable {

    private static final long serialVersionUID = -4455989547307204538L;

    private String dubbingId;
    private String dubbingName;
    private String videoUrl;
    private List<DubbingAppUser> users = new LinkedList<>();

    @Setter
    @Getter
    public static class DubbingAppUser implements Serializable {

        private static final long serialVersionUID = -4037361896736044943L;

        private Long userId;
        private String userName;
        private Integer durationTime;
        private String duration;
        private Date finishedAt;
        private String studentVideoUrl;
        private boolean syntheticSuccess = true;
    }
}
