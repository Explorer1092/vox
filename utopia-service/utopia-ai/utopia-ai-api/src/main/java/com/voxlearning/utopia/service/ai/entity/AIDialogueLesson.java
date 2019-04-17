package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AIDialogueLesson implements Serializable {
    private static final long serialVersionUID = 9110189000231373092L;
    private String translation;
    private String cnTranslation;
    private String tip;
    private List<String> picture;
    private String video;
    private String firstFrame;
    private String roleImage;
    private String audio;
    private String feedback;
    private String level;
}
