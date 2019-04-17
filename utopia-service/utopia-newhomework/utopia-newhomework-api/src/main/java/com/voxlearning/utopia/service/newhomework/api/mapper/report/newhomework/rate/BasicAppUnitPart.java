package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BasicAppUnitPart implements Serializable {
    private static final long serialVersionUID = 5480183051718620045L;
    private String unitName;
    private String unitId;
    private List<BasicAppLessonPart> lessons = new LinkedList<>();
    private Map<String, BasicAppLessonPart> lessonPartMap = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class BasicAppLessonPart implements Serializable {
        private static final long serialVersionUID = -3834457422802333048L;
        private String lessonId;
        private String lessonName;
        private List<BasicAppCategoryPart> categories = new LinkedList<>();
        private Map<Integer, BasicAppCategoryPart> categoryPartMap = new LinkedHashMap<>();
    }

    @Getter
    @Setter
    public static class BasicAppCategoryPart implements Serializable {
        private static final long serialVersionUID = 5897062492139908209L;
        private Integer categoryId;
        private String categoryName;
        private int practiceCategory;
        private double totalScore;
        private int num;
        private int averageScore;
        private String homeworkId;


        //个人信息

        private Long userId; //用户ID
        private String userName; //用户名称
        private List<String> voiceUrls;//音频地址
        private String voiceScoringMode;
        private boolean finished;

    }
}
