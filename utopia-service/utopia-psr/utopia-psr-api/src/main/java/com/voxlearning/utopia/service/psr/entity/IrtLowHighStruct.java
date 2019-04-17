package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

@Data
@Deprecated
public class IrtLowHighStruct {

    private String tr;
    private double lowv;
    private double highv;

    public IrtLowHighStruct() {
        set("", 0.0, 0.0);
    }

    public void set(String tr, double lowv, double highv) {
        this.tr = tr;
        this.lowv = lowv;
        this.highv = highv;
    }

    public void set(double lowv, double highv) {
        set("", lowv, highv);
    }
}

