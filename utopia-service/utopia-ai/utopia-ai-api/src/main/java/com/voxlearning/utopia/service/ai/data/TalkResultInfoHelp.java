package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TalkResultInfoHelp implements Serializable {
    private static final long serialVersionUID = -7274228820198577017L;
    private String help_audio;
    private String help_cn;
    private String help_en;
}
