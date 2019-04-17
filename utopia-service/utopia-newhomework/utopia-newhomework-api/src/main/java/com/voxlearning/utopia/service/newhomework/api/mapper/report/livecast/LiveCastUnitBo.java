package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class LiveCastUnitBo implements Serializable {
    private static final long serialVersionUID = -6714889395657590505L;
    private String unitName = "";
    private String unitId;
    private boolean flag;
    private List<LiveCastLessonBo> lessons = new LinkedList<>();


    @Setter
    @Getter
    public static class LiveCastLessonBo implements Serializable {
        private static final long serialVersionUID = 1600106409693451560L;
        private String lessonId;
        private String lessonName = "";
        private String unitId;
        private List<LiveLessonData.LiveCategoryData> categories = new LinkedList<>();
    }


}
