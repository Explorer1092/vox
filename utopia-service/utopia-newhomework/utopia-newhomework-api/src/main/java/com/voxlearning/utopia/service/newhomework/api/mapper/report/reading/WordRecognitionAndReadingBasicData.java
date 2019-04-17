package com.voxlearning.utopia.service.newhomework.api.mapper.report.reading;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class WordRecognitionAndReadingBasicData implements Serializable {

    private static final long serialVersionUID = -4901240065511313359L;
    private String lessonName;
    private String questionBoxId;
    private boolean standard;
    private List<String> voices = new LinkedList<>();
    private String totalDuration;
    private String standardStr;//达标比例： 2/9字达标
    private List<User> users=new LinkedList<>();
    private Integer wordNum;//生字数目
    private List<WordRecognitionAndReadingDetail> detailList=new LinkedList<>();

    @Getter
    @Setter
    public static class User implements Serializable  {
        private Long userId;
        private String userName;
        private List<String> voices = new LinkedList<>();
        private boolean standard;
        private String duration;
    }

}
