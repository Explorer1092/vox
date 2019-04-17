package com.voxlearning.utopia.service.newhomework.api.mapper.report.pc;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CalculationStudent implements Serializable {

    private static final long serialVersionUID = 224977422717329291L;
    private String imageUrl;
    private Long userId;
    private String userName;
    private int score;
    private int duration;
    private String durationStr;
}
