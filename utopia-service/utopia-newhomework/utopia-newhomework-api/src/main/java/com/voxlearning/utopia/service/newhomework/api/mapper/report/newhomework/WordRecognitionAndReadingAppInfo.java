package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class WordRecognitionAndReadingAppInfo implements Serializable {

    private static final long serialVersionUID = -8286492413004682439L;
    private String lessonId;
    private String questionBoxId;
    private String lessonName = "";
    private long standardNum=0;


}
