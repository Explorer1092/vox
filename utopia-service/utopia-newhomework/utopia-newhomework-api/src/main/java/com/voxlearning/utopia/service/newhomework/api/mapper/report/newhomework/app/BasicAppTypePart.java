
package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


@Getter
@Setter
public class BasicAppTypePart implements Serializable {
    private static final long serialVersionUID = 1292564314881320466L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean hasFinishUser;
    private boolean showUrl;
    private String url;
    private int tapType = 1;
    private int averScore;
    private boolean showScore;
    private String subContent;
    private long averDuration;
    private List<Unit> units = new LinkedList<>();

    @Getter
    @Setter
    public static class Unit implements Serializable {
        private static final long serialVersionUID = 898161458112373915L;
        private String unitId;
        private String unitName;
        private List<Lesson> lessons = new LinkedList<>();
    }


    @Getter
    @Setter
    public static class Lesson implements Serializable {
        private static final long serialVersionUID = 1181450142568104954L;
        private String lessonId;
        private String lessonName;
        private List<Category> tabs = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class Category implements Serializable {
        private static final long serialVersionUID = -4349251335143404803L;
        private Integer categoryId;
        private String categoryName;
        private String tabName;
        private String tabValue;
        private int averScore;
        private String lessonId;
        private String key;
        private String url;
        private boolean showUrl = true;
        private double totalScore;
        private int num;
    }

}