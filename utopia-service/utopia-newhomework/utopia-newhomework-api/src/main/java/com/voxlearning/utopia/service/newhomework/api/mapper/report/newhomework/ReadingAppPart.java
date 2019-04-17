package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ReadingAppPart implements Serializable {
    private static final long serialVersionUID = 9091870255777693808L;
    private String pictureBookId;
    private String pictureBookName;
    private boolean containsDubbing;
    private List<ReadingAppUser> users = new LinkedList<>();

    @Getter
    @Setter
    public static class ReadingAppUser implements Serializable {
        private static final long serialVersionUID = -3728229662026527039L;
        private Long uid;
        private String userName;
        private double score;
        private String dubbingId;
        private String dubbingScoreLevel;
    }
}
