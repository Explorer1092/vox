package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class PersonalDetail implements Serializable {
    private static final long serialVersionUID = -5712194835907689675L;
    private Long userId;
    private String userName;
    private int score;
    private List<String> voiceUrls;
    private String voiceScoringMode;
}
