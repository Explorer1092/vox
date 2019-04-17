package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AITaskLesson implements Serializable {
    private static final long serialVersionUID = 881866884830764385L;
    private String translation;
    private String cnTranslation;
    private String tip;
    private List<String> picture;
    private String video;
    private String audio;
    private String feedback;
    private String level;
}
