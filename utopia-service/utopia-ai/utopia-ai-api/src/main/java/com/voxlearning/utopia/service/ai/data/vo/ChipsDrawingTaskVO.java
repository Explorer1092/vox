package com.voxlearning.utopia.service.ai.data.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChipsDrawingTaskVO implements Serializable {
    private static final long serialVersionUID = -2530754344589299463L;
    private String image;
    private Boolean gain;
}
