package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TalkResultInfoData implements Serializable {
    private static final long serialVersionUID = -3774820519019568531L;
    private TalkResultInfoContent content;
    private TalkResultInfoHelp help;
    private TalkResultInfoKnowledge knowledge;
    private String status;
    private String name;
}
