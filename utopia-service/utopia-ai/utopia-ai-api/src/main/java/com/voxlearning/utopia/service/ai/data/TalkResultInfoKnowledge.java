package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TalkResultInfoKnowledge implements Serializable {
    private static final long serialVersionUID = 331205302893296441L;
    private List<TalkResultInfoKnowledgeSentence> sentences;
}
