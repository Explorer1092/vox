package com.voxlearning.utopia.entity.task.week;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClazzDetail implements java.io.Serializable {

    private Long clazzId;
    private List<HomeworkDetail> homeworkList = new ArrayList<>();

    @Data
    public static class HomeworkDetail implements java.io.Serializable {
        private String homeworkId;
        private Integer finishNum;
        private Integer integralNum;
        private Integer expNum;
    }
}