package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TalkResultInfoKnowledgeSentence implements Serializable {
    private static final long serialVersionUID = 331205302893296441L;
    private String sentence;
    private String sentence_audio;
}
