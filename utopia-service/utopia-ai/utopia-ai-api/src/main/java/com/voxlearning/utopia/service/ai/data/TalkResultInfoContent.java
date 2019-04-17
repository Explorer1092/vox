package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TalkResultInfoContent implements Serializable {
    private static final long serialVersionUID = 331205302893296441L;
    private String name;
    private String audio;
    private String video;
    private String cn_translation;
    private String translation;
    private String role_image;
    private String level;
}
