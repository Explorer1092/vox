package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate;

import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class StudentPersonalInfo implements Serializable {
    private static final long serialVersionUID = 2851340183923766692L;
    private String userName;
    private Long userId;
    private List<String> voiceUrls;
    private String voiceScoringMode;
    private double score;
    private AppOralScoreLevel appOralScoreLevel;
}
