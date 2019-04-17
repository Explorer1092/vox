package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

@Data
public class IrtLowHighStructEx {

    private String key;
    private double value;

    public IrtLowHighStructEx() {
        this.key = "";
        this.value = 0.0;
    }
}
