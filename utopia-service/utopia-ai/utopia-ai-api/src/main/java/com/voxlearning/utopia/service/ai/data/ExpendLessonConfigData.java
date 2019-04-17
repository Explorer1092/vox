package com.voxlearning.utopia.service.ai.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExpendLessonConfigData implements Serializable {
    private static final long serialVersionUID = 8895412304098289020L;
    private String id;
    private String title;
    private List<ExpendLessonConfigJsgf> jsgfList;


    @Data
    public static class ExpendLessonConfigJsgf implements Serializable{
        private static final long serialVersionUID = -5632926927064855465L;
        private String level;
        private String jsgf;
        private List<String> data;
    }
}



