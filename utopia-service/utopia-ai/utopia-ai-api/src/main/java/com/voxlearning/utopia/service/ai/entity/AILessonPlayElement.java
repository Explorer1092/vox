package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AILessonPlayElement implements Serializable {
    private static final long serialVersionUID = 3298809754010778030L;
    private String roleName;
    private String original;
    private String translation;
    private String media;
}
