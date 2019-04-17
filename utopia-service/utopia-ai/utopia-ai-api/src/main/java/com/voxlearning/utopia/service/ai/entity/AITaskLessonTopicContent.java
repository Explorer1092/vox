package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AITaskLessonTopicContent extends AITaskLesson {
    private static final long serialVersionUID = -8555398976555063854L;
    private String warning;
    private String warningAudio;
}
