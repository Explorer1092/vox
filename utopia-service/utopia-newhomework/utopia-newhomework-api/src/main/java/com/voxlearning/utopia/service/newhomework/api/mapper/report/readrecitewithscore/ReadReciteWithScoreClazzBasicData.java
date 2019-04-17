package com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ReadReciteWithScoreClazzBasicData implements Serializable {
    private static final long serialVersionUID = 4592842761670562915L;
    private String lessonName = "";
    private String lessonId;
    private String questionBoxId;
    private List<User> users = new LinkedList<>();
    private List<Integer> paragraphs = new LinkedList<>();

    @Getter
    @Setter
    public static class User implements Serializable {
        private static final long serialVersionUID = 7989607462172540249L;
        private Long userId;
        private String userName;
        private List<String> voices = new LinkedList<>();
        private boolean standard;
        private String durationStr;
    }
}
