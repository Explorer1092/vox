package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BasicAppStudentDetail implements Serializable {
    private static final long serialVersionUID = 8850824572497093608L;
    private long userId;
    private String userName;
    private int score;
    private String voiceScoringMode = "Normal";
    private List<String> voiceUrls = new LinkedList<>();
    private List<QuestionDetail> questionDetailList = new LinkedList<>();
    private Map<String,QuestionDetail> questionDetailMap = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class QuestionDetail implements Serializable {
        private static final long serialVersionUID = 4007845199841850440L;
        private List<Map<String, Object>> sentences;
        private AppOralScoreLevel appOralScoreLevel;
        public String voiceScoringMode;
        private List<String> voiceUrls = new LinkedList<>();
    }
}
