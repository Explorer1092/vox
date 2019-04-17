package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class LiveLessonData implements Serializable {
    private static final long serialVersionUID = -7114850073173482009L;

    private String lessonId;
    private List<LiveCategoryData> categories = new LinkedList<>();
    private boolean flag;


    @Getter
    @Setter
    public static class LiveCategoryData implements Serializable {

        private static final long serialVersionUID = -5360000814829232646L;
        private Integer categoryId;
        private String lessonId;
        private String categoryName;
        private Long userId;
        private String userName;
        private int practiceCategory;
        private int averageScore;
        private List<String> voiceUrls = new LinkedList<>();
        private String voiceScoringMode;
        private boolean flag;
    }


}
