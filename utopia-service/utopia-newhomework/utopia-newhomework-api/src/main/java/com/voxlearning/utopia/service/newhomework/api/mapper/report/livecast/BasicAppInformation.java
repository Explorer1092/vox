package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class BasicAppInformation implements Serializable {
    private static final long serialVersionUID = -4710484051715375743L;
    private String unitName;
    private String unitId;
    private List<Lesson> lessons = new LinkedList<>();


    @Setter
    @Getter
    public static class Lesson implements Serializable {
        private static final long serialVersionUID = -1254155009966587288L;
        private String lessonId;
        private String lessonName;
        private List<Category> categories = new LinkedList<>();
    }

    @Setter
    @Getter
    public static class Category implements Serializable {
        private static final long serialVersionUID = -809386476666794984L;
        private int averageScore;
        private Integer categoryId;
        private String categoryName;
        private int practiceCategory;
    }

}
